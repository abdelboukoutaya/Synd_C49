package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.BuildingProfile
import com.example.data.local.SpecialCallRecord
import com.example.data.model.ApartmentRecord
import com.example.data.model.ExpenseRecord
import com.example.data.model.ProjectRecord
import com.example.ui.components.CoinbaseKpiMetricCard
import com.example.ui.components.CollectionRateRingCard
import com.example.ui.components.DashboardQuickActionButton
import com.example.ui.components.FintechWhiteCard
import com.example.ui.components.GlobalSearchDialog
import com.example.ui.components.HeroOverlayCircleButton
import com.example.ui.components.LocalPrivacyMode
import com.example.ui.components.MetaCobaltBuyButton
import com.example.ui.components.MonthlyCollectionsChartCard
import com.example.ui.components.ReadOnlyCouncilBanner
import com.example.ui.components.RemindedPillBadge
import com.example.ui.components.ResidenceHeroBanner
import com.example.ui.components.SageSecondaryButton
import com.example.ui.components.SyndicatFilterChip
import com.example.ui.components.SyndicatProgressBar
import com.example.ui.components.SyndicatTopBrandBar
import com.example.ui.components.TreasuryPortfolioHeroCard
import com.example.ui.components.WhatsAppReminderDialog
import com.example.ui.components.maskSensitiveDh
import com.example.ui.components.maskSensitiveName
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
  isCloudSynced: Boolean = false,
  specialCalls: List<SpecialCallRecord> = emptyList(),
  onNavigateToUnpaidExpenses: () -> Unit = onNavigateToExpenses,
  onNavigateToSpecialCalls: () -> Unit = onNavigateToProjects,
  onQuickPayTap: () -> Unit = onNavigateToApartments,
  isPrivacyMode: Boolean = false,
  onTogglePrivacy: () -> Unit = {},
  onSearchSelectApartment: (ApartmentRecord) -> Unit = { _ -> onNavigateToApartments() },
  onSearchSelectExpense: (ExpenseRecord) -> Unit = { _ -> onNavigateToExpenses() },
  onSearchSelectProject: (ProjectRecord) -> Unit = { _ -> onNavigateToProjects() },
  buildings: List<BuildingProfile> = emptyList(),
  activeBuildingId: String = "default",
  onSelectBuilding: (String) -> Unit = {},
  isReadOnlyMode: Boolean = false,
) {
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val isPrivacy = LocalPrivacyMode.current || isPrivacyMode
  val cfg = data.config
  val cur = cfg.currency
  val dig = cfg.digitsAr
  val isAr = strings.language == AppLanguage.AR
  val numericFont = tokens.numericFontFamily

  val listState = rememberLazyListState()
  val showStickyTreasuryBar by remember {
    derivedStateOf {
      listState.firstVisibleItemIndex >= 2 ||
        (listState.firstVisibleItemIndex == 1 && listState.firstVisibleItemScrollOffset > 140)
    }
  }

  var showCampaignDialog by remember { mutableStateOf(false) }
  var selectedSingleReminderApt by remember { mutableStateOf<ApartmentRecord?>(null) }
  var showGlobalSearch by remember { mutableStateOf(false) }

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

  if (showGlobalSearch) {
    GlobalSearchDialog(
      data = data,
      isAr = isAr,
      onDismiss = { showGlobalSearch = false },
      onSelectApartment = { apt ->
        showGlobalSearch = false
        onSearchSelectApartment(apt)
      },
      onSelectExpense = { exp ->
        showGlobalSearch = false
        onSearchSelectExpense(exp)
      },
      onSelectProject = { proj ->
        showGlobalSearch = false
        onSearchSelectProject(proj)
      },
    )
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

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(tokens.screenBg),
  ) {
    LazyColumn(
      state = listState,
      modifier = Modifier
        .fillMaxSize()
        .testTag("dashboard_screen"),
      contentPadding = PaddingValues(
        start = 16.dp,
        end = 16.dp,
        top = 14.dp,
        bottom = 96.dp,
      ),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // =========================================================================
      // 1. BANNIÈRE PHOTO PERSONNALISÉE DE LA RÉSIDENCE (avec léger parallaxe au scroll)
      // =========================================================================
      item(key = "dash_hero_residence") {
        val cityPart = if (cfg.city.isNotBlank()) " · ${cfg.city}" else ""
        val addrPart = if (cfg.address.isNotBlank()) " · ${cfg.address}" else ""
        val subText = "${cfg.syndicatName.ifBlank { strings.authTitle }}$cityPart$addrPart"
        val yearBadge = "${bidiLtr(localizeDigits(cfg.year.toString(), dig))} · ${strings.navDashboard}"
        val statusLabel = when {
          isBackupOverdue -> strings.syncStatusBackupDuePill
          isCloudSynced -> strings.syncStatusCloudPill
          else -> strings.syncStatusLocalPill
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          if (isReadOnlyMode) {
            ReadOnlyCouncilBanner()
          }
          SyndicatTopBrandBar(
            tag = "dashboard_app_brand_bar",
            statusLabel = statusLabel,
            statusIsWarning = isBackupOverdue,
            statusIsSynced = isCloudSynced && !isBackupOverdue,
            onStatusClick = if (isBackupOverdue) onTriggerBackupJson else null,
            onSearchClick = { showGlobalSearch = true },
            isPrivacyMode = isPrivacy,
            onTogglePrivacy = onTogglePrivacy,
            showBrandHeader = false,
          )

          if (buildings.size > 1) {
            LazyRow(
              modifier = Modifier
                .fillMaxWidth()
                .testTag("dash_building_switcher_row"),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              items(buildings, key = { it.id }) { b ->
                SyndicatFilterChip(
                  selected = b.id == activeBuildingId,
                  onClick = { onSelectBuilding(b.id) },
                  label = b.name,
                  modifier = Modifier.testTag("dash_switch_building_${b.id}"),
                )
              }
            }
          }

          ResidenceHeroBanner(
            residenceName = cfg.residenceName.ifBlank { strings.navDashboard },
            subtitle = subText,
            badgeText = yearBadge,
            onPhotoFeedback = onInfoMessage,
            modifier = Modifier.graphicsLayer {
              if (listState.firstVisibleItemIndex == 0) {
                val offsetPx = listState.firstVisibleItemScrollOffset.toFloat()
                translationY = offsetPx * 0.14f
                alpha = (1f - (offsetPx / 620f)).coerceIn(0.84f, 1f)
              }
            },
            trailingActions = {
              HeroOverlayCircleButton(
                icon = Icons.Default.Tune,
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
      }

      // =========================================================================
      // 2. CARTE PORTEFEUILLE DE TRÉSORERIE EN DIRECT (Coinbase Editorial Dark)
      // =========================================================================
      item(key = "dash_treasury_portfolio") {
        TreasuryPortfolioHeroCard(
          data = data,
          numericFont = numericFont,
          onUnpaidExpensesClick = onNavigateToUnpaidExpenses,
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
      val balanceText = formatDh(data.balanceDh, cur, dig)
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
            valueText = maskSensitiveDh(formatDh(data.totalCaisseDh, cur, dig), isPrivacy),
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
            valueText = maskSensitiveDh(formatDh(data.totalExpensesDh, cur, dig), isPrivacy),
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
            valueText = maskSensitiveDh(balanceText, isPrivacy),
            valueColor = if (data.balanceDh >= 0L) tokens.inkDeep else tokens.negativeDeep,
            subtitle = if (isAr) "المداخيل − المصاريف" else "Recettes − Dépenses",
            numericFont = numericFont,
            onClick = if (data.unpaidExpensesDh > 0L) onNavigateToUnpaidExpenses else onNavigateToExpenses,
            modifier = Modifier
              .weight(1f)
              .testTag("kpi_balance_card"),
          )
        }
      }
    }

    // =========================================================================
    // 6. CARROUSEL ÉCHÉANCES & PRIORITÉS + CENTRE DE RECOUVREMENT WHATSAPP
    // =========================================================================
    item(key = "dash_collection_action_center") {
      Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        UpcomingPrioritiesCarousel(
          data = data,
          specialCalls = specialCalls,
          isBackupOverdue = isBackupOverdue,
          isAr = isAr,
          numericFont = numericFont,
          onLateClick = onNavigateToLateApartments,
          onUnpaidExpensesClick = onNavigateToUnpaidExpenses,
          onSpecialCallsClick = onNavigateToSpecialCalls,
          onBackupClick = onTriggerBackupJson,
        )
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
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                  Box(
                    modifier = Modifier
                      .size(6.dp)
                      .clip(CircleShape)
                      .background(tokens.statusLateText),
                  )
                  Text(
                    text = strings.dashUnpaidSub(data.lateApartments.size),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = tokens.statusLateText,
                  )
                }
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
                text = maskSensitiveDh(formatDh(data.totalRemainingToCollectDh, cur, dig), isPrivacy),
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
                  val rawOwnerLabel = apt.displayOwner(isAr).ifBlank { "${strings.colAppt} ${apt.displayNumber}" }
                  val ownerLabel = maskSensitiveName(rawOwnerLabel, isPrivacy)

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
                            text = maskSensitiveDh(formatDh(rem, cur, dig), isPrivacy),
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
            Icon(
              imageVector = Icons.Default.Construction,
              contentDescription = null,
              tint = tokens.linkColor,
              modifier = Modifier.size(20.dp),
            )
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
                text = maskSensitiveDh(formatDh(totalFloorDh, cur, dig), isPrivacy),
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
            icon = Icons.Default.Tune,
            onClick = onOpenWizard,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("dash_bottom_wizard_btn"),
          )
        }
      }
    }
    }

    // En-tête compact collant de trésorerie lorsque la hero card défile hors écran
    AnimatedVisibility(
      visible = showStickyTreasuryBar,
      enter = fadeIn() + slideInVertically { -it },
      exit = fadeOut() + slideOutVertically { -it },
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = CoinbaseSurfaceDark,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
        tonalElevation = 0.dp,
        shadowElevation = 2.dp,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("dash_sticky_treasury_bar"),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text(
              text = strings.dashCashEyebrow,
              style = MaterialTheme.typography.labelMedium,
              color = CoinbaseOnDarkSoft,
            )
            Text(
              text = maskSensitiveDh(formatDh(data.cashNowDh, cur, dig), isPrivacy),
              style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = numericFont,
                fontFeatureSettings = "tnum",
              ),
              fontWeight = FontWeight.SemiBold,
              color = Color.White,
            )
          }
          Surface(
            shape = RoundedCornerShape(100.dp),
            color = tokens.primary,
            modifier = Modifier
              .clip(RoundedCornerShape(100.dp))
              .clickable(onClick = onQuickPayTap)
              .testTag("dash_sticky_quick_pay_btn"),
          ) {
            Text(
              text = strings.dashStickyTreasuryBtn,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun UpcomingPrioritiesCarousel(
  data: SyndicatDashboardData,
  specialCalls: List<SpecialCallRecord>,
  isBackupOverdue: Boolean,
  isAr: Boolean,
  numericFont: FontFamily,
  onLateClick: () -> Unit,
  onUnpaidExpensesClick: () -> Unit,
  onSpecialCallsClick: () -> Unit,
  onBackupClick: () -> Unit,
) {
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val isPrivacy = LocalPrivacyMode.current
  val cfg = data.config
  val cur = cfg.currency
  val dig = cfg.digitsAr

  val activeCalls = remember(specialCalls, data.apartments) {
    specialCalls.filter { it.paidApartmentsCount(data.apartments) < it.eligibleApartments(data.apartments).size }
  }

  val hasAny = data.lateApartments.isNotEmpty() ||
    data.unpaidExpensesDh > 0L ||
    activeCalls.isNotEmpty() ||
    isBackupOverdue

  if (!hasAny) return

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("dash_upcoming_carousel"),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Text(
      text = strings.dashUpcomingTitle,
      style = MaterialTheme.typography.titleSmall,
      fontWeight = FontWeight.SemiBold,
      color = tokens.inkDeep,
      modifier = Modifier.padding(horizontal = 2.dp),
    )

    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      if (data.lateApartments.isNotEmpty()) {
        item(key = "prio_late") {
          PriorityMiniCard(
            icon = Icons.Default.Schedule,
            tint = tokens.statusLateText,
            tintBg = if (tokens.isDark) CoinbaseDarkErrorContainer else CoinbaseErrorTintLight,
            title = strings.dashUnpaidSub(data.lateApartments.size),
            value = maskSensitiveDh(formatDh(data.totalRemainingToCollectDh, cur, dig), isPrivacy),
            numericFont = numericFont,
            onClick = onLateClick,
            tag = "dash_priority_late_card",
          )
        }
      }
      if (data.unpaidExpensesDh > 0L) {
        item(key = "prio_exp") {
          PriorityMiniCard(
            icon = Icons.AutoMirrored.Filled.ReceiptLong,
            tint = tokens.warningContent,
            tintBg = if (tokens.isDark) Color(0xFF2B2209) else CoinbaseWarningTintLight,
            title = "${strings.dashUnpaidExpensesCardTitle} (${formatNumber(data.unpaidExpensesCount.toLong(), dig)})",
            value = maskSensitiveDh(formatDh(data.unpaidExpensesDh, cur, dig), isPrivacy),
            numericFont = numericFont,
            onClick = onUnpaidExpensesClick,
            tag = "dash_priority_expenses_card",
          )
        }
      }
      activeCalls.forEach { call ->
        item(key = "prio_call_${call.id}") {
          val eligible = call.eligibleApartments(data.apartments).size
          val paid = call.paidApartmentsCount(data.apartments)
          PriorityMiniCard(
            icon = Icons.Default.Campaign,
            tint = tokens.linkColor,
            tintBg = if (tokens.isDark) CoinbaseDarkBlueContainer else CoinbasePrimaryTintLight,
            title = call.displayTitle(isAr).ifBlank { strings.dashSpecialCallsCardTitle },
            value = bidiLtr("${localizeDigits(paid.toString(), dig)}/${localizeDigits(eligible.toString(), dig)}"),
            numericFont = numericFont,
            onClick = onSpecialCallsClick,
            tag = "dash_priority_call_${call.id}",
          )
        }
      }
      if (isBackupOverdue) {
        item(key = "prio_backup") {
          PriorityMiniCard(
            icon = Icons.Default.Backup,
            tint = tokens.warningContent,
            tintBg = if (tokens.isDark) Color(0xFF2B2209) else CoinbaseWarningTintLight,
            title = strings.syncStatusBackupDuePill,
            value = strings.dashBackupNowBtn,
            numericFont = numericFont,
            onClick = onBackupClick,
            tag = "dash_priority_backup_card",
          )
        }
      }
    }
  }
}

@Composable
private fun PriorityMiniCard(
  icon: ImageVector,
  tint: Color,
  tintBg: Color,
  title: String,
  value: String,
  numericFont: FontFamily,
  onClick: () -> Unit,
  tag: String,
) {
  val tokens = LocalSyndicatTokens.current
  val shape = RoundedCornerShape(tokens.contentCardRadiusDp.dp)
  Surface(
    shape = shape,
    color = tokens.cardBg,
    border = BorderStroke(tokens.borderStrokeDp.dp, tokens.cardBorder),
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
    modifier = Modifier
      .width(198.dp)
      .clip(shape)
      .clickable(onClick = onClick)
      .testTag(tag),
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = tintBg,
        modifier = Modifier.size(34.dp),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = icon,
            contentDescription = title,
            tint = tint,
            modifier = Modifier.size(18.dp),
          )
        }
      }
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
          text = title,
          style = MaterialTheme.typography.labelSmall,
          color = tokens.body,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = value,
          style = MaterialTheme.typography.titleSmall.copy(
            fontFamily = numericFont,
            fontFeatureSettings = "tnum",
          ),
          fontWeight = FontWeight.SemiBold,
          color = tint,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}


