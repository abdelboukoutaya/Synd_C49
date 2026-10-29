package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

// ============================================================================
// Coinbase Design System — palette (source : DESIGN.md Coinbase)
// Coinbase Blue is the single brand voltage; everything else is white, ink and soft gray.
// ============================================================================

// Brand & accent
val CoinbasePrimary = Color(0xFF0052FF)          // {colors.primary} — every primary CTA pill, links
val CoinbasePrimaryActive = Color(0xFF003ECC)    // {colors.primary-active} — pressed state
val CoinbasePrimaryDisabled = Color(0xFFA8B8CC)  // {colors.primary-disabled}
val CoinbaseOnPrimary = Color(0xFFFFFFFF)        // {colors.on-primary}
val CoinbaseAccentYellow = Color(0xFFF4B000)     // {colors.accent-yellow} — illustrative only, never an action color

// Surfaces
val CoinbaseCanvas = Color(0xFFFFFFFF)           // {colors.canvas} — page floor, cards
val CoinbaseSurfaceSoft = Color(0xFFF7F7F7)      // {colors.surface-soft} — subtle alternating bands
val CoinbaseSurfaceStrong = Color(0xFFEEF0F3)    // {colors.surface-strong} — secondary buttons, search pill, badges
val CoinbaseSurfaceDark = Color(0xFF0A0B0D)      // {colors.surface-dark} — dark editorial bands
val CoinbaseSurfaceDarkElevated = Color(0xFF16181C) // {colors.surface-dark-elevated} — cards on dark

// Hairlines
val CoinbaseHairline = Color(0xFFDEE1E6)         // {colors.hairline} — 1px dividers and card outlines
val CoinbaseHairlineSoft = Color(0xFFEEF0F3)     // {colors.hairline-soft}

// Text
val CoinbaseInk = Color(0xFF0A0B0D)              // {colors.ink} — headings, nav, strong text
val CoinbaseBody = Color(0xFF5B616E)             // {colors.body} — running text
val CoinbaseMuted = Color(0xFF7C828A)            // {colors.muted} — secondary labels
val CoinbaseMutedSoft = Color(0xFFA8ACB3)        // {colors.muted-soft} — disabled text
val CoinbaseOnDark = Color(0xFFFFFFFF)           // {colors.on-dark}
val CoinbaseOnDarkSoft = Color(0xFFA8ACB3)       // {colors.on-dark-soft}

// Semantics (the source defines them for text only, never for button or large background fills)
val CoinbaseSemanticUp = Color(0xFF05B169)       // {colors.semantic-up}
val CoinbaseSemanticDown = Color(0xFFCF202F)     // {colors.semantic-down}

// ----------------------------------------------------------------------------
// Adaptations for the syndicate app (derived, not in DESIGN.md — validate visually)
// ----------------------------------------------------------------------------
val CoinbasePositiveDeep = Color(0xFF087443)     // semantic-up darkened: ≈5.8:1 on white (semantic-up is only ≈2.8:1)
val CoinbaseNegativeDeep = Color(0xFFA81724)     // semantic-down darkened for text on tinted surfaces (≈7.5:1 on white)
val CoinbasePrimaryTintLight = Color(0xFFE6EEFF) // primary at ~10% on white (selected / info containers)
val CoinbaseSuccessTintLight = Color(0xFFE6F7F0)
val CoinbaseWarningTintLight = Color(0xFFFEF7E6)
val CoinbaseErrorTintLight = Color(0xFFFAE9EA)

