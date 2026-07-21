package com.myluggagepartner.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myluggagepartner.app.ui.theme.AppTheme
import com.myluggagepartner.app.ui.theme.BodyFamily

@Composable
fun MaterialThemeBody(): TextStyle =
    TextStyle(fontFamily = BodyFamily, fontSize = 14.sp)

@Composable
fun Chip(text: String, bg: Color, fg: Color) {
    Row(
        Modifier.clip(CircleShape).background(bg).padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) { Text(text, color = fg, fontSize = 12.5.sp, fontWeight = FontWeight.Bold) }
}

@Composable
fun ProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    track: Color,
    fill: Color,
) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), label = "prog")
    Box(modifier.height(height).clip(CircleShape).background(track)) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(animated).clip(CircleShape).background(fill))
    }
}

@Composable
fun CircleIconButton(
    onClick: () -> Unit,
    bg: Color = AppTheme.colors.surfaceContainer,
    content: @Composable () -> Unit,
) {
    Box(
        Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 24.dp),
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) { content() }
}
