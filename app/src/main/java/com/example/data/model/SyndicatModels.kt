package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class MonthColumn(
  val monthNumber: Int,
  val key: String,
  val shortFr: String,
  val fullFr: String,
  val shortAr: String,
  val fullAr: String,
  val defaultC49Fee: Long,
) {
  JAN(1, "jan", "Jan", "Janvier", "يناير", "يناير", 50L),
  FEB(2, "feb", "Fév", "Février", "فبراير", "فبراير", 50L),
  MAR(3, "mar", "Mars", "Mars", "مارس", "مارس", 50L),
  APR(4, "apr", "Avr", "Avril", "أبريل", "أبريل", 50L),
  MAY(5, "may", "Mai", "Mai", "ماي", "ماي", 50L),
  JUIN(6, "juin", "Juin", "Juin", "يونيو", "يونيو", 100L),
  JUIL(7, "juil", "Juil", "Juillet", "يوليوز", "يوليوز", 100L),
  AOUT(8, "aout", "Août", "Août", "غشت", "غشت", 50L),
  SEPT(9, "sept", "Sept", "Septembre", "شتنبر", "شتنبر", 50L),
  OCT(10, "oct", "Oct", "Octobre", "أكتوبر", "أكتوبر", 50L),
  NOV(11, "nov", "Nov", "Novembre", "نونبر", "نونبر", 50L),
  DEC(12, "dec", "Déc", "Décembre", "دجنبر", "دجنبر", 50L);

  companion object {
    val DEFAULT_C49_ACTIVE_NUMBERS: List<Long> = listOf(6L, 7L, 8L, 9L, 10L, 11L, 12L)
    val FULL_YEAR_NUMBERS: List<Long> = (1L..12L).toList()

    fun fromNumber(num: Int): MonthColumn {
      return entries.firstOrNull { it.monthNumber == num } ?: OCT
    }
  }
}

enum class UnitStatus(val code: String) {
  OCCUPIED("occupied"),
  EMPTY("empty"),
  EXEMPT("exempt");

  companion object {
    fun fromCode(code: String?): UnitStatus = when (code?.lowercase()) {
      "empty" -> EMPTY
      "exempt" -> EXEMPT
      else -> OCCUPIED
    }
  }
}

enum class PaymentBadgeStatus {
  PAID,
  PARTIAL,
  LATE,
  EMPTY,
  EXEMPT,
}

val EXPENSE_CATEGORIES = listOf(
  "Ménage",
  "Électricité",
  "Eau",
  "Ascenseur",
  "Gardiennage",
  "Entretien",
  "Réparations",
  "Divers",
  "Historique",
)

const val DEFAULT_WHATSAPP_TEMPLATE_AR =
  "السلام عليكم {الاسم}\n" +
    "تذكير بسيط بخصوص المساهمة الشهرية لإقامة {اسم_العمارة} (الشقة {رقم_الشقة})، المبلغ المتبقي هو {المبلغ} {العملة} ({الأشهر_المتبقية}) إلى غاية نهاية شهر {الشهر}.\n" +
    "وشكراً على تفهمكم وتعاونكم."

const val DEFAULT_WHATSAPP_TEMPLATE_URGENT_AR =
  "تحية طيبة {الاسم}،\n" +
    "نحيطكم علماً بأن واجب المساهمة الشهرية للشقة رقم {رقم_الشقة} بإقامة {اسم_العمارة} لا يزال في ذمتكم بمبلغ {المبلغ} {العملة} عن الأشهر ({الأشهر_المتبقية}).\n" +
    "المرجو تسوية الوضعية قبل يوم {يوم_الأداء} من الشهر لضمان استمرار خدمات النظافة والصيانة. شكراً لكم."

const val DEFAULT_WHATSAPP_TEMPLATE_FR =
  "Bonjour {الاسم},\n" +
    "Petit rappel concernant la cotisation syndicale de la résidence {اسم_العمارة} (Appt {رقم_الشقة}) : le reste à régler est de {المبلغ} {العملة} ({الأشهر_المتبقية}) à fin {الشهر}.\n" +
    "Merci beaucoup pour votre collaboration."

const val DEFAULT_WHATSAPP_TEMPLATE_URGENT_FR =
  "Bonjour {الاسم},\n" +
    "Sauf erreur de notre part, la cotisation syndicale de l'appartement {رقم_الشقة} ({اسم_العمارة}) présente un solde impayé de {المبلغ} {العملة} pour : {الأشهر_المتبقية}.\n" +
    "Merci de bien vouloir régulariser avant le {يوم_الأداء} du mois afin d'assurer le bon entretien de l'immeuble."

