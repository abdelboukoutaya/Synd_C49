package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// ============================================================================
// 1. Coinbase ColorSchemes
// ============================================================================

val lightScheme: ColorScheme = lightColorScheme(
  primary = CoinbasePrimary,
  onPrimary = CoinbaseOnPrimary,
  primaryContainer = CoinbasePrimaryTintLight,
  onPrimaryContainer = CoinbasePrimaryActive,
  secondary = CoinbaseInk,
  onSecondary = CoinbaseCanvas,
  secondaryContainer = CoinbaseSurfaceStrong,
  onSecondaryContainer = CoinbaseInk,
  tertiary = CoinbaseBody,
  onTertiary = CoinbaseCanvas,
  tertiaryContainer = CoinbaseSurfaceStrong,
  onTertiaryContainer = CoinbaseInk,
  background = CoinbaseCanvas,
  onBackground = CoinbaseInk,
  surface = CoinbaseCanvas,
  onSurface = CoinbaseInk,
  surfaceVariant = CoinbaseSurfaceStrong,
  onSurfaceVariant = CoinbaseBody,
  surfaceTint = Color.Transparent,
  inverseSurface = CoinbaseSurfaceDark,
  inverseOnSurface = CoinbaseOnDark,
  inversePrimary = CoinbaseDarkBlueText,
  surfaceDim = CoinbaseSurfaceStrong,
  surfaceBright = CoinbaseCanvas,
  surfaceContainerLowest = CoinbaseCanvas,
  surfaceContainerLow = CoinbaseSurfaceSoft,
  surfaceContainer = CoinbaseSurfaceSoft,
  surfaceContainerHigh = CoinbaseSurfaceStrong,
  surfaceContainerHighest = CoinbaseSurfaceStrong,
  outline = CoinbaseMuted,
  outlineVariant = CoinbaseHairline,
  error = CoinbaseSemanticDown,
  onError = CoinbaseCanvas,
  errorContainer = CoinbaseErrorTintLight,
  onErrorContainer = CoinbaseSemanticDown,
  scrim = CoinbaseInk.copy(alpha = 0.48f),
)

val darkScheme: ColorScheme = darkColorScheme(
  primary = CoinbaseDarkBlueText,
  onPrimary = CoinbaseInk,
  primaryContainer = CoinbaseDarkBlueContainer,
  onPrimaryContainer = CoinbaseDarkInkDeep,
  secondary = CoinbaseDarkInkDeep,
  onSecondary = CoinbaseInk,
  secondaryContainer = CoinbaseDarkSurfaceSoft,
  onSecondaryContainer = CoinbaseDarkInkDeep,
  tertiary = CoinbaseDarkCharcoal,
  onTertiary = CoinbaseInk,
  tertiaryContainer = CoinbaseDarkSurfaceHigh,
  onTertiaryContainer = CoinbaseDarkInk,
  background = CoinbaseDarkScreenBg,
  onBackground = CoinbaseDarkInkDeep,
  surface = CoinbaseDarkCardBg,
  onSurface = CoinbaseDarkInk,
  surfaceVariant = CoinbaseDarkSurfaceSoft,
  onSurfaceVariant = CoinbaseDarkCharcoal,
  surfaceTint = Color.Transparent,
  inverseSurface = CoinbaseCanvas,
  inverseOnSurface = CoinbaseInk,
  inversePrimary = CoinbasePrimary,
  surfaceDim = CoinbaseDarkScreenBg,
  surfaceBright = CoinbaseDarkSurfaceHigh,
  surfaceContainerLowest = CoinbaseDarkScreenBg,
  surfaceContainerLow = CoinbaseDarkSurfaceLow,
  surfaceContainer = CoinbaseDarkCardBg,
  surfaceContainerHigh = CoinbaseDarkSurfaceSoft,
  surfaceContainerHighest = CoinbaseDarkSurfaceHigh,
  outline = CoinbaseMuted,
  outlineVariant = CoinbaseDarkHairline,
  error = CoinbaseDarkCritical,
  onError = CoinbaseInk,
  errorContainer = CoinbaseDarkErrorContainer,
  onErrorContainer = CoinbaseDarkInkDeep,
  scrim = Color.Black.copy(alpha = 0.68f),
)

// ============================================================================
// 2. Meta ColorSchemes (Cobalt #0064E0 + Soft Cloud #F1F4F7 + Deep Ink #0A1317)
// ============================================================================

