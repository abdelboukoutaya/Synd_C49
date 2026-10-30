package com.example.ui.i18n

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.data.model.MonthColumn
import com.example.data.model.PaymentBadgeStatus
import com.example.data.model.ProjectPriority
import com.example.data.model.ProjectStatus
import com.example.data.model.UnitStatus
import com.example.ui.viewmodel.StatusFilter

enum class AppLanguage(
  val code: String,
  val nativeLabel: String,
  val subtitle: String,
  val isRtl: Boolean,
) {
  AR("ar", "العربية", "واجهة عربية كاملة من اليمين إلى اليسار (RTL)", true),
  FR("fr", "Français", "Interface française de gauche à droite (LTR)", false);

  companion object {
    fun fromCode(code: String?): AppLanguage {
      return if (code?.lowercase() == "fr") FR else AR
    }
  }
}

object LanguagePreferences {
  private const val PREFS_NAME = "syndicat_locale_prefs"
  private const val KEY_LANG = "app_language"
  private const val KEY_THEME_STYLE = "app_theme_style"
  private const val KEY_PIN = "app_pin_code"
  private const val KEY_LAST_BACKUP = "last_backup_epoch_ms"
  private const val KEY_AIRTABLE_MIGRATED_V1 = "airtable_default_applied_v1"
  private const val SEVEN_DAYS_MS = 7L * 24L * 60L * 60L * 1000L

  fun getSavedLanguage(context: Context): AppLanguage {
    val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return AppLanguage.fromCode(prefs.getString(KEY_LANG, "ar"))
  }

  fun saveLanguage(context: Context, language: AppLanguage) {
    val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putString(KEY_LANG, language.code).apply()
  }

  fun getThemeStyle(context: Context): String {
    val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return com.example.ui.theme.sanitizeThemeStyle(prefs.getString(KEY_THEME_STYLE, "coinbase"), "coinbase")
  }

  fun saveThemeStyle(context: Context, style: String) {
    val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val clean = com.example.ui.theme.sanitizeThemeStyle(style, "coinbase")
    prefs.edit()
      .putString(KEY_THEME_STYLE, clean)
      .putBoolean(KEY_AIRTABLE_MIGRATED_V1, true)
      .apply()
  }

  fun isAirtableDefaultApplied(context: Context): Boolean = true

  fun markAirtableDefaultApplied(context: Context) {
    saveThemeStyle(context, getThemeStyle(context))
  }

  fun getPinCode(context: Context): String {
    val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getString(KEY_PIN, "") ?: ""
  }

  fun savePinCode(context: Context, pin: String) {
    val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putString(KEY_PIN, pin.trim()).apply()
  }

  fun getLastBackupMs(context: Context): Long {
    val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getLong(KEY_LAST_BACKUP, 0L)
  }

  fun recordBackupNow(context: Context) {
    val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putLong(KEY_LAST_BACKUP, System.currentTimeMillis()).apply()
  }

  private const val KEY_HAS_GOOGLE_SESSION = "has_previous_google_session"

  fun isBackupOverdue(context: Context): Boolean {
    val last = getLastBackupMs(context)
    if (last <= 0L) return true
    return (System.currentTimeMillis() - last) >= SEVEN_DAYS_MS
  }

  fun hasPreviousGoogleSession(context: Context): Boolean {
    val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getBoolean(KEY_HAS_GOOGLE_SESSION, false)
  }

  fun setHasPreviousGoogleSession(context: Context, active: Boolean) {
    val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putBoolean(KEY_HAS_GOOGLE_SESSION, active).apply()
  }
}

const val LRM = "\u200E"
const val RLM = "\u200F"
const val NBSP = " "

/**
 * Wraps an inherently left-to-right token (number, phone, ratio, date, percentage) with Unicode
 * Left-to-Right Marks (U+200E) so it never reverses inside Arabic RTL paragraphs.
 */
fun bidiLtr(input: String): String {
  if (input.isEmpty()) return input
  return "$LRM$input$LRM"
}

fun localizeDigits(input: String, digitsAr: Boolean): String {
  if (!digitsAr) return input
  val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
  val sb = StringBuilder(input.length)
  for (ch in input) {
    if (ch in '0'..'9') {
      sb.append(arabicDigits[ch - '0'])
    } else {
      sb.append(ch)
    }
  }
  return sb.toString()
}

/**
 * Formats an integer amount with thousands separators (e.g., 1 632 or ١ ٦٣٢) in a strictly
 * BiDi-safe left-to-right run so Arabic RTL paragraphs never invert groups into "632 1".
 */
fun formatNumber(
  amount: Long,
  digitsAr: Boolean = false,
  explicitPlus: Boolean = false,
): String {
  val signPrefix = when {
    amount < 0L -> "-$LRM"
    explicitPlus && amount > 0L -> "+$LRM"
    else -> ""
  }
  val absStr = kotlin.math.abs(amount).toString()
  val chunks = absStr
    .reversed()
    .chunked(3)
    .map { localizeDigits(it.reversed(), digitsAr) }
    .reversed()
  val grouped = chunks.joinToString("$LRM$NBSP$LRM")
  return "$LRM$signPrefix$grouped$LRM"
}

