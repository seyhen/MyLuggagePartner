package com.myluggagepartner.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.ZoneId
import com.myluggagepartner.app.Draft
import com.myluggagepartner.app.model.Intensity
import com.myluggagepartner.app.model.TripType
import com.myluggagepartner.app.ui.AppSwitch
import com.myluggagepartner.app.ui.QtyStepper
import com.myluggagepartner.app.ui.SegmentedButton
import com.myluggagepartner.app.ui.theme.*

@Composable
fun CreateScreen(
    step: Int,
    draft: Draft,
    onStep: (Int) -> Unit,
    onDraft: ((Draft) -> Draft) -> Unit,
    onCancel: () -> Unit,
    onFinish: () -> Unit,
) {
    val c = AppTheme.colors
    val titles = mapOf(
        1 to ("NATURE DU VOYAGE" to "Section 1 sur 3 — obligatoire"),
        2 to ("DESTINATION ET DATES" to "Section 2 sur 3 — facultatif"),
        3 to ("DÉTAILS DU CONTENU" to "Section 3 sur 3 — facultatif"),
    )

    Box(Modifier.fillMaxSize().background(c.surface).windowInsetsPadding(WindowInsets.safeDrawing)) {
        Column(Modifier.fillMaxSize()) {
            // Bandeau de formulaire
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NavIconButton(onClick = onCancel) {
                    Icon(Icons.Default.Close, "Fermer", tint = c.onSurface, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.weight(1f))
                // Progression du formulaire : trois cases à remplir
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    (1..3).forEach { s ->
                        Box(
                            Modifier
                                .size(width = 26.dp, height = 7.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(if (s <= step) c.primary else c.surfaceContainerLow)
                                .border(1.dp, if (s <= step) c.primary else c.outline, RoundedCornerShape(1.dp)),
                        )
                    }
                }
            }

            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            ) {
                Spacer(Modifier.height(10.dp))
                Text(titles[step]!!.first, style = TitleStep, color = c.onSurface)
                Spacer(Modifier.height(8.dp))
                Text(titles[step]!!.second, style = LabelStamp, color = c.onSurfaceVariant)
                Spacer(Modifier.height(14.dp))
                FormRule()
                Spacer(Modifier.height(24.dp))

                when (step) {
                    1 -> StepType(draft, onDraft)
                    2 -> StepDestination(draft, onDraft)
                    3 -> StepOptions(draft, onDraft)
                }
                Spacer(Modifier.height(140.dp))
            }
        }

        // Pied de formulaire
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(c.surface)
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            val secondaryLabel = when {
                step == 1 && draft.type != null -> "COMPLÉTER LES SECTIONS SUIVANTES"
                step == 2 -> "SECTION SUIVANTE"
                else -> null
            }
            if (secondaryLabel != null) {
                Box(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .defaultMinSize(minHeight = 44.dp)
                        .clip(RoundedCornerShape(Radius.xs))
                        .clickable { onStep(step + 1) }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) { Text(secondaryLabel, style = LabelStamp, color = c.primary) }
                Spacer(Modifier.height(8.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .clip(RoundedCornerShape(Radius.xs))
                        .clickable { if (step > 1) onStep(step - 1) else onCancel() }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(if (step > 1) "RETOUR" else "ANNULER", style = LabelStamp, color = c.onSurfaceVariant)
                }
                Spacer(Modifier.weight(1f))
                CtaButton("GÉNÉRER LA LISTE", enabled = draft.type != null, fill = step == 3) { onFinish() }
            }
        }
    }
}

@Composable
private fun CtaButton(label: String, enabled: Boolean, fill: Boolean, onClick: () -> Unit) {
    val c = AppTheme.colors
    Row(
        Modifier
            .then(if (fill) Modifier.fillMaxWidth() else Modifier)
            .clip(RoundedCornerShape(Radius.sm))
            .background(if (enabled) c.primary else c.surfaceContainerLow)
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 24.dp, vertical = 17.dp),
        horizontalArrangement = Arrangement.Center,
    ) { Text(label, style = LabelStamp, color = if (enabled) c.onPrimary else c.onSurfaceVariant, fontSize = 12.sp) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StepDestination(draft: Draft, onDraft: ((Draft) -> Draft) -> Unit) {
    val c = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Field("Destination", draft.destination, "Ex. Lisbonne") { v -> onDraft { it.copy(destination = v) } }
        Text(
            "Les dates servent à calculer les quantités. Tout reste modifiable ensuite.",
            color = c.onSurfaceVariant, fontSize = 13.sp, lineHeight = 20.sp,
        )

        var showPicker by rememberSaveable { mutableStateOf(false) }
        val dateLabel = if (draft.from != null && draft.to != null) {
            "${draft.from.dayOfMonth}/${draft.from.monthValue} — ${draft.to.dayOfMonth}/${draft.to.monthValue}/${draft.to.year}"
        } else "Sélectionner les dates"

        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text("DATES", style = LabelStamp, color = c.primary)
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(Radius.xs))
                    .background(c.surfaceContainer).border(1.5.dp, c.outline, RoundedCornerShape(Radius.xs))
                    .clickable { showPicker = !showPicker }
                    .padding(horizontal = 14.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.CalendarToday, null, tint = c.onSurfaceVariant, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    dateLabel,
                    style = if (draft.from != null) DataMono else LocalTextStyle.current,
                    color = if (draft.from != null) c.onSurface else c.onSurfaceVariant,
                    fontSize = 15.sp,
                )
            }
        }

        if (showPicker) {
            val pickerState = rememberDateRangePickerState(
                initialSelectedStartDateMillis = draft.from?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
                initialSelectedEndDateMillis = draft.to?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
            )

            LaunchedEffect(pickerState.selectedStartDateMillis, pickerState.selectedEndDateMillis) {
                val startMs = pickerState.selectedStartDateMillis
                val endMs = pickerState.selectedEndDateMillis
                if (startMs != null && endMs != null) {
                    val from = Instant.ofEpochMilli(startMs).atZone(ZoneId.systemDefault()).toLocalDate()
                    val to = Instant.ofEpochMilli(endMs).atZone(ZoneId.systemDefault()).toLocalDate()
                    onDraft { it.copy(from = from, to = to) }
                }
            }

            DateRangePicker(
                state = pickerState,
                title = null,
                headline = null,
                showModeToggle = false,
                colors = DatePickerDefaults.colors(
                    containerColor = c.surfaceContainer,
                    dayContentColor = c.onSurface,
                    selectedDayContainerColor = c.primary,
                    selectedDayContentColor = c.onPrimary,
                    todayContentColor = c.primary,
                    todayDateBorderColor = c.primary,
                ),
                modifier = Modifier.fillMaxWidth().height(400.dp)
                    .clip(RoundedCornerShape(Radius.md)),
            )
        }
    }
}

