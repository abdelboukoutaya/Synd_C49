package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
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
) {
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val cfg = data.config
  val cur = cfg.currency
  val dig = cfg.digitsAr

  val cashDisplay = if (data.cashNowDh < 0L) {
    "▼ ${formatDh(data.cashNowDh, cur, dig)}"
  } else {
    formatDh(data.cashNowDh, cur, dig)
  }

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
            value = formatDh(data.totalCaisseDh, cur, dig, explicitPlus = true),
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
            value = formatDh(data.totalExpensesPaidDh, cur, dig),
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
            value = formatDh(data.balanceDh, cur, dig),
            valueColor = if (data.balanceDh >= 0L) Color.White else Color(0xFFFF6B78),
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
                text = strings.dashOpeningBalanceNote(formatDh(cfg.openingBalanceDh, cur, dig)),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.90f),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
              )
            }
          }
          if (data.unpaidExpensesDh > 0L) {
            Surface(
              shape = RoundedCornerShape(100.dp),
              color = Color(0xFF3B1219),
              border = BorderStroke(1.dp, Color(0xFFFF6B78).copy(alpha = 0.35f)),
            ) {
              Text(
                text = "◐ ${strings.expSupplierRemaining(formatDh(data.unpaidExpensesDh, cur, dig))}",
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
  val ringColor = when {
    clamped >= 80 -> tokens.positive
    clamped >= 50 -> tokens.linkColor
    else -> tokens.warning
  }
  val trackColor = tokens.divider

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
              sweepAngle = 360f * (clamped / 100f),
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
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
        }
      }
    }

    if (activeMonths.isNotEmpty()) {
      Spacer(modifier = Modifier.height(18.dp))

      Surface(
        shape = RoundedCornerShape(14.dp),
        color = tokens.surfaceSoft,
        border = BorderStroke(1.dp, tokens.divider),
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
            val ratio = (collected.toFloat() / maxExpected.toFloat()).coerceIn(0f, 1f)
            val barColor = if (collected <= 0L) {
              tokens.divider
            } else {
              cotisationColorForAmount(monthFee, cfg.defaultMonthlyFee)
            }
            val barFillFraction = if (collected > 0L) ratio.coerceAtLeast(0.10f) else 0.06f

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
                  .fillMaxWidth(0.68f)
                  .height(76.dp)
                  .clip(RoundedCornerShape(6.dp))
                  .background(tokens.trackBg),
                contentAlignment = Alignment.BottomCenter,
              ) {
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height((76f * barFillFraction).dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(barColor),
                )
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
