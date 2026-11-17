package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.IncidentRecord
import com.example.data.local.RecurringExpenseTemplate
import com.example.data.local.VendorRecord
import com.example.data.model.EXPENSE_CATEGORIES
import com.example.data.model.ExpenseRecord
import com.example.ui.components.AirtableSignatureCard
import com.example.ui.components.AirtableSoftCard
import com.example.ui.components.ExpenseCategoriesBreakdownCard
import com.example.ui.components.FintechTextField
import com.example.ui.components.FintechWhiteCard
import com.example.ui.components.LocalPrivacyMode
import com.example.ui.components.MetaCobaltBuyButton
import com.example.ui.components.MetaSegmentedThreePillRow
import com.example.ui.components.ReadOnlyCouncilBanner
import com.example.ui.components.SageSecondaryButton
import com.example.ui.components.SyndicatDialog
import com.example.ui.components.SyndicatEmptyStateCard
import com.example.ui.components.SyndicatFilterChip
import com.example.ui.components.SyndicatProgressBar
import com.example.ui.components.maskSensitiveDh
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalStrings
import com.example.ui.i18n.bidiLtr
import com.example.ui.i18n.formatNumber
import com.example.ui.i18n.localizeDigits
import com.example.ui.theme.LocalSyndicatTokens
import com.example.ui.viewmodel.SyndicatDashboardData
import com.example.ui.viewmodel.formatDh
import com.example.util.BackupManager
import com.example.util.ExpensePhotoManager
import com.example.util.ExportVariant
import com.example.util.InvoiceOcrHelper
import com.example.util.PdfPosterExporter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.launch

private enum class ExpenseSortMode {
  RECENT,
  UNPAID_DESC,
  AMOUNT_DESC,
}

private enum class ExpensesSubTab {
  EXPENSES,
  ABOS_VENDORS,
  CAISSE,
}

