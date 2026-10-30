package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

// ============================================================================
// Supported Theme Identifiers (Blueprint + Firestore Rules + Local Storage)
// ============================================================================

val VALID_THEME_STYLES: Set<String> = setOf(
  "coinbase",
  "meta",
  "minimax",
  "airtable",
  "wise",
  "xai",
)

val SELECTABLE_THEME_STYLES: List<String> = listOf(
  "meta",
  "coinbase",
  "minimax",
)

/**
 * Validates a themeStyle string before storing in Firestore or local JSON.
 * Keeps legacy values ("airtable", "wise", "xai") valid while supporting
 * the 3 distinct choices ("meta", "coinbase", "minimax").
 */
fun sanitizeThemeStyle(raw: String?, fallback: String = "coinbase"): String {
  val cleaned = raw?.trim()?.lowercase()
  return if (cleaned != null && cleaned in VALID_THEME_STYLES) cleaned else fallback
}

/**
 * Resolves any stored themeStyle (including legacy "airtable", "wise", "xai")
 * to one of the 3 active visual design systems ("meta", "coinbase", "minimax").
 */
fun resolveVisualThemeFamily(raw: String?): String = when (raw?.trim()?.lowercase()) {
  "meta", "xai" -> "meta"
  "minimax", "wise" -> "minimax"
  "coinbase", "airtable" -> "coinbase"
  else -> "coinbase"
}

// ============================================================================
// 1. Coinbase Design System — Palette
// ============================================================================

val CoinbasePrimary = Color(0xFF0052FF)
val CoinbasePrimaryActive = Color(0xFF003ECC)
val CoinbasePrimaryDisabled = Color(0xFFA8B8CC)
val CoinbaseOnPrimary = Color(0xFFFFFFFF)
val CoinbaseAccentYellow = Color(0xFFF4B000)

val CoinbaseCanvas = Color(0xFFFFFFFF)
val CoinbaseSurfaceSoft = Color(0xFFF7F7F7)
val CoinbaseSurfaceStrong = Color(0xFFEEF0F3)
val CoinbaseSurfaceDark = Color(0xFF0A0B0D)
val CoinbaseSurfaceDarkElevated = Color(0xFF16181C)

val CoinbaseHairline = Color(0xFFDEE1E6)
val CoinbaseHairlineSoft = Color(0xFFEEF0F3)

val CoinbaseInk = Color(0xFF0A0B0D)
val CoinbaseBody = Color(0xFF5B616E)
val CoinbaseMuted = Color(0xFF7C828A)
val CoinbaseMutedSoft = Color(0xFFA8ACB3)
val CoinbaseOnDark = Color(0xFFFFFFFF)
val CoinbaseOnDarkSoft = Color(0xFFA8ACB3)

val CoinbaseSemanticUp = Color(0xFF05B169)
val CoinbaseSemanticDown = Color(0xFFCF202F)

val CoinbasePositiveDeep = Color(0xFF087443)
val CoinbaseNegativeDeep = Color(0xFFA81724)
val CoinbasePrimaryTintLight = Color(0xFFE6EEFF)
val CoinbaseSuccessTintLight = Color(0xFFE6F7F0)
val CoinbaseWarningTintLight = Color(0xFFFEF7E6)
val CoinbaseErrorTintLight = Color(0xFFFAE9EA)

val CoinbaseDarkScreenBg = CoinbaseSurfaceDark
val CoinbaseDarkCardBg = CoinbaseSurfaceDarkElevated
val CoinbaseDarkSurfaceLow = Color(0xFF111317)
val CoinbaseDarkSurfaceSoft = Color(0xFF1C1F24)
val CoinbaseDarkSurfaceHigh = Color(0xFF262A31)
val CoinbaseDarkHairline = Color(0xFF2D3138)
val CoinbaseDarkHairlineSoft = Color(0xFF22262C)
val CoinbaseDarkInkDeep = Color(0xFFFFFFFF)
val CoinbaseDarkInk = Color(0xFFE6E8EB)
val CoinbaseDarkCharcoal = CoinbaseOnDarkSoft
val CoinbaseDarkSteel = CoinbaseMuted
val CoinbaseDarkBlueText = Color(0xFF5B8CFF)
val CoinbaseDarkBlueContainer = Color(0xFF0D2B6B)
val CoinbasePrimarySoftOnDark = Color(0xFF8FB0FF)
val CoinbaseDarkSuccess = CoinbaseSemanticUp
val CoinbaseDarkCritical = Color(0xFFFF6B78)
val CoinbaseDarkErrorContainer = Color(0xFF3B1219)

// ============================================================================
// 2. Meta Design System — Palette (source : DESIGNmeta.md)
// ============================================================================

