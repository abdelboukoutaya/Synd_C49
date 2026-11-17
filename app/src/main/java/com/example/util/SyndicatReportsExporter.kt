package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.local.AgMinutesDraft
import com.example.data.local.SpecialCallRecord
import com.example.data.model.ApartmentRecord
import com.example.data.model.MonthColumn
import com.example.data.model.PaymentRecord
import com.example.ui.i18n.SyndicatStrings
import com.example.ui.i18n.formatNumber
import com.example.ui.i18n.localizeDigits
import com.example.ui.i18n.stringsForLanguage
import com.example.ui.viewmodel.SyndicatDashboardData
import com.example.ui.viewmodel.formatDh
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Exportateur PDF A4 dédié aux documents de transparence :
 * 1) Relevé annuel individuel d'un appartement (cotisations mensuelles, appels de fonds, historique, solde dû)
 * 2) Procès-Verbal (PV) officiel d'Assemblée Générale (quorum, ordre du jour, résolutions et votes)
 * Les deux documents prennent en charge le mode Couleur et le mode Noir & Blanc (monochromeMode).
 */
object SyndicatReportsExporter {

  private const val PAGE_W = 1240
  private const val PAGE_H = 1754

  fun buildApartmentWhatsAppStatement(
    strings: SyndicatStrings,
    data: SyndicatDashboardData,
    apartment: ApartmentRecord,
    specialCalls: List<SpecialCallRecord>,
  ): String {
    val cfg = data.config
    val isAr = cfg.language == "ar"
    val cur = cfg.currency
    val dig = cfg.digitsAr
    val totalPaid = apartment.totalForConfig(cfg)
    val expectedCutoff = apartment.dueAtCutoffDh(cfg)
    val remaining = apartment.remainingDh(cfg)
    val owner = apartment.displayOwner(isAr).ifBlank { "—" }

    val paidMonths = cfg.activeMonthColumns.filter { apartment.amountFor(it) > 0L }
      .joinToString(", ") { strings.monthShort(it) }
      .ifBlank { "—" }
    val unpaidMonths = cfg.activeMonthColumns.filter { m ->
      !apartment.isVacantForConfig(cfg) &&
        m.monthNumber <= cfg.cutoffMonth.toInt() &&
        apartment.amountFor(m) < apartment.expectedFeeForMonth(m, cfg)
    }.joinToString(", ") { strings.monthShort(it) }.ifBlank { "—" }

    val activeCalls = specialCalls.filter { apartment.status == "occupied" }
    val callsSummary = if (activeCalls.isEmpty()) {
      ""
    } else {
      val unpaidCalls = activeCalls.filter { !it.isApartmentPaid(apartment.number) }
      val totalCallsDue = unpaidCalls.sumOf { it.amountPerAptDh }
      "\n${strings.tabSpecialCalls}: ${activeCalls.size - unpaidCalls.size}/${activeCalls.size} (${strings.dashRemaining}: ${formatDh(totalCallsDue, cur, dig)})"
    }

    return buildString {
      append("*${cfg.residenceName}* — ${strings.statementTitle}\n")
      append("${strings.apptLabel} ${apartment.displayNumber} (${strings.floorWithPrefix(apartment.floor)}) · $owner\n")
      append("${strings.yearExerciseLabel} : ${localizeDigits(cfg.year.toString(), dig)}\n")
      append("• ${strings.cardTotalPaid} : ${formatDh(totalPaid, cur, dig)}\n")
      append("• ${strings.cardDueLabel} : ${formatDh(expectedCutoff, cur, dig)}\n")
      append("• ${strings.dashRemaining} : ${formatDh(remaining, cur, dig)}\n")
      append("• ✓ : $paidMonths\n")
      append("• ! : $unpaidMonths")
      append(callsSummary)
    }
  }