@Composable
fun ExpensesScreen(
  data: SyndicatDashboardData,
  onAddExpense: (
    title: String,
    category: String,
    amountDh: Long,
    paidAmountDh: Long,
    supplier: String,
    dateLabel: String,
    notes: String,
  ) -> Unit,
  onUpdateExpense: (ExpenseRecord) -> Unit,
  onDeleteExpense: (ExpenseRecord) -> Unit,
  onExportError: (String) -> Unit,
  onAddExpenseWithId: ((
    title: String,
    category: String,
    amountDh: Long,
    paidAmountDh: Long,
    supplier: String,
    dateLabel: String,
    notes: String,
    explicitId: String?,
  ) -> Unit)? = null,
  modifier: Modifier = Modifier,
  initialUnpaidOnly: Boolean = false,
  recurringTemplates: List<RecurringExpenseTemplate> = emptyList(),
  vendors: List<VendorRecord> = emptyList(),
  onSeedRecurringPresets: () -> Unit = {},
  onUpsertRecurring: (RecurringExpenseTemplate) -> Unit = {},
  onDeleteRecurring: (String) -> Unit = {},
  onMarkRecurringApplied: (Set<String>, String) -> Unit = { _, _ -> },
  onUpsertVendor: (VendorRecord) -> Unit = {},
  onDeleteVendor: (String) -> Unit = {},
  prefillIncidentDraft: IncidentRecord? = null,
  onIncidentConverted: (IncidentRecord, String) -> Unit = { _, _ -> },
  onConsumePrefillIncident: () -> Unit = {},
  onInfoMessage: (String) -> Unit = {},
  isReadOnlyMode: Boolean = false,
) {
  val context = LocalContext.current
  val haptics = LocalHapticFeedback.current
  val scope = rememberCoroutineScope()
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val isPrivacy = LocalPrivacyMode.current
  val cfg = data.config
  val cur = cfg.currency
  val dig = cfg.digitsAr
  val isAr = strings.language == AppLanguage.AR
  val numericFont = tokens.numericFontFamily ?: tokens.monoFontFamily

  var subTab by rememberSaveable { mutableStateOf(ExpensesSubTab.EXPENSES) }
  var selectedCategory by remember { mutableStateOf<String?>(null) }
  var unpaidOnlyFilter by rememberSaveable(initialUnpaidOnly) { mutableStateOf(initialUnpaidOnly) }
  var sortMode by rememberSaveable { mutableStateOf(ExpenseSortMode.RECENT) }
  var showAddModal by remember { mutableStateOf(false) }
  var editingExpense by remember { mutableStateOf<ExpenseRecord?>(null) }
  var armedDeleteId by remember { mutableStateOf<String?>(null) }
  var photoRevision by remember { mutableIntStateOf(0) }
  var previewPhotoExpense by remember { mutableStateOf<ExpenseRecord?>(null) }

  LaunchedEffect(initialUnpaidOnly) {
    if (initialUnpaidOnly) {
      unpaidOnlyFilter = true
      subTab = ExpensesSubTab.EXPENSES
    }
  }

  LaunchedEffect(prefillIncidentDraft) {
    if (prefillIncidentDraft != null && !isReadOnlyMode) {
      subTab = ExpensesSubTab.EXPENSES
      showAddModal = true
    }
  }

  val filteredExpenses = remember(data.expenses, selectedCategory, unpaidOnlyFilter, sortMode) {
    val base = data.expenses.filter { exp ->
      val catOk = selectedCategory == null || exp.category == selectedCategory
      val unpaidOk = !unpaidOnlyFilter || !exp.isFullyPaid
      catOk && unpaidOk
    }
    when (sortMode) {
      ExpenseSortMode.RECENT -> base
      ExpenseSortMode.UNPAID_DESC -> base.sortedByDescending { it.remainingToPayDh }
      ExpenseSortMode.AMOUNT_DESC -> base.sortedByDescending { it.amountDh }
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(tokens.screenBg)
      .testTag("expenses_screen"),
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      if (isReadOnlyMode) {
        ReadOnlyCouncilBanner(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
      }

      // Header + CSV shortcut + 3-Tab Meta Pill Switcher (Dépenses | Abos & Prestataires | Caisse)
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
            text = when (subTab) {
              ExpensesSubTab.EXPENSES -> strings.tabExpensesMode
              ExpensesSubTab.ABOS_VENDORS -> strings.tabAbosVendorsMode
              ExpensesSubTab.CAISSE -> strings.tabCaisseMode
            },
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Medium,
            color = tokens.inkDeep,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
          )
          IconButton(
            onClick = {
              val csv = BackupManager.generateExpensesCsv(data)
              BackupManager.shareCsvFile(
                context = context,
                filePrefix = "expenses_${cfg.year}",
                csvContent = csv,
                chooserTitle = strings.exportExpensesCsvBtn,
                onError = onExportError,
              )
            },
            modifier = Modifier
              .size(48.dp)
              .testTag("export_expenses_csv_top_btn"),
          ) {
            Icon(
              imageVector = Icons.Default.FileDownload,
              contentDescription = strings.exportExpensesCsvBtn,
              tint = tokens.inkDeep,
            )
          }
        }

        MetaSegmentedThreePillRow(
          labels = Triple(strings.tabExpensesMode, strings.tabAbosVendorsMode, strings.tabCaisseMode),
          selectedIndex = subTab.ordinal,
          onSelectIndex = { idx -> subTab = ExpensesSubTab.entries[idx] },
          tags = Triple("switch_expenses_mode", "switch_abos_vendors_mode", "switch_caisse_mode"),
          modifier = Modifier.fillMaxWidth(),
        )
      }

      when (subTab) {
        ExpensesSubTab.EXPENSES -> {
        // =========================================================================
        // MODE المصاريف (§6 : cartes de contenu 10dp bordées, montant Inter 500, barre 4dp ink sur surfaceStrong, 1 seul bouton primaire noir 12dp)
        // =========================================================================
        LazyRow(
          modifier = Modifier.testTag("expenses_filter_row"),
          contentPadding = PaddingValues(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          item {
            SyndicatFilterChip(
              selected = selectedCategory == null && !unpaidOnlyFilter,
              onClick = {
                selectedCategory = null
                unpaidOnlyFilter = false
              },
              label = strings.expFilterAll(data.expenses.size),
            )
          }
          item {
            SyndicatFilterChip(
              selected = unpaidOnlyFilter,
              onClick = { unpaidOnlyFilter = !unpaidOnlyFilter },
              label = strings.expFilterUnpaidOnly(data.unpaidExpensesCount),
              modifier = Modifier.testTag("expense_filter_unpaid"),
            )
          }
          item {
            SyndicatFilterChip(
              selected = sortMode == ExpenseSortMode.RECENT,
              onClick = { sortMode = ExpenseSortMode.RECENT },
              label = strings.expSortRecent,
              modifier = Modifier.testTag("exp_sort_recent"),
            )
          }
          item {
            SyndicatFilterChip(
              selected = sortMode == ExpenseSortMode.UNPAID_DESC,
              onClick = { sortMode = ExpenseSortMode.UNPAID_DESC },
              label = strings.expSortUnpaidDesc,
              modifier = Modifier.testTag("exp_sort_unpaid"),
            )
          }
          item {
            SyndicatFilterChip(
              selected = sortMode == ExpenseSortMode.AMOUNT_DESC,
              onClick = { sortMode = ExpenseSortMode.AMOUNT_DESC },
              label = strings.expSortAmountDesc,
              modifier = Modifier.testTag("exp_sort_amount"),
            )
          }
          items(EXPENSE_CATEGORIES) { cat ->
            val selected = selectedCategory == cat
            SyndicatFilterChip(
              selected = selected,
              onClick = { selectedCategory = if (selected) null else cat },
              label = strings.categoryLabel(cat),
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .testTag("expenses_list"),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          if (data.expenses.isNotEmpty()) {
            item(key = "expense_categories_donut") {
              ExpenseCategoriesBreakdownCard(
                expenses = data.expenses,
                currency = cur,
                digitsAr = dig,
                selectedCategory = selectedCategory,
                onSelectCategory = { selectedCategory = it },
              )
            }
          }

          if (filteredExpenses.isEmpty()) {
            item {
              SyndicatEmptyStateCard(
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                title = strings.expEmptyTitle,
                subtitle = strings.expEmptyTip,
                actionLabel = strings.expEmptyActionBtn,
                actionIcon = Icons.Default.Add,
                actionTag = "expenses_empty_add_btn",
                onAction = { showAddModal = true },
              )
            }
          } else {
            items(
              items = filteredExpenses,
              key = { it.id.ifBlank { "${it.title}_${it.dateLabel}" } },
            ) { exp ->
              val paid = exp.effectivePaidDh
              val rem = exp.remainingToPayDh
              val ratio = if (exp.amountDh > 0L) {
                (paid.toFloat() / exp.amountDh.toFloat()).coerceIn(0f, 1f)
              } else {
                1f
              }
              val isDeleteArmed = armedDeleteId == exp.id

              val thumbBitmap by produceState<ImageBitmap?>(
                initialValue = null,
                exp.id,
                photoRevision,
              ) {
                value = if (ExpensePhotoManager.hasPhoto(context, exp.id)) {
                  ExpensePhotoManager.loadBitmap(context, exp.id)
                } else {
                  null
                }
              }

              FintechWhiteCard(
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("expense_card_${exp.id}"),
                padding = 16.dp,
                onClick = { editingExpense = exp },
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.Top,
                ) {
                  if (thumbBitmap != null) {
                    Image(
                      bitmap = thumbBitmap!!,
                      contentDescription = strings.expPhotoPreviewTitle,
                      contentScale = ContentScale.Crop,
                      modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { previewPhotoExpense = exp }
                        .testTag("expense_thumb_${exp.id}"),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                  }

                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = exp.title,
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Medium,
                      color = tokens.ink,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val meta = buildList {
                      add(strings.categoryLabel(exp.category))
                      if (exp.supplier.isNotBlank()) add(exp.supplier)
                      if (exp.dateLabel.isNotBlank()) add(bidiLtr(localizeDigits(exp.dateLabel, dig)))
                    }.joinToString(" · ")
                    Text(
                      text = meta,
                      style = MaterialTheme.typography.bodySmall,
                      color = tokens.body,
                    )
                  }

                  Spacer(modifier = Modifier.width(8.dp))

                  Text(
                    text = maskSensitiveDh(formatDh(exp.amountDh, cur, dig), isPrivacy),
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = numericFont ?: MaterialTheme.typography.titleLarge.fontFamily,
                    fontWeight = FontWeight.Medium,
                    color = tokens.ink,
                  )
                }

                Spacer(modifier = Modifier.height(14.dp))

                SyndicatProgressBar(
                  progress = ratio,
                  modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = strings.expPaidProgressLabel(
                        maskSensitiveDh(formatDh(paid, cur, dig), isPrivacy),
                        maskSensitiveDh(formatDh(exp.amountDh, cur, dig), isPrivacy),
                      ),
                      style = MaterialTheme.typography.labelSmall,
                      color = tokens.body,
                    )
                    if (rem > 0L) {
                      Text(
                        text = strings.expSupplierRemaining(maskSensitiveDh(formatDh(rem, cur, dig), isPrivacy)),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = tokens.negativeDeep,
                      )
                    }
                  }

                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                  ) {
                    val hasPhoto = remember(exp.id, photoRevision) {
                      ExpensePhotoManager.hasPhoto(context, exp.id)
                    }
                    if (hasPhoto) {
                      Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = tokens.surfaceSoft,
                        border = BorderStroke(1.dp, tokens.divider),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                        modifier = Modifier
                          .minimumInteractiveComponentSize()
                          .heightIn(min = 48.dp)
                          .clip(RoundedCornerShape(100.dp))
                          .clickable { previewPhotoExpense = exp }
                          .testTag("expense_photo_badge_${exp.id}"),
                      ) {
                        Row(
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.spacedBy(4.dp),
                          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                          Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = strings.expPhotoBadgeLabel,
                            tint = tokens.infoColor,
                            modifier = Modifier.size(16.dp),
                          )
                          Text(
                            text = strings.expPhotoBadgeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = tokens.infoColor,
                          )
                        }
                      }
                    }
                    if (!isReadOnlyMode) {
                      IconButton(
                        onClick = { editingExpense = exp },
                        modifier = Modifier.size(48.dp),
                      ) {
                        Icon(
                          imageVector = Icons.Default.Edit,
                          contentDescription = strings.expDialogEdit,
                          tint = tokens.ink,
                        )
                      }

                      if (isDeleteArmed) {
                        val confirmShape = RoundedCornerShape(tokens.badgeRadiusDp.dp)
                        Surface(
                          shape = confirmShape,
                          color = tokens.negativeBg,
                          tonalElevation = 0.dp,
                          shadowElevation = 0.dp,
                          modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .heightIn(min = 48.dp)
                            .clip(confirmShape)
                            .clickable {
                              armedDeleteId = null
                              haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                              scope.launch { ExpensePhotoManager.deletePhoto(context, exp.id) }
                              onDeleteExpense(exp)
                            }
                            .testTag("confirm_delete_expense_${exp.id}"),
                        ) {
                          Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                          ) {
                            Text(
                              text = "! ${strings.settingsConfirmBtn}",
                              style = MaterialTheme.typography.labelSmall,
                              fontWeight = FontWeight.Medium,
                              color = Color.White,
                            )
                          }
                        }
                      } else {
                        IconButton(
                          onClick = { armedDeleteId = exp.id },
                          modifier = Modifier
                            .size(48.dp)
                            .testTag("delete_expense_${exp.id}"),
                        ) {
                          Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = strings.expDelete,
                            tint = tokens.negativeDeep,
                          )
                        }
                      }
                    }
                  }
                }
                if (rem > 0L && !isReadOnlyMode) {
                  Spacer(modifier = Modifier.height(8.dp))
                  SageSecondaryButton(
                    text = "${strings.expSettleRemainingBtn} (${maskSensitiveDh(formatDh(rem, cur, dig), isPrivacy)})",
                    icon = Icons.Default.DoneAll,
                    compact = true,
                    onClick = {
                      haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                      onUpdateExpense(exp.copy(paidAmountDh = exp.amountDh))
                    },
                    modifier = Modifier
                      .fillMaxWidth()
                      .testTag("exp_settle_btn_${exp.id}"),
                  )
                }
              }
            }
          }
        }
        }

        ExpensesSubTab.ABOS_VENDORS -> {
          RecurringAndVendorsTabContent(
            templates = recurringTemplates,
            vendors = vendors,
            expenses = data.expenses,
            currency = cur,
            digitsAr = dig,
            isAr = isAr,
            isReadOnlyMode = isReadOnlyMode,
            onSeedPresets = onSeedRecurringPresets,
            onGenerateCurrentMonth = {
              val monthKey = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
              val todayDate = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Date())
              val pending = recurringTemplates.filter { it.active && it.lastAppliedMonthKey != monthKey }
              if (pending.isEmpty()) {
                onInfoMessage(strings.recurringNonePendingMsg)
              } else {
                pending.forEach { tpl ->
                  onAddExpense(
                    tpl.title,
                    tpl.category,
                    tpl.amountDh,
                    tpl.amountDh,
                    tpl.supplier,
                    todayDate,
                    "Abonnement mensuel ($monthKey)",
                  )
                }
                onMarkRecurringApplied(pending.map { it.id }.toSet(), monthKey)
                onInfoMessage(strings.recurringGeneratedMsg(pending.size))
              }
            },
            onUpsertTemplate = onUpsertRecurring,
            onDeleteTemplate = onDeleteRecurring,
            onUpsertVendor = onUpsertVendor,
            onDeleteVendor = onDeleteVendor,
            onInfoMessage = onInfoMessage,
          )
        }

        ExpensesSubTab.CAISSE -> {
        // =========================================================================
        // MODE الصندوق (CAISSE : Carte Forêt #0A2E0E -> AirtableSoftCard #F8FAFC -> FintechWhiteCard)
        // =========================================================================
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 32.dp),
          verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
          item {
            AirtableSignatureCard(
              backgroundColor = tokens.signatureForest,
              modifier = Modifier.fillMaxWidth(),
              padding = 24.dp,
            ) {
              Text(
                text = strings.caisseAvailableTitle,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Normal,
                color = Color.White,
              )
              Spacer(modifier = Modifier.height(8.dp))
              val cashDisp = formatDh(data.cashNowDh, cur, dig)
              Text(
                text = maskSensitiveDh(cashDisp, isPrivacy),
                style = MaterialTheme.typography.displayLarge.copy(
                  fontFamily = numericFont ?: MaterialTheme.typography.displayLarge.fontFamily,
                  fontFeatureSettings = "tnum",
                ),
                fontWeight = FontWeight.Medium,
                color = Color.White,
              )
            }
          }

          item {
            AirtableSoftCard(
              modifier = Modifier.fillMaxWidth(),
              padding = 24.dp,
            ) {
              CaisseLedgerLine(strings.caisseOpeningBalance, maskSensitiveDh(formatDh(cfg.openingBalanceDh, cur, dig), isPrivacy), tokens.ink)
              CaisseLedgerLine(
                strings.caisseTotalReceipts,
                maskSensitiveDh(formatDh(data.totalCaisseDh, cur, dig, explicitPlus = true), isPrivacy),
                tokens.positiveDeep,
              )
              CaisseLedgerLine(
                strings.caissePaidToSuppliers,
                maskSensitiveDh(formatDh(-data.totalExpensesPaidDh, cur, dig), isPrivacy),
                tokens.negativeDeep,
              )
              HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = tokens.divider)
              CaisseLedgerLine(strings.caisseTotalExpenses, maskSensitiveDh(formatDh(data.totalExpensesDh, cur, dig), isPrivacy), tokens.body)
              CaisseLedgerLine(
                strings.caisseUnpaidSuppliers,
                maskSensitiveDh(formatDh(data.unpaidExpensesDh, cur, dig), isPrivacy),
                if (data.unpaidExpensesDh > 0L) tokens.negativeDeep else tokens.ink,
              )
              val balText = formatDh(data.balanceDh, cur, dig)
              CaisseLedgerLine(
                strings.caisseAccountingBalance,
                maskSensitiveDh(balText, isPrivacy),
                if (data.balanceDh >= 0L) tokens.positiveDeep else tokens.negativeDeep,
              )

              Spacer(modifier = Modifier.height(16.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                // Meta button-buy-cta (#0064E0 Cobalt pill for Caisse report export)
                MetaCobaltBuyButton(
                  text = strings.exportPdfPrimaryBtn,
                  icon = Icons.Default.PictureAsPdf,
                  compact = true,
                  onClick = {
                    PdfPosterExporter.shareCaisseReportPdf(context, data, false, onExportError, ExportVariant.PRINT)
                  },
                  modifier = Modifier.weight(1f).testTag("caisse_export_pdf_btn"),
                )
                SageSecondaryButton(
                  text = strings.exportPngSecondaryBtn,
                  icon = Icons.Default.Image,
                  compact = true,
                  onClick = {
                    PdfPosterExporter.shareCaisseReportImage(context, data, false, onExportError, ExportVariant.PRINT)
                  },
                  modifier = Modifier.weight(1f).testTag("caisse_export_png_btn"),
                )
              }
            }
          }

          // Encaissements mensuels (FintechWhiteCard — alternance propre après AirtableSoftCard)
          item {
            FintechWhiteCard(
              modifier = Modifier.fillMaxWidth(),
              padding = 24.dp,
            ) {
              Text(
                text = strings.caisseMovementsTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Normal,
                color = tokens.ink,
              )
              Spacer(modifier = Modifier.height(12.dp))

              val monthTotals = cfg.activeMonthColumns.map { col ->
                col to data.apartments.sumOf { it.amountFor(col) }
              }
              val peak = (monthTotals.maxOfOrNull { it.second } ?: 1L).coerceAtLeast(1L)

              monthTotals.forEach { (col, amt) ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                  Text(
                    text = strings.monthShort(col),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = tokens.ink,
                    modifier = Modifier.width(56.dp),
                  )
                  SyndicatProgressBar(
                    progress = (amt.toFloat() / peak.toFloat()).coerceIn(0f, 1f),
                    modifier = Modifier.weight(1f),
                  )
                  Text(
                    text = formatNumber(amt, dig),
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = numericFont ?: MaterialTheme.typography.labelMedium.fontFamily,
                    fontWeight = FontWeight.Medium,
                    color = tokens.positiveDeep,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(64.dp),
                  )
                }
              }
            }
          }
        }
        } // end when subTab
      }
    }

    // Meta button-primary (#000000 black pill {rounded.full} 100dp, zero drop shadow)
    if (subTab == ExpensesSubTab.EXPENSES && !isReadOnlyMode) {
      ExtendedFloatingActionButton(
        onClick = { showAddModal = true },
        containerColor = tokens.primary,
        contentColor = tokens.onPrimary,
        shape = RoundedCornerShape(100.dp),
        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
        icon = { Icon(Icons.Default.Add, contentDescription = null) },
        text = {
          Text(
            text = strings.expAddFab,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
          )
        },
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(16.dp)
          .testTag("add_expense_fab"),
      )
    }
  }

  if (showAddModal && !isReadOnlyMode) {
    val draftFromIncident = prefillIncidentDraft?.let { inc ->
      ExpenseRecord(
        id = "",
        title = inc.title,
        category = "Maintenance",
        amountDh = 0L,
        paidAmountDh = 0L,
        supplier = "",
        dateLabel = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Date()),
        notes = inc.description,
      )
    }
    ExpenseEditorModal(
      initial = draftFromIncident,
      isNewFromDraft = prefillIncidentDraft != null,
      currency = cur,
      onPhotoChanged = { photoRevision++ },
      onDismiss = {
        showAddModal = false
        if (prefillIncidentDraft != null) onConsumePrefillIncident()
      },
      onConfirm = { title, cat, amount, paid, supplier, date, notes, expenseId ->
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        if (onAddExpenseWithId != null) {
          onAddExpenseWithId(title, cat, amount, paid, supplier, date, notes, expenseId)
        } else {
          onAddExpense(title, cat, amount, paid, supplier, date, notes)
        }
        if (prefillIncidentDraft != null) {
          onIncidentConverted(prefillIncidentDraft, expenseId)
          onConsumePrefillIncident()
        }
        photoRevision++
        showAddModal = false
      },
    )
  }

  editingExpense?.let { exp ->
    ExpenseEditorModal(
      initial = exp,
      currency = cur,
      onPhotoChanged = { photoRevision++ },
      onDismiss = { editingExpense = null },
      onConfirm = { title, cat, amount, paid, supplier, date, notes, _ ->
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        onUpdateExpense(
          exp.copy(
            title = title,
            category = cat,
            amountDh = amount,
            paidAmountDh = paid,
            supplier = supplier,
            dateLabel = date,
            notes = notes,
          )
        )
        photoRevision++
        editingExpense = null
      },
    )
  }

  previewPhotoExpense?.let { targetExp ->
    ExpensePhotoPreviewDialog(
      expense = targetExp,
      onDismiss = { previewPhotoExpense = null },
    )
  }
}

