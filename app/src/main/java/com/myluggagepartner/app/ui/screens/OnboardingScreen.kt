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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CardGiftcard
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myluggagepartner.app.FREE_TRIP_LIMIT
import com.myluggagepartner.app.ui.theme.AppTheme
import com.myluggagepartner.app.ui.theme.DisplayLarge

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val c = AppTheme.colors
    Column(
        Modifier
            .fillMaxSize()
            .background(c.surface)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        Box(
            Modifier.size(76.dp).clip(RoundedCornerShape(24.dp)).background(c.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) { Text("🧳", fontSize = 40.sp) }

        Spacer(Modifier.height(24.dp))
        Text("MyLuggagePartner", style = DisplayLarge, color = c.onSurface)
        Spacer(Modifier.height(10.dp))
        Text(
            "Une liste de bagages complète, prête en quelques secondes.",
            color = c.onSurfaceVariant, fontSize = 16.sp, lineHeight = 23.sp, fontWeight = FontWeight.Medium,
        )

        Spacer(Modifier.height(38.dp))
        Feature(Icons.Default.AutoAwesome, "Les quantités, calculées pour vous", "Selon la durée du séjour, le type de voyage et le nombre de voyageurs.")
        Spacer(Modifier.height(20.dp))
        Feature(Icons.Default.Edit, "Tout reste modifiable", "Ajoutez, cochez, retirez. La liste s'adapte à vous, jamais l'inverse.")
        Spacer(Modifier.height(20.dp))
        Feature(Icons.Default.Lock, "100 % hors ligne", "Vos listes restent sur votre téléphone. Aucun compte, aucune pub.")

        Spacer(Modifier.height(36.dp))
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(c.surfaceContainer).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.CardGiftcard, null, tint = c.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(
                "$FREE_TRIP_LIMIT valises gratuites, sans inscription.",
                color = c.onSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(Modifier.height(32.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(CircleShape)
                .background(c.primary)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = c.onPrimary),
                    onClick = onDone,
                )
                .padding(vertical = 18.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text("Créer ma première valise", color = c.onPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(Modifier.height(14.dp))
        Text(
            "Aucune donnée envoyée. Prêt en une minute.",
            color = c.onSurfaceVariant, fontSize = 12.sp, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun Feature(icon: ImageVector, title: String, sub: String) {
    val c = AppTheme.colors
    Row(verticalAlignment = Alignment.Top) {
        Box(
            Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(c.surfaceContainer),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, null, tint = c.primary, modifier = Modifier.size(22.dp)) }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = c.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(sub, color = c.onSurfaceVariant, fontSize = 13.sp, lineHeight = 19.sp)
        }
    }
}
