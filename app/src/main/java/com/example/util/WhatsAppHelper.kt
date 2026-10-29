package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.model.ApartmentRecord
import com.example.data.model.DEFAULT_WHATSAPP_TEMPLATE_AR
import com.example.data.model.DEFAULT_WHATSAPP_TEMPLATE_FR
import com.example.data.model.DEFAULT_WHATSAPP_TEMPLATE_URGENT_AR
import com.example.data.model.DEFAULT_WHATSAPP_TEMPLATE_URGENT_FR
import com.example.data.model.MonthColumn
import com.example.data.model.SyndicatConfig
import com.example.ui.i18n.LRM
import com.example.ui.i18n.NBSP
import com.example.ui.i18n.bidiLtr
import com.example.ui.i18n.formatNumber
import com.example.ui.i18n.localizeDigits
import com.example.ui.i18n.stringsForLanguage

enum class WhatsAppTemplatePreset(val id: String) {
  FRIENDLY_AR("friendly_ar"),
  URGENT_AR("urgent_ar"),
  FRIENDLY_FR("friendly_fr"),
  URGENT_FR("urgent_fr");

  fun templateText(): String = when (this) {
    FRIENDLY_AR -> DEFAULT_WHATSAPP_TEMPLATE_AR
    URGENT_AR -> DEFAULT_WHATSAPP_TEMPLATE_URGENT_AR
    FRIENDLY_FR -> DEFAULT_WHATSAPP_TEMPLATE_FR
    URGENT_FR -> DEFAULT_WHATSAPP_TEMPLATE_URGENT_FR
  }
}

object WhatsAppHelper {

  val SUPPORTED_VARIABLES: List<String> = listOf(
    "{الاسم}",
    "{المبلغ}",
    "{العملة}",
    "{الشهر}",
    "{الأشهر_المتبقية}",
    "{رقم_الشقة}",
    "{الطابق}",
    "{اسم_العمارة}",
    "{يوم_الأداء}",
  )

  /**
   * Normalizes Moroccan phone numbers (06 / 07 / 05 / +212 / 00212) to international WhatsApp format
   * without '+' (e.g., "212612345678"). Preserves other international numbers if >= 8 digits.
   */
  fun normalizePhoneForWhatsApp(rawPhone: String): String {
    val digits = rawPhone.filter { it.isDigit() }
    if (digits.isEmpty()) return ""
    return when {
      digits.startsWith("00212") && digits.length >= 13 -> "212" + digits.drop(5)
      digits.startsWith("212") && digits.length >= 11 -> digits
      (digits.startsWith("06") || digits.startsWith("07") || digits.startsWith("05")) && digits.length == 10 ->
        "212" + digits.drop(1)
      (digits.startsWith("6") || digits.startsWith("7")) && digits.length == 9 ->
        "212$digits"
      digits.length >= 8 -> digits
      else -> ""
    }
  }

  fun hasValidWhatsAppPhone(rawPhone: String): Boolean {
    return normalizePhoneForWhatsApp(rawPhone).isNotBlank()
  }

  fun formatDisplayPhone(rawPhone: String): String {
    val wa = normalizePhoneForWhatsApp(rawPhone)
    return if (wa.startsWith("212") && wa.length == 12) {
      val groups = listOf(
        "+${LRM}212",
        wa.substring(3, 4),
        wa.substring(4, 6),
        wa.substring(6, 8),
        wa.substring(8, 10),
        wa.substring(10, 12),
      )
      bidiLtr(groups.joinToString("$LRM$NBSP$LRM"))
    } else {
      val trimmed = rawPhone.trim()
      if (trimmed.isEmpty()) "" else bidiLtr(trimmed)
    }
  }

