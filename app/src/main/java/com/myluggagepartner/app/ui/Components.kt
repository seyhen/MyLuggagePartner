package com.myluggagepartner.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myluggagepartner.app.ui.theme.AppTheme
import com.myluggagepartner.app.ui.theme.DataMono
import com.myluggagepartner.app.ui.theme.LabelStamp
import com.myluggagepartner.app.ui.theme.Radius

/** Zone tactile minimale recommandée (48dp) — le visuel peut rester plus petit. */
private val MinTouchTarget = 48.dp

/**
 * Case de formulaire postal : carrée, bord franc, oblitérée d'une croix
 * quand l'objet est dans la valise.
 */
@Composable
fun AppCheckbox(checked: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    val bg by animateColorAsState(if (checked) c.primary else Color.Transparent, label = "ckBg")
    val border by animateColorAsState(if (checked) c.primary else c.onSurfaceVariant, label = "ckBd")
    val desc = if (checked) "Coché" else "Non coché"
    Box(
        modifier
            .size(MinTouchTarget)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 24.dp),
                onClick = onToggle,
            )
            .semantics { contentDescription = desc },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(Radius.xs))
                .background(bg)
                .border(1.5.dp, border, RoundedCornerShape(Radius.xs)),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) Icon(Icons.Default.Check, null, tint = c.onPrimary, modifier = Modifier.size(15.dp))
        }
    }
}

/** Interrupteur mécanique : glissière rectangulaire, curseur carré. */
@Composable
fun AppSwitch(checked: Boolean, onToggle: () -> Unit) {
    val c = AppTheme.colors
    val trackBg by animateColorAsState(if (checked) c.primary else c.surfaceContainerLow, label = "swTrack")
    val trackBorder by animateColorAsState(if (checked) c.primary else c.onSurfaceVariant, label = "swBorder")
    val thumbOffset by animateDpAsState(if (checked) 24.dp else 3.dp, label = "swThumb")
    val thumbColor by animateColorAsState(if (checked) c.onPrimary else c.onSurfaceVariant, label = "swThumbC")
    Box(
        Modifier
            .width(50.dp).height(28.dp)
            .clip(RoundedCornerShape(Radius.xs))
            .background(trackBg)
            .border(1.5.dp, trackBorder, RoundedCornerShape(Radius.xs))
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
                .clip(RoundedCornerShape(1.dp))
                .background(thumbColor),
        )
    }
}

/** Onglets de formulaire : cases jointives séparées par un filet franc. */
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
            .clip(RoundedCornerShape(Radius.sm))
            .border(1.5.dp, c.outline, RoundedCornerShape(Radius.sm)),
    ) {
        options.forEachIndexed { i, (value, label) ->
            val isSel = value == selected
            val bg by animateColorAsState(if (isSel) c.primary else Color.Transparent, label = "segBg")
            if (i > 0) Box(Modifier.width(1.5.dp).height(44.dp).background(c.outline))
            Box(
                Modifier
                    .weight(1f)
                    .background(bg)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(),
                        onClick = { onSelect(value) },
                    )
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label.uppercase(),
                    style = LabelStamp,
                    color = if (isSel) c.onPrimary else c.onSurfaceVariant,
                )
            }
        }
    }
}

/** Champ quantité de bordereau : − [ 04 ] + , chiffres en machine à écrire. */
@Composable
fun QtyStepper(
    qty: Int,
    onDelta: (Int) -> Unit,
    big: Boolean = false,
) {
    val c = AppTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        StepBtn("−", if (qty > 1) c.onSurface else c.onSurfaceVariant.copy(alpha = 0.35f)) {
            if (qty > 1) onDelta(-1)
        }
        Box(
            Modifier
                .widthIn(min = if (big) 44.dp else 38.dp)
                .clip(RoundedCornerShape(Radius.xs))
                .background(c.surface)
                .border(1.dp, c.outline, RoundedCornerShape(Radius.xs))
                .padding(vertical = 5.dp, horizontal = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                qty.toString().padStart(2, '0'),
                style = DataMono,
                fontSize = if (big) 15.sp else 13.sp,
                color = c.onSurface,
                textAlign = TextAlign.Center,
            )
        }
        StepBtn("+", c.primary) { onDelta(1) }
    }
}

@Composable
private fun StepBtn(label: String, color: Color, onClick: () -> Unit) {
    val desc = if (label == "+") "Augmenter la quantité" else "Diminuer la quantité"
    Box(
        Modifier
            .size(MinTouchTarget)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 20.dp),
                onClick = onClick,
            )
            .semantics { contentDescription = desc },
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = color, fontSize = 19.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
    }
}
