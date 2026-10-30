package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.ApartmentRecord
import com.example.data.model.MonthColumn
import com.example.data.model.SyndicatConfig
import com.example.data.model.UnitStatus
import com.example.data.model.buildFloorLabels
import com.example.ui.components.FintechPalePositiveCard
import com.example.ui.components.FintechTextField
import com.example.ui.components.FintechWhiteCard
import com.example.ui.components.InkTertiaryButton
import com.example.ui.components.MetaCobaltBuyButton
import com.example.ui.components.RoundIconCircleButton
import com.example.ui.components.SageSecondaryButton
import com.example.ui.components.SyndicatDialog
import com.example.ui.components.SyndicatFilterChip
import com.example.ui.components.SyndicatProgressBar
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalStrings
import com.example.ui.i18n.formatNumber
import com.example.ui.theme.LocalSyndicatTokens
import com.example.ui.viewmodel.formatDh
import com.example.util.BackupManager

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingWizardScreen(
  initialConfig: SyndicatConfig,
  existingApartments: List<ApartmentRecord>,
  onCancel: () -> Unit,
  onComplete: (SyndicatConfig, List<ApartmentRecord>, Boolean) -> Unit,
) {
  val context = LocalContext.current
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val isAr = strings.language == AppLanguage.AR
  val numericFont = tokens.numericFontFamily ?: tokens.monoFontFamily

  var currentStep by remember { mutableIntStateOf(1) }

  // Step 1: Building
  var residenceName by remember { mutableStateOf(initialConfig.residenceName) }
  var city by remember { mutableStateOf(initialConfig.city) }
  var treasurerName by remember { mutableStateOf(initialConfig.treasurerName) }
  var syndicatName by remember { mutableStateOf(initialConfig.syndicatName) }

  // Step 2: Floors & Units
  var includeRdc by remember { mutableStateOf(initialConfig.includeRdc) }
  var floorsCount by remember { mutableIntStateOf(initialConfig.floorsCount.toInt().coerceIn(1, 20)) }
  var isAdvancedPerFloor by remember {
    mutableStateOf(
      initialConfig.floorAptCounts.isNotEmpty() &&
        initialConfig.floorAptCounts.distinct().size > 1
    )
  }
  var uniformPerFloor by remember {
    val base = (initialConfig.apartmentsCount.toInt() / initialConfig.floorsCount.toInt().coerceAtLeast(1)).coerceIn(1, 20)
    mutableIntStateOf(base)
  }
  val perFloorCounts = remember {
    mutableStateListOf<Int>().apply {
      val initCounts = initialConfig.effectiveFloorCounts()
      for (i in 0 until 20) {
        add(initCounts.getOrElse(i) { uniformPerFloor })
      }
    }
  }

  val generatedFloors = buildFloorLabels(includeRdc, floorsCount)
  val computedFloorCounts: List<Int> = if (isAdvancedPerFloor) {
    List(floorsCount) { idx -> perFloorCounts.getOrElse(idx) { 4 }.coerceIn(1, 30) }
  } else {
    List(floorsCount) { uniformPerFloor }
  }
  val totalApartmentsCount = computedFloorCounts.sum().coerceIn(1, 120)

  val draftApartments = remember {
    val byNum = existingApartments.associateBy { it.number.toInt() }
    mutableStateListOf<ApartmentRecord>().apply {
      for (n in 1..120) {
        val existing = byNum[n]
        add(
          existing ?: ApartmentRecord(
            id = "appt_%02d".format(n),
            number = n.toLong(),
            status = "occupied",
          )
        )
      }
    }
  }

  fun floorForNumberInDraft(aptNum: Int): String {
    var cum = 0
    for (i in generatedFloors.indices) {
      cum += computedFloorCounts.getOrElse(i) { 0 }
      if (aptNum <= cum) return generatedFloors[i]
    }
    return generatedFloors.last()
  }

  var contactTargetAptIndex by remember { mutableIntStateOf(-1) }
  val contactPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
  ) { result ->
    if (result.resultCode == Activity.RESULT_OK && contactTargetAptIndex in 0 until draftApartments.size) {
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
            val name = cursor.getString(0) ?: ""
            val phone = cursor.getString(1) ?: ""
            val cur = draftApartments[contactTargetAptIndex]
            val hasArabic = name.any { it.code in 0x0600..0x06FF }
            draftApartments[contactTargetAptIndex] = cur.copy(
              ownerNameAr = if (hasArabic) name else cur.ownerNameAr.ifBlank { name },
              ownerName = if (!hasArabic) name else cur.ownerName,
              phone = phone,
              status = "occupied",
            )
          }
        }
      } catch (_: Exception) {
      }
    }
  }

  var showBulkTextDialog by remember { mutableStateOf(false) }
  var bulkRawInput by remember { mutableStateOf("") }

  // Step 4: Cotisation
  var yearText by remember { mutableStateOf(initialConfig.year.toString()) }
  var defaultFeeText by remember { mutableStateOf(initialConfig.defaultMonthlyFee.toString()) }
  var dueDayText by remember { mutableStateOf(initialConfig.dueDayOfMonth.toString()) }
  var cutoffMonth by remember { mutableIntStateOf(initialConfig.cutoffMonth.toInt().coerceIn(1, 12)) }
  var draftFeeConfig by remember { mutableStateOf(initialConfig) }

  // Step 5: History & Opening Balance
  var openingBalanceText by remember { mutableStateOf(initialConfig.openingBalanceDh.toString()) }
  var startFromZeroPayments by remember { mutableStateOf(false) }

  fun buildFinalConfig(): SyndicatConfig {
    val yr = yearText.toLongOrNull()?.coerceIn(2020L, 2100L) ?: 2026L
    val defFee = defaultFeeText.toLongOrNull()?.coerceIn(0L, 100_000L) ?: 50L
    val openBal = openingBalanceText.toLongOrNull()?.coerceIn(-10_000_000L, 100_000_000L) ?: 0L
    val dueDay = dueDayText.toLongOrNull()?.coerceIn(1L, 31L) ?: 5L
    val cleanRes = residenceName.trim().ifBlank { "الإقامة" }
    val autoTitle = if (isAr) {
      "المساهمة الشهرية $yr – $cleanRes"
    } else {
      "Cotisation mensuelle $yr – $cleanRes"
    }
    return draftFeeConfig.copy(
      syndicatName = syndicatName.trim().ifBlank { "إدارة السانديك" },
      residenceName = cleanRes,
      city = city.trim(),
      treasurerName = treasurerName.trim(),
      buildingTitle = autoTitle,
      year = yr,
      defaultMonthlyFee = defFee,
      openingBalanceDh = openBal,
      dueDayOfMonth = dueDay,
      cutoffMonth = cutoffMonth.toLong(),
      includeRdc = includeRdc,
      floorsCount = floorsCount.toLong(),
      apartmentsCount = totalApartmentsCount.toLong(),
      floorAptCounts = computedFloorCounts.map { it.toLong() },
      themeStyle = com.example.ui.theme.sanitizeThemeStyle(initialConfig.themeStyle, "coinbase"),
      onboardingCompleted = true,
      seeded = true,
    )
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(tokens.screenBg)
      .testTag("onboarding_wizard_screen"),
  ) {
    // §6 Assistant d'installation: fine 4dp progress bar + mute eyebrow « الخطوة X من 6 »
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = strings.wizardStepIndicator(currentStep, 6),
          style = MaterialTheme.typography.labelMedium,
          color = tokens.mute,
        )
        RoundIconCircleButton(
          icon = Icons.Default.Close,
          contentDescription = strings.apptCancel,
          onClick = onCancel,
        )
      }
      Spacer(modifier = Modifier.height(10.dp))
      SyndicatProgressBar(
        progress = currentStep / 6f,
        modifier = Modifier.fillMaxWidth(),
      )
    }

    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      when (currentStep) {
        1 -> {
          item {
            WizardStepHeader(strings.wizardStep1Title, strings.wizardStep1Sub)
          }
          item {
            FintechWhiteCard(modifier = Modifier.fillMaxWidth(), padding = 24.dp) {
              Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FintechTextField(
                  value = residenceName,
                  onValueChange = { residenceName = it },
                  label = strings.settingsResidenceName,
                  placeholder = "C49 GH12 – بيت المستقبل 2",
                  modifier = Modifier.fillMaxWidth().testTag("wizard_residence_input"),
                )
                FintechTextField(
                  value = city,
                  onValueChange = { city = it },
                  label = strings.settingsCity,
                  placeholder = if (isAr) "الدار البيضاء / طنجة / الرباط…" else "Casablanca / Rabat…",
                  modifier = Modifier.fillMaxWidth(),
                )
                FintechTextField(
                  value = treasurerName,
                  onValueChange = { treasurerName = it },
                  label = strings.settingsTreasurer,
                  placeholder = if (isAr) "اسم أمين المال" else "Nom du trésorier",
                  modifier = Modifier.fillMaxWidth(),
                )
                FintechTextField(
                  value = syndicatName,
                  onValueChange = { syndicatName = it },
                  label = strings.settingsSyndicatName,
                  modifier = Modifier.fillMaxWidth(),
                )
              }
            }
          }
        }

        2 -> {
          item {
            WizardStepHeader(strings.wizardStep2Title, strings.wizardStep2Sub)
          }
          item {
            FintechWhiteCard(modifier = Modifier.fillMaxWidth(), padding = 24.dp) {
              Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                    style = MaterialTheme.typography.titleMedium,
                    color = tokens.ink,
                    modifier = Modifier.weight(1f),
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Switch(checked = includeRdc, onCheckedChange = { includeRdc = it })
                }

                WizardCounterRow(
                  label = strings.settingsFloorsCount,
                  value = floorsCount,
                  onDec = { if (floorsCount > 1) floorsCount-- },
                  onInc = { if (floorsCount < 20) floorsCount++ },
                )

                FlowRow(
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  SyndicatFilterChip(
                    selected = !isAdvancedPerFloor,
                    onClick = { isAdvancedPerFloor = false },
                    label = strings.wizardSimpleMode,
                  )
                  SyndicatFilterChip(
                    selected = isAdvancedPerFloor,
                    onClick = { isAdvancedPerFloor = true },
                    label = strings.wizardAdvancedMode,
                  )
                }

                if (!isAdvancedPerFloor) {
                  WizardCounterRow(
                    label = strings.wizardApptsPerFloor,
                    value = uniformPerFloor,
                    onDec = { if (uniformPerFloor > 1) uniformPerFloor-- },
                    onInc = { if (uniformPerFloor < 20 && (uniformPerFloor + 1) * floorsCount <= 120) uniformPerFloor++ },
                  )
                } else {
                  generatedFloors.forEachIndexed { idx, fl ->
                    WizardCounterRow(
                      label = strings.floorWithPrefix(fl),
                      value = perFloorCounts[idx],
                      onDec = { if (perFloorCounts[idx] > 1) perFloorCounts[idx] = perFloorCounts[idx] - 1 },
                      onInc = { if (totalApartmentsCount < 120) perFloorCounts[idx] = perFloorCounts[idx] + 1 },
                    )
                  }
                }

                Surface(
                  shape = RoundedCornerShape(tokens.fieldRadiusDp.dp),
                  color = tokens.surfaceSoft,
                  border = BorderStroke(1.dp, tokens.divider),
                  tonalElevation = 0.dp,
                  shadowElevation = 0.dp,
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  Text(
                    text = "${strings.settingsApartmentsCount}: ${formatNumber(totalApartmentsCount.toLong(), initialConfig.digitsAr)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = tokens.ink,
                    modifier = Modifier.padding(14.dp),
                  )
                }
              }
            }
          }
          items(totalApartmentsCount) { idx ->
            val aptNum = idx + 1
            val fl = floorForNumberInDraft(aptNum)
            val apt = draftApartments[idx]
            FintechWhiteCard(modifier = Modifier.fillMaxWidth(), padding = 16.dp) {
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                  text = "${strings.colAppt} ${apt.displayNumber} · ${strings.floorWithPrefix(fl)}",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Medium,
                  color = tokens.ink,
                )
                FlowRow(
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                  verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                  UnitStatus.entries.forEach { st ->
                    SyndicatFilterChip(
                      selected = UnitStatus.fromCode(apt.status) == st,
                      onClick = { draftApartments[idx] = apt.copy(status = st.code) },
                      label = strings.unitStatusLabel(st),
                    )
                  }
                }
              }
            }
          }
        }

        3 -> {
          item {
            WizardStepHeader(strings.wizardStep3Title, strings.wizardStep3Sub)
          }
          item {
            SageSecondaryButton(
              text = strings.wizardBulkImportBtn,
              icon = Icons.AutoMirrored.Filled.PlaylistAdd,
              onClick = { showBulkTextDialog = true },
              modifier = Modifier.fillMaxWidth(),
            )
          }
          items(totalApartmentsCount) { idx ->
            val aptNum = idx + 1
            val fl = floorForNumberInDraft(aptNum)
            val apt = draftApartments[idx]
            FintechWhiteCard(modifier = Modifier.fillMaxWidth(), padding = 16.dp) {
              Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FlowRow(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Text(
                    text = "${strings.colAppt} ${apt.displayNumber} · ${strings.floorWithPrefix(fl)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = tokens.ink,
                    modifier = Modifier.align(Alignment.CenterVertically),
                  )
                  SageSecondaryButton(
                    text = strings.apptPickContactBtn,
                    icon = Icons.Default.Contacts,
                    compact = true,
                    onClick = {
                      contactTargetAptIndex = idx
                      val pickIntent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
                      contactPickerLauncher.launch(pickIntent)
                    },
                  )
                }
                FintechTextField(
                  value = apt.ownerNameAr,
                  onValueChange = { draftApartments[idx] = apt.copy(ownerNameAr = it) },
                  label = strings.apptOwnerArLabel,
                  placeholder = strings.apptOwnerArPlaceholder,
                  modifier = Modifier.fillMaxWidth(),
                )
                FintechTextField(
                  value = apt.ownerName,
                  onValueChange = { draftApartments[idx] = apt.copy(ownerName = it) },
                  label = strings.apptOwnerLatinLabel,
                  placeholder = strings.apptOwnerLatinPlaceholder,
                  modifier = Modifier.fillMaxWidth(),
                )
                FintechTextField(
                  value = apt.phone,
                  onValueChange = { draftApartments[idx] = apt.copy(phone = it) },
                  label = strings.apptPhoneLabel,
                  placeholder = "06...",
                  keyboardType = KeyboardType.Phone,
                  modifier = Modifier.fillMaxWidth(),
                )
              }
            }
          }
        }

        4 -> {
          item {
            WizardStepHeader(strings.wizardStep4Title, strings.wizardStep4Sub)
          }
          item {
            FintechWhiteCard(modifier = Modifier.fillMaxWidth(), padding = 24.dp) {
              Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                    value = dueDayText,
                    onValueChange = { dueDayText = it.filter { c -> c.isDigit() }.take(2) },
                    label = strings.settingsDueDayLabel,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f),
                  )
                }

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalAlignment = Alignment.Bottom,
                ) {
                  FintechTextField(
                    value = defaultFeeText,
                    onValueChange = { defaultFeeText = it.filter { c -> c.isDigit() }.take(6) },
                    label = strings.settingsDefaultFee(draftFeeConfig.currency),
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f),
                  )
                  SageSecondaryButton(
                    text = strings.settingsApplyDefaultToAll,
                    compact = true,
                    onClick = {
                      val v = defaultFeeText.toLongOrNull() ?: 50L
                      draftFeeConfig = draftFeeConfig.withAllFeesSetTo(v)
                    },
                  )
                }

                FlowRow(
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  SyndicatFilterChip(
                    selected = draftFeeConfig.activeMonths.size == 12,
                    onClick = { draftFeeConfig = draftFeeConfig.copy(activeMonths = MonthColumn.FULL_YEAR_NUMBERS) },
                    label = strings.settingsPresetFullYear,
                  )
                  SyndicatFilterChip(
                    selected = draftFeeConfig.activeMonths == MonthColumn.DEFAULT_C49_ACTIVE_NUMBERS,
                    onClick = { draftFeeConfig = draftFeeConfig.copy(activeMonths = MonthColumn.DEFAULT_C49_ACTIVE_NUMBERS) },
                    label = strings.settingsPresetJunDec,
                  )
                }

                HorizontalDivider(color = tokens.divider)

                MonthColumn.entries.forEach { month ->
                  val mLong = month.monthNumber.toLong()
                  val isActive = mLong in draftFeeConfig.activeMonths
                  val mFee = draftFeeConfig.feeForMonth(month)
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .heightIn(min = 52.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.weight(1f),
                    ) {
                      Checkbox(
                        checked = isActive,
                        onCheckedChange = { checked ->
                          val curSet = draftFeeConfig.activeMonths.toMutableSet()
                          if (checked) curSet.add(mLong) else if (curSet.size > 1) curSet.remove(mLong)
                          draftFeeConfig = draftFeeConfig.copy(activeMonths = curSet.sorted())
                        },
                      )
                      Text(
                        text = strings.monthFull(month),
                        style = MaterialTheme.typography.titleSmall,
                        color = tokens.ink,
                      )
                    }
                    FintechTextField(
                      value = mFee.toString(),
                      onValueChange = { txt ->
                        val parsed = txt.filter { it.isDigit() }.take(6).toLongOrNull() ?: 0L
                        draftFeeConfig = draftFeeConfig.withMonthFee(month, parsed)
                      },
                      label = "",
                      trailingIcon = {
                        Text(
                          text = draftFeeConfig.currency,
                          style = MaterialTheme.typography.labelSmall,
                          color = tokens.mute,
                          modifier = Modifier.padding(horizontal = 4.dp),
                        )
                      },
                      keyboardType = KeyboardType.Number,
                      modifier = Modifier.width(128.dp),
                    )
                  }
                }
              }
            }
          }
        }

        5 -> {
          item {
            WizardStepHeader(strings.wizardStep5Title, strings.wizardStep5Sub)
          }
          item {
            FintechWhiteCard(modifier = Modifier.fillMaxWidth(), padding = 24.dp) {
              Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FintechTextField(
                  value = openingBalanceText,
                  onValueChange = { openingBalanceText = it.filter { c -> c.isDigit() || c == '-' }.take(8) },
                  label = strings.settingsOpeningBalance(draftFeeConfig.currency),
                  keyboardType = KeyboardType.Number,
                  modifier = Modifier.fillMaxWidth(),
                )

                SyndicatFilterChip(
                  selected = !startFromZeroPayments,
                  onClick = { startFromZeroPayments = false },
                  label = strings.wizardEnterPastPayments,
                  leadingIcon = if (!startFromZeroPayments) {
                    { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                  } else null,
                )
                SyndicatFilterChip(
                  selected = startFromZeroPayments,
                  onClick = { startFromZeroPayments = true },
                  label = strings.wizardStartFromZero,
                  leadingIcon = if (startFromZeroPayments) {
                    { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                  } else null,
                )
              }
            }
          }

          if (!startFromZeroPayments) {
            val previewCfg = buildFinalConfig()
            items(totalApartmentsCount) { idx ->
              val apt = draftApartments[idx]
              val fl = floorForNumberInDraft(idx + 1)
              FintechWhiteCard(modifier = Modifier.fillMaxWidth(), padding = 16.dp) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = "${strings.colAppt} ${apt.displayNumber} (${strings.floorDisplay(fl)}) · ${apt.displayOwner(isAr).ifBlank { "—" }}",
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Medium,
                      color = tokens.ink,
                    )
                    Text(
                      text = "${strings.colPaid}: ${formatDh(apt.totalForConfig(previewCfg), previewCfg.currency, previewCfg.digitsAr)}",
                      style = MaterialTheme.typography.bodySmall,
                      fontFamily = numericFont ?: MaterialTheme.typography.bodySmall.fontFamily,
                      color = tokens.positiveDeep,
                    )
                  }
                  SageSecondaryButton(
                    text = strings.apptPayFullBtn,
                    compact = true,
                    onClick = {
                      var settled = apt
                      for (col in previewCfg.cutoffMonthColumns) {
                        settled = settled.withMonthAmount(col, previewCfg.feeForMonth(col))
                      }
                      draftApartments[idx] = settled
                    },
                  )
                }
              }
            }
          }
        }

        6 -> {
          val finalCfg = buildFinalConfig()
          val finalList = (0 until totalApartmentsCount).map { idx ->
            val base = draftApartments[idx].copy(
              number = (idx + 1).toLong(),
              floor = floorForNumberInDraft(idx + 1),
            )
            if (startFromZeroPayments) {
              base.copy(
                jan = 0, feb = 0, mar = 0, apr = 0, may = 0, juin = 0,
                juil = 0, aout = 0, sept = 0, oct = 0, nov = 0, dec = 0,
              )
            } else {
              base
            }
          }
          val totalCollected = finalList.sumOf { it.totalForConfig(finalCfg) }

          item {
            WizardStepHeader(strings.wizardStep6Title, strings.wizardStep6Sub)
          }
          item {
            FintechPalePositiveCard(modifier = Modifier.fillMaxWidth(), padding = 24.dp) {
              Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                  text = finalCfg.residenceName,
                  style = MaterialTheme.typography.headlineMedium,
                  fontWeight = FontWeight.Medium,
                  color = tokens.inkDeep,
                )
                Text(
                  text = "${strings.settingsFloorsCount}: ${formatNumber(finalCfg.floorsCount, finalCfg.digitsAr)}  ·  ${strings.settingsApartmentsCount}: ${formatNumber(finalCfg.apartmentsCount, finalCfg.digitsAr)}",
                  color = tokens.body,
                )
                Text(
                  text = "${strings.settingsDefaultFee(finalCfg.currency)}: ${formatDh(finalCfg.defaultMonthlyFee, finalCfg.currency, finalCfg.digitsAr)}",
                  color = tokens.body,
                )
                Text(
                  text = "${strings.caisseOpeningBalance}: ${formatDh(finalCfg.openingBalanceDh, finalCfg.currency, finalCfg.digitsAr)}",
                  color = tokens.body,
                )
                Text(
                  text = "${strings.dashCollectedLabel}: ${formatDh(totalCollected, finalCfg.currency, finalCfg.digitsAr)}",
                  style = MaterialTheme.typography.titleMedium,
                  fontFamily = numericFont ?: MaterialTheme.typography.titleMedium.fontFamily,
                  fontWeight = FontWeight.Medium,
                  color = tokens.positiveDeep,
                )
              }
            }
          }
        }
      }
    }

    // Bottom Navigation Buttons (§6: Single primary black button « التالي / ابدأ » + secondary white button « رجوع »)
    Column(modifier = Modifier.fillMaxWidth()) {
      HorizontalDivider(thickness = 1.dp, color = tokens.divider)
      Surface(
        color = tokens.navBg,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          if (currentStep > 1) {
            InkTertiaryButton(
              text = strings.wizardBackBtn,
              onClick = { currentStep-- },
              modifier = Modifier.weight(1f),
            )
          }
          MetaCobaltBuyButton(
            text = if (currentStep < 6) strings.wizardNextBtn else strings.wizardStartBtn,
            onClick = {
              if (currentStep < 6) {
                currentStep++
              } else {
                val finalCfg = buildFinalConfig()
                val finalAppts = (0 until totalApartmentsCount).map { idx ->
                  val base = draftApartments[idx].copy(
                    id = "appt_%02d".format(idx + 1),
                    number = (idx + 1).toLong(),
                    floor = floorForNumberInDraft(idx + 1),
                  )
                  if (startFromZeroPayments) {
                    base.copy(
                      jan = 0, feb = 0, mar = 0, apr = 0, may = 0, juin = 0,
                      juil = 0, aout = 0, sept = 0, oct = 0, nov = 0, dec = 0,
                    )
                  } else {
                    base
                  }
                }
                onComplete(finalCfg, finalAppts, startFromZeroPayments)
              }
            },
            modifier = Modifier.weight(1.5f).testTag("wizard_next_or_start_btn"),
          )
        }
      }
    }
  }

  if (showBulkTextDialog) {
    SyndicatDialog(
      onDismissRequest = { showBulkTextDialog = false },
      title = {
        Text(
          text = strings.wizardBulkImportBtn,
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
            text = if (isAr) "سطر لكل شقة: رقم;الاسم بالعربية;الاسم باللاتينية;الهاتف" else "Une ligne par appt : num;nom_ar;nom_latin;tel",
            style = MaterialTheme.typography.bodySmall,
            color = tokens.body,
          )
          FintechTextField(
            value = bulkRawInput,
            onValueChange = { bulkRawInput = it },
            label = "CSV / Liste",
            placeholder = "1;عبد الفتاح;Abdelfetah;0612345678\n2;حميد;Hamid;0698765432",
            singleLine = false,
            minLines = 4,
            maxLines = 8,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      },
      confirmButton = {
        MetaCobaltBuyButton(
          text = strings.apptSave,
          compact = true,
          onClick = {
            val parsed = BackupManager.parseOwnersBulkLines(bulkRawInput)
            parsed.forEach { row ->
              val idx = row.apartmentNumber - 1
              if (idx in 0 until draftApartments.size) {
                val cur = draftApartments[idx]
                draftApartments[idx] = cur.copy(
                  ownerNameAr = row.ownerNameAr.ifBlank { cur.ownerNameAr },
                  ownerName = row.ownerNameLatin.ifBlank { cur.ownerName },
                  phone = row.phone.ifBlank { cur.phone },
                  status = "occupied",
                )
              }
            }
            showBulkTextDialog = false
          },
        )
      },
      dismissButton = {
        SageSecondaryButton(
          text = strings.apptCancel,
          compact = true,
          onClick = { showBulkTextDialog = false },
        )
      },
    )
  }
}

