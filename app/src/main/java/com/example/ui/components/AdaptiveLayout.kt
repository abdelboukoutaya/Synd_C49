package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalSyndicatTokens

enum class SyndicatWidthClass {
  COMPACT_NARROW,
  COMPACT,
  MEDIUM,
  EXPANDED,
}

@Immutable
data class SyndicatAdaptiveSpec(
  val widthClass: SyndicatWidthClass = SyndicatWidthClass.COMPACT,
  val availableWidthDp: Dp = 400.dp,
  val availableHeightDp: Dp = 800.dp,
  val isNarrowPhone: Boolean = false,
  val isVeryNarrowPhone: Boolean = false,
  val isCompactHeight: Boolean = false,
  val isMedium: Boolean = false,
  val isExpanded: Boolean = false,
  val isTabletOrWide: Boolean = false,
  val useRail: Boolean = false,
  val horizontalPadding: Dp = 16.dp,
  val cardPadding: Dp = 18.dp,
  val sectionSpacing: Dp = 14.dp,
  val listBottomPadding: Dp = 96.dp,
  val readableMaxWidth: Dp = 720.dp,
  val wideContainerMaxWidth: Dp = 1200.dp,
  val gridColumns: Int = 1,
  val bannerHeight: Dp = 196.dp,
)

fun computeSyndicatAdaptiveSpec(
  maxWidth: Dp,
  maxHeight: Dp,
): SyndicatAdaptiveSpec {
  val widthClass = when {
    maxWidth < 360.dp -> SyndicatWidthClass.COMPACT_NARROW
    maxWidth < 600.dp -> SyndicatWidthClass.COMPACT
    maxWidth < 840.dp -> SyndicatWidthClass.MEDIUM
    else -> SyndicatWidthClass.EXPANDED
  }
  val isNarrow = maxWidth < 360.dp
  val isVeryNarrow = maxWidth < 340.dp
  val isCompactHeight = maxHeight < 480.dp
  val isMedium = widthClass == SyndicatWidthClass.MEDIUM
  val isExpanded = widthClass == SyndicatWidthClass.EXPANDED
  val useRail = maxWidth >= 600.dp

  val horizontalPadding = when (widthClass) {
    SyndicatWidthClass.COMPACT_NARROW -> 12.dp
    SyndicatWidthClass.COMPACT -> 16.dp
    SyndicatWidthClass.MEDIUM -> 22.dp
    SyndicatWidthClass.EXPANDED -> 24.dp
  }
  val cardPadding = when (widthClass) {
    SyndicatWidthClass.COMPACT_NARROW -> 14.dp
    SyndicatWidthClass.COMPACT -> 18.dp
    SyndicatWidthClass.MEDIUM -> 20.dp
    SyndicatWidthClass.EXPANDED -> 20.dp
  }
  val sectionSpacing = when (widthClass) {
    SyndicatWidthClass.COMPACT_NARROW -> 12.dp
    SyndicatWidthClass.COMPACT -> 14.dp
    SyndicatWidthClass.MEDIUM, SyndicatWidthClass.EXPANDED -> 16.dp
  }
  val bannerHeight = when {
    isCompactHeight -> 142.dp
    isNarrow -> 176.dp
    isExpanded -> 212.dp
    else -> 196.dp
  }
  return SyndicatAdaptiveSpec(
    widthClass = widthClass,
    availableWidthDp = maxWidth,
    availableHeightDp = maxHeight,
    isNarrowPhone = isNarrow,
    isVeryNarrowPhone = isVeryNarrow,
    isCompactHeight = isCompactHeight,
    isMedium = isMedium,
    isExpanded = isExpanded,
    isTabletOrWide = useRail,
    useRail = useRail,
    horizontalPadding = horizontalPadding,
    cardPadding = cardPadding,
    sectionSpacing = sectionSpacing,
    listBottomPadding = if (useRail) 28.dp else 96.dp,
    readableMaxWidth = 720.dp,
    wideContainerMaxWidth = 1200.dp,
    gridColumns = when {
      isExpanded -> 2
      isMedium && maxWidth >= 700.dp -> 2
      else -> 1
    },
    bannerHeight = bannerHeight,
  )
}

val LocalSyndicatAdaptiveSpec = compositionLocalOf { SyndicatAdaptiveSpec() }

/**
 * Conteneur centré limitant la largeur maximale sur tablette/bureau sans contraindre
 * l'espace disponible sur les petits téléphones.
 */
@Composable
fun AdaptiveContentBox(
  modifier: Modifier = Modifier,
  maxWidth: Dp = LocalSyndicatAdaptiveSpec.current.readableMaxWidth,
  content: @Composable BoxScope.() -> Unit,
) {
  Box(
    modifier = modifier.fillMaxWidth(),
    contentAlignment = Alignment.TopCenter,
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = maxWidth),
      content = content,
    )
  }
}

/**
 * Retourne une couleur et bordure sensibles au survol souris/trackpad pour ChromeOS, DeX et tablettes.
 */
@Composable
fun rememberSyndicatHoverBorder(
  baseBorder: BorderStroke?,
  enabled: Boolean = true,
  hoverColor: Color = Color.Unspecified,
): Pair<MutableInteractionSource, BorderStroke?> {
  val tokens = LocalSyndicatTokens.current
  val interactionSource = remember { MutableInteractionSource() }
  val isHovered by interactionSource.collectIsHoveredAsState()
  val resolvedHoverColor = if (hoverColor == Color.Unspecified) {
    tokens.primary.copy(alpha = 0.55f)
  } else {
    hoverColor
  }
  val effectiveBorder = if (enabled && isHovered) {
    BorderStroke((baseBorder?.width ?: 1.dp), resolvedHoverColor)
  } else {
    baseBorder
  }
  return interactionSource to effectiveBorder
}

fun Modifier.syndicatPointerHover(
  interactionSource: MutableInteractionSource,
  enabled: Boolean = true,
): Modifier = if (enabled) this.hoverable(interactionSource = interactionSource) else this