val MetaBrandCobalt = Color(0xFF0064E0)
val MetaBrandCobaltDeep = Color(0xFF0457CB)
val MetaBrandCobaltSoft = Color(0xFF4DA3FF)
val MetaSoftCloud = Color(0xFFF1F4F7)
val MetaSoftCloudStrong = Color(0xFFE3E9F0)
val MetaDeepInk = Color(0xFF0A1317)
val MetaTextInk = Color(0xFF1C1E21)
val MetaBodyCharcoal = Color(0xFF444950)
val MetaMutedSteel = Color(0xFF5D6C7B)
val MetaStoneGray = Color(0xFF8595A4)
val MetaLineHairline = Color(0xFFCED0D4)
val MetaLineSoft = Color(0xFFDEE3E9)
val MetaOculusAccent = Color(0xFFA121CE)
val MetaSuccessGreen = Color(0xFF1E7E34)
val MetaCriticalRed = Color(0xFFD91B3A)
val MetaTintLight = Color(0xFFE7F1FD)

val MetaDarkBg = Color(0xFF0A1317)
val MetaDarkCard = Color(0xFF142028)
val MetaDarkElevated = Color(0xFF1C2B36)
val MetaDarkHigh = Color(0xFF253746)
val MetaDarkBorder = Color(0xFF2C3F4F)
val MetaDarkTint = Color(0xFF0E2F5E)

// ============================================================================
// 3. MiniMax Design System — Palette (Warm Editorial Studio + Vermilion Coral)
// ============================================================================

val MiniMaxPrimary = Color(0xFFD93F26)
val MiniMaxPrimaryActive = Color(0xFFB8301A)
val MiniMaxPrimaryOnDark = Color(0xFFFF826E)
val MiniMaxWarmPaper = Color(0xFFFAF8F5)
val MiniMaxCardWhite = Color(0xFFFFFFFF)
val MiniMaxSurfaceSoft = Color(0xFFF3F0EB)
val MiniMaxSurfaceStrong = Color(0xFFEAE5DD)
val MiniMaxHairline = Color(0xFFDCD6CC)
val MiniMaxDivider = Color(0xFFE8E3DA)
val MiniMaxInkDeep = Color(0xFF141518)
val MiniMaxInk = Color(0xFF1C1D22)
val MiniMaxBody = Color(0xFF4E525C)
val MiniMaxMuted = Color(0xFF6E7380)
val MiniMaxStone = Color(0xFF9B9FA8)
val MiniMaxTintLight = Color(0xFFFFECE8)
val MiniMaxPositiveDeep = Color(0xFF0F7652)
val MiniMaxNegativeDeep = Color(0xFFB42318)
val MiniMaxAccentAmber = Color(0xFFD97706)

val MiniMaxDarkBg = Color(0xFF111216)
val MiniMaxDarkCard = Color(0xFF1A1C22)
val MiniMaxDarkSoft = Color(0xFF22252D)
val MiniMaxDarkHigh = Color(0xFF2C303A)
val MiniMaxDarkBorder = Color(0xFF323642)
val MiniMaxDarkTint = Color(0xFF421912)
val MiniMaxDarkSuccess = Color(0xFF34D399)
val MiniMaxDarkCritical = Color(0xFFFF7A70)

// ============================================================================
// Backwards-compatible aliases (Meta* and Airtable*) so existing screens compile
// ============================================================================

