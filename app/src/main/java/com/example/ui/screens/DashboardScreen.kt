package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.HorizontalDivider
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
import com.example.ui.components.CoinbaseKpiMetricCard
import com.example.ui.components.CollectionRateRingCard
import com.example.ui.components.DashboardQuickActionButton
import com.example.ui.components.FintechWhiteCard
import com.example.ui.components.HeroOverlayCircleButton
import com.example.ui.components.MetaCobaltBuyButton
import com.example.ui.components.MonthlyCollectionsChartCard
import com.example.ui.components.RemindedPillBadge
import com.example.ui.components.ResidenceHeroBanner
import com.example.ui.components.SageSecondaryButton
import com.example.ui.components.SyndicatProgressBar
import com.example.ui.components.TreasuryPortfolioHeroCard
import com.example.ui.components.WhatsAppReminderDialog
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalStrings
import com.example.ui.i18n.bidiLtr
import com.example.ui.i18n.formatNumber
import com.example.ui.i18n.localizeDigits
import com.example.ui.theme.CoinbaseDarkBlueContainer
import com.example.ui.theme.CoinbaseDarkErrorContainer
import com.example.ui.theme.CoinbaseErrorTintLight
import com.example.ui.theme.CoinbaseOnDarkSoft
import com.example.ui.theme.CoinbasePrimary
import com.example.ui.theme.CoinbasePrimaryTintLight
import com.example.ui.theme.CoinbaseSemanticUp
import com.example.ui.theme.CoinbaseSuccessTintLight
import com.example.ui.theme.CoinbaseSurfaceDark
import com.example.ui.theme.CoinbaseSurfaceDarkElevated
import com.example.ui.theme.CoinbaseWarningTintLight
import com.example.ui.theme.LocalSyndicatTokens
import com.example.ui.theme.cotisationColorForAmount
import com.example.ui.viewmodel.SyndicatDashboardData
import com.example.ui.viewmodel.formatDh
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Tableau de bord recréé dans l'esprit Coinbase Fintech :
 * - En-tête photo personnalisable de la résidence (`ResidenceHeroBanner`)
 * - Carte Portefeuille de Trésorerie (`hero_cash_card`) avec ventilation directe Encaissé / Réglé / Solde
 * - Barre de 4 actions rapides (Cotisations, Dépenses, Relances WhatsApp, Affiches PDF)
 * - Grille 2×2 de cartes KPI épurées sur fond blanc bordé avec anneau circulaire de recouvrement
 * - Centre de recouvrement & file prioritaire WhatsApp avec bouton pilule Bleu Coinbase (`#0052FF`)
 * - Suivi du chantier en cours & projets finançables selon l'avance de caisse
 * - Graphique mensuel des encaissements et répartition par étage (zéro scroll horizontal)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
  data: SyndicatDashboardData,
  isBackupOverdue: Boolean,
  onTriggerBackupJson: () -> Unit,
  onOpenWizard: () -> Unit,
  onMarkReminded: (ApartmentRecord, String?, String?) -> Unit,
  onNavigateToPosters: () -> Unit,
  onNavigateToProjects: () -> Unit = {},
  onNavigateToApartments: () -> Unit = {},
  onNavigateToExpenses: () -> Unit = {},
  onNavigateToLateApartments: () -> Unit,
  onInfoMessage: (String) -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val cfg = data.config
  val cur = cfg.currency
  val dig = cfg.digitsAr
  val isAr = strings.language == AppLanguage.AR
  val numericFont = tokens.numericFontFamily

  var showCampaignDialog by remember { mutableStateOf(false) }
  var selectedSingleReminderApt by remember { mutableStateOf<ApartmentRecord?>(null) }

  val campaignTargets = remember(data.reminderQueueApartments, data.lateApartments) {
    data.reminderQueueApartments.ifEmpty { data.lateApartments }
  }

  val triggerSendReminders = {
    if (campaignTargets.isNotEmpty()) {
      selectedSingleReminderApt = null
      showCampaignDialog = true
    } else {
      onNavigateToLateApartments()
    }
  }

  if (showCampaignDialog && campaignTargets.isNotEmpty()) {
    val singleTarget = selectedSingleReminderApt
    val queueForDialog = if (singleTarget != null) listOf(singleTarget) else campaignTargets
    WhatsAppReminderDialog(
      apartmentsQueue = queueForDialog,
      config = cfg,
      initialApartment = singleTarget,
      onDismiss = {
        showCampaignDialog = false
        selectedSingleReminderApt = null
      },
      onReminderSent = { apt, updatedPhone, updatedTemplate ->
        onMarkReminded(apt, updatedPhone, updatedTemplate)
      },
      onInfoMessage = onInfoMessage,
    )
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(tokens.screenBg)
      .testTag("dashboard_screen"),
    contentPadding = PaddingValues(
      start = 16.dp,
      end = 16.dp,
      top = 14.dp,
      bottom = 32.dp,
    ),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    // =========================================================================
    // 1. BANNIÈRE PHOTO PERSONNALISÉE DE LA RÉSIDENCE
    // =========================================================================
    item(key = "dash_hero_residence") {
      val cityPart = if (cfg.city.isNotBlank()) " · ${cfg.city}" else ""
      val addrPart = if (cfg.address.isNotBlank()) " · ${cfg.address}" else ""
      val subText = "${cfg.syndicatName.ifBlank { strings.authTitle }}$cityPart$addrPart"
      val yearBadge = "${bidiLtr(localizeDigits(cfg.year.toString(), dig))} · ${strings.navDashboard}"

      ResidenceHeroBanner(
        residenceName = cfg.residenceName.ifBlank { strings.navDashboard },
        subtitle = subText,
        badgeText = yearBadge,
        onPhotoFeedback = onInfoMessage,
        trailingActions = {
          HeroOverlayCircleButton(
            icon = Icons.Default.AutoFixHigh,
            contentDescription = strings.dashLaunchWizardBtn,
            onClick = onOpenWizard,
            modifier = Modifier.testTag("dash_open_wizard_btn"),
          )
          HeroOverlayCircleButton(
            icon = Icons.Default.PictureAsPdf,
            contentDescription = strings.navPoster,
            onClick = onNavigateToPosters,
            modifier = Modifier.testTag("dash_open_posters_btn"),
          )
        },
      )
    }

    // =========================================================================
    // 2. CARTE PORTEFEUILLE DE TRÉSORERIE EN DIRECT (Coinbase Editorial Dark)
    // =========================================================================
    item(key = "dash_treasury_portfolio") {
      TreasuryPortfolioHeroCard(
        data = data,
        numericFont = numericFont,
      )
    }

    // =========================================================================
    // 3. BARRE DE 4 ACTIONS RAPIDES (Style Coinbase Portfolio Actions)
    // =========================================================================
    item(key = "dash_quick_actions_row") {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        DashboardQuickActionButton(
          icon = Icons.Default.Payments,
          label = strings.dashQuickCotisations,
          isPrimary = true,
          onClick = onNavigateToApartments,
          modifier = Modifier
            .weight(1f)
            .testTag("dash_quick_cotisations_btn"),
        )
        DashboardQuickActionButton(
          icon = Icons.AutoMirrored.Filled.ReceiptLong,
          label = strings.dashQuickExpenses,
          isPrimary = false,
          onClick = onNavigateToExpenses,
          modifier = Modifier
            .weight(1f)
            .testTag("dash_quick_expenses_btn"),
        )
        DashboardQuickActionButton(
          icon = Icons.AutoMirrored.Filled.Send,
          label = strings.dashQuickRemind,
          isPrimary = false,
          badgeCount = data.reminderQueueApartments.size,
          digitsAr = dig,
          onClick = triggerSendReminders,
          modifier = Modifier
            .weight(1f)
            .testTag("dash_quick_remind_btn"),
        )
        DashboardQuickActionButton(
          icon = Icons.Default.PictureAsPdf,
          label = strings.dashQuickExports,
          isPrimary = false,
          onClick = onNavigateToPosters,
          modifier = Modifier
            .weight(1f)
            .testTag("dash_quick_posters_btn"),
        )
      }
    }

    // =========================================================================
    // 4. ALERTE SAUVEGARDE JSON (uniquement mise en avant lorsqu'en retard)
    // =========================================================================
    if (isBackupOverdue) {
      item(key = "dash_backup_banner") {
        val warnBg = if (tokens.isDark) Color(0xFF2B2209) else CoinbaseWarningTintLight
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = warnBg,
          border = BorderStroke(1.dp, tokens.warning.copy(alpha = 0.55f)),
          tonalElevation = 0.dp,
          shadowElevation = 0.dp,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("backup_reminder_banner"),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Row(
              modifier = Modifier.weight(1f),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = null,
                tint = tokens.warning,
                modifier = Modifier.size(20.dp),
              )
              Text(
                text = strings.dashBackupReminder,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = tokens.inkDeep,
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            SageSecondaryButton(
              text = strings.dashBackupNowBtn,
              icon = Icons.Default.Backup,
              compact = true,
              onClick = onTriggerBackupJson,
              modifier = Modifier.testTag("dash_backup_now_btn"),
            )
          }
        }
      }
    }

    // =========================================================================
    // 5. GRILLE 2×2 DES INDICATEURS CLÉS (Cartes Coinbase épurées + Anneau %)
    // =========================================================================
    item(key = "dash_kpi_grid") {
      val balanceText = if (data.balanceDh < 0L) {
        "▼ ${formatDh(data.balanceDh, cur, dig)}"
      } else {
        formatDh(data.balanceDh, cur, dig)
      }
      val posBg = if (tokens.isDark) Color(0xFF0A2E1F) else CoinbaseSuccessTintLight
      val negBg = if (tokens.isDark) CoinbaseDarkErrorContainer else CoinbaseErrorTintLight
      val blueBg = if (tokens.isDark) CoinbaseDarkBlueContainer else CoinbasePrimaryTintLight

      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          CoinbaseKpiMetricCard(
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            iconTint = tokens.positiveDeep,
            iconContainer = posBg,
            label = strings.dashCollectedLabel,
            valueText = formatDh(data.totalCaisseDh, cur, dig),
            valueColor = tokens.positiveDeep,
            subtitle = "${formatNumber(cfg.activeMonthColumns.size.toLong(), dig)} ${if (isAr) "أشهر نشطة" else "mois actifs"}",
            numericFont = numericFont,
            onClick = onNavigateToApartments,
            modifier = Modifier
              .weight(1f)
              .testTag("kpi_collected_card"),
          )
          CoinbaseKpiMetricCard(
            icon = Icons.AutoMirrored.Filled.TrendingDown,
            iconTint = tokens.negativeDeep,
            iconContainer = negBg,
            label = strings.dashExpensesLabel,
            valueText = formatDh(data.totalExpensesDh, cur, dig),
            valueColor = tokens.inkDeep,
            subtitle = "${formatNumber(data.expenses.size.toLong(), dig)} ${if (isAr) "عمليات" else "opérations"}",
            numericFont = numericFont,
            onClick = onNavigateToExpenses,
            modifier = Modifier
              .weight(1f)
              .testTag("kpi_expenses_card"),
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          // Carte Taux de recouvrement avec anneau circulaire Canvas
          CollectionRateRingCard(
            ratePercent = data.collectionRatePercent,
            upToDateCount = data.upToDateCount,
            occupiedCount = data.occupiedCount,
            digitsAr = dig,
            numericFont = numericFont,
            onClick = onNavigateToApartments,
            modifier = Modifier
              .weight(1f)
              .testTag("kpi_rate_card"),
          )

          CoinbaseKpiMetricCard(
            icon = Icons.Default.AccountBalanceWallet,
            iconTint = tokens.linkColor,
            iconContainer = blueBg,
            label = strings.dashNetBalanceLabel,
            valueText = balanceText,
            valueColor = if (data.balanceDh >= 0L) tokens.inkDeep else tokens.negativeDeep,
            subtitle = if (isAr) "المداخيل − المصاريف" else "Recettes − Dépenses",
            numericFont = numericFont,
            onClick = onNavigateToExpenses,
            modifier = Modifier
              .weight(1f)
              .testTag("kpi_balance_card"),
          )
        }
      }
    }

    // =========================================================================
    // 6. CENTRE DE RECOUVREMENT & RELANCES WHATSAPP
    // =========================================================================
    item(key = "dash_collection_action_center") {
      if (data.lateApartments.isEmpty()) {
        val okBg = if (tokens.isDark) Color(0xFF0A2E1F) else CoinbaseSuccessTintLight
        Surface(
          shape = RoundedCornerShape(tokens.cardRadiusDp.dp),
          color = okBg,
          border = BorderStroke(1.dp, tokens.positive.copy(alpha = 0.35f)),
          tonalElevation = 0.dp,
          shadowElevation = 0.dp,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("all_paid_card"),
        ) {
          Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Surface(
                shape = CircleShape,
                color = tokens.positive.copy(alpha = 0.18f),
                modifier = Modifier.size(44.dp),
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = tokens.positiveDeep,
                    modifier = Modifier.size(24.dp),
                  )
                }
              }
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = strings.dashAllPaidTitle,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = tokens.inkDeep,
                )
                Text(
                  text = strings.dashAllPaidSub,
                  style = MaterialTheme.typography.bodySmall,
                  color = tokens.body,
                )
              }
            }
            SageSecondaryButton(
              text = strings.dashSendRemindersBtn,
              icon = Icons.AutoMirrored.Filled.Send,
              compact = true,
              onClick = triggerSendReminders,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("dash_send_reminders_cta"),
            )
          }
        }
      } else {
        // Carte Recouvrement & Relances WhatsApp (Coinbase crisp white/dark card + Primary Blue CTA)
        Surface(
          shape = RoundedCornerShape(tokens.cardRadiusDp.dp),
          color = tokens.cardBg,
          border = BorderStroke(1.dp, tokens.cardBorder),
          tonalElevation = 0.dp,
          shadowElevation = 0.dp,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("unpaid_feature_dark_card"),
        ) {
          Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              val errTint = if (tokens.isDark) CoinbaseDarkErrorContainer else CoinbaseErrorTintLight
              Surface(
                shape = RoundedCornerShape(100.dp),
                color = errTint,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
              ) {
                Text(
                  text = "! ${strings.dashUnpaidSub(data.lateApartments.size)}",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = tokens.statusLateText,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
              }

              Text(
                text = "${strings.monthShort(data.cutoffMonthColumn)} ${bidiLtr(localizeDigits(cfg.year.toString(), dig))}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = tokens.mute,
              )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Text(
                text = strings.dashUnpaidTitle,
                style = MaterialTheme.typography.bodyMedium,
                color = tokens.body,
              )
              Text(
                text = formatDh(data.totalRemainingToCollectDh, cur, dig),
                style = MaterialTheme.typography.headlineLarge.copy(
                  fontFamily = numericFont,
                  fontFeatureSettings = "tnum",
                ),
                fontWeight = FontWeight.SemiBold,
                color = tokens.statusLateText,
              )
            }

            // Bouton primaire Bleu Coinbase (#0052FF) + bouton secondaire vers la liste filtrée
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              MetaCobaltBuyButton(
                text = strings.dashSendRemindersBtn,
                icon = Icons.AutoMirrored.Filled.Send,
                onClick = triggerSendReminders,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("dash_send_reminders_cta"),
              )
              SageSecondaryButton(
                text = strings.dashViewLateBtn,
                compact = true,
                onClick = onNavigateToLateApartments,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("dash_view_late_btn"),
              )
            }

            // File prioritaire intégrée (jusqu'à 5 appartements à relancer immédiatement)
            if (data.reminderQueueApartments.isNotEmpty()) {
              HorizontalDivider(color = tokens.divider)
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = strings.dashRelanceQueueTitle,
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.SemiBold,
                  color = tokens.inkDeep,
                )
                Surface(
                  shape = RoundedCornerShape(100.dp),
                  color = tokens.surfaceStrong,
                  tonalElevation = 0.dp,
                  shadowElevation = 0.dp,
                ) {
                  Text(
                    text = formatNumber(data.reminderQueueApartments.size.toLong(), dig),
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = numericFont,
                    fontWeight = FontWeight.SemiBold,
                    color = tokens.inkDeep,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                  )
                }
              }

              Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                data.reminderQueueApartments.take(5).forEach { apt ->
                  val rem = apt.remainingDh(cfg)
                  val missingLabels = apt.missingMonths(cfg).joinToString(" · ") { strings.monthShort(it) }
                  val ownerLabel = apt.displayOwner(isAr).ifBlank { "${strings.colAppt} ${apt.displayNumber}" }

                  Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = tokens.surfaceSoft,
                    border = BorderStroke(1.dp, tokens.divider),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth(),
                  ) {
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                      Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                      ) {
                        Surface(
                          shape = CircleShape,
                          color = tokens.cardBg,
                          border = BorderStroke(1.dp, tokens.cardBorder),
                          modifier = Modifier.size(38.dp),
                        ) {
                          Box(contentAlignment = Alignment.Center) {
                            Text(
                              text = localizeDigits(apt.displayNumber, dig),
                              style = MaterialTheme.typography.labelLarge,
                              fontFamily = numericFont,
                              fontWeight = FontWeight.SemiBold,
                              color = tokens.inkDeep,
                            )
                          }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                          Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                          ) {
                            Text(
                              text = ownerLabel,
                              style = MaterialTheme.typography.titleSmall,
                              fontWeight = FontWeight.SemiBold,
                              color = tokens.inkDeep,
                              maxLines = 1,
                              overflow = TextOverflow.Ellipsis,
                              modifier = Modifier.weight(1f, fill = false),
                            )
                            if (apt.lastReminderAt > 0L) {
                              val dateFmt = bidiLtr(
                                localizeDigits(
                                  SimpleDateFormat("dd/MM", Locale.US).format(Date(apt.lastReminderAt)),
                                  dig,
                                )
                              )
                              RemindedPillBadge(label = "${strings.badgeReminded} $dateFmt")
                            }
                          }
                          Text(
                            text = "${strings.floorDisplay(apt.floor)} · $missingLabels",
                            style = MaterialTheme.typography.labelSmall,
                            color = tokens.body,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                          )
                          Text(
                            text = formatDh(rem, cur, dig),
                            style = MaterialTheme.typography.labelLarge,
                            fontFamily = numericFont,
                            fontWeight = FontWeight.SemiBold,
                            color = tokens.statusLateText,
                          )
                        }
                      }

                      Spacer(modifier = Modifier.width(8.dp))

                      SageSecondaryButton(
                        text = strings.dashRemindBtn,
                        icon = Icons.AutoMirrored.Filled.Send,
                        compact = true,
                        onClick = {
                          selectedSingleReminderApt = apt
                          showCampaignDialog = true
                        },
                        modifier = Modifier.testTag("dash_remind_apt_${apt.number}"),
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

    // =========================================================================
    // 7. PROJETS & TRAVAUX DE L'IMMEUBLE (Chantier en cours + Avance Caisse)
    // =========================================================================
    item(key = "dash_projects_overview") {
      FintechWhiteCard(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("dash_projects_summary_card"),
        padding = 20.dp,
        onClick = onNavigateToProjects,
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            val blueTint = if (tokens.isDark) CoinbaseDarkBlueContainer else CoinbasePrimaryTintLight
            Surface(
              shape = CircleShape,
              color = blueTint,
              modifier = Modifier.size(38.dp),
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.Construction,
                  contentDescription = null,
                  tint = tokens.linkColor,
                  modifier = Modifier.size(19.dp),
                )
              }
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = strings.dashProjectsCardTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = tokens.inkDeep,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              if (data.plannedProjects.isNotEmpty()) {
                Text(
                  text = "${strings.projectsKpiReadyLabel}: ${formatNumber(data.readyToStartProjectsCount.toLong(), dig)} / ${formatNumber(data.plannedProjects.size.toLong(), dig)}",
                  style = MaterialTheme.typography.labelSmall,
                  color = tokens.body,
                )
              }
            }
          }
          Spacer(modifier = Modifier.width(8.dp))
          SageSecondaryButton(
            text = strings.dashProjectsOpenBtn,
            compact = true,
            onClick = onNavigateToProjects,
            modifier = Modifier.testTag("dash_open_projects_btn"),
          )
        }

        val topOngoing = data.inProgressProjects.firstOrNull()
        if (topOngoing != null) {
          Spacer(modifier = Modifier.height(14.dp))
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = tokens.surfaceSoft,
            border = BorderStroke(1.dp, tokens.divider),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(
              modifier = Modifier.padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = topOngoing.displayTitle(isAr),
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = tokens.inkDeep,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = bidiLtr("${localizeDigits(topOngoing.clampedProgressPercent.toString(), dig)}%"),
                  style = MaterialTheme.typography.titleSmall,
                  fontFamily = numericFont,
                  fontWeight = FontWeight.SemiBold,
                  color = tokens.positiveDeep,
                )
              }
              SyndicatProgressBar(
                progress = topOngoing.clampedProgressPercent / 100f,
                modifier = Modifier.fillMaxWidth(),
              )
            }
          }
        } else if (data.projects.isEmpty()) {
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = strings.projectsEmptySub,
            style = MaterialTheme.typography.bodySmall,
            color = tokens.mute,
          )
        }
      }
    }

    // =========================================================================
    // 8. GRAPHIQUE MENSUEL DES ENCAISSEMENTS (100 % largeur portrait, sans slider)
    // =========================================================================
    item(key = "dash_monthly_chart") {
      MonthlyCollectionsChartCard(
        data = data,
        isAr = isAr,
        numericFont = numericFont,
        onClick = onNavigateToApartments,
      )
    }

    // =========================================================================
    // 9. PERFORMANCE DE RECOUVREMENT PAR ÉTAGE
    // =========================================================================
    item(key = "dash_floors_breakdown") {
      FintechWhiteCard(
        modifier = Modifier.fillMaxWidth(),
        padding = 20.dp,
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Icon(
              imageVector = Icons.Default.Apartment,
              contentDescription = null,
              tint = tokens.linkColor,
              modifier = Modifier.size(20.dp),
            )
            Text(
              text = strings.dashFloorsSection,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              color = tokens.inkDeep,
            )
          }
          Text(
            text = strings.dashUnitsSummary(data.upToDateCount, data.occupiedCount),
            style = MaterialTheme.typography.labelSmall,
            color = tokens.mute,
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        data.orderedFloors.forEachIndexed { idx, floor ->
          val floorApts = data.apartments.filter { it.floor == floor }
          val occ = floorApts.count { !it.isVacantForConfig(cfg) }
          val totalFloorDh = floorApts.sumOf { it.totalForConfig(cfg) }
          val dueFloorDh = floorApts.sumOf { it.dueAtCutoffDh(cfg) }
          val pct = if (dueFloorDh > 0L) {
            ((totalFloorDh.toFloat() / dueFloorDh.toFloat()) * 100f).toInt().coerceIn(0, 100)
          } else {
            100
          }

          if (idx > 0) {
            HorizontalDivider(
              modifier = Modifier.padding(vertical = 12.dp),
              color = tokens.divider,
            )
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = strings.dashFloorRowTitle(strings.floorWithPrefix(floor), occ, floorApts.size),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = tokens.inkDeep,
              )
              Spacer(modifier = Modifier.height(6.dp))
              SyndicatProgressBar(
                progress = pct / 100f,
                modifier = Modifier.fillMaxWidth(0.88f),
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = formatDh(totalFloorDh, cur, dig),
                style = MaterialTheme.typography.titleSmall,
                fontFamily = numericFont,
                fontWeight = FontWeight.SemiBold,
                color = tokens.positiveDeep,
              )
              Text(
                text = bidiLtr("${localizeDigits(pct.toString(), dig)}%"),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = numericFont,
                color = tokens.mute,
              )
            }
          }
        }
      }
    }

    // =========================================================================
    // 10. OUTILS D'ADMINISTRATION, EXPORT PDF A4 & SAUVEGARDE JSON
    // =========================================================================
    item(key = "dash_admin_shortcuts") {
      Surface(
        shape = RoundedCornerShape(tokens.cardRadiusDp.dp),
        color = tokens.surfaceSoft,
        border = BorderStroke(1.dp, tokens.divider),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Text(
            text = strings.dashExportShortcutsTitle,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = tokens.inkDeep,
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            SageSecondaryButton(
              text = strings.navPoster,
              icon = Icons.Default.PictureAsPdf,
              onClick = onNavigateToPosters,
              modifier = Modifier
                .weight(1f)
                .testTag("dash_shortcut_posters"),
            )
            SageSecondaryButton(
              text = strings.dashBackupNowBtn,
              icon = Icons.Default.Backup,
              onClick = onTriggerBackupJson,
              modifier = Modifier
                .weight(1f)
                .testTag("dash_shortcut_backup"),
            )
          }
          SageSecondaryButton(
            text = strings.dashLaunchWizardBtn,
            icon = Icons.Default.AutoFixHigh,
            onClick = onOpenWizard,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("dash_bottom_wizard_btn"),
          )
        }
      }
    }
  }
}

