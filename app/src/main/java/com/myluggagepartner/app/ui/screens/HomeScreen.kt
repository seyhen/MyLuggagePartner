package com.myluggagepartner.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myluggagepartner.app.model.Trip
import com.myluggagepartner.app.ui.blobShape
import com.myluggagepartner.app.ui.theme.*

@Composable
fun HomeScreen(
    trips: List<Trip>,
    canCreate: Boolean,
    onOpenTrip: (Long) -> Unit,
    onCreate: () -> Unit,
    onLimitReached: () -> Unit,
    onSettings: () -> Unit,
) {
    val c = AppTheme.colors
    Box(Modifier.fillMaxSize().background(c.surface)) {
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
                            if (trips.isEmpty()) "Prêt à partir ?"
                            else "${trips.size} voyage${if (trips.size > 1) "s" else ""} en préparation",
                            style = MaterialThemeBody(), color = c.onSurfaceVariant, fontWeight = FontWeight.Medium,
                        )
                    }
                }

                if (trips.isEmpty()) {
                    item { EmptyState() }
                } else {
                    items(trips, key = { it.id }) { trip ->
                        Box(Modifier.padding(horizontal = 20.dp, vertical = 7.dp)) {
                            if (trip.hasPhoto) PhotoTripCard(trip) { onOpenTrip(trip.id) }
                            else FlatTripCard(trip) { onOpenTrip(trip.id) }
                        }
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
            ),
    ) {
        // Scrim dégradé
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to Color(0x59140C08), 0.34f to Color.Transparent,
                    0.45f to Color.Transparent, 1f to Color(0x9E140C08),
                ),
            ),
        )
        Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip("${trip.type.emoji} ${trip.type.label}", Color(0xEBFFFFFF), Color(0xFF241914))
                Chip(trip.dates, Color(0x73140C08), Color(0xE6FFFFFF))
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
            .padding(22.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(56.dp).clip(blobShape()).background(c.surfaceContainer),
            contentAlignment = Alignment.Center,
        ) { Text(trip.type.emoji, fontSize = 26.sp) }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(trip.name, style = TitleCard, color = c.onSurface)
            Spacer(Modifier.height(4.dp))
            Text("${trip.dates} · ${trip.type.label}", color = c.onSurfaceVariant, fontSize = 13.sp, fontWeight = FontWeight.Medium)
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