val MetaPrimaryCobalt = CoinbasePrimary
val MetaPrimaryDeep = CoinbasePrimaryActive
val MetaPrimarySoft = CoinbaseDarkBlueText
val MetaOnPrimary = CoinbaseOnPrimary
val MetaInkButton = CoinbaseInk
val MetaOnInkButton = CoinbaseOnDark
val MetaFbBlue = CoinbasePrimary
val MetaRadioSelected = CoinbasePrimaryActive
val MetaLink = CoinbasePrimary
val MetaOculusPurple = MetaOculusAccent
val MetaSuccess = CoinbaseSemanticUp
val MetaSuccessBg = CoinbaseSemanticUp
val MetaAttention = CoinbaseAccentYellow
val MetaWarning = CoinbaseAccentYellow
val MetaWarningBg = CoinbaseAccentYellow
val MetaCritical = CoinbaseSemanticDown
val MetaCriticalStrong = CoinbaseNegativeDeep
val MetaCanvas = CoinbaseCanvas
val MetaSurfaceSoft = CoinbaseSurfaceStrong
val MetaSurfaceContainer = CoinbaseSurfaceStrong
val MetaHairline = CoinbaseHairline
val MetaHairlineSoft = CoinbaseHairlineSoft
val MetaDisabledText = CoinbasePrimaryDisabled
val MetaInkDeep = CoinbaseInk
val MetaInk = CoinbaseInk
val MetaCharcoal = CoinbaseBody
val MetaSlate = CoinbaseBody
val MetaSteel = CoinbaseMuted
val MetaStone = CoinbaseMutedSoft
val MetaCobaltTintLight = CoinbasePrimaryTintLight
val MetaSuccessTintLight = CoinbaseSuccessTintLight
val MetaWarningTintLight = CoinbaseWarningTintLight
val MetaErrorTintLight = CoinbaseErrorTintLight
val MetaDarkScreenBg = CoinbaseDarkScreenBg
val MetaDarkCardBg = CoinbaseDarkCardBg
val MetaDarkSurfaceLow = CoinbaseDarkSurfaceLow
val MetaDarkSurfaceSoft = CoinbaseDarkSurfaceSoft
val MetaDarkSurfaceHigh = CoinbaseDarkSurfaceHigh
val MetaDarkInkDeep = CoinbaseDarkInkDeep
val MetaDarkInk = CoinbaseDarkInk
val MetaDarkCharcoal = CoinbaseDarkCharcoal
val MetaDarkSteel = CoinbaseDarkSteel
val MetaDarkHairline = CoinbaseDarkHairline
val MetaDarkHairlineSoft = CoinbaseDarkHairlineSoft
val MetaDarkCobalt = CoinbaseDarkBlueText
val MetaDarkCobaltContainer = CoinbaseDarkBlueContainer
val MetaDarkSuccess = CoinbaseDarkSuccess
val MetaDarkCritical = CoinbaseDarkCritical
val MetaDarkErrorContainer = CoinbaseDarkErrorContainer

val AirtablePrimary = CoinbasePrimary
val AirtablePrimaryActive = CoinbasePrimaryActive
val AirtableOnPrimary = CoinbaseOnPrimary
val AirtablePrimaryContainer = CoinbasePrimaryTintLight
val AirtableOnPrimaryContainer = CoinbasePrimaryActive
val AirtableInk = MetaInkDeep
val AirtableBody = MetaInk
val AirtableMuted = MetaSteel
val AirtableCanvas = MetaCanvas
val AirtableSurfaceSoft = MetaSurfaceSoft
val AirtableSurfaceContainer = MetaSurfaceSoft
val AirtableSurfaceStrong = MetaSurfaceContainer
val AirtableSurfaceDark = CoinbaseSurfaceDark
val AirtableSurfaceDarkElevated = CoinbaseSurfaceDarkElevated
val AirtableHairline = MetaHairline
val AirtableBorderStrong = CoinbaseMuted
val AirtableSignatureForest = CoinbaseSurfaceDark
val AirtableSignatureCoral = CoinbaseSurfaceDark
val AirtableSignatureCream = CoinbaseSurfaceSoft
val AirtableSignatureMint = CoinbaseSurfaceSoft
val AirtableSignaturePeach = CoinbaseSurfaceSoft
val AirtableSignatureYellow = CoinbaseAccentYellow
val AirtableSignatureMustard = CoinbaseAccentYellow
val AirtableLink = CoinbasePrimary
val AirtableLinkActive = CoinbasePrimaryActive
val AirtableInfo = CoinbasePrimary
val AirtableInfoContainer = CoinbasePrimaryTintLight
val AirtableInfoBorder = CoinbasePrimary
val AirtableSuccess = CoinbasePositiveDeep
val AirtableSuccessBorder = CoinbaseSemanticUp
val AirtableErrorContainerLight = CoinbaseErrorTintLight
val AirtableDarkScreenBg = CoinbaseDarkScreenBg
val AirtableDarkCardBg = CoinbaseDarkCardBg
val AirtableDarkSurfaceLow = CoinbaseDarkSurfaceLow
val AirtableDarkSurfaceContainer = CoinbaseDarkSurfaceSoft
val AirtableDarkSurfaceHigh = CoinbaseDarkSurfaceHigh
val AirtableDarkInk = CoinbaseDarkInkDeep
val AirtableDarkBody = CoinbaseDarkInk
val AirtableDarkMuted = CoinbaseDarkSteel
val AirtableDarkHairline = CoinbaseDarkHairlineSoft
val AirtableDarkBorderStrong = CoinbaseDarkHairline
val AirtableDarkInfo = CoinbaseDarkBlueText
val AirtableDarkInfoContainer = CoinbaseDarkBlueContainer
val AirtableDarkLink = CoinbaseDarkBlueText
val AirtableDarkSuccessText = CoinbaseDarkSuccess
val AirtableDarkCoralText = CoinbaseDarkCritical
val AirtableDarkErrorContainer = CoinbaseDarkErrorContainer

// ============================================================================
// Design tokens shared across Coinbase, Meta, and MiniMax
// ============================================================================

