package com.example.data.local

import android.content.Context
import com.example.data.model.ApartmentRecord
import com.example.data.model.DEFAULT_RELANCE_HEADER_AR
import com.example.data.model.DEFAULT_WHATSAPP_TEMPLATE_AR
import com.example.data.model.ExpenseRecord
import com.example.data.model.MonthColumn
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentRecord
import com.example.data.model.ProjectRecord
import com.example.data.model.SyndicatConfig
import com.example.data.model.canonicalApartmentDocId
import com.example.data.model.isCoherentApartmentId
import com.example.data.model.normalizeSyndicatCategory
import com.example.data.model.sortProjectsByPriority
import com.example.data.repository.LOCAL_DEVICE_USER_ID
import com.example.data.repository.SyndicatDataSource
import com.example.data.seed.InitialSyndicatData
import com.example.ui.theme.sanitizeThemeStyle
import com.example.ui.viewmodel.SyndicatDashboardData
import com.example.util.BackupManager
import com.example.util.ParsedOwnerRow
import com.example.util.ParsedSyndicatBackup
import com.google.firebase.Timestamp
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Account-free on-device persistence layer stored in the phone's internal storage
 * (`filesDir/local_store/syndicat_local_state.json`).
 *
 * - Works 100% offline and without any Google or Firebase account.
 * - Never touches FirebaseAuth or FirebaseFirestore.
 * - Can export a [ParsedSyndicatBackup] snapshot at any time when the user chooses
 *   optional Google Sign-In cloud synchronization, and mirrors cloud updates locally
 *   so signing out preserves all data on the phone.
 */
