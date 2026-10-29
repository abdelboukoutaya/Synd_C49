package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ApartmentRecord
import com.example.ui.components.AirtableCreamCalloutCard
import com.example.ui.components.AirtablePastelKpiCard
import com.example.ui.components.AirtableSignatureCard
import com.example.ui.components.AirtableSoftCard
import com.example.ui.components.FintechPalePositiveCard
import com.example.ui.components.FintechWhiteCard
import com.example.ui.components.LimePrimaryButton
import com.example.ui.components.RemindedPillBadge
import com.example.ui.components.RoundIconCircleButton
import com.example.ui.components.SageSecondaryButton
import com.example.ui.components.SyndicatProgressBar
import com.example.ui.components.WhatsAppReminderDialog
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalStrings
import com.example.ui.i18n.bidiLtr
import com.example.ui.i18n.formatNumber
import com.example.ui.i18n.localizeDigits
import com.example.ui.theme.LocalSyndicatTokens
import com.example.ui.theme.MetaCanvas
import com.example.ui.theme.MetaCritical
import com.example.ui.theme.MetaInkDeep
import com.example.ui.theme.MetaPrimaryCobalt
import com.example.ui.theme.MetaSuccess
import com.example.ui.theme.MetaWarning
import com.example.ui.theme.cotisationColorForAmount
import com.example.ui.viewmodel.SyndicatDashboardData
import com.example.ui.viewmodel.formatDh
import com.example.util.WhatsAppHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
  val numericFont = tokens.numericFontFamily ?: tokens.monoFontFamily

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
      top = 16.dp,
      bottom = 32.dp,
    ),
    verticalArrangement = Arrangement.spacedBy(24.dp),
  ) {
    // 1. Bande blanche : Titre de l'immeuble en title (400) + boutons icône 48dp
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = cfg.residenceName.ifBlank { strings.navDashboard },
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Medium,
            color = tokens.inkDeep,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
          val cityPart = if (cfg.city.isNotBlank()) " · ${cfg.city}" else ""
          val subText = "${strings.navDashboard}$cityPart · ${bidiLtr(localizeDigits(cfg.year.toString(), dig))}"
          Text(
            text = subText,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Light,
            color = tokens.body,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          RoundIconCircleButton(
            icon = Icons.Default.AutoFixHigh,
            contentDescription = strings.dashLaunchWizardBtn,
            onClick = onOpenWizard,
            modifier = Modifier.testTag("dash_open_wizard_btn"),
          )
          RoundIconCircleButton(
            icon = Icons.Default.PictureAsPdf,
            contentDescription = strings.navPoster,
            onClick = onNavigateToPosters,
            modifier = Modifier.testTag("dash_open_posters_btn"),
          )
        }
      }
    }

    // 2. Meta Showcase Card (#0A1317 Deep Ink, 32dp {rounded.xxxl}, 28dp padding)
    item {
      AirtableSignatureCard(
        backgroundColor = tokens.signatureForest,
        modifier = Modifier.fillMaxWidth().testTag("hero_cash_card"),
        padding = 28.dp,
      ) {
        Text(
          text = strings.dashCashEyebrow,
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Light,
          color = Color.White.copy(alpha = 0.88f),
        )
        Spacer(modifier = Modifier.height(8.dp))
        val cashDisplay = if (data.cashNowDh < 0L) {
          "▼ ${formatDh(data.cashNowDh, cur, dig)}"
        } else {
          formatDh(data.cashNowDh, cur, dig)
        }
        Text(
          text = cashDisplay,
          style = MaterialTheme.typography.displayLarge.copy(
            fontFamily = numericFont,
            fontFeatureSettings = "tnum, ss01, ss02",
          ),
          fontWeight = FontWeight.Medium,
          color = Color.White,
          modifier = Modifier.testTag("cash_now_value"),
        )

        if (cfg.openingBalanceDh != 0L || data.unpaidExpensesDh > 0L) {
          Spacer(modifier = Modifier.height(14.dp))
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            if (cfg.openingBalanceDh != 0L) {
              Surface(
                shape = RoundedCornerShape(100.dp),
                color = Color.White.copy(alpha = 0.14f),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
              ) {
                Text(
                  text = strings.dashOpeningBalanceNote(formatDh(cfg.openingBalanceDh, cur, dig)),
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                )
              }
            }
            if (data.unpaidExpensesDh > 0L) {
              Surface(
                shape = RoundedCornerShape(100.dp),
                color = Color.White.copy(alpha = 0.14f),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
              ) {
                Text(
                  text = "◐ ${strings.expSupplierRemaining(formatDh(data.unpaidExpensesDh, cur, dig))}",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                )
              }
            }
          }
        }
      }
    }

    // 3. Meta 2×2 Soft Cloud KPI Grid ({colors.surface-soft} #F1F4F7, {rounded.xxl} 24dp)
    item {
      val balanceText = if (data.balanceDh < 0L) {
        "▼ ${formatDh(data.balanceDh, cur, dig)}"
      } else {
        formatDh(data.balanceDh, cur, dig)
      }
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          AirtablePastelKpiCard(
            backgroundColor = MetaSuccess,
            label = strings.dashCollectedLabel,
            valueText = formatDh(data.totalCaisseDh, cur, dig),
            subtitle = "${formatNumber(cfg.activeMonthColumns.size.toLong(), dig)} ${if (isAr) "أشهر" else "mois"}",
            modifier = Modifier.weight(1f).testTag("kpi_collected_card"),
          )
          AirtablePastelKpiCard(
            backgroundColor = MetaCritical,
            label = strings.dashExpensesLabel,
            valueText = formatDh(data.totalExpensesDh, cur, dig),
            subtitle = "${formatNumber(data.expenses.size.toLong(), dig)} ${if (isAr) "عمليات" else "lignes"}",
            modifier = Modifier.weight(1f).testTag("kpi_expenses_card"),
          )
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          AirtablePastelKpiCard(
            backgroundColor = MetaWarning,
            label = strings.dashCollectionRateLabel,
            valueText = bidiLtr("${localizeDigits(data.collectionRatePercent.toString(), dig)}%"),
            subtitle = "✓ ${bidiLtr("${localizeDigits(data.upToDateCount.toString(), dig)}/${localizeDigits(data.occupiedCount.toString(), dig)}")} ${strings.badgePaid}",
            modifier = Modifier.weight(1f).testTag("kpi_rate_card"),
          )
          AirtableSoftCard(
            modifier = Modifier.weight(1f).testTag("kpi_balance_card"),
            padding = 20.dp,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .background(MetaPrimaryCobalt, CircleShape),
              )
              Text(
                text = strings.dashNetBalanceLabel,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = tokens.body,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = balanceText,
              style = MaterialTheme.typography.displayMedium.copy(
                fontFamily = numericFont,
                fontFeatureSettings = "tnum, ss01, ss02",
              ),
              fontWeight = FontWeight.Medium,
              color = if (data.balanceDh >= 0L) tokens.positiveDeep else tokens.negativeDeep,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = if (isAr) "المداخيل − المصاريف" else "Recettes − Dépenses",
              style = MaterialTheme.typography.bodySmall,
              color = tokens.mute,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }
    }

    // 4. Meta Promo-Strip Unpaid Card (#0A1317 32dp + badge-critical #E41E3F pill) or Soft Cloud All-Paid
    item {
      if (data.lateApartments.isEmpty()) {
        FintechPalePositiveCard(
          modifier = Modifier.fillMaxWidth().testTag("all_paid_card"),
          padding = 24.dp,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = tokens.positive,
              modifier = Modifier.size(30.dp),
            )
            Column {
              Text(
                text = "✓ ${strings.dashAllPaidTitle}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                color = tokens.inkDeep,
              )
              Text(
                text = strings.dashAllPaidSub,
                style = MaterialTheme.typography.bodySmall,
                color = tokens.body,
              )
            }
          }
        }
      } else {
        AirtableSignatureCard(
          backgroundColor = MetaCritical,
          modifier = Modifier.fillMaxWidth().testTag("unpaid_feature_dark_card"),
          padding = 26.dp,
        ) {
          Surface(
            shape = RoundedCornerShape(100.dp),
            color = MetaCritical,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
          ) {
            Text(
              text = "! ${strings.dashUnpaidSub(data.lateApartments.size)}",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MetaCanvas,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            )
          }
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = strings.dashUnpaidTitle,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Light,
            color = Color.White.copy(alpha = 0.90f),
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = formatDh(data.totalRemainingToCollectDh, cur, dig),
            style = MaterialTheme.typography.displayLarge.copy(
              fontFamily = numericFont,
              fontFeatureSettings = "tnum, ss01, ss02",
            ),
            fontWeight = FontWeight.Medium,
            color = Color.White,
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "${strings.monthFull(data.cutoffMonthColumn)} · ${cfg.year}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.82f),
          )
          Spacer(modifier = Modifier.height(20.dp))
          SageSecondaryButton(
            text = strings.dashViewLateBtn,
            onSignatureCard = true,
            onClick = onNavigateToLateApartments,
            modifier = Modifier.fillMaxWidth().testTag("dash_view_late_btn"),
          )
        }
      }
    }

    // 5. Meta Promo-Banner (#F7B928 yellow alert when backup overdue) or Soft Cobalt Callout (24dp)
    item {
      if (isBackupOverdue) {
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = MetaWarning,
          tonalElevation = 0.dp,
          shadowElevation = 0.dp,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("backup_reminder_banner"),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 18.dp, vertical = 14.dp),
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
                tint = MetaInkDeep,
              )
              Text(
                text = strings.dashBackupReminder,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MetaInkDeep,
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
      } else {
        AirtableCreamCalloutCard(
          modifier = Modifier.fillMaxWidth(),
          padding = 18.dp,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = null,
              tint = tokens.infoColor,
            )
            Text(
              text = strings.dashCreamTip,
              style = MaterialTheme.typography.bodySmall,
              color = tokens.inkDeep,
            )
          }
        }
      }
    }

    // 6. LE SEUL bouton primaire noir de l'écran : « إرسال التذكيرات »
    item {
      LimePrimaryButton(
        text = strings.dashSendRemindersBtn,
        icon = Icons.AutoMirrored.Filled.Send,
        onClick = triggerSendReminders,
        modifier = Modifier.fillMaxWidth().testTag("dash_send_reminders_cta"),
      )
    }

    // 6b. Aperçu rapide des Projets de l'immeuble (projet en cours + futurs selon avance de caisse)
    item {
      FintechWhiteCard(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("dash_projects_summary_card"),
        padding = 20.dp,
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
            Icon(
              imageVector = Icons.Default.Construction,
              contentDescription = null,
              tint = tokens.ink,
              modifier = Modifier.size(22.dp),
            )
            Text(
              text = strings.dashProjectsCardTitle,
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Normal,
              color = tokens.ink,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
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
          Spacer(modifier = Modifier.height(12.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = topOngoing.displayTitle(isAr),
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Medium,
              color = tokens.ink,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = bidiLtr("${localizeDigits(topOngoing.clampedProgressPercent.toString(), dig)}%"),
              style = MaterialTheme.typography.titleMedium,
              fontFamily = numericFont ?: MaterialTheme.typography.titleMedium.fontFamily,
              fontWeight = FontWeight.Medium,
              color = tokens.positiveDeep,
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          SyndicatProgressBar(
            progress = topOngoing.clampedProgressPercent / 100f,
            modifier = Modifier.fillMaxWidth(),
          )
        }

        if (data.plannedProjects.isNotEmpty()) {
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "${strings.projectsKpiReadyLabel}: ${formatNumber(data.readyToStartProjectsCount.toLong(), dig)} / ${formatNumber(data.plannedProjects.size.toLong(), dig)}",
            style = MaterialTheme.typography.bodySmall,
            color = tokens.body,
          )
        }
      }
    }

    // 7. File de relance WhatsApp (cartes blanches bordées + bouton secondaire « تذكير »)
    if (data.reminderQueueApartments.isNotEmpty()) {
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, start = 4.dp, end = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = strings.dashRelanceQueueTitle,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Normal,
            color = tokens.ink,
          )
          Surface(
            shape = RoundedCornerShape(tokens.badgeRadiusDp.dp),
            color = tokens.surfaceSoft,
            border = BorderStroke(1.dp, tokens.divider),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
          ) {
            Text(
              text = formatNumber(data.reminderQueueApartments.size.toLong(), dig),
              style = MaterialTheme.typography.labelMedium,
              fontFamily = numericFont ?: MaterialTheme.typography.labelMedium.fontFamily,
              fontWeight = FontWeight.Medium,
              color = tokens.ink,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            )
          }
        }
      }

      items(
        items = data.reminderQueueApartments.take(10),
        key = { "relance_${it.id.ifBlank { it.number.toString() }}" },
      ) { apt ->
        val rem = apt.remainingDh(cfg)
        val missingLabels = apt.missingMonths(cfg).joinToString("، ") { strings.monthShort(it) }
        val ownerLabel = apt.displayOwner(isAr).ifBlank { "${strings.colAppt} ${apt.displayNumber}" }

        FintechWhiteCard(
          modifier = Modifier.fillMaxWidth(),
          padding = 16.dp,
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Row(
              modifier = Modifier.weight(1f),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Surface(
                shape = CircleShape,
                color = tokens.surfaceSoft,
                border = BorderStroke(1.dp, tokens.divider),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                modifier = Modifier.size(44.dp),
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                    text = localizeDigits(apt.displayNumber, dig),
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = numericFont ?: MaterialTheme.typography.titleMedium.fontFamily,
                    fontWeight = FontWeight.Medium,
                    color = tokens.ink,
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
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = tokens.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                  if (apt.lastReminderAt > 0L) {
                    val dateFmt = bidiLtr(localizeDigits(SimpleDateFormat("dd/MM", Locale.US).format(Date(apt.lastReminderAt)), dig))
                    RemindedPillBadge(label = "${strings.badgeReminded} $dateFmt")
                  }
                }
                Text(
                  text = "${strings.floorWithPrefix(apt.floor)} · $missingLabels",
                  style = MaterialTheme.typography.bodySmall,
                  color = tokens.body,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Text(
                  text = "! ${formatDh(rem, cur, dig)}",
                  style = MaterialTheme.typography.titleMedium,
                  fontFamily = numericFont ?: MaterialTheme.typography.titleMedium.fontFamily,
                  fontWeight = FontWeight.Medium,
                  color = tokens.negativeDeep,
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

    // 8. Bande surfaceSoft (#F8FAFC) : Recouvrement mensuel (alternance propre après les cartes blanches)
    item {
      AirtableSoftCard(
        modifier = Modifier.fillMaxWidth(),
        padding = 24.dp,
      ) {
        Text(
          text = strings.dashMonthlySection(cfg.year),
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Normal,
          color = tokens.ink,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(
            text = "✓ ${if (isAr) "عادي:" else "Standard :"} ${formatDh(cfg.defaultMonthlyFee, cur, dig)}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = cotisationColorForAmount(cfg.defaultMonthlyFee, cfg.defaultMonthlyFee),
          )
          Text(
            text = "◐ ${strings.dashCustomFeeLegend(cur)}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = cotisationColorForAmount(cfg.defaultMonthlyFee + 50L, cfg.defaultMonthlyFee),
          )
        }
        Spacer(modifier = Modifier.height(16.dp))

        val maxExpected = (data.occupiedCount.coerceAtLeast(1) *
          (cfg.activeMonthColumns.maxOfOrNull { cfg.feeForMonth(it) } ?: 100L)).coerceAtLeast(1L)

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          cfg.activeMonthColumns.forEach { month ->
            val monthFee = cfg.feeForMonth(month)
            val collected = data.apartments.sumOf { it.amountFor(month) }
            val ratio = (collected.toFloat() / maxExpected.toFloat()).coerceIn(0f, 1f)
            val tariffColor = cotisationColorForAmount(monthFee, cfg.defaultMonthlyFee)

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Text(
                    text = strings.monthFull(month),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = tokens.ink,
                  )
                  Surface(
                    shape = RoundedCornerShape(tokens.badgeRadiusDp.dp),
                    color = tokens.cardBg,
                    border = BorderStroke(1.dp, tokens.divider),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                  ) {
                    Text(
                      text = formatDh(monthFee, cur, dig),
                      style = MaterialTheme.typography.labelSmall,
                      fontFamily = numericFont ?: MaterialTheme.typography.labelSmall.fontFamily,
                      fontWeight = FontWeight.Medium,
                      color = tariffColor,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                  }
                }
                Text(
                  text = formatDh(collected, cur, dig),
                  style = MaterialTheme.typography.bodyMedium,
                  fontFamily = numericFont ?: MaterialTheme.typography.bodyMedium.fontFamily,
                  fontWeight = FontWeight.Medium,
                  color = tokens.ink,
                )
              }
              SyndicatProgressBar(
                progress = ratio,
                modifier = Modifier.fillMaxWidth(),
              )
            }
          }
        }
      }
    }

    // 9. Carte blanche bordée : Recouvrement par étage (alternance après surfaceSoft)
    item {
      FintechWhiteCard(
        modifier = Modifier.fillMaxWidth(),
        padding = 24.dp,
      ) {
        Text(
          text = strings.dashFloorsSection,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Normal,
          color = tokens.ink,
        )
        Spacer(modifier = Modifier.height(12.dp))
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
                fontWeight = FontWeight.Medium,
                color = tokens.ink,
              )
              Spacer(modifier = Modifier.height(6.dp))
              SyndicatProgressBar(
                progress = pct / 100f,
                modifier = Modifier.fillMaxWidth(0.85f),
              )
            }
            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = formatDh(totalFloorDh, cur, dig),
                style = MaterialTheme.typography.titleMedium,
                fontFamily = numericFont ?: MaterialTheme.typography.titleMedium.fontFamily,
                fontWeight = FontWeight.Medium,
                color = tokens.positiveDeep,
              )
              Text(
                text = bidiLtr("${localizeDigits(pct.toString(), dig)}%"),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = numericFont ?: MaterialTheme.typography.labelSmall.fontFamily,
                color = tokens.mute,
              )
            }
          }
        }
      }
    }

    // 10. Meta Soft Cloud warranty-card (#F1F4F7, 24dp {rounded.xxl}) with 100dp outlined pill shortcuts
    item {
      AirtableSoftCard(
        modifier = Modifier.fillMaxWidth(),
        padding = 24.dp,
      ) {
        Text(
          text = strings.dashExportShortcutsTitle,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Medium,
          color = tokens.inkDeep,
        )
        Spacer(modifier = Modifier.height(14.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          SageSecondaryButton(
            text = strings.navPoster,
            icon = Icons.Default.PictureAsPdf,
            onClick = onNavigateToPosters,
            modifier = Modifier.weight(1f).testTag("dash_shortcut_posters"),
          )
          SageSecondaryButton(
            text = strings.dashBackupNowBtn,
            icon = Icons.Default.Backup,
            onClick = onTriggerBackupJson,
            modifier = Modifier.weight(1f).testTag("dash_shortcut_backup"),
          )
        }
        Spacer(modifier = Modifier.height(10.dp))
        SageSecondaryButton(
          text = strings.dashLaunchWizardBtn,
          icon = Icons.Default.AutoFixHigh,
          onClick = onOpenWizard,
          modifier = Modifier.fillMaxWidth().testTag("dash_bottom_wizard_btn"),
        )
      }
    }
  }
}