  /**
   * Builds the personalized WhatsApp reminder message using the configured or overridden template
   * and variables (supporting both Arabic and French variable aliases):
   * - {الاسم} / {nom}
   * - {المبلغ} / {montant}
   * - {العملة} / {devise}
   * - {الشهر} / {mois}
   * - {الأشهر_المتبقية} / {mois_impayes}
   * - {رقم_الشقة} / {appartement}
   * - {الطابق} / {etage}
   * - {اسم_العمارة} / {immeuble}
   * - {يوم_الأداء} / {jour_echeance}
   */
  fun buildReminderMessage(
    apartment: ApartmentRecord,
    config: SyndicatConfig,
    overrideTemplate: String? = null,
  ): String {
    val isAr = config.language != "fr"
    val fallbackTemplate = if (isAr) DEFAULT_WHATSAPP_TEMPLATE_AR else DEFAULT_WHATSAPP_TEMPLATE_FR
    val template = (overrideTemplate ?: config.whatsappTemplate).trim().ifBlank { fallbackTemplate }

    val ownerName = if (isAr) {
      apartment.ownerNameAr.trim().ifBlank { apartment.ownerName.trim() }.ifBlank { "المالك الكريم" }
    } else {
      apartment.ownerName.trim().ifBlank { apartment.ownerNameAr.trim() }.ifBlank { "Copropriétaire" }
    }

    val remainingStr = formatNumber(apartment.remainingDh(config), config.digitsAr)
    val currencyStr = config.currency.trim().ifBlank { if (isAr) "درهم" else "DH" }
    val cutoffCol = MonthColumn.fromNumber(config.cutoffMonth.toInt())
    val monthName = if (isAr) cutoffCol.fullAr else cutoffCol.fullFr

    val missingCols = apartment.missingMonths(config)
    val missingMonthsStr = if (missingCols.isEmpty()) {
      monthName
    } else {
      val sep = if (isAr) "، " else ", "
      missingCols.joinToString(sep) { if (isAr) it.fullAr else it.fullFr }
    }

    val strings = stringsForLanguage(config.language)
    val floorStr = strings.floorWithPrefix(apartment.floor)
    val aptNumStr = bidiLtr(localizeDigits(apartment.displayNumber, config.digitsAr))
    val residenceStr = config.residenceName.trim().ifBlank { if (isAr) "الإقامة" else "la résidence" }
    val dueDayStr = bidiLtr(localizeDigits(config.dueDayOfMonth.toString(), config.digitsAr))

    return template
      .replace("{الاسم}", ownerName)
      .replace("{nom}", ownerName)
      .replace("{المبلغ}", remainingStr)
      .replace("{montant}", remainingStr)
      .replace("{العملة}", currencyStr)
      .replace("{devise}", currencyStr)
      .replace("{الشهر}", monthName)
      .replace("{mois}", monthName)
      .replace("{الأشهر_المتبقية}", missingMonthsStr)
      .replace("{mois_impayes}", missingMonthsStr)
      .replace("{رقم_الشقة}", aptNumStr)
      .replace("{appartement}", aptNumStr)
      .replace("{الطابق}", floorStr)
      .replace("{etage}", floorStr)
      .replace("{اسم_العمارة}", residenceStr)
      .replace("{immeuble}", residenceStr)
      .replace("{يوم_الأداء}", dueDayStr)
      .replace("{jour_echeance}", dueDayStr)
  }

  /**
   * Builds a realistic sample preview apartment so the syndic can preview the exact WhatsApp
   * message inside Settings or the Campaign Assistant even before selecting a specific apartment.
   */
  fun buildSamplePreviewMessage(
    config: SyndicatConfig,
    overrideTemplate: String? = null,
    sampleApartment: ApartmentRecord? = null,
  ): String {
    val sample = sampleApartment ?: ApartmentRecord(
      id = "preview_apt",
      number = 4L,
      floor = "1er",
      status = "occupied",
      ownerName = "Karim Benali",
      ownerNameAr = "كريم بنعلي",
      phone = "0612345678",
      juin = config.feeJuin,
      juil = config.feeJuil,
      aout = config.feeAout,
      sept = 0L,
      oct = 0L,
    )
    return buildReminderMessage(
      apartment = sample,
      config = config,
      overrideTemplate = overrideTemplate,
    )
  }