class LocalSyndicatRepository(
  context: Context,
  private val storageFileOverride: File? = null,
) : SyndicatDataSource {

  private val appContext = context.applicationContext
  private val mutex = Mutex()

  private val storeFile: File by lazy {
    storageFileOverride ?: File(File(appContext.filesDir, "local_store").apply { mkdirs() }, "syndicat_local_state.json")
  }

  private val configFlow = MutableStateFlow<SyndicatConfig?>(null)
  private val apartmentsFlow = MutableStateFlow<List<ApartmentRecord>>(emptyList())
  private val expensesFlow = MutableStateFlow<List<ExpenseRecord>>(emptyList())
  private val projectsFlow = MutableStateFlow<List<ProjectRecord>>(emptyList())
  private val paymentsFlow = MutableStateFlow<List<PaymentRecord>>(emptyList())

  @Volatile
  private var initializedFromDisk = false

  init {
    loadSynchronouslyIfPresent()
  }

  private fun loadSynchronouslyIfPresent() {
    try {
      val candidate = when {
        storeFile.exists() && storeFile.length() > 0L -> storeFile
        storageFileOverride == null -> {
          val fallbackSnapshot = File(File(appContext.filesDir, "backups"), "syndicat_auto_weekly.json")
          if (fallbackSnapshot.exists() && fallbackSnapshot.length() > 0L) fallbackSnapshot else null
        }
        else -> null
      }
      if (candidate != null) {
        val raw = candidate.readText(Charsets.UTF_8)
        if (raw.isNotBlank()) {
          val parsed = BackupManager.parseBackupJson(raw, LOCAL_DEVICE_USER_ID)
          configFlow.value = parsed.config.copy(userId = LOCAL_DEVICE_USER_ID, seeded = true)
          apartmentsFlow.value = parsed.apartments.sortedBy { it.number }
          expensesFlow.value = parsed.expenses
          projectsFlow.value = parsed.projects
          paymentsFlow.value = parsed.payments
          initializedFromDisk = true
        }
      }
    } catch (_: Exception) {
    }
  }

  private suspend fun persistCurrentStateLocked() {
    val cfg = configFlow.value ?: return
    val dash = SyndicatDashboardData(
      config = cfg,
      apartments = apartmentsFlow.value,
      expenses = expensesFlow.value,
      projects = projectsFlow.value,
      payments = paymentsFlow.value,
      baseApartments = apartmentsFlow.value,
      baseYear = cfg.year,
    )
    withContext(Dispatchers.IO) {
      val parent = storeFile.parentFile ?: appContext.filesDir
      if (!parent.exists()) parent.mkdirs()
      val tmpFile = File(parent, "${storeFile.name}.tmp")
      val json = BackupManager.serializeBackupJson(dash)
      tmpFile.writeText(json, Charsets.UTF_8)
      if (!tmpFile.renameTo(storeFile)) {
        storeFile.writeText(json, Charsets.UTF_8)
        tmpFile.delete()
      }
    }
  }

  fun hasLocalUserData(): Boolean {
    return initializedFromDisk && configFlow.value != null && apartmentsFlow.value.isNotEmpty()
  }

  fun exportCurrentBackup(targetUserId: String = LOCAL_DEVICE_USER_ID): ParsedSyndicatBackup? {
    val cfg = configFlow.value ?: return null
    return ParsedSyndicatBackup(
      config = cfg.copy(userId = targetUserId),
      apartments = apartmentsFlow.value.map { it.copy(userId = targetUserId) },
      expenses = expensesFlow.value.map { it.copy(userId = targetUserId) },
      projects = projectsFlow.value.map { it.copy(userId = targetUserId) },
      payments = paymentsFlow.value.map { it.copy(userId = targetUserId) },
    )
  }

  suspend fun mirrorFromCloud(data: SyndicatDashboardData) {
    mutex.withLock {
      val sourceApartments = data.baseApartments.ifEmpty { data.apartments }
      configFlow.value = data.config.copy(userId = LOCAL_DEVICE_USER_ID, year = data.baseYear, seeded = true)
      apartmentsFlow.value = sourceApartments.map {
        val safeNum = it.number.coerceIn(1L, 200L)
        it.copy(
          id = canonicalApartmentDocId(safeNum),
          userId = LOCAL_DEVICE_USER_ID,
          number = safeNum,
        )
      }.sortedBy { it.number }
      expensesFlow.value = data.expenses.map { it.copy(userId = LOCAL_DEVICE_USER_ID) }
      projectsFlow.value = sortProjectsByPriority(data.projects.map { it.copy(userId = LOCAL_DEVICE_USER_ID) })
      paymentsFlow.value = data.payments.map { it.copy(userId = LOCAL_DEVICE_USER_ID) }
      initializedFromDisk = true
      persistCurrentStateLocked()
    }
  }

  override fun observeConfig(userId: String): Flow<SyndicatConfig?> = configFlow.asStateFlow()
  override fun observeApartments(userId: String): Flow<List<ApartmentRecord>> = apartmentsFlow.asStateFlow()
  override fun observeExpenses(userId: String): Flow<List<ExpenseRecord>> = expensesFlow.asStateFlow()
  override fun observeProjects(userId: String): Flow<List<ProjectRecord>> = projectsFlow.asStateFlow()
  override fun observePayments(userId: String): Flow<List<PaymentRecord>> = paymentsFlow.asStateFlow()

  override suspend fun ensureSeeded(forceReset: Boolean, preserveLanguage: String?) {
    mutex.withLock {
      if (!forceReset && initializedFromDisk && configFlow.value?.seeded == true) {
        return
      }
      val existingLang = preserveLanguage ?: configFlow.value?.language ?: "ar"
      val existingStyle = sanitizeThemeStyle(configFlow.value?.themeStyle, "coinbase")
      val seededCfg = SyndicatConfig(
        userId = LOCAL_DEVICE_USER_ID,
        language = if (existingLang == "fr") "fr" else "ar",
        digitsAr = configFlow.value?.digitsAr ?: false,
        themeStyle = existingStyle,
        themeMode = configFlow.value?.themeMode ?: "system",
        fontScale = configFlow.value?.fontScale ?: "normal",
        syndicatName = InitialSyndicatData.DEFAULT_SYNDICAT_NAME,
        residenceName = InitialSyndicatData.DEFAULT_RESIDENCE_NAME,
        address = InitialSyndicatData.DEFAULT_ADDRESS,
        city = "",
        treasurerName = "",
        buildingTitle = InitialSyndicatData.DEFAULT_BUILDING_TITLE,
        currency = if (existingLang == "fr") "DH" else InitialSyndicatData.DEFAULT_CURRENCY,
        year = InitialSyndicatData.DEFAULT_YEAR,
        openingBalanceDh = 0L,
        dueDayOfMonth = 5L,
        minReminderAmountDh = 20L,
        whatsappTemplate = DEFAULT_WHATSAPP_TEMPLATE_AR,
        relanceHeaderNote = DEFAULT_RELANCE_HEADER_AR,
        includeRdc = true,
        floorsCount = 5L,
        apartmentsCount = 30L,
        floorAptCounts = listOf(6L, 6L, 6L, 6L, 6L),
        defaultMonthlyFee = 50L,
        cutoffMonth = 10L,
        activeMonths = InitialSyndicatData.DEFAULT_ACTIVE_MONTHS,
        feeJan = 50L,
        feeFeb = 50L,
        feeMar = 50L,
        feeApr = 50L,
        feeMay = 50L,
        feeJuin = 100L,
        feeJuil = 100L,
        feeAout = 50L,
        feeSept = 50L,
        feeOct = 50L,
        feeNov = 50L,
        feeDec = 50L,
        footerNote = InitialSyndicatData.DEFAULT_FOOTER_NOTE,
        onboardingCompleted = true,
        seeded = true,
      )

      val seededAppts = InitialSyndicatData.apartments.map { seed ->
        ApartmentRecord(
          id = seed.docId,
          userId = LOCAL_DEVICE_USER_ID,
          number = seed.number,
          floor = seed.floor,
          status = seed.status,
          ownerName = seed.ownerName,
          ownerNameAr = seed.ownerNameAr,
          jan = seed.jan,
          feb = seed.feb,
          mar = seed.mar,
          apr = seed.apr,
          may = seed.may,
          juin = seed.juin,
          juil = seed.juil,
          aout = seed.aout,
          sept = seed.sept,
          oct = seed.oct,
          nov = seed.nov,
          dec = seed.dec,
        )
      }.sortedBy { it.number }

      val seededExps = listOf(
        ExpenseRecord(
          id = InitialSyndicatData.INITIAL_EXPENSE_ID,
          userId = LOCAL_DEVICE_USER_ID,
          title = InitialSyndicatData.INITIAL_EXPENSE_TITLE,
          category = "Historique",
          amountDh = InitialSyndicatData.INITIAL_EXPENSE_AMOUNT_DH,
          paidAmountDh = InitialSyndicatData.INITIAL_EXPENSE_AMOUNT_DH,
          supplier = "",
          dateLabel = InitialSyndicatData.INITIAL_EXPENSE_DATE,
          notes = InitialSyndicatData.INITIAL_EXPENSE_NOTES,
        )
      )

      val seededProjs = sortProjectsByPriority(
        InitialSyndicatData.projects.map { seed ->
          ProjectRecord(
            id = seed.docId,
            userId = LOCAL_DEVICE_USER_ID,
            titleAr = seed.titleAr,
            titleFr = seed.titleFr,
            descriptionAr = seed.descriptionAr,
            descriptionFr = seed.descriptionFr,
            category = seed.category,
            priority = seed.priority,
            status = seed.status,
            targetBudgetDh = seed.targetBudgetDh,
            minCashToStartDh = seed.minCashToStartDh,
            allocatedAmountDh = seed.allocatedAmountDh,
            progressPercent = seed.progressPercent,
            contractor = seed.contractor,
            targetDateLabel = seed.targetDateLabel,
          )
        }
      )

      configFlow.value = seededCfg
      apartmentsFlow.value = seededAppts
      expensesFlow.value = seededExps
      projectsFlow.value = seededProjs
      if (forceReset) {
        paymentsFlow.value = emptyList()
      }
      initializedFromDisk = true
      persistCurrentStateLocked()
    }
  }

  override suspend fun saveFullConfiguration(
    config: SyndicatConfig,
    resetBlankBuilding: Boolean,
    explicitApartments: List<ApartmentRecord>?,
  ) {
    mutex.withLock {
      val cleanCfg = sanitizeLocalConfig(config)
      val totalAppts = cleanCfg.apartmentsCount.toInt().coerceIn(1, 120)
      val existingByNumber = apartmentsFlow.value.associateBy { it.number.toInt() }
      val explicitByNumber = explicitApartments?.associateBy { it.number.toInt() }.orEmpty()

      val nextAppts = (1..totalAppts).map { num ->
        val docId = "appt_%02d".format(num)
        val computedFloor = cleanCfg.floorForApartmentNumber(num)
        val explicit = explicitByNumber[num]
        val existing = existingByNumber[num]
        when {
          explicit != null -> explicit.copy(
            id = docId,
            userId = LOCAL_DEVICE_USER_ID,
            number = num.toLong(),
            floor = computedFloor,
            jan = if (resetBlankBuilding) 0L else explicit.jan.coerceIn(0L, 100_000L),
            feb = if (resetBlankBuilding) 0L else explicit.feb.coerceIn(0L, 100_000L),
            mar = if (resetBlankBuilding) 0L else explicit.mar.coerceIn(0L, 100_000L),
            apr = if (resetBlankBuilding) 0L else explicit.apr.coerceIn(0L, 100_000L),
            may = if (resetBlankBuilding) 0L else explicit.may.coerceIn(0L, 100_000L),
            juin = if (resetBlankBuilding) 0L else explicit.juin.coerceIn(0L, 100_000L),
            juil = if (resetBlankBuilding) 0L else explicit.juil.coerceIn(0L, 100_000L),
            aout = if (resetBlankBuilding) 0L else explicit.aout.coerceIn(0L, 100_000L),
            sept = if (resetBlankBuilding) 0L else explicit.sept.coerceIn(0L, 100_000L),
            oct = if (resetBlankBuilding) 0L else explicit.oct.coerceIn(0L, 100_000L),
            nov = if (resetBlankBuilding) 0L else explicit.nov.coerceIn(0L, 100_000L),
            dec = if (resetBlankBuilding) 0L else explicit.dec.coerceIn(0L, 100_000L),
          )
          resetBlankBuilding -> ApartmentRecord(
            id = docId,
            userId = LOCAL_DEVICE_USER_ID,
            number = num.toLong(),
            floor = computedFloor,
            status = "occupied",
          )
          existing != null -> existing.copy(
            id = docId,
            userId = LOCAL_DEVICE_USER_ID,
            number = num.toLong(),
            floor = computedFloor,
          )
          else -> ApartmentRecord(
            id = docId,
            userId = LOCAL_DEVICE_USER_ID,
            number = num.toLong(),
            floor = computedFloor,
            status = "occupied",
          )
        }
      }

      configFlow.value = cleanCfg
      apartmentsFlow.value = nextAppts
      if (resetBlankBuilding) {
        expensesFlow.value = emptyList()
        paymentsFlow.value = emptyList()
      }
      initializedFromDisk = true
      persistCurrentStateLocked()
    }
  }

  override suspend fun restoreFromBackup(backup: ParsedSyndicatBackup) {
    mutex.withLock {
      configFlow.value = sanitizeLocalConfig(backup.config)
      apartmentsFlow.value = backup.apartments.map {
        val safeNum = it.number.coerceIn(1L, 200L)
        it.copy(
          id = canonicalApartmentDocId(safeNum),
          userId = LOCAL_DEVICE_USER_ID,
          number = safeNum,
        )
      }.sortedBy { it.number }
      expensesFlow.value = backup.expenses.map {
        val safeAmt = it.amountDh.coerceIn(0L, 10_000_000L)
        val safePaid = if (it.paidAmountDh < 0L) -1L else it.paidAmountDh.coerceIn(0L, safeAmt)
        it.copy(
          id = it.id.ifBlank { "exp_${UUID.randomUUID().toString().take(8)}" },
          userId = LOCAL_DEVICE_USER_ID,
          category = normalizeSyndicatCategory(it.category),
          amountDh = safeAmt,
          paidAmountDh = safePaid,
        )
      }
      projectsFlow.value = backup.projects.map {
        sanitizeLocalProject(
          it.copy(
            id = it.id.ifBlank { "proj_${UUID.randomUUID().toString().take(8)}" },
            userId = LOCAL_DEVICE_USER_ID,
          )
        )
      }
      paymentsFlow.value = backup.payments.map {
        val safeNum = it.apartmentNumber.coerceIn(1L, 200L)
        it.copy(
          id = it.id.ifBlank { "pay_${UUID.randomUUID().toString().take(8)}" },
          userId = LOCAL_DEVICE_USER_ID,
          apartmentId = canonicalApartmentDocId(safeNum),
          apartmentNumber = safeNum,
          year = it.year.coerceIn(2020L, 2100L),
          month = it.month.coerceIn(0L, 12L),
          monthsCovered = it.monthsCovered.filter { m -> m in 1L..12L }.distinct().sorted(),
          amountDh = it.amountDh.coerceIn(1L, 1_000_000L),
          paymentMethod = PaymentMethod.fromCode(it.paymentMethod).code,
        )
      }
      initializedFromDisk = true
      persistCurrentStateLocked()
    }
  }

  override suspend fun importOwnerRows(rows: List<ParsedOwnerRow>) {
    if (rows.isEmpty()) return
    mutex.withLock {
      val byNum = rows.associateBy { it.apartmentNumber.toLong() }
      apartmentsFlow.value = apartmentsFlow.value.map { apt ->
        val row = byNum[apt.number]
        if (row == null) {
          apt
        } else {
          apt.copy(
            ownerNameAr = row.ownerNameAr.trim().take(80).ifBlank { apt.ownerNameAr },
            ownerName = row.ownerNameLatin.trim().take(80).ifBlank { apt.ownerName },
            phone = row.phone.trim().take(30).ifBlank { apt.phone },
          )
        }
      }
      persistCurrentStateLocked()
    }
  }

  override suspend fun updateDisplayPrefs(
    digitsAr: Boolean?,
    themeStyle: String?,
    themeMode: String?,
    fontScale: String?,
  ) {
    mutex.withLock {
      val current = configFlow.value ?: SyndicatConfig(userId = LOCAL_DEVICE_USER_ID, seeded = true)
      configFlow.value = current.copy(
        digitsAr = digitsAr ?: current.digitsAr,
        themeStyle = if (themeStyle != null) sanitizeThemeStyle(themeStyle, current.themeStyle) else current.themeStyle,
        themeMode = if (themeMode in listOf("system", "light", "dark")) themeMode!! else current.themeMode,
        fontScale = if (fontScale in listOf("small", "normal", "large")) fontScale!! else current.fontScale,
      )
      persistCurrentStateLocked()
    }
  }

  override suspend fun updateLanguage(languageCode: String) {
    mutex.withLock {
      val current = configFlow.value ?: SyndicatConfig(userId = LOCAL_DEVICE_USER_ID, seeded = true)
      configFlow.value = current.copy(language = if (languageCode == "fr") "fr" else "ar")
      persistCurrentStateLocked()
    }
  }

  override suspend fun updateWhatsAppTemplate(template: String) {
    mutex.withLock {
      val current = configFlow.value ?: return
      configFlow.value = current.copy(whatsappTemplate = template.trim().take(1000))
      persistCurrentStateLocked()
    }
  }

  override suspend fun updateApartment(apartment: ApartmentRecord) {
    require(apartment.id.isNotBlank()) { "Identifiant d'appartement invalide." }
    require(isCoherentApartmentId(apartment.id, apartment.number)) {
      "L'identifiant du document (${apartment.id}) ne correspond pas au numéro d'appartement (${apartment.number})."
    }
    mutex.withLock {
      val clean = apartment.copy(userId = LOCAL_DEVICE_USER_ID)
      val current = apartmentsFlow.value.toMutableList()
      val idx = current.indexOfFirst { it.id == clean.id || it.number == clean.number }
      if (idx >= 0) {
        current[idx] = clean
      } else {
        current.add(clean)
      }
      apartmentsFlow.value = current.sortedBy { it.number }
      persistCurrentStateLocked()
    }
  }

  override suspend fun markReminderSent(apartmentId: String) {
    if (apartmentId.isBlank()) return
    mutex.withLock {
      val now = System.currentTimeMillis()
      apartmentsFlow.value = apartmentsFlow.value.map {
        if (it.id == apartmentId) it.copy(lastReminderAt = now) else it
      }
      persistCurrentStateLocked()
    }
  }

  override suspend fun incrementReceiptCount(apartment: ApartmentRecord): Long {
    if (apartment.id.isBlank()) return 1L
    return mutex.withLock {
      val nextCount = (apartment.receiptCount + 1L).coerceIn(1L, 100_000L)
      apartmentsFlow.value = apartmentsFlow.value.map {
        if (it.id == apartment.id) it.copy(receiptCount = nextCount) else it
      }
      persistCurrentStateLocked()
      nextCount
    }
  }

  override suspend fun addPayment(payment: PaymentRecord, explicitId: String?): String {
    require(payment.apartmentNumber in 1L..200L) { "Numéro d'appartement invalide (${payment.apartmentNumber})." }
    val expectedAptId = canonicalApartmentDocId(payment.apartmentNumber)
    if (payment.apartmentId.isNotBlank()) {
      require(payment.apartmentId == expectedAptId) {
        "L'identifiant d'appartement (${payment.apartmentId}) ne correspond pas au numéro (${payment.apartmentNumber})."
      }
    }
    require(payment.amountDh in 1L..1_000_000L) { "Montant de paiement invalide (${payment.amountDh})." }
    require(payment.year in 2020L..2100L) { "Année de paiement invalide (${payment.year})." }

    val docId = explicitId?.takeIf { it.isNotBlank() }
      ?: "pay_${UUID.randomUUID().toString().replace("-", "").take(16)}"
    val nowTs = Timestamp.now()

    mutex.withLock {
      val clean = PaymentRecord(
        id = docId,
        userId = LOCAL_DEVICE_USER_ID,
        apartmentId = expectedAptId,
        apartmentNumber = payment.apartmentNumber,
        year = payment.year,
        month = payment.month.coerceIn(0L, 12L),
        monthsCovered = payment.monthsCovered.filter { it in 1L..12L }.distinct().sorted().take(12),
        amountDh = payment.amountDh,
        paymentDate = payment.paymentDate.trim().ifEmpty { "01/01/${payment.year}" }.take(30),
        paymentMethod = PaymentMethod.fromCode(payment.paymentMethod).code,
        receiptNumber = payment.receiptNumber.coerceIn(0L, 100_000L),
        notes = payment.notes.trim().take(200),
        createdAt = payment.createdAt ?: nowTs,
        updatedAt = nowTs,
      )
      paymentsFlow.value = listOf(clean) + paymentsFlow.value.filterNot { it.id == docId }
      persistCurrentStateLocked()
    }
    return docId
  }

  override suspend fun deletePayment(paymentId: String) {
    if (paymentId.isBlank()) return
    mutex.withLock {
      paymentsFlow.value = paymentsFlow.value.filterNot { it.id == paymentId }
      persistCurrentStateLocked()
    }
  }

  override suspend fun addExpense(
    title: String,
    category: String,
    amountDh: Long,
    paidAmountDh: Long,
    supplier: String,
    dateLabel: String,
    notes: String,
    explicitId: String?,
  ): String {
    val cleanTitle = title.trim()
    require(cleanTitle.isNotEmpty() && cleanTitle.length <= 150) { "Intitulé invalide." }
    require(amountDh in 0L..10_000_000L) { "Montant invalide ($amountDh)." }
    require(paidAmountDh <= amountDh) {
      "Le montant payé ($paidAmountDh) ne peut pas dépasser le montant total ($amountDh)."
    }
    val validCategory = normalizeSyndicatCategory(category, "Divers")
    val clampedPaid = if (paidAmountDh < 0L) amountDh else paidAmountDh.coerceIn(0L, amountDh)
    val docId = explicitId?.takeIf { it.isNotBlank() } ?: "exp_${UUID.randomUUID().toString().replace("-", "").take(16)}"

    mutex.withLock {
      val record = ExpenseRecord(
        id = docId,
        userId = LOCAL_DEVICE_USER_ID,
        title = cleanTitle,
        category = validCategory,
        amountDh = amountDh,
        paidAmountDh = clampedPaid,
        supplier = supplier.trim().take(100),
        dateLabel = dateLabel.trim().ifEmpty { "01/10/2026" }.take(30),
        notes = notes.trim().take(300),
      )
      expensesFlow.value = listOf(record) + expensesFlow.value.filterNot { it.id == docId }
      persistCurrentStateLocked()
    }
    return docId
  }

  override suspend fun updateExpense(expense: ExpenseRecord) {
    require(expense.id.isNotBlank()) { "Identifiant de dépense manquant." }
    require(expense.amountDh in 0L..10_000_000L) { "Montant invalide (${expense.amountDh})." }
    require(expense.paidAmountDh <= expense.amountDh) {
      "Le montant payé (${expense.paidAmountDh}) ne peut pas dépasser le montant total (${expense.amountDh})."
    }
    mutex.withLock {
      val clampedAmt = expense.amountDh
      val clampedPaid = if (expense.paidAmountDh < 0L) clampedAmt else expense.paidAmountDh.coerceIn(0L, clampedAmt)
      val updated = expense.copy(
        userId = LOCAL_DEVICE_USER_ID,
        title = expense.title.trim().ifBlank { "مصروف" }.take(150),
        category = normalizeSyndicatCategory(expense.category, "Divers"),
        amountDh = clampedAmt,
        paidAmountDh = clampedPaid,
        supplier = expense.supplier.trim().take(100),
        dateLabel = expense.dateLabel.trim().ifBlank { "01/10/2026" }.take(30),
        notes = expense.notes.trim().take(300),
      )
      expensesFlow.value = expensesFlow.value.map { if (it.id == updated.id) updated else it }
      persistCurrentStateLocked()
    }
  }

  override suspend fun deleteExpense(expenseId: String) {
    if (expenseId.isBlank()) return
    mutex.withLock {
      expensesFlow.value = expensesFlow.value.filterNot { it.id == expenseId }
      persistCurrentStateLocked()
    }
  }

  override suspend fun seedStarterProjects() {
    mutex.withLock {
      if (projectsFlow.value.isNotEmpty()) return
      projectsFlow.value = InitialSyndicatData.projects.map { seed ->
        ProjectRecord(
          id = seed.docId,
          userId = LOCAL_DEVICE_USER_ID,
          titleAr = seed.titleAr,
          titleFr = seed.titleFr,
          descriptionAr = seed.descriptionAr,
          descriptionFr = seed.descriptionFr,
          category = seed.category,
          priority = seed.priority,
          status = seed.status,
          targetBudgetDh = seed.targetBudgetDh,
          minCashToStartDh = seed.minCashToStartDh,
          allocatedAmountDh = seed.allocatedAmountDh,
          progressPercent = seed.progressPercent,
          contractor = seed.contractor,
          targetDateLabel = seed.targetDateLabel,
        )
      }
      persistCurrentStateLocked()
    }
  }

  override suspend fun addProject(project: ProjectRecord, explicitId: String?): String {
    val cleanAr = project.titleAr.trim().take(150)
    val cleanFr = project.titleFr.trim().take(150)
    require(cleanAr.isNotEmpty() || cleanFr.isNotEmpty()) { "Titre du projet requis." }
    val docId = explicitId?.takeIf { it.isNotBlank() } ?: "proj_${UUID.randomUUID().toString().replace("-", "").take(16)}"

    mutex.withLock {
      val clean = sanitizeLocalProject(project.copy(id = docId, titleAr = cleanAr, titleFr = cleanFr))
      projectsFlow.value = sortProjectsByPriority(projectsFlow.value.filterNot { it.id == docId } + clean)
      persistCurrentStateLocked()
    }
    return docId
  }

  override suspend fun updateProject(project: ProjectRecord) {
    require(project.id.isNotBlank()) { "Identifiant du projet manquant." }
    mutex.withLock {
      val clean = sanitizeLocalProject(project)
      projectsFlow.value = sortProjectsByPriority(projectsFlow.value.map { if (it.id == clean.id) clean else it })
      persistCurrentStateLocked()
    }
  }

  override suspend fun deleteProject(projectId: String) {
    if (projectId.isBlank()) return
    mutex.withLock {
      projectsFlow.value = projectsFlow.value.filterNot { it.id == projectId }
      persistCurrentStateLocked()
    }
  }

  private fun sanitizeLocalProject(project: ProjectRecord): ProjectRecord {
    val validCategory = normalizeSyndicatCategory(project.category, "Entretien")
    val validStatus = if (project.status in listOf("in_progress", "planned", "completed")) project.status else "planned"
    val targetBudget = project.targetBudgetDh.coerceIn(0L, 10_000_000L)
    val minStart = if (project.minCashToStartDh <= 0L && targetBudget > 0L) targetBudget else project.minCashToStartDh.coerceIn(0L, 10_000_000L)
    val progress = when (validStatus) {
      "completed" -> 100L
      "planned" -> 0L
      else -> project.progressPercent.coerceIn(0L, 100L)
    }
    return project.copy(
      userId = LOCAL_DEVICE_USER_ID,
      titleAr = project.titleAr.trim().take(150),
      titleFr = project.titleFr.trim().take(150),
      descriptionAr = project.descriptionAr.trim().take(400),
      descriptionFr = project.descriptionFr.trim().take(400),
      category = validCategory,
      priority = project.priority.coerceIn(1L, 4L),
      status = validStatus,
      targetBudgetDh = targetBudget,
      minCashToStartDh = minStart,
      allocatedAmountDh = project.allocatedAmountDh.coerceIn(0L, 10_000_000L),
      progressPercent = progress,
      contractor = project.contractor.trim().take(100),
      targetDateLabel = project.targetDateLabel.trim().take(30),
    )
  }

  private fun sanitizeLocalConfig(config: SyndicatConfig): SyndicatConfig {
    return config.copy(
      userId = LOCAL_DEVICE_USER_ID,
      language = if (config.language == "fr") "fr" else "ar",
      themeStyle = sanitizeThemeStyle(config.themeStyle, "coinbase"),
      themeMode = if (config.themeMode in listOf("system", "light", "dark")) config.themeMode else "system",
      fontScale = if (config.fontScale in listOf("small", "normal", "large")) config.fontScale else "normal",
      syndicatName = config.syndicatName.trim().ifBlank { "إدارة السانديك" }.take(100),
      residenceName = config.residenceName.trim().ifBlank { "إقامة" }.take(120),
      address = config.address.trim().take(200),
      city = config.city.trim().take(100),
      treasurerName = config.treasurerName.trim().take(100),
      buildingTitle = config.buildingTitle.trim().ifBlank { "المساهمة الشهرية" }.take(180),
      currency = config.currency.trim().ifBlank { "درهم" }.take(15),
      year = config.year.coerceIn(2020L, 2100L),
      openingBalanceDh = config.openingBalanceDh.coerceIn(-10_000_000L, 100_000_000L),
      dueDayOfMonth = config.dueDayOfMonth.coerceIn(1L, 31L),
      minReminderAmountDh = config.minReminderAmountDh.coerceIn(0L, 100_000L),
      whatsappTemplate = config.whatsappTemplate.trim().take(1000),
      relanceHeaderNote = config.relanceHeaderNote.trim().take(300),
      floorsCount = config.floorsCount.coerceIn(1L, 20L),
      apartmentsCount = config.apartmentsCount.coerceIn(1L, 120L),
      defaultMonthlyFee = config.defaultMonthlyFee.coerceIn(0L, 100_000L),
      cutoffMonth = config.cutoffMonth.coerceIn(1L, 12L),
      activeMonths = config.activeMonths.filter { it in 1L..12L }.distinct().sorted().ifEmpty { listOf(10L) },
      footerNote = config.footerNote.trim().ifBlank { "—" }.take(300),
      onboardingCompleted = true,
      seeded = true,
    )
  }

  companion object {
    @Volatile
    private var INSTANCE: LocalSyndicatRepository? = null

    fun getInstance(context: Context): LocalSyndicatRepository {
      return INSTANCE ?: synchronized(this) {
        INSTANCE ?: LocalSyndicatRepository(context.applicationContext).also { INSTANCE = it }
      }
    }
  }
}
