package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalSyndicatTokens

data class SyndicatNavItemSpec(
  val route: String,
  val label: String,
  val filledIcon: ImageVector,
  val outlinedIcon: ImageVector,
  val badgeCount: Int = 0,
  val isSelected: Boolean = false,
  val onClick: () -> Unit,
)

/**
 * Barre de navigation flottante purement iconique (sans texte), avec icônes minimalistes explicatives,
 * capsule active sobre unie et adaptation stricte des petits écrans (320dp+) jusqu'aux tablettes.
 */
@Composable
fun SyndicatFloatingBottomDock(
  items: List<SyndicatNavItemSpec>,
  modifier: Modifier = Modifier,
) {
  val tokens = LocalSyndicatTokens.current
  val haptic = LocalHapticFeedback.current
  val dockBg = if (tokens.isDark) tokens.cardBg else tokens.navBg
  val borderTint = tokens.cardBorder

  BoxWithConstraints(
    modifier = modifier
      .fillMaxWidth()
      .background(
        brush = Brush.verticalGradient(
          colors = listOf(
            Color.Transparent,
            tokens.screenBg.copy(alpha = 0.88f),
          ),
        ),
      )
      .windowInsetsPadding(WindowInsets.navigationBars)
      .testTag("syndicat_bottom_nav_dock"),
    contentAlignment = Alignment.Center,
  ) {
    val isNarrow = maxWidth < 360.dp
    val outerHorizontalPadding = if (isNarrow) 6.dp else 16.dp
    val outerVerticalPadding = if (isNarrow) 8.dp else 10.dp
    val innerHorizontalPadding = if (isNarrow) 4.dp else 8.dp
    val itemSpacing = if (isNarrow) 2.dp else 4.dp

    Surface(
      shape = RoundedCornerShape(if (isNarrow) 20.dp else 24.dp),
      color = dockBg,
      border = BorderStroke(tokens.borderStrokeDp.dp, borderTint),
      tonalElevation = 0.dp,
      shadowElevation = if (tokens.isDark) 2.dp else 3.dp,
      modifier = Modifier
        .padding(horizontal = outerHorizontalPadding, vertical = outerVerticalPadding)
        .fillMaxWidth()
        .widthIn(max = 560.dp),
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = innerHorizontalPadding, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(itemSpacing),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        items.forEach { item ->
          DockIconTabItem(
            item = item,
            dockBg = dockBg,
            compactWidth = isNarrow,
            onTap = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              item.onClick()
            },
            modifier = Modifier.weight(1f),
          )
        }
      }
    }
  }
}

@Composable
private fun DockIconTabItem(
  item: SyndicatNavItemSpec,
  dockBg: Color,
  compactWidth: Boolean = false,
  compactHeight: Boolean = false,
  onTap: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val tokens = LocalSyndicatTokens.current
  val selectedState = item.isSelected
  val interactionSource = remember { MutableInteractionSource() }
  val isHovered by interactionSource.collectIsHoveredAsState()

  val iconScale by animateFloatAsState(
    targetValue = if (selectedState) 1.0f else if (isHovered) 0.99f else 0.96f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioNoBouncy,
      stiffness = Spring.StiffnessMedium,
    ),
    label = "nav_icon_scale_${item.route}",
  )
  val pillColor by animateColorAsState(
    targetValue = when {
      selectedState -> tokens.primary
      isHovered -> tokens.surfaceStrong
      else -> Color.Transparent
    },
    animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
    label = "nav_pill_bg_${item.route}",
  )
  val iconTint by animateColorAsState(
    targetValue = if (selectedState) tokens.onPrimary else if (isHovered) tokens.inkDeep else tokens.body,
    animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
    label = "nav_icon_tint_${item.route}",
  )

  val pillWidth = if (compactWidth) 42.dp else 48.dp
  val pillHeight = if (compactHeight) 36.dp else 38.dp
  val iconSize = if (compactWidth) 20.dp else 22.dp

  Box(
    modifier = modifier
      .minimumInteractiveComponentSize()
      .heightIn(min = if (compactHeight) 48.dp else 50.dp)
      .clip(RoundedCornerShape(16.dp))
      .hoverable(interactionSource = interactionSource)
      .clickable(
        interactionSource = interactionSource,
        indication = androidx.compose.material3.ripple(),
        role = Role.Tab,
        onClick = onTap,
      )
      .semantics {
        selected = selectedState
      }
      .padding(vertical = if (compactHeight) 1.dp else 2.dp)
      .testTag("nav_${item.route}"),
    contentAlignment = Alignment.Center,
  ) {
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .graphicsLayer {
          scaleX = iconScale
          scaleY = iconScale
        },
    ) {
      Box(
        modifier = Modifier
          .width(pillWidth)
          .height(pillHeight)
          .clip(RoundedCornerShape(14.dp))
          .background(pillColor),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = if (selectedState) item.filledIcon else item.outlinedIcon,
          contentDescription = item.label,
          tint = iconTint,
          modifier = Modifier.size(iconSize),
        )
      }

      if (item.badgeCount > 0) {
        NavCounterBadgePill(
          count = item.badgeCount,
          cutoutColor = dockBg,
          route = item.route,
          modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(x = if (compactWidth) 1.dp else 2.dp, y = (-2).dp),
        )
      }
    }
  }
}