// Dark mode (DESIGN.md only gives surface-dark and surface-dark-elevated; the rest is derived)
val CoinbaseDarkScreenBg = CoinbaseSurfaceDark            // #0A0B0D
val CoinbaseDarkCardBg = CoinbaseSurfaceDarkElevated      // #16181C
val CoinbaseDarkSurfaceLow = Color(0xFF111317)
val CoinbaseDarkSurfaceSoft = Color(0xFF1C1F24)
val CoinbaseDarkSurfaceHigh = Color(0xFF262A31)
val CoinbaseDarkHairline = Color(0xFF2D3138)
val CoinbaseDarkHairlineSoft = Color(0xFF22262C)
val CoinbaseDarkInkDeep = Color(0xFFFFFFFF)
val CoinbaseDarkInk = Color(0xFFE6E8EB)
val CoinbaseDarkCharcoal = CoinbaseOnDarkSoft             // #A8ACB3 — secondary text (≈7.9:1 on cards)
val CoinbaseDarkSteel = CoinbaseMuted                     // #7C828A — quiet labels (≈4.6:1 on cards)
val CoinbaseDarkBlueText = Color(0xFF5B8CFF)              // Coinbase Blue lightened for text/links on dark (≈6.3:1)
val CoinbaseDarkBlueContainer = Color(0xFF0D2B6B)
val CoinbasePrimarySoftOnDark = Color(0xFF8FB0FF)           // pressed link on dark surfaces
val CoinbaseDarkSuccess = CoinbaseSemanticUp              // ≈6.4:1 on dark cards
val CoinbaseDarkCritical = Color(0xFFFF6B78)              // semantic-down lightened for dark (≈6.5:1)
val CoinbaseDarkErrorContainer = Color(0xFF3B1219)

// ============================================================================
// Backwards-compatible aliases (Meta* and Airtable*) so the rest of the app keeps compiling.
// New code should use SyndicatDesignTokens (LocalSyndicatTokens) or the Coinbase* names.
// ============================================================================

// ---- Meta* aliases -------------------------------------------------------
val MetaPrimaryCobalt = CoinbasePrimary
val MetaPrimaryDeep = CoinbasePrimaryActive
val MetaPrimarySoft = CoinbaseDarkBlueText
val MetaOnPrimary = CoinbaseOnPrimary
val MetaInkButton = CoinbaseInk
val MetaOnInkButton = CoinbaseOnDark
val MetaFbBlue = CoinbasePrimary
val MetaRadioSelected = CoinbasePrimaryActive
val MetaLink = CoinbasePrimary
val MetaOculusPurple = CoinbasePrimary          // Coinbase has no secondary brand color
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

// ---- Airtable* aliases ---------------------------------------------------
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

// Signature showcase surfaces mapped to Coinbase's white + ink + soft-gray system
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
// Design tokens (same field names as before so existing screens keep working)
// ============================================================================

