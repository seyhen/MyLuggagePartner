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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.LocalDate
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
        1 to ("Quel type de voyage ?" to "L'essentiel — le reste est optionnel"),
        2 to ("Où et quand ?" to "Optionnel · affine les quantités"),
        3 to ("Derniers réglages" to "Optionnel · personnalise la liste"),
    )

    Box(Modifier.fillMaxSize().background(c.surface).windowInsetsPadding(WindowInsets.safeDrawing)) {
        Column(Modifier.fillMaxSize()) {
            // Nav + stepper
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircleIconButton(onClick = onCancel) {
                    Icon(Icons.Default.Close, "Fermer", tint = c.onSurface, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..3).forEach { s ->
                        Box(
                            Modifier.width(42.dp).height(8.dp).clip(CircleShape)
                                .background(c.primary.copy(alpha = if (s <= step) 1f else 0.2f)),
                        )
                    }
                }
            }

            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            ) {
                Spacer(Modifier.height(12.dp))
                Text(titles[step]!!.first, style = TitleStep, color = c.onSurface)
                Spacer(Modifier.height(8.dp))
                Text(titles[step]!!.second, color = c.onSurfaceVariant, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(24.dp))

                when (step) {
                    1 -> StepType(draft, onDraft)
                    2 -> StepDestination(draft, onDraft)
                    3 -> StepOptions(draft, onDraft)
                }
                Spacer(Modifier.height(130.dp))
            }
        }

        // Footer
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 20.dp, vertical = 22.dp),
        ) {
            // Raccourci vers l'étape suivante — le CTA principal génère toujours la liste.
            val secondaryLabel = when {
                step == 1 && draft.type != null -> "Personnaliser (dates, options…)"
                step == 2 -> "Plus d'options (style, voyageurs…)"
                else -> null
            }
            if (secondaryLabel != null) {
                Text(
                    secondaryLabel,
                    color = c.primary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .defaultMinSize(minHeight = 44.dp)
                        .clip(CircleShape)
                        .clickable { onStep(step + 1) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                )
                Spacer(Modifier.height(6.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .clip(CircleShape)
                        .clickable { if (step > 1) onStep(step - 1) else onCancel() }
                        .padding(horizontal = 18.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(if (step > 1) "Retour" else "Annuler", color = c.primary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Spacer(Modifier.weight(1f))
                CtaButton("Générer ma liste", enabled = draft.type != null, fill = step == 3) { onFinish() }
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
            .clip(CircleShape)
            .background(c.primary.copy(alpha = if (enabled) 1f else 0.35f))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 28.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
    ) { Text(label, color = c.onPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StepDestination(draft: Draft, onDraft: ((Draft) -> Draft) -> Unit) {
    val c = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Field("Destination", draft.destination, "Ex. Lisbonne") { v -> onDraft { it.copy(destination = v) } }
        Text(
            "Les dates servent à calculer les quantités. Tout reste modifiable ensuite.",
            color = c.onSurfaceVariant, fontSize = 13.sp, lineHeight = 20.sp,
        )

        var showPicker by remember { mutableStateOf(false) }
        val dateLabel = if (draft.from != null && draft.to != null) {
            "${draft.from.dayOfMonth}/${draft.from.monthValue} — ${draft.to.dayOfMonth}/${draft.to.monthValue}/${draft.to.year}"
        } else "Sélectionner les dates"

        Text("DATES", color = c.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                .background(c.surface).border(1.5.dp, c.outline, RoundedCornerShape(16.dp))
                .clickable { showPicker = !showPicker }
                .padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.CalendarToday, null, tint = c.onSurfaceVariant, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                dateLabel,
                color = if (draft.from != null) c.onSurface else c.onSurfaceVariant,
                fontSize = 16.sp,
            )
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
                    .clip(RoundedCornerShape(24.dp)),
            )
        }
    }
}

@Composable
private fun Field(label: String, value: String, placeholder: String, onChange: (String) -> Unit) {
    val c = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label.uppercase(), color = c.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                .background(c.surface).border(1.5.dp, c.outline, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 15.dp),
        ) {
            if (value.isEmpty()) Text(placeholder, color = c.onSurfaceVariant, fontSize = 16.sp)
            BasicTextField(
                value = value, onValueChange = onChange,
                textStyle = LocalTextStyle.current.copy(color = c.onSurface, fontSize = 16.sp),
                cursorBrush = SolidColor(c.primary),
                singleLine = true,
            )
        }
    }
}

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
                            .clip(RoundedCornerShape(if (sel) 32.dp else 16.dp))
                            .background(if (sel) c.primaryContainer else Color.Transparent)
                            .border(
                                if (sel) 2.dp else 1.5.dp,
                                if (sel) c.primary else c.outline,
                                RoundedCornerShape(if (sel) 32.dp else 16.dp),
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(),
                                onClick = { onDraft { it.copy(type = type) } },
                            )
                            .padding(horizontal = 12.dp, vertical = 20.dp),
                    ) {
                        Column {
                            Text(type.emoji, fontSize = 30.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(type.label, color = c.onSurface, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                        if (sel) Box(
                            Modifier.align(Alignment.TopEnd).size(28.dp).clip(CircleShape).background(c.primary),
                            contentAlignment = Alignment.Center,
                        ) { Icon(Icons.Default.Check, null, tint = c.onPrimary, modifier = Modifier.size(15.dp)) }
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
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("STYLE DE VALISE", color = c.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        SegmentedButton(
            options = Intensity.entries.map { it to it.label },
            selected = draft.intensity,
            onSelect = { v -> onDraft { it.copy(intensity = v) } },
        )
        ToggleRow("Accès à une machine à laver", "Réduit les quantités de vêtements", draft.laundry) { v -> onDraft { it.copy(laundry = v) } }
        ToggleRow("Voyage avec enfants", "Ajoute les indispensables", draft.kids) { v -> onDraft { it.copy(kids = v) } }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Voyageurs", color = c.onSurface, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            QtyStepper(draft.travelers, onDelta = { d -> onDraft { it.copy(travelers = (it.travelers + d).coerceAtLeast(1)) } }, big = true)
        }
    }
}

@Composable
private fun ToggleRow(name: String, sub: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    val c = AppTheme.colors
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(name, color = c.onSurface, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(3.dp))
            Text(sub, color = c.onSurfaceVariant, fontSize = 13.sp)
        }
        AppSwitch(checked) { onToggle(!checked) }
    }
}