const val DEFAULT_RELANCE_HEADER_AR =
  "نرجو من السادة المتأخرين تسوية وضعيتهم في أقرب الآجال وشكراً لتعاونكم"

fun buildFloorLabels(includeRdc: Boolean, floorsCount: Int): List<String> {
  val safeCount = floorsCount.coerceIn(1, 20)
  return List(safeCount) { idx ->
    if (includeRdc) {
      when (idx) {
        0 -> "RDC"
        1 -> "1er"
        else -> "${idx}e"
      }
    } else {
      when (idx) {
        0 -> "1er"
        else -> "${idx + 1}e"
      }
    }
  }
}

@IgnoreExtraProperties
data class SyndicatConfig(
  @DocumentId val id: String = "",
  val userId: String = "",
  val language: String = "ar",
  val digitsAr: Boolean = false,
  val themeStyle: String = "airtable",
  val themeMode: String = "system",
  val fontScale: String = "normal",
  val syndicatName: String = "إدارة السانديك",
  val residenceName: String = "C49 GH12 – بيت المستقبل 2",
  val address: String = "GH12 · بيت المستقبل 2",
  val city: String = "",
  val treasurerName: String = "",
  val buildingTitle: String = "المساهمة الشهرية 2026 – C49 GH12 بيت المستقبل 2",
  val currency: String = "درهم",
  val year: Long = 2026L,
  val openingBalanceDh: Long = 0L,
  val dueDayOfMonth: Long = 5L,
  val minReminderAmountDh: Long = 20L,
  val whatsappTemplate: String = DEFAULT_WHATSAPP_TEMPLATE_AR,
  val relanceHeaderNote: String = DEFAULT_RELANCE_HEADER_AR,
  val includeRdc: Boolean = true,
  val floorsCount: Long = 5L,
  val apartmentsCount: Long = 30L,
  val floorAptCounts: List<Long> = emptyList(),
  val defaultMonthlyFee: Long = 50L,
  val cutoffMonth: Long = 10L,
  val activeMonths: List<Long> = MonthColumn.DEFAULT_C49_ACTIVE_NUMBERS,
  val feeJan: Long = 50L,
  val feeFeb: Long = 50L,
  val feeMar: Long = 50L,
  val feeApr: Long = 50L,
  val feeMay: Long = 50L,
  val feeJuin: Long = 100L,
  val feeJuil: Long = 100L,
  val feeAout: Long = 50L,
  val feeSept: Long = 50L,
  val feeOct: Long = 50L,
  val feeNov: Long = 50L,
  val feeDec: Long = 50L,
  val footerNote: String = "يتم تحيين هذا الإعلان بانتظام.",
  val onboardingCompleted: Boolean = true,
  val seeded: Boolean = false,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
) {
  @get:Exclude
  val activeMonthColumns: List<MonthColumn>
    get() {
      val set = activeMonths.map { it.toInt() }.toSet()
      val cols = MonthColumn.entries.filter { it.monthNumber in set }
      return cols.ifEmpty {
        MonthColumn.entries.filter { it.monthNumber.toLong() in MonthColumn.DEFAULT_C49_ACTIVE_NUMBERS }
      }
    }

  @get:Exclude
  val cutoffMonthColumns: List<MonthColumn>
    get() = activeMonthColumns.filter { it.monthNumber <= cutoffMonth.toInt() }

  @get:Exclude
  val generatedFloors: List<String>
    get() = buildFloorLabels(includeRdc = includeRdc, floorsCount = floorsCount.toInt())

  fun feeForMonth(month: MonthColumn): Long = when (month) {
    MonthColumn.JAN -> feeJan
    MonthColumn.FEB -> feeFeb
    MonthColumn.MAR -> feeMar
    MonthColumn.APR -> feeApr
    MonthColumn.MAY -> feeMay
    MonthColumn.JUIN -> feeJuin
    MonthColumn.JUIL -> feeJuil
    MonthColumn.AOUT -> feeAout
    MonthColumn.SEPT -> feeSept
    MonthColumn.OCT -> feeOct
    MonthColumn.NOV -> feeNov
    MonthColumn.DEC -> feeDec
  }

  fun withMonthFee(month: MonthColumn, fee: Long): SyndicatConfig {
    val clean = fee.coerceIn(0L, 100_000L)
    return when (month) {
      MonthColumn.JAN -> copy(feeJan = clean)
      MonthColumn.FEB -> copy(feeFeb = clean)
      MonthColumn.MAR -> copy(feeMar = clean)
      MonthColumn.APR -> copy(feeApr = clean)
      MonthColumn.MAY -> copy(feeMay = clean)
      MonthColumn.JUIN -> copy(feeJuin = clean)
      MonthColumn.JUIL -> copy(feeJuil = clean)
      MonthColumn.AOUT -> copy(feeAout = clean)
      MonthColumn.SEPT -> copy(feeSept = clean)
      MonthColumn.OCT -> copy(feeOct = clean)
      MonthColumn.NOV -> copy(feeNov = clean)
      MonthColumn.DEC -> copy(feeDec = clean)
    }
  }

  fun withAllFeesSetTo(fee: Long): SyndicatConfig {
    val clean = fee.coerceIn(0L, 100_000L)
    return copy(
      defaultMonthlyFee = clean,
      feeJan = clean,
      feeFeb = clean,
      feeMar = clean,
      feeApr = clean,
      feeMay = clean,
      feeJuin = clean,
      feeJuil = clean,
      feeAout = clean,
      feeSept = clean,
      feeOct = clean,
      feeNov = clean,
      feeDec = clean,
    )
  }

  fun effectiveFloorCounts(): List<Int> {
    val floors = generatedFloors
    val fCount = floors.size.coerceAtLeast(1)
    if (floorAptCounts.size == fCount && floorAptCounts.sum() > 0L) {
      return floorAptCounts.map { it.toInt().coerceAtLeast(0) }
    }
    val totalAppts = apartmentsCount.toInt().coerceAtLeast(1)
    val basePerFloor = totalAppts / fCount
    val remainder = totalAppts % fCount
    return List(fCount) { i -> basePerFloor + if (i < remainder) 1 else 0 }
  }

  fun floorForApartmentNumber(aptNumber: Int): String {
    val floors = generatedFloors
    val counts = effectiveFloorCounts()
    var cumulative = 0
    for (i in floors.indices) {
      cumulative += counts.getOrElse(i) { 0 }
      if (aptNumber <= cumulative) {
        return floors[i]
      }
    }
    return floors.last()
  }
}

