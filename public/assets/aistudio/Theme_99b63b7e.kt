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

/**
 * Coinbase Design System — Light Material 3 ColorScheme:
 * pure white canvas (#FFFFFF), Ink (#0A0B0D), soft gray surfaces (#F7F7F7 / #EEF0F3)
 * and Coinbase Blue (#0052FF) as the single brand voltage for primary actions.
 * Coinbase has no secondary brand color: tertiary is a neutral gray.
 */
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
  // Outlines: muted gray (~3.9:1) for field and outlined-button borders; hairline for dividers and cards
  outline = CoinbaseMuted,
  outlineVariant = CoinbaseHairline,
  error = CoinbaseSemanticDown,
  onError = CoinbaseCanvas,
  errorContainer = CoinbaseErrorTintLight,
  onErrorContainer = CoinbaseSemanticDown,
  scrim = CoinbaseInk.copy(alpha = 0.48f),
)

/**
 * Coinbase Design System — Dark Material 3 ColorScheme:
 * anchored on surface-dark (#0A0B0D) with surface-dark-elevated (#16181C) cards.
 * The brand blue is lightened (#5B8CFF) for text, icons and links; solid CTA pills keep #0052FF
 * (see CoinbaseDarkTokens.buyCta). Values other than the two dark surfaces are derived.
 */
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

/**
 * Coinbase border radius scale (DESIGN.md):
 * - extraSmall : 12dp ({rounded.md} — form inputs)
 * - small      : 16dp ({rounded.lg} — mid-size cards, list items)
 * - medium     : 24dp ({rounded.xl} — feature cards, product-UI cards, dialogs)
 * - large      : 24dp ({rounded.xl} — Coinbase has no larger card radius)
 * - extraLarge : 100dp ({rounded.pill} — every CTA button, search pill, badge)
 */
val AppShapes = Shapes(
  extraSmall = RoundedCornerShape(12.dp),
  small = RoundedCornerShape(16.dp),
  medium = RoundedCornerShape(24.dp),
  large = RoundedCornerShape(24.dp),
  extraLarge = RoundedCornerShape(100.dp),
)

@Composable
fun AppTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  isArabic: Boolean = true,
  fontScale: String = "normal",
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) darkScheme else lightScheme
  val tokens = if (darkTheme) CoinbaseDarkTokens else CoinbaseLightTokens
  val typography = buildCoinbaseTypography(
    isArabic = isArabic,
    scaleMultiplier = fontScaleMultiplier(fontScale),
  )

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window ?: return@SideEffect
      @Suppress("DEPRECATION")
      window.statusBarColor = colorScheme.background.toArgb()
      @Suppress("DEPRECATION")
      window.navigationBarColor = tokens.navBg.toArgb()
      val insetsController = WindowCompat.getInsetsController(window, view)
      insetsController.isAppearanceLightStatusBars = !darkTheme
      insetsController.isAppearanceLightNavigationBars = !darkTheme
    }
  }

  CompositionLocalProvider(
    LocalSyndicatTokens provides tokens,
    // Arabic UI is laid out right-to-left. Remove this line if the layout direction is already set elsewhere.
    LocalLayoutDirection provides if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr,
  ) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = typography,
      shapes = AppShapes,
      content = content,
    )
  }
}
