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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.SpecialCallRecord
import com.example.data.model.ApartmentRecord
import com.example.data.model.MonthColumn
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentRecord
import com.example.data.model.SyndicatConfig
import com.example.data.model.UnitStatus
import com.example.ui.components.AdaptiveContentBox
import com.example.ui.components.FintechPalePositiveCard
import com.example.ui.components.FintechTextField
import com.example.ui.components.FintechWhiteCard
import com.example.ui.components.LocalPrivacyMode
import com.example.ui.components.LocalSyndicatAdaptiveSpec
import com.example.ui.components.MetaCobaltBuyButton
import com.example.ui.components.MetaSearchPill
import com.example.ui.components.MetaSegmentedPillRow
import com.example.ui.components.ReadOnlyCouncilBanner
import com.example.ui.components.RemindedPillBadge
import com.example.ui.components.ResidenceHeroBanner
import com.example.ui.components.RoundIconCircleButton
import com.example.ui.components.SageSecondaryButton
import com.example.ui.components.StatusPillBadge
import com.example.ui.components.SyndicatDialog
import com.example.ui.components.SyndicatEmptyStateCard
import com.example.ui.components.SyndicatFilterChip
import com.example.ui.components.WhatsAppReminderDialog
import com.example.ui.components.maskSensitiveDh
import com.example.ui.components.maskSensitiveName
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
import com.example.util.SyndicatReportsExporter
import com.example.util.WhatsAppHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

private enum class ApartmentSortMode {
  DEFAULT,
  LATE_DESC,
  PAID_DESC,
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
  onSelectYear: (Long) -> Unit = {},
  onSaveApartmentWithPayment: ((ApartmentRecord, ApartmentRecord, String, String, String) -> Unit)? = null,
  onDeletePayment: (PaymentRecord) -> Unit = {},
  initialQuickPayAptNumber: Long? = null,
  onConsumeQuickPayTarget: () -> Unit = {},
  specialCalls: List<SpecialCallRecord> = emptyList(),
  isReadOnlyMode: Boolean = false,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val adaptive = LocalSyndicatAdaptiveSpec.current
  val isPrivacy = LocalPrivacyMode.current
  val maxHeaderWidth = if (adaptive.isExpanded) adaptive.wideContainerMaxWidth else adaptive.readableMaxWidth
  val cfg = data.config
  val cur = cfg.currency
  val dig = cfg.digitsAr
  val isAr = strings.language == AppLanguage.AR
  val monoOrDefault = tokens.monoFontFamily

  val haptics = LocalHapticFeedback.current
  var isGridView by remember { mutableStateOf(false) }
  var sortMode by rememberSaveable { mutableStateOf(ApartmentSortMode.DEFAULT) }
  var editingApartment by remember { mutableStateOf<ApartmentRecord?>(null) }
  var remindingApartment by remember { mutableStateOf<ApartmentRecord?>(null) }
  var showFilteredBatchRemind by remember { mutableStateOf(false) }
  var quickCellTarget by remember { mutableStateOf<Pair<ApartmentRecord, MonthColumn>?>(null) }
  var postPaymentReceiptTarget by remember { mutableStateOf<ApartmentRecord?>(null) }
  var selectedTabletAptNumber by remember { mutableStateOf<Long?>(null) }

  LaunchedEffect(initialQuickPayAptNumber, data.apartments) {
    val targetAptNum = initialQuickPayAptNumber
    if (targetAptNum != null) {
      val targetApt = data.apartments.firstOrNull { it.number == targetAptNum }
      if (targetApt != null) {
        val nextUnpaidCol = cfg.activeMonthColumns.firstOrNull { col ->
          targetApt.amountFor(col) < targetApt.expectedFeeForMonth(col, cfg)
        } ?: cfg.activeMonthColumns.firstOrNull() ?: MonthColumn.JAN
        quickCellTarget = targetApt to nextUnpaidCol
      }
      onConsumeQuickPayTarget()
    }
  }

  val saveApartmentWithMeta: (ApartmentRecord, ApartmentRecord, String, String, String) -> Unit =
    { updated, prev, method, dateStr, notes ->
      if (onSaveApartmentWithPayment != null) {
        onSaveApartmentWithPayment(updated, prev, method, dateStr, notes)
      } else {
        onSaveApartment(updated, prev)
      }
    }

  val statusCounts = remember(data.apartments, cfg) {
    mapOf(
      StatusFilter.ALL to data.apartments.size,
      StatusFilter.UP_TO_DATE to data.apartments.count { it.isUpToDate(cfg) },
      StatusFilter.LATE to data.apartments.count { !it.isVacantForConfig(cfg) && !it.isUpToDate(cfg) },
      StatusFilter.VACANT to data.apartments.count { it.isVacantForConfig(cfg) },
    )
  }