@IgnoreExtraProperties
data class ApartmentRecord(
  @DocumentId val id: String = "",
  val userId: String = "",
  val number: Long = 1L,
  val customNumberLabel: String = "",
  val floor: String = "RDC",
  val status: String = "occupied",
  val ownerName: String = "",
  val ownerNameAr: String = "",
  val phone: String = "",
  val isTenant: Boolean = false,
  val customMonthlyFee: Long = -1L,
  val lastReminderAt: Long = 0L,
  val receiptCount: Long = 0L,
  val jan: Long = 0L,
  val feb: Long = 0L,
  val mar: Long = 0L,
  val apr: Long = 0L,
  val may: Long = 0L,
  val juin: Long = 0L,
  val juil: Long = 0L,
  val aout: Long = 0L,
  val sept: Long = 0L,
  val oct: Long = 0L,
  val nov: Long = 0L,
  val dec: Long = 0L,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
) {
  @get:Exclude
  val displayNumber: String
    get() = customNumberLabel.trim().ifBlank { number.toString() }

  @get:Exclude
  val totalAllMonthsDh: Long
    get() = jan + feb + mar + apr + may + juin + juil + aout + sept + oct + nov + dec

  @get:Exclude
  val totalDh: Long
    get() = totalAllMonthsDh

  fun displayOwner(isArabic: Boolean): String {
    return if (isArabic) {
      ownerNameAr.trim().ifBlank { ownerName.trim() }
    } else {
      ownerName.trim().ifBlank { ownerNameAr.trim() }
    }
  }

  fun secondaryOwner(isArabic: Boolean): String {
    return if (isArabic) {
      if (ownerNameAr.isNotBlank() && ownerName.isNotBlank() && ownerNameAr.trim() != ownerName.trim()) {
        ownerName.trim()
      } else {
        ""
      }
    } else {
      if (ownerName.isNotBlank() && ownerNameAr.isNotBlank() && ownerName.trim() != ownerNameAr.trim()) {
        ownerNameAr.trim()
      } else {
        ""
      }
    }
  }

  fun effectiveUnitStatus(config: SyndicatConfig): UnitStatus {
    return when (UnitStatus.fromCode(status)) {
      UnitStatus.EMPTY -> UnitStatus.EMPTY
      UnitStatus.EXEMPT -> UnitStatus.EXEMPT
      UnitStatus.OCCUPIED -> {
        if (ownerName.isBlank() && ownerNameAr.isBlank() && totalForConfig(config) == 0L) {
          UnitStatus.EMPTY
        } else {
          UnitStatus.OCCUPIED
        }
      }
    }
  }

  fun totalForConfig(config: SyndicatConfig): Long {
    return config.activeMonthColumns.sumOf { amountFor(it) }
  }

  fun isVacantForConfig(config: SyndicatConfig): Boolean {
    val st = effectiveUnitStatus(config)
    return st == UnitStatus.EMPTY || st == UnitStatus.EXEMPT
  }

  @get:Exclude
  val isVacant: Boolean
    get() = status == "empty" || (ownerName.isBlank() && ownerNameAr.isBlank() && totalAllMonthsDh == 0L)

  fun expectedFeeForMonth(month: MonthColumn, config: SyndicatConfig): Long {
    if (isVacantForConfig(config)) return 0L
    return if (customMonthlyFee >= 0L) customMonthlyFee else config.feeForMonth(month)
  }

  fun dueAtCutoffDh(config: SyndicatConfig): Long {
    if (isVacantForConfig(config)) return 0L
    return config.cutoffMonthColumns.sumOf { col -> expectedFeeForMonth(col, config) }
  }

  fun missingMonths(config: SyndicatConfig): List<MonthColumn> {
    if (isVacantForConfig(config)) return emptyList()
    return config.cutoffMonthColumns.filter { col ->
      val expected = expectedFeeForMonth(col, config)
      expected > 0L && amountFor(col) < expected
    }
  }

  fun remainingDh(config: SyndicatConfig): Long {
    if (isVacantForConfig(config)) return 0L
    val perMonthShortfall = config.cutoffMonthColumns.sumOf { col ->
      (expectedFeeForMonth(col, config) - amountFor(col)).coerceAtLeast(0L)
    }
    val globalNetShortfall = (dueAtCutoffDh(config) - totalForConfig(config)).coerceAtLeast(0L)
    return minOf(perMonthShortfall, globalNetShortfall.takeIf { totalForConfig(config) >= dueAtCutoffDh(config) } ?: perMonthShortfall)
  }

  fun advanceDh(config: SyndicatConfig): Long {
    if (isVacantForConfig(config)) return 0L
    return (totalForConfig(config) - dueAtCutoffDh(config)).coerceAtLeast(0L)
  }

  fun isUpToDate(config: SyndicatConfig): Boolean {
    return effectiveUnitStatus(config) == UnitStatus.OCCUPIED && remainingDh(config) == 0L
  }

  fun badgeStatus(config: SyndicatConfig): PaymentBadgeStatus {
    return when (effectiveUnitStatus(config)) {
      UnitStatus.EMPTY -> PaymentBadgeStatus.EMPTY
      UnitStatus.EXEMPT -> PaymentBadgeStatus.EXEMPT
      UnitStatus.OCCUPIED -> {
        val rem = remainingDh(config)
        val paid = totalForConfig(config)
        when {
          rem == 0L -> PaymentBadgeStatus.PAID
          paid > 0L -> PaymentBadgeStatus.PARTIAL
          else -> PaymentBadgeStatus.LATE
        }
      }
    }
  }

  fun settleAllActiveMonths(config: SyndicatConfig): ApartmentRecord {
    var updated = this.copy(status = "occupied")
    for (col in config.activeMonthColumns) {
      updated = updated.withMonthAmount(col, expectedFeeForMonth(col, config).takeIf { it > 0 } ?: config.feeForMonth(col))
    }
    return updated
  }

  /**
   * Distributes an incoming payment (partial, exact, multi-month, or advance) automatically
   * across the oldest unpaid active months in chronological order.
   */
  fun applyLumpSumPayment(paymentDh: Long, config: SyndicatConfig): ApartmentRecord {
    var pool = paymentDh.coerceAtLeast(0L)
    if (pool == 0L) return this
    var updated = if (status == "empty") copy(status = "occupied") else this
    for (col in config.activeMonthColumns) {
      if (pool <= 0L) break
      val target = if (customMonthlyFee >= 0L) customMonthlyFee else config.feeForMonth(col)
      val current = updated.amountFor(col)
      if (current < target) {
        val needed = target - current
        val toAdd = minOf(pool, needed)
        updated = updated.withMonthAmount(col, current + toAdd)
        pool -= toAdd
      }
    }
    // If excess advance remains after all active months are full, add to the last active month
    if (pool > 0L && config.activeMonthColumns.isNotEmpty()) {
      val lastCol = config.activeMonthColumns.last()
      updated = updated.withMonthAmount(lastCol, updated.amountFor(lastCol) + pool)
    }
    return updated
  }

  fun amountFor(month: MonthColumn): Long = when (month) {
    MonthColumn.JAN -> jan
    MonthColumn.FEB -> feb
    MonthColumn.MAR -> mar
    MonthColumn.APR -> apr
    MonthColumn.MAY -> may
    MonthColumn.JUIN -> juin
    MonthColumn.JUIL -> juil
    MonthColumn.AOUT -> aout
    MonthColumn.SEPT -> sept
    MonthColumn.OCT -> oct
    MonthColumn.NOV -> nov
    MonthColumn.DEC -> dec
  }

  fun withMonthAmount(month: MonthColumn, amount: Long): ApartmentRecord {
    val sanitized = amount.coerceIn(0L, 100_000L)
    return when (month) {
      MonthColumn.JAN -> copy(jan = sanitized)
      MonthColumn.FEB -> copy(feb = sanitized)
      MonthColumn.MAR -> copy(mar = sanitized)
      MonthColumn.APR -> copy(apr = sanitized)
      MonthColumn.MAY -> copy(may = sanitized)
      MonthColumn.JUIN -> copy(juin = sanitized)
      MonthColumn.JUIL -> copy(juil = sanitized)
      MonthColumn.AOUT -> copy(aout = sanitized)
      MonthColumn.SEPT -> copy(sept = sanitized)
      MonthColumn.OCT -> copy(oct = sanitized)
      MonthColumn.NOV -> copy(nov = sanitized)
      MonthColumn.DEC -> copy(dec = sanitized)
    }
  }
}

