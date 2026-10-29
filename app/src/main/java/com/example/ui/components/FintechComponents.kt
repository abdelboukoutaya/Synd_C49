package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.PaymentBadgeStatus
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.LocalSyndicatTokens
import com.example.ui.theme.MetaAttention
import com.example.ui.theme.MetaCanvas
import com.example.ui.theme.MetaCobaltTintLight
import com.example.ui.theme.MetaCritical
import com.example.ui.theme.MetaInkDeep
import com.example.ui.theme.MetaOculusPurple
import com.example.ui.theme.MetaPrimaryCobalt
import com.example.ui.theme.MetaPrimarySoft
import com.example.ui.theme.MetaSuccess
import com.example.ui.theme.MetaWarning

/**
 * Meta `card-icon-feature` / `why-buy-tile`:
 * Stark white canvas (`#FFFFFF` light / `#131D24` dark), `{rounded.xl}` (16dp) corners,
 * `1px solid {colors.hairline-soft}` (`#DEE3E9`), flat 0dp elevation.
 */
@Composable
fun FintechWhiteCard(
  modifier: Modifier = Modifier,
  padding: Dp = Dp.Unspecified,
  onClick: (() -> Unit)? = null,
  elevatedCheckout: Boolean = false,
  content: @Composable ColumnScope.() -> Unit,
) {
  val tokens = LocalSyndicatTokens.current
  val resolvedPadding = if (padding == Dp.Unspecified) tokens.defaultCardPaddingDp.dp else padding
  val shape = RoundedCornerShape(tokens.contentCardRadiusDp.dp)
  val baseMod = if (onClick != null) {
    modifier.clip(shape).clickable(onClick = onClick)
  } else {
    modifier
  }
  Surface(
    modifier = baseMod,
    shape = shape,
    color = tokens.cardBg,
    border = BorderStroke(1.dp, tokens.cardBorder),
    tonalElevation = 0.dp,
    shadowElevation = if (elevatedCheckout) 2.dp else 0.dp,
  ) {
    Column(
      modifier = Modifier.padding(resolvedPadding),
      content = content,
    )
  }
}

/**
 * Meta `card-promo-strip` / `hero-band-marketing`:
 * Deep Ink (`#0A1317`), `{rounded.xxxl}` (32dp) showcase rounding, subtle atmospheric radial depth,
 * crisp white typography, zero outer drop shadow.
 */
@Composable
fun FintechDarkFeatureCard(
  modifier: Modifier = Modifier,
  padding: Dp = 28.dp,
  content: @Composable ColumnScope.() -> Unit,
) {
  AirtableSignatureCard(
    backgroundColor = MetaInkDeep,
    modifier = modifier,
    padding = padding,
    content = content,
  )
}

/**
 * Meta `warranty-card` (affirmative / reassurance surface):
 * Soft Cloud (`#F1F4F7` light / `#1B2730` dark), `{rounded.xxl}` (24dp) rounding, zero border/shadow.
 */
@Composable
fun FintechPalePositiveCard(
  modifier: Modifier = Modifier,
  padding: Dp = 20.dp,
  content: @Composable ColumnScope.() -> Unit,
) {
  val tokens = LocalSyndicatTokens.current
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(tokens.cardRadiusDp.dp),
    color = tokens.surfaceSoft,
    border = BorderStroke(1.dp, tokens.divider),
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Column(
      modifier = Modifier.padding(padding),
      content = content,
    )
  }
}

/**
 * Meta Showcase / Promo Strip (`{rounded.xxxl}` = 32dp):
 * Renders on `{colors.ink-deep}` (`#0A1317`) with subtle Cobalt/Oculus atmospheric glow in the corner
 * so product/financial hero banners achieve Meta's signature hardware showcase presence.
 */
@Composable
fun AirtableSignatureCard(
  backgroundColor: Color,
  modifier: Modifier = Modifier,
  padding: Dp = 26.dp,
  accentGlow: Color = MetaPrimaryCobalt,
  content: @Composable ColumnScope.() -> Unit,
) {
  val tokens = LocalSyndicatTokens.current
  val shape = RoundedCornerShape(tokens.featureCardRadiusDp.dp)
  val baseColor = if (backgroundColor == MetaCritical) MetaInkDeep else backgroundColor
  val glowTint = if (backgroundColor == MetaCritical) MetaCritical else accentGlow

  Surface(
    modifier = modifier,
    shape = shape,
    color = baseColor,
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Column(
      modifier = Modifier
        .drawBehind {
          drawCircle(
            brush = Brush.radialGradient(
              colors = listOf(
                glowTint.copy(alpha = 0.22f),
                MetaOculusPurple.copy(alpha = 0.08f),
                Color.Transparent,
              ),
              center = Offset(size.width * 0.88f, size.height * 0.15f),
              radius = size.maxDimension * 0.72f,
            ),
            radius = size.maxDimension * 0.72f,
            center = Offset(size.width * 0.88f, size.height * 0.15f),
          )
        }
        .padding(padding),
      content = content,
    )
  }
}

