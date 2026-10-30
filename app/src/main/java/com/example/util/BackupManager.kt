package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
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
import com.example.data.model.normalizeSyndicatCategory
import com.example.ui.i18n.LanguagePreferences
import com.example.ui.viewmodel.SyndicatDashboardData
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject

data class ParsedSyndicatBackup(
  val config: SyndicatConfig,
  val apartments: List<ApartmentRecord>,
  val expenses: List<ExpenseRecord>,
  val projects: List<ProjectRecord> = emptyList(),
  val payments: List<PaymentRecord> = emptyList(),
)

data class ParsedOwnerRow(
  val apartmentNumber: Int,
  val ownerNameAr: String,
  val ownerNameLatin: String,
  val phone: String,
)

object BackupManager {

  fun serializeBackupJson(data: SyndicatDashboardData): String {
    val root = JSONObject()
    root.put("formatVersion", 1)
    root.put("exportedAtEpochMs", System.currentTimeMillis())

    val cfg = data.config
    val cfgObj = JSONObject().apply {
      put("language", cfg.language)
      put("digitsAr", cfg.digitsAr)
      put("themeStyle", cfg.themeStyle)
      put("themeMode", cfg.themeMode)
      put("fontScale", cfg.fontScale)
      put("syndicatName", cfg.syndicatName)
      put("residenceName", cfg.residenceName)
      put("address", cfg.address)
      put("city", cfg.city)
      put("treasurerName", cfg.treasurerName)
      put("buildingTitle", cfg.buildingTitle)
      put("currency", cfg.currency)
      put("year", data.baseYear)
      put("openingBalanceDh", cfg.openingBalanceDh)
      put("dueDayOfMonth", cfg.dueDayOfMonth)
      put("minReminderAmountDh", cfg.minReminderAmountDh)
      put("whatsappTemplate", cfg.whatsappTemplate)
      put("relanceHeaderNote", cfg.relanceHeaderNote)
      put("includeRdc", cfg.includeRdc)
      put("floorsCount", cfg.floorsCount)
      put("apartmentsCount", cfg.apartmentsCount)
      put("floorAptCounts", JSONArray(cfg.floorAptCounts))
      put("defaultMonthlyFee", cfg.defaultMonthlyFee)
      put("cutoffMonth", cfg.cutoffMonth)
      put("activeMonths", JSONArray(cfg.activeMonths))
      put("feeJan", cfg.feeJan)
      put("feeFeb", cfg.feeFeb)
      put("feeMar", cfg.feeMar)
      put("feeApr", cfg.feeApr)
      put("feeMay", cfg.feeMay)
      put("feeJuin", cfg.feeJuin)
      put("feeJuil", cfg.feeJuil)
      put("feeAout", cfg.feeAout)
      put("feeSept", cfg.feeSept)
      put("feeOct", cfg.feeOct)
      put("feeNov", cfg.feeNov)
      put("feeDec", cfg.feeDec)
      put("footerNote", cfg.footerNote)
    }
    root.put("config", cfgObj)

    val sourceApartments = data.baseApartments.ifEmpty { data.apartments }
    val apptsArr = JSONArray()
    for (appt in sourceApartments.sortedBy { it.number }) {
      val safeNum = appt.number.coerceIn(1L, 200L)
      apptsArr.put(
        JSONObject().apply {
          put("id", canonicalApartmentDocId(safeNum))
          put("number", safeNum)
          put("customNumberLabel", appt.customNumberLabel)
          put("floor", appt.floor)
          put("status", appt.status)
          put("ownerName", appt.ownerName)
          put("ownerNameAr", appt.ownerNameAr)
          put("phone", appt.phone)
          put("isTenant", appt.isTenant)
          put("customMonthlyFee", appt.customMonthlyFee)
          put("lastReminderAt", appt.lastReminderAt)
          put("receiptCount", appt.receiptCount)
          put("jan", appt.jan)
          put("feb", appt.feb)
          put("mar", appt.mar)
          put("apr", appt.apr)
          put("may", appt.may)
          put("juin", appt.juin)
          put("juil", appt.juil)
          put("aout", appt.aout)
          put("sept", appt.sept)
          put("oct", appt.oct)
          put("nov", appt.nov)
          put("dec", appt.dec)
        }
      )
    }
    root.put("apartments", apptsArr)

    val payArr = JSONArray()
    for (pay in data.payments) {
      val safeAptNum = pay.apartmentNumber.coerceIn(1L, 200L)
      payArr.put(
        JSONObject().apply {
          put("id", pay.id)
          put("apartmentId", canonicalApartmentDocId(safeAptNum))
          put("apartmentNumber", safeAptNum)
          put("year", pay.year.coerceIn(2020L, 2100L))
          put("month", pay.month.coerceIn(0L, 12L))
          put("monthsCovered", JSONArray(pay.monthsCovered.filter { it in 1L..12L }))
          put("amountDh", pay.amountDh.coerceIn(1L, 1_000_000L))
          put("paymentDate", pay.paymentDate)
          put("paymentMethod", PaymentMethod.fromCode(pay.paymentMethod).code)
          put("receiptNumber", pay.receiptNumber.coerceIn(0L, 100_000L))
          put("notes", pay.notes)
        }
      )
    }
    root.put("payments", payArr)

    val expArr = JSONArray()
    for (exp in data.expenses) {
      val safeAmt = exp.amountDh.coerceIn(0L, 10_000_000L)
      val safePaid = if (exp.paidAmountDh < 0L) -1L else exp.paidAmountDh.coerceIn(0L, safeAmt)
      expArr.put(
        JSONObject().apply {
          put("id", exp.id)
          put("title", exp.title)
          put("category", normalizeSyndicatCategory(exp.category))
          put("amountDh", safeAmt)
          put("paidAmountDh", safePaid)
          put("supplier", exp.supplier)
          put("dateLabel", exp.dateLabel)
          put("notes", exp.notes)
        }
      )
    }
    root.put("expenses", expArr)

    val projArr = JSONArray()
    for (proj in data.projects) {
      projArr.put(
        JSONObject().apply {
          put("id", proj.id)
          put("titleAr", proj.titleAr)
          put("titleFr", proj.titleFr)
          put("descriptionAr", proj.descriptionAr)
          put("descriptionFr", proj.descriptionFr)
          put("category", normalizeSyndicatCategory(proj.category, "Entretien"))
          put("priority", proj.priority)
          put("status", proj.status)
          put("targetBudgetDh", proj.targetBudgetDh)
          put("minCashToStartDh", proj.minCashToStartDh)
          put("allocatedAmountDh", proj.allocatedAmountDh)
          put("progressPercent", proj.progressPercent)
          put("contractor", proj.contractor)
          put("targetDateLabel", proj.targetDateLabel)
        }
      )
    }
    root.put("projects", projArr)

    return root.toString(2)
  }