@Composable
private fun Field(label: String, value: String, placeholder: String, onChange: (String) -> Unit) {
    val c = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(label.uppercase(), style = LabelStamp, color = c.primary)
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(Radius.xs))
                .background(c.surfaceContainer).border(1.5.dp, c.outline, RoundedCornerShape(Radius.xs))
                .padding(horizontal = 14.dp, vertical = 16.dp),
        ) {
            if (value.isEmpty()) Text(placeholder, color = c.onSurfaceVariant, fontSize = 15.sp)
            BasicTextField(
                value = value, onValueChange = onChange,
                textStyle = LocalTextStyle.current.copy(color = c.onSurface, fontSize = 15.sp),
                cursorBrush = SolidColor(c.primary),
                singleLine = true,
            )
        }
    }
}

/** Planche de timbres : on choisit le trigramme du voyage. */
@Composable
private fun StepType(draft: Draft, onDraft: ((Draft) -> Draft) -> Unit) {
    val c = AppTheme.colors
    val types = TripType.entries
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        types.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { type ->
                    val sel = draft.type == type
                    Box(
                        Modifier.weight(1f)
                            .clip(RoundedCornerShape(Radius.sm))
                            .background(if (sel) c.primaryContainer else c.surfaceContainer)
                            .border(
                                if (sel) 2.dp else 1.dp,
                                if (sel) c.primary else c.outline,
                                RoundedCornerShape(Radius.sm),
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(),
                                onClick = { onDraft { it.copy(type = type) } },
                            )
                            .padding(20.dp),
                    ) {
                        Column {
                            Box(
                                Modifier
                                    .size(58.dp)
                                    .clip(RoundedCornerShape(Radius.xs))
                                    .background(if (sel) c.primary else c.surface)
                                    .border(1.5.dp, if (sel) c.primary else c.outline, RoundedCornerShape(Radius.xs)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    type.code, style = DataMono, fontSize = 15.sp,
                                    color = if (sel) c.onPrimary else c.onSurfaceVariant,
                                )
                            }
                            Spacer(Modifier.height(14.dp))
                            Text(type.label.uppercase(), style = CategoryTitle, color = c.onSurface, fontSize = 13.sp)
                        }
                        if (sel) Box(
                            Modifier.align(Alignment.TopEnd).size(22.dp)
                                .clip(RoundedCornerShape(Radius.xs)).background(c.primary),
                            contentAlignment = Alignment.Center,
                        ) { Icon(Icons.Default.Check, null, tint = c.onPrimary, modifier = Modifier.size(13.dp)) }
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StepOptions(draft: Draft, onDraft: ((Draft) -> Draft) -> Unit) {
    val c = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("VOLUME DE BAGAGE", style = LabelStamp, color = c.primary)
            SegmentedButton(
                options = Intensity.entries.map { it to it.label },
                selected = draft.intensity,
                onSelect = { v -> onDraft { it.copy(intensity = v) } },
            )
        }
        FormRule()
        ToggleRow("Accès à une machine à laver", "Réduit les quantités de vêtements", draft.laundry) { v -> onDraft { it.copy(laundry = v) } }
        FormRule()
        ToggleRow("Voyage avec enfants", "Ajoute les indispensables", draft.kids) { v -> onDraft { it.copy(kids = v) } }
        FormRule()
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Voyageurs", color = c.onSurface, fontSize = 15.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                Spacer(Modifier.height(3.dp))
                Text("Ajuste les quantités partagées", color = c.onSurfaceVariant, fontSize = 13.sp)
            }
            QtyStepper(draft.travelers, onDelta = { d -> onDraft { it.copy(travelers = (it.travelers + d).coerceAtLeast(1)) } }, big = true)
        }
    }
}

@Composable
private fun ToggleRow(name: String, sub: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    val c = AppTheme.colors
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(name, color = c.onSurface, fontSize = 15.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
            Spacer(Modifier.height(3.dp))
            Text(sub, color = c.onSurfaceVariant, fontSize = 13.sp)
        }
        Spacer(Modifier.width(12.dp))
        AppSwitch(checked) { onToggle(!checked) }
    }
}