@IgnoreExtraProperties
data class ExpenseRecord(
  @DocumentId val id: String = "",
  val userId: String = "",
  val title: String = "",
  val category: String = "Divers",
  val amountDh: Long = 0L,
  val paidAmountDh: Long = -1L,
  val supplier: String = "",
  val dateLabel: String = "",
  val notes: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
) {
  @get:Exclude
  val effectivePaidDh: Long
    get() = if (paidAmountDh < 0L) amountDh else paidAmountDh.coerceIn(0L, amountDh)

  @get:Exclude
  val remainingToPayDh: Long
    get() = (amountDh - effectivePaidDh).coerceAtLeast(0L)

  @get:Exclude
  val isFullyPaid: Boolean
    get() = remainingToPayDh == 0L
}

val PROJECT_CATEGORIES = listOf(
  "Peinture",
  "Sécurité",
  "Étanchéité",
  "Ascenseur",
  "Plomberie",
  "Électricité",
  "Entretien",
  "Jardin",
  "Divers",
)

enum class ProjectStatus(val code: String, val sortOrder: Int) {
  IN_PROGRESS("in_progress", 0),
  PLANNED("planned", 1),
  COMPLETED("completed", 2);

  companion object {
    fun fromCode(code: String?): ProjectStatus = when (code?.lowercase()) {
      "in_progress" -> IN_PROGRESS
      "completed" -> COMPLETED
      else -> PLANNED
    }
  }
}