@Immutable
data class SyndicatDesignTokens(
  val id: String = "coinbase",
  val designSystemName: String = "Coinbase",
  val labelAr: String = "الأزرق المؤسسي",
  val primary: Color,
  val onPrimary: Color,
  val primaryActive: Color,
  val buyCta: Color,
  val buyCtaPressed: Color,
  val onBuyCta: Color,
  val inkDeep: Color,
  val ink: Color,
  val body: Color,
  val mute: Color,
  val stone: Color,
  val canvas: Color,
  val canvasSoft: Color,
  val divider: Color,
  val hairline: Color,
  val disabledBg: Color,
  val positive: Color,
  val positiveDeep: Color,
  val attention: Color,
  val warning: Color,
  val warningContent: Color,
  val negative: Color,
  val negativeDeep: Color,
  val negativeBg: Color,
  val isDark: Boolean,
  val screenBg: Color = canvas,
  val navBg: Color = canvas,
  val cardBg: Color = canvas,
  val cardBorder: Color = hairline,
  val featureCardRadiusDp: Int = 24,
  val cardRadiusDp: Int = 24,
  val contentCardRadiusDp: Int = 16,
  val buttonRadiusDp: Int = 100,
  val badgeRadiusDp: Int = 100,
  val defaultCardPaddingDp: Int = 24,
  val fieldBg: Color = canvas,
  val fieldBorder: Color = CoinbaseMuted,
  val fieldFocusBorder: Color = CoinbasePrimary,
  val radioSelectedBorder: Color = CoinbasePrimaryActive,
  val fieldRadiusDp: Int = 12,
  val trackBg: Color = divider,
  val surfaceSoft: Color = canvasSoft,
  val surfaceStrong: Color = CoinbaseSurfaceStrong,
  val signatureForest: Color = CoinbaseSurfaceDark,
  val signatureCoral: Color = CoinbaseSurfaceDark,
  val signatureCream: Color = CoinbaseSurfaceSoft,
  val signatureMint: Color = CoinbaseSurfaceSoft,
  val signaturePeach: Color = CoinbaseSurfaceSoft,
  val signatureYellow: Color = CoinbaseAccentYellow,
  val signatureMustard: Color = CoinbaseAccentYellow,
  val oculusPurple: Color = CoinbasePrimary,
  val linkColor: Color = CoinbasePrimary,
  val linkActiveColor: Color = CoinbasePrimaryActive,
  val infoColor: Color = CoinbasePrimary,
  val infoBorder: Color = CoinbasePrimary,
  val monoFontFamily: FontFamily = InterNumericFontFamily,
  val numericFontFamily: FontFamily = InterNumericFontFamily,
  val onDark: Color = CoinbaseOnDark,
  val onDarkSoft: Color = CoinbaseOnDarkSoft,
  val darkBand: Color = CoinbaseSurfaceDark,
  val darkBandElevated: Color = CoinbaseSurfaceDarkElevated,
  val badgeBg: Color = CoinbaseSurfaceStrong,
  val primaryTint: Color = CoinbasePrimaryTintLight,
  val statusPaidText: Color = positiveDeep,
  val statusLateText: Color = negativeDeep,
  val statusPartialDot: Color = warning,
) {
  val themeStyle: String
    get() = id

  fun weight(requested: FontWeight): FontWeight = when {
    requested.weight <= 300 -> FontWeight.Light
    requested.weight >= 700 -> FontWeight.Bold
    requested.weight >= 600 -> FontWeight.SemiBold
    requested.weight >= 500 -> FontWeight.Medium
    else -> FontWeight.Normal
  }
}

// ----------------------------------------------------------------------------
// 1. Coinbase Tokens (Light & Dark)
// ----------------------------------------------------------------------------

