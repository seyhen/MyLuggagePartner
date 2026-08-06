package com.myluggagepartner.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myluggagepartner.app.model.Trip
import com.myluggagepartner.app.model.TripTemplate
import com.myluggagepartner.app.ui.theme.*
import java.time.LocalDate

private fun countdownLabel(trip: Trip): String? {
    val epoch = trip.departureDateEpoch ?: return null
    val today = LocalDate.now().toEpochDay()
    val diff = epoch - today
    return when {
        diff < 0 -> null
        diff == 0L -> "Aujourd'hui"
        diff == 1L -> "Demain"
        diff <= 7 -> "J-$diff"
        else -> null
    }
}

@Composable
fun HomeScreen(
    trips: List<Trip>,
    templates: List<TripTemplate>,
    canCreate: Boolean,
    onOpenTrip: (Long) -> Unit,
    onCreate: () -> Unit,
    onUseTemplate: (Long) -> Unit,
    onDeleteTemplate: (Long) -> Unit,
    onLimitReached: () -> Unit,
    onSettings: () -> Unit,
) {
    val c = AppTheme.colors
    val today = LocalDate.now().toEpochDay()
    val sortedTrips = remember(trips) {
        trips.sortedWith(compareBy<Trip> { trip ->
            val epoch = trip.departureDateEpoch ?: Long.MAX_VALUE
            if (epoch >= today) 0 else 1
        }.thenBy { trip ->
            trip.departureDateEpoch ?: Long.MAX_VALUE
        })
    }
    // Le prochain voyage porte le liseré — c'est l'enveloppe en tête de pile.
    val featuredTripId = sortedTrips.firstOrNull()?.id

    Box(Modifier.fillMaxSize().background(c.surface).windowInsetsPadding(WindowInsets.safeDrawing)) {
        Column(Modifier.fillMaxSize()) {
            // En-tête : cartouche d'expéditeur
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(Radius.xs))
                        .background(c.primary),
                    contentAlignment = Alignment.Center,
                ) { Text("M", style = TitleCard, color = c.onPrimary, fontSize = 18.sp) }
                Spacer(Modifier.width(12.dp))
                Text("MYLUGGAGEPARTNER", style = LabelStamp, color = c.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                NavIconButton(onClick = onSettings) {
                    Icon(Icons.Default.Settings, "Paramètres", tint = c.onSurface, modifier = Modifier.size(19.dp))
                }
            }

            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 116.dp),
            ) {
                item {
                    Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 20.dp)) {
                        Text("MES VALISES", style = DisplayLarge, color = c.onSurface)
                        Spacer(Modifier.height(12.dp))
                        FormRule()
                        Spacer(Modifier.height(12.dp))
                        Text(
                            if (sortedTrips.isEmpty()) "Aucune valise en préparation"
                            else "${sortedTrips.size} valise${if (sortedTrips.size > 1) "s" else ""} en préparation",
                            style = LabelStamp, color = c.onSurfaceVariant,
                        )
                    }
                }

                if (sortedTrips.isEmpty()) {
                    item { EmptyState() }
                } else {
                    items(sortedTrips, key = { it.id }) { trip ->
                        Box(Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                            if (trip.id == featuredTripId) FeaturedTripCard(trip) { onOpenTrip(trip.id) }
                            else FlatTripCard(trip) { onOpenTrip(trip.id) }
                        }
                    }
                }

                if (templates.isNotEmpty()) {
                    item {
                        Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 26.dp, bottom = 10.dp)) {
                            Text("MODÈLES ENREGISTRÉS", style = LabelStamp, color = c.onSurfaceVariant)
                            Spacer(Modifier.height(8.dp))
                            FormRule()
                        }
                    }
                    items(templates, key = { it.id }) { tpl ->
                        TemplateCard(tpl, onUse = { onUseTemplate(tpl.id) }, onDelete = { onDeleteTemplate(tpl.id) })
                    }
                }
            }
        }

        // Bouton d'affranchissement
        Row(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .clip(RoundedCornerShape(Radius.sm))
                .background(c.primary)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = c.onPrimary),
                    onClick = { if (canCreate) onCreate() else onLimitReached() },
                )
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Add, null, tint = c.onPrimary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("NOUVELLE VALISE", style = LabelStamp, color = c.onPrimary)
        }
    }
}

/** Timbre affranchi : trigramme du type de voyage dans un cadre à filet. */
@Composable
private fun TypeStamp(
    code: String,
    size: androidx.compose.ui.unit.Dp,
    fg: Color,
    border: Color,
    bg: Color,
) {
    Box(
        Modifier
            .size(size)
            .clip(RoundedCornerShape(Radius.xs))
            .background(bg)
            .border(1.5.dp, border, RoundedCornerShape(Radius.xs)),
        contentAlignment = Alignment.Center,
    ) {
        Text(code, style = DataMono, color = fg, fontSize = (size.value * 0.34f).sp)
    }
}