/**
 * Meta `warranty-card` (`{colors.surface-soft}` `#F1F4F7`, `{rounded.xxl}` = 24dp).
 */
@Composable
fun AirtableSoftCard(
  modifier: Modifier = Modifier,
  padding: Dp = 24.dp,
  content: @Composable ColumnScope.() -> Unit,
) {
  val tokens = LocalSyndicatTokens.current
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(tokens.cardRadiusDp.dp),
    color = tokens.surfaceSoft,
    border = if (tokens.isDark) BorderStroke(1.dp, tokens.divider) else null,
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Column(
      modifier = Modifier.padding(padding),
      content = content,
    )
  }
}

/**
 * Meta KPI Tile (`{colors.surface-soft}` `#F1F4F7` with `{rounded.xxl}` = 24dp):
 * Replaces legacy pastel fills with Meta's clean Soft Cloud tile + crisp Deep Ink tabular display figure
 * and subtle semantic accent dot.
 */
@Composable
fun AirtablePastelKpiCard(
  backgroundColor: Color,
  label: String,
  valueText: String,
  modifier: Modifier = Modifier,
  subtitle: String? = null,
) {
  val tokens = LocalSyndicatTokens.current
  val indicatorDot = when (backgroundColor) {
    MetaWarning -> MetaWarning
    MetaSuccess -> MetaSuccess
    MetaCritical -> MetaCritical
    MetaPrimaryCobalt -> MetaPrimaryCobalt
    else -> MetaPrimaryCobalt
  }
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(tokens.cardRadiusDp.dp),
    color = tokens.surfaceSoft,
    border = if (tokens.isDark) BorderStroke(1.dp, tokens.divider) else null,
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Column(
      modifier = Modifier.padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(indicatorDot),
        )
        Text(
          text = label,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = tokens.body,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      Text(
        text = valueText,
        style = MaterialTheme.typography.displayMedium.copy(
          fontFamily = tokens.numericFontFamily,
          fontFeatureSettings = "tnum, ss01, ss02",
        ),
        fontWeight = FontWeight.Medium,
        color = tokens.inkDeep,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      if (!subtitle.isNullOrBlank()) {
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = tokens.mute,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

/**
 * Meta Soft Cobalt / Informational Callout (`{colors.primary-soft}` at 12% alpha, `{rounded.xxl}` = 24dp).
 */
@Composable
fun AirtableCreamCalloutCard(
  modifier: Modifier = Modifier,
  padding: Dp = 20.dp,
  content: @Composable ColumnScope.() -> Unit,
) {
  val tokens = LocalSyndicatTokens.current
  val bg = if (tokens.isDark) tokens.surfaceSoft else MetaCobaltTintLight
  val borderCol = if (tokens.isDark) tokens.divider else MetaPrimarySoft.copy(alpha = 0.25f)
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(tokens.cardRadiusDp.dp),
    color = bg,
    border = BorderStroke(1.dp, borderCol),
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Column(
      modifier = Modifier.padding(padding),
      content = content,
    )
  }
}

/**
 * Meta `promo-banner`:
 * High-contrast promotional / time-bound announcement strip (`{colors.ink-deep}` `#0A1317` or `{colors.warning}` `#F7B928`),
 * `{typography.body-sm-bold}` copy + optional inline pill CTA.
 */
@Composable
fun MetaPromoBanner(
  text: String,
  modifier: Modifier = Modifier,
  isYellowAlert: Boolean = false,
  actionLabel: String? = null,
  onActionClick: (() -> Unit)? = null,
) {
  val bg = if (isYellowAlert) MetaWarning else MetaInkDeep
  val fg = if (isYellowAlert) MetaInkDeep else MetaCanvas
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    color = bg,
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = fg,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(1f),
      )
      if (!actionLabel.isNullOrBlank() && onActionClick != null) {
        Spacer(modifier = Modifier.width(12.dp))
        Surface(
          shape = RoundedCornerShape(100.dp),
          color = if (isYellowAlert) MetaInkDeep else MetaCanvas,
          modifier = Modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(100.dp))
            .clickable(onClick = onActionClick),
        ) {
          Text(
            text = actionLabel,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (isYellowAlert) MetaCanvas else MetaInkDeep,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
          )
        }
      }
    }
  }
}

