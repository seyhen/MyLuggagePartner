package com.myluggagepartner.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = com.myluggagepartner.app.R.array.com_google_android_gms_fonts_certs,
)

private val bricolageGrotesque = GoogleFont("Bricolage Grotesque")
private val figtree = GoogleFont("Figtree")

val DisplayFamily: FontFamily = FontFamily(
    Font(googleFont = bricolageGrotesque, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = bricolageGrotesque, fontProvider = provider, weight = FontWeight.ExtraBold),
)

val BodyFamily: FontFamily = FontFamily(
    Font(googleFont = figtree, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = figtree, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = figtree, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = figtree, fontProvider = provider, weight = FontWeight.Bold),
)

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
    bodyLarge = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
    labelMedium = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
)