  fun parseBackupJson(jsonString: String, targetUserId: String): ParsedSyndicatBackup {
    val root = JSONObject(jsonString)
    val cfgObj = root.optJSONObject("config") ?: JSONObject()

    val activeMonthsList = mutableListOf<Long>()
    val activeArr = cfgObj.optJSONArray("activeMonths")
    if (activeArr != null) {
      for (i in 0 until activeArr.length()) {
        val m = activeArr.optLong(i, -1L)
        if (m in 1L..12L) activeMonthsList.add(m)
      }
    }

    val floorCountsList = mutableListOf<Long>()
    val floorArr = cfgObj.optJSONArray("floorAptCounts")
    if (floorArr != null) {
      for (i in 0 until floorArr.length()) {
        floorCountsList.add(floorArr.optLong(i, 0L).coerceIn(0L, 120L))
      }
    }

    val config = SyndicatConfig(
      userId = targetUserId,
      language = cfgObj.optString("language", "ar").let { if (it == "fr") "fr" else "ar" },
      digitsAr = cfgObj.optBoolean("digitsAr", false),
      themeStyle = com.example.ui.theme.sanitizeThemeStyle(cfgObj.optString("themeStyle", "coinbase"), "coinbase"),
      themeMode = cfgObj.optString("themeMode", "system").let { if (it in listOf("system", "light", "dark")) it else "system" },
      fontScale = cfgObj.optString("fontScale", "normal").let { if (it in listOf("small", "normal", "large")) it else "normal" },
      syndicatName = cfgObj.optString("syndicatName", "إدارة السانديك").take(100),
      residenceName = cfgObj.optString("residenceName", "إقامة").ifBlank { "إقامة" }.take(120),
      address = cfgObj.optString("address", "").take(200),
      city = cfgObj.optString("city", "").take(100),
      treasurerName = cfgObj.optString("treasurerName", "").take(100),
      buildingTitle = cfgObj.optString("buildingTitle", "المساهمة الشهرية").ifBlank { "المساهمة الشهرية" }.take(180),
      currency = cfgObj.optString("currency", "درهم").ifBlank { "درهم" }.take(15),
      year = cfgObj.optLong("year", 2026L).coerceIn(2020L, 2100L),
      openingBalanceDh = cfgObj.optLong("openingBalanceDh", 0L).coerceIn(-10_000_000L, 100_000_000L),
      dueDayOfMonth = cfgObj.optLong("dueDayOfMonth", 5L).coerceIn(1L, 31L),
      minReminderAmountDh = cfgObj.optLong("minReminderAmountDh", 20L).coerceIn(0L, 100_000L),
      whatsappTemplate = cfgObj.optString("whatsappTemplate", DEFAULT_WHATSAPP_TEMPLATE_AR).take(1000),
      relanceHeaderNote = cfgObj.optString("relanceHeaderNote", DEFAULT_RELANCE_HEADER_AR).take(300),
      includeRdc = cfgObj.optBoolean("includeRdc", true),
      floorsCount = cfgObj.optLong("floorsCount", 5L).coerceIn(1L, 20L),
      apartmentsCount = cfgObj.optLong("apartmentsCount", 30L).coerceIn(1L, 120L),
      floorAptCounts = floorCountsList,
      defaultMonthlyFee = cfgObj.optLong("defaultMonthlyFee", 50L).coerceIn(0L, 100_000L),
      cutoffMonth = cfgObj.optLong("cutoffMonth", 10L).coerceIn(1L, 12L),
      activeMonths = activeMonthsList.distinct().sorted().ifEmpty { MonthColumn.DEFAULT_C49_ACTIVE_NUMBERS },
      feeJan = cfgObj.optLong("feeJan", 50L).coerceIn(0L, 100_000L),
      feeFeb = cfgObj.optLong("feeFeb", 50L).coerceIn(0L, 100_000L),
      feeMar = cfgObj.optLong("feeMar", 50L).coerceIn(0L, 100_000L),
      feeApr = cfgObj.optLong("feeApr", 50L).coerceIn(0L, 100_000L),
      feeMay = cfgObj.optLong("feeMay", 50L).coerceIn(0L, 100_000L),
      feeJuin = cfgObj.optLong("feeJuin", 100L).coerceIn(0L, 100_000L),
      feeJuil = cfgObj.optLong("feeJuil", 100L).coerceIn(0L, 100_000L),
      feeAout = cfgObj.optLong("feeAout", 50L).coerceIn(0L, 100_000L),
      feeSept = cfgObj.optLong("feeSept", 50L).coerceIn(0L, 100_000L),
      feeOct = cfgObj.optLong("feeOct", 50L).coerceIn(0L, 100_000L),
      feeNov = cfgObj.optLong("feeNov", 50L).coerceIn(0L, 100_000L),
      feeDec = cfgObj.optLong("feeDec", 50L).coerceIn(0L, 100_000L),
      footerNote = cfgObj.optString("footerNote", "—").ifBlank { "—" }.take(300),
      onboardingCompleted = true,
      seeded = true,
    )

    val appts = mutableListOf<ApartmentRecord>()
    val apptsArr = root.optJSONArray("apartments")
    if (apptsArr != null) {
      for (i in 0 until apptsArr.length()) {
        val obj = apptsArr.optJSONObject(i) ?: continue
        val num = obj.optLong("number", (i + 1).toLong()).coerceIn(1L, 200L)
        appts.add(
          ApartmentRecord(
            id = canonicalApartmentDocId(num),
            userId = targetUserId,
            number = num,
            customNumberLabel = obj.optString("customNumberLabel", "").take(20),
            floor = obj.optString("floor", config.floorForApartmentNumber(num.toInt())).ifBlank { "RDC" }.take(25),
            status = obj.optString("status", "occupied").let { if (it in listOf("occupied", "empty", "exempt")) it else "occupied" },
            ownerName = obj.optString("ownerName", "").take(80),
            ownerNameAr = obj.optString("ownerNameAr", "").take(80),
            phone = obj.optString("phone", "").take(30),
            isTenant = obj.optBoolean("isTenant", false),
            customMonthlyFee = obj.optLong("customMonthlyFee", -1L).coerceIn(-1L, 100_000L),
            lastReminderAt = obj.optLong("lastReminderAt", 0L).coerceAtLeast(0L),
            receiptCount = obj.optLong("receiptCount", 0L).coerceIn(0L, 100_000L),
            jan = obj.optLong("jan", 0L).coerceIn(0L, 100_000L),
            feb = obj.optLong("feb", 0L).coerceIn(0L, 100_000L),
            mar = obj.optLong("mar", 0L).coerceIn(0L, 100_000L),
            apr = obj.optLong("apr", 0L).coerceIn(0L, 100_000L),
            may = obj.optLong("may", 0L).coerceIn(0L, 100_000L),
            juin = obj.optLong("juin", 0L).coerceIn(0L, 100_000L),
            juil = obj.optLong("juil", 0L).coerceIn(0L, 100_000L),
            aout = obj.optLong("aout", 0L).coerceIn(0L, 100_000L),
            sept = obj.optLong("sept", 0L).coerceIn(0L, 100_000L),
            oct = obj.optLong("oct", 0L).coerceIn(0L, 100_000L),
            nov = obj.optLong("nov", 0L).coerceIn(0L, 100_000L),
            dec = obj.optLong("dec", 0L).coerceIn(0L, 100_000L),
          )
        )
      }
    }

    val payments = mutableListOf<PaymentRecord>()
    val payArr = root.optJSONArray("payments")
    if (payArr != null) {
      for (i in 0 until payArr.length()) {
        val obj = payArr.optJSONObject(i) ?: continue
        val aptNum = obj.optLong("apartmentNumber", 1L).coerceIn(1L, 200L)
        val amt = obj.optLong("amountDh", 0L).coerceIn(0L, 1_000_000L)
        if (amt <= 0L) continue
        val monthsCov = mutableListOf<Long>()
        val covArr = obj.optJSONArray("monthsCovered")
        if (covArr != null) {
          for (j in 0 until covArr.length()) {
            val m = covArr.optLong(j, -1L)
            if (m in 1L..12L) monthsCov.add(m)
          }
        }
        val monthVal = obj.optLong("month", 0L).coerceIn(0L, 12L)
        payments.add(
          PaymentRecord(
            id = obj.optString("id", "pay_restored_$i").replace(Regex("[^a-zA-Z0-9_-]"), "_").ifBlank { "pay_restored_$i" },
            userId = targetUserId,
            apartmentId = canonicalApartmentDocId(aptNum),
            apartmentNumber = aptNum,
            year = obj.optLong("year", config.year).coerceIn(2020L, 2100L),
            month = monthVal,
            monthsCovered = monthsCov.distinct().sorted().take(12),
            amountDh = amt,
            paymentDate = obj.optString("paymentDate", "01/01/${config.year}").ifBlank { "01/01/${config.year}" }.take(30),
            paymentMethod = PaymentMethod.fromCode(obj.optString("paymentMethod", "cash")).code,
            receiptNumber = obj.optLong("receiptNumber", 0L).coerceIn(0L, 100_000L),
            notes = obj.optString("notes", "").take(300),
          )
        )
      }
    }

    val expenses = mutableListOf<ExpenseRecord>()
    val expArr = root.optJSONArray("expenses")
    if (expArr != null) {
      for (i in 0 until expArr.length()) {
        val obj = expArr.optJSONObject(i) ?: continue
        val cat = normalizeSyndicatCategory(obj.optString("category", "Divers"))
        val amt = obj.optLong("amountDh", 0L).coerceIn(0L, 10_000_000L)
        val rawPaid = obj.optLong("paidAmountDh", -1L)
        val safePaid = if (rawPaid < 0L) -1L else rawPaid.coerceIn(0L, amt)
        expenses.add(
          ExpenseRecord(
            id = obj.optString("id", "exp_restored_$i").replace(Regex("[^a-zA-Z0-9_-]"), "_"),
            userId = targetUserId,
            title = obj.optString("title", "مصروف").ifBlank { "مصروف" }.take(150),
            category = cat,
            amountDh = amt,
            paidAmountDh = safePaid,
            supplier = obj.optString("supplier", "").take(100),
            dateLabel = obj.optString("dateLabel", "01/01/2026").ifBlank { "01/01/2026" }.take(30),
            notes = obj.optString("notes", "").take(300),
          )
        )
      }
    }

    val projects = mutableListOf<ProjectRecord>()
    val projArr = root.optJSONArray("projects")
    if (projArr != null) {
      for (i in 0 until projArr.length()) {
        val obj = projArr.optJSONObject(i) ?: continue
        val cat = normalizeSyndicatCategory(obj.optString("category", "Entretien"), "Entretien")
        val status = obj.optString("status", "planned").let {
          if (it in listOf("in_progress", "planned", "completed")) it else "planned"
        }
        val tAr = obj.optString("titleAr", "").take(150)
        val tFr = obj.optString("titleFr", "").take(150)
        projects.add(
          ProjectRecord(
            id = obj.optString("id", "proj_restored_$i").replace(Regex("[^a-zA-Z0-9_-]"), "_"),
            userId = targetUserId,
            titleAr = if (tAr.isBlank() && tFr.isBlank()) "مشروع" else tAr,
            titleFr = tFr,
            descriptionAr = obj.optString("descriptionAr", "").take(400),
            descriptionFr = obj.optString("descriptionFr", "").take(400),
            category = cat,
            priority = obj.optLong("priority", 2L).coerceIn(1L, 4L),
            status = status,
            targetBudgetDh = obj.optLong("targetBudgetDh", 0L).coerceIn(0L, 10_000_000L),
            minCashToStartDh = obj.optLong("minCashToStartDh", 0L).coerceIn(0L, 10_000_000L),
            allocatedAmountDh = obj.optLong("allocatedAmountDh", 0L).coerceIn(0L, 10_000_000L),
            progressPercent = obj.optLong("progressPercent", 0L).coerceIn(0L, 100L),
            contractor = obj.optString("contractor", "").take(100),
            targetDateLabel = obj.optString("targetDateLabel", "").take(30),
          )
        )
      }
    }

    return ParsedSyndicatBackup(
      config = config,
      apartments = appts,
      expenses = expenses,
      projects = projects,
      payments = payments,
    )
  }

