package com.example.data.seed

import com.example.data.model.MonthColumn

data class SeedApartment(
  val number: Long,
  val floor: String,
  val ownerName: String,
  val ownerNameAr: String = "",
  val status: String = "occupied",
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
) {
  val docId: String
    get() = "appt_%02d".format(number)
}

object InitialSyndicatData {
  const val DEFAULT_SYNDICAT_NAME = "إدارة السانديك – C49"
  const val DEFAULT_BUILDING_TITLE = "المساهمة الشهرية 2026 – C49 GH12 بيت المستقبل 2"
  const val DEFAULT_RESIDENCE_NAME = "C49 GH12 – بيت المستقبل 2"
  const val DEFAULT_ADDRESS = "GH12 · بيت المستقبل 2"
  const val DEFAULT_CURRENCY = "درهم"
  const val DEFAULT_YEAR = 2026L
  const val DEFAULT_FOOTER_NOTE = "يتم تحيين هذا الإعلان كل يوم إثنين."
  val DEFAULT_ACTIVE_MONTHS: List<Long> = MonthColumn.DEFAULT_C49_ACTIVE_NUMBERS

  const val INITIAL_EXPENSE_ID = "exp_initial_2026"
  const val INITIAL_EXPENSE_AMOUNT_DH = 6768L
  const val INITIAL_EXPENSE_DATE = "21/10/2026"
  const val INITIAL_EXPENSE_TITLE = "مجموع المصاريف المسجلة إلى غاية 21/10/2026"
  const val INITIAL_EXPENSE_NOTES = "المبلغ الأولي المعتمد من الإعلان الرسمي للسانديك (6768 درهم)."

  // Exact 30 apartments from Syndicat C49 – Gestion (21/10/2026), Total Caisse = 8200 DH
  // Enriched with Arabic names alongside Latin names for full Arabic RTL & search support
  val apartments: List<SeedApartment> = listOf(
    // RDC (Total = 1850 DH)
    SeedApartment(1, "RDC", "Abdelfetah", "عبد الفتاح", juin = 100, juil = 100, aout = 50),
    SeedApartment(2, "RDC", "Hamid", "حميد", juin = 100, juil = 100, aout = 50, sept = 50, oct = 50),
    SeedApartment(3, "RDC", "Aziz", "عزيز", juin = 100, juil = 100, aout = 50, sept = 50, oct = 50, nov = 50),
    SeedApartment(4, "RDC", "Bouchra", "بشرى", juin = 100, juil = 100, aout = 50, sept = 50),
    SeedApartment(5, "RDC", "Toufik", "توفيق", juin = 100, juil = 100, aout = 50, sept = 50),
    SeedApartment(6, "RDC", "Etayeb", "الطيب", juin = 100, juil = 100, aout = 50),

    // 1er (Total = 1650 DH)
    SeedApartment(7, "1er", "Hicham", "هشام", juin = 100, juil = 100, aout = 50, sept = 50),
    SeedApartment(8, "1er", "Abdelhadi", "عبد الهادي", juin = 100, juil = 100, aout = 50, sept = 50, oct = 50, nov = 50),
    SeedApartment(9, "1er", "Ibtissam", "ابتسام", juin = 100, juil = 100, aout = 50, sept = 50, oct = 50),
    SeedApartment(10, "1er", "Amine", "أمين", juin = 100, juil = 100),
    SeedApartment(11, "1er", "", "", status = "empty"),
    SeedApartment(12, "1er", "Youssef", "يوسف", juin = 100, juil = 100, aout = 50, sept = 50, oct = 50, nov = 50),

    // 2e (Total = 1250 DH)
    SeedApartment(13, "2e", "Safae", "صفاء", juin = 100, juil = 100, aout = 50, sept = 50),
    SeedApartment(14, "2e", "Ayoub", "أيوب", juin = 100, juil = 100, aout = 50),
    SeedApartment(15, "2e", "Ali", "علي", juin = 100, juil = 100, aout = 50, sept = 50, oct = 50, nov = 50),
    SeedApartment(16, "2e", "Noureddine", "نور الدين", juin = 100, juil = 100, aout = 50, sept = 50),
    SeedApartment(17, "2e", "", "", status = "empty"),
    SeedApartment(18, "2e", "", "", status = "empty"),

    // 3e (Total = 1950 DH)
    SeedApartment(19, "3e", "Bilal", "بلال", juin = 100, juil = 100, aout = 50, sept = 50, oct = 50, nov = 50),
    SeedApartment(20, "3e", "Soufiane", "سفيان", juin = 100, juil = 100, aout = 50),
    SeedApartment(21, "3e", "Abdeladem", "عبد العظيم", juin = 100, juil = 100, aout = 50, sept = 50, oct = 50),
    SeedApartment(22, "3e", "Abdeddayem", "عبد الدايم", juin = 100, juil = 100, aout = 50),
    SeedApartment(23, "3e", "Essaadia", "السعدية", juin = 100, juil = 100, aout = 50, sept = 50, oct = 50),
    SeedApartment(24, "3e", "Fatima Zahrae", "فاطمة الزهراء", juin = 100, juil = 100, aout = 50, sept = 50, oct = 50),

    // 4e (Total = 1500 DH)
    SeedApartment(25, "4e", "Soumia", "سمية", juin = 100, juil = 100),
    SeedApartment(26, "4e", "Azzeddine", "عز الدين", juin = 100, juil = 100, aout = 50),
    SeedApartment(27, "4e", "Ismail", "إسماعيل", juin = 100, juil = 100),
    SeedApartment(28, "4e", "Abdelhak", "عبد الحق", juin = 100, juil = 100, aout = 50, sept = 50),
    SeedApartment(29, "4e", "Amal", "آمال", juin = 100, juil = 100, aout = 50),
    SeedApartment(30, "4e", "Ismail", "إسماعيل", juin = 100, juil = 100, aout = 50, sept = 50),
  )

