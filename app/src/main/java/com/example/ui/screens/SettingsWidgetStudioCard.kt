package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FintechWhiteCard
import com.example.ui.components.LocalSyndicatAdaptiveSpec
import com.example.ui.components.MetaCobaltBuyButton
import com.example.ui.components.MetaSegmentedThreePillRow
import com.example.ui.components.SyndicatFilterChip
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.LocalSyndicatTokens
import com.example.ui.viewmodel.SyndicatDashboardData
import com.example.ui.viewmodel.formatDh
import com.example.widget.SyndicatWidgetPrefs
import com.example.widget.SyndicatWidgetSnapshot
import com.example.widget.WidgetFormatPreset
import com.example.widget.WidgetQuickAction
import com.example.widget.WidgetThemePreset
import com.example.widget.widgetPaletteFor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsWidgetStudioCard(
  data: SyndicatDashboardData,
  onMessage: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val adaptive = LocalSyndicatAdaptiveSpec.current
  val isAr = strings.language == AppLanguage.AR

  var prefs by remember { mutableStateOf(SyndicatWidgetSnapshot.loadPrefs(context)) }

  fun updatePrefs(transform: (SyndicatWidgetPrefs) -> SyndicatWidgetPrefs) {
    val updated = transform(prefs)
    prefs = updated
    SyndicatWidgetSnapshot.savePrefs(context, updated)
  }

  val cfg = data.config
  val cur = cfg.currency
  val dig = cfg.digitsAr
  val cashDisplay = if (prefs.privacyMask) "••••••" else formatDh(data.cashNowDh, cur, dig)
  val collectedDisplay = if (prefs.privacyMask) "••••" else formatDh(data.totalCaisseDh, cur, dig)
  val unpaidExpDisplay = if (prefs.privacyMask) "••••" else formatDh(data.unpaidExpensesDh, cur, dig)
  val rateDisplay = "${data.collectionRatePercent}%"
  val lateApts = data.lateApartments
  val lateLines = remember(lateApts, cfg, isAr, cur, dig) {
    lateApts
      .sortedByDescending { it.remainingDh(cfg) }
      .take(3)
      .map { apt ->
        val owner = apt.displayOwner(isAr).ifBlank { if (isAr) "مالك" else "Occupant" }
        val unitLabel = if (isAr) "شقة ${apt.displayNumber}" else "Appt ${apt.displayNumber}"
        val rem = formatDh(apt.remainingDh(cfg), cur, dig)
        if (prefs.privacyMask) "$unitLabel · $owner" else "$unitLabel · $owner : $rem"
      }
  }

  val actionLabel = when (prefs.primaryAction) {
    WidgetQuickAction.QUICK_PAY -> strings.widgetActionPay
    WidgetQuickAction.LATE_LIST -> strings.widgetActionLate
    WidgetQuickAction.ADD_EXPENSE -> strings.widgetActionExpense
    WidgetQuickAction.OPEN_DASHBOARD -> strings.widgetActionOpen
  }

  FintechWhiteCard(
    modifier = modifier
      .fillMaxWidth()
      .testTag("widget_studio_card"),
    padding = if (adaptive.isNarrowPhone) 16.dp else 22.dp,
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Text(
        text = strings.widgetStudioTitle,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Normal,
        color = tokens.inkDeep,
      )

      Text(
        text = strings.widgetStudioSub,
        style = MaterialTheme.typography.bodySmall,
        color = tokens.body,
      )

      // 1. Format selector (Compact 2×1 | Classique 4×2 | Complet 4×3)
      Text(
        text = strings.widgetFormatLabel,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = tokens.inkDeep,
      )
      MetaSegmentedThreePillRow(
        labels = Triple(strings.widgetFormatCompact, strings.widgetFormatClassic, strings.widgetFormatDetailed),
        selectedIndex = prefs.previewFormat.ordinal,
        onSelectIndex = { idx ->
          updatePrefs { it.copy(previewFormat = WidgetFormatPreset.entries[idx]) }
        },
        tags = Triple("widget_format_compact", "widget_format_classic", "widget_format_detailed"),
        modifier = Modifier.fillMaxWidth(),
      )

      // 2. Live Preview Surface mirroring exact Home Screen Widget render
      val palette = widgetPaletteFor(prefs.theme)
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = palette.bg,
        border = BorderStroke(
          1.dp,
          if (prefs.theme == WidgetThemePreset.LIGHT_CLEAN) tokens.divider else palette.surfaceSoft,
        ),
        shadowElevation = 2.dp,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("widget_preview_box"),
      ) {
        when (prefs.previewFormat) {
          WidgetFormatPreset.COMPACT -> {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = cfg.residenceName.ifBlank { "SYNDICAT" },
                  fontSize = 11.sp,
                  color = palette.secondaryText,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Text(
                  text = cashDisplay,
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = palette.primaryText,
                  maxLines = 1,
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Surface(
                shape = RoundedCornerShape(14.dp),
                color = palette.ctaBg,
              ) {
                Text(
                  text = actionLabel,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = palette.ctaText,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
              }
            }
          }

          WidgetFormatPreset.CLASSIC -> {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = cfg.residenceName.ifBlank { "SYNDICAT" },
                    fontSize = 12.sp,
                    color = palette.secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                  Text(
                    text = cashDisplay,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.primaryText,
                    maxLines = 1,
                  )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                  shape = RoundedCornerShape(14.dp),
                  color = palette.ctaBg,
                ) {
                  Text(
                    text = actionLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.ctaText,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                  )
                }
              }
              if (prefs.showRate || prefs.showLateCount) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                  if (prefs.showRate) {
                    Text(
                      text = if (isAr) "التحصيل: $rateDisplay" else "Recouvrement : $rateDisplay",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Medium,
                      color = palette.positiveText,
                    )
                  }
                  if (prefs.showLateCount) {
                    Text(
                      text = if (isAr) "المتأخرون: ${lateApts.size}" else "Retards : ${lateApts.size}",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Medium,
                      color = if (lateApts.isNotEmpty()) palette.warningText else palette.positiveText,
                    )
                  }
                }
              }
            }
          }

          WidgetFormatPreset.DETAILED -> {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = cfg.residenceName.ifBlank { "SYNDICAT" },
                    fontSize = 12.sp,
                    color = palette.secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                  Text(
                    text = cashDisplay,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.primaryText,
                    maxLines = 1,
                  )
                }
                Surface(
                  shape = RoundedCornerShape(14.dp),
                  color = palette.ctaBg,
                ) {
                  Text(
                    text = actionLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.ctaText,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                  )
                }
              }

              if (prefs.showRate || prefs.showExpenses || prefs.showLateCount) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                  if (prefs.showRate) {
                    Surface(
                      shape = RoundedCornerShape(12.dp),
                      color = palette.surfaceSoft,
                      modifier = Modifier.weight(1f),
                    ) {
                      Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
                        Text(
                          text = if (isAr) "التحصيل" else "Recouvrement",
                          fontSize = 10.sp,
                          color = palette.secondaryText,
                          maxLines = 1,
                        )
                        Text(
                          text = rateDisplay,
                          fontSize = 14.sp,
                          fontWeight = FontWeight.Bold,
                          color = palette.positiveText,
                          maxLines = 1,
                        )
                      }
                    }
                  }
                  if (prefs.showExpenses) {
                    Surface(
                      shape = RoundedCornerShape(12.dp),
                      color = palette.surfaceSoft,
                      modifier = Modifier.weight(1f),
                    ) {
                      Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
                        Text(
                          text = if (isAr) "باقي المزودين" else "Reste dép.",
                          fontSize = 10.sp,
                          color = palette.secondaryText,
                          maxLines = 1,
                        )
                        Text(
                          text = unpaidExpDisplay,
                          fontSize = 14.sp,
                          fontWeight = FontWeight.Bold,
                          color = palette.primaryText,
                          maxLines = 1,
                        )
                      }
                    }
                  }
                  if (prefs.showLateCount) {
                    Surface(
                      shape = RoundedCornerShape(12.dp),
                      color = palette.surfaceSoft,
                      modifier = Modifier.weight(1f),
                    ) {
                      Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
                        Text(
                          text = if (isAr) "متأخرون" else "Retards",
                          fontSize = 10.sp,
                          color = palette.secondaryText,
                          maxLines = 1,
                        )
                        Text(
                          text = "${lateApts.size}",
                          fontSize = 14.sp,
                          fontWeight = FontWeight.Bold,
                          color = if (lateApts.isNotEmpty()) palette.warningText else palette.positiveText,
                          maxLines = 1,
                        )
                      }
                    }
                  }
                }
              }

              if (prefs.showLatePreviewList) {
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = palette.surfaceSoft,
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                  ) {
                    if (lateLines.isEmpty()) {
                      Text(
                        text = if (isAr) "جميع الشقق مؤدّية ✓" else "Toutes les cotisations à jour ✓",
                        fontSize = 11.sp,
                        color = palette.positiveText,
                      )
                    } else {
                      lateLines.forEach { line ->
                        Text(
                          text = line,
                          fontSize = 11.sp,
                          color = palette.primaryText,
                          maxLines = 1,
                          overflow = TextOverflow.Ellipsis,
                        )
                      }
                    }
                  }
                }
              }

              FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Text(
                  text = if (isAr) "المحصل: $collectedDisplay" else "Encaissé : $collectedDisplay",
                  fontSize = 11.sp,
                  color = palette.secondaryText,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.align(Alignment.CenterVertically),
                )
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = palette.surfaceSoft,
                ) {
                  Text(
                    text = if (prefs.primaryAction == WidgetQuickAction.LATE_LIST) strings.widgetActionExpense else strings.widgetActionLate,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.primaryText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                  )
                }
              }
            }
          }
        }
      }

      // 3. Appearance Theme Chips
      Text(
        text = strings.widgetThemeLabel,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = tokens.inkDeep,
      )
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        val themes = listOf(
          WidgetThemePreset.NAVY_PRO to strings.widgetThemeNavy,
          WidgetThemePreset.LIGHT_CLEAN to strings.widgetThemeLight,
          WidgetThemePreset.AMOLED_DARK to strings.widgetThemeDark,
          WidgetThemePreset.EMERALD_FINTECH to strings.widgetThemeEmerald,
        )
        themes.forEach { (preset, label) ->
          val swatch = widgetPaletteFor(preset).bg
          SyndicatFilterChip(
            selected = prefs.theme == preset,
            onClick = { updatePrefs { it.copy(theme = preset) } },
            label = label,
            leadingIcon = {
              Box(
                modifier = Modifier
                  .size(12.dp)
                  .clip(CircleShape)
                  .background(swatch)
                  .border(1.dp, tokens.divider, CircleShape),
              )
            },
            modifier = Modifier.testTag("widget_theme_${preset.code}"),
          )
        }
      }

      // 4. Primary Action Button Chips
      Text(
        text = strings.widgetActionLabel,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = tokens.inkDeep,
      )
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        val actions = listOf(
          WidgetQuickAction.QUICK_PAY to strings.widgetActionPay,
          WidgetQuickAction.LATE_LIST to strings.widgetActionLate,
          WidgetQuickAction.ADD_EXPENSE to strings.widgetActionExpense,
          WidgetQuickAction.OPEN_DASHBOARD to strings.widgetActionOpen,
        )
        actions.forEach { (act, label) ->
          SyndicatFilterChip(
            selected = prefs.primaryAction == act,
            onClick = { updatePrefs { it.copy(primaryAction = act) } },
            label = label,
            modifier = Modifier.testTag("widget_action_${act.code}"),
          )
        }
      }

      HorizontalDivider(color = tokens.divider)

      // 5. Display Options Switches
      WidgetOptionSwitchRow(
        label = strings.widgetOptPrivacy,
        checked = prefs.privacyMask,
        onCheckedChange = { checked -> updatePrefs { it.copy(privacyMask = checked) } },
        tag = "widget_opt_privacy",
      )
      WidgetOptionSwitchRow(
        label = strings.widgetOptShowRate,
        checked = prefs.showRate,
        onCheckedChange = { checked -> updatePrefs { it.copy(showRate = checked) } },
        tag = "widget_opt_rate",
      )
      WidgetOptionSwitchRow(
        label = strings.widgetOptShowLateCount,
        checked = prefs.showLateCount,
        onCheckedChange = { checked -> updatePrefs { it.copy(showLateCount = checked) } },
        tag = "widget_opt_late_count",
      )
      WidgetOptionSwitchRow(
        label = strings.widgetOptShowExpenses,
        checked = prefs.showExpenses,
        onCheckedChange = { checked -> updatePrefs { it.copy(showExpenses = checked) } },
        tag = "widget_opt_expenses",
      )
      WidgetOptionSwitchRow(
        label = strings.widgetOptShowLateList,
        checked = prefs.showLatePreviewList,
        onCheckedChange = { checked -> updatePrefs { it.copy(showLatePreviewList = checked) } },
        tag = "widget_opt_late_list",
      )

      // 6. Pin Selected Widget Format to Home Screen
      MetaCobaltBuyButton(
        text = strings.widgetPinBtn,
        onClick = {
          val ok = SyndicatWidgetSnapshot.pinWidget(context, prefs.previewFormat)
          onMessage(if (ok) strings.widgetPinRequestedMsg else strings.widgetPinManualHint)
        },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("widget_pin_btn"),
      )
    }
  }
}

@Composable
private fun WidgetOptionSwitchRow(
  label: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  tag: String,
) {
  val tokens = LocalSyndicatTokens.current
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      color = tokens.inkDeep,
      modifier = Modifier.weight(1f),
    )
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      modifier = Modifier.testTag(tag),
    )
  }
}