class SyndicatStrings(
  val language: AppLanguage,
  // Navigation (5 tabs per Section 3 bis & 6)
  val navDashboard: String,
  val navApartments: String,
  val navExpenses: String,
  val navPoster: String,
  val navSettings: String,
  val loadingSync: String,
  val undoAction: String,

  // Auth Screen
  val authBadge: String,
  val authTitle: String,
  val authSubtitle: String,
  val authFeat1Title: String,
  val authFeat1Sub: String,
  val authFeat2Title: String,
  val authFeat2Sub: String,
  val authFeat3Title: String,
  val authFeat3Sub: String,
  val authSignInGoogle: String,
  val authConnectingGoogle: String,
  val authCancelBtn: String,
  val authCancelledHint: String,
  val authNoAccountHint: String,
  val authTimeoutHint: String,
  val authSecurityNote: String,

  // Badges & Unit statuses
  val badgePaid: String,
  val badgePartial: String,
  val badgeLate: String,
  val badgeEmpty: String,
  val badgeExempt: String,
  val badgeTenant: String,
  val badgeReminded: String,

  // Dashboard (لوحة القيادة)
  val dashCashEyebrow: String,
  val dashCashNowLabel: String,
  val dashOpeningBalanceNote: (String) -> String,
  val dashCollectedLabel: String,
  val dashExpensesLabel: String,
  val dashNetBalanceLabel: String,
  val dashCollectionRateLabel: String,
  val dashUnpaidTitle: String,
  val dashUnpaidSub: (Int) -> String,
  val dashSendRemindersBtn: String,
  val dashViewLateBtn: String,
  val dashAllPaidTitle: String,
  val dashAllPaidSub: String,
  val dashBackupReminder: String,
  val dashBackupNowBtn: String,
  val dashCreamTip: String,
  val dashLaunchWizardBtn: String,
  val dashRelanceQueueTitle: String,
  val dashRemindBtn: String,
  val dashExportShortcutsTitle: String,
  val dashMonthlySection: (Long) -> String,
  val dashStandardFeeLegend: (Long, String) -> String,
  val dashCustomFeeLegend: (String) -> String,
  val dashFloorsSection: String,
  val dashFloorRowTitle: (String, Int, Int) -> String,

  // Apartments / Cotisations (المساهمات)
  val apptSearchHint: String,
  val apptClearSearch: String,
  val apptFilterAll: (Int) -> String,
  val apptViewCards: String,
  val apptViewGrid: String,
  val apptSubtotal: String,
  val apptRecordPayment: String,
  val apptPayFullBtn: String,
  val apptReceiptBtn: String,
  val apptDialogTitle: (String, String) -> String,
  val apptCalculatedTotal: String,
  val apptOwnerArLabel: String,
  val apptOwnerArPlaceholder: String,
  val apptOwnerLatinLabel: String,
  val apptOwnerLatinPlaceholder: String,
  val apptPhoneLabel: String,
  val apptPhonePlaceholder: String,
  val apptPickContactBtn: String,
  val apptUnitStatusLabel: String,
  val apptIsTenantLabel: String,
  val apptCustomFeeLabel: (String) -> String,
  val apptLumpSumTitle: String,
  val apptLumpSumHint: (String) -> String,
  val apptDistributeBtn: String,
  val apptMonthlyFeesHeader: (String) -> String,
  val apptSettleAllActive: String,
  val apptTariffPrefix: String,
  val apptSave: String,
  val apptCancel: String,
  val apptAdvancePrefix: String,

  // Table & Poster common headers
  val colFloor: String,
  val colAppt: String,
  val colOwner: String,
  val colTotal: String,
  val colMissingMonths: String,
  val colPaid: String,
  val colRemaining: String,
  val rowCaisse: String,
  val datePrefix: String,

  // Expenses & Caisse (المصاريف والصندوق)
  val tabExpensesMode: String,
  val tabCaisseMode: String,
  val expAddFab: String,
  val expFilterAll: (Int) -> String,
  val expEmptyTitle: String,
  val expEmptyTip: String,
  val expDialogAdd: String,
  val expDialogEdit: String,
  val expFieldTitle: String,
  val expFieldTitleHint: String,
  val expFieldCategory: String,
  val expFieldSupplier: String,
  val expFieldSupplierHint: String,
  val expFieldAmount: (String) -> String,
  val expFieldPaidAmount: (String) -> String,
  val expFieldDate: String,
  val expFieldNotes: String,
  val expDelete: String,
  val expDeleteConfirmTap: String,
  val expPaidProgressLabel: (String, String) -> String,
  val expSupplierRemaining: (String) -> String,
  val expErrTitle: String,
  val expErrAmount: String,
  val expErrDate: String,
  val caisseAvailableTitle: String,
  val caisseOpeningBalance: String,
  val caisseTotalReceipts: String,
  val caisseTotalExpenses: String,
  val caissePaidToSuppliers: String,
  val caisseUnpaidSuppliers: String,
  val caisseAccountingBalance: String,
  val caisseMovementsTitle: String,

  // Posters & 3 Official Exports (الملصقات)
  val posterPageTitle: String,
  val posterPageSub: String,
  val exportVariantLabel: String,
  val exportVariantPrint: String,
  val exportVariantScreen: String,
  val exportPdfPrimaryBtn: String,
  val exportPngSecondaryBtn: String,
  val exportCard1Title: String,
  val exportCard1Sub: String,
  val exportOptShowNames: String,
  val exportOptUntilCutoffOnly: String,
  val exportOptInkSaver: String,
  val exportCard2Title: String,
  val exportCard2Sub: String,
  val exportOptAnonymousMode: String,
  val exportCard3Title: String,
  val exportCard3Sub: String,
  val posterShareWhatsappText: String,
  val posterLegendSpecial: String,
  val posterLegendStandard: String,
  val relanceDocTitle: (String) -> String,
  val relanceDocSubtitle: (String, String, String, Long) -> String,
  val relanceBannerSummary: (Int, String) -> String,
  val relanceTotalRemaining: String,
  val caisseDocTitle: (String, Long) -> String,
  val caisseIssuedOn: (String) -> String,
  val receiptDocTitle: String,

  // Onboarding 6-Step Wizard (مساعد الإعداد الأولي)
  val wizardStepIndicator: (Int, Int) -> String,
  val wizardNextBtn: String,
  val wizardBackBtn: String,
  val wizardStartBtn: String,
  val wizardStep1Title: String,
  val wizardStep1Sub: String,
  val wizardStep2Title: String,
  val wizardStep2Sub: String,
  val wizardSimpleMode: String,
  val wizardAdvancedMode: String,
  val wizardApptsPerFloor: String,
  val wizardStep3Title: String,
  val wizardStep3Sub: String,
  val wizardBulkImportBtn: String,
  val wizardStep4Title: String,
  val wizardStep4Sub: String,
  val wizardStep5Title: String,
  val wizardStep5Sub: String,
  val wizardEnterPastPayments: String,
  val wizardStartFromZero: String,
  val wizardStep6Title: String,
  val wizardStep6Sub: String,

  // Settings (الإعدادات)
  val settingsPageTitle: String,
  val settingsPageSubtitle: String,
  val settingsOpenWizardCardTitle: String,
  val settingsOpenWizardCardSub: String,
  val settingsLangDisplayTitle: String,
  val settingsDigitsArLabel: String,
  val settingsStyleLabel: String,
  val settingsStyleAirtable: String,
  val settingsStyleXai: String,
  val settingsStyleWise: String,
  val settingsStyleDarkOnlyNote: String,
  val settingsThemeLabel: String,
  val settingsThemeSystem: String,
  val settingsThemeLight: String,
  val settingsThemeDark: String,
  val settingsFontScaleLabel: String,
  val settingsFontSmall: String,
  val settingsFontNormal: String,
  val settingsFontLarge: String,
  val settingsIdentityTitle: String,
  val settingsSyndicatName: String,
  val settingsResidenceName: String,
  val settingsCity: String,
  val settingsAddress: String,
  val settingsTreasurer: String,
  val settingsBuildingTitle: String,
  val settingsAutoTitleBtn: String,
  val settingsYear: String,
  val settingsCurrency: String,
  val settingsOpeningBalance: (String) -> String,
  val settingsFooterNote: String,
  val settingsStructureTitle: String,
  val settingsIncludeRdc: String,
  val settingsFloorsCount: String,
  val settingsApartmentsCount: String,
  val settingsFeesTitle: String,
  val settingsDefaultFee: (String) -> String,
  val settingsApplyDefaultToAll: String,
  val settingsDueDayLabel: String,
  val settingsCutoffMonthLabel: String,
  val settingsActiveMonthsLabel: String,
  val settingsPresetFullYear: String,
  val settingsPresetJunDec: String,
  val settingsWhatsappTitle: String,
  val settingsWhatsappTemplateLabel: String,
  val settingsWhatsappVarsHint: String,
  val settingsMinReminderLabel: (String) -> String,
  val settingsRelanceHeaderLabel: String,
  val settingsBackupSecurityTitle: String,
  val settingsExportJsonBtn: String,
  val settingsImportJsonBtn: String,
  val settingsImportCsvBtn: String,
  val settingsPinTitle: String,
  val settingsSetPinBtn: String,
  val settingsDisablePinBtn: String,
  val settingsSaveButton: String,
  val settingsNewBlankBuildingBtn: String,
  val settingsResetC49Btn: String,
  val settingsConfirmBtn: String,
  val settingsConnectedAs: (String) -> String,
  val signOut: String,
  val pinUnlockTitle: String,
  val pinUnlockSub: String,
  val pinWrongError: String,
) {
  val settingsWhatsappPresetsLabel: String
    get() = if (language == AppLanguage.AR) "قوالب رسائل جاهزة" else "Modèles prédéfinis"
  val whatsappPresetFriendlyAr: String
    get() = if (language == AppLanguage.AR) "ودي (عربي)" else "Courtois (AR)"
  val whatsappPresetUrgentAr: String
    get() = if (language == AppLanguage.AR) "عاجل (عربي)" else "Urgent (AR)"
  val whatsappPresetFriendlyFr: String
    get() = if (language == AppLanguage.AR) "ودي (فرنسي)" else "Courtois (FR)"
  val whatsappPresetUrgentFr: String
    get() = if (language == AppLanguage.AR) "عاجل (فرنسي)" else "Urgent (FR)"
  val whatsappPresetCustom: String
    get() = if (language == AppLanguage.AR) "النموذج المحفوظ" else "Modèle enregistré"
  val settingsWhatsappLivePreviewLabel: String
    get() = if (language == AppLanguage.AR) "معاينة حية للرسالة المولّدة" else "Aperçu en direct du message généré"
  val whatsappCampaignTitle: String
    get() = if (language == AppLanguage.AR) "حملة التذكير التلقائي عبر واتساب" else "Campagne de relance WhatsApp"
  val whatsappCampaignSub: (Int, String) -> String
    get() = { count, totalRem ->
      if (language == AppLanguage.AR) {
        "${formatNumber(count.toLong())} سكان معنيون · المتبقي: $totalRem"
      } else {
        "$count résidents à relancer · Total dû : $totalRem"
      }
    }
  val whatsappFilterAllLate: (Int) -> String
    get() = { count -> if (language == AppLanguage.AR) "الكل (${formatNumber(count.toLong())})" else "Tous ($count)" }
  val whatsappFilterUnreminded: (Int) -> String
    get() = { count -> if (language == AppLanguage.AR) "لم يُذكَّر بعد (${formatNumber(count.toLong())})" else "Non relancés ($count)" }
  val whatsappFilterWithPhone: (Int) -> String
    get() = { count -> if (language == AppLanguage.AR) "برقم واتساب (${formatNumber(count.toLong())})" else "Avec numéro ($count)" }
  val whatsappProgressStep: (Int, Int) -> String
    get() = { idx, total ->
      if (language == AppLanguage.AR) {
        "الساكن ${bidiLtr(idx.toString())} من ${bidiLtr(total.toString())}"
      } else {
        "Résident $idx sur $total"
      }
    }
  val whatsappSendAndNextBtn: String
    get() = if (language == AppLanguage.AR) "إرسال عبر واتساب والتالي" else "Envoyer sur WhatsApp & Suivant"
  val whatsappSendSingleBtn: String
    get() = if (language == AppLanguage.AR) "إرسال عبر واتساب" else "Envoyer sur WhatsApp"
  val whatsappSkipNextBtn: String
    get() = if (language == AppLanguage.AR) "تخطي للتالي" else "Passer au suivant"
  val whatsappCopyMsgBtn: String
    get() = if (language == AppLanguage.AR) "نسخ الرسالة" else "Copier le message"
  val whatsappShareGroupBtn: String
    get() = if (language == AppLanguage.AR) "مشاركة لائحة جماعية للمجموعة" else "Partager le récapitulatif au groupe"
  val whatsappMsgCopiedToast: String
    get() = if (language == AppLanguage.AR) "تم نسخ نص التذكير إلى الحافظة." else "Message de relance copié dans le presse-papiers."
  val whatsappNoPhoneHint: String
    get() = if (language == AppLanguage.AR) {
      "لا يوجد رقم هاتف مسجل لهذه الشقة — أدخل رقم واتساب أدناه ليُحفظ تلقائياً عند الإرسال:"
    } else {
      "Aucun numéro enregistré pour cet appartement — saisissez son numéro WhatsApp ci-dessous (enregistré automatiquement à l'envoi) :"
    }
  val whatsappEditMessageLabel: String
    get() = if (language == AppLanguage.AR) {
      "الرسالة الجاهزة للإرسال (تُملأ تلقائياً من القالب)"
    } else {
      "Message prêt à envoyer (rempli automatiquement via le modèle)"
    }
  val whatsappSaveTemplateCheck: String
    get() = if (language == AppLanguage.AR) {
      "حفظ هذا القالب كإعداد افتراضي للعمارة"
    } else {
      "Enregistrer ce modèle par défaut pour l'immeuble"
    }
  val whatsappAllDoneTitle: String
    get() = if (language == AppLanguage.AR) "اكتملت حملة التذكير بنجاح" else "Campagne de relance terminée"
  val whatsappAllDoneSub: String
    get() = if (language == AppLanguage.AR) {
      "تم تحضير وإرسال رسائل التذكير لجميع السكان المختارين في القائمة."
    } else {
      "Tous les résidents sélectionnés dans la file ont été traités."
    }

  fun floorDisplay(rawFloor: String): String {
    if (language == AppLanguage.FR) {
      return if (rawFloor.equals("RDC", ignoreCase = true)) "RDC" else rawFloor
    }
    return when {
      rawFloor.equals("RDC", ignoreCase = true) -> "الأرضي"
      rawFloor == "1er" -> "الأول"
      rawFloor == "2e" -> "الثاني"
      rawFloor == "3e" -> "الثالث"
      rawFloor == "4e" -> "الرابع"
      rawFloor == "5e" -> "الخامس"
      rawFloor == "6e" -> "السادس"
      rawFloor == "7e" -> "السابع"
      rawFloor == "8e" -> "الثامن"
      rawFloor == "9e" -> "التاسع"
      rawFloor == "10e" -> "العاشر"
      rawFloor.endsWith("e") -> {
        val num = rawFloor.removeSuffix("e")
        if (num.all { it.isDigit() }) "ط $num" else rawFloor
      }
      else -> rawFloor
    }
  }

  fun floorWithPrefix(rawFloor: String): String {
    return if (language == AppLanguage.FR) {
      if (rawFloor.equals("RDC", ignoreCase = true)) "Rez-de-chaussée (RDC)" else "Étage $rawFloor"
    } else {
      val ord = floorDisplay(rawFloor)
      if (ord.startsWith("الطابق")) ord else "الطابق $ord"
    }
  }

  fun monthShort(month: MonthColumn): String {
    return if (language == AppLanguage.AR) month.shortAr else month.shortFr
  }

  fun monthFull(month: MonthColumn): String {
    return if (language == AppLanguage.AR) month.fullAr else month.fullFr
  }

  fun badgeText(status: PaymentBadgeStatus): String = when (status) {
    PaymentBadgeStatus.PAID -> badgePaid
    PaymentBadgeStatus.PARTIAL -> badgePartial
    PaymentBadgeStatus.LATE -> badgeLate
    PaymentBadgeStatus.EMPTY -> badgeEmpty
    PaymentBadgeStatus.EXEMPT -> badgeExempt
  }

  fun unitStatusLabel(status: UnitStatus): String = when (status) {
    UnitStatus.OCCUPIED -> if (language == AppLanguage.AR) "مأهولة" else "Occupé"
    UnitStatus.EMPTY -> badgeEmpty
    UnitStatus.EXEMPT -> badgeExempt
  }

  fun statusFilterLabel(filter: StatusFilter, cutoffShort: String): String {
    return if (language == AppLanguage.AR) {
      when (filter) {
        StatusFilter.ALL -> "الكل"
        StatusFilter.UP_TO_DATE -> "مؤدّى ($cutoffShort)"
        StatusFilter.LATE -> "متأخر / جزئي"
        StatusFilter.VACANT -> "فارغة / معفاة"
      }
    } else {
      when (filter) {
        StatusFilter.ALL -> "Tous"
        StatusFilter.UP_TO_DATE -> "À jour ($cutoffShort)"
        StatusFilter.LATE -> "En retard"
        StatusFilter.VACANT -> "Vide / Exonéré"
      }
    }
  }

  fun categoryLabel(category: String): String {
    if (language == AppLanguage.FR) return category
    return when (category) {
      "Ménage" -> "النظافة"
      "Sécurité" -> "الأمن والكاميرات"
      "Gardiennage" -> "الحراسة"
      "Électricité" -> "الكهرباء"
      "Eau" -> "الماء"
      "Plomberie" -> "السباكة والترصيص"
      "Ascenseur" -> "المصعد"
      "Entretien" -> "الصيانة"
      "Réparations" -> "الإصلاحات"
      "Peinture" -> "الصباغة والترميم"
      "Étanchéité" -> "العزل المائي"
      "Jardin" -> "البستنة والمدخل"
      "Divers" -> "متنوعة"
      "Historique" -> "رصيد سابق"
      else -> category
    }
  }

  // --- Navigation compacte & Page des Projets (calculées pour respecter la limite JVM 255 params) ---
  val navBarDashboard: String
    get() = if (language == AppLanguage.AR) "الرئيسية" else "Accueil"
  val navBarApartments: String
    get() = if (language == AppLanguage.AR) "المساهمات" else "Cotis."
  val navProjects: String
    get() = if (language == AppLanguage.AR) "المشاريع" else "Projets"

  val projectsPageTitle: String
    get() = if (language == AppLanguage.AR) "مشاريع وأوراش العمارة" else "Projets de l'immeuble"
  val projectsPageSub: String
    get() = if (language == AppLanguage.AR) {
      "مرتبة حسب الأولوية · متابعة تقدم المشروع الحالي وجاهزية المشاريع المستقبلية حسب تسبيق الصندوق."
    } else {
      "Triés par priorité · État d'avancement du projet en cours et lancement des futurs projets selon l'avance de caisse."
    }
  val projectsAddBtn: String
    get() = if (language == AppLanguage.AR) "إضافة مشروع" else "Nouveau projet"
  val projectsLoadSamplesBtn: String
    get() = if (language == AppLanguage.AR) "تحميل أمثلة مشاريع العمارة" else "Charger des projets types"
  val projectsKpiCaisseLabel: String
    get() = if (language == AppLanguage.AR) "السيولة المتوفرة بالصندوق" else "Avance disponible en caisse"
  val projectsKpiOngoingLabel: String
    get() = if (language == AppLanguage.AR) "قيد الإنجاز حالياً" else "En cours d'avancement"
  val projectsKpiReadyLabel: String
    get() = if (language == AppLanguage.AR) "مستقبلية جاهزة للبدء" else "Futurs prêts à débuter"
  val projectsKpiPlannedSub: (Int) -> String
    get() = { totalPlanned ->
      if (language == AppLanguage.AR) {
        "من أصل ${formatNumber(totalPlanned.toLong())} مبرمجة"
      } else {
        "sur $totalPlanned planifiés"
      }
    }

  val projectsFilterAll: (Int) -> String
    get() = { c -> if (language == AppLanguage.AR) "الكل (${formatNumber(c.toLong())})" else "Tous ($c)" }
  val projectsFilterOngoing: (Int) -> String
    get() = { c -> if (language == AppLanguage.AR) "قيد الإنجاز (${formatNumber(c.toLong())})" else "En cours ($c)" }
  val projectsFilterPlanned: (Int) -> String
    get() = { c -> if (language == AppLanguage.AR) "مستقبلية (${formatNumber(c.toLong())})" else "Futurs ($c)" }
  val projectsFilterCompleted: (Int) -> String
    get() = { c -> if (language == AppLanguage.AR) "منجزة (${formatNumber(c.toLong())})" else "Terminés ($c)" }

  val projectsOngoingSectionTitle: String
    get() = if (language == AppLanguage.AR) "المشروع الحالي · حالة التقدم" else "Projet en cours · État d'avancement"
  val projectsFutureSectionTitle: String
    get() = if (language == AppLanguage.AR) {
      "المشاريع المستقبلية حسب تسبيق الصندوق للبدء"
    } else {
      "Projets futurs selon l'avance de caisse pour débuter"
    }
  val projectsFutureSectionSub: (String) -> String
    get() = { monthlyExpected ->
      if (language == AppLanguage.AR) {
        "مرتبة تلقائياً حسب الأولوية مع حساب السيولة اللازمة لانطلاق كل مشروع (المدخول الشهري المتوقع: $monthlyExpected)."
      } else {
        "Classés par priorité avec calcul de l'avance en caisse requise pour démarrer (cotisations mensuelles attendues : $monthlyExpected)."
      }
    }
  val projectsCompletedSectionTitle: String
    get() = if (language == AppLanguage.AR) "المشاريع المنجزة" else "Projets terminés"

  fun projectPriorityLabel(priority: ProjectPriority): String {
    return if (language == AppLanguage.AR) {
      when (priority) {
        ProjectPriority.URGENT -> "P1 · أولوية عاجلة"
        ProjectPriority.HIGH -> "P2 · أولوية عالية"
        ProjectPriority.NORMAL -> "P3 · أولوية عادية"
        ProjectPriority.LOW -> "P4 · أولوية منخفضة"
      }
    } else {
      when (priority) {
        ProjectPriority.URGENT -> "P1 · Priorité urgente"
        ProjectPriority.HIGH -> "P2 · Priorité haute"
        ProjectPriority.NORMAL -> "P3 · Priorité normale"
        ProjectPriority.LOW -> "P4 · Priorité basse"
      }
    }
  }

  fun projectPriorityShort(priority: ProjectPriority): String {
    return if (language == AppLanguage.AR) {
      when (priority) {
        ProjectPriority.URGENT -> "P1 عاجل"
        ProjectPriority.HIGH -> "P2 عالي"
        ProjectPriority.NORMAL -> "P3 عادي"
        ProjectPriority.LOW -> "P4 منخفض"
      }
    } else {
      when (priority) {
        ProjectPriority.URGENT -> "P1 Urgent"
        ProjectPriority.HIGH -> "P2 Haute"
        ProjectPriority.NORMAL -> "P3 Normale"
        ProjectPriority.LOW -> "P4 Basse"
      }
    }
  }

  fun projectStatusLabel(status: ProjectStatus): String {
    return if (language == AppLanguage.AR) {
      when (status) {
        ProjectStatus.IN_PROGRESS -> "قيد الإنجاز"
        ProjectStatus.PLANNED -> "مشروع مستقبلي"
        ProjectStatus.COMPLETED -> "منجز"
      }
    } else {
      when (status) {
        ProjectStatus.IN_PROGRESS -> "En cours"
        ProjectStatus.PLANNED -> "Futur / Planifié"
        ProjectStatus.COMPLETED -> "Terminé"
      }
    }
  }

  fun projectCategoryLabel(category: String): String = categoryLabel(category)

  val projectProgressStateLabel: String
    get() = if (language == AppLanguage.AR) "نسبة التقدم والإنجاز" else "État d'avancement"
  val projectAllocatedVsBudget: (String, String) -> String
    get() = { paid, budget ->
      if (language == AppLanguage.AR) {
        "المؤدى للمقاول: $paid من أصل $budget"
      } else {
        "Engagé / Versé : $paid sur $budget"
      }
    }
  val projectRemainingBudget: (String) -> String
    get() = { rem ->
      if (language == AppLanguage.AR) "الباقي لإتمام الميزانية: $rem" else "Reste sur budget : $rem"
    }
  val projectPlus10Btn: String
    get() = if (language == AppLanguage.AR) "+10% تقدم" else "+10% avancement"
  val projectMarkDoneBtn: String
    get() = if (language == AppLanguage.AR) "إنهاء المشروع ✓" else "Terminer ✓"
  val projectEditBtn: String
    get() = if (language == AppLanguage.AR) "تعديل" else "Modifier"
  val projectReopenBtn: String
    get() = if (language == AppLanguage.AR) "إعادة لطور الإنجاز" else "Reprendre en cours"

  val projectTotalBudgetLabel: String
    get() = if (language == AppLanguage.AR) "الميزانية الإجمالية" else "Budget total"
  val projectRequiredAdvanceLabel: String
    get() = if (language == AppLanguage.AR) "تسبيق الصندوق للبدء" else "Avance caisse requise"
  val projectAvailableCaisseLabel: String
    get() = if (language == AppLanguage.AR) "المتوفر بالصندوق الآن" else "Caisse actuelle"
  val projectReadyDirectBadge: String
    get() = if (language == AppLanguage.AR) {
      "جاهز للانطلاق بتسبيق الصندوق الحالي"
    } else {
      "Prêt à débuter avec l'avance en caisse"
    }
  val projectMissingAdvanceBadge: (String) -> String
    get() = { missing ->
      if (language == AppLanguage.AR) {
        "ينقص $missing في الصندوق لبدء المشروع"
      } else {
        "Manque $missing en caisse pour débuter"
      }
    }
  val projectCaisseCoverageLabel: (String) -> String
    get() = { pct ->
      if (language == AppLanguage.AR) {
        "تغطية تسبيق البداية من الصندوق: $pct"
      } else {
        "Couverture de l'avance de démarrage : $pct"
      }
    }
  val projectWaterfallReadyNote: String
    get() = if (language == AppLanguage.AR) {
      "يبقى ممولاً حتى بعد خصم تسبيق المشاريع ذات الأولوية الأعلى."
    } else {
      "Finançable même après déduction des projets plus prioritaires."
    }
  val projectWaterfallWaitNote: (String) -> String
    get() = { shortfallAfter ->
      if (language == AppLanguage.AR) {
        "بعد تمويل المشاريع الأسبق في الأولوية، سيلزم جمع $shortfallAfter إضافية."
      } else {
        "Après lancement des priorités précédentes, il manquera $shortfallAfter."
      }
    }
  val projectEstMonthsNote: (Int) -> String
    get() = { months ->
      if (language == AppLanguage.AR) {
        "المدة التقديرية لجمع التسبيق: حوالي ${formatNumber(months.toLong())} أشهر من المساهمات"
      } else {
        "Délai estimé de collecte : env. $months mois de cotisations"
      }
    }
  val projectStartWorkBtn: String
    get() = if (language == AppLanguage.AR) "بدء الأشغال الآن" else "Débuter le projet"

  val projectDialogAddTitle: String
    get() = if (language == AppLanguage.AR) "إضافة مشروع جديد" else "Nouveau projet"
  val projectDialogEditTitle: String
    get() = if (language == AppLanguage.AR) "تعديل المشروع" else "Modifier le projet"
  val projectFieldTitleAr: String
    get() = if (language == AppLanguage.AR) "عنوان المشروع بالعربية" else "Titre du projet (Arabe)"
  val projectFieldTitleArHint: String
    get() = if (language == AppLanguage.AR) "مثال: صباغة مدخل العمارة" else "Ex : صباغة مدخل العمارة"
  val projectFieldTitleFr: String
    get() = if (language == AppLanguage.AR) "عنوان المشروع بالفرنسية" else "Titre du projet (Français)"
  val projectFieldTitleFrHint: String
    get() = if (language == AppLanguage.AR) "Ex: Peinture du hall" else "Ex : Peinture du hall d'entrée"
  val projectFieldPriority: String
    get() = if (language == AppLanguage.AR) "الأولوية" else "Priorité"
  val projectFieldStatus: String
    get() = if (language == AppLanguage.AR) "وضعية المشروع" else "Statut du projet"
  val projectFieldCategory: String
    get() = if (language == AppLanguage.AR) "صنف الأشغال" else "Catégorie"
  val projectFieldBudget: (String) -> String
    get() = { cur -> if (language == AppLanguage.AR) "الميزانية الإجمالية ($cur)" else "Budget total ($cur)" }
  val projectFieldMinAdvance: (String) -> String
    get() = { cur -> if (language == AppLanguage.AR) "تسبيق الصندوق للبدء ($cur)" else "Avance caisse pour débuter ($cur)" }
  val projectAdvancePresetsLabel: String
    get() = if (language == AppLanguage.AR) "حساب التسبيق تلقائياً من الميزانية:" else "Calcul rapide de l'avance :"
  val projectFieldAllocated: (String) -> String
    get() = { cur -> if (language == AppLanguage.AR) "المبلغ المخصص / المؤدى ($cur)" else "Montant déjà versé ($cur)" }
  val projectFieldProgress: String
    get() = if (language == AppLanguage.AR) "نسبة التقدم (%)" else "Avancement (%)"
  val projectFieldContractor: String
    get() = if (language == AppLanguage.AR) "المقاول / الشركة (اختياري)" else "Prestataire / Artisan"
  val projectFieldTargetDate: String
    get() = if (language == AppLanguage.AR) "تاريخ الإنجاز المستهدف" else "Échéance cible (JJ/MM/AAAA)"
  val projectFieldDescAr: String
    get() = if (language == AppLanguage.AR) "وصف ومراحل الأشغال (عربي)" else "Description / étapes (Arabe)"
  val projectFieldDescFr: String
    get() = if (language == AppLanguage.AR) "وصف ومراحل الأشغال (فرنسي)" else "Description / étapes (Français)"
  val projectErrTitle: String
    get() = if (language == AppLanguage.AR) {
      "يرجى إدخال عنوان المشروع بالعربية أو الفرنسية."
    } else {
      "Veuillez saisir un titre en arabe ou en français."
    }
  val projectErrBudget: String
    get() = if (language == AppLanguage.AR) "يرجى إدخال ميزانية صحيحة." else "Budget invalide."

  val projectsEmptyTitle: String
    get() = if (language == AppLanguage.AR) "لا توجد مشاريع مسجلة بعد" else "Aucun projet enregistré"
  val projectsEmptySub: String
    get() = if (language == AppLanguage.AR) {
      "رتّب مشاريع الإصلاح والتجهيز حسب الأولوية، وتابع نسبة تقدم الورش الحالي وجاهزية المشاريع المستقبلية حسب رصيد الصندوق."
    } else {
      "Planifiez vos travaux par ordre de priorité, suivez l'état d'avancement du chantier en cours et vérifiez quels futurs projets peuvent débuter avec la caisse."
    }

  val dashProjectsCardTitle: String
    get() = if (language == AppLanguage.AR) "مشاريع وأوراش العمارة" else "Projets & Travaux de l'immeuble"
  val dashProjectsOpenBtn: String
    get() = if (language == AppLanguage.AR) "صفحة المشاريع" else "Voir les projets"

  // Personalized Residence Hero Photo & Direct Monthly Cotisations Grid
  val residenceCustomizePhotoShort: String
    get() = if (language == AppLanguage.AR) "صورة الإقامة" else "Photo"
  val residenceCustomizePhotoLabel: String
    get() = if (language == AppLanguage.AR) "تخصيص صورة الإقامة" else "Personnaliser la photo de la résidence"
  val residenceResetPhotoLabel: String
    get() = if (language == AppLanguage.AR) "استعادة الصورة الافتراضية" else "Restaurer l'image par défaut"
  val residencePhotoSavedMsg: String
    get() = if (language == AppLanguage.AR) "تم تحديث صورة الإقامة بنجاح" else "Photo de la résidence enregistrée"
  val residencePhotoResetMsg: String
    get() = if (language == AppLanguage.AR) "تمت استعادة الصورة الافتراضية للإقامة" else "Image par défaut de la résidence restaurée"
  val apptMonthsSectionLabel: String
    get() = if (language == AppLanguage.AR) "الأشهر (اضغط على أي شهر للتسجيل المباشر)" else "Mois (touchez un mois pour saisir directement)"
  val apptClearMonthZeroBtn: String
    get() = if (language == AppLanguage.AR) "تصفير هذا الشهر (0)" else "Remettre ce mois à 0"

  // Recreated Fintech Dashboard labels
  val dashLiveTreasuryBadge: String
    get() = if (language == AppLanguage.AR) "خزينة السانديك المباشرة" else "Trésorerie en direct"
  val dashPaidOutLabel: String
    get() = if (language == AppLanguage.AR) "المؤدى للمزودين" else "Réglé prestataires"
  val dashQuickCotisations: String
    get() = if (language == AppLanguage.AR) "المساهمات" else "Cotisations"
  val dashQuickExpenses: String
    get() = if (language == AppLanguage.AR) "المصاريف" else "Dépenses"
  val dashQuickRemind: String
    get() = if (language == AppLanguage.AR) "تذكير واتساب" else "Relances"
  val dashQuickExports: String
    get() = if (language == AppLanguage.AR) "ملصقات PDF" else "Affiches PDF"
  val dashUnitsSummary: (Int, Int) -> String
    get() = { upToDate, occupied ->
      if (language == AppLanguage.AR) {
        "${bidiLtr("${formatNumber(upToDate.toLong())}/${formatNumber(occupied.toLong())}")} شقة في الوضعية السليمة"
      } else {
        "$upToDate/$occupied appartements à jour"
      }
    }

  // Separate Theme Choices (Meta, Coinbase, MiniMax)
  val themeChoiceMeta: String
    get() = "Meta"
  val themeChoiceCoinbase: String
    get() = "Coinbase"
  val themeChoiceMinimax: String
    get() = "MiniMax"

  fun themeChoiceSubtitle(styleKey: String): String {
    val family = com.example.ui.theme.resolveVisualThemeFamily(styleKey)
    return if (language == AppLanguage.AR) {
      when (family) {
        "meta" -> "Meta · أزرق كوبالت (#0064E0) مع خلفية سحابية ناعمة وحواف دائرية رحبة."
        "minimax" -> "MiniMax · مرجاني دافئ (#D93F26) على ورق عاجي وهندسة عصرية حادة."
        else -> "Coinbase · أزرق مؤسسي نقي (#0052FF) مع أسطح بيضاء هادئة وتباين واضح."
      }
    } else {
      when (family) {
        "meta" -> "Meta · Bleu cobalt (#0064E0), fond nuage doux et cartes généreusement arrondies."
        "minimax" -> "MiniMax · Corail vermillon (#D93F26), papier albâtre chaud et géométrie nette."
        else -> "Coinbase · Bleu institutionnel (#0052FF), surfaces blanches épurées et fort contraste."
      }
    }
  }

  // Local Phone Storage & Optional Cloud Synchronization
  val syncSectionTitle: String
    get() = if (language == AppLanguage.AR) "التخزين على الهاتف والمزامنة الاختيارية" else "Stockage local & Synchronisation optionnelle"
  val syncLocalBadge: String
    get() = if (language == AppLanguage.AR) "تخزين محلي · بدون حساب" else "Stockage local · Sans compte"
  val syncLocalDesc: String
    get() = if (language == AppLanguage.AR) {
      "التطبيق يشتغل بالكامل بدون حساب، وجميع بيانات الإقامة محفوظة محلياً على هاتفك. يمكنك ربط حساب Google اختيارياً لتفعيل المزامنة السحابية."
    } else {
      "L'application fonctionne intégralement sans compte avec stockage local sur ce téléphone. Connectez Google uniquement si vous souhaitez synchroniser vos données dans le cloud."
    }
  val syncCloudBadge: String
    get() = if (language == AppLanguage.AR) "مزامنة سحابية مفعّلة" else "Synchronisation cloud active"
  val syncCloudDesc: String
    get() = if (language == AppLanguage.AR) {
      "يتم حفظ نسخة محلية تلقائية على الهاتف مع مزامنة فورية عبر حساب Google الخاص بك."
    } else {
      "Vos données sont sauvegardées sur le téléphone et synchronisées en temps réel avec votre compte Google."
    }
  val syncConnectGoogleBtn: String
    get() = if (language == AppLanguage.AR) "تفعيل المزامنة عبر Google (اختياري)" else "Activer la synchronisation Google (optionnel)"
  val syncPushLocalBtn: String
    get() = if (language == AppLanguage.AR) "إرسال بيانات الهاتف المحلية إلى السحابة" else "Envoyer les données locales vers le cloud"
  val syncLocalMigratedToast: String
    get() = if (language == AppLanguage.AR) "تمت مزامنة بيانات الهاتف المحلية مع حسابك السحابي." else "Données locales synchronisées vers votre compte cloud."
  val signOutToLocal: String
    get() = if (language == AppLanguage.AR) "إيقاف المزامنة والمتابعة محلياً بدون حساب" else "Se déconnecter (continuer en local sans compte)"
  val authContinueWithoutAccount: String
    get() = if (language == AppLanguage.AR) "المتابعة بدون حساب (تخزين محلي)" else "Continuer sans compte (stockage local)"

  // Multi-Year Ledger, Payment History & Amount Coherence
  val expErrPaidExceedsAmount: String
    get() = if (language == AppLanguage.AR) {
      "المبلغ المؤدى لا يمكن أن يتجاوز المبلغ الإجمالي."
    } else {
      "Le montant réglé ne peut pas dépasser le montant total."
    }

  val yearSelectorLabel: String
    get() = if (language == AppLanguage.AR) "السنة المالية" else "Exercice"
  val yearAddNextBtn: String
    get() = if (language == AppLanguage.AR) "+ سنة" else "+ Année"

  val paymentMethodFieldLabel: String
    get() = if (language == AppLanguage.AR) "طريقة الأداء" else "Mode de paiement"
  val paymentDateFieldLabel: String
    get() = if (language == AppLanguage.AR) "تاريخ الأداء" else "Date du paiement"

  fun paymentMethodLabel(methodCode: String): String {
    return if (language == AppLanguage.AR) {
      when (methodCode.trim().lowercase()) {
        "transfer", "virement" -> "تحويل بنكي"
        "check", "cheque", "chèque" -> "شيك"
        "mobile" -> "أداء هاتفي"
        "other", "autre" -> "أخرى"
        else -> "نقداً"
      }
    } else {
      when (methodCode.trim().lowercase()) {
        "transfer", "virement" -> "Virement"
        "check", "cheque", "chèque" -> "Chèque"
        "mobile" -> "Mobile"
        "other", "autre" -> "Autre"
        else -> "Espèces"
      }
    }
  }

  val paymentHistoryTitle: String
    get() = if (language == AppLanguage.AR) "سجل الأداءات والوصولات" else "Historique des paiements & reçus"
  val paymentHistoryEmpty: String
    get() = if (language == AppLanguage.AR) {
      "لا توجد دفعات مؤرخة مسجلة لهذه الشقة بعد."
    } else {
      "Aucun versement horodaté enregistré pour cet appartement."
    }
  val paymentReceiptShareDesc: String
    get() = if (language == AppLanguage.AR) "مشاركة الوصل" else "Partager le reçu"
  val paymentDeleteDesc: String
    get() = if (language == AppLanguage.AR) "حذف هذه الدفعة" else "Supprimer ce paiement"
  val receiptMethodRowLabel: String
    get() = if (language == AppLanguage.AR) "طريقة الأداء" else "Mode de règlement"
}