enum class ProjectPriority(val level: Int) {
  URGENT(1),
  HIGH(2),
  NORMAL(3),
  LOW(4);

  companion object {
    fun fromLevel(level: Long): ProjectPriority = when (level.toInt()) {
      1 -> URGENT
      2 -> HIGH
      3 -> NORMAL
      4 -> LOW
      else -> HIGH
    }
  }
}

@IgnoreExtraProperties
data class ProjectRecord(
  @DocumentId val id: String = "",
  val userId: String = "",
  val titleAr: String = "",
  val titleFr: String = "",
  val descriptionAr: String = "",
  val descriptionFr: String = "",
  val category: String = "Entretien",
  val priority: Long = 2L,
  val status: String = "planned",
  val targetBudgetDh: Long = 0L,
  val minCashToStartDh: Long = 0L,
  val allocatedAmountDh: Long = 0L,
  val progressPercent: Long = 0L,
  val contractor: String = "",
  val targetDateLabel: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
) {
  @get:Exclude
  val parsedStatus: ProjectStatus
    get() = ProjectStatus.fromCode(status)

  @get:Exclude
  val parsedPriority: ProjectPriority
    get() = ProjectPriority.fromLevel(priority)

  @get:Exclude
  val effectiveMinCashToStartDh: Long
    get() = if (minCashToStartDh > 0L) minCashToStartDh else targetBudgetDh.coerceAtLeast(0L)

  @get:Exclude
  val clampedProgressPercent: Int
    get() = when (parsedStatus) {
      ProjectStatus.COMPLETED -> 100
      else -> progressPercent.toInt().coerceIn(0, 100)
    }

  @get:Exclude
  val remainingBudgetDh: Long
    get() = (targetBudgetDh - allocatedAmountDh).coerceAtLeast(0L)

  fun displayTitle(isArabic: Boolean): String {
    return if (isArabic) {
      titleAr.trim().ifBlank { titleFr.trim() }
    } else {
      titleFr.trim().ifBlank { titleAr.trim() }
    }
  }

  fun secondaryTitle(isArabic: Boolean): String {
    return if (isArabic) {
      if (titleAr.isNotBlank() && titleFr.isNotBlank() && titleAr.trim() != titleFr.trim()) {
        titleFr.trim()
      } else {
        ""
      }
    } else {
      if (titleFr.isNotBlank() && titleAr.isNotBlank() && titleFr.trim() != titleAr.trim()) {
        titleAr.trim()
      } else {
        ""
      }
    }
  }

  fun displayDescription(isArabic: Boolean): String {
    return if (isArabic) {
      descriptionAr.trim().ifBlank { descriptionFr.trim() }
    } else {
      descriptionFr.trim().ifBlank { descriptionAr.trim() }
    }
  }
}

