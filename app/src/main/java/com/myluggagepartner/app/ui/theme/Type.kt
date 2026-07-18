package com.myluggagepartner.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * TYPOGRAPHIE
 *
 * Le design utilise Bricolage Grotesque (display) + Figtree (body).
 * Pour garder ce projet compilable sans binaires, on référence ici des
 * FontFamily système. Pour installer les vraies polices :
 *
 *   1. Télécharge les .ttf depuis Google Fonts (Bricolage Grotesque, Figtree)
 *   2. Place-les dans app/src/main/res/font/  (noms en minuscules, ex.
 *      bricolage_grotesque_bold.ttf, figtree_regular.ttf …)
 *   3. Remplace Display/Body ci-dessous par des FontFamily(Font(R.font.xxx, weight)).
 *
 * Claude Code peut faire cette bascule en une passe une fois les .ttf ajoutés.
 */
val DisplayFamily: FontFamily = FontFamily.Default   // → Bricolage Grotesque
val BodyFamily: FontFamily = FontFamily.SansSerif    // → Figtree

// Titres display (Bricolage, tracking serré -0.02em)
val DisplayLarge = TextStyle(
    fontFamily = DisplayFamily, fontWeight = FontWeight.ExtraBold,
    fontSize = 42.sp, lineHeight = 44.sp, letterSpacing = (-0.02).em,
)
val DisplayMedium = TextStyle(
    fontFamily = DisplayFamily, fontWeight = FontWeight.Bold,
    fontSize = 34.sp, lineHeight = 38.sp, letterSpacing = (-0.02).em,
)
val TitleStep = TextStyle(
    fontFamily = DisplayFamily, fontWeight = FontWeight.Bold,
    fontSize = 32.sp, lineHeight = 36.sp, letterSpacing = (-0.02).em,
)
val TitleCard = TextStyle(
    fontFamily = DisplayFamily, fontWeight = FontWeight.Bold,
    fontSize = 20.sp, lineHeight = 24.sp, letterSpacing = (-0.02).em,
)
val CategoryTitle = TextStyle(
    fontFamily = DisplayFamily, fontWeight = FontWeight.Bold,
    fontSize = 16.sp, lineHeight = 20.sp,
)

val AppTypography = Typography(
    // On mappe le corps de texte Material sur Figtree
    bodyLarge = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
    labelMedium = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
)
