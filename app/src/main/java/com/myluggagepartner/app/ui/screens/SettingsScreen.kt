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
import androidx.compose.ui.graphics.vector.ImageVector
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
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp)) {
            NavIconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour", tint = c.onSurface, modifier = Modifier.size(19.dp))
            }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Text("RÉGLAGES", style = DisplayLarge, color = c.onSurface)
            Spacer(Modifier.height(10.dp))
            AirmailBand(height = 6.dp)
            Spacer(Modifier.height(30.dp))

            Text("APPARENCE", style = LabelStamp, color = c.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
            SegmentedButton(
                options = listOf(ThemeMode.LIGHT to "Clair", ThemeMode.DARK to "Sombre", ThemeMode.AUTO to "Auto"),
                selected = themeMode,
                onSelect = onTheme,
            )

            Spacer(Modifier.height(30.dp))
            // Affranchissement illimité
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.md))
                    .background(c.secondaryContainer)
                    .border(1.dp, c.outline, RoundedCornerShape(Radius.md)),
            ) {
                AirmailBand(height = 6.dp)
                Column(Modifier.padding(20.dp)) {
                    if (!premium) {
                        Text("ILLIMITÉ, POUR TOUJOURS", style = TitleCard, color = c.onSecondaryContainer)
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "Autant de valises que vous voulez. Pas d'abonnement, pas de compte, pas de pub.",
                            color = c.onSecondaryContainer.copy(alpha = 0.85f), fontSize = 13.sp, lineHeight = 19.sp,
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(
                            Modifier.clip(RoundedCornerShape(Radius.sm)).background(c.secondaryCta)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(), onClick = onUnlock,
                                )
                                .padding(horizontal = 20.dp, vertical = 15.dp),
                        ) { Text("DÉBLOQUER · 3,99 €", style = LabelStamp, color = c.onPrimary, fontSize = 12.sp) }
                    } else {
                        Text("VERSION ILLIMITÉE", style = TitleCard, color = c.onSecondaryContainer)
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "Merci pour votre soutien. Bon voyage !",
                            color = c.onSecondaryContainer.copy(alpha = 0.85f), fontSize = 13.sp,
                        )
                    }
                }
            }

            Spacer(Modifier.height(26.dp))
            Text("EXPÉDITION", style = LabelStamp, color = c.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
            FormBox(Modifier.fillMaxWidth()) {
                ExportRow(Icons.Default.Share, "Partager mes listes", onShare)
            }

            Spacer(Modifier.height(36.dp))
            FormRule()
            Spacer(Modifier.height(14.dp))
            Text(
                "MYLUGGAGEPARTNER · V2.0",
                style = LabelStamp, color = c.onSurfaceVariant,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Vos listes restent sur votre appareil.",
                color = c.onSurfaceVariant, fontSize = 12.sp, lineHeight = 18.sp,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun ExportRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    val c = AppTheme.colors
    Row(
        Modifier.fillMaxWidth()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = ripple(), onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(36.dp).clip(RoundedCornerShape(Radius.xs)).background(c.surface)
                .border(1.dp, c.outline, RoundedCornerShape(Radius.xs)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = c.onSurface, modifier = Modifier.size(17.dp))
        }
        Spacer(Modifier.width(14.dp))
        Text(label, color = c.onSurface, fontSize = 15.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, modifier = Modifier.weight(1f))
    }
}