  fun generateApartmentStatementBitmap(
    data: SyndicatDashboardData,
    apartment: ApartmentRecord,
    specialCalls: List<SpecialCallRecord>,
    payments: List<PaymentRecord>,
    monochromeMode: Boolean = false,
  ): Bitmap {
    val cfg = data.config
    val strings = stringsForLanguage(cfg.language)
    val isAr = cfg.language == "ar"
    val cur = cfg.currency
    val dig = cfg.digitsAr

    val bmp = Bitmap.createBitmap(PAGE_W, PAGE_H, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    canvas.drawColor(Color.WHITE)

    val brandInk = if (monochromeMode) Color.BLACK else Color.rgb(16, 44, 92)
    val accentBlue = if (monochromeMode) Color.rgb(40, 40, 40) else Color.rgb(0, 82, 255)
    val accentGreen = if (monochromeMode) Color.rgb(35, 35, 35) else Color.rgb(9, 133, 81)
    val accentRed = if (monochromeMode) Color.rgb(20, 20, 20) else Color.rgb(207, 32, 47)
    val softBg = if (monochromeMode) Color.rgb(244, 244, 244) else Color.rgb(245, 248, 255)
    val borderColor = if (monochromeMode) Color.rgb(140, 140, 140) else Color.rgb(214, 222, 235)

    val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = brandInk }
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.WHITE
      textSize = 38f
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val subWhitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.rgb(225, 232, 245)
      textSize = 23f
    }
    val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = brandInk
      textSize = 27f
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.rgb(32, 38, 46)
      textSize = 22f
    }
    val boldBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.rgb(16, 20, 26)
      textSize = 23f
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val mutePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.rgb(98, 108, 122)
      textSize = 19f
    }
    val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = softBg }
    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = borderColor
      style = Paint.Style.STROKE
      strokeWidth = 2f
    }

    // Header banner
    canvas.drawRect(0f, 0f, PAGE_W.toFloat(), 196f, headerPaint)
    canvas.drawText(cfg.residenceName.ifBlank { "SYNDICAT" }, 58f, 74f, titlePaint)
    canvas.drawText(
      "${strings.statementTitle} · ${strings.yearExerciseLabel} ${localizeDigits(cfg.year.toString(), dig)}",
      58f,
      118f,
      subWhitePaint,
    )
    val dateToday = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Date())
    canvas.drawText(
      "${cfg.syndicatName} · ${localizeDigits(dateToday, dig)}",
      58f,
      158f,
      subWhitePaint,
    )

    // Lot & Owner summary card
    var y = 234f
    val infoRect = RectF(58f, y, PAGE_W - 58f, y + 150f)
    canvas.drawRoundRect(infoRect, 18f, 18f, cardPaint)
    canvas.drawRoundRect(infoRect, 18f, 18f, strokePaint)

    val ownerName = apartment.displayOwner(isAr).ifBlank { "—" }
    canvas.drawText(
      "${strings.apptLabel} ${apartment.displayNumber}  ·  ${strings.floorWithPrefix(apartment.floor)}",
      86f,
      y + 46f,
      sectionPaint,
    )
    canvas.drawText("$ownerName  ${if (apartment.phone.isNotBlank()) "· ${apartment.phone}" else ""}", 86f, y + 84f, bodyPaint)

    val totalPaid = apartment.totalForConfig(cfg)
    val expectedCut = apartment.dueAtCutoffDh(cfg)
    val remaining = apartment.remainingDh(cfg)
    val kpiPaint = Paint(boldBodyPaint).apply { color = accentGreen }
    canvas.drawText("${strings.cardTotalPaid}: ${formatDh(totalPaid, cur, dig)}", 86f, y + 124f, kpiPaint)
    val remPaint = Paint(boldBodyPaint).apply { color = if (remaining > 0L) accentRed else accentGreen }
    canvas.drawText("${strings.dashRemaining}: ${formatDh(remaining, cur, dig)} / ${formatDh(expectedCut, cur, dig)}", 560f, y + 124f, remPaint)

    // 12-month grid (3 rows x 4 columns)
    y += 188f
    canvas.drawText(strings.statementMonthsSection, 58f, y, sectionPaint)
    y += 22f
    val colW = (PAGE_W - 116f - 36f) / 4f
    val tileH = 104f
    MonthColumn.entries.chunked(4).forEachIndexed { rIdx, rowCols ->
      val ry = y + rIdx * (tileH + 12f)
      rowCols.forEachIndexed { cIdx, m ->
        val rx = 58f + cIdx * (colW + 12f)
        val rect = RectF(rx, ry, rx + colW, ry + tileH)
        val paid = apartment.amountFor(m)
        val exp = apartment.expectedFeeForMonth(m, cfg)
        val isActive = m in cfg.activeMonthColumns
        val isVacant = apartment.isVacantForConfig(cfg)
        val tilePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          color = when {
            !isActive || isVacant -> Color.rgb(245, 245, 245)
            paid >= exp && exp > 0L -> if (monochromeMode) Color.rgb(232, 232, 232) else Color.rgb(232, 248, 240)
            paid > 0L -> if (monochromeMode) Color.rgb(238, 238, 238) else Color.rgb(255, 246, 224)
            m.monthNumber <= cfg.cutoffMonth.toInt() -> if (monochromeMode) Color.rgb(228, 228, 228) else Color.rgb(255, 236, 239)
            else -> Color.WHITE
          }
        }
        canvas.drawRoundRect(rect, 12f, 12f, tilePaint)
        canvas.drawRoundRect(rect, 12f, 12f, strokePaint)
        canvas.drawText(strings.monthShort(m), rx + 16f, ry + 36f, boldBodyPaint)
        val statusStr = when {
          !isActive -> "—"
          isVacant -> strings.statusVacant
          paid >= exp && exp > 0L -> "✓ ${formatDh(paid, cur, dig)}"
          paid > 0L -> "◐ ${formatDh(paid, cur, dig)} / ${formatNumber(exp, dig)}"
          m.monthNumber <= cfg.cutoffMonth.toInt() -> "! 0 / ${formatNumber(exp, dig)}"
          else -> formatDh(exp, cur, dig)
        }
        val statusPaint = Paint(bodyPaint).apply {
          textSize = 19f
          color = when {
            paid >= exp && exp > 0L -> accentGreen
            m.monthNumber <= cfg.cutoffMonth.toInt() && !isVacant && paid < exp -> accentRed
            else -> accentBlue
          }
        }
        canvas.drawText(statusStr, rx + 16f, ry + 76f, statusPaint)
      }
    }

    y += 3 * (tileH + 12f) + 34f

    // Special Calls section
    val aptCalls = if (apartment.status == "occupied") specialCalls else emptyList()
    if (aptCalls.isNotEmpty()) {
      canvas.drawText(strings.tabSpecialCalls, 58f, y, sectionPaint)
      y += 28f
      aptCalls.take(5).forEach { call ->
        val paid = call.isApartmentPaid(apartment.number)
        canvas.drawText(
          "• ${call.displayTitle(isAr)} : ${formatDh(call.amountPerAptDh, cur, dig)} — ${if (paid) "✓" else "!"}",
          72f,
          y + 18f,
          bodyPaint,
        )
        y += 34f
      }
      y += 18f
    }

    // Recent payment receipts history
    val aptPayments = payments.filter { it.apartmentNumber == apartment.number }.take(10)
    canvas.drawText(strings.paymentHistoryTitle, 58f, y, sectionPaint)
    y += 30f
    if (aptPayments.isEmpty()) {
      canvas.drawText("—", 72f, y + 14f, mutePaint)
    } else {
      aptPayments.forEach { pay ->
        if (y < PAGE_H - 130f) {
          val recTag = if (pay.receiptNumber > 0L) "#${pay.receiptNumber} · " else ""
          canvas.drawText(
            "$recTag${localizeDigits(pay.paymentDate, dig)}  |  ${strings.paymentMethodLabel(pay.paymentMethod)}  |  ${formatDh(pay.amountDh, cur, dig)}",
            72f,
            y + 16f,
            bodyPaint,
          )
          y += 32f
        }
      }
    }

    // Footer
    canvas.drawText("SYNDICAT · DYNIC · ${cfg.footerNote}", 58f, PAGE_H - 48f, mutePaint)
    return bmp
  }

  fun exportApartmentStatementPdf(
    context: Context,
    data: SyndicatDashboardData,
    apartment: ApartmentRecord,
    specialCalls: List<SpecialCallRecord>,
    payments: List<PaymentRecord>,
    monochromeMode: Boolean = false,
    onError: (String) -> Unit = {},
  ) {
    val bmp = generateApartmentStatementBitmap(data, apartment, specialCalls, payments, monochromeMode)
    shareSinglePageBitmapAsPdf(
      context = context,
      bitmap = bmp,
      fileName = "releve_appt_${apartment.number}_${data.config.year}.pdf",
      chooserTitle = stringsForLanguage(data.config.language).statementTitle,
      onError = onError,
    )
  }

  fun generateAgMinutesBitmap(
    data: SyndicatDashboardData,
    draft: AgMinutesDraft,
    monochromeMode: Boolean = false,
  ): Bitmap {
    val cfg = data.config
    val strings = stringsForLanguage(cfg.language)
    val dig = cfg.digitsAr

    val bmp = Bitmap.createBitmap(PAGE_W, PAGE_H, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    canvas.drawColor(Color.WHITE)

    val headerColor = if (monochromeMode) Color.BLACK else Color.rgb(16, 44, 92)
    val accentGreen = if (monochromeMode) Color.rgb(30, 30, 30) else Color.rgb(9, 133, 81)
    val accentRed = if (monochromeMode) Color.rgb(20, 20, 20) else Color.rgb(207, 32, 47)
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = if (monochromeMode) Color.rgb(130, 130, 130) else Color.rgb(210, 220, 234)
      style = Paint.Style.STROKE
      strokeWidth = 2f
    }
    val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = if (monochromeMode) Color.rgb(245, 245, 245) else Color.rgb(244, 247, 255)
    }

    val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = headerColor }
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.WHITE
      textSize = 36f
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val subWhitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.rgb(225, 232, 245)
      textSize = 22f
    }
    val h2Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = headerColor
      textSize = 26f
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.rgb(28, 34, 42)
      textSize = 21f
    }
    val boldPaint = Paint(bodyPaint).apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
    val mutePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.rgb(100, 110, 125)
      textSize = 18f
    }

    // Banner
    canvas.drawRect(0f, 0f, PAGE_W.toFloat(), 190f, headerPaint)
    canvas.drawText(strings.agPvPdfTitle, 58f, 72f, titlePaint)
    canvas.drawText("${cfg.residenceName} · ${cfg.syndicatName}", 58f, 114f, subWhitePaint)
    val dateLabel = draft.meetingDate.ifBlank { SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Date()) }
    val locLabel = draft.location.ifBlank { cfg.address.ifBlank { cfg.city.ifBlank { "—" } } }
    canvas.drawText(
      "${localizeDigits(dateLabel, dig)}  ·  $locLabel",
      58f,
      154f,
      subWhitePaint,
    )

    // Quorum Summary Box
    val occupied = data.apartments.filter { it.status == "occupied" }
    val presentCount = draft.presentAptNumbers.count { num -> occupied.any { it.number == num } }
    val pct = if (occupied.isEmpty()) 0 else (presentCount * 100) / occupied.size
    val quorumOk = pct >= 50

    var y = 228f
    val qRect = RectF(58f, y, PAGE_W - 58f, y + 128f)
    canvas.drawRoundRect(qRect, 16f, 16f, boxPaint)
    canvas.drawRoundRect(qRect, 16f, 16f, borderPaint)
    canvas.drawText(strings.agQuorumTitle, 84f, y + 42f, h2Paint)
    val qTextPaint = Paint(boldPaint).apply { color = if (quorumOk) accentGreen else accentRed }
    canvas.drawText(
      "${strings.agQuorumPresentCount(presentCount, occupied.size)}  —  ${formatNumber(pct.toLong(), dig)}%  (${if (quorumOk) strings.agQuorumReached else strings.agQuorumMissing})",
      84f,
      y + 84f,
      qTextPaint,
    )
    val presentNumsStr = draft.presentAptNumbers.sorted().joinToString(", ") { "#$it" }.ifBlank { "—" }
    canvas.drawText(presentNumsStr.take(86), 84f, y + 114f, mutePaint)

    // Agenda
    y += 166f
    canvas.drawText(strings.agAgendaLabel, 58f, y, h2Paint)
    y += 30f
    val agendaLines = draft.agenda.ifBlank { "—" }.lines().take(6)
    agendaLines.forEach { line ->
      canvas.drawText("• ${line.take(92)}", 72f, y + 16f, bodyPaint)
      y += 30f
    }

    // Resolutions
    y += 28f
    canvas.drawText(strings.agResolutionsLabel, 58f, y, h2Paint)
    y += 26f
    if (draft.resolutions.isEmpty()) {
      canvas.drawText("—", 72f, y + 20f, mutePaint)
      y += 42f
    } else {
      draft.resolutions.take(8).forEachIndexed { idx, res ->
        if (y < PAGE_H - 280f) {
          val rRect = RectF(58f, y, PAGE_W - 58f, y + 92f)
          canvas.drawRoundRect(rRect, 12f, 12f, boxPaint)
          canvas.drawRoundRect(rRect, 12f, 12f, borderPaint)
          canvas.drawText("${idx + 1}. ${res.title.take(78)}", 80f, y + 36f, boldPaint)
          val outcome = if (res.isAdopted()) strings.agResolutionAdopted else strings.agResolutionRejected
          val oPaint = Paint(boldPaint).apply { color = if (res.isAdopted()) accentGreen else accentRed }
          canvas.drawText(
            "${strings.agVoteFor}: ${res.votesFor}  ·  ${strings.agVoteAgainst}: ${res.votesAgainst}  ·  ${strings.agVoteAbstain}: ${res.votesAbstain}   →   $outcome",
            80f,
            y + 72f,
            oPaint,
          )
          y += 106f
        }
      }
    }

    // Signatures block at bottom
    val sigTop = (PAGE_H - 220f).coerceAtLeast(y + 24f)
    val sigW = (PAGE_W - 148f) / 2f
    val sigLeft = RectF(58f, sigTop, 58f + sigW, sigTop + 120f)
    val sigRight = RectF(90f + sigW, sigTop, PAGE_W - 58f, sigTop + 120f)
    canvas.drawRoundRect(sigLeft, 12f, 12f, borderPaint)
    canvas.drawRoundRect(sigRight, 12f, 12f, borderPaint)
    canvas.drawText(strings.agSigSyndic, 78f, sigTop + 34f, boldPaint)
    canvas.drawText(strings.agSigCouncil, 110f + sigW, sigTop + 34f, boldPaint)

    canvas.drawText("SYNDICAT · DYNIC · ${cfg.footerNote}", 58f, PAGE_H - 42f, mutePaint)
    return bmp
  }

  fun exportAgMinutesPdf(
    context: Context,
    data: SyndicatDashboardData,
    draft: AgMinutesDraft,
    monochromeMode: Boolean = false,
    onError: (String) -> Unit = {},
  ) {
    val bmp = generateAgMinutesBitmap(data, draft, monochromeMode)
    shareSinglePageBitmapAsPdf(
      context = context,
      bitmap = bmp,
      fileName = "pv_ag_${data.config.year}.pdf",
      chooserTitle = stringsForLanguage(data.config.language).agPvPdfTitle,
      onError = onError,
    )
  }

  private fun shareSinglePageBitmapAsPdf(
    context: Context,
    bitmap: Bitmap,
    fileName: String,
    chooserTitle: String,
    onError: (String) -> Unit,
  ) {
    try {
      val dir = File(context.cacheDir, "exports").apply { mkdirs() }
      val outFile = File(dir, fileName)
      val pdf = PdfDocument()
      val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
      val page = pdf.startPage(pageInfo)
      page.canvas.drawBitmap(bitmap, 0f, 0f, null)
      pdf.finishPage(page)
      FileOutputStream(outFile).use { fos -> pdf.writeTo(fos) }
      pdf.close()

      val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outFile)
      val share = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
      context.startActivity(Intent.createChooser(share, chooserTitle).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: Exception) {
      onError(e.localizedMessage ?: "Export PDF error")
    }
  }
}
