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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
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
import com.myluggagepartner.app.FREE_TRIP_LIMIT
import com.myluggagepartner.app.ui.theme.*

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val c = AppTheme.colors
    Column(
        Modifier
            .fillMaxSize()
            .background(c.surface)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(28.dp))

        // Cartouche d'en-tête, façon face d'enveloppe
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.md))
                .background(c.surfaceContainer)
                .border(1.dp, c.outline, RoundedCornerShape(Radius.md)),
        ) {
            AirmailBand(height = 9.dp)
            Column(Modifier.padding(20.dp)) {
                Text("PAR AVION", style = LabelStamp, color = c.reminder)
                Spacer(Modifier.height(10.dp))
                Text("MYLUGGAGE\nPARTNER", style = DisplayLarge, color = c.onSurface, fontSize = 36.sp)
                Spacer(Modifier.height(12.dp))
                Text(
                    "Une liste de bagages complète, prête en quelques secondes.",
                    color = c.onSurfaceVariant, fontSize = 15.sp, lineHeight = 22.sp,
                )
            }
        }

        Spacer(Modifier.height(32.dp))
        Feature(Icons.Default.AutoAwesome, "QUANTITÉS CALCULÉES", "Selon la durée du séjour, le type de voyage et le nombre de voyageurs.")
        Spacer(Modifier.height(4.dp))
        FormRule()
        Spacer(Modifier.height(4.dp))
        Feature(Icons.Default.Edit, "TOUT RESTE MODIFIABLE", "Ajoutez, cochez, retirez. La liste s'adapte à vous, jamais l'inverse.")
        Spacer(Modifier.height(4.dp))
        FormRule()
        Spacer(Modifier.height(4.dp))
        Feature(Icons.Default.Lock, "100 % HORS LIGNE", "Vos listes restent sur votre téléphone. Aucun compte, aucune pub.")

        Spacer(Modifier.height(28.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.sm))
                .background(c.surfaceContainer)
                .border(1.dp, c.outline, RoundedCornerShape(Radius.sm))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("$FREE_TRIP_LIMIT", style = DataMono, color = c.reminder, fontSize = 22.sp)
            Spacer(Modifier.width(14.dp))
            Text(
                "valises gratuites, sans inscription.",
                color = c.onSurface, fontSize = 14.sp,
            )
        }

        Spacer(Modifier.height(28.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.sm))
                .background(c.primary)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = c.onPrimary),
                    onClick = onDone,
                )
                .padding(vertical = 19.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text("CRÉER MA PREMIÈRE VALISE", style = LabelStamp, color = c.onPrimary, fontSize = 12.sp)
        }
        Spacer(Modifier.height(14.dp))
        Text(
            "Aucune donnée envoyée. Prêt en une minute.",
            style = LabelStamp, color = c.onSurfaceVariant,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun Feature(icon: ImageVector, title: String, sub: String) {
    val c = AppTheme.colors
    Row(Modifier.padding(vertical = 14.dp), verticalAlignment = Alignment.Top) {
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(Radius.xs)).background(c.surfaceContainer)
                .border(1.dp, c.outline, RoundedCornerShape(Radius.xs)),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, null, tint = c.primary, modifier = Modifier.size(19.dp)) }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = CategoryTitle, color = c.onSurface)
            Spacer(Modifier.height(5.dp))
            Text(sub, color = c.onSurfaceVariant, fontSize = 13.sp, lineHeight = 19.sp)
        }
    }
}
