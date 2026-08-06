package com.myluggagepartner.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myluggagepartner.app.ui.theme.AppTheme
import com.myluggagepartner.app.ui.theme.BodyFamily
import com.myluggagepartner.app.ui.theme.LabelStamp
import com.myluggagepartner.app.ui.theme.Radius
import com.myluggagepartner.app.ui.theme.airmailStripe

@Composable
fun MaterialThemeBody(): TextStyle =
    TextStyle(fontFamily = BodyFamily, fontSize = 14.sp)

/** Étiquette collée : rectangle franc, texte en capitales machine. */
@Composable
fun Chip(text: String, bg: Color, fg: Color) {
    Row(
        Modifier.clip(RoundedCornerShape(Radius.xs)).background(bg).padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) { Text(text.uppercase(), style = LabelStamp, color = fg) }
}

/**
 * Progression = le liseré par avion qui gagne du terrain sur la bande.
 * Remplissage plein et uni : un niveau doit se lire d'un coup d'œil.
 * Le motif rayé est réservé au liseré de l'enveloppe — jamais ici.
 */
@Composable
fun ProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    track: Color,
    fill: Color,
) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), label = "prog")
    Box(
        modifier
            .height(height)
            .clip(RoundedCornerShape(Radius.xs))
            .background(track),
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(animated)
                .clip(RoundedCornerShape(Radius.xs))
                .background(fill),
        )
    }
}

/** Bouton de navigation : carré à angles cassés, comme une case de bordereau. */
@Composable
fun NavIconButton(
    onClick: () -> Unit,
    bg: Color = AppTheme.colors.surfaceContainer,
    content: @Composable () -> Unit,
) {
    Box(
        Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(Radius.sm))
            .background(bg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) { content() }
}

/** Filet horizontal fin — sépare les blocs comme les lignes d'un formulaire. */
@Composable
fun FormRule(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(1.dp).background(AppTheme.colors.outline))
}

/** Bandeau liseré par avion — la signature, en tête des surfaces importantes. */
@Composable
fun AirmailBand(modifier: Modifier = Modifier, height: Dp = 8.dp) {
    Box(modifier.fillMaxWidth().height(height).background(airmailStripe(period = 30f)))
}

/** Encadré à filet : le conteneur de base, façon case de formulaire. */
@Composable
fun FormBox(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = AppTheme.colors
    Column(
        modifier
            .clip(RoundedCornerShape(Radius.md))
            .background(c.surfaceContainer)
            .border(1.dp, c.outline, RoundedCornerShape(Radius.md)),
        content = content,
    )
}
