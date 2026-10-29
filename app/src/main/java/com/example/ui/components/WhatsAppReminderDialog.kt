package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ApartmentRecord
import com.example.data.model.SyndicatConfig
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalStrings
import com.example.ui.i18n.localizeDigits
import com.example.ui.theme.LocalSyndicatTokens
import com.example.ui.viewmodel.formatDh
import com.example.util.WhatsAppHelper
import com.example.util.WhatsAppTemplatePreset

private enum class ReminderQueueFilter {
  ALL,
  UNREMINDED,
  WITH_PHONE,
}

private enum class SelectedTemplateOption {
  CUSTOM_SAVED,
  FRIENDLY_AR,
  URGENT_AR,
  FRIENDLY_FR,
  URGENT_FR,
}

/**
 * Interactive & Automated WhatsApp Reminder Assistant Dialog ("Airtable" editorial aesthetic):
 * - Automatically populates personalized reminder messages for each late resident from a predefined or saved template.
 * - Supports 1-tap switching across predefined templates (Courteous AR/FR, Urgent AR/FR, or Saved Building Template).
 * - Inline WhatsApp phone entry & auto-save when a resident's phone number is missing.
 * - Sequential "Send via WhatsApp & Next" campaign stepper + Copy to Clipboard + Group Summary broadcast.
 * - Strict compliance with "الأبيض الهادئ": max 1 black primary button (`LimePrimaryButton`), secondary buttons white with 1dp hairline.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WhatsAppReminderDialog(
  apartmentsQueue: List<ApartmentRecord>,
  config: SyndicatConfig,
  initialApartment: ApartmentRecord? = null,
  onDismiss: () -> Unit,
  onReminderSent: (apartment: ApartmentRecord, updatedPhone: String?, updatedTemplateToSave: String?) -> Unit,
  onInfoMessage: (String) -> Unit = {},
) {
  val context = LocalContext.current
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val isAr = strings.language == AppLanguage.AR
  val dig = config.digitsAr
  val cur = config.currency
  val numericFont = tokens.numericFontFamily ?: tokens.monoFontFamily

  val isSingleResidentMode = initialApartment != null && apartmentsQueue.size == 1 && apartmentsQueue.first().id == initialApartment.id

  var queueFilter by remember { mutableStateOf(ReminderQueueFilter.ALL) }
  var selectedTemplateOption by remember { mutableStateOf(SelectedTemplateOption.CUSTOM_SAVED) }
  var saveTemplateAsDefault by remember { mutableStateOf(false) }

  val filteredList = remember(apartmentsQueue, queueFilter) {
    when (queueFilter) {
      ReminderQueueFilter.ALL -> apartmentsQueue
      ReminderQueueFilter.UNREMINDED -> apartmentsQueue.filter { it.lastReminderAt == 0L }
      ReminderQueueFilter.WITH_PHONE -> apartmentsQueue.filter { WhatsAppHelper.hasValidWhatsAppPhone(it.phone) }
    }
  }

  var currentIndex by remember(initialApartment?.id, queueFilter) {
    val startIdx = if (initialApartment != null) {
      filteredList.indexOfFirst { it.id == initialApartment.id && it.number == initialApartment.number }.coerceAtLeast(0)
    } else {
      val firstUnreminded = filteredList.indexOfFirst { it.lastReminderAt == 0L }
      if (firstUnreminded >= 0) firstUnreminded else 0
    }
    mutableIntStateOf(startIdx)
  }

  val activeTemplateText = remember(selectedTemplateOption, config.whatsappTemplate) {
    when (selectedTemplateOption) {
      SelectedTemplateOption.CUSTOM_SAVED -> config.whatsappTemplate
      SelectedTemplateOption.FRIENDLY_AR -> WhatsAppTemplatePreset.FRIENDLY_AR.templateText()
      SelectedTemplateOption.URGENT_AR -> WhatsAppTemplatePreset.URGENT_AR.templateText()
      SelectedTemplateOption.FRIENDLY_FR -> WhatsAppTemplatePreset.FRIENDLY_FR.templateText()
      SelectedTemplateOption.URGENT_FR -> WhatsAppTemplatePreset.URGENT_FR.templateText()
    }
  }

  val currentApartment = filteredList.getOrNull(currentIndex)
  var phoneDraft by remember(currentApartment?.id, currentApartment?.number) {
    mutableStateOf(currentApartment?.phone ?: "")
  }
  var messageDraft by remember { mutableStateOf("") }

  LaunchedEffect(currentApartment?.id, currentApartment?.number, activeTemplateText) {
    if (currentApartment != null) {
      messageDraft = WhatsAppHelper.buildReminderMessage(
        apartment = currentApartment,
        config = config,
        overrideTemplate = activeTemplateText,
      )
    }
  }

  val totalRemainingInFilter = remember(filteredList, config) {
    filteredList.sumOf { it.remainingDh(config) }
  }

  SyndicatDialog(
    onDismissRequest = onDismiss,
    title = {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = strings.whatsappCampaignTitle,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Medium,
          color = tokens.inkDeep,
        )
        Text(
          text = strings.whatsappCampaignSub(
            filteredList.size,
            formatDh(totalRemainingInFilter, cur, dig),
          ),
          style = MaterialTheme.typography.bodySmall,
          color = tokens.body,
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
      // 1. Queue Filter Chips (when multiple late residents exist)
      if (!isSingleResidentMode && apartmentsQueue.size > 1) {
        val unremindedCount = apartmentsQueue.count { it.lastReminderAt == 0L }
        val withPhoneCount = apartmentsQueue.count { WhatsAppHelper.hasValidWhatsAppPhone(it.phone) }
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          SyndicatFilterChip(
            selected = queueFilter == ReminderQueueFilter.ALL,
            onClick = { queueFilter = ReminderQueueFilter.ALL },
            label = strings.whatsappFilterAllLate(apartmentsQueue.size),
            modifier = Modifier.testTag("wa_filter_all"),
          )
          SyndicatFilterChip(
            selected = queueFilter == ReminderQueueFilter.UNREMINDED,
            onClick = { queueFilter = ReminderQueueFilter.UNREMINDED },
            label = strings.whatsappFilterUnreminded(unremindedCount),
            modifier = Modifier.testTag("wa_filter_unreminded"),
          )
          SyndicatFilterChip(
            selected = queueFilter == ReminderQueueFilter.WITH_PHONE,
            onClick = { queueFilter = ReminderQueueFilter.WITH_PHONE },
            label = strings.whatsappFilterWithPhone(withPhoneCount),
            modifier = Modifier.testTag("wa_filter_with_phone"),
          )
        }
      }

      if (currentApartment == null) {
        // Completed or empty filter state
        FintechPalePositiveCard(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("wa_campaign_completed_card"),
          padding = 20.dp,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = tokens.positive,
              modifier = Modifier.size(28.dp),
            )
            Column {
              Text(
                text = strings.whatsappAllDoneTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = tokens.inkDeep,
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = strings.whatsappAllDoneSub,
                style = MaterialTheme.typography.bodySmall,
                color = tokens.body,
              )
            }
          }
        }
      } else {
        // 2. Progress Indicator + Resident Identity Card
        if (filteredList.size > 1) {
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = strings.whatsappProgressStep(currentIndex + 1, filteredList.size),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = tokens.ink,
              )
              if (currentIndex + 1 < filteredList.size) {
                Text(
                  text = strings.whatsappSkipNextBtn,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = tokens.linkColor,
                  modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .clickable { currentIndex = (currentIndex + 1).coerceAtMost(filteredList.size) }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .testTag("wa_skip_resident_btn"),
                )
              }
            }
            SyndicatProgressBar(
              progress = ((currentIndex + 1).toFloat() / filteredList.size.toFloat()).coerceIn(0f, 1f),
              modifier = Modifier.fillMaxWidth(),
            )
          }
        }

        val ownerLabel = currentApartment.displayOwner(isAr)
          .ifBlank { "${strings.colAppt} ${currentApartment.displayNumber}" }
        val remDh = currentApartment.remainingDh(config)
        val missingLabels = currentApartment.missingMonths(config).joinToString(if (isAr) "، " else ", ") {
          strings.monthShort(it)
        }

        Surface(
          shape = RoundedCornerShape(tokens.contentCardRadiusDp.dp),
          color = tokens.surfaceSoft,
          border = BorderStroke(1.dp, tokens.divider),
          tonalElevation = 0.dp,
          shadowElevation = 0.dp,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("wa_current_resident_card"),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
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
                border = BorderStroke(1.dp, tokens.divider),
                modifier = Modifier.size(40.dp),
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                    text = localizeDigits(currentApartment.displayNumber, dig),
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = numericFont ?: MaterialTheme.typography.titleMedium.fontFamily,
                    fontWeight = FontWeight.Medium,
                    color = tokens.ink,
                  )
                }
              }
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = ownerLabel,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Medium,
                  color = tokens.ink,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Text(
                  text = "${strings.floorWithPrefix(currentApartment.floor)}${if (missingLabels.isNotBlank()) " · $missingLabels" else ""}",
                  style = MaterialTheme.typography.bodySmall,
                  color = tokens.body,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "! ${formatDh(remDh, cur, dig)}",
              style = MaterialTheme.typography.titleMedium,
              fontFamily = numericFont ?: MaterialTheme.typography.titleMedium.fontFamily,
              fontWeight = FontWeight.Medium,
              color = tokens.negativeDeep,
            )
          }
        }

        // 3. WhatsApp Phone Field (highlighted if missing so syndic can enter & auto-save it)
        val hasValidPhone = WhatsAppHelper.hasValidWhatsAppPhone(phoneDraft)
        if (!hasValidPhone) {
          AirtableCreamCalloutCard(
            modifier = Modifier.fillMaxWidth(),
            padding = 12.dp,
          ) {
            Text(
              text = strings.whatsappNoPhoneHint,
              style = MaterialTheme.typography.bodySmall,
              color = tokens.inkDeep,
            )
          }
        }
        FintechTextField(
          value = phoneDraft,
          onValueChange = { next ->
            phoneDraft = next.filter { it.isDigit() || it == '+' || it == ' ' }.take(24)
          },
          label = strings.apptPhoneLabel,
          placeholder = strings.apptPhonePlaceholder,
          keyboardType = KeyboardType.Phone,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("wa_resident_phone_input"),
        )

        // 4. Predefined Template Selector Chips
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = strings.settingsWhatsappPresetsLabel,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = tokens.ink,
          )
          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            listOf(
              SelectedTemplateOption.CUSTOM_SAVED to strings.whatsappPresetCustom,
              SelectedTemplateOption.FRIENDLY_AR to strings.whatsappPresetFriendlyAr,
              SelectedTemplateOption.URGENT_AR to strings.whatsappPresetUrgentAr,
              SelectedTemplateOption.FRIENDLY_FR to strings.whatsappPresetFriendlyFr,
              SelectedTemplateOption.URGENT_FR to strings.whatsappPresetUrgentFr,
            ).forEach { (opt, label) ->
              SyndicatFilterChip(
                selected = selectedTemplateOption == opt,
                onClick = { selectedTemplateOption = opt },
                label = label,
                modifier = Modifier.testTag("wa_preset_${opt.name.lowercase()}"),
              )
            }
          }
        }

        // 5. Auto-generated Personalized Message Preview / Editor
        FintechTextField(
          value = messageDraft,
          onValueChange = { messageDraft = it.take(1200) },
          label = strings.whatsappEditMessageLabel,
          singleLine = false,
          maxLines = 6,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("wa_generated_message_input"),
        )

        if (selectedTemplateOption != SelectedTemplateOption.CUSTOM_SAVED) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .minimumInteractiveComponentSize()
              .heightIn(min = 48.dp)
              .clip(RoundedCornerShape(8.dp))
              .clickable { saveTemplateAsDefault = !saveTemplateAsDefault }
              .testTag("wa_save_default_template_toggle"),
          ) {
            Checkbox(
              checked = saveTemplateAsDefault,
              onCheckedChange = { saveTemplateAsDefault = it },
            )
            Text(
              text = strings.whatsappSaveTemplateCheck,
              style = MaterialTheme.typography.bodySmall,
              color = tokens.ink,
            )
          }
        }

        // 6. Secondary Actions: Copy Message & Share Group Broadcast
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          SageSecondaryButton(
            text = strings.whatsappCopyMsgBtn,
            icon = Icons.Default.ContentCopy,
            compact = true,
            onClick = {
              WhatsAppHelper.copyTextToClipboard(context, "WhatsApp Reminder", messageDraft)
              onInfoMessage(strings.whatsappMsgCopiedToast)
            },
            modifier = Modifier
              .weight(1f)
              .testTag("wa_copy_message_btn"),
          )
          if (!isSingleResidentMode && filteredList.size > 1) {
            SageSecondaryButton(
              text = strings.whatsappShareGroupBtn,
              icon = Icons.Default.Groups,
              compact = true,
              onClick = {
                val broadcast = WhatsAppHelper.buildGroupReminderBroadcastMessage(
                  apartments = filteredList,
                  config = config,
                )
                WhatsAppHelper.openRawWhatsAppMessage(
                  context = context,
                  rawPhone = "",
                  message = broadcast,
                  onError = onInfoMessage,
                )
              },
              modifier = Modifier
                .weight(1f)
                .testTag("wa_share_group_btn"),
            )
          }
        }
      }
      }
    },
    confirmButton = {
      if (currentApartment != null) {
        val hasMoreAfterCurrent = currentIndex + 1 < filteredList.size
        val primaryLabel = if (hasMoreAfterCurrent) {
          strings.whatsappSendAndNextBtn
        } else {
          strings.whatsappSendSingleBtn
        }

        // Meta `button-buy-cta` (#0064E0 Cobalt pill) pour l'envoi transactionnel WhatsApp
        MetaCobaltBuyButton(
          text = primaryLabel,
          icon = Icons.AutoMirrored.Filled.Send,
          compact = true,
          onClick = {
            val aptToRemind = currentApartment
            val updatedTemplate = if (saveTemplateAsDefault && selectedTemplateOption != SelectedTemplateOption.CUSTOM_SAVED) {
              activeTemplateText
            } else {
              null
            }
            val opened = WhatsAppHelper.openRawWhatsAppMessage(
              context = context,
              rawPhone = phoneDraft,
              message = messageDraft,
              onError = onInfoMessage,
            )
            if (opened) {
              onReminderSent(aptToRemind, phoneDraft, updatedTemplate)
            }
            if (hasMoreAfterCurrent) {
              currentIndex += 1
            } else {
              currentIndex = filteredList.size
              if (isSingleResidentMode) {
                onDismiss()
              }
            }
          },
          modifier = Modifier.testTag("wa_send_primary_btn"),
        )
      }
    },
    dismissButton = {
      SageSecondaryButton(
        text = strings.apptCancel,
        compact = true,
        onClick = onDismiss,
        modifier = Modifier.testTag("wa_close_dialog_btn"),
      )
    },
  )
}