val ArabicStrings = SyndicatStrings(
  language = AppLanguage.AR,
  navDashboard = "لوحة القيادة",
  navApartments = "المساهمات",
  navExpenses = "المصاريف",
  navPoster = "الملصقات",
  navSettings = "الإعدادات",
  loadingSync = "جاري تحميل بيانات السانديك…",
  undoAction = "تراجع",
  authBadge = "إدارة السانديك · عربي RTL",
  authTitle = "إدارة السانديك",
  authSubtitle = "سجّل الدخول بحساب Google للوصول إلى صندوق الإقامة والمساهمات والمشاريع.",
  authFeat1Title = "تخصيص سريع لأي عمارة",
  authFeat1Sub = "مساعد إعداد من 6 مراحل للطوابق، الشقق، الملاك، المساهمات والأشهر المخصصة.",
  authFeat2Title = "تذكير واتساب بضغطة واحدة",
  authFeat2Sub = "رسالة عربية تلقائية باسم المالك والمبلغ المتبقي مع تسجيل تاريخ التذكير.",
  authFeat3Title = "3 ملصقات رسمية ووصل أداء",
  authFeat3Sub = "ملصق المساهمات، ملصق التذكير ووضعية الصندوق بصيغة PDF أو صورة مع نسخة احتياطية.",
  authSignInGoogle = "تسجيل الدخول باستخدام Google",
  authConnectingGoogle = "جاري الاتصال بحساب Google…",
  authCancelBtn = "إلغاء المحاولة",
  authCancelledHint = "تم إغلاق نافذة Google قبل اختيار الحساب. تأكد من ربط حساب Google في إعدادات الهاتف أو المحاكي ثم أعد المحاولة.",
  authNoAccountHint = "لم يتم العثور على أي حساب Google مفعل على هذا الجهاز. أضف حساب Google من إعدادات النظام (الحسابات / Play Store) ثم اضغط للدخول.",
  authTimeoutHint = "استغرق الاتصال بـ Google وقتاً أطول من المعتاد. تحقق من الاتصال بالإنترنت وحاول مجدداً.",
  authSecurityNote = "تشتغل البيانات دون إنترنت مع حفظ تلقائي ونسخة احتياطية JSON.",
  badgePaid = "مؤدّى",
  badgePartial = "جزئي",
  badgeLate = "متأخر",
  badgeEmpty = "فارغة",
  badgeExempt = "معفاة",
  badgeTenant = "كراء",
  badgeReminded = "تم التذكير",
  dashCashEyebrow = "الصندوق",
  dashCashNowLabel = "المتوفر في الصندوق الآن",
  dashOpeningBalanceNote = { amt -> "يشمل رصيداً افتتاحياً قدره $amt" },
  dashCollectedLabel = "المداخيل المحصلة",
  dashExpensesLabel = "المصاريف",
  dashNetBalanceLabel = "الرصيد الصافي",
  dashCollectionRateLabel = "نسبة التحصيل",
  dashUnpaidTitle = "المتبقي للتحصيل",
  dashUnpaidSub = { count -> "${formatNumber(count.toLong())} شقق متأخرة عن الأداء" },
  dashSendRemindersBtn = "إرسال التذكيرات",
  dashViewLateBtn = "عرض المتأخرين",
  dashAllPaidTitle = "كل الشقق مؤدّية",
  dashAllPaidSub = "لا توجد أي متأخرات مستحقة إلى غاية الشهر المرجعي.",
  dashBackupReminder = "لم تقم بنسخة احتياطية خارجية منذ 7 أيام",
  dashBackupNowBtn = "حفظ نسخة احتياطية",
  dashCreamTip = "نصيحة: شارك ملصق المساهمات أو التذكير بصيغة PDF أو صورة بضغطة واحدة مع السكان.",
  dashLaunchWizardBtn = "مساعد إعداد العمارة (6 مراحل)",
  dashRelanceQueueTitle = "قائمة التذكير عبر واتساب",
  dashRemindBtn = "تذكير",
  dashExportShortcutsTitle = "تصدير الملصقات والبيانات",
  dashMonthlySection = { year -> "تحصيل الأشهر لسنة ${bidiLtr(year.toString())}" },
  dashStandardFeeLegend = { fee, cur -> "عادي: ${formatNumber(fee)} $cur" },
  dashCustomFeeLegend = { cur -> "مخصص ($cur)" },
  dashFloorsSection = "نسبة التحصيل حسب الطوابق",
  dashFloorRowTitle = { floorLabel, occ, total -> "$floorLabel (${bidiLtr("$occ/$total")} مأهولة)" },
  apptSearchHint = "بحث برقم الشقة أو الاسم بالعربية أو اللاتينية…",
  apptClearSearch = "مسح البحث",
  apptFilterAll = { count -> "الكل (${formatNumber(count.toLong())})" },
  apptViewCards = "بطاقات",
  apptViewGrid = "جدول 12 شهراً",
  apptSubtotal = "مجموع الطابق:",
  apptRecordPayment = "تسجيل دفعة",
  apptPayFullBtn = "دفع كامل",
  apptReceiptBtn = "وصل",
  apptDialogTitle = { num, floorLabel -> "الشقة $num · $floorLabel" },
  apptCalculatedTotal = "مجموع المؤدى:",
  apptOwnerArLabel = "الاسم بالعربية (أساسي)",
  apptOwnerArPlaceholder = "مثال: عبد الفتاح",
  apptOwnerLatinLabel = "الاسم باللاتينية (اختياري للبحث)",
  apptOwnerLatinPlaceholder = "Ex: Abdelfetah",
  apptPhoneLabel = "رقم الهاتف (واتساب)",
  apptPhonePlaceholder = "0612345678",
  apptPickContactBtn = "استيراد من جهات الاتصال",
  apptUnitStatusLabel = "وضعية الشقة",
  apptIsTenantLabel = "مكتري (كراء)",
  apptCustomFeeLabel = { cur -> "واجب شهري خاص بهذه الشقة ($cur)" },
  apptLumpSumTitle = "تسجيل دفعة متعددة الأشهر / تسبيق",
  apptLumpSumHint = { cur -> "المبلغ المدفوع ($cur)" },
  apptDistributeBtn = "توزيع تلقائي",
  apptMonthlyFeesHeader = { cur -> "تفصيل الأشهر ($cur)" },
  apptSettleAllActive = "أداء كل الأشهر",
  apptTariffPrefix = "الواجب",
  apptSave = "حفظ",
  apptCancel = "إلغاء",
  apptAdvancePrefix = "تسبيق",
  colFloor = "الطابق",
  colAppt = "الشقة",
  colOwner = "المالك",
  colTotal = "المجموع",
  colMissingMonths = "الأشهر المتبقية",
  colPaid = "المؤدى",
  colRemaining = "الباقي",
  rowCaisse = "الصندوق",
  datePrefix = "التاريخ:",
  tabExpensesMode = "المصاريف",
  tabCaisseMode = "الصندوق",
  expAddFab = "إضافة مصروف",
  expFilterAll = { count -> "الكل (${formatNumber(count.toLong())})" },
  expEmptyTitle = "لا توجد مصاريف مسجلة",
  expEmptyTip = "أضف مصاريف النظافة والكهرباء والصيانة لمتابعة الصندوق تلقائياً.",
  expDialogAdd = "إضافة مصروف جديد",
  expDialogEdit = "تعديل المصروف",
  expFieldTitle = "بيان المصروف",
  expFieldTitleHint = "مثال: صيانة المصعد أو فاتورة الكهرباء",
  expFieldCategory = "الصنف",
  expFieldSupplier = "المزود / المقاول (اختياري)",
  expFieldSupplierHint = "مثال: شركة النظافة",
  expFieldAmount = { cur -> "المبلغ الإجمالي ($cur)" },
  expFieldPaidAmount = { cur -> "المبلغ المؤدى فعلياً ($cur)" },
  expFieldDate = "التاريخ (يوم/شهر/سنة)",
  expFieldNotes = "ملاحظات إضافية",
  expDelete = "حذف",
  expDeleteConfirmTap = "اضغط مرة ثانية لتأكيد الحذف",
  expPaidProgressLabel = { paid, total -> "مؤدّى: $paid من أصل $total" },
  expSupplierRemaining = { rem -> "الباقي للمزود: $rem" },
  expErrTitle = "يرجى إدخال بيان المصروف.",
  expErrAmount = "يرجى إدخال مبلغ صحيح.",
  expErrDate = "يرجى تحديد التاريخ.",
  caisseAvailableTitle = "السيولة المتوفرة في الصندوق الآن",
  caisseOpeningBalance = "الرصيد الافتتاحي المرحل",
  caisseTotalReceipts = "مجموع المساهمات المحصلة",
  caisseTotalExpenses = "إجمالي المصاريف المسجلة",
  caissePaidToSuppliers = "المصاريف المؤداة فعلياً",
  caisseUnpaidSuppliers = "الباقي أداؤه للمزودين",
  caisseAccountingBalance = "الرصيد المحاسبي (المداخيل − المصاريف)",
  caisseMovementsTitle = "سجل حركات الصندوق والمداخيل الشهرية",
  posterPageTitle = "الملصقات والتقارير الجاهزة",
  posterPageSub = "ثلاث وثائق بتصميم عمودي (A4)، واضحة القراءة على شاشة الهاتف وجاهزة للطباعة أو المشاركة عبر واتساب.",
  exportVariantLabel = "نسخة الملصق",
  exportVariantPrint = "للطباعة",
  exportVariantScreen = "للشاشة",
  exportPdfPrimaryBtn = "تصدير PDF",
  exportPngSecondaryBtn = "صورة",
  exportCard1Title = "1. ملصق المساهمات الشهرية",
  exportCard1Sub = "جدول جميع الطوابق والشقق والأشهر مع خطوط الفصل والمجموع العام للصندوق.",
  exportOptShowNames = "إظهار أسماء الملاك",
  exportOptUntilCutoffOnly = "إظهار الأشهر حتى الشهر المرجعي فقط",
  exportOptInkSaver = "وضع اقتصاد الحبر (بدون خلفيات داكنة)",
  exportCard2Title = "2. ملصق التذكير بالأداء",
  exportCard2Sub = "لائحة الشقق المتأخرة مع المبلغ المتبقي والأشهر المعنية (بأسماء أو بأرقام الشقق فقط).",
  exportOptAnonymousMode = "وضع بدون أسماء (أرقام الشقق فقط)",
  exportCard3Title = "3. وضعية المصاريف وحالة الصندوق",
  exportCard3Sub = "تقرير مالي شامل للمداخيل، المصاريف، الباقي للمزودين والرصيد المتوفر للجمع العام.",
  posterShareWhatsappText = "مشاركة ملخص نصي عبر واتساب",
  posterLegendSpecial = "شهر بواجب مخصص",
  posterLegendStandard = "واجب شهري عادي",
  relanceDocTitle = { res -> "ملصق التذكير بالمساهمات – $res" },
  relanceDocSubtitle = { date, startM, endM, year -> "الوضعية المحينة بتاريخ ${bidiLtr(date)} ($startM – $endM ${bidiLtr(year.toString())})" },
  relanceBannerSummary = { count, rem -> "${formatNumber(count.toLong())} شقق معنية · المجموع المتبقي للتحصيل: $rem" },
  relanceTotalRemaining = "المجموع المتبقي للتحصيل",
  caisseDocTitle = { res, year -> "وضعية المصاريف وحالة الصندوق ${bidiLtr(year.toString())} – $res" },
  caisseIssuedOn = { date -> "حرر بتاريخ ${bidiLtr(date)}" },
  receiptDocTitle = "وصل أداء المساهمة الشهرية",
  wizardStepIndicator = { step, total -> "الخطوة ${bidiLtr(step.toString())} من ${bidiLtr(total.toString())}" },
  wizardNextBtn = "التالي",
  wizardBackBtn = "رجوع",
  wizardStartBtn = "ابدأ",
  wizardStep1Title = "معلومات العمارة",
  wizardStep1Sub = "حدد اسم الإقامة، المدينة واسم أمين المال أو السانديك.",
  wizardStep2Title = "الطوابق والشقق",
  wizardStep2Sub = "اختر عدد الطوابق والشقق (عدد موحد أو مختلف لكل طابق) وحالة كل شقة.",
  wizardSimpleMode = "عدد موحد لكل طابق",
  wizardAdvancedMode = "عدد مختلف حسب الطابق",
  wizardApptsPerFloor = "عدد الشقق في كل طابق",
  wizardStep3Title = "أسماء الملاك وأرقام واتساب",
  wizardStep3Sub = "أدخل الاسم بالعربية ورقم الهاتف للتذكير أو استورد من جهات الاتصال.",
  wizardBulkImportBtn = "إدخال سريع في لائحة",
  wizardStep4Title = "المساهمة الشهرية",
  wizardStep4Sub = "حدد واجب المساهمة العادي وخصص أو عطّل أشهراً محددة.",
  wizardStep5Title = "الدفعات السابقة ورصيد الصندوق",
  wizardStep5Sub = "أدخل الرصيد الافتتاحي للصندوق واختر طريقة بدء الدفعات.",
  wizardEnterPastPayments = "الاحتفاظ بالدفعات المسجلة / تعديلها",
  wizardStartFromZero = "ابدأ من الصفر (تصفير الدفعات)",
  wizardStep6Title = "ملخص الإعداد",
  wizardStep6Sub = "تحقق من معطيات العمارة قبل الانطلاق إلى لوحة القيادة.",
  settingsPageTitle = "الإعدادات",
  settingsPageSubtitle = "تخصيص العمارة، الطوابق، المساهمات، رسائل واتساب، النسخ الاحتياطي والأمان.",
  settingsOpenWizardCardTitle = "مساعد الإعداد خطوة بخطوة (6 مراحل)",
  settingsOpenWizardCardSub = "أعد ضبط العمارة والطوابق والملاك والمساهمات بطريقة مبسطة.",
  settingsLangDisplayTitle = "اللغة والعرض",
  settingsDigitsArLabel = "استخدام الأرقام العربية الشرقية (٠-٩)",
  settingsStyleLabel = "النمط",
  settingsStyleAirtable = "ميتا العصري",
  settingsStyleXai = "الداكن الأنيق",
  settingsStyleWise = "الأخضر الهادئ",
  settingsStyleDarkOnlyNote = "نمط «الداكن الأنيق» يعمل بالوضع الداكن فقط (الأبيض مخصص للطباعة).",
  settingsThemeLabel = "المظهر",
  settingsThemeSystem = "تلقائي",
  settingsThemeLight = "فاتح",
  settingsThemeDark = "داكن",
  settingsFontScaleLabel = "حجم الخط",
  settingsFontSmall = "صغير",
  settingsFontNormal = "عادي",
  settingsFontLarge = "كبير",
  settingsIdentityTitle = "بيانات العمارة والسانديك",
  settingsSyndicatName = "اسم المكتب / السانديك",
  settingsResidenceName = "اسم العمارة / الإقامة",
  settingsCity = "المدينة",
  settingsAddress = "العنوان / الحي",
  settingsTreasurer = "اسم السانديك / أمين المال",
  settingsBuildingTitle = "عنوان الملصق الرسمي",
  settingsAutoTitleBtn = "توليد تلقائي",
  settingsYear = "سنة التسيير",
  settingsCurrency = "العملة",
  settingsOpeningBalance = { cur -> "الرصيد الافتتاحي للصندوق ($cur)" },
  settingsFooterNote = "ملاحظة أسفل الملصق",
  settingsStructureTitle = "الطوابق والشقق",
  settingsIncludeRdc = "يوجد طابق أرضي",
  settingsFloorsCount = "عدد الطوابق / المستويات",
  settingsApartmentsCount = "إجمالي عدد الشقق",
  settingsFeesTitle = "المساهمات والأشهر المخصصة",
  settingsDefaultFee = { cur -> "المساهمة الشهرية الافتراضية ($cur)" },
  settingsApplyDefaultToAll = "تعميم على الكل",
  settingsDueDayLabel = "آخر أجل للأداء في الشهر (اليوم)",
  settingsCutoffMonthLabel = "الشهر المرجعي لحساب المتأخرات",
  settingsActiveMonthsLabel = "جدول 12 شهراً (تفعيل وتخصيص المبلغ)",
  settingsPresetFullYear = "كل السنة (12 شهراً)",
  settingsPresetJunDec = "يونيو – دجنبر (7 أشهر)",
  settingsWhatsappTitle = "تذكيرات واتساب التلقائية",
  settingsWhatsappTemplateLabel = "نموذج رسالة التذكير",
  settingsWhatsappVarsHint = "اضغط لإدراج متغير تلقائي في نص الرسالة:",
  settingsMinReminderLabel = { cur -> "الحد الأدنى للتذكير ($cur)" },
  settingsRelanceHeaderLabel = "عبارة أعلى ملصق التذكير",
  settingsBackupSecurityTitle = "النسخ الاحتياطي والأمان",
  settingsExportJsonBtn = "حفظ نسخة احتياطية (.json)",
  settingsImportJsonBtn = "استرجاع من ملف (.json)",
  settingsImportCsvBtn = "استيراد الملاك (CSV / نص)",
  settingsPinTitle = "قفل التطبيق برمز سري (PIN)",
  settingsSetPinBtn = "تفعيل / تغيير رمز PIN",
  settingsDisablePinBtn = "إلغاء القفل",
  settingsSaveButton = "حفظ الإعدادات",
  settingsNewBlankBuildingBtn = "تهيئة عمارة فارغة من الصفر",
  settingsResetC49Btn = "استرجاع بيانات C49 النموذجية",
  settingsConfirmBtn = "تأكيد",
  settingsConnectedAs = { email -> "متصل بحساب: $email" },
  signOut = "تسجيل الخروج",
  pinUnlockTitle = "أدخل الرمز السري",
  pinUnlockSub = "التطبيق محمي برمز PIN خاص بالسانديك",
  pinWrongError = "الرمز السري غير صحيح",
)