/**
 * Meta `button-primary` (Overview / Marketing Primary Pill CTA):
 * Background `{colors.ink-button}` (`#000000` light / `#FFFFFF` dark),
 * pressed `{colors.charcoal}` (`#444950`), disabled `{colors.disabled-text}` (`#BCC0C4`),
 * pill shape `{rounded.full}` (100dp), `{typography.button-md}` (14sp / 700).
 */
@Composable
fun LimePrimaryButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
  enabled: Boolean = true,
  compact: Boolean = false,
) {
  val tokens = LocalSyndicatTokens.current
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val activeContainer = if (isPressed) tokens.primaryActive else tokens.primary

  Button(
    onClick = onClick,
    enabled = enabled,
    interactionSource = interactionSource,
    shape = RoundedCornerShape(100.dp),
    colors = ButtonDefaults.buttonColors(
      containerColor = activeContainer,
      contentColor = tokens.onPrimary,
      disabledContainerColor = tokens.disabledBg,
      disabledContentColor = MetaCanvas,
    ),
    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
    contentPadding = if (compact) {
      PaddingValues(horizontal = 22.dp, vertical = 12.dp)
    } else {
      PaddingValues(horizontal = 30.dp, vertical = 14.dp)
    },
    modifier = modifier
      .minimumInteractiveComponentSize()
      .heightIn(min = 48.dp),
  ) {
    if (icon != null) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(18.dp),
      )
      Spacer(modifier = Modifier.width(8.dp))
    }
    Text(
      text = text,
      style = MaterialTheme.typography.labelLarge,
      fontWeight = FontWeight.SemiBold,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

/**
 * Meta `button-buy-cta` (Saturated Cobalt `#0064E0` Pill CTA):
 * Strictly reserved for in-flow configurator, payment recording, WhatsApp dispatch, and PDF checkout panels!
 * Pressed `{colors.primary-deep}` (`#0457CB`), pill shape `{rounded.full}` (100dp), `{typography.button-md}` (14sp / 700).
 */
@Composable
fun MetaCobaltBuyButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
  enabled: Boolean = true,
  compact: Boolean = false,
) {
  val tokens = LocalSyndicatTokens.current
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val activeContainer = if (isPressed) tokens.buyCtaPressed else tokens.buyCta

  Button(
    onClick = onClick,
    enabled = enabled,
    interactionSource = interactionSource,
    shape = RoundedCornerShape(100.dp),
    colors = ButtonDefaults.buttonColors(
      containerColor = activeContainer,
      contentColor = tokens.onBuyCta,
      disabledContainerColor = tokens.disabledBg,
      disabledContentColor = MetaCanvas,
    ),
    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
    contentPadding = if (compact) {
      PaddingValues(horizontal = 22.dp, vertical = 12.dp)
    } else {
      PaddingValues(horizontal = 30.dp, vertical = 14.dp)
    },
    modifier = modifier
      .minimumInteractiveComponentSize()
      .heightIn(min = 48.dp),
  ) {
    if (icon != null) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(18.dp),
      )
      Spacer(modifier = Modifier.width(8.dp))
    }
    Text(
      text = text,
      style = MaterialTheme.typography.labelLarge,
      fontWeight = FontWeight.SemiBold,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

/**
 * Coinbase secondary & ghost pill button:
 * Pill shape `{rounded.pill}` (100dp), `{colors.surface-strong}` (`#EEF0F3` light / `#262A31` dark) fill,
 * `{colors.ink}` text, `{typography.button}` (16sp / 600).
 */
@Composable
fun SageSecondaryButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
  enabled: Boolean = true,
  compact: Boolean = false,
  onSignatureCard: Boolean = false,
) {
  val tokens = LocalSyndicatTokens.current
  val bg = when {
    onSignatureCard -> Color.White.copy(alpha = 0.14f)
    compact -> tokens.surfaceStrong
    else -> tokens.surfaceStrong
  }
  val fg = if (onSignatureCard) MetaCanvas else tokens.inkDeep
  val borderStroke = if (onSignatureCard) {
    BorderStroke(1.dp, Color.White.copy(alpha = 0.45f))
  } else {
    BorderStroke(1.dp, tokens.divider)
  }
  OutlinedButton(
    onClick = onClick,
    enabled = enabled,
    shape = RoundedCornerShape(100.dp),
    border = borderStroke,
    colors = ButtonDefaults.outlinedButtonColors(
      containerColor = bg,
      contentColor = fg,
      disabledContainerColor = tokens.surfaceSoft,
      disabledContentColor = fg.copy(alpha = 0.45f),
    ),
    contentPadding = if (compact) {
      PaddingValues(horizontal = 20.dp, vertical = 10.dp)
    } else {
      PaddingValues(horizontal = 28.dp, vertical = 12.dp)
    },
    modifier = modifier
      .minimumInteractiveComponentSize()
      .heightIn(min = 48.dp),
  ) {
    if (icon != null) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(18.dp),
      )
      Spacer(modifier = Modifier.width(8.dp))
    }
    Text(
      text = text,
      style = MaterialTheme.typography.labelLarge,
      fontWeight = FontWeight.SemiBold,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Composable
fun InkTertiaryButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
  enabled: Boolean = true,
) {
  SageSecondaryButton(
    text = text,
    onClick = onClick,
    modifier = modifier,
    icon = icon,
    enabled = enabled,
    compact = true,
  )
}