val metaLightScheme: ColorScheme = lightColorScheme(
  primary = MetaBrandCobalt,
  onPrimary = Color.White,
  primaryContainer = MetaTintLight,
  onPrimaryContainer = MetaBrandCobaltDeep,
  secondary = MetaDeepInk,
  onSecondary = Color.White,
  secondaryContainer = MetaSoftCloudStrong,
  onSecondaryContainer = MetaTextInk,
  tertiary = MetaOculusAccent,
  onTertiary = Color.White,
  tertiaryContainer = MetaSoftCloud,
  onTertiaryContainer = MetaTextInk,
  background = MetaSoftCloud,
  onBackground = MetaDeepInk,
  surface = Color.White,
  onSurface = MetaTextInk,
  surfaceVariant = MetaSoftCloudStrong,
  onSurfaceVariant = MetaBodyCharcoal,
  surfaceTint = Color.Transparent,
  inverseSurface = MetaDeepInk,
  inverseOnSurface = Color.White,
  inversePrimary = MetaBrandCobaltSoft,
  surfaceDim = MetaSoftCloudStrong,
  surfaceBright = Color.White,
  surfaceContainerLowest = Color.White,
  surfaceContainerLow = MetaSoftCloud,
  surfaceContainer = MetaSoftCloud,
  surfaceContainerHigh = MetaSoftCloudStrong,
  surfaceContainerHighest = MetaSoftCloudStrong,
  outline = MetaMutedSteel,
  outlineVariant = MetaLineHairline,
  error = MetaCriticalRed,
  onError = Color.White,
  errorContainer = CoinbaseErrorTintLight,
  onErrorContainer = MetaCriticalRed,
  scrim = MetaDeepInk.copy(alpha = 0.50f),
)

val metaDarkScheme: ColorScheme = darkColorScheme(
  primary = MetaBrandCobaltSoft,
  onPrimary = MetaDeepInk,
  primaryContainer = MetaDarkTint,
  onPrimaryContainer = Color.White,
  secondary = Color.White,
  onSecondary = MetaDeepInk,
  secondaryContainer = MetaDarkElevated,
  onSecondaryContainer = Color.White,
  tertiary = Color(0xFFD580FF),
  onTertiary = MetaDeepInk,
  tertiaryContainer = MetaDarkHigh,
  onTertiaryContainer = Color.White,
  background = MetaDarkBg,
  onBackground = Color.White,
  surface = MetaDarkCard,
  onSurface = Color(0xFFE6ECF2),
  surfaceVariant = MetaDarkElevated,
  onSurfaceVariant = Color(0xFFB0BEC9),
  surfaceTint = Color.Transparent,
  inverseSurface = Color.White,
  inverseOnSurface = MetaDeepInk,
  inversePrimary = MetaBrandCobalt,
  surfaceDim = MetaDarkBg,
  surfaceBright = MetaDarkHigh,
  surfaceContainerLowest = MetaDarkBg,
  surfaceContainerLow = MetaDarkCard,
  surfaceContainer = MetaDarkCard,
  surfaceContainerHigh = MetaDarkElevated,
  surfaceContainerHighest = MetaDarkHigh,
  outline = MetaStoneGray,
  outlineVariant = MetaDarkBorder,
  error = Color(0xFFFF6B81),
  onError = MetaDeepInk,
  errorContainer = CoinbaseDarkErrorContainer,
  onErrorContainer = Color.White,
  scrim = Color.Black.copy(alpha = 0.70f),
)

// ============================================================================
// 3. MiniMax ColorSchemes (Warm Alabaster + Coral Vermilion #D93F26)
// ============================================================================

val miniMaxLightScheme: ColorScheme = lightColorScheme(
  primary = MiniMaxPrimary,
  onPrimary = Color.White,
  primaryContainer = MiniMaxTintLight,
  onPrimaryContainer = MiniMaxPrimaryActive,
  secondary = MiniMaxInkDeep,
  onSecondary = Color.White,
  secondaryContainer = MiniMaxSurfaceStrong,
  onSecondaryContainer = MiniMaxInkDeep,
  tertiary = MiniMaxBody,
  onTertiary = Color.White,
  tertiaryContainer = MiniMaxSurfaceSoft,
  onTertiaryContainer = MiniMaxInk,
  background = MiniMaxWarmPaper,
  onBackground = MiniMaxInkDeep,
  surface = MiniMaxCardWhite,
  onSurface = MiniMaxInk,
  surfaceVariant = MiniMaxSurfaceStrong,
  onSurfaceVariant = MiniMaxBody,
  surfaceTint = Color.Transparent,
  inverseSurface = MiniMaxDarkBg,
  inverseOnSurface = Color.White,
  inversePrimary = MiniMaxPrimaryOnDark,
  surfaceDim = MiniMaxSurfaceStrong,
  surfaceBright = MiniMaxCardWhite,
  surfaceContainerLowest = MiniMaxCardWhite,
  surfaceContainerLow = MiniMaxWarmPaper,
  surfaceContainer = MiniMaxSurfaceSoft,
  surfaceContainerHigh = MiniMaxSurfaceStrong,
  surfaceContainerHighest = MiniMaxSurfaceStrong,
  outline = MiniMaxMuted,
  outlineVariant = MiniMaxHairline,
  error = MiniMaxNegativeDeep,
  onError = Color.White,
  errorContainer = CoinbaseErrorTintLight,
  onErrorContainer = MiniMaxNegativeDeep,
  scrim = MiniMaxInkDeep.copy(alpha = 0.48f),
)

