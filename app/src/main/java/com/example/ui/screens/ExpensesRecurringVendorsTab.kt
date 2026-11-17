package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.RecurringExpenseTemplate
import com.example.data.local.VendorRecord
import com.example.data.model.EXPENSE_CATEGORIES
import com.example.data.model.ExpenseRecord
import com.example.ui.components.AirtableSoftCard
import com.example.ui.components.FintechTextField
import com.example.ui.components.FintechWhiteCard
import com.example.ui.components.LocalPrivacyMode
import com.example.ui.components.MetaCobaltBuyButton
import com.example.ui.components.SageSecondaryButton
import com.example.ui.components.SyndicatDialog
import com.example.ui.components.SyndicatFilterChip
import com.example.ui.components.maskSensitiveDh
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.LocalSyndicatTokens
import com.example.ui.viewmodel.formatDh
import com.example.util.WhatsAppHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecurringAndVendorsTabContent(
  templates: List<RecurringExpenseTemplate>,
  vendors: List<VendorRecord>,
  expenses: List<ExpenseRecord>,
  currency: String,
  digitsAr: Boolean,
  isAr: Boolean,
  isReadOnlyMode: Boolean,
  onSeedPresets: () -> Unit,
  onGenerateCurrentMonth: () -> Unit,
  onUpsertTemplate: (RecurringExpenseTemplate) -> Unit,
  onDeleteTemplate: (String) -> Unit,
  onUpsertVendor: (VendorRecord) -> Unit,
  onDeleteVendor: (String) -> Unit,
  onInfoMessage: (String) -> Unit,
) {
  val context = LocalContext.current
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val isPrivacy = LocalPrivacyMode.current
  val currentMonthKey = remember { SimpleDateFormat("yyyy-MM", Locale.US).format(Date()) }

  var showTemplateModal by remember { mutableStateOf(false) }
  var editingTemplate by remember { mutableStateOf<RecurringExpenseTemplate?>(null) }
  var showVendorModal by remember { mutableStateOf(false) }
  var editingVendor by remember { mutableStateOf<VendorRecord?>(null) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("abos_vendors_list"),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    // 1. Dépenses récurrentes en 1 clic
    item(key = "recurring_header_card") {
      FintechWhiteCard(modifier = Modifier.fillMaxWidth(), padding = 18.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Icon(
              imageVector = Icons.Default.EventRepeat,
              contentDescription = null,
              tint = tokens.linkColor,
            )
            Text(
              text = strings.recurringSectionTitle,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = tokens.inkDeep,
              modifier = Modifier.weight(1f),
            )
          }

          if (!isReadOnlyMode) {
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              if (templates.isNotEmpty()) {
                MetaCobaltBuyButton(
                  text = strings.recurringGenerateMonthBtn,
                  icon = Icons.Default.AutoAwesome,
                  compact = true,
                  onClick = onGenerateCurrentMonth,
                  modifier = Modifier.testTag("generate_recurring_month_btn"),
                )
              }
              SageSecondaryButton(
                text = strings.recurringAddBtn,
                icon = Icons.Default.Add,
                compact = true,
                onClick = {
                  editingTemplate = null
                  showTemplateModal = true
                },
                modifier = Modifier.testTag("add_recurring_btn"),
              )
              if (templates.isEmpty()) {
                SageSecondaryButton(
                  text = strings.recurringPresetsBtn,
                  icon = Icons.Default.AutoAwesome,
                  compact = true,
                  onClick = onSeedPresets,
                  modifier = Modifier.testTag("seed_recurring_presets_btn"),
                )
              }
            }
          }

          if (templates.isEmpty()) {
            Text(
              text = strings.recurringEmptyHint,
              style = MaterialTheme.typography.bodySmall,
              color = tokens.body,
            )
          } else {
            templates.forEach { tpl ->
              val alreadyDone = tpl.lastAppliedMonthKey == currentMonthKey
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = tokens.surfaceSoft,
                border = BorderStroke(1.dp, tokens.divider),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("recurring_card_${tpl.id}"),
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = tpl.title,
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.SemiBold,
                      color = tokens.inkDeep,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                      text = "${strings.categoryLabel(tpl.category)} · ${maskSensitiveDh(formatDh(tpl.amountDh, currency, digitsAr), isPrivacy)}${if (alreadyDone) " · ✓" else ""}",
                      style = MaterialTheme.typography.bodySmall,
                      color = if (alreadyDone) tokens.positiveDeep else tokens.body,
                    )
                  }
                  if (!isReadOnlyMode) {
                    Switch(
                      checked = tpl.active,
                      onCheckedChange = { onUpsertTemplate(tpl.copy(active = it)) },
                    )
                    IconButton(
                      onClick = {
                        editingTemplate = tpl
                        showTemplateModal = true
                      },
                      modifier = Modifier.size(48.dp),
                    ) {
                      Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = strings.expDialogEdit,
                        tint = tokens.ink,
                      )
                    }
                    IconButton(
                      onClick = { onDeleteTemplate(tpl.id) },
                      modifier = Modifier.size(48.dp),
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
    }

    // 2. Carnet des Prestataires & Contrats
    item(key = "vendors_header_card") {
      AirtableSoftCard(modifier = Modifier.fillMaxWidth(), padding = 18.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Row(
              modifier = Modifier.weight(1f),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Icon(
                imageVector = Icons.Default.Storefront,
                contentDescription = null,
                tint = tokens.inkDeep,
              )
              Text(
                text = strings.vendorsSectionTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = tokens.inkDeep,
              )
            }
            if (!isReadOnlyMode) {
              SageSecondaryButton(
                text = strings.vendorAddBtn,
                icon = Icons.Default.Add,
                compact = true,
                onClick = {
                  editingVendor = null
                  showVendorModal = true
                },
                modifier = Modifier.testTag("add_vendor_btn"),
              )
            }
          }

          if (vendors.isEmpty()) {
            Text(
              text = strings.vendorEmptyHint,
              style = MaterialTheme.typography.bodySmall,
              color = tokens.body,
            )
          }
        }
      }
    }

    items(vendors, key = { it.id }) { vendor ->
      val ytdPaid = remember(expenses, vendor.name) {
        expenses
          .filter { it.supplier.equals(vendor.name, ignoreCase = true) }
          .sumOf { it.effectivePaidDh }
      }
      FintechWhiteCard(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("vendor_card_${vendor.id}"),
        padding = 16.dp,
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = vendor.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = tokens.inkDeep,
              )
              if (vendor.specialty.isNotBlank()) {
                Text(
                  text = vendor.specialty,
                  style = MaterialTheme.typography.labelLarge,
                  color = tokens.linkColor,
                )
              }
            }
            if (vendor.monthlyFeeDh > 0L) {
              Text(
                text = "${maskSensitiveDh(formatDh(vendor.monthlyFeeDh, currency, digitsAr), isPrivacy)}/m",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = tokens.inkDeep,
              )
            }
          }

          if (vendor.contractEndDate.isNotBlank()) {
            Text(
              text = "${strings.vendorContractEndLabel} : ${vendor.contractEndDate}",
              style = MaterialTheme.typography.bodySmall,
              color = tokens.body,
            )
          }

          Text(
            text = strings.vendorPaidYtdLabel(maskSensitiveDh(formatDh(ytdPaid, currency, digitsAr), isPrivacy)),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = tokens.positiveDeep,
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            if (vendor.phone.isNotBlank()) {
              SageSecondaryButton(
                text = vendor.phone,
                icon = Icons.Default.Call,
                compact = true,
                onClick = {
                  runCatching {
                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${vendor.phone}")).apply {
                      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(dialIntent)
                  }.onFailure {
                    onInfoMessage(it.localizedMessage ?: "Appel indisponible")
                  }
                },
                modifier = Modifier.weight(1f),
              )
              IconButton(
                onClick = {
                  WhatsAppHelper.openRawWhatsAppMessage(
                    context = context,
                    rawPhone = vendor.phone,
                    message = "Bonjour ${vendor.name},",
                    onError = onInfoMessage,
                  )
                },
                modifier = Modifier.size(48.dp),
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.Send,
                  contentDescription = "WhatsApp ${vendor.name}",
                  tint = tokens.positiveDeep,
                )
              }
            } else {
              Spacer(modifier = Modifier.weight(1f))
            }

            if (!isReadOnlyMode) {
              IconButton(
                onClick = {
                  editingVendor = vendor
                  showVendorModal = true
                },
                modifier = Modifier.size(48.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.Edit,
                  contentDescription = strings.expDialogEdit,
                  tint = tokens.ink,
                )
              }
              IconButton(
                onClick = { onDeleteVendor(vendor.id) },
                modifier = Modifier.size(48.dp),
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

  if (showTemplateModal) {
    RecurringTemplateEditorDialog(
      initial = editingTemplate,
      currency = currency,
      onDismiss = { showTemplateModal = false },
      onSave = { tpl ->
        onUpsertTemplate(tpl)
        showTemplateModal = false
      },
    )
  }

  if (showVendorModal) {
    VendorEditorDialog(
      initial = editingVendor,
      currency = currency,
      onDismiss = { showVendorModal = false },
      onSave = { v ->
        onUpsertVendor(v)
        showVendorModal = false
      },
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecurringTemplateEditorDialog(
  initial: RecurringExpenseTemplate?,
  currency: String,
  onDismiss: () -> Unit,
  onSave: (RecurringExpenseTemplate) -> Unit,
) {
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  var title by remember(initial) { mutableStateOf(initial?.title.orEmpty()) }
  var category by remember(initial) { mutableStateOf(initial?.category ?: EXPENSE_CATEGORIES.first()) }
  var supplier by remember(initial) { mutableStateOf(initial?.supplier.orEmpty()) }
  var amountText by remember(initial) { mutableStateOf(initial?.amountDh?.takeIf { it > 0L }?.toString().orEmpty()) }

  SyndicatDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = strings.recurringAddBtn,
        style = MaterialTheme.typography.titleLarge,
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
          value = title,
          onValueChange = { title = it.take(120) },
          label = strings.expFieldTitle,
          modifier = Modifier.fillMaxWidth().testTag("recurring_title_input"),
        )
        FintechTextField(
          value = amountText,
          onValueChange = { amountText = it.filter { c -> c.isDigit() }.take(7) },
          label = strings.expFieldAmount(currency),
          keyboardType = KeyboardType.Number,
          modifier = Modifier.fillMaxWidth().testTag("recurring_amount_input"),
        )
        FintechTextField(
          value = supplier,
          onValueChange = { supplier = it.take(100) },
          label = strings.expFieldSupplier,
          modifier = Modifier.fillMaxWidth(),
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
            )
          }
        }
      }
    },
    confirmButton = {
      MetaCobaltBuyButton(
        text = strings.apptSave,
        compact = true,
        enabled = title.isNotBlank() && (amountText.toLongOrNull() ?: 0L) > 0L,
        onClick = {
          onSave(
            RecurringExpenseTemplate(
              id = initial?.id.orEmpty(),
              title = title.trim(),
              category = category,
              amountDh = amountText.toLongOrNull() ?: 0L,
              supplier = supplier.trim(),
              lastAppliedMonthKey = initial?.lastAppliedMonthKey.orEmpty(),
              active = initial?.active ?: true,
            ),
          )
        },
        modifier = Modifier.testTag("save_recurring_btn"),
      )
    },
    dismissButton = {
      SageSecondaryButton(text = strings.apptCancel, compact = true, onClick = onDismiss)
    },
  )
}