/**
 * Meta `button-icon-circular`:
 * 44dp circular button inside a 48dp accessible hit target, `{colors.canvas}` fill, `{colors.ink}` icon.
 */
@Composable
fun RoundIconCircleButton(
  icon: ImageVector,
  contentDescription: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  selected: Boolean = false,
) {
  val tokens = LocalSyndicatTokens.current
  Box(
    modifier = modifier
      .minimumInteractiveComponentSize()
      .size(48.dp)
      .clip(CircleShape)
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center,
  ) {
    Surface(
      shape = CircleShape,
      color = if (selected) tokens.inkDeep else tokens.cardBg,
      border = if (selected) null else BorderStroke(1.dp, tokens.divider),
      tonalElevation = 0.dp,
      shadowElevation = 0.dp,
      modifier = Modifier.size(42.dp),
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
          imageVector = icon,
          contentDescription = contentDescription,
          tint = if (selected) tokens.canvas else tokens.ink,
          modifier = Modifier.size(20.dp),
        )
      }
    }
  }
}

/**
 * Meta `button-pill-tab` + `button-pill-tab-active`:
 * - Inactive: background `{colors.canvas}`, text `{colors.ink}`, border `1px solid {colors.hairline}`,
 *   `{typography.body-sm-bold}` (14sp / 700), `{rounded.full}` (100dp).
 * - Active: background `{colors.ink-deep}` (`#0A1317` light / `#FFFFFF` dark), text `{colors.canvas}`,
 *   NO border in active state ("the dark fill replaces it").
 */
