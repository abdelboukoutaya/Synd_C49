package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
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
 * capsule active animée en dégradé et respect strict des WindowInsets.navigationBars.
 */
@Composable
fun SyndicatFloatingBottomDock(
  items: List<SyndicatNavItemSpec>,
  modifier: Modifier = Modifier,
) {
  val tokens = LocalSyndicatTokens.current
  val haptic = LocalHapticFeedback.current
  val dockBg = if (tokens.isDark) tokens.cardBg else Color.White
  val borderTint = tokens.cardBorder.copy(alpha = if (tokens.isDark) 0.95f else 0.90f)
  val topShine = Color.White.copy(alpha = if (tokens.isDark) 0.09f else 0.80f)

  Box(
    modifier = modifier
      .fillMaxWidth()
      .background(
        brush = Brush.verticalGradient(
          colors = listOf(
            Color.Transparent,
            tokens.screenBg.copy(alpha = 0.78f),
            tokens.screenBg.copy(alpha = 0.96f),
          ),
        ),
      )
      .windowInsetsPadding(WindowInsets.navigationBars)
      .padding(horizontal = 14.dp, vertical = 10.dp)
      .testTag("syndicat_bottom_nav_dock"),
  ) {
    Surface(
      shape = RoundedCornerShape(30.dp),
      color = dockBg,
      border = BorderStroke(1.dp, borderTint),
      tonalElevation = 0.dp,
      shadowElevation = if (tokens.isDark) 10.dp else 12.dp,
      modifier = Modifier
        .fillMaxWidth()
        .drawBehind {
          drawLine(
            color = topShine,
            start = Offset(28.dp.toPx(), 1.dp.toPx()),
            end = Offset(size.width - 28.dp.toPx(), 1.dp.toPx()),
            strokeWidth = 1.dp.toPx(),
          )
        },
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        items.forEach { item ->
          DockIconTabItem(
            item = item,
            dockBg = dockBg,
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
  onTap: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val tokens = LocalSyndicatTokens.current
  val selectedState = item.isSelected

  val iconScale by animateFloatAsState(
    targetValue = if (selectedState) 1.06f else 0.96f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessMediumLow,
    ),
    label = "nav_icon_scale_${item.route}",
  )
  val dotWidth by animateDpAsState(
    targetValue = if (selectedState) 16.dp else 0.dp,
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    label = "nav_dot_width_${item.route}",
  )
  val inactiveIconTint by animateColorAsState(
    targetValue = if (tokens.isDark) tokens.inkDeep.copy(alpha = 0.62f) else tokens.inkDeep.copy(alpha = 0.56f),
    label = "nav_icon_tint_${item.route}",
  )

  val activeBrush = Brush.linearGradient(
    colors = listOf(tokens.linkColor, tokens.linkActiveColor),
  )

  Box(
    modifier = modifier
      .minimumInteractiveComponentSize()
      .heightIn(min = 52.dp)
      .clip(RoundedCornerShape(22.dp))
      .clickable(
        role = Role.Tab,
        onClick = onTap,
      )
      .semantics {
        selected = selectedState
      }
      .padding(vertical = 2.dp)
      .testTag("nav_${item.route}"),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
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
            .width(48.dp)
            .height(38.dp)
            .clip(RoundedCornerShape(19.dp))
            .then(
              if (selectedState) {
                Modifier.background(brush = activeBrush)
              } else {
                Modifier
              },
            ),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = if (selectedState) item.filledIcon else item.outlinedIcon,
            contentDescription = item.label,
            tint = if (selectedState) Color.White else inactiveIconTint,
            modifier = Modifier.size(23.dp),
          )
        }

        if (item.badgeCount > 0) {
          NavCounterBadgePill(
            count = item.badgeCount,
            cutoutColor = dockBg,
            route = item.route,
            modifier = Modifier
              .align(Alignment.TopEnd)
              .offset(x = 2.dp, y = (-2).dp),
          )
        }
      }

      if (dotWidth > 0.dp) {
        Spacer(modifier = Modifier.height(3.dp))
        Box(
          modifier = Modifier
            .width(dotWidth)
            .height(3.dp)
            .clip(CircleShape)
            .background(tokens.linkColor),
        )
      }
    }
  }
}

/**
 * Rail latéral tablette & écran large assorti au dock iconique sans texte surchargé.
 */
@Composable
fun SyndicatNavigationRailPane(
  items: List<SyndicatNavItemSpec>,
  modifier: Modifier = Modifier,
) {
  val tokens = LocalSyndicatTokens.current
  val haptic = LocalHapticFeedback.current
  val railBg = if (tokens.isDark) tokens.cardBg else Color.White

  Row(
    modifier = modifier
      .fillMaxHeight()
      .background(railBg)
      .testTag("navigation_rail"),
  ) {
    Column(
      modifier = Modifier
        .width(76.dp)
        .fillMaxHeight()
        .windowInsetsPadding(WindowInsets.safeDrawing)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 10.dp, vertical = 14.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = tokens.linkColor,
        tonalElevation = 0.dp,
        shadowElevation = 2.dp,
        modifier = Modifier.size(40.dp),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Text(
            text = "SY",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      items.forEach { item ->
        DockIconTabItem(
          item = item,
          dockBg = railBg,
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