/**
 * Rail latéral tablette & écran large (ou paysage) assorti au dock iconique sans texte surchargé.
 */
@Composable
fun SyndicatNavigationRailPane(
  items: List<SyndicatNavItemSpec>,
  modifier: Modifier = Modifier,
) {
  val tokens = LocalSyndicatTokens.current
  val adaptive = LocalSyndicatAdaptiveSpec.current
  val haptic = LocalHapticFeedback.current
  val railBg = if (tokens.isDark) tokens.cardBg else tokens.navBg
  val compactHeight = adaptive.isCompactHeight

  Row(
    modifier = modifier
      .fillMaxHeight()
      .background(railBg)
      .testTag("navigation_rail"),
  ) {
    Column(
      modifier = Modifier
        .width(if (compactHeight) 72.dp else 76.dp)
        .fillMaxHeight()
        .windowInsetsPadding(WindowInsets.safeDrawing)
        .verticalScroll(rememberScrollState())
        .padding(
          horizontal = 10.dp,
          vertical = if (compactHeight) 8.dp else 14.dp,
        ),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(if (compactHeight) 4.dp else 10.dp),
    ) {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = tokens.primary,
        border = BorderStroke(1.dp, tokens.primaryActive.copy(alpha = 0.25f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.size(if (compactHeight) 36.dp else 40.dp),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Text(
            text = "SY",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = tokens.onPrimary,
          )
        }
      }

      if (!compactHeight) {
        Spacer(modifier = Modifier.height(4.dp))
      }

      items.forEach { item ->
        DockIconTabItem(
          item = item,
          dockBg = railBg,
          compactHeight = compactHeight,
          onTap = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            item.onClick()
          },
          modifier = Modifier.fillMaxWidth(),
        )
      }
    }
    VerticalDivider(thickness = 1.dp, color = tokens.divider)
  }
}

@Composable
private fun NavCounterBadgePill(
  count: Int,
  cutoutColor: Color,
  route: String,
  modifier: Modifier = Modifier,
) {
  val tokens = LocalSyndicatTokens.current
  Box(
    modifier = modifier
      .defaultMinSize(minWidth = 17.dp, minHeight = 17.dp)
      .clip(CircleShape)
      .background(tokens.negativeDeep)
      .border(1.5.dp, cutoutColor, CircleShape)
      .padding(horizontal = 4.dp, vertical = 1.dp)
      .testTag("nav_badge_$route"),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = if (count > 99) "99+" else count.toString(),
      style = MaterialTheme.typography.labelSmall.copy(
        fontSize = 9.sp,
        lineHeight = 10.sp,
      ),
      fontWeight = FontWeight.Bold,
      color = Color.White,
      maxLines = 1,
      softWrap = false,
    )
  }
}
