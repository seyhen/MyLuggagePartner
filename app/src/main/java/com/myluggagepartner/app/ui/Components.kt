package com.myluggagepartner.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myluggagepartner.app.ui.theme.AppTheme

/** Forme blob organique (border-radius asymétrique de la maquette). */
fun blobShape(): Shape = GenericShape { size, _ ->
    val w = size.width; val h = size.height
    // Approximation d'un blob doux via courbes de Bézier
    moveTo(w * 0.5f, 0f)
    cubicTo(w * 0.82f, 0f, w, h * 0.20f, w, h * 0.48f)
    cubicTo(w, h * 0.78f, w * 0.80f, h, w * 0.50f, h)
    cubicTo(w * 0.22f, h, 0f, h * 0.80f, 0f, h * 0.52f)
    cubicTo(0f, h * 0.22f, w * 0.20f, 0f, w * 0.5f, 0f)
    close()
}

@Composable
fun AppCheckbox(checked: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    val bg by animateColorAsState(if (checked) c.primary else Color.Transparent, label = "ckBg")
    val border by animateColorAsState(if (checked) c.primary else c.onSurfaceVariant, label = "ckBd")
    val desc = if (checked) "Coché" else "Non coché"
    Box(
        modifier
            .size(28.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(bg)
            .border(2.dp, border, RoundedCornerShape(7.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 20.dp),
                onClick = onToggle,
            )
            .semantics { contentDescription = desc },
        contentAlignment = Alignment.Center,
    ) {
        if (checked) Icon(Icons.Default.Check, null, tint = c.onPrimary, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun AppSwitch(checked: Boolean, onToggle: () -> Unit) {
    val c = AppTheme.colors
    val trackBg by animateColorAsState(if (checked) c.primary else c.surfaceContainerLow, label = "swTrack")
    val trackBorder by animateColorAsState(if (checked) c.primary else c.onSurfaceVariant, label = "swBorder")
    val thumbOffset by animateDpAsState(if (checked) 26.dp else 4.dp, label = "swThumb")
    val thumbColor by animateColorAsState(if (checked) c.onPrimary else c.onSurfaceVariant, label = "swThumbC")
    Box(
        Modifier
            .width(54.dp).height(32.dp)
            .clip(CircleShape)
            .background(trackBg)
            .border(2.dp, trackBorder, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle,
            ),
    ) {
        Box(
            Modifier
                .padding(start = thumbOffset)
                .align(Alignment.CenterStart)
                .size(20.dp)
                .clip(CircleShape)
                .background(thumbColor),
        )
    }
}

@Composable
fun <T> SegmentedButton(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
) {
    val c = AppTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .border(1.5.dp, c.outline, CircleShape),
    ) {
        options.forEachIndexed { i, (value, label) ->
            val isSel = value == selected
            val bg by animateColorAsState(if (isSel) c.surfaceContainer else Color.Transparent, label = "segBg")
            if (i > 0) Box(Modifier.width(1.5.dp).height(46.dp).background(c.outline))
            Row(
                Modifier
                    .weight(1f)
                    .background(bg)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(),
                        onClick = { onSelect(value) },
                    )
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isSel) {
                    Text("✓ ", color = c.primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    label,
                    color = if (isSel) c.onSurface else c.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.SemiBold,
                )
            }
        }
    }
}

/** Stepper quantité en pilule (− valeur +). */
@Composable
fun QtyStepper(
    qty: Int,
    onDelta: (Int) -> Unit,
    big: Boolean = false,
) {
    val c = AppTheme.colors
    val btn = if (big) 36.dp else 34.dp
    Row(
        Modifier
            .clip(CircleShape)
            .background(if (big) c.surfaceContainer else c.surface)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepBtn("−", if (qty > 1) c.onSurfaceVariant else c.onSurfaceVariant.copy(alpha = 0.3f), btn) {
            if (qty > 1) onDelta(-1)
        }
        Text(
            "$qty",
            fontWeight = FontWeight.ExtraBold,
            fontSize = if (big) 16.sp else 14.sp,
            color = c.onSurface,
            modifier = Modifier.widthIn(min = if (big) 28.dp else 20.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        StepBtn("+", c.primary, btn) { onDelta(1) }
    }
}

@Composable
private fun StepBtn(label: String, color: Color, size: androidx.compose.ui.unit.Dp, onClick: () -> Unit) {
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = size / 2),
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = color, fontSize = if (size > 30.dp) 18.sp else 16.sp, fontWeight = FontWeight.Bold)
    }
}
