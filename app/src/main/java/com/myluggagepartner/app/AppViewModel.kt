package com.myluggagepartner.app

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.myluggagepartner.app.data.AppDatabase
import com.myluggagepartner.app.data.PackItemEntity
import com.myluggagepartner.app.data.TemplateEntity
import com.myluggagepartner.app.data.TemplateItemEntity
import com.myluggagepartner.app.data.TripEntity
import com.myluggagepartner.app.model.Category
import com.myluggagepartner.app.model.GenParams
import com.myluggagepartner.app.model.Intensity
import com.myluggagepartner.app.model.PackItem
import com.myluggagepartner.app.model.Trip
import com.myluggagepartner.app.model.TripTemplate
import com.myluggagepartner.app.model.TripType
import com.myluggagepartner.app.model.generateItems
import com.myluggagepartner.app.model.newId
import com.myluggagepartner.app.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private val Application.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
private val KEY_THEME = stringPreferencesKey("theme")
private val KEY_PREMIUM = booleanPreferencesKey("premium")
private val KEY_ONBOARDED = booleanPreferencesKey("onboarded")

/** Nombre de valises offertes avant le premium. */
const val FREE_TRIP_LIMIT = 5

data class Draft(
    val destination: String = "",
    val from: LocalDate? = null,
    val to: LocalDate? = null,
    val type: TripType? = null,
    val intensity: Intensity = Intensity.NORMAL,
    val laundry: Boolean = false,
    val kids: Boolean = false,
    val travelers: Int = 1,
)

data class UiState(
    val trips: List<Trip> = emptyList(),
    val templates: List<TripTemplate> = emptyList(),
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val premium: Boolean = false,
    val draft: Draft = Draft(),
    val step: Int = 1,
    val loaded: Boolean = false,
    val onboarded: Boolean = false,
)