val miniMaxDarkScheme: ColorScheme = darkColorScheme(
  primary = MiniMaxPrimaryOnDark,
  onPrimary = MiniMaxInkDeep,
  primaryContainer = MiniMaxDarkTint,
  onPrimaryContainer = Color.White,
  secondary = Color.White,
  onSecondary = MiniMaxInkDeep,
  secondaryContainer = MiniMaxDarkSoft,
  onSecondaryContainer = Color.White,
  tertiary = Color(0xFFB5B7C0),
  onTertiary = MiniMaxInkDeep,
  tertiaryContainer = MiniMaxDarkHigh,
  onTertiaryContainer = Color.White,
  background = MiniMaxDarkBg,
  onBackground = Color.White,
  surface = MiniMaxDarkCard,
  onSurface = Color(0xFFECEAE6),
  surfaceVariant = MiniMaxDarkSoft,
  onSurfaceVariant = Color(0xFFB5B7C0),
  surfaceTint = Color.Transparent,
  inverseSurface = MiniMaxCardWhite,
  inverseOnSurface = MiniMaxInkDeep,
  inversePrimary = MiniMaxPrimary,
  surfaceDim = MiniMaxDarkBg,
  surfaceBright = MiniMaxDarkHigh,
  surfaceContainerLowest = MiniMaxDarkBg,
  surfaceContainerLow = MiniMaxDarkCard,
  surfaceContainer = MiniMaxDarkCard,
  surfaceContainerHigh = MiniMaxDarkSoft,
  surfaceContainerHighest = MiniMaxDarkHigh,
  outline = MiniMaxMuted,
  outlineVariant = MiniMaxDarkBorder,
  error = MiniMaxDarkCritical,
  onError = MiniMaxInkDeep,
  errorContainer = CoinbaseDarkErrorContainer,
  onErrorContainer = Color.White,
  scrim = Color.Black.copy(alpha = 0.70f),
)

// ============================================================================
// Shapes per Design System
// ============================================================================

val AppShapes = Shapes(
  extraSmall = RoundedCornerShape(12.dp),
  small = RoundedCornerShape(16.dp),
  medium = RoundedCornerShape(24.dp),
  large = RoundedCornerShape(24.dp),
  extraLarge = RoundedCornerShape(100.dp),
)

val MetaShapes = Shapes(
  extraSmall = RoundedCornerShape(14.dp),
  small = RoundedCornerShape(16.dp),
  medium = RoundedCornerShape(24.dp),
  large = RoundedCornerShape(28.dp),
  extraLarge = RoundedCornerShape(100.dp),
)

val MiniMaxShapes = Shapes(
  extraSmall = RoundedCornerShape(10.dp),
  small = RoundedCornerShape(12.dp),
  medium = RoundedCornerShape(18.dp),
  large = RoundedCornerShape(20.dp),
  extraLarge = RoundedCornerShape(14.dp),
)

fun colorSchemeForStyle(themeStyle: String?, darkTheme: Boolean): ColorScheme {
  return when (resolveVisualThemeFamily(themeStyle)) {
    "meta" -> if (darkTheme) metaDarkScheme else metaLightScheme
    "minimax" -> if (darkTheme) miniMaxDarkScheme else miniMaxLightScheme
    else -> if (darkTheme) darkScheme else lightScheme
  }
}

fun shapesForStyle(themeStyle: String?): Shapes {
  return when (resolveVisualThemeFamily(themeStyle)) {
    "meta" -> MetaShapes
    "minimax" -> MiniMaxShapes
    else -> AppShapes
  }
}

@Composable
fun AppTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  isArabic: Boolean = true,
  fontScale: String = "normal",
  themeStyle: String = "coinbase",
  content: @Composable () -> Unit,
) {
  val family = resolveVisualThemeFamily(themeStyle)
  val colorScheme = colorSchemeForStyle(family, darkTheme)
  val tokens = tokensForThemeStyle(family, darkTheme)
  val shapes = shapesForStyle(family)
  val typography = buildAppTypography(
    themeStyle = family,
    isArabic = isArabic,
    scaleMultiplier = fontScaleMultiplier(fontScale),
  )

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window ?: return@SideEffect
      @Suppress("DEPRECATION")
      window.statusBarColor = tokens.screenBg.toArgb()
      @Suppress("DEPRECATION")
      window.navigationBarColor = tokens.navBg.toArgb()
      val insetsController = WindowCompat.getInsetsController(window, view)
      insetsController.isAppearanceLightStatusBars = !darkTheme
      insetsController.isAppearanceLightNavigationBars = !darkTheme
    }
  }

  CompositionLocalProvider(
    LocalSyndicatTokens provides tokens,
    LocalLayoutDirection provides if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr,
  ) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = typography,
      shapes = shapes,
      content = content,
    )
  }
}