  val filteredApartments = remember(data.apartments, cfg, searchQuery, selectedFloor, statusFilter, sortMode) {
    val base = data.apartments.filter { apt ->
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
    when (sortMode) {
      ApartmentSortMode.DEFAULT -> base
      ApartmentSortMode.LATE_DESC -> base.sortedWith(
        compareByDescending<ApartmentRecord> { it.remainingDh(cfg) }.thenBy { it.number },
      )
      ApartmentSortMode.PAID_DESC -> base.sortedWith(
        compareByDescending<ApartmentRecord> { it.totalForConfig(cfg) }.thenBy { it.number },
      )
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(tokens.screenBg)
      .testTag("apartments_screen"),
  ) {
    // Top Header + Search Bar + Cards/Grid View Switcher
    AdaptiveContentBox(maxWidth = maxHeaderWidth) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = adaptive.horizontalPadding, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      if (isReadOnlyMode) {
        ReadOnlyCouncilBanner()
      }
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Text(
          text = strings.navApartments,
          style = if (adaptive.isNarrowPhone) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineLarge,
          fontWeight = FontWeight.Medium,
          color = tokens.inkDeep,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.align(Alignment.CenterVertically),
        )

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
          modifier = Modifier.align(Alignment.CenterVertically),
        )
      }

      // Multi-Year Selector Row (Exercice annuel)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("apartments_year_selector"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Text(
          text = "${strings.yearSelectorLabel} :",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Medium,
          color = tokens.body,
        )
        LazyRow(
          modifier = Modifier.weight(1f),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          items(data.availableYears) { yr ->
            SyndicatFilterChip(
              selected = yr == cfg.year,
              onClick = { onSelectYear(yr) },
              label = localizeDigits(yr.toString(), dig),
              modifier = Modifier.testTag("year_chip_$yr"),
            )
          }
          item {
            SyndicatFilterChip(
              selected = false,
              onClick = {
                val nextYr = ((data.availableYears.maxOrNull() ?: cfg.year) + 1L).coerceAtMost(2100L)
                onSelectYear(nextYr)
              },
              label = strings.yearAddNextBtn,
              modifier = Modifier.testTag("year_add_next_chip"),
            )
          }
        }
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
        modifier = Modifier.testTag("apartments_filter_row"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        items(StatusFilter.entries) { filter ->
          val count = statusCounts[filter] ?: 0
          val baseLabel = strings.statusFilterLabel(filter, cutoffShort)
          SyndicatFilterChip(
            selected = statusFilter == filter,
            onClick = { onSelectStatusFilter(filter) },
            label = "$baseLabel (${formatNumber(count.toLong(), dig)})",
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
        item {
          SyndicatFilterChip(
            selected = sortMode == ApartmentSortMode.DEFAULT,
            onClick = { sortMode = ApartmentSortMode.DEFAULT },
            label = strings.apptSortDefault,
            modifier = Modifier.testTag("appt_sort_default"),
          )
        }
        item {
          SyndicatFilterChip(
            selected = sortMode == ApartmentSortMode.LATE_DESC,
            onClick = { sortMode = ApartmentSortMode.LATE_DESC },
            label = strings.apptSortLateDesc,
            modifier = Modifier.testTag("appt_sort_late"),
          )
        }
        item {
          SyndicatFilterChip(
            selected = sortMode == ApartmentSortMode.PAID_DESC,
            onClick = { sortMode = ApartmentSortMode.PAID_DESC },
            label = strings.apptSortPaidDesc,
            modifier = Modifier.testTag("appt_sort_paid"),
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

    BoxWithConstraints(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
    ) {
      val isTabletSplit = maxWidth >= 840.dp && !isGridView
      val singlePaneConstraintWidth = if (isTabletSplit) adaptive.wideContainerMaxWidth else adaptive.readableMaxWidth

      if (!isGridView) {
        val listContent: @Composable (Modifier) -> Unit = { listMod ->
          LazyColumn(
            modifier = listMod
              .fillMaxHeight()
              .testTag("apartments_cards_list"),
            contentPadding = PaddingValues(
              start = adaptive.horizontalPadding,
              end = adaptive.horizontalPadding,
              top = 4.dp,
              bottom = adaptive.listBottomPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            item(key = "cotisations_residence_banner") {
              AdaptiveContentBox(maxWidth = singlePaneConstraintWidth) {
              ResidenceHeroBanner(
                residenceName = cfg.residenceName.ifBlank { strings.navApartments },
                subtitle = "${strings.navApartments} · ${bidiLtr(localizeDigits(cfg.year.toString(), dig))}",
                badgeText = strings.residenceCustomizePhotoShort,
                compact = true,
                onPhotoFeedback = onExportError,
              )
              }
            }

            if (filteredApartments.isEmpty()) {
              item(key = "apts_empty_filtered") {
                SyndicatEmptyStateCard(
                  icon = Icons.Default.Search,
                  title = strings.apptEmptyFilterTitle,
                  subtitle = strings.apptEmptyFilterSub,
                  actionLabel = strings.apptResetFiltersBtn,
                  actionIcon = Icons.Default.Clear,
                  actionTag = "apts_reset_filters_btn",
                  onAction = {
                    onSearchQueryChange("")
                    onSelectFloor(null)
                    onSelectStatusFilter(StatusFilter.ALL)
                  },
                )
              }
            } else {
              data.orderedFloors.forEach { floor ->
                val floorApts = filteredApartments.filter { it.floor == floor }
                if (floorApts.isNotEmpty()) {
                  item(key = "floor_hdr_$floor") {
                    val subtotal = floorApts.sumOf { it.totalForConfig(cfg) }
                    AdaptiveContentBox(maxWidth = singlePaneConstraintWidth) {
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
                        text = "${strings.apptSubtotal} ${maskSensitiveDh(formatDh(subtotal, cur, dig), isPrivacy)}",
                        style = MaterialTheme.typography.labelLarge,
                        fontFamily = monoOrDefault,
                        fontWeight = FontWeight.Medium,
                        color = tokens.positiveDeep,
                      )
                    }
                    }
                  }

                  items(
                    items = floorApts,
                    key = { it.id.ifBlank { "apt_${it.number}" } },
                  ) { apt ->
                    val isVacant = apt.isVacantForConfig(cfg)
                    val cardContent: @Composable () -> Unit = {
                      AdaptiveContentBox(maxWidth = singlePaneConstraintWidth) {
                      ApartmentFintechCard(
                        apartment = apt,
                        config = cfg,
                        isAr = isAr,
                        onEditClick = {
                          if (isTabletSplit) {
                            selectedTabletAptNumber = apt.number
                          } else {
                            editingApartment = apt
                          }
                        },
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

                    if (!isVacant) {
                      val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                          when (value) {
                            SwipeToDismissBoxValue.StartToEnd -> {
                              haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                              val nextUnpaid = cfg.activeMonthColumns.firstOrNull { col ->
                                apt.amountFor(col) < apt.expectedFeeForMonth(col, cfg)
                              } ?: cfg.activeMonthColumns.firstOrNull() ?: MonthColumn.JAN
                              quickCellTarget = apt to nextUnpaid
                              false
                            }
                            SwipeToDismissBoxValue.EndToStart -> {
                              haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                              remindingApartment = apt
                              false
                            }
                            SwipeToDismissBoxValue.Settled -> false
                          }
                        },
                      )
                      SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = true,
                        enableDismissFromEndToStart = true,
                        backgroundContent = {
                          val dir = dismissState.dismissDirection
                          val bgColor = when (dir) {
                            SwipeToDismissBoxValue.StartToEnd -> if (tokens.isDark) Color(0xFF0A2E1F) else CoinbaseSuccessTintLight
                            SwipeToDismissBoxValue.EndToStart -> tokens.primaryTint
                            SwipeToDismissBoxValue.Settled -> Color.Transparent
                          }
                          Box(
                            modifier = Modifier
                              .fillMaxSize()
                              .clip(RoundedCornerShape(tokens.cardRadiusDp.dp))
                              .background(bgColor)
                              .padding(horizontal = 20.dp),
                            contentAlignment = if (dir == SwipeToDismissBoxValue.StartToEnd) {
                              Alignment.CenterStart
                            } else {
                              Alignment.CenterEnd
                            },
                          ) {
                            if (dir == SwipeToDismissBoxValue.StartToEnd) {
                              Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                              ) {
                                Icon(
                                  imageVector = Icons.Default.Payments,
                                  contentDescription = strings.apptRecordPayment,
                                  tint = tokens.positiveDeep,
                                )
                                Text(
                                  text = strings.apptRecordPayment,
                                  style = MaterialTheme.typography.labelLarge,
                                  fontWeight = FontWeight.Bold,
                                  color = tokens.positiveDeep,
                                )
                              }
                            } else if (dir == SwipeToDismissBoxValue.EndToStart) {
                              Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                              ) {
                                Text(
                                  text = strings.dashRemindBtn,
                                  style = MaterialTheme.typography.labelLarge,
                                  fontWeight = FontWeight.Bold,
                                  color = tokens.linkColor,
                                )
                                Icon(
                                  imageVector = Icons.AutoMirrored.Filled.Send,
                                  contentDescription = strings.dashRemindBtn,
                                  tint = tokens.linkColor,
                                )
                              }
                            }
                          }
                        },
                      ) {
                        cardContent()
                      }
                    } else {
                      cardContent()
                    }
                  }
                }
              }
            }
          }
        }

        if (isTabletSplit) {
          AdaptiveContentBox(maxWidth = adaptive.wideContainerMaxWidth) {
          Row(modifier = Modifier.fillMaxSize()) {
            listContent(Modifier.weight(0.54f))
            val selectedApt = filteredApartments.firstOrNull { it.number == selectedTabletAptNumber }
              ?: filteredApartments.firstOrNull()
            TabletApartmentDetailPane(
              apartment = selectedApt,
              config = cfg,
              isAr = isAr,
              paymentsHistory = selectedApt?.let { data.paymentsForApartment(it.number) }.orEmpty(),
              isReadOnlyMode = isReadOnlyMode,
              onOpenFullEditor = { apt -> editingApartment = apt },
              onQuickMonthClick = { apt, m -> if (!isReadOnlyMode) quickCellTarget = apt to m },
              onRemindWhatsApp = { apt -> remindingApartment = apt },
              onShareReceipt = { apt ->
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
              onExportStatementPdf = { apt ->
                SyndicatReportsExporter.exportApartmentStatementPdf(
                  context = context,
                  data = data,
                  apartment = apt,
                  specialCalls = specialCalls,
                  payments = data.paymentsForApartment(apt.number),
                  onError = onExportError,
                )
              },
              onShareStatementWhatsApp = { apt ->
                val msg = SyndicatReportsExporter.buildApartmentWhatsAppStatement(strings, data, apt, specialCalls)
                WhatsAppHelper.openRawWhatsAppMessage(context, apt.phone, msg, onError = onExportError)
              },
              modifier = Modifier
                .weight(0.46f)
                .fillMaxHeight()
                .padding(end = adaptive.horizontalPadding, bottom = 24.dp, top = 4.dp),
            )
          }
          }
        } else {
          listContent(Modifier.fillMaxWidth())
        }
      } else {
      // =========================================================================
      // COMPACT TABLE / GRID VIEW SANS SLIDER GAUCHE-DROITE :
      // Grille multi-colonnes adaptative (1 colonne sur téléphone, 2 colonnes sur tablette)
      // =========================================================================
      val activeMonths = cfg.activeMonthColumns
      val gridCols = if (maxWidth >= 700.dp) 2 else 1
      val gridMaxWidth = if (gridCols > 1) adaptive.wideContainerMaxWidth else adaptive.readableMaxWidth
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .testTag("apartments_grid_list"),
        contentPadding = PaddingValues(
          start = if (adaptive.isNarrowPhone) 10.dp else adaptive.horizontalPadding,
          end = if (adaptive.isNarrowPhone) 10.dp else adaptive.horizontalPadding,
          top = 4.dp,
          bottom = adaptive.listBottomPadding,
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        item(key = "grid_tariffs_legend") {
          AdaptiveContentBox(maxWidth = gridMaxWidth) {
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
        }

        data.orderedFloors.forEach { floor ->
          val floorApts = filteredApartments.filter { it.floor == floor }
          if (floorApts.isNotEmpty()) {
            item(key = "grid_floor_$floor") {
              val subtotal = floorApts.sumOf { it.totalForConfig(cfg) }
              AdaptiveContentBox(maxWidth = gridMaxWidth) {
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
                  text = "${strings.apptSubtotal} ${maskSensitiveDh(formatDh(subtotal, cur, dig), isPrivacy)}",
                  style = MaterialTheme.typography.labelMedium,
                  fontFamily = monoOrDefault,
                  fontWeight = FontWeight.SemiBold,
                  color = tokens.positiveDeep,
                )
              }
              }
            }

            val chunkedFloorApts = floorApts.chunked(gridCols)
            items(
              items = chunkedFloorApts,
              key = { chunk -> "grid_row_${chunk.first().id.ifBlank { chunk.first().number.toString() }}" },
            ) { rowApts ->
              AdaptiveContentBox(maxWidth = gridMaxWidth) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
              ) {
              rowApts.forEach { apt ->
              val rawOwnerLabel = apt.displayOwner(isAr).ifBlank { "${strings.colAppt} ${apt.displayNumber}" }
              val ownerLabel = maskSensitiveName(rawOwnerLabel, isPrivacy)
              val isVacant = apt.isVacantForConfig(cfg)

              Surface(
                shape = RoundedCornerShape(tokens.contentCardRadiusDp.dp),
                color = tokens.cardBg,
                border = BorderStroke(1.dp, tokens.cardBorder),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                modifier = Modifier.weight(1f),
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
                      horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                      StatusPillBadge(status = apt.badgeStatus(cfg))
                      Text(
                        text = maskSensitiveDh(formatDh(apt.totalForConfig(cfg), cur, dig), isPrivacy),
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
                    onMonthClick = { m -> if (!isReadOnlyMode) quickCellTarget = apt to m },
                  )
                }
              }
              }
              if (rowApts.size < gridCols) {
                repeat(gridCols - rowApts.size) {
                  Spacer(modifier = Modifier.weight(1f))
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

  // Quick Multi-Month Bottom Sheet Payment
  quickCellTarget?.let { (apt, month) ->
    QuickMonthCellPaymentSheet(
      apartment = apt,
      month = month,
      config = cfg,
      onDismiss = { quickCellTarget = null },
      onSaveMonths = { updatedMonths, methodCode, dateStr ->
        var updated = apt
        var anyIncreased = false
        updatedMonths.forEach { (m, amt) ->
          if (amt > apt.amountFor(m)) anyIncreased = true
          updated = updated.withMonthAmount(m, amt)
        }
        if (updatedMonths.any { it.second > 0L } && updated.status == "empty") {
          updated = updated.copy(status = "occupied")
        }
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        saveApartmentWithMeta(updated, apt, methodCode, dateStr, "")
        quickCellTarget = null
        if (anyIncreased && updated.totalForConfig(cfg) > 0L) {
          postPaymentReceiptTarget = updated
        }
      },
    )
  }

  // Comprehensive Apartment Edit & Multi-Month Payment Modal
  editingApartment?.let { apt ->
    val liveApt = data.apartments.firstOrNull { it.number == apt.number } ?: apt
    val aptPayments = remember(data.payments, liveApt.number) {
      data.paymentsForApartment(liveApt.number)
    }
    ApartmentPaymentAndOwnerModal(
      apartment = liveApt,
      config = cfg,
      paymentsHistory = aptPayments,
      isReadOnlyMode = isReadOnlyMode,
      onDismiss = { editingApartment = null },
      onSharePaymentReceipt = { payRecord ->
        scope.launch {
          val rNo = payRecord.receiptNumber.takeIf { it > 0L } ?: onRequestReceiptNo(liveApt)
          PdfPosterExporter.sharePaymentReceiptImage(
            context = context,
            apartment = liveApt,
            data = data,
            receiptNo = rNo,
            onError = onExportError,
            payment = payRecord,
          )
        }
      },
      onSharePaymentWhatsApp = { payRecord ->
        scope.launch {
          val rNo = payRecord.receiptNumber.takeIf { it > 0L } ?: onRequestReceiptNo(liveApt)
          PdfPosterExporter.sharePaymentReceiptViaWhatsApp(
            context = context,
            apartment = liveApt,
            data = data,
            receiptNo = rNo,
            onError = onExportError,
            payment = payRecord,
          )
        }
      },
      onExportStatementPdf = {
        SyndicatReportsExporter.exportApartmentStatementPdf(
          context = context,
          data = data,
          apartment = liveApt,
          specialCalls = specialCalls,
          payments = aptPayments,
          onError = onExportError,
        )
      },
      onShareStatementWhatsApp = {
        val msg = SyndicatReportsExporter.buildApartmentWhatsAppStatement(strings, data, liveApt, specialCalls)
        WhatsAppHelper.openRawWhatsAppMessage(context, liveApt.phone, msg, onError = onExportError)
      },
      onDeletePayment = onDeletePayment,
      onSave = { updated, methodCode, dateStr ->
        if (!isReadOnlyMode) {
          val oldTotal = liveApt.totalForConfig(cfg)
          val newTotal = updated.totalForConfig(cfg)
          saveApartmentWithMeta(updated, liveApt, methodCode, dateStr, "")
          editingApartment = null
          if (newTotal > oldTotal && newTotal > 0L) {
            postPaymentReceiptTarget = updated
          }
        }
      },
    )
  }

  postPaymentReceiptTarget?.let { targetApt ->
    val liveTarget = data.apartments.firstOrNull { it.number == targetApt.number } ?: targetApt
    val latestPayment = data.paymentsForApartment(liveTarget.number).firstOrNull()
    val ownerConfirmed = maskSensitiveName(liveTarget.displayOwner(isAr).ifBlank { "${strings.colAppt} ${liveTarget.displayNumber}" }, isPrivacy)
    SyndicatDialog(
      onDismissRequest = { postPaymentReceiptTarget = null },
      title = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Surface(
            shape = CircleShape,
            color = if (tokens.isDark) Color(0xFF0A2E1F) else CoinbaseSuccessTintLight,
            modifier = Modifier.size(40.dp),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = tokens.positiveDeep,
                modifier = Modifier.size(22.dp),
              )
            }
          }
          Text(
            text = strings.receiptPostPaymentTitle,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = tokens.inkDeep,
            modifier = Modifier.testTag("post_payment_receipt_dialog"),
          )
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = tokens.surfaceSoft,
            border = BorderStroke(1.dp, tokens.divider),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(
              modifier = Modifier.padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Text(
                text = "${strings.colAppt} ${localizeDigits(liveTarget.displayNumber, dig)} · $ownerConfirmed",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = tokens.inkDeep,
              )
              Text(
                text = "${strings.colPaid} : ${maskSensitiveDh(formatDh(liveTarget.totalForConfig(cfg), cur, dig), isPrivacy)}",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.positiveDeep,
                fontWeight = FontWeight.Medium,
              )
            }
          }
          Text(
            text = strings.receiptPostPaymentSub,
            style = MaterialTheme.typography.bodyMedium,
            color = tokens.body,
          )
          MetaCobaltBuyButton(
            text = strings.receiptWhatsappBtn,
            icon = Icons.AutoMirrored.Filled.Send,
            compact = true,
            onClick = {
              postPaymentReceiptTarget = null
              scope.launch {
                val rNo = latestPayment?.receiptNumber?.takeIf { it > 0L } ?: onRequestReceiptNo(liveTarget)
                PdfPosterExporter.sharePaymentReceiptViaWhatsApp(
                  context = context,
                  apartment = liveTarget,
                  data = data,
                  receiptNo = rNo,
                  onError = onExportError,
                  payment = latestPayment,
                )
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("post_payment_whatsapp_btn"),
          )
          SageSecondaryButton(
            text = strings.apptReceiptBtn,
            icon = Icons.Default.Receipt,
            compact = true,
            onClick = {
              postPaymentReceiptTarget = null
              scope.launch {
                val rNo = latestPayment?.receiptNumber?.takeIf { it > 0L } ?: onRequestReceiptNo(liveTarget)
                PdfPosterExporter.sharePaymentReceiptImage(
                  context = context,
                  apartment = liveTarget,
                  data = data,
                  receiptNo = rNo,
                  onError = onExportError,
                  payment = latestPayment,
                )
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("post_payment_image_btn"),
          )
        }
      },
      confirmButton = {},
      dismissButton = {
        SageSecondaryButton(
          text = strings.apptCancel,
          compact = true,
          onClick = { postPaymentReceiptTarget = null },
        )
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
  val isPrivacy = LocalPrivacyMode.current
  val cur = config.currency
  val dig = config.digitsAr
  val monoOrDefault = tokens.monoFontFamily

  val badgeStatus = apartment.badgeStatus(config)
  val totalPaid = apartment.totalForConfig(config)
  val remaining = apartment.remainingDh(config)
  val advance = apartment.advanceDh(config)
  val primaryOwner = maskSensitiveName(apartment.displayOwner(isAr), isPrivacy)
  val secondaryOwner = maskSensitiveName(apartment.secondaryOwner(isAr), isPrivacy)

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
            if (apartment.phone.isNotBlank() && !isPrivacy) {
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
            text = maskSensitiveDh(formatDh(totalPaid, cur, dig), isPrivacy),
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
                text = "! ${maskSensitiveDh(formatDh(remaining, cur, dig), isPrivacy)}",
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
                text = maskSensitiveDh(formatDh(advance, cur, dig, explicitPlus = true), isPrivacy),
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

    // Ruban thermique 12 mois ultra-lisible (J F M A M J J A S O N D)
    ApartmentMiniHeatmapRibbon(
      apartment = apartment,
      config = config,
      onMonthClick = onQuickMonthClick,
    )

    Spacer(modifier = Modifier.height(10.dp))

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

          val isCurrentCutoff = m.monthNumber == config.cutoffMonth.toInt()
          val tileBg = when {
            isPaidFull -> if (tokens.isDark) Color(0xFF0A2E1F) else CoinbaseSuccessTintLight
            isPartial -> if (tokens.isDark) Color(0xFF332707) else CoinbaseWarningTintLight
            isLateMonth -> if (tokens.isDark) CoinbaseDarkErrorContainer else CoinbaseErrorTintLight
            else -> tokens.surfaceSoft
          }
          val tileBorder = when {
            isCurrentCutoff -> tokens.linkColor
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
            border = BorderStroke(if (isCurrentCutoff) 1.5.dp else 1.dp, tileBorder),
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
private fun ApartmentMiniHeatmapRibbon(
  apartment: ApartmentRecord,
  config: SyndicatConfig,
  onMonthClick: (MonthColumn) -> Unit,
) {
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val isVacant = apartment.isVacantForConfig(config)
  val activeSet = config.activeMonthColumns.toSet()

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("apt_heatmap_${apartment.number}"),
    horizontalArrangement = Arrangement.spacedBy(3.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    MonthColumn.entries.forEach { m ->
      val isActive = m in activeSet
      val paid = apartment.amountFor(m)
      val expected = apartment.expectedFeeForMonth(m, config)
      val isPartial = isActive && paid > 0L && expected > 0L && paid < expected
      val isLate = isActive && !isVacant && m.monthNumber <= config.cutoffMonth.toInt() && paid < expected
      val isFull = isActive && paid > 0L && !isPartial

      val bg = when {
        !isActive -> tokens.divider.copy(alpha = 0.35f)
        isFull -> tokens.positive
        isPartial -> tokens.warning
        isLate -> tokens.statusLateText
        else -> tokens.surfaceStrong
      }
      val fg = when {
        isFull || isLate -> Color.White
        isPartial -> Color(0xFF1E1B16)
        !isActive -> tokens.mute.copy(alpha = 0.6f)
        else -> tokens.body
      }
      val shortOne = strings.monthShort(m).take(1).uppercase()
      Box(
        modifier = Modifier
          .weight(1f)
          .height(22.dp)
          .clip(RoundedCornerShape(6.dp))
          .background(bg)
          .let { if (isActive) it.clickable { onMonthClick(m) } else it },
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = shortOne,
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = fg,
          maxLines = 1,
        )
      }
    }
  }
}

@Composable
private fun TabletApartmentDetailPane(
  apartment: ApartmentRecord?,
  config: SyndicatConfig,
  isAr: Boolean,
  paymentsHistory: List<PaymentRecord>,
  isReadOnlyMode: Boolean = false,
  onOpenFullEditor: (ApartmentRecord) -> Unit,
  onQuickMonthClick: (ApartmentRecord, MonthColumn) -> Unit,
  onRemindWhatsApp: (ApartmentRecord) -> Unit,
  onShareReceipt: (ApartmentRecord) -> Unit,
  onExportStatementPdf: (ApartmentRecord) -> Unit,
  onShareStatementWhatsApp: (ApartmentRecord) -> Unit,
  modifier: Modifier = Modifier,
) {
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val isPrivacy = LocalPrivacyMode.current
  val cur = config.currency
  val dig = config.digitsAr

  FintechWhiteCard(
    modifier = modifier.testTag("tablet_apartment_detail_pane"),
    padding = 20.dp,
  ) {
    if (apartment == null) {
      SyndicatEmptyStateCard(
        icon = Icons.Default.Apartment,
        title = strings.tabletDetailPromptTitle,
        subtitle = strings.tabletDetailPromptSub,
      )
    } else {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = strings.apptDialogTitle(apartment.displayNumber, strings.floorWithPrefix(apartment.floor)),
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.SemiBold,
              color = tokens.inkDeep,
            )
            val owner = apartment.displayOwner(isAr)
            if (owner.isNotBlank()) {
              Text(
                text = maskSensitiveName(owner, isPrivacy),
                style = MaterialTheme.typography.bodyMedium,
                color = tokens.body,
              )
            }
          }
          StatusPillBadge(status = apartment.badgeStatus(config))
        }

        ApartmentMiniHeatmapRibbon(
          apartment = apartment,
          config = config,
          onMonthClick = { m -> onQuickMonthClick(apartment, m) },
        )

        ApartmentMonthsNoScrollGrid(
          apartment = apartment,
          activeMonths = config.activeMonthColumns,
          config = config,
          cellTagPrefix = "tablet_detail_${apartment.number}",
          onMonthClick = { m -> onQuickMonthClick(apartment, m) },
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          if (!isReadOnlyMode) {
            MetaCobaltBuyButton(
              text = strings.apptRecordPayment,
              icon = Icons.Default.Payments,
              compact = true,
              onClick = { onOpenFullEditor(apartment) },
              modifier = Modifier.weight(1f),
            )
          }
          if (apartment.remainingDh(config) > 0L && !apartment.isVacantForConfig(config)) {
            SageSecondaryButton(
              text = strings.dashRemindBtn,
              icon = Icons.AutoMirrored.Filled.Send,
              compact = true,
              onClick = { onRemindWhatsApp(apartment) },
            )
          }
          if (apartment.totalForConfig(config) > 0L) {
            SageSecondaryButton(
              text = strings.apptReceiptBtn,
              icon = Icons.Default.Receipt,
              compact = true,
              onClick = { onShareReceipt(apartment) },
            )
          }
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          SageSecondaryButton(
            text = strings.aptStatementPdfBtn,
            icon = Icons.Default.PictureAsPdf,
            compact = true,
            onClick = { onExportStatementPdf(apartment) },
            modifier = Modifier.weight(1f),
          )
          SageSecondaryButton(
            text = strings.aptStatementWaBtn,
            icon = Icons.AutoMirrored.Filled.Send,
            compact = true,
            onClick = { onShareStatementWhatsApp(apartment) },
            modifier = Modifier.weight(1f),
          )
        }

        if (paymentsHistory.isNotEmpty()) {
          HorizontalDivider(color = tokens.divider)
          Text(
            text = "${strings.paymentHistoryTitle} (${formatNumber(paymentsHistory.size.toLong(), dig)})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = tokens.inkDeep,
          )
          paymentsHistory.take(6).forEach { pay ->
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = tokens.surfaceSoft,
              border = BorderStroke(1.dp, tokens.divider),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = "${bidiLtr(localizeDigits(pay.paymentDate, dig))} · ${strings.paymentMethodLabel(pay.paymentMethod)}",
                  style = MaterialTheme.typography.bodySmall,
                  color = tokens.body,
                  modifier = Modifier.weight(1f),
                )
                Text(
                  text = maskSensitiveDh(formatDh(pay.amountDh, cur, dig), isPrivacy),
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.SemiBold,
                  color = tokens.positiveDeep,
                )
              }
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun QuickMonthCellPaymentSheet(
  apartment: ApartmentRecord,
  month: MonthColumn,
  config: SyndicatConfig,
  onDismiss: () -> Unit,
  onSaveMonths: (List<Pair<MonthColumn, Long>>, String, String) -> Unit,
) {
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val today = remember { SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Date()) }
  val activeCols = config.activeMonthColumns
  val startIdx = remember(activeCols, month) {
    activeCols.indexOf(month).coerceAtLeast(0)
  }
  val maxRangeCount = (activeCols.size - startIdx).coerceAtLeast(1)

  var selectedRangeCount by remember(apartment, month) { mutableIntStateOf(1) }
  val targetMonths = remember(activeCols, startIdx, selectedRangeCount) {
    if (selectedRangeCount >= activeCols.size) {
      activeCols
    } else {
      activeCols.subList(startIdx, (startIdx + selectedRangeCount).coerceAtMost(activeCols.size))
    }
  }

  val expectedFee = apartment.expectedFeeForMonth(month, config)
    .takeIf { it > 0L } ?: config.feeForMonth(month)
  val rangeFullTotalDh = remember(targetMonths, apartment, config) {
    targetMonths.sumOf { m ->
      apartment.expectedFeeForMonth(m, config).takeIf { it > 0L } ?: config.feeForMonth(m)
    }
  }

  var amountText by remember(apartment, month) {
    val curAmt = apartment.amountFor(month)
    mutableStateOf(if (curAmt == 0L) "" else curAmt.toString())
  }
  var selectedMethod by remember { mutableStateOf(PaymentMethod.CASH) }
  var paymentDate by remember { mutableStateOf(today) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = tokens.cardBg,
    contentColor = tokens.inkDeep,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp)
        .padding(bottom = 28.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Column {
        Text(
          text = "${strings.colAppt} ${apartment.displayNumber} · ${strings.monthFull(month)} ${localizeDigits(config.year.toString(), config.digitsAr)}",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.SemiBold,
          color = tokens.inkDeep,
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

      // Sélection rapide de période (1 mois / Trimestre / Semestre / Année complète)
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          text = strings.quickPayRangeLabel,
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Medium,
          color = tokens.body,
        )
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          val presets = buildList {
            add(1 to strings.quickPayRange1Month)
            if (maxRangeCount >= 3) add(3 to strings.quickPayRange3Months)
            if (maxRangeCount >= 6) add(6 to strings.quickPayRange6Months)
            if (activeCols.size > 1) add(activeCols.size to strings.quickPayRangeFullYear)
          }
          presets.forEach { (cnt, lbl) ->
            SyndicatFilterChip(
              selected = selectedRangeCount == cnt,
              onClick = { selectedRangeCount = cnt },
              label = lbl,
              modifier = Modifier.testTag("quick_pay_range_$cnt"),
            )
          }
        }
      }

      if (selectedRangeCount == 1) {
        FintechTextField(
          value = amountText,
          onValueChange = { next -> amountText = next.filter { it.isDigit() }.take(6) },
          label = "${strings.colPaid} (${config.currency})",
          placeholder = expectedFee.toString(),
          keyboardType = KeyboardType.Number,
          modifier = Modifier.fillMaxWidth().testTag("quick_cell_amount_input"),
        )
      } else {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = tokens.primaryTint,
          border = BorderStroke(1.dp, tokens.linkColor.copy(alpha = 0.25f)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Text(
              text = targetMonths.joinToString(" · ") { strings.monthShort(it) },
              style = MaterialTheme.typography.labelSmall,
              color = tokens.body,
            )
            Text(
              text = strings.quickPayPreviewTotal(
                targetMonths.size,
                formatDh(rangeFullTotalDh, config.currency, config.digitsAr),
              ),
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.SemiBold,
              color = tokens.linkColor,
            )
          }
        }
      }

      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          text = strings.paymentMethodFieldLabel,
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Medium,
          color = tokens.body,
        )
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          PaymentMethod.entries.forEach { m ->
            val mIcon = when (m) {
              PaymentMethod.CASH -> Icons.Default.Payments
              PaymentMethod.TRANSFER -> Icons.Default.AccountBalance
              PaymentMethod.CHECK -> Icons.Default.Receipt
              PaymentMethod.MOBILE -> Icons.Default.PhoneAndroid
              PaymentMethod.OTHER -> Icons.Default.MoreHoriz
            }
            SyndicatFilterChip(
              selected = selectedMethod == m,
              onClick = { selectedMethod = m },
              label = strings.paymentMethodLabel(m.code),
              leadingIcon = {
                Icon(
                  imageVector = mIcon,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                )
              },
              modifier = Modifier.testTag("quick_pay_method_${m.code}"),
            )
          }
        }
      }

      FintechTextField(
        value = paymentDate,
        onValueChange = { paymentDate = it.take(25) },
        label = strings.paymentDateFieldLabel,
        keyboardType = KeyboardType.Number,
        modifier = Modifier.fillMaxWidth().testTag("quick_pay_date_input"),
      )

      val fullBtnText = if (selectedRangeCount == 1) {
        "${strings.apptPayFullBtn} (${formatDh(expectedFee, config.currency, config.digitsAr)})"
      } else {
        strings.quickPayConfirmBtn(formatDh(rangeFullTotalDh, config.currency, config.digitsAr))
      }

      MetaCobaltBuyButton(
        text = fullBtnText,
        icon = Icons.Default.DoneAll,
        onClick = {
          val updates = targetMonths.map { m ->
            val fee = apartment.expectedFeeForMonth(m, config).takeIf { it > 0L } ?: config.feeForMonth(m)
            m to fee
          }
          onSaveMonths(updates, selectedMethod.code, paymentDate.trim())
        },
        modifier = Modifier.fillMaxWidth().testTag("quick_cell_pay_full_btn"),
      )

      if (selectedRangeCount == 1 && apartment.amountFor(month) > 0L) {
        SageSecondaryButton(
          text = strings.apptClearMonthZeroBtn,
          icon = Icons.Default.Clear,
          compact = true,
          onClick = { onSaveMonths(listOf(month to 0L), selectedMethod.code, paymentDate.trim()) },
          modifier = Modifier.fillMaxWidth().testTag("quick_cell_clear_zero_btn"),
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        SageSecondaryButton(
          text = strings.apptCancel,
          compact = true,
          onClick = onDismiss,
          modifier = Modifier.weight(1f),
        )
        if (selectedRangeCount == 1) {
          SageSecondaryButton(
            text = strings.apptSave,
            compact = true,
            onClick = {
              onSaveMonths(
                listOf(month to (amountText.toLongOrNull() ?: 0L)),
                selectedMethod.code,
                paymentDate.trim(),
              )
            },
            modifier = Modifier.weight(1f).testTag("quick_cell_save_btn"),
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ApartmentPaymentAndOwnerModal(
  apartment: ApartmentRecord,
  config: SyndicatConfig,
  paymentsHistory: List<PaymentRecord>,
  isReadOnlyMode: Boolean = false,
  onDismiss: () -> Unit,
  onSharePaymentReceipt: (PaymentRecord) -> Unit,
  onSharePaymentWhatsApp: (PaymentRecord) -> Unit,
  onExportStatementPdf: () -> Unit,
  onShareStatementWhatsApp: () -> Unit,
  onDeletePayment: (PaymentRecord) -> Unit,
  onSave: (ApartmentRecord, String, String) -> Unit,
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
  val today = remember { SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Date()) }
  var selectedMethod by remember { mutableStateOf(PaymentMethod.CASH) }
  var paymentDate by remember { mutableStateOf(today) }

  val monthInputs = remember(apartment.id, config.year) {
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
        // Relevé de situation annuel (PDF + WhatsApp)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          SageSecondaryButton(
            text = strings.aptStatementPdfBtn,
            icon = Icons.Default.PictureAsPdf,
            compact = true,
            onClick = onExportStatementPdf,
            modifier = Modifier.weight(1f).testTag("apt_statement_pdf_btn"),
          )
          SageSecondaryButton(
            text = strings.aptStatementWaBtn,
            icon = Icons.AutoMirrored.Filled.Send,
            compact = true,
            onClick = onShareStatementWhatsApp,
            modifier = Modifier.weight(1f).testTag("apt_statement_wa_btn"),
          )
        }
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

        // Mode & Date de paiement (pour tout nouveau versement enregistré)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = strings.paymentMethodFieldLabel,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = tokens.ink,
          )
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            PaymentMethod.entries.forEach { m ->
              SyndicatFilterChip(
                selected = selectedMethod == m,
                onClick = { selectedMethod = m },
                label = strings.paymentMethodLabel(m.code),
                modifier = Modifier.testTag("modal_pay_method_${m.code}"),
              )
            }
          }
        }

        FintechTextField(
          value = paymentDate,
          onValueChange = { paymentDate = it.take(25) },
          label = strings.paymentDateFieldLabel,
          keyboardType = KeyboardType.Number,
          modifier = Modifier.fillMaxWidth().testTag("modal_pay_date_input"),
        )

        // Historique des paiements & reçus multi-années
        HorizontalDivider(color = tokens.divider)
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("apartment_payment_history_section"),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text = "${strings.paymentHistoryTitle} (${localizeDigits(paymentsHistory.size.toString(), dig)})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = tokens.ink,
          )
          if (paymentsHistory.isEmpty()) {
            Text(
              text = strings.paymentHistoryEmpty,
              style = MaterialTheme.typography.bodySmall,
              color = tokens.mute,
            )
          } else {
            paymentsHistory.take(15).forEach { pay ->
              val monthsLabel = pay.effectiveMonthsCovered
                .joinToString(" · ") { strings.monthShort(it) }
                .ifBlank { "—" }
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = tokens.surfaceSoft,
                border = BorderStroke(1.dp, tokens.divider),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("payment_history_item_${pay.id}"),
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                  Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                  ) {
                    Text(
                      text = "${formatDh(pay.amountDh, cur, dig)} · ${strings.paymentMethodLabel(pay.paymentMethod)}",
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.SemiBold,
                      color = tokens.ink,
                    )
                    Text(
                      text = "${bidiLtr(localizeDigits(pay.paymentDate.ifBlank { "—" }, dig))} · ${localizeDigits(pay.year.toString(), dig)} ($monthsLabel) · #${"%03d".format(pay.receiptNumber)}",
                      style = MaterialTheme.typography.bodySmall,
                      color = tokens.body,
                    )
                  }
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                      onClick = { onSharePaymentWhatsApp(pay) },
                      modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("share_receipt_whatsapp_${pay.id}"),
                    ) {
                      Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = strings.receiptWhatsappBtn,
                        tint = tokens.positiveDeep,
                      )
                    }
                    IconButton(
                      onClick = { onSharePaymentReceipt(pay) },
                      modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("share_payment_receipt_${pay.id}"),
                    ) {
                      Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = strings.paymentReceiptShareDesc,
                        tint = MaterialTheme.colorScheme.primary,
                      )
                    }
                    IconButton(
                      onClick = { onDeletePayment(pay) },
                      modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("delete_payment_${pay.id}"),
                    ) {
                      Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = strings.paymentDeleteDesc,
                        tint = tokens.negative,
                      )
                    }
                  }
                }
              }
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
      if (!isReadOnlyMode) {
        MetaCobaltBuyButton(
          text = strings.apptSave,
          compact = true,
          onClick = { onSave(buildCurrentDraft(), selectedMethod.code, paymentDate.trim()) },
          modifier = Modifier.testTag("save_apartment_button"),
        )
      }
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