@Composable
fun SyndicatFilterChip(
  selected: Boolean,
  onClick: () -> Unit,
  label: String,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  leadingIcon: (@Composable (() -> Unit))? = null,
) {
  val tokens = LocalSyndicatTokens.current
  val pillShape = RoundedCornerShape(100.dp)
  val bg = when {
    !enabled -> tokens.surfaceSoft
    selected -> tokens.inkDeep
    else -> tokens.cardBg
  }
  val fg = when {
    !enabled -> tokens.mute.copy(alpha = 0.55f)
    selected -> if (tokens.isDark) MetaInkDeep else MetaCanvas
    else -> tokens.ink
  }
  val border = if (selected) {
    null
  } else {
    BorderStroke(1.dp, if (enabled) tokens.hairline else tokens.divider)
  }

  Surface(
    shape = pillShape,
    color = bg,
    border = border,
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
    modifier = modifier
      .minimumInteractiveComponentSize()
      .heightIn(min = 48.dp)
      .clip(pillShape)
      .clickable(enabled = enabled, onClick = onClick),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
      if (leadingIcon != null) {
        CompositionLocalProvider(LocalContentColor provides fg) {
          leadingIcon()
        }
        Spacer(modifier = Modifier.width(6.dp))
      }
      Text(
        text = label,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = fg,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

/**
 * Meta Segmented Pill Switcher (pair of `button-pill-tab` inside a `{colors.surface-soft}` pill container).
 */
@Composable
fun MetaSegmentedPillRow(
  firstLabel: String,
  secondLabel: String,
  isFirstSelected: Boolean,
  onSelectFirst: () -> Unit,
  onSelectSecond: () -> Unit,
  firstTag: String,
  secondTag: String,
  modifier: Modifier = Modifier,
  firstIcon: ImageVector? = null,
  secondIcon: ImageVector? = null,
) {
  val tokens = LocalSyndicatTokens.current
  val outerShape = RoundedCornerShape(100.dp)
  val innerShape = RoundedCornerShape(100.dp)

  Surface(
    shape = outerShape,
    color = tokens.surfaceSoft,
    border = BorderStroke(1.dp, tokens.divider),
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
    modifier = modifier,
  ) {
    Row(
      modifier = Modifier.padding(4.dp),
      horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      listOf(
        Triple(firstLabel, isFirstSelected, onSelectFirst) to Pair(firstTag, firstIcon),
        Triple(secondLabel, !isFirstSelected, onSelectSecond) to Pair(secondTag, secondIcon),
      ).forEach { (stateTriple, metaPair) ->
        val (lbl, active, onTap) = stateTriple
        val (tag, ic) = metaPair
        val activeBg = tokens.inkDeep
        val activeFg = if (tokens.isDark) MetaInkDeep else MetaCanvas
        Surface(
          shape = innerShape,
          color = if (active) activeBg else Color.Transparent,
          tonalElevation = 0.dp,
          shadowElevation = 0.dp,
          modifier = Modifier
            .minimumInteractiveComponentSize()
            .heightIn(min = 44.dp)
            .clip(innerShape)
            .clickable(onClick = onTap)
            .testTag(tag),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
          ) {
            if (ic != null) {
              Icon(
                imageVector = ic,
                contentDescription = null,
                tint = if (active) activeFg else tokens.mute,
                modifier = Modifier.size(16.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
              text = lbl,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = if (active) activeFg else tokens.ink,
            )
          }
        }
      }
    }
  }
}

/**
 * Meta Status Badges (`badge-success`, `badge-attention`, `badge-critical`):
 * Solid vibrant pill `{rounded.full}` (100dp), `{typography.caption-bold}` (12sp / 700), padding `4px 10px`.
 */
@Composable
fun StatusPillBadge(
  status: PaymentBadgeStatus,
  modifier: Modifier = Modifier,
  customLabel: String? = null,
) {
  val tokens = LocalSyndicatTokens.current
  val strings = LocalStrings.current
  val rawText = customLabel ?: strings.badgeText(status)

  val (iconGlyph, glyphColor, fg) = when (status) {
    PaymentBadgeStatus.PAID -> Triple("✓", tokens.statusPaidText, tokens.statusPaidText)
    PaymentBadgeStatus.PARTIAL -> Triple("●", tokens.statusPartialDot, tokens.warningContent)
    PaymentBadgeStatus.LATE -> Triple("!", tokens.statusLateText, tokens.statusLateText)
    PaymentBadgeStatus.EMPTY -> Triple("—", tokens.mute, tokens.mute)
    PaymentBadgeStatus.EXEMPT -> Triple("—", tokens.mute, tokens.mute)
  }
  Surface(
    shape = RoundedCornerShape(100.dp),
    color = tokens.badgeBg,
    border = BorderStroke(1.dp, tokens.divider),
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
    modifier = modifier,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp),
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
      Text(
        text = iconGlyph,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = glyphColor,
      )
      Text(
        text = rawText,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = fg,
      )
    }
  }
}

/**
 * Meta Soft-Cobalt Reminder Pill (`{rounded.full}` 100dp, `{typography.caption-bold}`).
 */
@Composable
fun RemindedPillBadge(
  label: String,
  modifier: Modifier = Modifier,
) {
  val tokens = LocalSyndicatTokens.current
  val bg = if (tokens.isDark) tokens.surfaceSoft else MetaCobaltTintLight
  Surface(
    shape = RoundedCornerShape(100.dp),
    color = bg,
    border = BorderStroke(1.dp, tokens.infoBorder.copy(alpha = 0.45f)),
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
    modifier = modifier,
  ) {
    Text(
      text = "✉ $label",
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.Bold,
      color = tokens.infoColor,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
    )
  }
}

/**
 * Meta Progress Bar (pill-rounded 6dp track on `{colors.hairline-soft}`, Cobalt `#0064E0` active fill).
 */
@Composable
fun SyndicatProgressBar(
  progress: Float,
  modifier: Modifier = Modifier,
  indicatorColor: Color = Color.Unspecified,
) {
  val tokens = LocalSyndicatTokens.current
  val clamped = progress.coerceIn(0f, 1f)
  val activeFill = if (indicatorColor != Color.Unspecified) indicatorColor else tokens.buyCta
  LinearProgressIndicator(
    progress = { clamped },
    modifier = modifier
      .height(6.dp)
      .clip(RoundedCornerShape(100.dp)),
    color = activeFill,
    trackColor = tokens.trackBg,
    gapSize = 0.dp,
    drawStopIndicator = {},
  )
}

/**
 * Meta Configurator / Modal Dialog (`{rounded.xxl}` = 24dp, stark white canvas, 1px hairline-soft border).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SyndicatDialog(
  onDismissRequest: () -> Unit,
  title: @Composable () -> Unit,
  text: @Composable () -> Unit,
  confirmButton: @Composable () -> Unit,
  dismissButton: (@Composable () -> Unit)? = null,
) {
  val tokens = LocalSyndicatTokens.current
  BasicAlertDialog(
    onDismissRequest = onDismissRequest,
    properties = DialogProperties(
      usePlatformDefaultWidth = false,
      decorFitsSystemWindows = true,
    ),
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 20.dp)
        .imePadding(),
      contentAlignment = Alignment.Center,
    ) {
      Surface(
        shape = RoundedCornerShape(tokens.cardRadiusDp.dp),
        color = tokens.cardBg,
        border = BorderStroke(1.dp, tokens.cardBorder),
        tonalElevation = 0.dp,
        shadowElevation = 4.dp,
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 520.dp),
      ) {
        Column(
          modifier = Modifier.padding(24.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          title()
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f, fill = false),
          ) {
            text()
          }
          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            if (dismissButton != null) {
              dismissButton()
            }
            confirmButton()
          }
        }
      }
    }
  }
}

/**
 * Meta `search-pill`:
 * Background `{colors.surface-soft}` (`#F1F4F7`), text `{colors.ink}`, placeholder `{colors.steel}` (`#5D6C7B`),
 * `{rounded.full}` (100dp), focused `2px solid {colors.fb-blue}` (`#1876F2`), accessible 48dp min height.
 */
@Composable
fun MetaSearchPill(
  value: String,
  onValueChange: (String) -> Unit,
  placeholder: String,
  clearDescription: String,
  modifier: Modifier = Modifier,
) {
  val tokens = LocalSyndicatTokens.current
  val interactionSource = remember { MutableInteractionSource() }
  val isFocused by interactionSource.collectIsFocusedAsState()
  val pillShape = RoundedCornerShape(100.dp)

  BasicTextField(
    value = value,
    onValueChange = onValueChange,
    singleLine = true,
    textStyle = MaterialTheme.typography.bodySmall.copy(
      color = tokens.inkDeep,
      fontWeight = FontWeight.Normal,
    ),
    cursorBrush = SolidColor(tokens.fieldFocusBorder),
    interactionSource = interactionSource,
    modifier = modifier,
    decorationBox = { innerTextField ->
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(min = 48.dp)
          .clip(pillShape)
          .background(if (isFocused) tokens.cardBg else tokens.surfaceSoft, pillShape)
          .border(
            width = if (isFocused) 2.dp else 1.dp,
            color = if (isFocused) tokens.fieldFocusBorder else tokens.divider,
            shape = pillShape,
          )
          .padding(start = 18.dp, end = if (value.isNotEmpty()) 6.dp else 18.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Icon(
          imageVector = Icons.Default.Search,
          contentDescription = null,
          tint = if (isFocused) tokens.fieldFocusBorder else tokens.mute,
          modifier = Modifier.size(20.dp),
        )
        Box(
          modifier = Modifier.weight(1f),
          contentAlignment = Alignment.CenterStart,
        ) {
          if (value.isEmpty()) {
            Text(
              text = placeholder,
              style = MaterialTheme.typography.bodySmall,
              color = tokens.mute,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
          innerTextField()
        }
        if (value.isNotEmpty()) {
          IconButton(
            onClick = { onValueChange("") },
            modifier = Modifier.minimumInteractiveComponentSize(),
          ) {
            Icon(
              imageVector = Icons.Default.Clear,
              contentDescription = clearDescription,
              tint = tokens.mute,
              modifier = Modifier.size(18.dp),
            )
          }
        }
      }
    },
  )
}

/**
 * Meta `text-input` / `text-input-focused` / `text-input-error`:
 * - Label cleanly stacked above the field (zero overlap with border or input text)
 * - Background `{colors.canvas}` (`#FFFFFF`), text `{colors.ink}` (`#1C1E21`), `{rounded.lg}` (8dp)
 * - Default border: `1px solid {colors.hairline}` (`#CED0D4`)
 * - Focused border: `2px solid {colors.fb-blue}` (`#1876F2`)
 * - Error border: `1px solid {colors.critical-strong}` (`#F0284A`) with `#F0284A` error label below
 */
@Composable
fun FintechTextField(
  value: String,
  onValueChange: (String) -> Unit,
  label: String,
  modifier: Modifier = Modifier,
  placeholder: String? = null,
  singleLine: Boolean = true,
  minLines: Int = if (singleLine) 1 else 3,
  maxLines: Int = if (singleLine) 1 else 5,
  keyboardType: KeyboardType = KeyboardType.Text,
  leadingIcon: (@Composable (() -> Unit))? = null,
  trailingIcon: (@Composable (() -> Unit))? = null,
  errorText: String? = null,
  labelColor: Color = Color.Unspecified,
) {
  val tokens = LocalSyndicatTokens.current
  val isError = !errorText.isNullOrBlank()
  val isNumeric = keyboardType == KeyboardType.Number ||
    keyboardType == KeyboardType.NumberPassword ||
    keyboardType == KeyboardType.Decimal ||
    keyboardType == KeyboardType.Phone
  val visualTrans = if (keyboardType == KeyboardType.NumberPassword || keyboardType == KeyboardType.Password) {
    PasswordVisualTransformation()
  } else {
    VisualTransformation.None
  }

  val interactionSource = remember { MutableInteractionSource() }
  val isFocused by interactionSource.collectIsFocusedAsState()
  val fieldShape = RoundedCornerShape(tokens.fieldRadiusDp.dp) // {rounded.lg} = 8dp
  val errColor = tokens.negativeDeep // {colors.critical-strong} #F0284A

  val borderColor = when {
    isError -> errColor
    isFocused -> tokens.fieldFocusBorder // {colors.fb-blue} #1876F2
    else -> tokens.fieldBorder // {colors.hairline} #CED0D4
  }
  val borderWidth = when {
    isFocused -> 2.dp
    else -> 1.dp
  }
  val resolvedLabelColor = when {
    isError -> errColor
    isFocused && labelColor == Color.Unspecified -> tokens.fieldFocusBorder
    labelColor != Color.Unspecified -> labelColor
    else -> tokens.ink
  }

  val baseStyle = MaterialTheme.typography.bodyLarge
  val textStyle = baseStyle.copy(
    color = tokens.inkDeep,
    fontWeight = FontWeight.Normal,
    fontFamily = if (isNumeric) tokens.numericFontFamily else baseStyle.fontFamily,
    fontFeatureSettings = if (isNumeric) "tnum" else baseStyle.fontFeatureSettings,
  )

  val safeMinLines = if (singleLine) 1 else minLines.coerceIn(1, maxLines.coerceAtLeast(1))
  val safeMaxLines = if (singleLine) 1 else maxLines.coerceAtLeast(safeMinLines)

  BasicTextField(
    value = value,
    onValueChange = onValueChange,
    singleLine = singleLine,
    minLines = safeMinLines,
    maxLines = safeMaxLines,
    textStyle = textStyle,
    cursorBrush = SolidColor(if (isError) errColor else tokens.fieldFocusBorder),
    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
    visualTransformation = visualTrans,
    interactionSource = interactionSource,
    modifier = modifier,
    decorationBox = { innerTextField ->
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        if (label.isNotBlank()) {
          Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = resolvedLabelColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
        }

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (singleLine) 48.dp else 88.dp)
            .clip(fieldShape)
            .background(tokens.fieldBg, fieldShape)
            .border(
              width = borderWidth,
              color = borderColor,
              shape = fieldShape,
            )
            .padding(
              start = if (leadingIcon != null) 12.dp else 14.dp,
              end = if (trailingIcon != null) 8.dp else 14.dp,
              top = if (trailingIcon != null && singleLine) 4.dp else 12.dp,
              bottom = if (trailingIcon != null && singleLine) 4.dp else 12.dp,
            ),
          verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          if (leadingIcon != null) {
            CompositionLocalProvider(
              LocalContentColor provides if (isFocused) tokens.fieldFocusBorder else tokens.mute
            ) {
              leadingIcon()
            }
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .padding(vertical = if (trailingIcon != null && singleLine) 8.dp else 0.dp),
            contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart,
          ) {
            if (value.isEmpty() && !placeholder.isNullOrBlank()) {
              Text(
                text = placeholder,
                style = MaterialTheme.typography.bodySmall,
                color = tokens.stone,
                maxLines = if (singleLine) 1 else safeMaxLines,
                overflow = TextOverflow.Ellipsis,
              )
            }
            innerTextField()
          }

          if (trailingIcon != null) {
            CompositionLocalProvider(
              LocalContentColor provides if (isFocused) tokens.fieldFocusBorder else tokens.mute
            ) {
              trailingIcon()
            }
          }
        }

        if (isError) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 2.dp),
          ) {
            Icon(
              imageVector = Icons.Default.ErrorOutline,
              contentDescription = null,
              tint = errColor,
              modifier = Modifier.size(15.dp),
            )
            Text(
              text = errorText ?: "",
              style = MaterialTheme.typography.bodySmall,
              color = errColor,
            )
          }
        }
      }
    },
  )
}

