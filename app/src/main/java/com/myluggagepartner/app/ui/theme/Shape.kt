package com.myluggagepartner.app.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.unit.dp

/**
 * Rayons de papier : on plie, on ne moule pas. Rien au-dessus de 10dp —
 * une enveloppe a des angles, pas des galets.
 */
object Radius {
    val xs = 2.dp   // champs de saisie, cases
    val sm = 4.dp   // puces, pastilles
    val md = 8.dp   // cartes, panneaux
    val lg = 10.dp  // grandes surfaces (en-tête, dialogues)
}

/** Échelle d'espacement 4/8dp. */
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
}

/* ————— Identité « par avion » ————— */

val AirmailPaper = Color(0xFFFBFAF7)
val AirmailRed = Color(0xFFD8232A)
val AirmailBlue = Color(0xFF0B5FA5)

/**
 * Le liseré diagonal rouge/blanc/bleu des enveloppes par avion.
 * [period] contrôle la largeur d'un cycle complet en pixels.
 */
fun airmailStripe(period: Float = 30f): Brush = Brush.linearGradient(
    0.00f to AirmailPaper, 0.34f to AirmailPaper,
    0.34f to AirmailRed, 0.67f to AirmailRed,
    0.67f to AirmailBlue, 1.00f to AirmailBlue,
    start = Offset(0f, 0f),
    end = Offset(period, period),
    tileMode = TileMode.Repeated,
)