val CoinbaseLightTokens = SyndicatDesignTokens(
  id = "coinbase",
  designSystemName = "Coinbase",
  labelAr = "Coinbase الأزرق",
  primary = CoinbasePrimary,
  onPrimary = CoinbaseOnPrimary,
  primaryActive = CoinbasePrimaryActive,
  buyCta = CoinbasePrimary,
  buyCtaPressed = CoinbasePrimaryActive,
  onBuyCta = CoinbaseOnPrimary,
  inkDeep = CoinbaseInk,
  ink = CoinbaseInk,
  body = CoinbaseBody,
  mute = CoinbaseMuted,
  stone = CoinbaseMutedSoft,
  canvas = CoinbaseCanvas,
  canvasSoft = CoinbaseSurfaceStrong,
  divider = CoinbaseHairlineSoft,
  hairline = CoinbaseHairline,
  disabledBg = CoinbasePrimaryDisabled,
  positive = CoinbaseSemanticUp,
  positiveDeep = CoinbasePositiveDeep,
  attention = CoinbaseAccentYellow,
  warning = CoinbaseAccentYellow,
  warningContent = CoinbaseInk,
  negative = CoinbaseSemanticDown,
  negativeDeep = CoinbaseNegativeDeep,
  negativeBg = CoinbaseSemanticDown,
  isDark = false,
  screenBg = CoinbaseCanvas,
  navBg = CoinbaseCanvas,
  cardBg = CoinbaseCanvas,
  cardBorder = CoinbaseHairline,
  featureCardRadiusDp = 24,
  cardRadiusDp = 24,
  contentCardRadiusDp = 16,
  buttonRadiusDp = 100,
  badgeRadiusDp = 100,
  fieldBg = CoinbaseCanvas,
  fieldBorder = CoinbaseMuted,
  fieldFocusBorder = CoinbasePrimary,
  radioSelectedBorder = CoinbasePrimaryActive,
  fieldRadiusDp = 12,
  trackBg = CoinbaseHairlineSoft,
  surfaceSoft = CoinbaseSurfaceSoft,
  surfaceStrong = CoinbaseSurfaceStrong,
  linkColor = CoinbasePrimary,
  linkActiveColor = CoinbasePrimaryActive,
  infoColor = CoinbasePrimary,
  infoBorder = CoinbasePrimary,
  badgeBg = CoinbaseSurfaceStrong,
  primaryTint = CoinbasePrimaryTintLight,
  statusPaidText = CoinbasePositiveDeep,
  statusLateText = CoinbaseNegativeDeep,
  statusPartialDot = CoinbaseAccentYellow,
)

val CoinbaseDarkTokens = SyndicatDesignTokens(
  id = "coinbase",
  designSystemName = "Coinbase",
  labelAr = "Coinbase الأزرق",
  primary = CoinbasePrimary,
  onPrimary = CoinbaseOnPrimary,
  primaryActive = CoinbasePrimaryActive,
  buyCta = CoinbasePrimary,
  buyCtaPressed = CoinbasePrimaryActive,
  onBuyCta = CoinbaseOnPrimary,
  inkDeep = CoinbaseDarkInkDeep,
  ink = CoinbaseDarkInk,
  body = CoinbaseDarkCharcoal,
  mute = CoinbaseDarkSteel,
  stone = CoinbaseMuted,
  canvas = CoinbaseDarkCardBg,
  canvasSoft = CoinbaseDarkSurfaceSoft,
  divider = CoinbaseDarkHairlineSoft,
  hairline = CoinbaseDarkHairline,
  disabledBg = CoinbaseDarkSurfaceHigh,
  positive = CoinbaseDarkSuccess,
  positiveDeep = CoinbaseDarkSuccess,
  attention = CoinbaseAccentYellow,
  warning = CoinbaseAccentYellow,
  warningContent = CoinbaseDarkInkDeep,
  negative = CoinbaseDarkCritical,
  negativeDeep = CoinbaseDarkCritical,
  negativeBg = CoinbaseSemanticDown,
  isDark = true,
  screenBg = CoinbaseDarkScreenBg,
  navBg = CoinbaseDarkScreenBg,
  cardBg = CoinbaseDarkCardBg,
  cardBorder = CoinbaseDarkHairline,
  featureCardRadiusDp = 24,
  cardRadiusDp = 24,
  contentCardRadiusDp = 16,
  buttonRadiusDp = 100,
  badgeRadiusDp = 100,
  fieldBg = CoinbaseDarkCardBg,
  fieldBorder = CoinbaseMuted,
  fieldFocusBorder = CoinbaseDarkBlueText,
  radioSelectedBorder = CoinbaseDarkBlueText,
  fieldRadiusDp = 12,
  trackBg = CoinbaseDarkHairline,
  surfaceSoft = CoinbaseDarkSurfaceSoft,
  surfaceStrong = CoinbaseDarkSurfaceHigh,
  signatureForest = CoinbaseDarkSurfaceSoft,
  signatureCoral = CoinbaseDarkSurfaceSoft,
  signatureCream = CoinbaseDarkSurfaceSoft,
  signatureMint = CoinbaseDarkSurfaceSoft,
  signaturePeach = CoinbaseDarkSurfaceSoft,
  linkColor = CoinbaseDarkBlueText,
  linkActiveColor = CoinbasePrimarySoftOnDark,
  infoColor = CoinbaseDarkBlueText,
  infoBorder = CoinbaseDarkBlueText,
  darkBand = CoinbaseDarkSurfaceSoft,
  darkBandElevated = CoinbaseDarkSurfaceHigh,
  badgeBg = CoinbaseDarkSurfaceHigh,
  primaryTint = CoinbaseDarkBlueContainer,
  statusPaidText = CoinbaseDarkSuccess,
  statusLateText = CoinbaseDarkCritical,
  statusPartialDot = CoinbaseAccentYellow,
)