@Immutable
data class SyndicatDesignTokens(
  val id: String = "airtable", // Stored Firestore config key preserved for rule compatibility
  val designSystemName: String = "Coinbase",
  val labelAr: String = "الأزرق المؤسسي",
  val primary: Color,               // {colors.primary} Coinbase Blue — every primary CTA pill (one per screen)
  val onPrimary: Color,             // #FFFFFF
  val primaryActive: Color,         // {colors.primary-active} #003ECC
  val buyCta: Color,                // same Coinbase Blue (kept for compatibility)
  val buyCtaPressed: Color,         // #003ECC
  val onBuyCta: Color,              // #FFFFFF
  val inkDeep: Color,               // {colors.ink} #0A0B0D (white in dark)
  val ink: Color,                   // {colors.ink}
  val body: Color,                  // {colors.body} #5B616E
  val mute: Color,                  // {colors.muted} #7C828A
  val stone: Color,                 // {colors.muted-soft} #A8ACB3 — disabled / decorative only
  val canvas: Color,                // {colors.canvas} #FFFFFF
  val canvasSoft: Color,            // {colors.surface-strong} #EEF0F3 — tiles, search pill, table header
  val divider: Color,               // {colors.hairline-soft} #EEF0F3 — row dividers
  val hairline: Color,              // {colors.hairline} #DEE1E6 — card outlines
  val disabledBg: Color,            // {colors.primary-disabled} #A8B8CC
  val positive: Color,              // {colors.semantic-up} #05B169 (icons, dark mode text)
  val positiveDeep: Color,          // derived #087443 — text on light surfaces (≥4.5:1)
  val attention: Color,             // {colors.accent-yellow} #F4B000
  val warning: Color,               // {colors.accent-yellow} #F4B000
  val warningContent: Color,        // {colors.ink} — text next to the yellow indicator
  val negative: Color,              // {colors.semantic-down} #CF202F
  val negativeDeep: Color,          // derived #A81724 — text on tinted surfaces
  val negativeBg: Color,            // solid red kept for compatibility (white text, ≈5.4:1)
  val isDark: Boolean,
  val screenBg: Color = canvas,
  val navBg: Color = canvas,
  val cardBg: Color = canvas,
  val cardBorder: Color = hairline,
  // Coinbase radius scale: lg=16dp (mid cards), xl=24dp (feature cards, tiles), md=12dp (inputs), pill=100dp
  val featureCardRadiusDp: Int = 24,
  val cardRadiusDp: Int = 24,
  val contentCardRadiusDp: Int = 16,
  val buttonRadiusDp: Int = 100,
  val badgeRadiusDp: Int = 100,
  val defaultCardPaddingDp: Int = 24, // 32px on desktop, reduced for phones
  val fieldBg: Color = canvas,
  // DESIGN.md uses a 1px hairline; on phones a darker border keeps ≈3:1 non-text contrast
  val fieldBorder: Color = CoinbaseMuted,
  val fieldFocusBorder: Color = CoinbasePrimary,   // 2px Coinbase Blue on focus
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
  val oculusPurple: Color = CoinbasePrimary, // no secondary brand color in Coinbase
  val linkColor: Color = CoinbasePrimary,
  val linkActiveColor: Color = CoinbasePrimaryActive,
  val infoColor: Color = CoinbasePrimary,
  val infoBorder: Color = CoinbasePrimary,
  val monoFontFamily: FontFamily = InterNumericFontFamily,   // substitute for CoinbaseMono
  val numericFontFamily: FontFamily = InterNumericFontFamily,
  // ---- New tokens (Coinbase) ----
  val onDark: Color = CoinbaseOnDark,
  val onDarkSoft: Color = CoinbaseOnDarkSoft,
  val darkBand: Color = CoinbaseSurfaceDark,                 // full-bleed dark editorial band / hero card
  val darkBandElevated: Color = CoinbaseSurfaceDarkElevated, // card floating on a dark band
  val badgeBg: Color = CoinbaseSurfaceStrong,                // neutral pill behind status text
  val primaryTint: Color = CoinbasePrimaryTintLight,         // selected / info container
  val statusPaidText: Color = positiveDeep,                  // status = colored text + icon on a neutral pill
  val statusLateText: Color = negativeDeep,
  val statusPartialDot: Color = warning,
) {
  val themeStyle: String
    get() = id

  /**
   * Coinbase weight rhythm: 400 display & body, 500 nav/numbers, 600 titles & buttons, 700 emphasized body.
   */
  fun weight(requested: FontWeight): FontWeight = when {
    requested.weight <= 300 -> FontWeight.Light
    requested.weight >= 700 -> FontWeight.Bold
    requested.weight >= 600 -> FontWeight.SemiBold
    requested.weight >= 500 -> FontWeight.Medium
    else -> FontWeight.Normal
  }
}

val CoinbaseLightTokens = SyndicatDesignTokens(
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
  fieldBg = CoinbaseCanvas,
  fieldBorder = CoinbaseMuted,
  fieldFocusBorder = CoinbasePrimary,
  radioSelectedBorder = CoinbasePrimaryActive,
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
  primary = CoinbasePrimary,           // brand blue pill keeps white text (≈5.7:1) in dark mode too
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
  fieldBg = CoinbaseDarkCardBg,
  fieldBorder = CoinbaseMuted,
  fieldFocusBorder = CoinbaseDarkBlueText,
  radioSelectedBorder = CoinbaseDarkBlueText,
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

// Compatibility names
val MetaLightTokens = CoinbaseLightTokens
val MetaDarkTokens = CoinbaseDarkTokens
val AirtableLightTokens = CoinbaseLightTokens
val AirtableDarkTokens = CoinbaseDarkTokens

val LocalSyndicatTokens = staticCompositionLocalOf { CoinbaseLightTokens }

fun isSpecialCotisationAmount(amountDh: Long, defaultMonthlyFee: Long = 50L): Boolean {
  return if (defaultMonthlyFee == 50L) {
    amountDh >= 100L || (amountDh > 0L && amountDh != 50L)
  } else {
    amountDh > 0L && amountDh != defaultMonthlyFee
  }
}

/**
 * Normal amount = positive (dark green on light surfaces for contrast, semantic-up on dark).
 * Special amount = Coinbase Blue accent (inline accent moment).
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
    if (lightPaper) CoinbasePrimary else CoinbaseDarkBlueText
  } else {
    if (lightPaper) CoinbasePositiveDeep else CoinbaseDarkSuccess
  }
}
