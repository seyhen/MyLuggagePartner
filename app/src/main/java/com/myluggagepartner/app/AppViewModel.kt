package com.myluggagepartner.app

import androidx.lifecycle.ViewModel
import com.myluggagepartner.app.model.*
import com.myluggagepartner.app.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/** Brouillon du flow de création. */
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
    val trips: List<Trip> = demoTrips(),
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val premium: Boolean = false,
    val draft: Draft = Draft(),
    val step: Int = 1,
)

/**
 * État applicatif en mémoire. La persistance (Room / DataStore) est laissée
 * volontairement de côté pour ce premier jet — à brancher dans Claude Code.
 */
class AppViewModel : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    fun tripById(id: Long): Trip? = _state.value.trips.find { it.id == id }

    /* ——— Thème / premium ——— */
    fun setTheme(mode: ThemeMode) = update { it.copy(themeMode = mode) }
    fun unlockPremium() = update { it.copy(premium = true) }
    fun canCreate(): Boolean = _state.value.premium || _state.value.trips.size < 2

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
        val trip = Trip(
            id = newId(),
            name = d.destination.trim().ifBlank { "Nouveau voyage" },
            destination = d.destination.trim(),
            dates = dates,
            type = type,
            hasPhoto = false,
            items = generateItems(GenParams(type, d.intensity, days, d.laundry, d.kids, d.travelers)),
        )
        update { it.copy(trips = listOf(trip) + it.trips) }
        return trip.id
    }

    /* ——— Mutations d'items ——— */
    fun toggleItem(tripId: Long, itemId: Long) = mutateTrip(tripId) { t ->
        t.copy(items = t.items.map { if (it.id == itemId) it.copy(checked = !it.checked) else it })
    }
    fun changeQty(tripId: Long, itemId: Long, delta: Int) = mutateTrip(tripId) { t ->
        t.copy(items = t.items.map { if (it.id == itemId) it.copy(qty = (it.qty + delta).coerceAtLeast(1)) else it })
    }
    fun addItem(tripId: Long, category: Category, name: String) = mutateTrip(tripId) { t ->
        t.copy(items = t.items + PackItem(newId(), category, name.trim(), 1, false))
    }
    fun removeItem(tripId: Long, itemId: Long) = mutateTrip(tripId) { t ->
        t.copy(items = t.items.filterNot { it.id == itemId })
    }
    fun restoreItem(tripId: Long, item: PackItem) = mutateTrip(tripId) { t ->
        t.copy(items = t.items + item)
    }

    /* ——— Mutations de voyages ——— */
    fun renameTrip(tripId: Long, name: String) = mutateTrip(tripId) { t ->
        t.copy(name = name.trim().ifBlank { t.name })
    }
    fun duplicateTrip(tripId: Long) {
        val t = tripById(tripId) ?: return
        val copy = t.copy(id = newId(), name = "${t.name} (copie)", items = t.items.map { it.copy(id = newId()) })
        update { it.copy(trips = listOf(copy) + it.trips) }
    }
    fun deleteTrip(tripId: Long) = update { it.copy(trips = it.trips.filterNot { tr -> tr.id == tripId }) }

    /* ——— Helpers ——— */
    private fun mutateTrip(tripId: Long, block: (Trip) -> Trip) = update { s ->
        s.copy(trips = s.trips.map { if (it.id == tripId) block(it) else it })
    }
    private inline fun update(block: (UiState) -> UiState) { _state.value = block(_state.value) }

    private fun fmt(d: LocalDate): String {
        val month = d.month.getDisplayName(TextStyle.SHORT, Locale.FRENCH).trimEnd('.')
        return "${d.dayOfMonth} $month"
    }
}