@Composable
private fun CaisseLedgerLine(
  label: String,
  value: String,
  valueColor: Color,
) {
  val tokens = LocalSyndicatTokens.current
  val numericFont = tokens.numericFontFamily ?: tokens.monoFontFamily
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 5.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      color = tokens.body,
      modifier = Modifier.weight(1f),
    )
    Text(
      text = value,
      style = MaterialTheme.typography.titleMedium,
      fontFamily = numericFont ?: MaterialTheme.typography.titleMedium.fontFamily,
      fontWeight = FontWeight.Medium,
      color = valueColor,
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExpenseEditorModal(
  initial: ExpenseRecord?,
  currency: String,
  isNewFromDraft: Boolean = false,
  onPhotoChanged: () -> Unit,
  onDismiss: () -> Unit,
  onConfirm: (
    title: String,
    category: String,
    amountDh: Long,
    paidAmountDh: Long,
    supplier: String,
    dateLabel: String,
    notes: String,
    expenseId: String,
  ) -> Unit,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val today = remember { SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date()) }
  val isEditingExisting = initial != null && !isNewFromDraft
  val targetExpenseId = remember(initial) {
    initial?.id?.takeIf { it.isNotBlank() } ?: "exp_${UUID.randomUUID().toString().take(8)}"
  }

  var title by remember(initial) { mutableStateOf(initial?.title.orEmpty()) }
  var category by remember(initial) { mutableStateOf(initial?.category ?: EXPENSE_CATEGORIES.first()) }
  var supplier by remember(initial) { mutableStateOf(initial?.supplier.orEmpty()) }
  var amountText by remember(initial) { mutableStateOf(initial?.amountDh?.takeIf { it > 0L }?.toString().orEmpty()) }
  var paidText by remember(initial) {
    mutableStateOf(initial?.effectivePaidDh?.takeIf { it > 0L }?.toString().orEmpty())
  }
  var paidManuallyEdited by remember(initial) {
    mutableStateOf(isEditingExisting && initial != null && !initial.isFullyPaid)
  }
  var dateLabel by remember(initial) { mutableStateOf(initial?.dateLabel?.ifBlank { today } ?: today) }
  var notes by remember(initial) { mutableStateOf(initial?.notes.orEmpty()) }
  var errorMsg by remember { mutableStateOf<String?>(null) }
  var ocrStatusMsg by remember { mutableStateOf<String?>(null) }
  var photoBitmap by remember(targetExpenseId) { mutableStateOf<ImageBitmap?>(null) }

  LaunchedEffect(targetExpenseId) {
    photoBitmap = ExpensePhotoManager.loadBitmap(context, targetExpenseId)
  }

  val photoPicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia(),
  ) { uri ->
    if (uri != null) {
      scope.launch {
        if (ExpensePhotoManager.savePhotoFromUri(context, targetExpenseId, uri)) {
          photoBitmap = ExpensePhotoManager.loadBitmap(context, targetExpenseId)
          onPhotoChanged()
        }
      }
    }
  }

  val ocrPhotoPicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia(),
  ) { uri ->
    if (uri != null) {
      ocrStatusMsg = strings.ocrScanningMsg
      scope.launch {
        if (ExpensePhotoManager.savePhotoFromUri(context, targetExpenseId, uri)) {
          photoBitmap = ExpensePhotoManager.loadBitmap(context, targetExpenseId)
          onPhotoChanged()
        }
        val res = InvoiceOcrHelper.recognizeInvoiceFromUri(context, uri)
        val hasUseful = res.detectedAmountDh != null || res.detectedDate != null || !res.detectedSupplier.isNullOrBlank()
        res.detectedAmountDh?.let { amt ->
          amountText = amt.toString()
          if (!paidManuallyEdited) paidText = amt.toString()
        }
        res.detectedDate?.let { d -> dateLabel = d }
        res.detectedSupplier?.let { sup ->
          if (supplier.isBlank()) supplier = sup
          if (title.isBlank()) title = sup
        }
        ocrStatusMsg = if (hasUseful) strings.ocrSuccessMsg else strings.ocrNoTextMsg
      }
    }
  }

  val handleDismiss: () -> Unit = {
    if (!isEditingExisting && photoBitmap != null) {
      scope.launch { ExpensePhotoManager.deletePhoto(context, targetExpenseId) }
    }
    onDismiss()
  }

  SyndicatDialog(
    onDismissRequest = handleDismiss,
    title = {
      Text(
        text = if (!isEditingExisting) strings.expDialogAdd else strings.expDialogEdit,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Normal,
        color = tokens.ink,
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        SageSecondaryButton(
          text = strings.ocrScanInvoiceBtn,
          icon = Icons.Default.QrCodeScanner,
          compact = true,
          onClick = {
            ocrPhotoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
          },
          modifier = Modifier.fillMaxWidth().testTag("ocr_scan_invoice_btn"),
        )
        ocrStatusMsg?.let { status ->
          Text(
            text = status,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = tokens.linkColor,
            modifier = Modifier.testTag("ocr_status_text"),
          )
        }
        FintechTextField(
          value = title,
          onValueChange = {
            title = it.take(140)
            errorMsg = null
          },
          label = strings.expFieldTitle,
          placeholder = strings.expFieldTitleHint,
          errorText = if (errorMsg == strings.expErrTitle) errorMsg else null,
          modifier = Modifier.fillMaxWidth().testTag("expense_title_input"),
        )

        FintechTextField(
          value = amountText,
          onValueChange = { next ->
            val clean = next.filter { it.isDigit() }.take(8)
            amountText = clean
            if (!paidManuallyEdited) {
              paidText = clean
            }
            errorMsg = null
          },
          label = strings.expFieldAmount(currency),
          placeholder = "500",
          keyboardType = KeyboardType.Number,
          errorText = if (errorMsg == strings.expErrAmount) errorMsg else null,
          modifier = Modifier.fillMaxWidth().testTag("expense_amount_input"),
        )

        FintechTextField(
          value = paidText,
          onValueChange = { next ->
            paidManuallyEdited = true
            paidText = next.filter { it.isDigit() }.take(8)
            errorMsg = null
          },
          label = strings.expFieldPaidAmount(currency),
          placeholder = amountText.ifBlank { "500" },
          keyboardType = KeyboardType.Number,
          errorText = if (errorMsg == strings.expErrPaidExceedsAmount) errorMsg else null,
          modifier = Modifier.fillMaxWidth().testTag("expense_paid_amount_input"),
        )

        FintechTextField(
          value = supplier,
          onValueChange = { supplier = it.take(100) },
          label = strings.expFieldSupplier,
          placeholder = strings.expFieldSupplierHint,
          modifier = Modifier.fillMaxWidth().testTag("expense_supplier_input"),
        )

        FintechTextField(
          value = dateLabel,
          onValueChange = {
            dateLabel = it.take(25)
            errorMsg = null
          },
          label = strings.expFieldDate,
          keyboardType = KeyboardType.Number,
          errorText = if (errorMsg == strings.expErrDate) errorMsg else null,
          modifier = Modifier.fillMaxWidth().testTag("expense_date_input"),
        )

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = strings.expFieldCategory,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = tokens.body,
          )
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            EXPENSE_CATEGORIES.forEach { cat ->
              SyndicatFilterChip(
                selected = category == cat,
                onClick = { category = cat },
                label = strings.categoryLabel(cat),
                modifier = Modifier.testTag("expense_category_$cat"),
              )
            }
          }
        }

        // Justificatif photo (Android Photo Picker sans permission)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          val bmp = photoBitmap
          if (bmp != null) {
            Image(
              bitmap = bmp,
              contentDescription = strings.expPhotoPreviewTitle,
              contentScale = ContentScale.Crop,
              modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(12.dp)),
            )
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              SageSecondaryButton(
                text = strings.expPhotoChangeBtn,
                icon = Icons.Default.AddAPhoto,
                compact = true,
                onClick = {
                  photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                modifier = Modifier.weight(1f).testTag("change_expense_photo_btn"),
              )
              SageSecondaryButton(
                text = strings.expPhotoRemoveBtn,
                icon = Icons.Default.DeleteOutline,
                compact = true,
                onClick = {
                  scope.launch {
                    ExpensePhotoManager.deletePhoto(context, targetExpenseId)
                    photoBitmap = null
                    onPhotoChanged()
                  }
                },
                modifier = Modifier.testTag("remove_expense_photo_btn"),
              )
            }
          } else {
            SageSecondaryButton(
              text = strings.expPhotoAttachBtn,
              icon = Icons.Default.AddAPhoto,
              compact = true,
              onClick = {
                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
              },
              modifier = Modifier.fillMaxWidth().testTag("attach_expense_photo_btn"),
            )
          }
        }

        FintechTextField(
          value = notes,
          onValueChange = { notes = it.take(280) },
          label = strings.expFieldNotes,
          singleLine = false,
          minLines = 2,
          maxLines = 4,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    },
    confirmButton = {
      MetaCobaltBuyButton(
        text = strings.apptSave,
        compact = true,
        onClick = {
          val amt = amountText.toLongOrNull() ?: 0L
          val rawPaid = if (paidText.isBlank()) amt else (paidText.toLongOrNull() ?: 0L)
          when {
            title.isBlank() -> errorMsg = strings.expErrTitle
            amt <= 0L -> errorMsg = strings.expErrAmount
            rawPaid > amt -> errorMsg = strings.expErrPaidExceedsAmount
            dateLabel.isBlank() -> errorMsg = strings.expErrDate
            else -> onConfirm(title.trim(), category, amt, rawPaid, supplier.trim(), dateLabel.trim(), notes.trim(), targetExpenseId)
          }
        },
        modifier = Modifier.testTag("save_expense_button"),
      )
    },
    dismissButton = {
      SageSecondaryButton(
        text = strings.apptCancel,
        compact = true,
        onClick = handleDismiss,
      )
    },
  )
}

