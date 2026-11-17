package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
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
import com.example.data.local.AuditLogEntry
import com.example.data.local.BuildingProfile
import com.example.data.local.SpecialCallRecord
import com.example.data.local.SyndicatExtrasStore
import com.example.data.local.SyndicatRoleMode
import com.example.data.model.MonthColumn
import com.example.data.model.SyndicatConfig
import com.example.data.model.buildFloorLabels
import com.example.ui.components.AirtableSoftCard
import com.example.ui.components.FintechDarkFeatureCard
import com.example.ui.components.FintechTextField
import com.example.ui.components.FintechWhiteCard
import com.example.ui.components.InkTertiaryButton
import com.example.ui.components.MetaCobaltBuyButton
import com.example.ui.components.PinSetupDialog
import com.example.ui.components.ReadOnlyCouncilBanner
import com.example.ui.components.ResidenceHeroBanner
import com.example.ui.components.RoundIconCircleButton
import com.example.ui.components.SageSecondaryButton
import com.example.ui.components.SyndicatAboutEditionCard
import com.example.ui.components.SyndicatDialog
import com.example.ui.components.SyndicatEmptyStateCard
import com.example.ui.components.SyndicatFilterChip
import com.example.ui.components.SyndicatTopBrandBar
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LanguagePreferences
import com.example.ui.i18n.LocalStrings
import com.example.ui.i18n.localizeDigits
import com.example.ui.theme.LocalSyndicatTokens
import com.example.ui.theme.cotisationColorForAmount
import com.example.ui.theme.resolveVisualThemeFamily
import com.example.ui.theme.sanitizeThemeStyle
import com.example.ui.viewmodel.SyndicatDashboardData
import com.example.util.BackupManager
import com.example.util.BiometricHelper
import com.example.util.MonthlyReminderScheduler
import com.example.util.ParsedOwnerRow
import com.example.util.ParsedSyndicatBackup
import com.example.util.WhatsAppHelper
import com.example.util.WhatsAppTemplatePreset

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
  data: SyndicatDashboardData,
  currentUserId: String,
  userEmail: String?,
  currentLanguage: AppLanguage,
  isSaving: Boolean,
  onSelectLanguage: (AppLanguage) -> Unit,
  onUpdateDisplayPrefs: (digitsAr: Boolean?, themeStyle: String?, themeMode: String?, fontScale: String?) -> Unit,
  onOpenWizard: () -> Unit,
  onSaveConfig: (SyndicatConfig, Boolean) -> Unit,
  onRestoreBackup: (ParsedSyndicatBackup) -> Unit,
  onImportCsvOwners: (List<ParsedOwnerRow>) -> Unit,
  onResetC49: () -> Unit,
  onBackupExported: () -> Unit,
  onPinUpdated: (String) -> Unit,
  onSignOut: () -> Unit,
  onMessage: (String) -> Unit,
  isCloudSynced: Boolean = !userEmail.isNullOrBlank(),
  onConnectGoogleSync: () -> Unit = {},
  onPushLocalToCloud: () -> Unit = {},
  auditLogs: List<AuditLogEntry> = emptyList(),
  buildings: List<BuildingProfile> = listOf(BuildingProfile("default", data.config.residenceName.ifBlank { "Résidence Principale" })),
  activeBuildingId: String = "default",
  onSelectBuilding: (String) -> Unit = {},
  onCreateBuilding: (name: String, city: String) -> Unit = { _, _ -> },
  onDeleteBuilding: (String) -> Unit = {},
  roleMode: SyndicatRoleMode = SyndicatRoleMode.ADMIN,
  onSetRoleMode: (SyndicatRoleMode) -> Unit = {},
  specialCalls: List<SpecialCallRecord> = emptyList(),
  onImportCouncilSnapshot: (ParsedSyndicatBackup) -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val cfg = data.config
  val isAr = currentLanguage == AppLanguage.AR
  val isReadOnlyMode = roleMode == SyndicatRoleMode.READ_ONLY_COUNCIL
  val monoOrDefault = tokens.monoFontFamily

  // Building Identity State
  var syndicatName by remember(cfg) { mutableStateOf(cfg.syndicatName) }
  var residenceName by remember(cfg) { mutableStateOf(cfg.residenceName) }
  var city by remember(cfg) { mutableStateOf(cfg.city) }
  var address by remember(cfg) { mutableStateOf(cfg.address) }
  var treasurerName by remember(cfg) { mutableStateOf(cfg.treasurerName) }
  var buildingTitle by remember(cfg) { mutableStateOf(cfg.buildingTitle) }
  var yearText by remember(cfg) { mutableStateOf(cfg.year.toString()) }
  var currency by remember(cfg) { mutableStateOf(cfg.currency) }
  var openingBalanceText by remember(cfg) { mutableStateOf(cfg.openingBalanceDh.toString()) }
  var footerNote by remember(cfg) { mutableStateOf(cfg.footerNote) }

  // Floors & Apartments Structure State
  var includeRdc by remember(cfg) { mutableStateOf(cfg.includeRdc) }
  var floorsCount by remember(cfg) { mutableIntStateOf(cfg.floorsCount.toInt().coerceIn(1, 20)) }
  var isVariablePerFloor by remember(cfg) {
    mutableStateOf(cfg.floorAptCounts.isNotEmpty() && cfg.floorAptCounts.distinct().size > 1)
  }
  var uniformTotalApptsText by remember(cfg) { mutableStateOf(cfg.apartmentsCount.toString()) }
  val perFloorCounts = remember(cfg) {
    mutableStateListOf<Int>().apply {
      val initial = cfg.effectiveFloorCounts()
      for (i in 0 until 20) {
        add(initial.getOrElse(i) { 6 })
      }
    }
  }

  // Fees & 12-Month Custom Tariffs State
  var defaultFeeText by remember(cfg) { mutableStateOf(cfg.defaultMonthlyFee.toString()) }
  var dueDayText by remember(cfg) { mutableStateOf(cfg.dueDayOfMonth.toString()) }
  var cutoffMonth by remember(cfg) { mutableIntStateOf(cfg.cutoffMonth.toInt().coerceIn(1, 12)) }
  val activeMonthSet = remember(cfg) {
    mutableStateMapOf<Int, Boolean>().apply {
      val set = cfg.activeMonths.map { it.toInt() }.toSet()
      MonthColumn.entries.forEach { m -> put(m.monthNumber, m.monthNumber in set) }
    }
  }
  val monthFeeTexts = remember(cfg) {
    mutableStateMapOf<MonthColumn, String>().apply {
      MonthColumn.entries.forEach { m -> put(m, cfg.feeForMonth(m).toString()) }
    }
  }

  // WhatsApp & Relance State
  var whatsappTemplate by remember(cfg) { mutableStateOf(cfg.whatsappTemplate) }
  var minReminderText by remember(cfg) { mutableStateOf(cfg.minReminderAmountDh.toString()) }
  var relanceHeaderNote by remember(cfg) { mutableStateOf(cfg.relanceHeaderNote) }

  // Dialogs & Non-blocking 2-tap confirmations
  var showPinDialog by remember { mutableStateOf(false) }
  var hasPin by remember { mutableStateOf(LanguagePreferences.getPinCode(context).isNotBlank()) }
  val bioAvailable = remember { BiometricHelper.isBiometricAvailable(context) }
  var biometricEnabled by remember { mutableStateOf(LanguagePreferences.isBiometricUnlockEnabled(context)) }
  var reminderEnabled by remember { mutableStateOf(LanguagePreferences.isMonthlyReminderEnabled(context)) }
  var reminderDay by remember { mutableIntStateOf(LanguagePreferences.getMonthlyReminderDay(context)) }
  var pendingTestReminder by remember { mutableStateOf(false) }
  var showAuditDialog by remember { mutableStateOf(false) }
  var showCsvPasteDialog by remember { mutableStateOf(false) }
  var csvPasteText by remember { mutableStateOf("") }
  var armBlankReset by remember { mutableStateOf(false) }
  var armC49Reset by remember { mutableStateOf(false) }

  val notifPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
  ) { granted ->
    if (granted) {
      if (pendingTestReminder) {
        pendingTestReminder = false
        MonthlyReminderScheduler.showReminderNotificationNow(context, data.lateApartments.size)
      } else {
        reminderEnabled = true
        LanguagePreferences.setMonthlyReminderEnabled(context, true)
        MonthlyReminderScheduler.scheduleMonthlyReminder(context, reminderDay, true)
      }
    } else {
      pendingTestReminder = false
    }
  }

  // JSON Backup Restore File Picker (.json)
  val jsonImportLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
  ) { uri ->
    if (uri != null) {
      try {
        val raw = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
        if (!raw.isNullOrBlank()) {
          val parsed = BackupManager.parseBackupJson(raw, currentUserId)
          onRestoreBackup(parsed)
        }
      } catch (e: Exception) {
        onMessage(e.localizedMessage ?: "ملف النسخة الاحتياطية غير صالح")
      }
    }
  }

  // CSV / Text Owners File Picker
  val csvFileImportLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
  ) { uri ->
    if (uri != null) {
      try {
        val raw = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
        if (!raw.isNullOrBlank()) {
          val rows = BackupManager.parseOwnersBulkLines(raw)
          if (rows.isNotEmpty()) {
            onImportCsvOwners(rows)
          }
        }
      } catch (e: Exception) {
        onMessage(e.localizedMessage ?: "تعذر قراءة ملف CSV")
      }
    }
  }

  fun buildUpdatedConfig(): SyndicatConfig {
    val activeNums = MonthColumn.entries
      .filter { activeMonthSet[it.monthNumber] == true }
      .map { it.monthNumber.toLong() }
      .ifEmpty { listOf(cutoffMonth.toLong()) }

    val cleanFloors = floorsCount.coerceIn(1, 20)
    val floorCountsList: List<Long>
    val totalAppts: Long
    if (isVariablePerFloor) {
      floorCountsList = (0 until cleanFloors).map { perFloorCounts[it].toLong().coerceIn(1L, 120L) }
      totalAppts = floorCountsList.sum().coerceIn(1L, 120L)
    } else {
      floorCountsList = emptyList()
      totalAppts = (uniformTotalApptsText.toLongOrNull() ?: cfg.apartmentsCount).coerceIn(1L, 120L)
    }

    var next = cfg.copy(
      language = currentLanguage.code,
      syndicatName = syndicatName.trim().ifBlank { "إدارة السانديك" },
      residenceName = residenceName.trim().ifBlank { "إقامة" },
      city = city.trim(),
      address = address.trim(),
      treasurerName = treasurerName.trim(),
      buildingTitle = buildingTitle.trim().ifBlank { "المساهمة الشهرية – ${residenceName.trim()}" },
      year = (yearText.toLongOrNull() ?: 2026L).coerceIn(2020L, 2100L),
      currency = currency.trim().ifBlank { "درهم" }.take(15),
      openingBalanceDh = (openingBalanceText.toLongOrNull() ?: 0L).coerceIn(-10_000_000L, 100_000_000L),
      footerNote = footerNote.trim().ifBlank { "—" },
      includeRdc = includeRdc,
      floorsCount = cleanFloors.toLong(),
      apartmentsCount = totalAppts,
      floorAptCounts = floorCountsList,
      defaultMonthlyFee = (defaultFeeText.toLongOrNull() ?: 50L).coerceIn(0L, 100_000L),
      dueDayOfMonth = (dueDayText.toLongOrNull() ?: 5L).coerceIn(1L, 31L),
      cutoffMonth = cutoffMonth.toLong(),
      activeMonths = activeNums,
      whatsappTemplate = whatsappTemplate.trim(),
      minReminderAmountDh = (minReminderText.toLongOrNull() ?: 20L).coerceIn(0L, 100_000L),
      relanceHeaderNote = relanceHeaderNote.trim().ifBlank { "—" },
      themeStyle = sanitizeThemeStyle(cfg.themeStyle, "coinbase"),
      onboardingCompleted = true,
    )
    MonthColumn.entries.forEach { col ->
      val f = (monthFeeTexts[col]?.toLongOrNull() ?: next.defaultMonthlyFee).coerceIn(0L, 100_000L)
      next = next.withMonthFee(col, f)
    }
    return next
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(tokens.screenBg)
      .testTag("settings_screen"),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 36.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    // Title + Application Brand Bar (SYNDICAT · Éditeur DYNIC · Version 2.0 · 2026)
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (isReadOnlyMode) {
          ReadOnlyCouncilBanner()
        }
        SyndicatTopBrandBar(tag = "settings_app_brand_bar")
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = strings.settingsPageTitle,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Medium,
            color = tokens.inkDeep,
          )
          Text(
            text = strings.settingsPageSubtitle,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Light,
            color = tokens.body,
          )
        }
      }
    }

    // =========================================================================
    // 1. LANGUE & AFFICHAGE (Priorité 1 — tout en haut)
    // =========================================================================
    item(key = "settings_group_1_header") {
      SettingsGroupHeader(
        title = strings.settingsGroupLanguageDisplay,
        tag = "settings_group_1",
      )
    }

    item(key = "settings_lang_display_item") {
      FintechWhiteCard(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("settings_lang_display_card"),
        padding = 24.dp,
      ) {
        Text(
          text = strings.settingsLangDisplayTitle,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Normal,
          color = tokens.ink,
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          AppLanguage.entries.forEach { lang ->
            SyndicatFilterChip(
              selected = currentLanguage == lang,
              onClick = { onSelectLanguage(lang) },
              label = lang.nativeLabel,
              modifier = Modifier
                .weight(1f)
                .testTag("lang_option_${lang.code}"),
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .heightIn(min = 48.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = strings.settingsDigitsArLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = tokens.ink,
            modifier = Modifier.weight(1f),
          )
          Switch(
            checked = cfg.digitsAr,
            onCheckedChange = { onUpdateDisplayPrefs(it, null, null, null) },
            modifier = Modifier.testTag("switch_digits_ar"),
          )
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = tokens.divider)
        Spacer(modifier = Modifier.height(12.dp))

        // Sélecteur de style : Meta, Coinbase ou MiniMax (stocké en base dans themeStyle)
        val activeThemeFamily = resolveVisualThemeFamily(cfg.themeStyle)
        Text(
          text = strings.settingsStyleLabel,
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Medium,
          color = tokens.ink,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          com.example.ui.components.ThemePreviewMiniCard(
            styleId = "meta",
            label = strings.themeChoiceMeta,
            selected = activeThemeFamily == "meta",
            accentColor = Color(0xFF0064E0),
            surfacePreviewColor = Color(0xFFFFFFFF),
            headerBgColor = Color(0xFF1C2B33),
            cornerRadiusDp = 12,
            onClick = { onUpdateDisplayPrefs(null, "meta", null, null) },
            modifier = Modifier.weight(1f),
          )
          com.example.ui.components.ThemePreviewMiniCard(
            styleId = "coinbase",
            label = strings.themeChoiceCoinbase,
            selected = activeThemeFamily == "coinbase",
            accentColor = Color(0xFF0052FF),
            surfacePreviewColor = Color(0xFFF5F8FF),
            headerBgColor = Color(0xFF0A0B0D),
            cornerRadiusDp = 10,
            onClick = { onUpdateDisplayPrefs(null, "coinbase", null, null) },
            modifier = Modifier.weight(1f),
          )
          com.example.ui.components.ThemePreviewMiniCard(
            styleId = "minimax",
            label = strings.themeChoiceMinimax,
            selected = activeThemeFamily == "minimax",
            accentColor = Color(0xFFD93F26),
            surfacePreviewColor = Color(0xFFFAF8F5),
            headerBgColor = Color(0xFF141518),
            cornerRadiusDp = 10,
            onClick = { onUpdateDisplayPrefs(null, "minimax", null, null) },
            modifier = Modifier.weight(1f),
          )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = strings.themeChoiceSubtitle(activeThemeFamily),
          style = MaterialTheme.typography.bodySmall,
          color = tokens.body,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Mode d'apparence : Système / Clair / Sombre
        Text(
          text = strings.settingsThemeLabel,
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Medium,
          color = tokens.ink,
        )
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          listOf(
            "system" to strings.settingsThemeSystem,
            "light" to strings.settingsThemeLight,
            "dark" to strings.settingsThemeDark,
          ).forEach { (mode, label) ->
            SyndicatFilterChip(
              selected = cfg.themeMode == mode,
              onClick = { onUpdateDisplayPrefs(null, null, mode, null) },
              label = label,
              modifier = Modifier.testTag("theme_mode_$mode"),
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = strings.settingsFontScaleLabel,
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Medium,
          color = tokens.ink,
        )
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          listOf(
            "small" to strings.settingsFontSmall,
            "normal" to strings.settingsFontNormal,
            "large" to strings.settingsFontLarge,
          ).forEach { (scale, label) ->
            SyndicatFilterChip(
              selected = cfg.fontScale == scale,
              onClick = { onUpdateDisplayPrefs(null, null, null, scale) },
              label = label,
              modifier = Modifier.testTag("font_scale_$scale"),
            )
          }
        }
        if (cfg.fontScale == "large") {
          Spacer(modifier = Modifier.height(8.dp))
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = tokens.primaryTint,
            border = BorderStroke(tokens.borderStrokeDp.dp, tokens.linkColor.copy(alpha = 0.25f)),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("font_scale_large_hint"),
          ) {
            Text(
              text = strings.settingsFontLargeHint,
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Medium,
              color = tokens.inkDeep,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            )
          }
        }
      }
    }

    // =========================================================================
    // 2. IMMEUBLE, STRUCTURE & COTISATIONS (Essentiel)
    // =========================================================================
    item(key = "settings_group_2_header") {
      SettingsGroupHeader(
        title = strings.settingsGroupBuildingConfig,
        tag = "settings_group_2",
      )
    }

    // Assistant rapide 6 étapes
    item(key = "settings_wizard_item") {
      FintechDarkFeatureCard(
        modifier = Modifier.fillMaxWidth(),
        padding = 24.dp,
      ) {
        Text(
          text = strings.settingsOpenWizardCardTitle,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Normal,
          color = Color.White,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = strings.settingsOpenWizardCardSub,
          style = MaterialTheme.typography.bodySmall,
          color = Color.White.copy(alpha = 0.88f),
        )
        Spacer(modifier = Modifier.height(14.dp))
        SageSecondaryButton(
          text = strings.dashLaunchWizardBtn,
          onSignatureCard = true,
          enabled = !isReadOnlyMode,
          onClick = onOpenWizard,
          modifier = Modifier.fillMaxWidth().testTag("settings_open_wizard_btn"),
        )
      }
    }

    // 3. Building & Syndic Identity (AirtableSoftCard — alternance après FintechWhiteCard)
    item {
      AirtableSoftCard(modifier = Modifier.fillMaxWidth(), padding = 24.dp) {
        Text(
          text = strings.settingsIdentityTitle,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Normal,
          color = tokens.ink,
        )
        Spacer(modifier = Modifier.height(12.dp))

        ResidenceHeroBanner(
          residenceName = residenceName.ifBlank { strings.settingsResidenceName },
          subtitle = address.ifBlank { city },
          badgeText = strings.residenceCustomizePhotoShort,
          compact = true,
          onPhotoFeedback = onMessage,
        )

        Spacer(modifier = Modifier.height(14.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          FintechTextField(
            value = residenceName,
            onValueChange = { residenceName = it.take(120) },
            label = strings.settingsResidenceName,
            modifier = Modifier.fillMaxWidth().testTag("settings_residence_input"),
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom,
          ) {
            FintechTextField(
              value = city,
              onValueChange = { city = it.take(100) },
              label = strings.settingsCity,
              modifier = Modifier.weight(1f).testTag("settings_city_input"),
            )
            FintechTextField(
              value = treasurerName,
              onValueChange = { treasurerName = it.take(100) },
              label = strings.settingsTreasurer,
              modifier = Modifier.weight(1f).testTag("settings_treasurer_input"),
            )
          }
          FintechTextField(
            value = address,
            onValueChange = { address = it.take(200) },
            label = strings.settingsAddress,
            modifier = Modifier.fillMaxWidth().testTag("settings_address_input"),
          )
          FintechTextField(
            value = syndicatName,
            onValueChange = { syndicatName = it.take(100) },
            label = strings.settingsSyndicatName,
            modifier = Modifier.fillMaxWidth(),
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            FintechTextField(
              value = buildingTitle,
              onValueChange = { buildingTitle = it.take(180) },
              label = strings.settingsBuildingTitle,
              modifier = Modifier.weight(1f),
            )
            SageSecondaryButton(
              text = strings.settingsAutoTitleBtn,
              compact = true,
              onClick = {
                val yr = yearText.ifBlank { "2026" }
                buildingTitle = if (isAr) {
                  "المساهمة الشهرية $yr – $residenceName"
                } else {
                  "COTISATION MENSUELLE $yr – $residenceName"
                }
              },
            )
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom,
          ) {
            FintechTextField(
              value = yearText,
              onValueChange = { yearText = it.filter { c -> c.isDigit() }.take(4) },
              label = strings.settingsYear,
              keyboardType = KeyboardType.Number,
              modifier = Modifier.weight(1f),
            )
            FintechTextField(
              value = currency,
              onValueChange = { currency = it.take(15) },
              label = strings.settingsCurrency,
              modifier = Modifier.weight(1f),
            )
          }
          FintechTextField(
            value = openingBalanceText,
            onValueChange = { next ->
              openingBalanceText = next.filterIndexed { i, c -> c.isDigit() || (i == 0 && c == '-') }.take(9)
            },
            label = strings.settingsOpeningBalance(currency),
            keyboardType = KeyboardType.Number,
            modifier = Modifier.fillMaxWidth().testTag("settings_opening_balance_input"),
          )
          FintechTextField(
            value = footerNote,
            onValueChange = { footerNote = it.take(300) },
            label = strings.settingsFooterNote,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }
    }

    // 4. Floors & Apartments (Uniform vs Variable per Floor)
    item {
      val generatedLabels = buildFloorLabels(includeRdc, floorsCount)
      FintechWhiteCard(modifier = Modifier.fillMaxWidth(), padding = 24.dp) {
        Text(
          text = strings.settingsStructureTitle,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Normal,
          color = tokens.ink,
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .heightIn(min = 48.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = strings.settingsIncludeRdc,
            style = MaterialTheme.typography.bodyMedium,
            color = tokens.ink,
            modifier = Modifier.weight(1f),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Switch(checked = includeRdc, onCheckedChange = { includeRdc = it })
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = strings.settingsFloorsCount,
            style = MaterialTheme.typography.bodyMedium,
            color = tokens.ink,
            modifier = Modifier.weight(1f),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RoundIconCircleButton(
              icon = Icons.Default.Remove,
              contentDescription = "-",
              onClick = { if (floorsCount > 1) floorsCount-- },
            )
            Text(
              text = localizeDigits(floorsCount.toString(), cfg.digitsAr),
              style = MaterialTheme.typography.titleLarge,
              fontFamily = monoOrDefault ?: MaterialTheme.typography.titleLarge.fontFamily,
              fontWeight = FontWeight.Medium,
              color = tokens.ink,
            )
            RoundIconCircleButton(
              icon = Icons.Default.Add,
              contentDescription = "+",
              onClick = { if (floorsCount < 20) floorsCount++ },
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          SyndicatFilterChip(
            selected = !isVariablePerFloor,
            onClick = { isVariablePerFloor = false },
            label = strings.wizardSimpleMode,
          )
          SyndicatFilterChip(
            selected = isVariablePerFloor,
            onClick = { isVariablePerFloor = true },
            label = strings.wizardAdvancedMode,
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (!isVariablePerFloor) {
          FintechTextField(
            value = uniformTotalApptsText,
            onValueChange = { uniformTotalApptsText = it.filter { c -> c.isDigit() }.take(3) },
            label = strings.settingsApartmentsCount,
            keyboardType = KeyboardType.Number,
            modifier = Modifier.fillMaxWidth().testTag("settings_appts_count_input"),
          )
        } else {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            generatedLabels.forEachIndexed { idx, rawFloor ->
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = strings.floorWithPrefix(rawFloor),
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Medium,
                  color = tokens.ink,
                  modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  RoundIconCircleButton(
                    icon = Icons.Default.Remove,
                    contentDescription = "-",
                    onClick = { if (perFloorCounts[idx] > 1) perFloorCounts[idx]-- },
                  )
                  Text(
                    text = localizeDigits(perFloorCounts[idx].toString(), cfg.digitsAr),
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = monoOrDefault ?: MaterialTheme.typography.titleMedium.fontFamily,
                    fontWeight = FontWeight.Medium,
                    color = tokens.ink,
                  )
                  RoundIconCircleButton(
                    icon = Icons.Default.Add,
                    contentDescription = "+",
                    onClick = { if (perFloorCounts[idx] < 40) perFloorCounts[idx]++ },
                  )
                }
              }
            }
          }
        }
      }
    }

    // 5. Cotisations & 12-Month Custom Tariffs (AirtableSoftCard — alternance après FintechWhiteCard)
    item {
      AirtableSoftCard(modifier = Modifier.fillMaxWidth(), padding = 24.dp) {
        Text(
          text = strings.settingsFeesTitle,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Normal,
          color = tokens.ink,
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.Bottom,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          FintechTextField(
            value = defaultFeeText,
            onValueChange = { defaultFeeText = it.filter { c -> c.isDigit() }.take(6) },
            label = strings.settingsDefaultFee(currency),
            keyboardType = KeyboardType.Number,
            modifier = Modifier.weight(1f).testTag("settings_default_fee_input"),
          )
          SageSecondaryButton(
            text = strings.settingsApplyDefaultToAll,
            compact = true,
            onClick = {
              val v = defaultFeeText.ifBlank { "50" }
              MonthColumn.entries.forEach { m -> monthFeeTexts[m] = v }
            },
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        FintechTextField(
          value = dueDayText,
          onValueChange = { dueDayText = it.filter { c -> c.isDigit() }.take(2) },
          label = strings.settingsDueDayLabel,
          keyboardType = KeyboardType.Number,
          modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = strings.settingsCutoffMonthLabel,
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Medium,
          color = tokens.ink,
        )
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          MonthColumn.entries.forEach { col ->
            SyndicatFilterChip(
              selected = cutoffMonth == col.monthNumber,
              onClick = { cutoffMonth = col.monthNumber },
              label = strings.monthShort(col),
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          SageSecondaryButton(
            text = strings.settingsPresetFullYear,
            compact = true,
            onClick = {
              MonthColumn.entries.forEach { m -> activeMonthSet[m.monthNumber] = true }
            },
          )
          SageSecondaryButton(
            text = strings.settingsPresetJunDec,
            compact = true,
            onClick = {
              MonthColumn.entries.forEach { m ->
                activeMonthSet[m.monthNumber] = m.monthNumber in 6..12
              }
            },
          )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = strings.settingsActiveMonthsLabel,
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Medium,
          color = tokens.ink,
        )
        Spacer(modifier = Modifier.height(8.dp))

        val defVal = defaultFeeText.toLongOrNull() ?: 50L
        MonthColumn.entries.chunked(2).forEach { pair ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            pair.forEach { col ->
              val enabled = activeMonthSet[col.monthNumber] == true
              val feeLong = monthFeeTexts[col]?.toLongOrNull() ?: defVal
              Surface(
                shape = RoundedCornerShape(tokens.fieldRadiusDp.dp),
                color = if (enabled) tokens.cardBg else tokens.cardBg.copy(alpha = 0.55f),
                border = BorderStroke(1.dp, tokens.divider),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                modifier = Modifier.weight(1f),
              ) {
                Column(
                  modifier = Modifier.padding(8.dp),
                  verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                      .fillMaxWidth()
                      .minimumInteractiveComponentSize()
                      .heightIn(min = 48.dp)
                      .clickable {
                        activeMonthSet[col.monthNumber] = !enabled
                      },
                  ) {
                    Checkbox(
                      checked = enabled,
                      onCheckedChange = { activeMonthSet[col.monthNumber] = it },
                    )
                    Text(
                      text = strings.monthFull(col),
                      style = MaterialTheme.typography.labelLarge,
                      fontWeight = FontWeight.Medium,
                      color = if (enabled) cotisationColorForAmount(feeLong, defVal) else tokens.mute,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                  }
                  if (enabled) {
                    FintechTextField(
                      value = monthFeeTexts[col] ?: "",
                      onValueChange = { next ->
                        monthFeeTexts[col] = next.filter { it.isDigit() }.take(6)
                      },
                      label = "",
                      trailingIcon = {
                        Text(
                          text = currency,
                          style = MaterialTheme.typography.labelSmall,
                          color = tokens.mute,
                          modifier = Modifier.padding(horizontal = 4.dp),
                        )
                      },
                      keyboardType = KeyboardType.Number,
                      modifier = Modifier.fillMaxWidth().testTag("fee_month_${col.key}"),
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    // =========================================================================
    // 3. COMMUNICATION, WIDGETS & CONSEIL
    // =========================================================================
    item(key = "settings_group_3_header") {
      SettingsGroupHeader(
        title = strings.settingsGroupToolsGovernance,
        tag = "settings_group_3",
      )
    }

    // Modèles WhatsApp & Relance
    item(key = "settings_whatsapp_item") {
      FintechWhiteCard(modifier = Modifier.fillMaxWidth(), padding = 24.dp) {
        Text(
          text = strings.settingsWhatsappTitle,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Normal,
          color = tokens.ink,
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = strings.settingsWhatsappPresetsLabel,
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Medium,
          color = tokens.ink,
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          listOf(
            WhatsAppTemplatePreset.FRIENDLY_AR to strings.whatsappPresetFriendlyAr,
            WhatsAppTemplatePreset.URGENT_AR to strings.whatsappPresetUrgentAr,
            WhatsAppTemplatePreset.FRIENDLY_FR to strings.whatsappPresetFriendlyFr,
            WhatsAppTemplatePreset.URGENT_FR to strings.whatsappPresetUrgentFr,
          ).forEach { (preset, label) ->
            val presetText = preset.templateText()
            SyndicatFilterChip(
              selected = whatsappTemplate.trim() == presetText.trim(),
              onClick = { whatsappTemplate = presetText },
              label = label,
              modifier = Modifier.testTag("settings_wa_preset_${preset.id}"),
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        FintechTextField(
          value = whatsappTemplate,
          onValueChange = { whatsappTemplate = it.take(950) },
          label = strings.settingsWhatsappTemplateLabel,
          singleLine = false,
          maxLines = 6,
          modifier = Modifier.fillMaxWidth().testTag("settings_whatsapp_template_input"),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = strings.settingsWhatsappVarsHint,
          style = MaterialTheme.typography.labelSmall,
          color = tokens.body,
        )
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          WhatsAppHelper.SUPPORTED_VARIABLES.forEach { tokenVar ->
            Surface(
              shape = RoundedCornerShape(100.dp),
              color = tokens.surfaceSoft,
              border = BorderStroke(1.dp, tokens.hairline),
              modifier = Modifier
                .minimumInteractiveComponentSize()
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(100.dp))
                .clickable { whatsappTemplate = "$whatsappTemplate $tokenVar".take(950) },
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
              ) {
                Text(
                  text = "+ $tokenVar",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = tokens.inkDeep,
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Live Preview of Automatic Generated WhatsApp Message
        val sampleApt = data.lateApartments.firstOrNull()
        val livePreviewText = WhatsAppHelper.buildSamplePreviewMessage(
          config = buildUpdatedConfig(),
          overrideTemplate = whatsappTemplate,
          sampleApartment = sampleApt,
        )
        Surface(
          shape = RoundedCornerShape(tokens.contentCardRadiusDp.dp),
          color = tokens.surfaceSoft,
          border = BorderStroke(1.dp, tokens.divider),
          tonalElevation = 0.dp,
          shadowElevation = 0.dp,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("settings_whatsapp_live_preview"),
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Text(
              text = strings.settingsWhatsappLivePreviewLabel,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Medium,
              color = tokens.mute,
            )
            Text(
              text = livePreviewText,
              style = MaterialTheme.typography.bodySmall,
              color = tokens.ink,
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))
        FintechTextField(
          value = minReminderText,
          onValueChange = { minReminderText = it.filter { c -> c.isDigit() }.take(5) },
          label = strings.settingsMinReminderLabel(currency),
          keyboardType = KeyboardType.Number,
          modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(10.dp))
        FintechTextField(
          value = relanceHeaderNote,
          onValueChange = { relanceHeaderNote = it.take(280) },
          label = strings.settingsRelanceHeaderLabel,
          singleLine = false,
          maxLines = 3,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    }

    // Partage Conseil Syndical (QR Code)
    item(key = "settings_council_share_item") {
      CouncilShareCard(
        data = data,
        specialCalls = specialCalls,
        roleMode = roleMode,
        currentUserId = currentUserId,
        onSetRoleMode = onSetRoleMode,
        onImportSnapshot = onImportCouncilSnapshot,
        onMessage = onMessage,
      )
    }

    // Multi-immeubles & Blocs
    item(key = "settings_multi_building_item") {
      MultiBuildingManagementCard(
        buildings = buildings,
        activeBuildingId = activeBuildingId,
        isReadOnlyMode = isReadOnlyMode,
        onSelectBuilding = onSelectBuilding,
        onCreateBuilding = onCreateBuilding,
        onDeleteBuilding = onDeleteBuilding,
      )
    }

    // Studio Widget Écran d'accueil
    item(key = "settings_widget_studio_item") {
      SettingsWidgetStudioCard(
        data = data,
        onMessage = onMessage,
      )
    }

    // Bouton principal d'enregistrement des paramètres
    item(key = "settings_save_cta_item") {
      MetaCobaltBuyButton(
        text = strings.settingsSaveButton,
        icon = Icons.Default.Save,
        enabled = !isSaving && !isReadOnlyMode,
        onClick = { onSaveConfig(buildUpdatedConfig(), false) },
        modifier = Modifier.fillMaxWidth().testTag("save_settings_button"),
      )
    }

    // =========================================================================
    // 4. SÉCURITÉ, RAPPELS & DONNÉES
    // =========================================================================
    item(key = "settings_group_4_header") {
      SettingsGroupHeader(
        title = strings.settingsGroupSecurityData,
        tag = "settings_group_4",
      )
    }

    // 4a. Sécurité (PIN & Biométrie), Rappels mensuels & Journal d'audit
    item(key = "settings_security_reminders_item") {
      FintechWhiteCard(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("settings_security_reminders_card"),
        padding = 24.dp,
      ) {
        Text(
          text = strings.settingsSecurityRemindersTitle,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Normal,
          color = tokens.ink,
        )
        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = strings.settingsPinTitle,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = tokens.ink,
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            SageSecondaryButton(
              text = strings.settingsSetPinBtn,
              compact = true,
              onClick = { showPinDialog = true },
              modifier = Modifier.weight(1f).testTag("set_pin_btn"),
            )
            if (hasPin) {
              InkTertiaryButton(
                text = strings.settingsDisablePinBtn,
                onClick = {
                  LanguagePreferences.savePinCode(context, "")
                  LanguagePreferences.setBiometricUnlockEnabled(context, false)
                  hasPin = false
                  biometricEnabled = false
                  onPinUpdated("")
                },
                modifier = Modifier.weight(1f).testTag("disable_pin_btn"),
              )
            }
          }

          if (bioAvailable) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = strings.settingsBiometricLabel,
                  style = MaterialTheme.typography.bodyMedium,
                  color = tokens.ink,
                )
                if (!hasPin) {
                  Text(
                    text = strings.settingsBiometricHint,
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.mute,
                  )
                }
              }
              Switch(
                checked = hasPin && biometricEnabled,
                enabled = hasPin,
                onCheckedChange = { enabled ->
                  biometricEnabled = enabled
                  LanguagePreferences.setBiometricUnlockEnabled(context, enabled)
                },
                modifier = Modifier.testTag("biometric_unlock_switch"),
              )
            }
          }

          HorizontalDivider(color = tokens.divider)

          Text(
            text = strings.settingsReminderTitle,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = tokens.ink,
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = strings.settingsReminderEnableLabel,
              style = MaterialTheme.typography.bodyMedium,
              color = tokens.ink,
              modifier = Modifier.weight(1f),
            )
            Switch(
              checked = reminderEnabled,
              onCheckedChange = { enable ->
                if (enable) {
                  if (MonthlyReminderScheduler.hasNotificationPermission(context)) {
                    reminderEnabled = true
                    LanguagePreferences.setMonthlyReminderEnabled(context, true)
                    MonthlyReminderScheduler.scheduleMonthlyReminder(context, reminderDay, true)
                  } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pendingTestReminder = false
                    notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                  }
                } else {
                  reminderEnabled = false
                  LanguagePreferences.setMonthlyReminderEnabled(context, false)
                  MonthlyReminderScheduler.scheduleMonthlyReminder(context, reminderDay, false)
                }
              },
              modifier = Modifier.testTag("monthly_reminder_switch"),
            )
          }
          Text(
            text = strings.settingsReminderDayLabel,
            style = MaterialTheme.typography.labelSmall,
            color = tokens.body,
          )
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            listOf(1, 5, 10, 15).forEach { dayOpt ->
              SyndicatFilterChip(
                selected = reminderDay == dayOpt,
                onClick = {
                  reminderDay = dayOpt
                  LanguagePreferences.setMonthlyReminderDay(context, dayOpt)
                  if (reminderEnabled) {
                    MonthlyReminderScheduler.scheduleMonthlyReminder(context, dayOpt, true)
                  }
                },
                label = localizeDigits(dayOpt.toString(), cfg.digitsAr),
                modifier = Modifier.testTag("reminder_day_$dayOpt"),
              )
            }
          }
          SageSecondaryButton(
            text = strings.settingsReminderTestBtn,
            compact = true,
            onClick = {
              if (MonthlyReminderScheduler.hasNotificationPermission(context)) {
                MonthlyReminderScheduler.showReminderNotificationNow(context, data.lateApartments.size)
              } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pendingTestReminder = true
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
              }
            },
            modifier = Modifier.fillMaxWidth().testTag("test_monthly_reminder_btn"),
          )

          HorizontalDivider(color = tokens.divider)

          SageSecondaryButton(
            text = "${strings.settingsAuditOpenBtn} (${localizeDigits(auditLogs.size.toString(), cfg.digitsAr)})",
            onClick = { showAuditDialog = true },
            modifier = Modifier.fillMaxWidth().testTag("open_audit_log_btn"),
          )
        }
      }
    }

    // 4b. Sauvegardes & Exports (JSON / CSV)
    item(key = "settings_backup_data_item") {
      AirtableSoftCard(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("settings_backup_data_card"),
        padding = 24.dp,
      ) {
        Text(
          text = strings.settingsBackupDataTitle,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Normal,
          color = tokens.ink,
        )
        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          SageSecondaryButton(
            text = strings.settingsExportJsonBtn,
            onClick = {
              val specialCalls = SyndicatExtrasStore.getInstance(context).specialCalls.value
              BackupManager.exportAndShareJsonBackup(
                context = context,
                data = data,
                onError = { err -> onMessage(err) },
                specialCalls = specialCalls,
              )
              onBackupExported()
            },
            modifier = Modifier.fillMaxWidth().testTag("export_json_backup_btn"),
          )

          SageSecondaryButton(
            text = strings.settingsImportJsonBtn,
            enabled = !isReadOnlyMode,
            onClick = {
              jsonImportLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
            },
            modifier = Modifier.fillMaxWidth().testTag("import_json_backup_btn"),
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            SageSecondaryButton(
              text = strings.exportPaymentsCsvBtn,
              compact = true,
              onClick = {
                val csv = BackupManager.generatePaymentsCsv(data)
                BackupManager.shareCsvFile(
                  context = context,
                  filePrefix = "payments_${cfg.year}",
                  csvContent = csv,
                  chooserTitle = strings.exportPaymentsCsvBtn,
                  onError = { err -> onMessage(err) },
                )
              },
              modifier = Modifier.weight(1f).testTag("settings_export_payments_csv_btn"),
            )
            SageSecondaryButton(
              text = strings.exportExpensesCsvBtn,
              compact = true,
              onClick = {
                val csv = BackupManager.generateExpensesCsv(data)
                BackupManager.shareCsvFile(
                  context = context,
                  filePrefix = "expenses_${cfg.year}",
                  csvContent = csv,
                  chooserTitle = strings.exportExpensesCsvBtn,
                  onError = { err -> onMessage(err) },
                )
              },
              modifier = Modifier.weight(1f).testTag("settings_export_expenses_csv_btn"),
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            SageSecondaryButton(
              text = strings.settingsImportCsvBtn,
              compact = true,
              enabled = !isReadOnlyMode,
              onClick = { showCsvPasteDialog = true },
              modifier = Modifier.weight(1f).testTag("open_csv_paste_btn"),
            )
            SageSecondaryButton(
              text = "CSV File",
              compact = true,
              enabled = !isReadOnlyMode,
              onClick = {
                csvFileImportLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "text/plain", "*/*"))
              },
            )
          }
        }
      }
    }

    // 4c. Stockage local & Synchronisation Google Cloud (optionnel)
    item(key = "settings_sync_item") {
      FintechWhiteCard(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("settings_sync_card"),
        padding = 24.dp,
      ) {
        Text(
          text = strings.syncSectionTitle,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Normal,
          color = tokens.inkDeep,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
          shape = RoundedCornerShape(tokens.badgeRadiusDp.dp),
          color = tokens.primaryTint,
          tonalElevation = 0.dp,
          shadowElevation = 0.dp,
        ) {
          Text(
            text = if (isCloudSynced) strings.syncCloudBadge else strings.syncLocalBadge,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = tokens.linkColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
          )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = if (isCloudSynced) strings.syncCloudDesc else strings.syncLocalDesc,
          style = MaterialTheme.typography.bodySmall,
          color = tokens.body,
        )
        if (isCloudSynced && !userEmail.isNullOrBlank()) {
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = strings.settingsConnectedAs(userEmail),
            style = MaterialTheme.typography.labelMedium,
            color = tokens.inkDeep,
          )
        }
        Spacer(modifier = Modifier.height(14.dp))
        if (!isCloudSynced) {
          MetaCobaltBuyButton(
            text = strings.syncConnectGoogleBtn,
            onClick = onConnectGoogleSync,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("settings_connect_google_btn"),
          )
        } else {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SageSecondaryButton(
              text = strings.syncPushLocalBtn,
              compact = true,
              onClick = onPushLocalToCloud,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("settings_push_local_btn"),
            )
            InkTertiaryButton(
              text = strings.signOutToLocal,
              onClick = onSignOut,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("settings_disconnect_cloud_btn"),
            )
          }
        }
      }
    }

    // =========================================================================
    // 5. ZONE AVANCÉE & APPLICATION (En bas)
    // =========================================================================
    item(key = "settings_group_5_header") {
      SettingsGroupHeader(
        title = strings.settingsGroupAdvancedAbout,
        tag = "settings_group_5",
      )
    }

    // Réinitialisation & Déconnexion
    item(key = "settings_danger_reset_item") {
      AirtableSoftCard(modifier = Modifier.fillMaxWidth(), padding = 24.dp) {
        Text(
          text = strings.settingsDangerResetTitle,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Normal,
          color = tokens.ink,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          SageSecondaryButton(
            text = if (armBlankReset) "! ${strings.settingsConfirmBtn}: ${strings.settingsNewBlankBuildingBtn}" else strings.settingsNewBlankBuildingBtn,
            enabled = !isReadOnlyMode,
            onClick = {
              if (armBlankReset) {
                armBlankReset = false
                onSaveConfig(buildUpdatedConfig(), true)
              } else {
                armBlankReset = true
              }
            },
            modifier = Modifier.fillMaxWidth().testTag("new_blank_building_btn"),
          )

          SageSecondaryButton(
            text = if (armC49Reset) "! ${strings.settingsConfirmBtn}: ${strings.settingsResetC49Btn}" else strings.settingsResetC49Btn,
            enabled = !isReadOnlyMode,
            onClick = {
              if (armC49Reset) {
                armC49Reset = false
                onResetC49()
              } else {
                armC49Reset = true
              }
            },
            modifier = Modifier.fillMaxWidth().testTag("reset_c49_btn"),
          )

          if (isCloudSynced) {
            HorizontalDivider(color = tokens.divider)

            if (!userEmail.isNullOrBlank()) {
              Text(
                text = strings.settingsConnectedAs(userEmail),
                style = MaterialTheme.typography.bodySmall,
                color = tokens.body,
              )
            }

            InkTertiaryButton(
              text = strings.signOutToLocal,
              onClick = onSignOut,
              modifier = Modifier.fillMaxWidth().testTag("sign_out_button"),
            )
          }
        }
      }
    }

    // À propos de l'application (SYNDICAT · Éditeur DYNIC · Version 2.0 · Édition 2026)
    item(key = "settings_about_item") {
      SyndicatAboutEditionCard()
    }
  }

  if (showPinDialog) {
    PinSetupDialog(
      onDismiss = { showPinDialog = false },
      onSavePin = { newPin ->
        LanguagePreferences.savePinCode(context, newPin)
        hasPin = true
        onPinUpdated(newPin)
        showPinDialog = false
      },
    )
  }

  if (showCsvPasteDialog) {
    SyndicatDialog(
      onDismissRequest = { showCsvPasteDialog = false },
      title = {
        Text(
          text = strings.settingsImportCsvBtn,
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
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text = "1;عبد الفتاح;Abdelfetah;0612345678",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = monoOrDefault ?: MaterialTheme.typography.labelSmall.fontFamily,
            color = tokens.body,
          )
          FintechTextField(
            value = csvPasteText,
            onValueChange = { csvPasteText = it },
            label = "CSV",
            singleLine = false,
            minLines = 4,
            maxLines = 8,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("csv_paste_input"),
          )
        }
      },
      confirmButton = {
        MetaCobaltBuyButton(
          text = strings.apptSave,
          compact = true,
          onClick = {
            val rows = BackupManager.parseOwnersBulkLines(csvPasteText)
            if (rows.isNotEmpty()) {
              onImportCsvOwners(rows)
            }
            showCsvPasteDialog = false
          },
          modifier = Modifier.testTag("csv_paste_confirm_btn"),
        )
      },
      dismissButton = {
        SageSecondaryButton(
          text = strings.apptCancel,
          compact = true,
          onClick = { showCsvPasteDialog = false },
        )
      },
    )
  }

  if (showAuditDialog) {
    SyndicatDialog(
      onDismissRequest = { showAuditDialog = false },
      title = {
        Text(
          text = strings.auditDialogTitle,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Normal,
          color = tokens.ink,
        )
      },
      text = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 380.dp)
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          if (auditLogs.isEmpty()) {
            SyndicatEmptyStateCard(
              icon = Icons.Default.History,
              title = strings.auditDialogTitle,
              subtitle = strings.auditEmptyText,
            )
          } else {
            auditLogs.take(120).forEach { entry ->
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = tokens.surfaceSoft,
                border = BorderStroke(1.dp, tokens.divider),
                modifier = Modifier.fillMaxWidth(),
              ) {
                Column(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                  verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                  ) {
                    Text(
                      text = entry.actionType.code.uppercase(),
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      color = tokens.infoColor,
                    )
                    Text(
                      text = localizeDigits(entry.formattedDateTime(), cfg.digitsAr),
                      style = MaterialTheme.typography.labelSmall,
                      color = tokens.mute,
                    )
                  }
                  Text(
                    text = entry.displaySummary(isAr),
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.ink,
                  )
                }
              }
            }
          }
        }
      },
      confirmButton = {
        if (auditLogs.isNotEmpty()) {
          MetaCobaltBuyButton(
            text = strings.auditExportCsvBtn,
            compact = true,
            onClick = {
              val csv = BackupManager.generateAuditLogCsv(auditLogs, isAr)
              BackupManager.shareCsvFile(
                context = context,
                filePrefix = "audit_log_${cfg.year}",
                csvContent = csv,
                chooserTitle = strings.auditDialogTitle,
                onError = { err -> onMessage(err) },
              )
            },
            modifier = Modifier.testTag("audit_export_csv_btn"),
          )
        }
      },
      dismissButton = {
        SageSecondaryButton(
          text = strings.apptCancel,
          compact = true,
          onClick = { showAuditDialog = false },
        )
      },
    )
  }
}

@Composable
private fun SettingsGroupHeader(
  title: String,
  tag: String,
) {
  val tokens = LocalSyndicatTokens.current
  Text(
    text = title,
    style = MaterialTheme.typography.labelLarge,
    fontWeight = FontWeight.SemiBold,
    color = tokens.primary,
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 8.dp, bottom = 2.dp, start = 4.dp, end = 4.dp)
      .testTag(tag),
  )
}