  fun saveAutoLocalSnapshot(context: Context, data: SyndicatDashboardData) {
    try {
      val dir = File(context.filesDir, "backups").apply { mkdirs() }
      val file = File(dir, "syndicat_auto_weekly.json")
      file.writeText(serializeBackupJson(data), Charsets.UTF_8)
    } catch (_: Exception) {
    }
  }

  fun exportAndShareJsonBackup(
    context: Context,
    data: SyndicatDashboardData,
    onError: (String) -> Unit,
  ) {
    try {
      val json = serializeBackupJson(data)
      val dateStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
      val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
      val file = File(exportDir, "Syndicat_Backup_$dateStamp.json")
      file.writeText(json, Charsets.UTF_8)

      LanguagePreferences.recordBackupNow(context)
      saveAutoLocalSnapshot(context, data)

      val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
      val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "نسخة احتياطية – ${data.config.residenceName}")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
      val chooser = Intent.createChooser(intent, "حفظ / مشاركة النسخة الاحتياطية").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(chooser)
    } catch (e: Exception) {
      onError(e.localizedMessage ?: "Échec de l'export JSON")
    }
  }

  /**
   * Parses CSV or multi-line quick entry into owner records.
   * Supported formats per line:
   * - "1;عبد الفتاح;Abdelfetah;0612345678"
   * - "1,عبد الفتاح,0612345678"
   * - "عبد الفتاح;0612345678" (sequential apartment index starting at 1)
   */
  fun parseOwnersBulkLines(rawText: String): List<ParsedOwnerRow> {
    val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }
    val result = mutableListOf<ParsedOwnerRow>()
    var sequentialNum = 1

    for (line in lines) {
      val parts = line.split(';', ',', '\t').map { it.trim() }
      if (parts.isEmpty()) continue
      // Skip obvious CSV header row
      if (parts[0].lowercase() in listOf("appt", "number", "num", "الشقة", "رقم")) continue

      val explicitNum = parts[0].toIntOrNull()
      if (explicitNum != null && parts.size >= 2) {
        sequentialNum = explicitNum
        val p1 = parts.getOrElse(1) { "" }
        val p2 = parts.getOrElse(2) { "" }
        val p3 = parts.getOrElse(3) { "" }
        val p2LooksLikePhone = p2.count { it.isDigit() } >= 8 && p3.isBlank()
        if (p2LooksLikePhone) {
          val hasArabic = p1.any { it.code in 0x0600..0x06FF }
          result.add(
            ParsedOwnerRow(
              apartmentNumber = explicitNum,
              ownerNameAr = if (hasArabic) p1 else "",
              ownerNameLatin = if (hasArabic) "" else p1,
              phone = p2,
            )
          )
        } else {
          result.add(
            ParsedOwnerRow(
              apartmentNumber = explicitNum,
              ownerNameAr = p1,
              ownerNameLatin = p2,
              phone = p3,
            )
          )
        }
        sequentialNum++
      } else {
        val p0 = parts.getOrElse(0) { "" }
        val p1 = parts.getOrElse(1) { "" }
        val p2 = parts.getOrElse(2) { "" }
        val p1LooksLikePhone = p1.count { it.isDigit() } >= 8 && p2.isBlank()
        val hasArabic = p0.any { it.code in 0x0600..0x06FF }
        result.add(
          ParsedOwnerRow(
            apartmentNumber = sequentialNum,
            ownerNameAr = if (hasArabic) p0 else "",
            ownerNameLatin = if (hasArabic) (if (p1LooksLikePhone) "" else p1) else p0,
            phone = if (p1LooksLikePhone) p1 else p2,
          )
        )
        sequentialNum++
      }
    }
    return result
  }
}
