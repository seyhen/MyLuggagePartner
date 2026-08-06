package com.myluggagepartner.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = com.myluggagepartner.app.R.array.com_google_android_gms_fonts_certs,
)

/* Trois voix, comme sur une enveloppe : le tampon, la main, la machine. */
private val archivoNarrow = GoogleFont("Archivo Narrow")  // signalétique postale
private val publicSans = GoogleFont("Public Sans")        // texte courant, neutre
private val courierPrime = GoogleFont("Courier Prime")    // chiffres et codes

/** Condensé, capitales — pour les titres, comme imprimé au pochoir sur un colis. */
val DisplayFamily: FontFamily = FontFamily(
    Font(googleFont = archivoNarrow, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = archivoNarrow, fontProvider = provider, weight = FontWeight.SemiBold),
)

val BodyFamily: FontFamily = FontFamily(
    Font(googleFont = publicSans, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = publicSans, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = publicSans, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = publicSans, fontProvider = provider, weight = FontWeight.Bold),
)

/** Quantités, compteurs, codes — tout ce qui est donnée chiffrée. */
val MonoFamily: FontFamily = FontFamily(
    Font(googleFont = courierPrime, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = courierPrime, fontProvider = provider, weight = FontWeight.Bold),
)

val DisplayLarge = TextStyle(
    fontFamily = DisplayFamily, fontWeight = FontWeight.Bold,
    fontSize = 40.sp, lineHeight = 42.sp, letterSpacing = 0.5.sp,
)
val DisplayMedium = TextStyle(
    fontFamily = DisplayFamily, fontWeight = FontWeight.Bold,
    fontSize = 30.sp, lineHeight = 34.sp, letterSpacing = 0.4.sp,
)
val TitleStep = TextStyle(
    fontFamily = DisplayFamily, fontWeight = FontWeight.Bold,
    fontSize = 30.sp, lineHeight = 34.sp, letterSpacing = 0.4.sp,
)
val TitleCard = TextStyle(
    fontFamily = DisplayFamily, fontWeight = FontWeight.Bold,
    fontSize = 20.sp, lineHeight = 24.sp, letterSpacing = 0.3.sp,
)

/** Bandeau de catégorie : petites capitales très espacées, façon étiquette de tri. */
val CategoryTitle = TextStyle(
    fontFamily = DisplayFamily, fontWeight = FontWeight.Bold,
    fontSize = 13.sp, lineHeight = 16.sp, letterSpacing = 1.6.sp,
)

/** Libellé administratif — au-dessus des champs, dans les en-têtes de section. */
val LabelStamp = TextStyle(
    fontFamily = MonoFamily, fontWeight = FontWeight.Bold,
    fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 1.4.sp,
)

/** Données chiffrées : 12/26, ×4, J-3. */
val DataMono = TextStyle(
    fontFamily = MonoFamily, fontWeight = FontWeight.Bold,
    fontSize = 13.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp,
)

val AppTypography = Typography(
    bodyLarge = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 23.sp),
    bodyMedium = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = BodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
    labelMedium = TextStyle(fontFamily = MonoFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.2.sp),
)
