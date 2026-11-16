package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ApartmentRecord
import com.example.data.model.ExpenseRecord
import com.example.data.model.ProjectRecord
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalStrings
import com.example.ui.i18n.bidiLtr
import com.example.ui.i18n.formatNumber
import com.example.ui.i18n.localizeDigits
import com.example.ui.theme.CoinbaseOnDarkSoft
import com.example.ui.theme.CoinbasePrimary
import com.example.ui.theme.CoinbaseSemanticUp
import com.example.ui.theme.CoinbaseSurfaceDark
import com.example.ui.theme.CoinbaseSurfaceDarkElevated
import com.example.ui.theme.LocalSyndicatTokens
import com.example.ui.theme.MetaAttention
import com.example.ui.theme.MetaOculusPurple
import com.example.ui.theme.MetaPrimaryCobalt
import com.example.ui.theme.MetaSuccess
import com.example.ui.theme.MetaWarning
import com.example.ui.theme.cotisationColorForAmount
import com.example.ui.viewmodel.SyndicatDashboardData
import com.example.ui.viewmodel.formatDh

/**
 * Carte Portefeuille de Trésorerie (Coinbase Dark Editorial Showcase) :
 * Affiche la trésorerie disponible en grand format tabulaire ainsi qu'une barre de ventilation
 * en 3 colonnes (Encaissé, Réglé, Solde net) sans surcharge visuelle.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TreasuryPortfolioHeroCard(
  data: SyndicatDashboardData,
  numericFont: FontFamily,
  onUnpaidExpensesClick: (() -> Unit)? = null,
) {
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val isPrivacy = LocalPrivacyMode.current
  val cfg = data.config
  val cur = cfg.currency
  val dig = cfg.digitsAr

  val animCashNow = rememberAnimatedLong(data.cashNowDh)
  val animCollected = rememberAnimatedLong(data.totalCaisseDh)
  val animPaidOut = rememberAnimatedLong(data.totalExpensesPaidDh)
  val animBalance = rememberAnimatedLong(data.balanceDh)

  val rawCashDisplay = if (animCashNow < 0L) {
    "▼ ${formatDh(animCashNow, cur, dig)}"
  } else {
    formatDh(animCashNow, cur, dig)
  }
  val cashDisplay = maskSensitiveDh(rawCashDisplay, isPrivacy)

  Surface(
    shape = RoundedCornerShape(tokens.featureCardRadiusDp.dp),
    color = CoinbaseSurfaceDark,
    border = BorderStroke(1.dp, if (tokens.isDark) tokens.cardBorder else CoinbaseSurfaceDarkElevated),
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
    modifier = Modifier
      .fillMaxWidth()
      .testTag("hero_cash_card"),
  ) {
    Column(
      modifier = Modifier
        .drawBehind {
          drawCircle(
            brush = Brush.radialGradient(
              colors = listOf(
                CoinbasePrimary.copy(alpha = 0.28f),
                CoinbasePrimary.copy(alpha = 0.06f),
                Color.Transparent,
              ),
              center = Offset(size.width * 0.88f, size.height * 0.12f),
              radius = size.maxDimension * 0.78f,
            ),
            radius = size.maxDimension * 0.78f,
            center = Offset(size.width * 0.88f, size.height * 0.12f),
          )
        }
        .padding(22.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .background(CoinbaseSemanticUp, CircleShape),
          )
          Text(
            text = strings.dashCashEyebrow,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = CoinbaseOnDarkSoft,
          )
        }

        Surface(
          shape = RoundedCornerShape(100.dp),
          color = CoinbaseSurfaceDarkElevated,
          border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        ) {
          Text(
            text = strings.dashLiveTreasuryBadge,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.88f),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          )
        }
      }

      Text(
        text = cashDisplay,
        style = MaterialTheme.typography.displayLarge.copy(
          fontFamily = numericFont,
          fontFeatureSettings = "tnum",
        ),
        fontWeight = FontWeight.Medium,
        color = Color.White,
        modifier = Modifier.testTag("cash_now_value"),
      )

      Surface(
        shape = RoundedCornerShape(16.dp),
        color = CoinbaseSurfaceDarkElevated,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          TreasuryMiniStatColumn(
            label = strings.dashCollectedLabel,
            value = maskSensitiveDh(formatDh(animCollected, cur, dig, explicitPlus = true), isPrivacy),
            valueColor = CoinbaseSemanticUp,
            numericFont = numericFont,
            modifier = Modifier.weight(1f),
          )
          Box(
            modifier = Modifier
              .width(1.dp)
              .height(28.dp)
              .background(Color.White.copy(alpha = 0.10f)),
          )
          TreasuryMiniStatColumn(
            label = strings.dashPaidOutLabel,
            value = maskSensitiveDh(formatDh(animPaidOut, cur, dig), isPrivacy),
            valueColor = Color.White,
            numericFont = numericFont,
            alignCenter = true,
            modifier = Modifier.weight(1f),
          )
          Box(
            modifier = Modifier
              .width(1.dp)
              .height(28.dp)
              .background(Color.White.copy(alpha = 0.10f)),
          )
          TreasuryMiniStatColumn(
            label = strings.dashNetBalanceLabel,
            value = maskSensitiveDh(formatDh(animBalance, cur, dig), isPrivacy),
            valueColor = if (animBalance >= 0L) Color.White else Color(0xFFFF6B78),
            numericFont = numericFont,
            alignEnd = true,
            modifier = Modifier.weight(1f),
          )
        }
      }

      if (cfg.openingBalanceDh != 0L || data.unpaidExpensesDh > 0L) {
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          if (cfg.openingBalanceDh != 0L) {
            Surface(
              shape = RoundedCornerShape(100.dp),
              color = Color.White.copy(alpha = 0.10f),
            ) {
              Text(
                text = strings.dashOpeningBalanceNote(maskSensitiveDh(formatDh(cfg.openingBalanceDh, cur, dig), isPrivacy)),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.90f),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
              )
            }
          }
          if (data.unpaidExpensesDh > 0L) {
            val pillShape = RoundedCornerShape(100.dp)
            Surface(
              shape = pillShape,
              color = Color(0xFF3B1219),
              border = BorderStroke(1.dp, Color(0xFFFF6B78).copy(alpha = 0.35f)),
              modifier = if (onUnpaidExpensesClick != null) {
                Modifier
                  .clip(pillShape)
                  .clickable(onClick = onUnpaidExpensesClick)
                  .testTag("hero_unpaid_expenses_pill")
              } else {
                Modifier
              },
            ) {
              Text(
                text = "◐ ${strings.expSupplierRemaining(maskSensitiveDh(formatDh(data.unpaidExpensesDh, cur, dig), isPrivacy))}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFFF8A94),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun TreasuryMiniStatColumn(
  label: String,
  value: String,
  valueColor: Color,
  numericFont: FontFamily,
  alignCenter: Boolean = false,
  alignEnd: Boolean = false,
  modifier: Modifier = Modifier,
) {
  val horizAlign = when {
    alignEnd -> Alignment.End
    alignCenter -> Alignment.CenterHorizontally
    else -> Alignment.Start
  }
  Column(
    modifier = modifier.padding(horizontal = 4.dp),
    horizontalAlignment = horizAlign,
    verticalArrangement = Arrangement.spacedBy(2.dp),
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = CoinbaseOnDarkSoft,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
    Text(
      text = value,
      style = MaterialTheme.typography.labelLarge.copy(
        fontFamily = numericFont,
        fontFeatureSettings = "tnum",
      ),
      fontWeight = FontWeight.SemiBold,
      color = valueColor,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

/**
 * Bouton d'action rapide style Coinbase Portfolio (cercle icône 42dp + libellé centré, cible >= 48dp).
 */
