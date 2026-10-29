package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ApartmentRecord
import com.example.data.model.MonthColumn
import com.example.data.model.SyndicatConfig
import com.example.data.model.UnitStatus
import com.example.ui.components.FintechPalePositiveCard
import com.example.ui.components.FintechTextField
import com.example.ui.components.FintechWhiteCard
import com.example.ui.components.MetaCobaltBuyButton
import com.example.ui.components.MetaSearchPill
import com.example.ui.components.MetaSegmentedPillRow
import com.example.ui.components.RemindedPillBadge
import com.example.ui.components.ResidenceHeroBanner
import com.example.ui.components.RoundIconCircleButton
import com.example.ui.components.SageSecondaryButton
import com.example.ui.components.StatusPillBadge
import com.example.ui.components.SyndicatDialog
import com.example.ui.components.SyndicatFilterChip
import com.example.ui.components.WhatsAppReminderDialog
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalStrings
import com.example.ui.i18n.bidiLtr
import com.example.ui.i18n.formatNumber
import com.example.ui.i18n.localizeDigits
import com.example.ui.theme.CoinbaseDarkErrorContainer
import com.example.ui.theme.CoinbaseErrorTintLight
import com.example.ui.theme.CoinbaseSuccessTintLight
import com.example.ui.theme.CoinbaseWarningTintLight
import com.example.ui.theme.LocalSyndicatTokens
import com.example.ui.theme.cotisationColorForAmount
import com.example.ui.viewmodel.StatusFilter
import com.example.ui.viewmodel.SyndicatDashboardData
import com.example.ui.viewmodel.formatDh
import com.example.util.PdfPosterExporter
import com.example.util.WhatsAppHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun ApartmentsScreen(
  data: SyndicatDashboardData,
  searchQuery: String,
  selectedFloor: String?,
  statusFilter: StatusFilter,
  onSearchQueryChange: (String) -> Unit,
  onSelectFloor: (String?) -> Unit,
  onSelectStatusFilter: (StatusFilter) -> Unit,
  onSaveApartment: (ApartmentRecord, ApartmentRecord) -> Unit,
  onMarkReminded: (ApartmentRecord, String?, String?) -> Unit,
  onRequestReceiptNo: suspend (ApartmentRecord) -> Long,
  onExportError: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val cfg = data.config
  val cur = cfg.currency
  val dig = cfg.digitsAr
  val isAr = strings.language == AppLanguage.AR
  val monoOrDefault = tokens.monoFontFamily

  var isGridView by remember { mutableStateOf(false) }
  var editingApartment by remember { mutableStateOf<ApartmentRecord?>(null) }
  var remindingApartment by remember { mutableStateOf<ApartmentRecord?>(null) }
  var showFilteredBatchRemind by remember { mutableStateOf(false) }
  var quickCellTarget by remember { mutableStateOf<Pair<ApartmentRecord, MonthColumn>?>(null) }

  val filteredApartments = remember(data.apartments, cfg, searchQuery, selectedFloor, statusFilter) {
    data.apartments.filter { apt ->
      val floorOk = selectedFloor == null || apt.floor == selectedFloor
      val q = searchQuery.trim().lowercase()
      val queryOk = q.isEmpty() ||
        apt.number.toString().contains(q) ||
        apt.displayNumber.lowercase().contains(q) ||
        apt.ownerNameAr.lowercase().contains(q) ||
        apt.ownerName.lowercase().contains(q) ||
        apt.phone.contains(q)
      val statusOk = when (statusFilter) {
        StatusFilter.ALL -> true
        StatusFilter.UP_TO_DATE -> apt.isUpToDate(cfg)
        StatusFilter.LATE -> !apt.isVacantForConfig(cfg) && !apt.isUpToDate(cfg)
        StatusFilter.VACANT -> apt.isVacantForConfig(cfg)
      }
      floorOk && queryOk && statusOk
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(tokens.screenBg)
      .testTag("apartments_screen"),
  ) {
    // Top Header + Search Bar + Cards/Grid View Switcher
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = strings.navApartments,
          style = MaterialTheme.typography.headlineLarge,
          fontWeight = FontWeight.Medium,
          color = tokens.inkDeep,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(8.dp))

        // Meta Segmented Pill Switcher (Cards vs 12-Month Grid — {rounded.full} 100dp pills)
        MetaSegmentedPillRow(
          firstLabel = strings.apptViewCards,
          secondLabel = strings.apptViewGrid,
          isFirstSelected = !isGridView,
          onSelectFirst = { isGridView = false },
          onSelectSecond = { isGridView = true },
          firstTag = "toggle_cards_view",
          secondTag = "toggle_grid_view",
          firstIcon = Icons.Default.ViewAgenda,
          secondIcon = Icons.Default.GridOn,
        )
      }

      // Meta search-pill ({colors.surface-soft} #F1F4F7, {rounded.full} 100dp, 2px #1876F2 focus border)
      MetaSearchPill(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        placeholder = strings.apptSearchHint,
        clearDescription = strings.apptClearSearch,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("apartment_search_input"),
      )

      // Status & Floor Filter Pills (48dp touch targets)
      val cutoffShort = strings.monthShort(data.cutoffMonthColumn)
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        items(StatusFilter.entries) { filter ->
          SyndicatFilterChip(
            selected = statusFilter == filter,
            onClick = { onSelectStatusFilter(filter) },
            label = strings.statusFilterLabel(filter, cutoffShort),
            modifier = Modifier.testTag("status_filter_${filter.name.lowercase()}"),
          )
        }
        item {
          SyndicatFilterChip(
            selected = selectedFloor == null,
            onClick = { onSelectFloor(null) },
            label = strings.apptFilterAll(data.apartments.size),
          )
        }
        items(data.orderedFloors) { floor ->
          val selected = selectedFloor == floor
          SyndicatFilterChip(
            selected = selected,
            onClick = { onSelectFloor(if (selected) null else floor) },
            label = strings.floorWithPrefix(floor),
          )
        }
      }

      val filteredLateTargets = remember(filteredApartments, cfg) {
        filteredApartments.filter { !it.isVacantForConfig(cfg) && !it.isUpToDate(cfg) }
      }
      if (statusFilter == StatusFilter.LATE && filteredLateTargets.isNotEmpty()) {
        SageSecondaryButton(
          text = "${strings.dashSendRemindersBtn} (${formatNumber(filteredLateTargets.size.toLong(), dig)})",
          icon = Icons.AutoMirrored.Filled.Send,
          compact = true,
          onClick = { showFilteredBatchRemind = true },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("apartments_batch_remind_btn"),
        )
      }
    }

    val singleRemindTarget = remindingApartment
    if (singleRemindTarget != null) {
      WhatsAppReminderDialog(
        apartmentsQueue = listOf(singleRemindTarget),
        config = cfg,
        initialApartment = singleRemindTarget,
        onDismiss = { remindingApartment = null },
        onReminderSent = { apt, updatedPhone, updatedTemplate ->
          onMarkReminded(apt, updatedPhone, updatedTemplate)
        },
        onInfoMessage = onExportError,
      )
    } else if (showFilteredBatchRemind) {
      val batchTargets = filteredApartments.filter { !it.isVacantForConfig(cfg) && !it.isUpToDate(cfg) }
      if (batchTargets.isNotEmpty()) {
        WhatsAppReminderDialog(
          apartmentsQueue = batchTargets,
          config = cfg,
          onDismiss = { showFilteredBatchRemind = false },
          onReminderSent = { apt, updatedPhone, updatedTemplate ->
            onMarkReminded(apt, updatedPhone, updatedTemplate)
          },
          onInfoMessage = onExportError,
        )
      }
    }

    if (!isGridView) {
      // =========================================================================
      // CARDS VIEW : En-tête résidence + 1 Carte par Appartement avec grille mensuelle
      // intégrée sur toute la largeur (ZÉRO glissement horizontal gauche-droite)
      // =========================================================================
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .testTag("apartments_cards_list"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        item(key = "cotisations_residence_banner") {
          ResidenceHeroBanner(
            residenceName = cfg.residenceName.ifBlank { strings.navApartments },
            subtitle = "${strings.navApartments} · ${bidiLtr(localizeDigits(cfg.year.toString(), dig))}",
            badgeText = strings.residenceCustomizePhotoShort,
            compact = true,
            onPhotoFeedback = onExportError,
          )
        }

        data.orderedFloors.forEach { floor ->
          val floorApts = filteredApartments.filter { it.floor == floor }
          if (floorApts.isNotEmpty()) {
            item(key = "floor_hdr_$floor") {
              val subtotal = floorApts.sumOf { it.totalForConfig(cfg) }
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(top = 6.dp, start = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = strings.floorWithPrefix(floor),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = tokens.ink,
                )
                Text(
                  text = "${strings.apptSubtotal} ${formatDh(subtotal, cur, dig)}",
                  style = MaterialTheme.typography.labelLarge,
                  fontFamily = monoOrDefault,
                  fontWeight = FontWeight.Medium,
                  color = tokens.positiveDeep,
                )
              }
            }

            items(
              items = floorApts,
              key = { it.id.ifBlank { "apt_${it.number}" } },
            ) { apt ->
              ApartmentFintechCard(
                apartment = apt,
                config = cfg,
                isAr = isAr,
                onEditClick = { editingApartment = apt },
                onQuickMonthClick = { month -> quickCellTarget = apt to month },
                onRemindWhatsApp = { remindingApartment = apt },
                onShareReceipt = {
                  scope.launch {
                    val receiptNo = onRequestReceiptNo(apt)
                    PdfPosterExporter.sharePaymentReceiptImage(
                      context = context,
                      apartment = apt,
                      data = data,
                      receiptNo = receiptNo,
                      onError = onExportError,
                    )
                  }
                },
              )
            }
          }
        }
      }
    } else {
      // =========================================================================
      // COMPACT TABLE / GRID VIEW SANS SLIDER GAUCHE-DROITE :
      // Tous les mois actifs s'affichent en grille multi-colonnes équilibrée (4 mois/ligne)
      // sur 100 % de la largeur de l'écran du téléphone, sans aucun défilement horizontal.
      // =========================================================================
      val activeMonths = cfg.activeMonthColumns
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 12.dp)
          .testTag("apartments_grid_list"),
        contentPadding = PaddingValues(top = 4.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        item(key = "grid_tariffs_legend") {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = tokens.surfaceSoft,
            border = BorderStroke(1.dp, tokens.divider),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text(
              text = strings.apptMonthsSectionLabel,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Medium,
              color = tokens.body,
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            )
          }
        }

        data.orderedFloors.forEach { floor ->
          val floorApts = filteredApartments.filter { it.floor == floor }
          if (floorApts.isNotEmpty()) {
            item(key = "grid_floor_$floor") {
              val subtotal = floorApts.sumOf { it.totalForConfig(cfg) }
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(top = 4.dp, start = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = strings.floorWithPrefix(floor),
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.SemiBold,
                  color = tokens.inkDeep,
                )
                Text(
                  text = "${strings.apptSubtotal} ${formatDh(subtotal, cur, dig)}",
                  style = MaterialTheme.typography.labelMedium,
                  fontFamily = monoOrDefault,
                  fontWeight = FontWeight.SemiBold,
                  color = tokens.positiveDeep,
                )
              }
            }

            items(
              items = floorApts,
              key = { "grid_row_${it.id.ifBlank { it.number.toString() }}" },
            ) { apt ->
              val ownerLabel = apt.displayOwner(isAr).ifBlank { "${strings.colAppt} ${apt.displayNumber}" }
              val isVacant = apt.isVacantForConfig(cfg)

              Surface(
                shape = RoundedCornerShape(tokens.contentCardRadiusDp.dp),
                color = tokens.cardBg,
                border = BorderStroke(1.dp, tokens.cardBorder),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                modifier = Modifier.fillMaxWidth(),
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                  verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                  // Ligne d'en-tête compacte : N° + Propriétaire + Statut + Total encaissé
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(10.dp))
                      .clickable { editingApartment = apt }
                      .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                  ) {
                    Row(
                      modifier = Modifier.weight(1f),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                      Surface(
                        shape = CircleShape,
                        color = tokens.surfaceStrong,
                        border = BorderStroke(1.dp, tokens.divider),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                        modifier = Modifier.size(30.dp),
                      ) {
                        Box(contentAlignment = Alignment.Center) {
                          Text(
                            text = localizeDigits(apt.displayNumber, dig),
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = monoOrDefault,
                            fontWeight = FontWeight.SemiBold,
                            color = tokens.inkDeep,
                          )
                        }
                      }
                      Text(
                        text = ownerLabel,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isVacant) tokens.mute else tokens.inkDeep,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                      )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                      StatusPillBadge(status = apt.badgeStatus(cfg))
                      Text(
                        text = formatDh(apt.totalForConfig(cfg), cur, dig),
                        style = MaterialTheme.typography.labelLarge,
                        fontFamily = monoOrDefault,
                        fontWeight = FontWeight.SemiBold,
                        color = tokens.positiveDeep,
                      )
                    }
                  }

                  // Grille mensuelle sans slider gauche-droite (4 colonnes par ligne sur 100 % de la largeur)
                  ApartmentMonthsNoScrollGrid(
                    apartment = apt,
                    activeMonths = activeMonths,
                    config = cfg,
                    cellTagPrefix = "grid_cell_${apt.number}",
                    onMonthClick = { m -> quickCellTarget = apt to m },
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  // Quick Single-Cell Payment Dialog
  quickCellTarget?.let { (apt, month) ->
    QuickMonthCellPaymentDialog(
      apartment = apt,
      month = month,
      config = cfg,
      onDismiss = { quickCellTarget = null },
      onSaveAmount = { newAmt ->
        val updated = apt.withMonthAmount(month, newAmt).let {
          if (newAmt > 0L && it.status == "empty") it.copy(status = "occupied") else it
        }
        onSaveApartment(updated, apt)
        quickCellTarget = null
      },
    )
  }

  // Comprehensive Apartment Edit & Multi-Month Payment Modal
  editingApartment?.let { apt ->
    ApartmentPaymentAndOwnerModal(
      apartment = apt,
      config = cfg,
      onDismiss = { editingApartment = null },
      onSave = { updated ->
        onSaveApartment(updated, apt)
        editingApartment = null
      },
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ApartmentFintechCard(
  apartment: ApartmentRecord,
  config: SyndicatConfig,
  isAr: Boolean,
  onEditClick: () -> Unit,
  onQuickMonthClick: (MonthColumn) -> Unit,
  onRemindWhatsApp: () -> Unit,
  onShareReceipt: () -> Unit,
) {
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val cur = config.currency
  val dig = config.digitsAr
  val monoOrDefault = tokens.monoFontFamily

  val badgeStatus = apartment.badgeStatus(config)
  val totalPaid = apartment.totalForConfig(config)
  val remaining = apartment.remainingDh(config)
  val advance = apartment.advanceDh(config)
  val primaryOwner = apartment.displayOwner(isAr)
  val secondaryOwner = apartment.secondaryOwner(isAr)

  FintechWhiteCard(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("apartment_card_${apartment.number}"),
    padding = 16.dp,
    onClick = onEditClick,
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.Top,
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
          modifier = Modifier.size(44.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text(
              text = localizeDigits(apartment.displayNumber, dig),
              style = MaterialTheme.typography.titleMedium,
              fontFamily = monoOrDefault ?: MaterialTheme.typography.titleMedium.fontFamily,
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
              text = primaryOwner.ifBlank { "${strings.colAppt} ${apartment.displayNumber}" },
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Medium,
              color = if (primaryOwner.isBlank()) tokens.mute else tokens.ink,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            if (apartment.isTenant) {
              Surface(
                shape = RoundedCornerShape(100.dp),
                color = tokens.surfaceSoft,
                border = BorderStroke(1.dp, tokens.divider),
              ) {
                Text(
                  text = strings.badgeTenant,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = tokens.body,
                  modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                )
              }
            }
          }

          val subParts = buildList {
            if (secondaryOwner.isNotBlank()) add(secondaryOwner)
            if (apartment.phone.isNotBlank()) {
              add(WhatsAppHelper.formatDisplayPhone(apartment.phone))
            }
          }
          if (subParts.isNotEmpty()) {
            Text(
              text = subParts.joinToString(" · "),
              style = MaterialTheme.typography.bodySmall,
              color = tokens.body,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }

      StatusPillBadge(status = badgeStatus)
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Paid vs Remaining / Advance Summary Row (Soft Cloud 12dp container)
    Surface(
      shape = RoundedCornerShape(12.dp),
      color = tokens.surfaceSoft,
      tonalElevation = 0.dp,
      shadowElevation = 0.dp,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column {
          Text(
            text = strings.colPaid,
            style = MaterialTheme.typography.labelSmall,
            color = tokens.mute,
          )
          Text(
            text = formatDh(totalPaid, cur, dig),
            style = MaterialTheme.typography.titleMedium,
            fontFamily = monoOrDefault ?: MaterialTheme.typography.titleMedium.fontFamily,
            fontWeight = FontWeight.Medium,
            color = tokens.positiveDeep,
          )
        }

        if (!apartment.isVacantForConfig(config)) {
          Column(horizontalAlignment = Alignment.End) {
            if (remaining > 0L) {
              Text(
                text = strings.colRemaining,
                style = MaterialTheme.typography.labelSmall,
                color = tokens.mute,
              )
              Text(
                text = "! ${formatDh(remaining, cur, dig)}",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = monoOrDefault ?: MaterialTheme.typography.titleMedium.fontFamily,
                fontWeight = FontWeight.Medium,
                color = tokens.negativeDeep,
              )
            } else if (advance > 0L) {
              Text(
                text = strings.apptAdvancePrefix,
                style = MaterialTheme.typography.labelSmall,
                color = tokens.mute,
              )
              Text(
                text = formatDh(advance, cur, dig, explicitPlus = true),
                style = MaterialTheme.typography.titleMedium,
                fontFamily = monoOrDefault ?: MaterialTheme.typography.titleMedium.fontFamily,
                fontWeight = FontWeight.Medium,
                color = tokens.positiveDeep,
              )
            } else {
              Text(
                text = "✓ ${strings.badgePaid}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = tokens.positiveDeep,
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Grille directe de tous les mois actifs (4 colonnes par ligne, zéro slider gauche-droite)
    Text(
      text = strings.apptMonthsSectionLabel,
      style = MaterialTheme.typography.labelSmall,
      color = tokens.mute,
    )
    Spacer(modifier = Modifier.height(6.dp))
    ApartmentMonthsNoScrollGrid(
      apartment = apartment,
      activeMonths = config.activeMonthColumns,
      config = config,
      cellTagPrefix = "card_month_${apartment.number}",
      onMonthClick = onQuickMonthClick,
    )

    if (apartment.lastReminderAt > 0L) {
      Spacer(modifier = Modifier.height(10.dp))
      val dt = bidiLtr(localizeDigits(SimpleDateFormat("dd/MM", Locale.US).format(Date(apartment.lastReminderAt)), dig))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
      ) {
        RemindedPillBadge(label = "${strings.badgeReminded} $dt")
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Boutons d'action de chaque carte
    FlowRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      if (remaining > 0L && !apartment.isVacantForConfig(config)) {
        SageSecondaryButton(
          text = strings.dashRemindBtn,
          icon = Icons.AutoMirrored.Filled.Send,
          compact = true,
          onClick = onRemindWhatsApp,
          modifier = Modifier.testTag("card_remind_apt_${apartment.number}"),
        )
      }
      SageSecondaryButton(
        text = strings.apptRecordPayment,
        icon = Icons.Default.Payments,
        compact = true,
        onClick = onEditClick,
        modifier = Modifier.testTag("card_pay_apt_${apartment.number}"),
      )

      if (totalPaid > 0L) {
        SageSecondaryButton(
          text = strings.apptReceiptBtn,
          icon = Icons.Default.Receipt,
          compact = true,
          onClick = onShareReceipt,
          modifier = Modifier.testTag("card_receipt_apt_${apartment.number}"),
        )
      }
    }
  }
}

/**
 * Grille mensuelle multi-lignes (4 mois par ligne) occupant 100 % de la largeur disponible
 * sans aucun glissement horizontal gauche-droite (`horizontalScroll` proscrit).
 * Chaque tuile respecte une hauteur minimale accessible de 48dp et permet la saisie directe en 1 clic.
 */
@Composable
private fun ApartmentMonthsNoScrollGrid(
  apartment: ApartmentRecord,
  activeMonths: List<MonthColumn>,
  config: SyndicatConfig,
  cellTagPrefix: String,
  onMonthClick: (MonthColumn) -> Unit,
) {
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val dig = config.digitsAr
  val isVacant = apartment.isVacantForConfig(config)
  val columnsPerRow = 4
  val rows = remember(activeMonths) { activeMonths.chunked(columnsPerRow) }

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    rows.forEach { rowMonths ->
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        rowMonths.forEach { m ->
          val paid = apartment.amountFor(m)
          val expected = apartment.expectedFeeForMonth(m, config)
          val isPartial = paid > 0L && expected > 0L && paid < expected
          val isLateMonth = !isVacant && m.monthNumber <= config.cutoffMonth.toInt() && paid < expected
          val isPaidFull = paid > 0L && !isPartial

          val tileBg = when {
            isPaidFull -> if (tokens.isDark) Color(0xFF0A2E1F) else CoinbaseSuccessTintLight
            isPartial -> if (tokens.isDark) Color(0xFF332707) else CoinbaseWarningTintLight
            isLateMonth -> if (tokens.isDark) CoinbaseDarkErrorContainer else CoinbaseErrorTintLight
            else -> tokens.surfaceSoft
          }
          val tileBorder = when {
            isPaidFull -> tokens.positive.copy(alpha = 0.36f)
            isPartial -> tokens.warning.copy(alpha = 0.48f)
            isLateMonth -> tokens.statusLateText.copy(alpha = 0.35f)
            else -> tokens.divider
          }
          val valueColor = when {
            isPaidFull -> cotisationColorForAmount(paid, config.defaultMonthlyFee)
            isPartial -> tokens.warningContent
            isLateMonth -> tokens.statusLateText
            else -> tokens.mute
          }
          val valueLabel = when {
            isPaidFull -> "✓ ${formatNumber(paid, dig)}"
            isPartial -> "◐ ${formatNumber(paid, dig)}"
            isLateMonth -> "! 0"
            isVacant -> "—"
            else -> formatNumber(expected, dig)
          }

          Surface(
            shape = RoundedCornerShape(10.dp),
            color = tileBg,
            border = BorderStroke(1.dp, tileBorder),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            modifier = Modifier
              .weight(1f)
              .heightIn(min = 48.dp)
              .clip(RoundedCornerShape(10.dp))
              .clickable { onMonthClick(m) }
              .testTag("${cellTagPrefix}_${m.key}"),
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center,
            ) {
              Text(
                text = strings.monthShort(m),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isLateMonth) tokens.statusLateText else tokens.inkDeep,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = valueLabel,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = tokens.numericFontFamily,
                fontWeight = if (paid > 0L || isLateMonth) FontWeight.SemiBold else FontWeight.Normal,
                color = valueColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }

        // Complète la dernière ligne avec des espaces invisibles de même poids pour aligner les colonnes
        val emptySlots = columnsPerRow - rowMonths.size
        repeat(emptySlots) {
          Spacer(modifier = Modifier.weight(1f))
        }
      }
    }
  }
}

@Composable
private fun QuickMonthCellPaymentDialog(
  apartment: ApartmentRecord,
  month: MonthColumn,
  config: SyndicatConfig,
  onDismiss: () -> Unit,
  onSaveAmount: (Long) -> Unit,
) {
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val expectedFee = apartment.expectedFeeForMonth(month, config)
    .takeIf { it > 0L } ?: config.feeForMonth(month)
  var amountText by remember(apartment, month) {
    val curAmt = apartment.amountFor(month)
    mutableStateOf(if (curAmt == 0L) "" else curAmt.toString())
  }

  SyndicatDialog(
    onDismissRequest = onDismiss,
    title = {
      Column {
        Text(
          text = "${strings.colAppt} ${apartment.displayNumber} · ${strings.monthFull(month)}",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Normal,
          color = tokens.ink,
        )
        val owner = apartment.displayOwner(strings.language == AppLanguage.AR)
        if (owner.isNotBlank()) {
          Text(
            text = owner,
            style = MaterialTheme.typography.bodySmall,
            color = tokens.body,
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        FintechTextField(
          value = amountText,
          onValueChange = { next -> amountText = next.filter { it.isDigit() }.take(6) },
          label = "${strings.colPaid} (${config.currency})",
          placeholder = expectedFee.toString(),
          keyboardType = KeyboardType.Number,
          modifier = Modifier.fillMaxWidth().testTag("quick_cell_amount_input"),
        )

        MetaCobaltBuyButton(
          text = "${strings.apptPayFullBtn} (${formatDh(expectedFee, config.currency, config.digitsAr)})",
          icon = Icons.Default.DoneAll,
          onClick = { onSaveAmount(expectedFee) },
          modifier = Modifier.fillMaxWidth().testTag("quick_cell_pay_full_btn"),
        )

        if (apartment.amountFor(month) > 0L) {
          SageSecondaryButton(
            text = strings.apptClearMonthZeroBtn,
            icon = Icons.Default.Clear,
            compact = true,
            onClick = { onSaveAmount(0L) },
            modifier = Modifier.fillMaxWidth().testTag("quick_cell_clear_zero_btn"),
          )
        }
      }
    },
    confirmButton = {
      SageSecondaryButton(
        text = strings.apptSave,
        compact = true,
        onClick = { onSaveAmount(amountText.toLongOrNull() ?: 0L) },
        modifier = Modifier.testTag("quick_cell_save_btn"),
      )
    },
    dismissButton = {
      SageSecondaryButton(
        text = strings.apptCancel,
        compact = true,
        onClick = onDismiss,
      )
    },
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ApartmentPaymentAndOwnerModal(
  apartment: ApartmentRecord,
  config: SyndicatConfig,
  onDismiss: () -> Unit,
  onSave: (ApartmentRecord) -> Unit,
) {
  val context = LocalContext.current
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val cur = config.currency
  val dig = config.digitsAr

  var ownerNameAr by remember(apartment.id) { mutableStateOf(apartment.ownerNameAr) }
  var ownerNameLatin by remember(apartment.id) { mutableStateOf(apartment.ownerName) }
  var phone by remember(apartment.id) { mutableStateOf(apartment.phone) }
  var unitStatus by remember(apartment.id) { mutableStateOf(UnitStatus.fromCode(apartment.status)) }
  var isTenant by remember(apartment.id) { mutableStateOf(apartment.isTenant) }
  var customFeeText by remember(apartment.id) {
    mutableStateOf(if (apartment.customMonthlyFee >= 0L) apartment.customMonthlyFee.toString() else "")
  }
  var lumpSumText by remember { mutableStateOf("") }

  val monthInputs = remember(apartment.id) {
    mutableStateMapOf<MonthColumn, String>().apply {
      MonthColumn.entries.forEach { col ->
        val v = apartment.amountFor(col)
        put(col, if (v == 0L) "" else v.toString())
      }
    }
  }

  val contactPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
  ) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
      val uri = result.data?.data ?: return@rememberLauncherForActivityResult
      try {
        context.contentResolver.query(
          uri,
          arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
          ),
          null,
          null,
          null,
        )?.use { cursor ->
          if (cursor.moveToFirst()) {
            val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val pickedName = if (nameIdx >= 0) cursor.getString(nameIdx).orEmpty().trim() else ""
            val pickedPhone = if (numIdx >= 0) cursor.getString(numIdx).orEmpty().trim() else ""
            if (pickedPhone.isNotBlank()) phone = pickedPhone
            if (pickedName.isNotBlank()) {
              val hasArabic = pickedName.any { it.code in 0x0600..0x06FF }
              if (hasArabic && ownerNameAr.isBlank()) {
                ownerNameAr = pickedName
              } else if (!hasArabic && ownerNameLatin.isBlank()) {
                ownerNameLatin = pickedName
              } else if (ownerNameAr.isBlank()) {
                ownerNameAr = pickedName
              }
            }
          }
        }
      } catch (_: Exception) {
      }
    }
  }

  fun buildCurrentDraft(): ApartmentRecord {
    var draft = apartment.copy(
      ownerNameAr = ownerNameAr.trim(),
      ownerName = ownerNameLatin.trim(),
      phone = phone.trim(),
      status = unitStatus.code,
      isTenant = isTenant,
      customMonthlyFee = customFeeText.toLongOrNull() ?: -1L,
    )
    MonthColumn.entries.forEach { col ->
      draft = draft.withMonthAmount(col, monthInputs[col]?.toLongOrNull() ?: 0L)
    }
    return draft
  }

  val liveTotalDh = config.activeMonthColumns.sumOf { col ->
    monthInputs[col]?.toLongOrNull() ?: 0L
  }

  SyndicatDialog(
    onDismissRequest = onDismiss,
    title = {
      Column {
        Text(
          text = strings.apptDialogTitle(apartment.displayNumber, strings.floorWithPrefix(apartment.floor)),
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Normal,
          color = tokens.ink,
        )
        Text(
          text = "${strings.apptCalculatedTotal} ${formatDh(liveTotalDh, cur, dig)}",
          style = MaterialTheme.typography.labelLarge,
          fontFamily = tokens.monoFontFamily ?: MaterialTheme.typography.labelLarge.fontFamily,
          fontWeight = FontWeight.Medium,
          color = tokens.positiveDeep,
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        // Multi-Month Lump Sum Auto-Distribution Card (Soft Cloud 24dp warranty-card)
        FintechPalePositiveCard(
          modifier = Modifier.fillMaxWidth(),
          padding = 16.dp,
        ) {
          Text(
            text = strings.apptLumpSumTitle,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = tokens.inkDeep,
          )
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            FintechTextField(
              value = lumpSumText,
              onValueChange = { lumpSumText = it.filter { c -> c.isDigit() }.take(6) },
              label = strings.apptLumpSumHint(cur),
              placeholder = "200",
              keyboardType = KeyboardType.Number,
              labelColor = tokens.inkDeep,
              modifier = Modifier.weight(1f).testTag("lump_sum_input"),
            )
            val distributeAction = {
              val addAmt = lumpSumText.toLongOrNull() ?: 0L
              if (addAmt > 0L) {
                val distributed = buildCurrentDraft().applyLumpSumPayment(addAmt, config)
                unitStatus = UnitStatus.fromCode(distributed.status)
                MonthColumn.entries.forEach { col ->
                  val v = distributed.amountFor(col)
                  monthInputs[col] = if (v == 0L) "" else v.toString()
                }
                lumpSumText = ""
              }
            }
            SageSecondaryButton(
              text = strings.apptDistributeBtn,
              compact = true,
              enabled = (lumpSumText.toLongOrNull() ?: 0L) > 0L,
              onClick = distributeAction,
              modifier = Modifier.testTag("distribute_lump_sum_btn"),
            )
          }
        }

        // Individual Active Month Inputs + Settle All Button
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text = strings.apptMonthlyFeesHeader(cur),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = tokens.ink,
            modifier = Modifier.align(Alignment.CenterVertically),
          )
          SageSecondaryButton(
            text = strings.apptSettleAllActive,
            icon = Icons.Default.DoneAll,
            compact = true,
            onClick = {
              unitStatus = UnitStatus.OCCUPIED
              val customFee = customFeeText.toLongOrNull() ?: -1L
              config.activeMonthColumns.forEach { col ->
                val target = if (customFee >= 0L) customFee else config.feeForMonth(col)
                monthInputs[col] = target.toString()
              }
            },
            modifier = Modifier.testTag("settle_all_months_button"),
          )
        }

        config.activeMonthColumns.chunked(2).forEach { pair ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom,
          ) {
            pair.forEach { col ->
              val fee = (customFeeText.toLongOrNull()?.takeIf { it >= 0L }) ?: config.feeForMonth(col)
              FintechTextField(
                value = monthInputs[col] ?: "",
                onValueChange = { next ->
                  monthInputs[col] = next.filter { it.isDigit() }.take(6)
                },
                label = "${strings.monthShort(col)} (${formatNumber(fee, dig)})",
                placeholder = "0",
                keyboardType = KeyboardType.Number,
                modifier = Modifier
                  .weight(1f)
                  .testTag("month_input_${col.key}"),
              )
            }
            if (pair.size == 1) {
              Spacer(modifier = Modifier.weight(1f))
            }
          }
        }

        HorizontalDivider(color = tokens.divider)

        // Owner & Contact Section
        FintechTextField(
          value = ownerNameAr,
          onValueChange = { ownerNameAr = it.take(80) },
          label = strings.apptOwnerArLabel,
          placeholder = strings.apptOwnerArPlaceholder,
          modifier = Modifier.fillMaxWidth().testTag("owner_ar_input"),
        )

        FintechTextField(
          value = ownerNameLatin,
          onValueChange = { ownerNameLatin = it.take(80) },
          label = strings.apptOwnerLatinLabel,
          placeholder = strings.apptOwnerLatinPlaceholder,
          modifier = Modifier.fillMaxWidth().testTag("owner_name_input"),
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.Bottom,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          FintechTextField(
            value = phone,
            onValueChange = { phone = it.take(25) },
            label = strings.apptPhoneLabel,
            placeholder = strings.apptPhonePlaceholder,
            keyboardType = KeyboardType.Phone,
            modifier = Modifier.weight(1f).testTag("owner_phone_input"),
          )
          RoundIconCircleButton(
            icon = Icons.Default.Contacts,
            contentDescription = strings.apptPickContactBtn,
            onClick = {
              try {
                val pickIntent = Intent(
                  Intent.ACTION_PICK,
                  ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                )
                contactPickerLauncher.launch(pickIntent)
              } catch (_: Exception) {
              }
            },
            modifier = Modifier.testTag("pick_contact_btn"),
          )
        }

        // Unit Status (Occupied / Empty / Exempt)
        Text(
          text = strings.apptUnitStatusLabel,
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Medium,
          color = tokens.ink,
        )
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          UnitStatus.entries.forEach { st ->
            SyndicatFilterChip(
              selected = unitStatus == st,
              onClick = { unitStatus = st },
              label = strings.unitStatusLabel(st),
            )
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .minimumInteractiveComponentSize()
            .heightIn(min = 48.dp)
            .clickable { isTenant = !isTenant },
        ) {
          Checkbox(checked = isTenant, onCheckedChange = { isTenant = it })
          Text(
            text = strings.apptIsTenantLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = tokens.ink,
          )
        }

        FintechTextField(
          value = customFeeText,
          onValueChange = { customFeeText = it.filter { c -> c.isDigit() }.take(6) },
          label = strings.apptCustomFeeLabel(cur),
          placeholder = config.defaultMonthlyFee.toString(),
          keyboardType = KeyboardType.Number,
          modifier = Modifier.fillMaxWidth().testTag("apt_custom_fee_input"),
        )
      }
    },
    confirmButton = {
      MetaCobaltBuyButton(
        text = strings.apptSave,
        compact = true,
        onClick = { onSave(buildCurrentDraft()) },
        modifier = Modifier.testTag("save_apartment_button"),
      )
    },
    dismissButton = {
      SageSecondaryButton(
        text = strings.apptCancel,
        compact = true,
        onClick = onDismiss,
      )
    },
  )
}