@Composable
private fun ExpensePhotoPreviewDialog(
  expense: ExpenseRecord,
  onDismiss: () -> Unit,
) {
  val context = LocalContext.current
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  var bmp by remember(expense.id) { mutableStateOf<ImageBitmap?>(null) }

  LaunchedEffect(expense.id) {
    bmp = ExpensePhotoManager.loadBitmap(context, expense.id)
  }

  SyndicatDialog(
    onDismissRequest = onDismiss,
    title = {
      Column {
        Text(
          text = strings.expPhotoPreviewTitle,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Normal,
          color = tokens.ink,
        )
        Text(
          text = expense.title,
          style = MaterialTheme.typography.bodySmall,
          color = tokens.body,
        )
      }
    },
    text = {
      val image = bmp
      if (image != null) {
        Image(
          bitmap = image,
          contentDescription = expense.title,
          contentScale = ContentScale.Fit,
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 380.dp)
            .clip(RoundedCornerShape(12.dp)),
        )
      } else {
        Text(
          text = "—",
          style = MaterialTheme.typography.bodyMedium,
          color = tokens.mute,
        )
      }
    },
    confirmButton = {
      SageSecondaryButton(
        text = strings.apptCancel,
        compact = true,
        onClick = onDismiss,
      )
    },
  )
}