class AppViewModel(private val app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).dao()
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var prefsReady = false
    private var tripsReady = false

    init {
        viewModelScope.launch {
            val prefs = app.dataStore.data.first()
            val theme = prefs[KEY_THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.LIGHT
            val premium = prefs[KEY_PREMIUM] ?: false
            val onboarded = prefs[KEY_ONBOARDED] ?: false
            prefsReady = true
            _state.value = _state.value.copy(themeMode = theme, premium = premium, onboarded = onboarded, loaded = tripsReady)
        }
        viewModelScope.launch {
            dao.allTrips().collect { tripEntities ->
                val trips = tripEntities.map { entity ->
                    val items = dao.itemsForTripOnce(entity.id).map { it.toDomain() }
                    entity.toDomain(items)
                }
                tripsReady = true
                _state.value = _state.value.copy(trips = trips, loaded = prefsReady)
            }
        }
        viewModelScope.launch {
            dao.allTemplates().collect { tplEntities ->
                val templates = tplEntities.mapNotNull { entity ->
                    val type = runCatching { TripType.valueOf(entity.type) }.getOrNull() ?: return@mapNotNull null
                    val items = dao.templateItems(entity.id).mapNotNull {
                        val cat = runCatching { Category.valueOf(it.category) }.getOrNull() ?: return@mapNotNull null
                        cat to it.name
                    }
                    TripTemplate(entity.id, entity.name, type, items)
                }
                _state.value = _state.value.copy(templates = templates)
            }
        }
    }

    fun tripById(id: Long): Trip? = _state.value.trips.find { it.id == id }

    /* ——— Thème / premium ——— */
    fun setTheme(mode: ThemeMode) {
        _state.value = _state.value.copy(themeMode = mode)
        viewModelScope.launch { app.dataStore.edit { it[KEY_THEME] = mode.name } }
    }
    fun unlockPremium() {
        _state.value = _state.value.copy(premium = true)
        viewModelScope.launch { app.dataStore.edit { it[KEY_PREMIUM] = true } }
    }
    fun completeOnboarding() {
        _state.value = _state.value.copy(onboarded = true)
        viewModelScope.launch { app.dataStore.edit { it[KEY_ONBOARDED] = true } }
    }
    fun canCreate(): Boolean = _state.value.premium || _state.value.trips.size < FREE_TRIP_LIMIT

    /* ——— Flow de création ——— */
    fun resetDraft() = update { it.copy(draft = Draft(), step = 1) }
    fun setStep(s: Int) = update { it.copy(step = s) }
    fun updateDraft(block: (Draft) -> Draft) = update { it.copy(draft = block(it.draft)) }

    fun finishCreate(): Long {
        val d = _state.value.draft
        val days = if (d.from != null && d.to != null)
            (d.to.toEpochDay() - d.from.toEpochDay()).toInt().coerceAtLeast(1) else 5
        val dates = if (d.from != null && d.to != null) "${fmt(d.from)} – ${fmt(d.to)}" else "$days jours"
        val type = d.type ?: TripType.VILLE
        val tripId = newId()
        val items = generateItems(GenParams(type, d.intensity, days, d.laundry, d.kids, d.travelers))
        val departureEpoch = d.from?.toEpochDay()
        val trip = Trip(
            id = tripId,
            name = d.destination.trim().ifBlank { "Nouveau voyage" },
            destination = d.destination.trim(),
            dates = dates,
            type = type,
            hasPhoto = false,
            items = items,
            departureDateEpoch = departureEpoch,
        )
        update { it.copy(trips = listOf(trip) + it.trips) }
        viewModelScope.launch {
            dao.insertTripWithItems(
                trip.toEntity(),
                items.mapIndexed { i, item -> item.toEntity(tripId, i) },
            )
        }
        if (departureEpoch != null) {
            ReminderScheduler.schedule(app, tripId, trip.name, departureEpoch)
        }
        return tripId
    }

    /* ——— Mutations d'items ——— */
    fun toggleItem(tripId: Long, itemId: Long) {
        val item = tripById(tripId)?.items?.find { it.id == itemId } ?: return
        viewModelScope.launch { dao.setItemChecked(itemId, !item.checked) }
        mutateLocal(tripId) { t ->
            t.copy(items = t.items.map { if (it.id == itemId) it.copy(checked = !it.checked) else it })
        }
    }
    fun changeQty(tripId: Long, itemId: Long, delta: Int) {
        val item = tripById(tripId)?.items?.find { it.id == itemId } ?: return
        val newQty = (item.qty + delta).coerceIn(1, 99)
        viewModelScope.launch { dao.setItemQty(itemId, newQty) }
        mutateLocal(tripId) { t ->
            t.copy(items = t.items.map { if (it.id == itemId) it.copy(qty = newQty) else it })
        }
    }
    fun addItem(tripId: Long, category: Category, name: String) {
        val id = newId()
        val trip = tripById(tripId) ?: return
        viewModelScope.launch { dao.insertItem(PackItemEntity(id, tripId, category.name, name.trim(), 1, false, trip.items.size)) }
        mutateLocal(tripId) { t ->
            t.copy(items = t.items + PackItem(id, category, name.trim(), 1, false))
        }
    }
    fun removeItem(tripId: Long, itemId: Long) {
        viewModelScope.launch { dao.deleteItem(itemId) }
        mutateLocal(tripId) { t -> t.copy(items = t.items.filterNot { it.id == itemId }) }
    }
    fun restoreItem(tripId: Long, item: PackItem) {
        val trip = tripById(tripId) ?: return
        viewModelScope.launch { dao.insertItem(item.toEntity(tripId, trip.items.size)) }
        mutateLocal(tripId) { t -> t.copy(items = t.items + item) }
    }

    /* ——— Mutations de voyages ——— */
    fun renameTrip(tripId: Long, name: String) {
        val trimmed = name.trim().ifBlank { return }
        viewModelScope.launch { dao.renameTrip(tripId, trimmed) }
        mutateLocal(tripId) { t -> t.copy(name = trimmed) }
    }
    fun duplicateTrip(tripId: Long) {
        val t = tripById(tripId) ?: return
        val newTripId = newId()
        val newItems = t.items.map { it.copy(id = newId()) }
        val copy = t.copy(id = newTripId, name = "${t.name} (copie)", items = newItems)
        update { it.copy(trips = listOf(copy) + it.trips) }
        viewModelScope.launch {
            dao.insertTripWithItems(
                copy.toEntity(),
                newItems.mapIndexed { i, item -> item.toEntity(newTripId, i) },
            )
        }
    }
    fun deleteTrip(tripId: Long) {
        ReminderScheduler.cancel(app, tripId)
        viewModelScope.launch { dao.deleteTrip(tripId) }
        update { it.copy(trips = it.trips.filterNot { tr -> tr.id == tripId }) }
    }

    fun resetChecks(tripId: Long) {
        viewModelScope.launch { dao.resetChecks(tripId) }
        mutateLocal(tripId) { t -> t.copy(items = t.items.map { it.copy(checked = false) }) }
    }

    /* ——— Templates ——— */
    fun saveAsTemplate(tripId: Long): String {
        val t = tripById(tripId) ?: return ""
        val tplId = newId()
        viewModelScope.launch {
            dao.insertTemplateWithItems(
                TemplateEntity(tplId, t.name, t.type.name),
                t.items.map { TemplateItemEntity(templateId = tplId, category = it.category.name, name = it.name) },
            )
        }
        return t.name
    }

    fun deleteTemplate(templateId: Long) {
        viewModelScope.launch { dao.deleteTemplate(templateId) }
        update { it.copy(templates = it.templates.filterNot { tpl -> tpl.id == templateId }) }
    }

    fun createFromTemplate(templateId: Long): Long {
        val tpl = _state.value.templates.find { it.id == templateId } ?: return -1
        val tripId = newId()
        val items = tpl.items.map { (cat, name) -> PackItem(newId(), cat, name) }
        val trip = Trip(tripId, "${tpl.name} (modèle)", tpl.name, tpl.type.label, tpl.type, false, items)
        update { it.copy(trips = listOf(trip) + it.trips) }
        viewModelScope.launch {
            dao.insertTripWithItems(
                trip.toEntity(),
                items.mapIndexed { i, item -> item.toEntity(tripId, i) },
            )
        }
        return tripId
    }

    /* ——— Helpers ——— */
    private fun mutateLocal(tripId: Long, block: (Trip) -> Trip) = update { s ->
        s.copy(trips = s.trips.map { if (it.id == tripId) block(it) else it })
    }
    private inline fun update(block: (UiState) -> UiState) { _state.value = block(_state.value) }

    private fun fmt(d: LocalDate): String {
        val month = d.month.getDisplayName(TextStyle.SHORT, Locale.FRENCH).trimEnd('.')
        return "${d.dayOfMonth} $month"
    }
}

/* ——— Mappers ——— */
private fun TripEntity.toDomain(items: List<PackItem>) = Trip(
    id = id, name = name, destination = destination, dates = dates,
    type = TripType.valueOf(type), hasPhoto = hasPhoto, items = items,
    departureDateEpoch = departureDateEpoch,
)

private fun PackItemEntity.toDomain() = PackItem(
    id = id, category = Category.valueOf(category), name = name, qty = qty, checked = checked,
)

private fun Trip.toEntity() = TripEntity(
    id = id, name = name, destination = destination, dates = dates,
    type = type.name, hasPhoto = hasPhoto, departureDateEpoch = departureDateEpoch,
)

private fun PackItem.toEntity(tripId: Long, sortOrder: Int) = PackItemEntity(
    id = id, tripId = tripId, category = category.name, name = name,
    qty = qty, checked = checked, sortOrder = sortOrder,
)