@Composable
fun DashboardQuickActionButton(
  icon: ImageVector,
  label: String,
  isPrimary: Boolean,
  onClick: () -> Unit,
  badgeCount: Int = 0,
  digitsAr: Boolean = false,
  modifier: Modifier = Modifier,
) {
  val tokens = LocalSyndicatTokens.current
  val circleBg = if (isPrimary) tokens.buyCta else tokens.surfaceStrong
  val iconTint = if (isPrimary) tokens.onBuyCta else tokens.inkDeep

  Surface(
    shape = RoundedCornerShape(16.dp),
    color = tokens.cardBg,
    border = BorderStroke(1.dp, tokens.cardBorder),
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
    modifier = modifier
      .minimumInteractiveComponentSize()
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 6.dp, vertical = 12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Box(contentAlignment = Alignment.TopEnd) {
        Surface(
          shape = CircleShape,
          color = circleBg,
          modifier = Modifier.size(42.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = icon,
              contentDescription = label,
              tint = iconTint,
              modifier = Modifier.size(20.dp),
            )
          }
        }
        if (badgeCount > 0) {
          Surface(
            shape = CircleShape,
            color = tokens.negative,
            border = BorderStroke(1.5.dp, tokens.cardBg),
          ) {
            Text(
              text = formatNumber(badgeCount.toLong(), digitsAr),
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
            )
          }
        }
      }
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = tokens.inkDeep,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

/**
 * Carte KPI Coinbase épurée sur fond blanc/sombre bordé (1dp hairline), icône en pastille douce
 * et valeur tabulaire haute lisibilité.
 */
@Composable
fun CoinbaseKpiMetricCard(
  icon: ImageVector,
  iconTint: Color,
  iconContainer: Color,
  label: String,
  valueText: String,
  valueColor: Color,
  subtitle: String,
  numericFont: FontFamily,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val tokens = LocalSyndicatTokens.current
  val shape = RoundedCornerShape(20.dp)

  Surface(
    shape = shape,
    color = tokens.cardBg,
    border = BorderStroke(1.dp, tokens.cardBorder),
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
    modifier = modifier
      .heightIn(min = 132.dp)
      .clip(shape)
      .clickable(onClick = onClick),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Surface(
          shape = CircleShape,
          color = iconContainer,
          modifier = Modifier.size(34.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = icon,
              contentDescription = null,
              tint = iconTint,
              modifier = Modifier.size(18.dp),
            )
          }
        }
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForward,
          contentDescription = null,
          tint = tokens.stone,
          modifier = Modifier.size(16.dp),
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
          text = label,
          style = MaterialTheme.typography.labelMedium,
          color = tokens.body,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = valueText,
          style = MaterialTheme.typography.titleLarge.copy(
            fontFamily = numericFont,
            fontFeatureSettings = "tnum",
          ),
          fontWeight = FontWeight.SemiBold,
          color = valueColor,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.labelSmall,
          color = tokens.mute,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

/**
 * Carte Taux de recouvrement dotée d'une jauge circulaire Canvas sur mesure.
 */
@Composable
fun CollectionRateRingCard(
  ratePercent: Int,
  upToDateCount: Int,
  occupiedCount: Int,
  digitsAr: Boolean,
  numericFont: FontFamily,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val shape = RoundedCornerShape(20.dp)
  val clamped = ratePercent.coerceIn(0, 100)
  val animatedSweepFraction = androidx.compose.runtime.remember(clamped) { clamped / 100f }
  val animatedFraction by animateFloatAsState(
    targetValue = animatedSweepFraction,
    animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
    label = "ring_rate_anim",
  )
  val ringColor = when {
    clamped >= 80 -> tokens.positive
    clamped >= 50 -> tokens.linkColor
    else -> tokens.warning
  }
  val trackColor = tokens.divider

  Surface(
    shape = shape,
    color = tokens.cardBg,
    border = BorderStroke(tokens.borderStrokeDp.dp, tokens.cardBorder),
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
    modifier = modifier
      .heightIn(min = 132.dp)
      .clip(shape)
      .clickable(onClick = onClick),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier = Modifier.size(38.dp),
          contentAlignment = Alignment.Center,
        ) {
          Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = 4.dp.toPx()
            val inset = strokePx / 2f
            val arcSize = Size(size.width - strokePx, size.height - strokePx)
            drawArc(
              color = trackColor,
              startAngle = -90f,
              sweepAngle = 360f,
              useCenter = false,
              topLeft = Offset(inset, inset),
              size = arcSize,
              style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )
            drawArc(
              color = ringColor,
              startAngle = -90f,
              sweepAngle = 360f * animatedFraction,
              useCenter = false,
              topLeft = Offset(inset, inset),
              size = arcSize,
              style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )
          }
        }
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForward,
          contentDescription = null,
          tint = tokens.stone,
          modifier = Modifier.size(16.dp),
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
          text = strings.dashCollectionRateLabel,
          style = MaterialTheme.typography.labelMedium,
          color = tokens.body,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = bidiLtr("${localizeDigits(clamped.toString(), digitsAr)}%"),
          style = MaterialTheme.typography.titleLarge.copy(
            fontFamily = numericFont,
            fontFeatureSettings = "tnum",
          ),
          fontWeight = FontWeight.SemiBold,
          color = tokens.inkDeep,
          maxLines = 1,
        )
        Text(
          text = "✓ ${bidiLtr("${localizeDigits(upToDateCount.toString(), digitsAr)}/${localizeDigits(occupiedCount.toString(), digitsAr)}")} ${strings.badgePaid}",
          style = MaterialTheme.typography.labelSmall,
          color = tokens.positiveDeep,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

/**
 * Graphique à barres verticales (100 % largeur portrait, zéro slider horizontal).
 */
@Composable
fun MonthlyCollectionsChartCard(
  data: SyndicatDashboardData,
  isAr: Boolean,
  numericFont: FontFamily,
  onClick: () -> Unit,
) {
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val cfg = data.config
  val cur = cfg.currency
  val dig = cfg.digitsAr
  val activeMonths = cfg.activeMonthColumns

  val maxExpected = (data.occupiedCount.coerceAtLeast(1) *
    (activeMonths.maxOfOrNull { cfg.feeForMonth(it) } ?: 100L)).coerceAtLeast(1L)

  FintechWhiteCard(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("dash_monthly_chart_card"),
    padding = 20.dp,
    onClick = onClick,
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = strings.dashMonthlySection(cfg.year),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = tokens.inkDeep,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "● ${if (isAr) "عادي:" else "Standard :"} ${formatDh(cfg.defaultMonthlyFee, cur, dig)}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = cotisationColorForAmount(cfg.defaultMonthlyFee, cfg.defaultMonthlyFee),
          )
          Text(
            text = "● ${strings.dashCustomFeeLegend(cur)}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = cotisationColorForAmount(cfg.defaultMonthlyFee + 50L, cfg.defaultMonthlyFee),
          )
          if (data.hasPreviousYearChartData) {
            Text(
              text = "◐ ${strings.dashPreviousYearLegend((cfg.year - 1L).coerceAtLeast(2020L))}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Medium,
              color = tokens.mute,
            )
          }
        }
      }
    }

    if (activeMonths.isNotEmpty()) {
      Spacer(modifier = Modifier.height(18.dp))

      Surface(
        shape = RoundedCornerShape(14.dp),
        color = tokens.surfaceSoft,
        border = BorderStroke(tokens.borderStrokeDp.dp, tokens.divider),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 14.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.Bottom,
        ) {
          activeMonths.forEach { month ->
            val monthFee = cfg.feeForMonth(month)
            val collected = data.apartments.sumOf { it.amountFor(month) }
            val prevCollected = data.previousYearMonthlyTotals[month] ?: 0L
            val ratio = (collected.toFloat() / maxExpected.toFloat()).coerceIn(0f, 1f)
            val prevRatio = (prevCollected.toFloat() / maxExpected.toFloat()).coerceIn(0f, 1f)
            val barColor = if (collected <= 0L) {
              tokens.divider
            } else {
              cotisationColorForAmount(monthFee, cfg.defaultMonthlyFee)
            }
            val barFillFraction by animateFloatAsState(
              targetValue = if (collected > 0L) ratio.coerceAtLeast(0.10f) else 0.06f,
              animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
              label = "chart_bar_${month.key}",
            )
            val prevFillFraction by animateFloatAsState(
              targetValue = if (prevCollected > 0L) prevRatio.coerceAtLeast(0.08f) else 0f,
              animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
              label = "chart_prev_bar_${month.key}",
            )

            Column(
              modifier = Modifier.weight(1f),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
              Text(
                text = if (collected > 0L) formatNumber(collected, dig) else "0",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = numericFont,
                fontWeight = if (collected > 0L) FontWeight.SemiBold else FontWeight.Normal,
                color = if (collected > 0L) tokens.inkDeep else tokens.mute,
                maxLines = 1,
                overflow = TextOverflow.Clip,
              )
              Box(
                modifier = Modifier
                  .fillMaxWidth(0.76f)
                  .height(76.dp)
                  .clip(RoundedCornerShape(6.dp))
                  .background(tokens.trackBg),
                contentAlignment = Alignment.BottomCenter,
              ) {
                Row(
                  modifier = Modifier.fillMaxSize(),
                  horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
                  verticalAlignment = Alignment.Bottom,
                ) {
                  if (data.hasPreviousYearChartData && prevFillFraction > 0f) {
                    Box(
                      modifier = Modifier
                        .weight(0.42f)
                        .height((76f * prevFillFraction).dp)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(tokens.mute.copy(alpha = 0.35f)),
                    )
                  }
                  Box(
                    modifier = Modifier
                      .weight(if (data.hasPreviousYearChartData && prevFillFraction > 0f) 0.58f else 1f)
                      .height((76f * barFillFraction).dp)
                      .clip(RoundedCornerShape(6.dp))
                      .background(barColor),
                  )
                }
              }
              Text(
                text = strings.monthShort(month).take(4),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = tokens.body,
                maxLines = 1,
                overflow = TextOverflow.Clip,
              )
            }
          }
        }
      }
    }
  }
}