@Composable
private fun VendorEditorDialog(
  initial: VendorRecord?,
  currency: String,
  onDismiss: () -> Unit,
  onSave: (VendorRecord) -> Unit,
) {
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  var name by remember(initial) { mutableStateOf(initial?.name.orEmpty()) }
  var specialty by remember(initial) { mutableStateOf(initial?.specialty.orEmpty()) }
  var phone by remember(initial) { mutableStateOf(initial?.phone.orEmpty()) }
  var feeText by remember(initial) { mutableStateOf(initial?.monthlyFeeDh?.takeIf { it > 0L }?.toString().orEmpty()) }
  var contractEnd by remember(initial) { mutableStateOf(initial?.contractEndDate.orEmpty()) }

  SyndicatDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = strings.vendorAddBtn,
        style = MaterialTheme.typography.titleLarge,
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
          value = name,
          onValueChange = { name = it.take(100) },
          label = strings.vendorNameLabel,
          modifier = Modifier.fillMaxWidth().testTag("vendor_name_input"),
        )
        FintechTextField(
          value = specialty,
          onValueChange = { specialty = it.take(80) },
          label = strings.vendorServiceLabel,
          modifier = Modifier.fillMaxWidth().testTag("vendor_service_input"),
        )
        FintechTextField(
          value = phone,
          onValueChange = { phone = it.take(30) },
          label = strings.apptPhoneLabel,
          keyboardType = KeyboardType.Phone,
          modifier = Modifier.fillMaxWidth().testTag("vendor_phone_input"),
        )
        FintechTextField(
          value = feeText,
          onValueChange = { feeText = it.filter { c -> c.isDigit() }.take(7) },
          label = strings.vendorMonthlyFeeLabel(currency),
          keyboardType = KeyboardType.Number,
          modifier = Modifier.fillMaxWidth(),
        )
        FintechTextField(
          value = contractEnd,
          onValueChange = { contractEnd = it.take(30) },
          label = strings.vendorContractEndLabel,
          placeholder = "31/12/2026",
          modifier = Modifier.fillMaxWidth(),
        )
      }
    },
    confirmButton = {
      MetaCobaltBuyButton(
        text = strings.apptSave,
        compact = true,
        enabled = name.isNotBlank(),
        onClick = {
          onSave(
            VendorRecord(
              id = initial?.id.orEmpty(),
              name = name.trim(),
              specialty = specialty.trim(),
              phone = phone.trim(),
              monthlyFeeDh = feeText.toLongOrNull() ?: 0L,
              contractEndDate = contractEnd.trim(),
            ),
          )
        },
        modifier = Modifier.testTag("save_vendor_btn"),
      )
    },
    dismissButton = {
      SageSecondaryButton(text = strings.apptCancel, compact = true, onClick = onDismiss)
    },
  )
}
