package com.myluggagepartner.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myluggagepartner.app.ui.SegmentedButton
import com.myluggagepartner.app.ui.theme.*

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    premium: Boolean,
    onTheme: (ThemeMode) -> Unit,
    onUnlock: () -> Unit,
    onShare: () -> Unit,
    onBack: () -> Unit,
) {
    val c = AppTheme.colors
    Column(Modifier.fillMaxSize().background(c.surface).windowInsetsPadding(WindowInsets.safeDrawing)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp)) {
            CircleIconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour", tint = c.onSurface, modifier = Modifier.size(20.dp))
            }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Text("Paramètres", style = DisplayLarge, color = c.onSurface)
            Spacer(Modifier.height(28.dp))

            Text("APPARENCE", color = c.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            SegmentedButton(
                options = listOf(ThemeMode.LIGHT to "Clair", ThemeMode.DARK to "Sombre", ThemeMode.AUTO to "Auto"),
                selected = themeMode,
                onSelect = onTheme,
            )

            Spacer(Modifier.height(26.dp))
            // Carte premium
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(c.secondaryContainer).padding(22.dp),
            ) {
                if (!premium) {
                    Text("Illimité, pour toujours", style = TitleCard, color = c.onSecondaryContainer)
                    Spacer(Modifier.height(10.dp))
                    Text("Autant de valises que vous voulez. Pas d'abonnement, pas de compte, pas de pub.",
                        color = c.onSecondaryContainer.copy(alpha = 0.8f), fontSize = 13.sp, lineHeight = 19.sp)
                    Spacer(Modifier.height(14.dp))
                    Row(
                        Modifier.clip(CircleShape).background(c.secondaryCta)
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = ripple(), onClick = onUnlock)
                            .padding(horizontal = 24.dp, vertical = 14.dp),
                    ) { Text("Débloquer · 3,99 €", color = if (c.isDark) c.secondaryContainer else Color(0xFFFBFAF7), fontWeight = FontWeight.Bold, fontSize = 15.sp) }
                } else {
                    Text("Version illimitée ✓", style = TitleCard, color = c.onSecondaryContainer)
                    Spacer(Modifier.height(10.dp))
                    Text("Merci pour votre soutien. Bon voyage !", color = c.onSecondaryContainer.copy(alpha = 0.8f), fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(22.dp))
            // Export / partage
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(c.surfaceContainer)) {
                ExportRow(Icons.Default.Share, "Partager mes listes", onShare)
            }

            Spacer(Modifier.height(32.dp))
            Text("MyLuggagePartner v2.0\nVos listes restent sur votre appareil.",
                color = c.onSurfaceVariant, fontSize = 12.sp, lineHeight = 19.sp,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ExportRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    val c = AppTheme.colors
    Row(
        Modifier.fillMaxWidth()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = ripple(), onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(c.surfaceContainerLow), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = c.onSurface, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(label, color = c.onSurface, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
    }
}