val FrenchStrings = SyndicatStrings(
  language = AppLanguage.FR,
  navDashboard = "Tableau de bord",
  navApartments = "Cotisations",
  navExpenses = "Dépenses",
  navPoster = "Affiches",
  navSettings = "Réglages",
  loadingSync = "Synchronisation du syndicat…",
  undoAction = "Annuler",
  authBadge = "GESTION DU SYNDIC · AR / FR",
  authTitle = "Gestion du Syndic",
  authSubtitle = "Connectez-vous avec Google pour accéder à la caisse, aux cotisations et aux projets.",
  authFeat1Title = "Assistant d'installation en 6 étapes",
  authFeat1Sub = "Configurez n'importe quel immeuble (étages uniformes ou variables, propriétaires, tarifs).",
  authFeat2Title = "Relances WhatsApp en 1 clic",
  authFeat2Sub = "Message personnalisé avec nom, montant restant et suivi des relances envoyées.",
  authFeat3Title = "3 Affiches PDF/PNG & Reçus",
  authFeat3Sub = "Affiche de cotisation, affiche de relance (nominative/anonyme) et état de caisse.",
  authSignInGoogle = "Se connecter avec Google",
  authConnectingGoogle = "Connexion au compte Google en cours…",
  authCancelBtn = "Annuler la tentative",
  authCancelledHint = "La sélection Google a été fermée. Vérifiez qu'un compte Google est bien connecté sur l'appareil ou l'émulateur, puis réessayez.",
  authNoAccountHint = "Aucun compte Google détecté sur cet appareil. Ajoutez un compte Google dans les Paramètres Android / Play Store puis réessayez.",
  authTimeoutHint = "La connexion Google a dépassé le délai d'attente. Vérifiez votre connexion réseau et réessayez.",
  authSecurityNote = "Fonctionne hors ligne avec cache local et sauvegarde JSON.",
  badgePaid = "Payé",
  badgePartial = "Partiel",
  badgeLate = "En retard",
  badgeEmpty = "Vide",
  badgeExempt = "Exonéré",
  badgeTenant = "Locataire",
  badgeReminded = "Relancé",
  dashCashEyebrow = "CAISSE",
  dashCashNowLabel = "Disponible en caisse maintenant",
  dashOpeningBalanceNote = { amt -> "Inclut un report initial de $amt" },
  dashCollectedLabel = "Encaissé",
  dashExpensesLabel = "Dépenses",
  dashNetBalanceLabel = "Solde net",
  dashCollectionRateLabel = "Recouvrement",
  dashUnpaidTitle = "Reste à recouvrer",
  dashUnpaidSub = { count -> "${formatNumber(count.toLong())} appartements en retard" },
  dashSendRemindersBtn = "Envoyer les relances",
  dashViewLateBtn = "Voir les retardataires",
  dashAllPaidTitle = "Tous les appartements sont à jour",
  dashAllPaidSub = "Aucun impayé constaté jusqu'au mois de référence.",
  dashBackupReminder = "Aucune sauvegarde JSON externe depuis 7 jours",
  dashBackupNowBtn = "Sauvegarder",
  dashCreamTip = "Astuce : partagez l'affiche de cotisation ou de relance en PDF ou image en un clic.",
  dashLaunchWizardBtn = "Assistant d'installation (6 étapes)",
  dashRelanceQueueTitle = "File de relance WhatsApp",
  dashRemindBtn = "Relancer",
  dashExportShortcutsTitle = "Exports & Affiches prêts à partager",
  dashMonthlySection = { year -> "Encaissements mensuels $year" },
  dashStandardFeeLegend = { fee, cur -> "Standard : ${formatNumber(fee)} $cur" },
  dashCustomFeeLegend = { cur -> "Spécial ($cur)" },
  dashFloorsSection = "Recouvrement par étage",
  dashFloorRowTitle = { floorLabel, occ, total -> "$floorLabel ($occ/$total occupés)" },
  apptSearchHint = "Rechercher n°, nom arabe ou latin…",
  apptClearSearch = "Effacer",
  apptFilterAll = { count -> "Tous (${formatNumber(count.toLong())})" },
  apptViewCards = "Cartes",
  apptViewGrid = "Grille 12 mois",
  apptSubtotal = "Sous-total :",
  apptRecordPayment = "Saisir paiement",
  apptPayFullBtn = "Tout régler",
  apptReceiptBtn = "Reçu",
  apptDialogTitle = { num, floorLabel -> "Appt $num · $floorLabel" },
  apptCalculatedTotal = "Total payé :",
  apptOwnerArLabel = "Nom en arabe (affichage & WhatsApp)",
  apptOwnerArPlaceholder = "مثال: عبد الفتاح",
  apptOwnerLatinLabel = "Nom latin (optionnel)",
  apptOwnerLatinPlaceholder = "Ex: Abdelfetah",
  apptPhoneLabel = "Téléphone WhatsApp",
  apptPhonePlaceholder = "0612345678",
  apptPickContactBtn = "Importer un contact",
  apptUnitStatusLabel = "Statut de l'appartement",
  apptIsTenantLabel = "Locataire",
  apptCustomFeeLabel = { cur -> "Tarif mensuel spécifique ($cur)" },
  apptLumpSumTitle = "Paiement global multi-mois / avance",
  apptLumpSumHint = { cur -> "Montant versé ($cur)" },
  apptDistributeBtn = "Répartir auto",
  apptMonthlyFeesHeader = { cur -> "Détail des 12 mois ($cur)" },
  apptSettleAllActive = "Régler toute l'année",
  apptTariffPrefix = "Tarif",
  apptSave = "Enregistrer",
  apptCancel = "Annuler",
  apptAdvancePrefix = "Avance",
  colFloor = "Étage",
  colAppt = "Appt",
  colOwner = "Propriétaire",
  colTotal = "Total",
  colMissingMonths = "Mois dus",
  colPaid = "Payé",
  colRemaining = "Reste",
  rowCaisse = "Caisse",
  datePrefix = "Date :",
  tabExpensesMode = "Dépenses",
  tabCaisseMode = "Caisse",
  expAddFab = "Ajouter dépense",
  expFilterAll = { count -> "Toutes ($count)" },
  expEmptyTitle = "Aucune dépense enregistrée",
  expEmptyTip = "Ajoutez vos dépenses d'immeuble pour suivre la caisse disponible.",
  expDialogAdd = "Nouvelle dépense",
  expDialogEdit = "Modifier la dépense",
  expFieldTitle = "Libellé de la dépense",
  expFieldTitleHint = "Ex: Réparation ascenseur",
  expFieldCategory = "Catégorie",
  expFieldSupplier = "Prestataire / Fournisseur",
  expFieldSupplierHint = "Ex: Société nettoyage",
  expFieldAmount = { cur -> "Montant total ($cur)" },
  expFieldPaidAmount = { cur -> "Montant réglé ($cur)" },
  expFieldDate = "Date (JJ/MM/AAAA)",
  expFieldNotes = "Notes",
  expDelete = "Supprimer",
  expDeleteConfirmTap = "Appuyez une 2e fois pour confirmer",
  expPaidProgressLabel = { paid, total -> "Payé : $paid sur $total" },
  expSupplierRemaining = { rem -> "Reste prestataire : $rem" },
  expErrTitle = "Veuillez saisir un libellé.",
  expErrAmount = "Montant invalide.",
  expErrDate = "Date requise.",
  caisseAvailableTitle = "En caisse maintenant",
  caisseOpeningBalance = "Solde initial reporté",
  caisseTotalReceipts = "Total des cotisations encaissées",
  caisseTotalExpenses = "Total des dépenses engagées",
  caissePaidToSuppliers = "Dépenses effectivement payées",
  caisseUnpaidSuppliers = "Reste à payer aux prestataires",
  caisseAccountingBalance = "Solde comptable (Recettes − Dépenses)",
  caisseMovementsTitle = "Mouvements & Encaissements mensuels",
  posterPageTitle = "Affiches & Exports officiels",
  posterPageSub = "Trois documents au format portrait (A4 vertical), nets sur écran de téléphone et prêts à imprimer ou partager sur WhatsApp.",
  exportVariantLabel = "Variante d'export",
  exportVariantPrint = "Impression",
  exportVariantScreen = "Écran",
  exportPdfPrimaryBtn = "Exporter PDF",
  exportPngSecondaryBtn = "Image PNG",
  exportCard1Title = "1. Affiche de cotisation",
  exportCard1Sub = "Tableau complet par étage et par mois avec total encaissé en caisse.",
  exportOptShowNames = "Afficher les noms des propriétaires",
  exportOptUntilCutoffOnly = "Afficher uniquement jusqu'au mois de référence",
  exportOptInkSaver = "Mode économie d'encre (sans aplats sombres)",
  exportCard2Title = "2. Affiche de relance",
  exportCard2Sub = "Liste des retardataires avec montant restant et mois concernés.",
  exportOptAnonymousMode = "Mode anonyme (numéros d'appartement uniquement)",
  exportCard3Title = "3. Situation des dépenses & État de caisse",
  exportCard3Sub = "Recettes, détail des dépenses (payé/reste), état de caisse et bilan par catégorie.",
  posterShareWhatsappText = "Partager un résumé texte sur WhatsApp",
  posterLegendSpecial = "Mois à tarif spécial",
  posterLegendStandard = "Tarif standard",
  relanceDocTitle = { res -> "Affiche de relance – $res" },
  relanceDocSubtitle = { date, startM, endM, year -> "Situation au $date ($startM – $endM $year)" },
  relanceBannerSummary = { count, rem -> "$count appartements en retard · Reste à recouvrer : $rem" },
  relanceTotalRemaining = "Total restant à recouvrer",
  caisseDocTitle = { res, year -> "Situation des dépenses & État de caisse $year – $res" },
  caisseIssuedOn = { date -> "Fait le $date" },
  receiptDocTitle = "Reçu de paiement de cotisation",
  wizardStepIndicator = { step, total -> "Étape $step sur $total" },
  wizardNextBtn = "Suivant",
  wizardBackBtn = "Retour",
  wizardStartBtn = "Démarrer",
  wizardStep1Title = "L'immeuble",
  wizardStep1Sub = "Nom de la résidence, ville et nom du syndic/trésorier.",
  wizardStep2Title = "Étages et appartements",
  wizardStep2Sub = "Nombre d'étages (avec/sans RDC), mode uniforme ou par étage, et statut des lots.",
  wizardSimpleMode = "Même nombre par étage",
  wizardAdvancedMode = "Nombre différent par étage",
  wizardApptsPerFloor = "Appartements par étage",
  wizardStep3Title = "Les propriétaires",
  wizardStep3Sub = "Nom en arabe, nom latin et numéro WhatsApp (avec import contacts ou liste).",
  wizardBulkImportBtn = "Saisie rapide en liste",
  wizardStep4Title = "La cotisation",
  wizardStep4Sub = "Montant par défaut et personnalisation des 12 mois.",
  wizardStep5Title = "Historique & Solde initial",
  wizardStep5Sub = "Solde initial de caisse et reprise des paiements précédents.",
  wizardEnterPastPayments = "Conserver / saisir les paiements passés",
  wizardStartFromZero = "Partir de zéro (vider les paiements)",
  wizardStep6Title = "Récapitulatif",
  wizardStep6Sub = "Vérifiez la configuration avant d'accéder au tableau de bord.",
  settingsPageTitle = "Réglages",
  settingsPageSubtitle = "Immeuble, étages, cotisations, modèle WhatsApp, sauvegarde JSON et code PIN.",
  settingsOpenWizardCardTitle = "Assistant d'installation (6 étapes)",
  settingsOpenWizardCardSub = "Reconfigurer l'immeuble étape par étape.",
  settingsLangDisplayTitle = "Langue & Affichage",
  settingsDigitsArLabel = "Chiffres arabes orientaux (٠-٩)",
  settingsStyleLabel = "Style",
  settingsStyleAirtable = "Meta Moderne",
  settingsStyleXai = "Sombre élégant",
  settingsStyleWise = "Citron calme",
  settingsStyleDarkOnlyNote = "Le style « Sombre élégant » est uniquement sombre (blanc réservé à l'impression).",
  settingsThemeLabel = "Thème",
  settingsThemeSystem = "Système",
  settingsThemeLight = "Clair",
  settingsThemeDark = "Sombre",
  settingsFontScaleLabel = "Taille du texte",
  settingsFontSmall = "Petite",
  settingsFontNormal = "Normale",
  settingsFontLarge = "Grande",
  settingsIdentityTitle = "Immeuble & Syndic",
  settingsSyndicatName = "Nom du syndicat",
  settingsResidenceName = "Nom de l'immeuble / résidence",
  settingsCity = "Ville",
  settingsAddress = "Adresse / Quartier",
  settingsTreasurer = "Nom du syndic / trésorier",
  settingsBuildingTitle = "Titre de l'affiche",
  settingsAutoTitleBtn = "Titre auto",
  settingsYear = "Année de gestion",
  settingsCurrency = "Devise",
  settingsOpeningBalance = { cur -> "Solde initial de la caisse ($cur)" },
  settingsFooterNote = "Note de bas d'affiche",
  settingsStructureTitle = "Étages & Appartements",
  settingsIncludeRdc = "Inclure le rez-de-chaussée (RDC)",
  settingsFloorsCount = "Nombre de niveaux",
  settingsApartmentsCount = "Nombre total d'appartements",
  settingsFeesTitle = "Cotisations & 12 Mois",
  settingsDefaultFee = { cur -> "Cotisation par défaut ($cur)" },
  settingsApplyDefaultToAll = "Appliquer à tous",
  settingsDueDayLabel = "Jour limite de paiement du mois",
  settingsCutoffMonthLabel = "Mois de référence des relances",
  settingsActiveMonthsLabel = "Tableau des 12 mois (activation & tarif)",
  settingsPresetFullYear = "12 mois (Année complète)",
  settingsPresetJunDec = "Juin – Déc (7 mois)",
  settingsWhatsappTitle = "Relances automatiques WhatsApp",
  settingsWhatsappTemplateLabel = "Modèle du message WhatsApp",
  settingsWhatsappVarsHint = "Appuyez sur une variable pour l'insérer automatiquement :",
  settingsMinReminderLabel = { cur -> "Seuil minimal de relance ($cur)" },
  settingsRelanceHeaderLabel = "Texte d'en-tête de l'affiche de relance",
  settingsBackupSecurityTitle = "Sauvegarde JSON & Sécurité PIN",
  settingsExportJsonBtn = "Sauvegarder (.json)",
  settingsImportJsonBtn = "Restaurer (.json)",
  settingsImportCsvBtn = "Importer propriétaires (CSV)",
  settingsPinTitle = "Verrouillage par code PIN",
  settingsSetPinBtn = "Définir / Modifier le PIN",
  settingsDisablePinBtn = "Désactiver le PIN",
  settingsSaveButton = "Enregistrer les réglages",
  settingsNewBlankBuildingBtn = "Initialiser un immeuble vierge",
  settingsResetC49Btn = "Restaurer les données C49 (8200 DH)",
  settingsConfirmBtn = "Confirmer",
  settingsConnectedAs = { email -> "Connecté : $email" },
  signOut = "Se déconnecter",
  pinUnlockTitle = "Saisissez le code PIN",
  pinUnlockSub = "Application protégée par code PIN du syndic",
  pinWrongError = "Code PIN incorrect",
)

fun stringsForLanguage(codeOrLang: String?): SyndicatStrings {
  return if (AppLanguage.fromCode(codeOrLang) == AppLanguage.FR) FrenchStrings else ArabicStrings
}

val LocalStrings = staticCompositionLocalOf { ArabicStrings }