/**
 * Anneau interactif des dépenses par catégorie : toucher une catégorie filtre la liste.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpenseCategoriesBreakdownCard(
  expenses: List<ExpenseRecord>,
  currency: String,
  digitsAr: Boolean,
  selectedCategory: String?,
  onSelectCategory: (String?) -> Unit,
  modifier: Modifier = Modifier,
) {
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val isPrivacy = LocalPrivacyMode.current
  val palette = remember(tokens) {
    listOf(
      MetaPrimaryCobalt,
      MetaSuccess,
      MetaAttention,
      MetaOculusPurple,
      MetaWarning,
      tokens.linkColor,
      tokens.negativeDeep,
    )
  }

  val grouped = remember(expenses) {
    expenses
      .groupBy { it.category }
      .mapValues { (_, list) -> list.sumOf { it.amountDh } }
      .entries
      .filter { it.value > 0L }
      .sortedByDescending { it.value }
  }
  val total = remember(grouped) { grouped.sumOf { it.value }.coerceAtLeast(1L) }
  if (grouped.isEmpty()) return

  FintechWhiteCard(
    modifier = modifier
      .fillMaxWidth()
      .testTag("expense_donut_chart"),
    padding = 16.dp,
  ) {
    Text(
      text = strings.expChartTitle,
      style = MaterialTheme.typography.titleSmall,
      fontWeight = FontWeight.SemiBold,
      color = tokens.inkDeep,
    )
    Spacer(modifier = Modifier.height(12.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Box(
        modifier = Modifier.size(86.dp),
        contentAlignment = Alignment.Center,
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val strokeW = 12.dp.toPx()
          val arcSize = Size(size.width - strokeW, size.height - strokeW)
          val topLeft = Offset(strokeW / 2f, strokeW / 2f)
          var startAngle = -90f
          grouped.forEachIndexed { idx, entry ->
            val sweep = (entry.value.toFloat() / total.toFloat()) * 360f
            val baseColor = palette[idx % palette.size]
            val dimmed = selectedCategory != null && selectedCategory != entry.key
            drawArc(
              color = if (dimmed) baseColor.copy(alpha = 0.25f) else baseColor,
              startAngle = startAngle,
              sweepAngle = (sweep - 2f).coerceAtLeast(1f),
              useCenter = false,
              topLeft = topLeft,
              size = arcSize,
              style = Stroke(width = strokeW, cap = StrokeCap.Round),
            )
            startAngle += sweep
          }
        }
        val centerAmt = if (selectedCategory != null) {
          grouped.firstOrNull { it.key == selectedCategory }?.value ?: total
        } else {
          total
        }
        Text(
          text = maskSensitiveDh(formatNumber(centerAmt, digitsAr), isPrivacy),
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = tokens.inkDeep,
          maxLines = 1,
        )
      }

      FlowRow(
        modifier = Modifier.weight(1f),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        grouped.take(5).forEachIndexed { idx, entry ->
          val color = palette[idx % palette.size]
          val isSelected = selectedCategory == entry.key
          val pct = ((entry.value.toFloat() / total.toFloat()) * 100f).toInt()
          val chipShape = RoundedCornerShape(100.dp)
          Surface(
            shape = chipShape,
            color = if (isSelected) color.copy(alpha = 0.18f) else tokens.surfaceSoft,
            border = BorderStroke(1.dp, if (isSelected) color else tokens.divider),
            modifier = Modifier
              .clip(chipShape)
              .clickable {
                onSelectCategory(if (isSelected) null else entry.key)
              }
              .testTag("exp_chart_cat_${entry.key}"),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(color),
              )
              Text(
                text = "${strings.categoryLabel(entry.key)} ${localizeDigits("$pct%", digitsAr)}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = tokens.inkDeep,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }
      }
    }
  }
}

/**
 * Dialogue de recherche globale unifiée (Appartements, Dépenses, Projets).
 */