  /**
   * Builds a consolidated group summary of all late residents using the residence details,
   * suitable for sharing directly into the building's WhatsApp Residents Group.
   */
  fun buildGroupReminderBroadcastMessage(
    apartments: List<ApartmentRecord>,
    config: SyndicatConfig,
    anonymousOnlyNumbers: Boolean = false,
  ): String {
    val isAr = config.language != "fr"
    val cur = config.currency.trim().ifBlank { if (isAr) "درهم" else "DH" }
    val cutoffCol = MonthColumn.fromNumber(config.cutoffMonth.toInt())
    val monthName = if (isAr) cutoffCol.fullAr else cutoffCol.fullFr
    val dueDayStr = bidiLtr(localizeDigits(config.dueDayOfMonth.toString(), config.digitsAr))
    val totalRem = formatNumber(apartments.sumOf { it.remainingDh(config) }, config.digitsAr)

    return buildString {
      if (isAr) {
        appendLine("📢 *تذكير بالمساهمة الشهرية – ${config.residenceName}*")
        appendLine("الوضعية إلى غاية شهر $monthName ${bidiLtr(localizeDigits(config.year.toString(), config.digitsAr))} (آخر أجل يوم $dueDayStr):")
        appendLine()
        apartments.forEach { apt ->
          val rem = formatNumber(apt.remainingDh(config), config.digitsAr)
          val missing = apt.missingMonths(config).joinToString("، ") { it.shortAr }
          val numLbl = bidiLtr(localizeDigits(apt.displayNumber, config.digitsAr))
          val who = if (anonymousOnlyNumbers) {
            "الشقة $numLbl"
          } else {
            val owner = apt.displayOwner(true)
            if (owner.isNotBlank()) "الشقة $numLbl ($owner)" else "الشقة $numLbl"
          }
          appendLine("• $who : $rem $cur${if (missing.isNotBlank()) " [$missing]" else ""}")
        }
        appendLine()
        appendLine("المجموع المتبقي للتحصيل: *$totalRem $cur*")
        append(config.relanceHeaderNote.ifBlank { "نرجو من السادة المتأخرين تسوية وضعيتهم وشكراً لتعاونكم." })
      } else {
        appendLine("📢 *Rappel de cotisation syndicale – ${config.residenceName}*")
        appendLine("Situation à fin $monthName ${config.year} (échéance le $dueDayStr du mois) :")
        appendLine()
        apartments.forEach { apt ->
          val rem = formatNumber(apt.remainingDh(config), config.digitsAr)
          val missing = apt.missingMonths(config).joinToString(", ") { it.shortFr }
          val who = if (anonymousOnlyNumbers) {
            "Appt ${apt.displayNumber}"
          } else {
            val owner = apt.displayOwner(false)
            if (owner.isNotBlank()) "Appt ${apt.displayNumber} ($owner)" else "Appt ${apt.displayNumber}"
          }
          appendLine("• $who : $rem $cur${if (missing.isNotBlank()) " [$missing]" else ""}")
        }
        appendLine()
        appendLine("Total restant à recouvrer : *$totalRem $cur*")
        append("Merci à chacun pour sa ponctualité afin d'assurer l'entretien de l'immeuble.")
      }
    }
  }

  fun copyTextToClipboard(
    context: Context,
    label: String,
    text: String,
  ) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
  }

  fun openWhatsAppReminder(
    context: Context,
    apartment: ApartmentRecord,
    config: SyndicatConfig,
    onError: (String) -> Unit = {},
    overrideTemplate: String? = null,
    overrideMessage: String? = null,
  ): Boolean {
    val message = overrideMessage ?: buildReminderMessage(apartment, config, overrideTemplate)
    return openRawWhatsAppMessage(
      context = context,
      rawPhone = apartment.phone,
      message = message,
      onError = onError,
    )
  }

  fun openRawWhatsAppMessage(
    context: Context,
    rawPhone: String,
    message: String,
    onError: (String) -> Unit = {},
  ): Boolean {
    val encodedText = Uri.encode(message)
    val waPhone = normalizePhoneForWhatsApp(rawPhone)
    val url = if (waPhone.isNotBlank()) {
      "https://wa.me/$waPhone?text=$encodedText"
    } else {
      "https://wa.me/?text=$encodedText"
    }

    return try {
      val viewIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(viewIntent)
      true
    } catch (_: Exception) {
      try {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
          type = "text/plain"
          putExtra(Intent.EXTRA_TEXT, message)
        }
        val chooser = Intent.createChooser(shareIntent, "WhatsApp").apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
        true
      } catch (e: Exception) {
        onError(e.localizedMessage ?: "WhatsApp non disponible")
        false
      }
    }
  }
}