// ----------------------------------------------------------------------------
// 2. Meta Tokens (Light & Dark — DESIGNmeta.md Cobalt #0064E0 + Soft Cloud #F1F4F7)
// ----------------------------------------------------------------------------

val MetaThemeLightTokens = SyndicatDesignTokens(
  id = "meta",
  designSystemName = "Meta",
  labelAr = "Meta كوبالت",
  primary = MetaBrandCobalt,
  onPrimary = Color.White,
  primaryActive = MetaBrandCobaltDeep,
  buyCta = MetaBrandCobalt,
  buyCtaPressed = MetaBrandCobaltDeep,
  onBuyCta = Color.White,
  inkDeep = MetaDeepInk,
  ink = MetaTextInk,
  body = MetaBodyCharcoal,
  mute = MetaMutedSteel,
  stone = MetaStoneGray,
  canvas = Color.White,
  canvasSoft = MetaSoftCloud,
  divider = MetaLineSoft,
  hairline = MetaLineHairline,
  disabledBg = Color(0xFFBCC0C4),
  positive = Color(0xFF31A24C),
  positiveDeep = MetaSuccessGreen,
  attention = Color(0xFFF2A918),
  warning = Color(0xFFF7B928),
  warningContent = MetaDeepInk,
  negative = MetaCriticalRed,
  negativeDeep = Color(0xFFB3142E),
  negativeBg = MetaCriticalRed,
  isDark = false,
  screenBg = MetaSoftCloud,
  navBg = Color.White,
  cardBg = Color.White,
  cardBorder = MetaLineHairline,
  featureCardRadiusDp = 28,
  cardRadiusDp = 24,
  contentCardRadiusDp = 16,
  buttonRadiusDp = 100,
  badgeRadiusDp = 100,
  fieldBg = Color.White,
  fieldBorder = MetaMutedSteel,
  fieldFocusBorder = MetaBrandCobalt,
  radioSelectedBorder = MetaBrandCobaltDeep,
  fieldRadiusDp = 14,
  trackBg = MetaLineSoft,
  surfaceSoft = MetaSoftCloud,
  surfaceStrong = MetaSoftCloudStrong,
  signatureForest = MetaDeepInk,
  signatureCoral = MetaDeepInk,
  signatureCream = MetaSoftCloud,
  signatureMint = MetaSoftCloud,
  signaturePeach = MetaSoftCloud,
  oculusPurple = MetaOculusAccent,
  linkColor = MetaBrandCobalt,
  linkActiveColor = MetaBrandCobaltDeep,
  infoColor = MetaBrandCobalt,
  infoBorder = MetaBrandCobalt,
  darkBand = MetaDeepInk,
  darkBandElevated = MetaDarkCard,
  badgeBg = MetaSoftCloudStrong,
  primaryTint = MetaTintLight,
  statusPaidText = MetaSuccessGreen,
  statusLateText = Color(0xFFB3142E),
  statusPartialDot = Color(0xFFF2A918),
)

val MetaThemeDarkTokens = SyndicatDesignTokens(
  id = "meta",
  designSystemName = "Meta",
  labelAr = "Meta كوبالت",
  primary = MetaBrandCobalt,
  onPrimary = Color.White,
  primaryActive = MetaBrandCobaltDeep,
  buyCta = MetaBrandCobalt,
  buyCtaPressed = MetaBrandCobaltDeep,
  onBuyCta = Color.White,
  inkDeep = Color.White,
  ink = Color(0xFFE6ECF2),
  body = Color(0xFFB0BEC9),
  mute = Color(0xFF8595A4),
  stone = Color(0xFF6B7C8C),
  canvas = MetaDarkCard,
  canvasSoft = MetaDarkElevated,
  divider = Color(0xFF21313E),
  hairline = MetaDarkBorder,
  disabledBg = MetaDarkHigh,
  positive = Color(0xFF4ADE80),
  positiveDeep = Color(0xFF4ADE80),
  attention = Color(0xFFF7B928),
  warning = Color(0xFFF7B928),
  warningContent = Color.White,
  negative = Color(0xFFFF6B81),
  negativeDeep = Color(0xFFFF6B81),
  negativeBg = MetaCriticalRed,
  isDark = true,
  screenBg = MetaDarkBg,
  navBg = MetaDarkBg,
  cardBg = MetaDarkCard,
  cardBorder = MetaDarkBorder,
  featureCardRadiusDp = 28,
  cardRadiusDp = 24,
  contentCardRadiusDp = 16,
  buttonRadiusDp = 100,
  badgeRadiusDp = 100,
  fieldBg = MetaDarkCard,
  fieldBorder = MetaStoneGray,
  fieldFocusBorder = MetaBrandCobaltSoft,
  radioSelectedBorder = MetaBrandCobaltSoft,
  fieldRadiusDp = 14,
  trackBg = MetaDarkBorder,
  surfaceSoft = MetaDarkElevated,
  surfaceStrong = MetaDarkHigh,
  signatureForest = MetaDarkElevated,
  signatureCoral = MetaDarkElevated,
  signatureCream = MetaDarkElevated,
  signatureMint = MetaDarkElevated,
  signaturePeach = MetaDarkElevated,
  oculusPurple = Color(0xFFD580FF),
  linkColor = MetaBrandCobaltSoft,
  linkActiveColor = Color(0xFF82BEFF),
  infoColor = MetaBrandCobaltSoft,
  infoBorder = MetaBrandCobaltSoft,
  darkBand = MetaDarkElevated,
  darkBandElevated = MetaDarkHigh,
  badgeBg = MetaDarkHigh,
  primaryTint = MetaDarkTint,
  statusPaidText = Color(0xFF4ADE80),
  statusLateText = Color(0xFFFF6B81),
  statusPartialDot = Color(0xFFF7B928),
)