/** L'enveloppe en tête de pile : liseré complet en haut, grand format. */
@Composable
private fun FeaturedTripCard(trip: Trip, onClick: () -> Unit) {
    val c = AppTheme.colors
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .border(1.dp, c.outline, RoundedCornerShape(Radius.md))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick,
            )
            .semantics(mergeDescendants = true) {},
    ) {
        AirmailBand(height = 9.dp)
        Box(
            Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 188.dp)
                .background(Brush.linearGradient(trip.type.gradient())),
        ) {
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        0f to Color(0x4D0A1420), 0.3f to Color(0x730A1420),
                        0.7f to Color(0x730A1420), 1f to Color(0xA60A1420),
                    ),
                ),
            )
            Column(Modifier.fillMaxWidth().padding(18.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    TypeStamp(trip.type.code, 46.dp, Color.White, Color(0x8CFFFFFF), Color(0x26FFFFFF))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(trip.type.label.uppercase(), style = LabelStamp, color = Color(0xE6FFFFFF))
                        if (trip.dates.isNotBlank()) {
                            Spacer(Modifier.height(3.dp))
                            Text(trip.dates, style = DataMono, color = Color(0xB3FFFFFF), fontSize = 11.sp)
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    countdownLabel(trip)?.let { label ->
                        Chip(label, Color(0xFFD8232A), Color.White)
                    }
                }

                // Aperçu du contenu — ce qui reste à préparer, pour donner une raison d'ouvrir.
                val pending = remember(trip.items) { trip.items.filterNot { it.checked }.take(3) }
                Spacer(Modifier.height(22.dp))
                if (pending.isNotEmpty()) {
                    Text("À VÉRIFIER", style = LabelStamp, color = Color(0x99FFFFFF))
                    Spacer(Modifier.height(8.dp))
                    pending.forEach { item ->
                        Text(
                            "·  ${item.name}",
                            color = Color(0xE6FFFFFF), fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 2.dp),
                        )
                    }
                } else {
                    Text("TOUT EST PRÊT", style = LabelStamp, color = Color(0xFFD8232A))
                }

                Spacer(Modifier.height(20.dp))
                Text(trip.name, style = DisplayMedium, color = Color.White)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProgressBar(
                        trip.progress, Modifier.weight(1f), 10.dp,
                        track = Color(0x40FFFFFF), fill = Color.White,
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("${trip.done}/${trip.total}", style = DataMono, color = Color.White, fontSize = 13.sp)
                }
            }
        }
    }
}

/** Les autres envois : ligne de bordereau compacte. */
@Composable
private fun FlatTripCard(trip: Trip, onClick: () -> Unit) {
    val c = AppTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(c.surfaceContainer)
            .border(1.dp, c.outline, RoundedCornerShape(Radius.md))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick,
            )
            .semantics(mergeDescendants = true) {}
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TypeStamp(trip.type.code, 46.dp, c.primary, c.outline, c.surface)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    trip.name, style = TitleCard, color = c.onSurface,
                    modifier = Modifier.weight(1f, fill = false),
                )
                countdownLabel(trip)?.let { label ->
                    Spacer(Modifier.width(8.dp))
                    Chip(label, c.reminder.copy(alpha = 0.14f), c.reminder)
                }
            }
            Spacer(Modifier.height(3.dp))
            Text(
                if (trip.dates.isNotBlank()) "${trip.dates} · ${trip.type.label}" else trip.type.label,
                color = c.onSurfaceVariant, fontSize = 13.sp,
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProgressBar(trip.progress, Modifier.weight(1f), 7.dp, track = c.surfaceContainerLow, fill = c.primary)
                Spacer(Modifier.width(10.dp))
                Text("${trip.done}/${trip.total}", style = DataMono, color = c.primary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun TemplateCard(tpl: TripTemplate, onUse: () -> Unit, onDelete: () -> Unit) {
    val c = AppTheme.colors
    Row(
        Modifier
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.sm))
            .background(c.surfaceContainer)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onUse,
            )
            .padding(start = 14.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TypeStamp(tpl.type.code, 36.dp, c.onSurfaceVariant, c.outline, c.surface)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(tpl.name, style = CategoryTitle, color = c.onSurface)
            Spacer(Modifier.height(2.dp))
            Text("${tpl.items.size} objets · ${tpl.type.label}", style = LabelStamp, color = c.onSurfaceVariant)
        }
        Box(
            Modifier.size(48.dp).clip(RoundedCornerShape(Radius.xs)).clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 22.dp),
                onClick = onDelete,
            ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Close, "Supprimer le modèle « ${tpl.name} »",
                tint = c.onSurfaceVariant, modifier = Modifier.size(17.dp),
            )
        }
    }
}

@Composable
private fun EmptyState() {
    val c = AppTheme.colors
    Column(
        Modifier.fillMaxWidth().padding(top = 60.dp, start = 32.dp, end = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(Radius.sm))
                .border(1.5.dp, c.outline, RoundedCornerShape(Radius.sm)),
            contentAlignment = Alignment.Center,
        ) {
            Text("?", style = DisplayMedium, color = c.outline, fontSize = 42.sp)
        }
        Spacer(Modifier.height(22.dp))
        Text("PREMIÈRE VALISE", style = TitleCard, color = c.onSurface, fontSize = 22.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            "Trois questions, une liste prête au départ.",
            color = c.onSurfaceVariant, fontSize = 14.sp, textAlign = TextAlign.Center,
        )
    }
}
