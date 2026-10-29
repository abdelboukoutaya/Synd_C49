package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.R

/**
 * Declares one weight of a font resource.
 * If the resource is a VARIABLE font (API 26+), the weight axis is set so 400/500/600/700 really differ.
 * If it is a static .ttf, the variation settings are ignored: add one file per weight instead
 * (e.g. R.font.ibm_plex_sans_arabic_semibold) and reference it here.
 */
@OptIn(ExperimentalTextApi::class)
private fun variableFont(resId: Int, weight: FontWeight): Font = Font(
  resId = resId,
  weight = weight,
  variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

private fun familyOf(resId: Int): FontFamily = FontFamily(
  variableFont(resId, FontWeight.Light),
  variableFont(resId, FontWeight.Normal),
  variableFont(resId, FontWeight.Medium),
  variableFont(resId, FontWeight.SemiBold),
  variableFont(resId, FontWeight.Bold),
)

/**
 * Latin: CoinbaseDisplay / CoinbaseSans are licensed, so DESIGN.md documents Inter as the substitute
 * (weights 400 / 500 / 600 / 700).
 */
val InterNumericFontFamily = familyOf(R.font.inter)
val CoinbaseSansFontFamily = InterNumericFontFamily

/**
 * CoinbaseMono (tabular numbers) is licensed too. Its documented substitutes are JetBrains Mono / Geist Mono,
 * but those files are not in res/font, so Inter with the "tnum" feature is used.
 * To switch: add res/font/jetbrains_mono.ttf and replace this line with familyOf(R.font.jetbrains_mono).
 */
val CoinbaseMonoFontFamily = InterNumericFontFamily

/** Arabic counterpart, 0sp letter-spacing everywhere to preserve cursive ligatures. */
val IbmPlexSansArabicFontFamily = familyOf(R.font.ibm_plex_sans_arabic)

// Kept for compatibility with the previous theme files
val MetaOptimisticLatinFontFamily = familyOf(R.font.plus_jakarta_sans)
val MontserratFontFamily = familyOf(R.font.montserrat)

fun fontScaleMultiplier(scaleKey: String?): Float = when (scaleKey) {
  "small" -> 0.90f
  "large" -> 1.12f
  else -> 1.0f
}

/**
 * Coinbase typography (DESIGN.md), mobile scale and Arabic adaptation:
 * - display stays at weight 400 ("editorial calm"); titles 600; emphasized body 700; nav/numbers 500
 * - negative letter-spacing on display only, and only for Latin/numbers: Arabic always 0sp
 * - Arabic line heights are raised (headings >= 1.4, buttons 1.4, body 1.5) — Latin ratios of 1.0–1.15 clip Arabic
 * - numbers use the numeric family with tabular figures ("tnum") instead of CoinbaseMono
 *
 * M3 slot             -> Coinbase token                         size / weight / lh
 * displayLarge        -> hero KPI number (display-md, mobile)   40 / 400 / 1.10
 * displayMedium       -> KPI number (title-lg)                  32 / 400 / 1.15
 * displaySmall        -> display-sm (mobile)                    30 / 400 / 1.11
 * headlineLarge       -> title-lg                               32 / 400 / 1.13
 * headlineMedium      -> derived                                28 / 400 / 1.14
 * headlineSmall       -> derived                                24 / 400 / 1.20
 * titleLarge          -> title-md                               18 / 600 / 1.33
 * titleMedium         -> title-sm                               16 / 600 / 1.25
 * titleSmall          -> nav-link                               14 / 500 / 1.40
 * bodyLarge/Medium    -> body-md                                16 / 400 / 1.50
 * bodySmall           -> body-sm                                14 / 400 / 1.50
 * labelLarge          -> button                                 16 / 600 / 1.15
 * labelMedium         -> caption-strong                         12 / 600 / 1.50
 * labelSmall          -> caption                                13 / 400 / 1.50
 */
fun buildCoinbaseTypography(
  isArabic: Boolean = true,
  scaleMultiplier: Float = 1.0f,
): Typography {
  val text = if (isArabic) IbmPlexSansArabicFontFamily else CoinbaseSansFontFamily
  val numeric = CoinbaseMonoFontFamily
  val s = scaleMultiplier.coerceIn(0.85f, 1.25f)

  fun size(v: Float): TextUnit = (v * s).sp
  fun lh(v: Float, latinRatio: Float, arabicRatio: Float): TextUnit =
    (v * s * (if (isArabic) maxOf(latinRatio, arabicRatio) else latinRatio)).sp
  fun track(latin: Float): TextUnit = if (isArabic) 0.sp else latin.sp
  val tnum = "tnum"

  return Typography(
    // KPI hero number (cash balance): 40 · 400 · tabular
    displayLarge = TextStyle(
      fontFamily = numeric,
      fontWeight = FontWeight.Normal,
      fontSize = size(40f),
      lineHeight = lh(40f, 1.10f, 1.20f),
      letterSpacing = (-0.8).sp,
      fontFeatureSettings = tnum,
    ),
    // KPI number: 32 · 400 · tabular
    displayMedium = TextStyle(
      fontFamily = numeric,
      fontWeight = FontWeight.Normal,
      fontSize = size(32f),
      lineHeight = lh(32f, 1.15f, 1.25f),
      letterSpacing = (-0.4).sp,
      fontFeatureSettings = tnum,
    ),
    // {typography.display-sm}: 36 -> 30 on phones · 400
    displaySmall = TextStyle(
      fontFamily = text,
      fontWeight = FontWeight.Normal,
      fontSize = size(30f),
      lineHeight = lh(30f, 1.11f, 1.40f),
      letterSpacing = track(-0.5f),
    ),
    // {typography.title-lg}: 32 · 400 · 1.13
    headlineLarge = TextStyle(
      fontFamily = text,
      fontWeight = FontWeight.Normal,
      fontSize = size(32f),
      lineHeight = lh(32f, 1.13f, 1.40f),
      letterSpacing = track(-0.4f),
    ),
    headlineMedium = TextStyle(
      fontFamily = text,
      fontWeight = FontWeight.Normal,
      fontSize = size(28f),
      lineHeight = lh(28f, 1.14f, 1.40f),
      letterSpacing = track(-0.3f),
    ),
    headlineSmall = TextStyle(
      fontFamily = text,
      fontWeight = FontWeight.Normal,
      fontSize = size(24f),
      lineHeight = lh(24f, 1.20f, 1.40f),
      letterSpacing = track(-0.2f),
    ),
    // {typography.title-md}: component titles · 18 · 600 · 1.33
    titleLarge = TextStyle(
      fontFamily = text,
      fontWeight = FontWeight.SemiBold,
      fontSize = size(18f),
      lineHeight = lh(18f, 1.33f, 1.50f),
      letterSpacing = 0.sp,
    ),
    // {typography.title-sm}: list labels · 16 · 600 · 1.25
    titleMedium = TextStyle(
      fontFamily = text,
      fontWeight = FontWeight.SemiBold,
      fontSize = size(16f),
      lineHeight = lh(16f, 1.25f, 1.50f),
      letterSpacing = 0.sp,
    ),
    // {typography.nav-link}: 14 · 500 · 1.4
    titleSmall = TextStyle(
      fontFamily = text,
      fontWeight = FontWeight.Medium,
      fontSize = size(14f),
      lineHeight = lh(14f, 1.40f, 1.50f),
      letterSpacing = 0.sp,
    ),
    // {typography.body-md}: 16 · 400 · 1.5 · 0
    bodyLarge = TextStyle(
      fontFamily = text,
      fontWeight = FontWeight.Normal,
      fontSize = size(16f),
      lineHeight = lh(16f, 1.50f, 1.50f),
      letterSpacing = 0.sp,
    ),
    bodyMedium = TextStyle(
      fontFamily = text,
      fontWeight = FontWeight.Normal,
      fontSize = size(16f),
      lineHeight = lh(16f, 1.50f, 1.50f),
      letterSpacing = 0.sp,
    ),
    // {typography.body-sm}: 14 · 400 · 1.5 · 0
    bodySmall = TextStyle(
      fontFamily = text,
      fontWeight = FontWeight.Normal,
      fontSize = size(14f),
      lineHeight = lh(14f, 1.50f, 1.50f),
      letterSpacing = 0.sp,
    ),
    // {typography.button}: pill CTA label · 16 · 600 · 1.15
    labelLarge = TextStyle(
      fontFamily = text,
      fontWeight = FontWeight.SemiBold,
      fontSize = size(16f),
      lineHeight = lh(16f, 1.15f, 1.40f),
      letterSpacing = 0.sp,
    ),
    // {typography.caption-strong}: badge labels · 12 · 600 · 1.5
    labelMedium = TextStyle(
      fontFamily = text,
      fontWeight = FontWeight.SemiBold,
      fontSize = size(12f),
      lineHeight = lh(12f, 1.50f, 1.50f),
      letterSpacing = 0.sp,
    ),
    // {typography.caption}: 13 · 400 · 1.5
    labelSmall = TextStyle(
      fontFamily = text,
      fontWeight = FontWeight.Normal,
      fontSize = size(13f),
      lineHeight = lh(13f, 1.50f, 1.50f),
      letterSpacing = 0.sp,
    ),
  )
}

// Compatibility with the previous theme files
fun buildMetaTypography(
  isArabic: Boolean = true,
  scaleMultiplier: Float = 1.0f,
): Typography = buildCoinbaseTypography(isArabic = isArabic, scaleMultiplier = scaleMultiplier)

fun buildAirtableTypography(
  isArabic: Boolean = true,
  scaleMultiplier: Float = 1.0f,
): Typography = buildCoinbaseTypography(isArabic = isArabic, scaleMultiplier = scaleMultiplier)

val AppTypography = buildCoinbaseTypography(isArabic = true, scaleMultiplier = 1.0f)
val Typography = buildCoinbaseTypography(isArabic = false, scaleMultiplier = 1.0f)
val ArabicTypography = AppTypography