// ----------------------------------------------------------------------------
// 3. MiniMax Tokens (Light & Dark — Warm Alabaster + Coral Vermilion + Sharp Geometry)
// ----------------------------------------------------------------------------

val MiniMaxLightTokens = SyndicatDesignTokens(
  id = "minimax",
  designSystemName = "MiniMax",
  labelAr = "MiniMax المرجاني",
  primary = MiniMaxPrimary,
  onPrimary = Color.White,
  primaryActive = MiniMaxPrimaryActive,
  buyCta = MiniMaxPrimary,
  buyCtaPressed = MiniMaxPrimaryActive,
  onBuyCta = Color.White,
  inkDeep = MiniMaxInkDeep,
  ink = MiniMaxInk,
  body = MiniMaxBody,
  mute = MiniMaxMuted,
  stone = MiniMaxStone,
  canvas = MiniMaxCardWhite,
  canvasSoft = MiniMaxSurfaceSoft,
  divider = MiniMaxDivider,
  hairline = MiniMaxHairline,
  disabledBg = Color(0xFFD5CFC7),
  positive = Color(0xFF10B981),
  positiveDeep = MiniMaxPositiveDeep,
  attention = MiniMaxAccentAmber,
  warning = MiniMaxAccentAmber,
  warningContent = MiniMaxInkDeep,
  negative = Color(0xFFD92D20),
  negativeDeep = MiniMaxNegativeDeep,
  negativeBg = Color(0xFFD92D20),
  isDark = false,
  screenBg = MiniMaxWarmPaper,
  navBg = MiniMaxCardWhite,
  cardBg = MiniMaxCardWhite,
  cardBorder = MiniMaxHairline,
  featureCardRadiusDp = 20,
  cardRadiusDp = 18,
  contentCardRadiusDp = 12,
  buttonRadiusDp = 14,
  badgeRadiusDp = 10,
  fieldBg = MiniMaxCardWhite,
  fieldBorder = MiniMaxMuted,
  fieldFocusBorder = MiniMaxPrimary,
  radioSelectedBorder = MiniMaxPrimaryActive,
  fieldRadiusDp = 10,
  trackBg = MiniMaxDivider,
  surfaceSoft = MiniMaxSurfaceSoft,
  surfaceStrong = MiniMaxSurfaceStrong,
  signatureForest = MiniMaxInkDeep,
  signatureCoral = MiniMaxInkDeep,
  signatureCream = MiniMaxSurfaceSoft,
  signatureMint = MiniMaxSurfaceSoft,
  signaturePeach = MiniMaxSurfaceSoft,
  signatureYellow = MiniMaxAccentAmber,
  signatureMustard = MiniMaxAccentAmber,
  oculusPurple = MiniMaxPrimary,
  linkColor = MiniMaxPrimary,
  linkActiveColor = MiniMaxPrimaryActive,
  infoColor = MiniMaxPrimary,
  infoBorder = MiniMaxPrimary,
  darkBand = Color(0xFF18191E),
  darkBandElevated = Color(0xFF23252C),
  badgeBg = MiniMaxSurfaceStrong,
  primaryTint = MiniMaxTintLight,
  statusPaidText = MiniMaxPositiveDeep,
  statusLateText = MiniMaxNegativeDeep,
  statusPartialDot = MiniMaxAccentAmber,
)

