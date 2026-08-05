package com.myluggagepartner.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TileMode
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myluggagepartner.app.model.Trip
import com.myluggagepartner.app.model.TripTemplate
import com.myluggagepartner.app.ui.blobShape
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
    // Le prochain voyage à venir est toujours mis en avant — c'est la carte qui porte le liseré signature.
    val featuredTripId = sortedTrips.firstOrNull()?.id
    Box(Modifier.fillMaxSize().background(c.surface).windowInsetsPadding(WindowInsets.safeDrawing)) {
        Column(Modifier.fillMaxSize()) {
            // Nav bar custom
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(c.surfaceContainer),
                    contentAlignment = Alignment.Center,
                ) { Text("M", style = TitleCard, color = c.primary, fontSize = 20.sp) }
                Spacer(Modifier.weight(1f))
                CircleIconButton(onClick = onSettings) {
                    Icon(Icons.Default.Settings, "Paramètres", tint = c.onSurface, modifier = Modifier.size(20.dp))
                }
            }

            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 96.dp),
            ) {
                item {
                    Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 18.dp)) {
                        Text("Mes valises", style = DisplayLarge, color = c.onSurface)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (sortedTrips.isEmpty()) "Prêt à partir ?"
                            else "${sortedTrips.size} voyage${if (sortedTrips.size > 1) "s" else ""} en préparation",
                            style = MaterialThemeBody(), color = c.onSurfaceVariant, fontWeight = FontWeight.Medium,
                        )
                    }
                }

                if (sortedTrips.isEmpty()) {
                    item { EmptyState() }
                } else {
                    items(sortedTrips, key = { it.id }) { trip ->
                        Box(Modifier.padding(horizontal = 20.dp, vertical = 7.dp)) {
                            if (trip.id == featuredTripId) PhotoTripCard(trip) { onOpenTrip(trip.id) }
                            else FlatTripCard(trip) { onOpenTrip(trip.id) }
                        }
                    }
                }

                if (templates.isNotEmpty()) {
                    item {
                        Text(
                            "Mes modèles",
                            style = DisplayMedium,
                            color = c.onSurface,
                            modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 10.dp),
                        )
                    }
                    items(templates, key = { it.id }) { tpl ->
                        TemplateCard(tpl, onUse = { onUseTemplate(tpl.id) }, onDelete = { onDeleteTemplate(tpl.id) })
                    }
                }
            }
        }

        // FAB étendu
        Row(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(c.primary)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = c.onPrimary),
                    onClick = { if (canCreate) onCreate() else onLimitReached() },
                )
                .padding(horizontal = 22.dp, vertical = 17.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Add, null, tint = c.onPrimary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Nouvelle valise", color = c.onPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
private fun PhotoTripCard(trip: Trip, onClick: () -> Unit) {
    val c = AppTheme.colors
    Box(
        Modifier
            .fillMaxWidth().height(300.dp)
            .clip(RoundedCornerShape(36.dp))
            .background(Brush.linearGradient(trip.type.gradient()))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick,
            )
            .semantics(mergeDescendants = true) {},
    ) {
        // Liseré « par avion » — signale la valise dont le départ approche.
        Box(
            Modifier
                .fillMaxWidth()
                .height(9.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFBFAF7), Color(0xFFFBFAF7),
                            Color(0xFFD8232A), Color(0xFFD8232A),
                            Color(0xFFFBFAF7), Color(0xFFFBFAF7),
                            Color(0xFF0B5FA5), Color(0xFF0B5FA5),
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(26f, 26f),
                        tileMode = TileMode.Repeated,
                    ),
                ),
        )
        // Scrim dégradé
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to Color(0x590A1420), 0.34f to Color.Transparent,
                    0.45f to Color.Transparent, 1f to Color(0x9E0A1420),
                ),
            ),
        )
        Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip("${trip.type.emoji} ${trip.type.label}", Color(0xEBFFFFFF), Color(0xFF14213D))
                if (trip.dates.isNotBlank()) Chip(trip.dates, Color(0x730A1420), Color(0xE6FFFFFF))
                countdownLabel(trip)?.let { label ->
                    Chip("🔔 $label", Color(0xEBFFFFFF), Color(0xFFD8232A))
                }
            }
            Column {
                Text(trip.name, style = DisplayMedium, color = Color.White)
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProgressBar(trip.progress, Modifier.weight(1f), 12.dp, Color(0x4DFFFFFF), Color.White)
                    Spacer(Modifier.width(10.dp))
                    Text("${trip.done}/${trip.total}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun FlatTripCard(trip: Trip, onClick: () -> Unit) {
    val c = AppTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(36.dp))
            .background(c.surfaceContainerLow)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick,
            )
            .semantics(mergeDescendants = true) {}
            .padding(22.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(56.dp).clip(blobShape()).background(c.surfaceContainer),
            contentAlignment = Alignment.Center,
        ) { Text(trip.type.emoji, fontSize = 26.sp) }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(trip.name, style = TitleCard, color = c.onSurface, modifier = Modifier.weight(1f, fill = false))
                countdownLabel(trip)?.let { label ->
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "🔔 $label", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        color = c.reminder,
                        modifier = Modifier.clip(CircleShape).background(c.reminder.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                if (trip.dates.isNotBlank()) "${trip.dates} · ${trip.type.label}" else trip.type.label,
                color = c.onSurfaceVariant, fontSize = 13.sp, fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProgressBar(trip.progress, Modifier.weight(1f), 8.dp, c.surfaceContainer, c.primary)
                Spacer(Modifier.width(10.dp))
                Text("${trip.done}/${trip.total}", color = c.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun TemplateCard(tpl: TripTemplate, onUse: () -> Unit, onDelete: () -> Unit) {
    val c = AppTheme.colors
    Row(
        Modifier
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(c.surfaceContainerLow)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onUse,
            )
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(c.surfaceContainer),
            contentAlignment = Alignment.Center,
        ) { Text(tpl.type.emoji, fontSize = 22.sp) }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(tpl.name, color = c.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text("${tpl.items.size} objets · ${tpl.type.label}", color = c.onSurfaceVariant, fontSize = 13.sp)
        }
        Box(
            Modifier.size(48.dp).clip(CircleShape).clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 22.dp),
                onClick = onDelete,
            ),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Default.Close, "Supprimer le modèle « ${tpl.name} »", tint = c.onSurfaceVariant, modifier = Modifier.size(18.dp)) }
    }
}

@Composable
private fun EmptyState() {
    val c = AppTheme.colors
    Column(
        Modifier.fillMaxWidth().padding(top = 80.dp, start = 40.dp, end = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("✈️", fontSize = 48.sp)
        Spacer(Modifier.height(20.dp))
        Text("Votre première valise vous attend", style = TitleCard, color = c.onSurface, fontSize = 24.sp)
        Spacer(Modifier.height(10.dp))
        Text("Trois questions, une liste prête. C'est tout.", color = c.onSurfaceVariant, fontSize = 14.sp)
    }
}
