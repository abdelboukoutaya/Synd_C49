package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.EXPENSE_CATEGORIES
import com.example.data.model.ExpenseRecord
import com.example.ui.components.AirtableSignatureCard
import com.example.ui.components.AirtableSoftCard
import com.example.ui.components.FintechTextField
import com.example.ui.components.FintechWhiteCard
import com.example.ui.components.MetaCobaltBuyButton
import com.example.ui.components.MetaSegmentedPillRow
import com.example.ui.components.SageSecondaryButton
import com.example.ui.components.SyndicatDialog
import com.example.ui.components.SyndicatFilterChip
import com.example.ui.components.SyndicatProgressBar
import com.example.ui.i18n.LocalStrings
import com.example.ui.i18n.bidiLtr
import com.example.ui.i18n.formatNumber
import com.example.ui.i18n.localizeDigits
import com.example.ui.theme.LocalSyndicatTokens
import com.example.ui.viewmodel.SyndicatDashboardData
import com.example.ui.viewmodel.formatDh
import com.example.util.ExportVariant
import com.example.util.PdfPosterExporter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val cfg = data.config
  val cur = cfg.currency
  val dig = cfg.digitsAr
  val numericFont = tokens.numericFontFamily ?: tokens.monoFontFamily

  var isCaisseMode by remember { mutableStateOf(false) }
  var selectedCategory by remember { mutableStateOf<String?>(null) }
  var showAddModal by remember { mutableStateOf(false) }
  var editingExpense by remember { mutableStateOf<ExpenseRecord?>(null) }
  var armedDeleteId by remember { mutableStateOf<String?>(null) }

  val filteredExpenses = remember(data.expenses, selectedCategory) {
    if (selectedCategory == null) data.expenses
    else data.expenses.filter { it.category == selectedCategory }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(tokens.screenBg)
      .testTag("expenses_screen"),
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Header + Meta Segmented Pill Switcher (المصاريف | الصندوق — {rounded.full} 100dp pills)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = if (isCaisseMode) strings.tabCaisseMode else strings.tabExpensesMode,
          style = MaterialTheme.typography.headlineLarge,
          fontWeight = FontWeight.Medium,
          color = tokens.inkDeep,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(8.dp))

        MetaSegmentedPillRow(
          firstLabel = strings.tabExpensesMode,
          secondLabel = strings.tabCaisseMode,
          isFirstSelected = !isCaisseMode,
          onSelectFirst = { isCaisseMode = false },
          onSelectSecond = { isCaisseMode = true },
          firstTag = "switch_expenses_mode",
          secondTag = "switch_caisse_mode",
        )
      }

      if (!isCaisseMode) {
        // =========================================================================
        // MODE المصاريف (§6 : cartes de contenu 10dp bordées, montant Inter 500, barre 4dp ink sur surfaceStrong, 1 seul bouton primaire noir 12dp)
        // =========================================================================
        LazyRow(
          contentPadding = PaddingValues(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          item {
            SyndicatFilterChip(
              selected = selectedCategory == null,
              onClick = { selectedCategory = null },
              label = strings.expFilterAll(data.expenses.size),
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
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          if (filteredExpenses.isEmpty()) {
            item {
              FintechWhiteCard(
                modifier = Modifier.fillMaxWidth(),
                padding = 28.dp,
              ) {
                Column(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                    contentDescription = null,
                    tint = tokens.mute,
                    modifier = Modifier.size(44.dp),
                  )
                  Spacer(modifier = Modifier.height(10.dp))
                  Text(
                    text = strings.expEmptyTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Normal,
                    color = tokens.ink,
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = strings.expEmptyTip,
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.body,
                  )
                }
              }
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

                  Text(
                    text = formatDh(exp.amountDh, cur, dig),
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
                    val paidPrefix = if (rem > 0L) "◐ " else "✓ "
                    Text(
                      text = "$paidPrefix${strings.expPaidProgressLabel(
                        formatDh(paid, cur, dig),
                        formatDh(exp.amountDh, cur, dig),
                      )}",
                      style = MaterialTheme.typography.labelSmall,
                      color = tokens.body,
                    )
                    if (rem > 0L) {
                      Text(
                        text = "! ${strings.expSupplierRemaining(formatDh(rem, cur, dig))}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = tokens.negativeDeep,
                      )
                    }
                  }

                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                  ) {
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
            }
          }
        }
      } else {
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
              val cashDisp = if (data.cashNowDh < 0L) {
                "▼ ${formatDh(data.cashNowDh, cur, dig)}"
              } else {
                formatDh(data.cashNowDh, cur, dig)
              }
              Text(
                text = cashDisp,
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
              CaisseLedgerLine(strings.caisseOpeningBalance, formatDh(cfg.openingBalanceDh, cur, dig), tokens.ink)
              CaisseLedgerLine(
                strings.caisseTotalReceipts,
                formatDh(data.totalCaisseDh, cur, dig, explicitPlus = true),
                tokens.positiveDeep,
              )
              CaisseLedgerLine(
                strings.caissePaidToSuppliers,
                "${if (data.totalExpensesPaidDh > 0L) "▼ " else ""}${formatDh(-data.totalExpensesPaidDh, cur, dig)}",
                tokens.negativeDeep,
              )
              HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = tokens.divider)
              CaisseLedgerLine(strings.caisseTotalExpenses, formatDh(data.totalExpensesDh, cur, dig), tokens.body)
              CaisseLedgerLine(
                strings.caisseUnpaidSuppliers,
                "${if (data.unpaidExpensesDh > 0L) "◐ " else ""}${formatDh(data.unpaidExpensesDh, cur, dig)}",
                tokens.ink,
              )
              val balText = if (data.balanceDh < 0L) {
                "▼ ${formatDh(data.balanceDh, cur, dig)}"
              } else {
                formatDh(data.balanceDh, cur, dig)
              }
              CaisseLedgerLine(
                strings.caisseAccountingBalance,
                balText,
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
      }
    }

    // Meta button-primary (#000000 black pill {rounded.full} 100dp, zero drop shadow)
    if (!isCaisseMode) {
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

  if (showAddModal) {
    ExpenseEditorModal(
      initial = null,
      currency = cur,
      onDismiss = { showAddModal = false },
      onConfirm = { title, cat, amount, paid, supplier, date, notes ->
        onAddExpense(title, cat, amount, paid, supplier, date, notes)
        showAddModal = false
      },
    )
  }

  editingExpense?.let { exp ->
    ExpenseEditorModal(
      initial = exp,
      currency = cur,
      onDismiss = { editingExpense = null },
      onConfirm = { title, cat, amount, paid, supplier, date, notes ->
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
        editingExpense = null
      },
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
  onDismiss: () -> Unit,
  onConfirm: (
    title: String,
    category: String,
    amountDh: Long,
    paidAmountDh: Long,
    supplier: String,
    dateLabel: String,
    notes: String,
  ) -> Unit,
) {
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val today = remember { SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date()) }

  var title by remember(initial) { mutableStateOf(initial?.title.orEmpty()) }
  var category by remember(initial) { mutableStateOf(initial?.category ?: EXPENSE_CATEGORIES.first()) }
  var supplier by remember(initial) { mutableStateOf(initial?.supplier.orEmpty()) }
  var amountText by remember(initial) { mutableStateOf(initial?.amountDh?.toString().orEmpty()) }
  var paidText by remember(initial) {
    mutableStateOf(initial?.effectivePaidDh?.toString().orEmpty())
  }
  var paidManuallyEdited by remember(initial) {
    mutableStateOf(initial != null && !initial.isFullyPaid)
  }
  var dateLabel by remember(initial) { mutableStateOf(initial?.dateLabel?.ifBlank { today } ?: today) }
  var notes by remember(initial) { mutableStateOf(initial?.notes.orEmpty()) }
  var errorMsg by remember { mutableStateOf<String?>(null) }

  SyndicatDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (initial == null) strings.expDialogAdd else strings.expDialogEdit,
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
            else -> onConfirm(title.trim(), category, amt, rawPaid, supplier.trim(), dateLabel.trim(), notes.trim())
          }
        },
        modifier = Modifier.testTag("save_expense_button"),
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