@Composable
fun GlobalSearchDialog(
  data: SyndicatDashboardData,
  isAr: Boolean,
  onDismiss: () -> Unit,
  onSelectApartment: (ApartmentRecord) -> Unit,
  onSelectExpense: (ExpenseRecord) -> Unit,
  onSelectProject: (ProjectRecord) -> Unit,
) {
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val cfg = data.config
  val cur = cfg.currency
  val dig = cfg.digitsAr
  val isPrivacy = LocalPrivacyMode.current

  var query by remember { mutableStateOf("") }
  val cleanQ = query.trim().lowercase()

  val matchingApts = remember(cleanQ, data.apartments) {
    if (cleanQ.isEmpty()) {
      data.apartments.take(4)
    } else {
      data.apartments.filter { apt ->
        apt.number.toString().contains(cleanQ) ||
          apt.displayNumber.lowercase().contains(cleanQ) ||
          apt.ownerName.lowercase().contains(cleanQ) ||
          apt.ownerNameAr.lowercase().contains(cleanQ) ||
          apt.phone.contains(cleanQ)
      }.take(6)
    }
  }
  val matchingExps = remember(cleanQ, data.expenses) {
    if (cleanQ.isEmpty()) {
      data.expenses.take(3)
    } else {
      data.expenses.filter { exp ->
        exp.title.lowercase().contains(cleanQ) ||
          exp.supplier.lowercase().contains(cleanQ) ||
          exp.category.lowercase().contains(cleanQ)
      }.take(5)
    }
  }
  val matchingProjs = remember(cleanQ, data.projects) {
    if (cleanQ.isEmpty()) {
      data.projects.take(3)
    } else {
      data.projects.filter { p ->
        p.titleFr.lowercase().contains(cleanQ) ||
          p.titleAr.lowercase().contains(cleanQ) ||
          p.contractor.lowercase().contains(cleanQ) ||
          p.category.lowercase().contains(cleanQ)
      }.take(5)
    }
  }

  SyndicatDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = strings.globalSearchTitle,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        color = tokens.inkDeep,
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("global_search_dialog"),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        MetaSearchPill(
          value = query,
          onValueChange = { query = it },
          placeholder = strings.globalSearchHint,
          clearDescription = strings.apptClearSearch,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("global_search_input"),
        )

        if (matchingApts.isEmpty() && matchingExps.isEmpty() && matchingProjs.isEmpty()) {
          Text(
            text = strings.globalSearchEmpty,
            style = MaterialTheme.typography.bodySmall,
            color = tokens.mute,
            modifier = Modifier.padding(vertical = 12.dp),
          )
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(max = 320.dp)
              .testTag("global_search_results"),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(vertical = 2.dp),
          ) {
            if (matchingApts.isNotEmpty()) {
              item {
                Text(
                  text = strings.globalSearchSecApts,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = tokens.mute,
                )
              }
              items(matchingApts, key = { "gs_apt_${it.number}" }) { apt ->
                val ownerLabel = maskSensitiveName(
                  apt.displayOwner(isAr).ifBlank { "—" },
                  isPrivacy,
                )
                GlobalSearchResultRow(
                  icon = Icons.Filled.Apartment,
                  title = "${strings.colAppt} ${localizeDigits(apt.displayNumber, dig)} · $ownerLabel",
                  trailing = maskSensitiveDh(formatDh(apt.remainingDh(cfg), cur, dig), isPrivacy),
                  onClick = { onSelectApartment(apt) },
                  testTag = "gs_apt_${apt.number}",
                )
              }
            }
            if (matchingExps.isNotEmpty()) {
              item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = strings.globalSearchSecExpenses,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = tokens.mute,
                )
              }
              items(matchingExps, key = { "gs_exp_${it.id}" }) { exp ->
                GlobalSearchResultRow(
                  icon = Icons.AutoMirrored.Filled.ReceiptLong,
                  title = exp.title,
                  trailing = maskSensitiveDh(formatDh(exp.amountDh, cur, dig), isPrivacy),
                  onClick = { onSelectExpense(exp) },
                  testTag = "gs_exp_${exp.id}",
                )
              }
            }
            if (matchingProjs.isNotEmpty()) {
              item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = strings.globalSearchSecProjects,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = tokens.mute,
                )
              }
              items(matchingProjs, key = { "gs_proj_${it.id}" }) { proj ->
                GlobalSearchResultRow(
                  icon = Icons.Filled.Construction,
                  title = proj.displayTitle(isAr),
                  trailing = "${localizeDigits(proj.clampedProgressPercent.toString(), dig)}%",
                  onClick = { onSelectProject(proj) },
                  testTag = "gs_proj_${proj.id}",
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {},
    dismissButton = {
      SageSecondaryButton(
        text = strings.apptCancel,
        compact = true,
        onClick = onDismiss,
      )
    },
  )
}

@Composable
private fun GlobalSearchResultRow(
  icon: ImageVector,
  title: String,
  trailing: String,
  onClick: () -> Unit,
  testTag: String,
) {
  val tokens = LocalSyndicatTokens.current
  val shape = RoundedCornerShape(12.dp)
  Surface(
    shape = shape,
    color = tokens.surfaceSoft,
    border = BorderStroke(1.dp, tokens.divider),
    modifier = Modifier
      .fillMaxWidth()
      .minimumInteractiveComponentSize()
      .clip(shape)
      .clickable(onClick = onClick)
      .testTag(testTag),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tokens.linkColor,
        modifier = Modifier.size(18.dp),
      )
      Text(
        text = title,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Medium,
        color = tokens.inkDeep,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(1f),
      )
      Text(
        text = trailing,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = tokens.body,
      )
    }
  }
}