data class ProjectCaisseReadiness(
  val project: ProjectRecord,
  val requiredAdvanceDh: Long,
  val availableCashNowDh: Long,
  val canStartNowDirect: Boolean,
  val directShortfallDh: Long,
  val directCoveragePercent: Int,
  val cashAfterHigherPrioritiesDh: Long,
  val canStartInPriorityWaterfall: Boolean,
  val waterfallShortfallDh: Long,
  val estimatedMonthsToStart: Int,
)

fun sortProjectsByPriority(projects: List<ProjectRecord>): List<ProjectRecord> {
  return projects.sortedWith(
    compareBy<ProjectRecord>(
      { it.priority.coerceIn(1L, 4L) },
      { it.parsedStatus.sortOrder },
      { it.effectiveMinCashToStartDh },
      { it.targetBudgetDh },
      { it.id },
    )
  )
}

fun evaluatePlannedProjectsReadiness(
  plannedProjects: List<ProjectRecord>,
  availableCashDh: Long,
  expectedMonthlyIncomeDh: Long,
): List<ProjectCaisseReadiness> {
  val sortedPlanned = plannedProjects.sortedWith(
    compareBy<ProjectRecord>(
      { it.priority.coerceIn(1L, 4L) },
      { it.effectiveMinCashToStartDh },
      { it.targetBudgetDh },
      { it.id },
    )
  )
  val positiveCash = availableCashDh.coerceAtLeast(0L)
  var waterfallPool = positiveCash

  return sortedPlanned.map { proj ->
    val req = proj.effectiveMinCashToStartDh
    val canDirect = positiveCash >= req
    val directShortfall = (req - positiveCash).coerceAtLeast(0L)
    val directPct = if (req <= 0L) {
      100
    } else {
      ((positiveCash.toDouble() / req.toDouble()) * 100.0).toInt().coerceIn(0, 100)
    }

    val cashBeforeThis = waterfallPool
    val canWaterfall = cashBeforeThis >= req
    val waterfallShortfall = (req - cashBeforeThis).coerceAtLeast(0L)
    if (canWaterfall) {
      waterfallPool = (waterfallPool - req).coerceAtLeast(0L)
    }

    val estMonths = when {
      canDirect -> 0
      expectedMonthlyIncomeDh <= 0L -> -1
      else -> kotlin.math.ceil(directShortfall.toDouble() / expectedMonthlyIncomeDh.toDouble()).toInt()
    }

    ProjectCaisseReadiness(
      project = proj,
      requiredAdvanceDh = req,
      availableCashNowDh = availableCashDh,
      canStartNowDirect = canDirect,
      directShortfallDh = directShortfall,
      directCoveragePercent = directPct,
      cashAfterHigherPrioritiesDh = cashBeforeThis,
      canStartInPriorityWaterfall = canWaterfall,
      waterfallShortfallDh = waterfallShortfall,
      estimatedMonthsToStart = estMonths,
    )
  }
}