val MiniMaxDarkTokens = SyndicatDesignTokens(
  id = "minimax",
  designSystemName = "MiniMax",
  labelAr = "MiniMax المرجاني",
  primary = MiniMaxPrimary,
  onPrimary = Color.White,
  primaryActive = MiniMaxPrimaryActive,
  buyCta = MiniMaxPrimary,
  buyCtaPressed = MiniMaxPrimaryActive,
  onBuyCta = Color.White,
  inkDeep = Color.White,
  ink = Color(0xFFECEAE6),
  body = Color(0xFFB5B7C0),
  mute = Color(0xFF8A8E99),
  stone = Color(0xFF6E727D),
  canvas = MiniMaxDarkCard,
  canvasSoft = MiniMaxDarkSoft,
  divider = Color(0xFF252831),
  hairline = MiniMaxDarkBorder,
  disabledBg = MiniMaxDarkHigh,
  positive = MiniMaxDarkSuccess,
  positiveDeep = MiniMaxDarkSuccess,
  attention = Color(0xFFFBBF24),
  warning = Color(0xFFFBBF24),
  warningContent = Color.White,
  negative = MiniMaxDarkCritical,
  negativeDeep = MiniMaxDarkCritical,
  negativeBg = Color(0xFFD92D20),
  isDark = true,
  screenBg = MiniMaxDarkBg,
  navBg = MiniMaxDarkBg,
  cardBg = MiniMaxDarkCard,
  cardBorder = MiniMaxDarkBorder,
  featureCardRadiusDp = 20,
  cardRadiusDp = 18,
  contentCardRadiusDp = 12,
  buttonRadiusDp = 14,
  badgeRadiusDp = 10,
  fieldBg = MiniMaxDarkCard,
  fieldBorder = MiniMaxMuted,
  fieldFocusBorder = MiniMaxPrimaryOnDark,
  radioSelectedBorder = MiniMaxPrimaryOnDark,
  fieldRadiusDp = 10,
  trackBg = MiniMaxDarkBorder,
  surfaceSoft = MiniMaxDarkSoft,
  surfaceStrong = MiniMaxDarkHigh,
  signatureForest = MiniMaxDarkSoft,
  signatureCoral = MiniMaxDarkSoft,
  signatureCream = MiniMaxDarkSoft,
  signatureMint = MiniMaxDarkSoft,
  signaturePeach = MiniMaxDarkSoft,
  signatureYellow = Color(0xFFFBBF24),
  signatureMustard = Color(0xFFFBBF24),
  oculusPurple = MiniMaxPrimaryOnDark,
  linkColor = MiniMaxPrimaryOnDark,
  linkActiveColor = Color(0xFFFFA596),
  infoColor = MiniMaxPrimaryOnDark,
  infoBorder = MiniMaxPrimaryOnDark,
  darkBand = MiniMaxDarkSoft,
  darkBandElevated = MiniMaxDarkHigh,
  badgeBg = MiniMaxDarkHigh,
  primaryTint = MiniMaxDarkTint,
  statusPaidText = MiniMaxDarkSuccess,
  statusLateText = MiniMaxDarkCritical,
  statusPartialDot = Color(0xFFFBBF24),
)

// Compatibility aliases
val MetaLightTokens = MetaThemeLightTokens
val MetaDarkTokens = MetaThemeDarkTokens
val AirtableLightTokens = CoinbaseLightTokens
val AirtableDarkTokens = CoinbaseDarkTokens

fun tokensForThemeStyle(themeStyle: String?, darkTheme: Boolean): SyndicatDesignTokens {
  return when (resolveVisualThemeFamily(themeStyle)) {
    "meta" -> if (darkTheme) MetaThemeDarkTokens else MetaThemeLightTokens
    "minimax" -> if (darkTheme) MiniMaxDarkTokens else MiniMaxLightTokens
    else -> if (darkTheme) CoinbaseDarkTokens else CoinbaseLightTokens
  }
}

val LocalSyndicatTokens = staticCompositionLocalOf { CoinbaseLightTokens }

fun isSpecialCotisationAmount(amountDh: Long, defaultMonthlyFee: Long = 50L): Boolean {
  return if (defaultMonthlyFee == 50L) {
    amountDh >= 100L || (amountDh > 0L && amountDh != 50L)
  } else {
    amountDh > 0L && amountDh != defaultMonthlyFee
  }
}

/**
 * Normal amount = positive status color; special amount = active theme brand link/accent color.
 */
@Composable
fun cotisationColorForAmount(
  amountDh: Long,
  defaultMonthlyFee: Long = 50L,
  forceLightPaper: Boolean = false,
): Color {
  val tokens = LocalSyndicatTokens.current
  val lightPaper = forceLightPaper || !tokens.isDark
  return if (isSpecialCotisationAmount(amountDh, defaultMonthlyFee)) {
    if (lightPaper) tokens.primary else tokens.linkColor
  } else {
    if (lightPaper) tokens.positiveDeep else tokens.positive
  }
}