  val projects: List<SeedProject> = listOf(
    SeedProject(
      docId = "proj_01",
      titleAr = "صباغة وتأهيل مدخل العمارة والدرج",
      titleFr = "Peinture & réfection du hall et cages d'escalier",
      descriptionAr = "إصلاح الشقوق، صباغة الطوابق الأربعة وتجديد مصابيح الحركة الأوتوماتيكية.",
      descriptionFr = "Rebouchage des fissures, peinture des paliers et remplacement des minuteries LED.",
      category = "Peinture",
      priority = 1L,
      status = "in_progress",
      targetBudgetDh = 2500L,
      minCashToStartDh = 1200L,
      allocatedAmountDh = 1625L,
      progressPercent = 65L,
      contractor = "المعلم رشيد · Maâlem Rachid",
      targetDateLabel = "15/11/2026",
    ),
    SeedProject(
      docId = "proj_02",
      titleAr = "تركيب كاميرات المراقبة بالمدخل",
      titleFr = "Installation de caméras de surveillance HD",
      descriptionAr = "تركيب 4 كاميرات عالية الدقة مع جهاز تسجيل لتأمين المدخل الرئيسي.",
      descriptionFr = "Pose de 4 caméras HD avec enregistreur NVR pour sécuriser l'entrée de l'immeuble.",
      category = "Sécurité",
      priority = 1L,
      status = "planned",
      targetBudgetDh = 2200L,
      minCashToStartDh = 1100L,
      allocatedAmountDh = 0L,
      progressPercent = 0L,
      contractor = "SecuriMaroc",
      targetDateLabel = "01/12/2026",
    ),
    SeedProject(
      docId = "proj_03",
      titleAr = "عزل وإصلاح سطح العمارة ضد التسربات",
      titleFr = "Étanchéité de la terrasse de l'immeuble",
      descriptionAr = "معالجة تسربات مياه الأمطار وتجديد طبقة العزل بالسطح المشترك.",
      descriptionFr = "Traitement des infiltrations d'eau pluviale et réfection de l'étanchéité terrasse.",
      category = "Étanchéité",
      priority = 2L,
      status = "planned",
      targetBudgetDh = 4500L,
      minCashToStartDh = 2500L,
      allocatedAmountDh = 0L,
      progressPercent = 0L,
      contractor = "",
      targetDateLabel = "15/01/2027",
    ),
    SeedProject(
      docId = "proj_04",
      titleAr = "تجهيز مضخة تقوية صبيب الماء",
      titleFr = "Installation d'un surpresseur d'eau",
      descriptionAr = "تركيب مضخة احتياطية لضمان استقرار صبيب الماء بالطوابق العليا.",
      descriptionFr = "Pose d'une pompe de surpression silencieuse pour les étages supérieurs.",
      category = "Plomberie",
      priority = 3L,
      status = "planned",
      targetBudgetDh = 3000L,
      minCashToStartDh = 1800L,
      allocatedAmountDh = 0L,
      progressPercent = 0L,
      contractor = "",
      targetDateLabel = "01/03/2027",
    ),
  )
}

data class SeedProject(
  val docId: String,
  val titleAr: String,
  val titleFr: String,
  val descriptionAr: String,
  val descriptionFr: String,
  val category: String,
  val priority: Long,
  val status: String,
  val targetBudgetDh: Long,
  val minCashToStartDh: Long,
  val allocatedAmountDh: Long,
  val progressPercent: Long,
  val contractor: String,
  val targetDateLabel: String,
)