@Composable
fun PinLockGateScreen(
  expectedPin: String,
  onUnlocked: () -> Unit,
) {
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  var entered by remember { mutableStateOf("") }
  var hasError by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(tokens.screenBg)
      .padding(24.dp),
    contentAlignment = Alignment.Center,
  ) {
    FintechWhiteCard(
      modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp),
      padding = 28.dp,
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Surface(
          shape = CircleShape,
          color = tokens.surfaceSoft,
          modifier = Modifier.size(64.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = null,
              tint = tokens.inkDeep,
              modifier = Modifier.size(26.dp),
            )
          }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = strings.pinUnlockTitle,
          style = MaterialTheme.typography.headlineLarge,
          fontWeight = FontWeight.Medium,
          color = tokens.inkDeep,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = strings.pinUnlockSub,
          style = MaterialTheme.typography.bodySmall,
          color = tokens.body,
        )
        Spacer(modifier = Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
          repeat(4) { i ->
            val filled = i < entered.length
            Box(
              modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(if (filled) tokens.buyCta else tokens.trackBg),
            )
          }
        }

        if (hasError) {
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "! ${strings.pinWrongError}",
            style = MaterialTheme.typography.labelMedium,
            color = tokens.negativeDeep,
          )
        }

        Spacer(modifier = Modifier.height(24.dp))

        val rows = listOf(
          listOf("1", "2", "3"),
          listOf("4", "5", "6"),
          listOf("7", "8", "9"),
          listOf("", "0", "⌫"),
        )
        rows.forEach { row ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
          ) {
            row.forEach { key ->
              if (key.isEmpty()) {
                Spacer(modifier = Modifier.size(64.dp))
              } else {
                Surface(
                  shape = CircleShape,
                  color = tokens.surfaceSoft,
                  tonalElevation = 0.dp,
                  shadowElevation = 0.dp,
                  modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .clickable {
                      hasError = false
                      if (key == "⌫") {
                        if (entered.isNotEmpty()) entered = entered.dropLast(1)
                      } else if (entered.length < 4) {
                        val next = entered + key
                        entered = next
                        if (next.length == 4) {
                          if (next == expectedPin) {
                            onUnlocked()
                          } else {
                            hasError = true
                            entered = ""
                          }
                        }
                      }
                    }
                    .testTag("pin_key_$key"),
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    if (key == "⌫") {
                      Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Backspace",
                        tint = tokens.inkDeep,
                      )
                    } else {
                      Text(
                        text = key,
                        style = MaterialTheme.typography.headlineSmall,
                        fontFamily = tokens.numericFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = tokens.inkDeep,
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun PinSetupDialog(
  onDismiss: () -> Unit,
  onSavePin: (String) -> Unit,
) {
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  var pinText by remember { mutableStateOf("") }

  SyndicatDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = strings.settingsPinTitle,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Medium,
        color = tokens.inkDeep,
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        FintechTextField(
          value = pinText,
          onValueChange = { next ->
            pinText = next.filter { it.isDigit() }.take(4)
          },
          label = "PIN (4 chiffres / 4 أرقام)",
          placeholder = "1234",
          keyboardType = KeyboardType.NumberPassword,
          modifier = Modifier.fillMaxWidth().testTag("pin_setup_input"),
        )
      }
    },
    confirmButton = {
      MetaCobaltBuyButton(
        text = strings.apptSave,
        enabled = pinText.length == 4,
        onClick = { onSavePin(pinText) },
        compact = true,
      )
    },
    dismissButton = {
      SageSecondaryButton(
        text = strings.apptCancel,
        onClick = onDismiss,
        compact = true,
      )
    },
  )
}