@Composable
private fun WizardStepHeader(title: String, subtitle: String) {
  val tokens = LocalSyndicatTokens.current
  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    Text(
      text = title,
      style = MaterialTheme.typography.headlineLarge,
      fontWeight = FontWeight.Medium,
      color = tokens.inkDeep,
    )
    Text(
      text = subtitle,
      style = MaterialTheme.typography.headlineMedium,
      fontWeight = FontWeight.Light,
      color = tokens.body,
    )
  }
}

@Composable
private fun WizardCounterRow(
  label: String,
  value: Int,
  onDec: () -> Unit,
  onInc: () -> Unit,
) {
  val tokens = LocalSyndicatTokens.current
  val numericFont = tokens.numericFontFamily ?: tokens.monoFontFamily
  Surface(
    shape = RoundedCornerShape(tokens.fieldRadiusDp.dp),
    color = tokens.surfaceSoft,
    border = BorderStroke(1.dp, tokens.divider),
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(label, style = MaterialTheme.typography.titleSmall, color = tokens.ink, modifier = Modifier.weight(1f))
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        IconButton(onClick = onDec, modifier = Modifier.size(48.dp)) {
          Icon(Icons.Default.Remove, contentDescription = "-", tint = tokens.ink)
        }
        Text(
          text = formatNumber(value.toLong()),
          style = MaterialTheme.typography.headlineMedium,
          fontFamily = numericFont ?: MaterialTheme.typography.headlineMedium.fontFamily,
          fontWeight = FontWeight.Medium,
          color = tokens.ink,
        )
        IconButton(onClick = onInc, modifier = Modifier.size(48.dp)) {
          Icon(Icons.Default.Add, contentDescription = "+", tint = tokens.ink)
        }
      }
    }
  }
}
