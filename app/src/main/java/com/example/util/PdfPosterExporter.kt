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
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.TypefaceCompat
import com.example.R
import com.example.data.model.ApartmentRecord
import com.example.data.model.ExpenseRecord
import com.example.data.model.MonthColumn
import com.example.data.model.PaymentRecord
import com.example.data.model.ProjectRecord
import com.example.data.model.sortProjectsByPriority
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.RLM
import com.example.ui.i18n.bidiLtr
import com.example.ui.i18n.formatNumber
import com.example.ui.i18n.localizeDigits
import com.example.ui.i18n.stringsForLanguage
import com.example.ui.theme.isSpecialCotisationAmount
import com.example.ui.viewmodel.SyndicatDashboardData
import com.example.ui.viewmodel.formatDh
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

enum class ExportVariant {
  PRINT,
  SCREEN,
}

data class CotisationExportOptions(
  val showNames: Boolean = true,
  val untilCutoffOnly: Boolean = false,
  val inkSaver: Boolean = false,
  val monochromeMode: Boolean = false,
  val variant: ExportVariant = ExportVariant.PRINT,
)

data class RelanceExportOptions(
  val anonymousMode: Boolean = false,
  val inkSaver: Boolean = false,
  val monochromeMode: Boolean = false,
  val variant: ExportVariant = ExportVariant.PRINT,
)

data class ProjectsExportOptions(
  val inkSaver: Boolean = false,
  val monochromeMode: Boolean = false,
  val variant: ExportVariant = ExportVariant.PRINT,
)

private data class ExportPalette(
  val pageBg: Int,
  val cardBg: Int,
  val headerBg: Int,
  val altRowBg: Int,
  val vacantRowBg: Int,
  val ink: Int,
  val body: Int,
  val mute: Int,
  val hairline: Int,
  val floorLine: Int,
  val paidFg: Int,
  val partialFg: Int,
  val lateFg: Int,
  val caisseBandBg: Int,
  val caisseBandFg: Int,
  val tfRegular: Typeface,
  val tfMedium: Typeface,
  val tfNumeric: Typeface,
  val isMonochrome: Boolean = false,
)

object PdfPosterExporter {

  // Format ISO A4 Portrait en points PDF (72 dpi : 210 × 297 mm = 595 × 842 pt)
  const val A4_PORTRAIT_WIDTH = 595
  const val A4_PORTRAIT_HEIGHT = 842

  // Format Portrait pour le reçu individuel (ratio A4 ~ 1 : 1.417, lisible plein écran sur mobile)
  const val RECEIPT_PORTRAIT_WIDTH = 480
  const val RECEIPT_PORTRAIT_HEIGHT = 680

  private const val COTISATION_MAX_ROWS_PER_A4_PAGE = 32
  private const val RELANCE_MAX_ROWS_PER_A4_PAGE = 17
  private const val CAISSE_FIRST_PAGE_MAX_EXPENSES = 12
  private const val CAISSE_NEXT_PAGE_MAX_EXPENSES = 19

  // Meta Design System paper & WhatsApp A4 Portrait palette — stark white #FFFFFF canvas + Deep Ink #0A1317 + Cobalt #0064E0
  private val INK_COLOR = Color.parseColor("#0A1317")
  private val BODY_COLOR = Color.parseColor("#1C1E21")
  private val MUTE_COLOR = Color.parseColor("#5D6C7B")
  private val SURFACE_SOFT = Color.parseColor("#F1F4F7")
  private val SURFACE_STRONG = Color.parseColor("#E8EEF3")
  private val HAIRLINE_COLOR = Color.parseColor("#DEE3E9")
  private val SUCCESS_GREEN = Color.parseColor("#31A24C")
  private val INFO_BLUE = Color.parseColor("#0064E0")
  private val SIGNATURE_FOREST = Color.parseColor("#0A1317")
  private val SIGNATURE_CORAL = Color.parseColor("#E41E3F")
  private val SIGNATURE_MINT = Color.parseColor("#EAF7EE")
  private val SIGNATURE_PEACH = Color.parseColor("#FFF8E1")

  private fun buildPalette(
    context: Context,
    inkSaver: Boolean,
    monochromeMode: Boolean = false,
  ): ExportPalette {
    val ibmPlexBase = try {
      ResourcesCompat.getFont(context, R.font.ibm_plex_sans_arabic) ?: Typeface.SANS_SERIF
    } catch (_: Exception) {
      Typeface.SANS_SERIF
    }
    val interBase = try {
      ResourcesCompat.getFont(context, R.font.inter) ?: ibmPlexBase
    } catch (_: Exception) {
      ibmPlexBase
    }

    val pureInk = Color.BLACK
    val pureBody = Color.parseColor("#222222")
    val pureMute = Color.parseColor("#555555")
    val pureHairline = Color.parseColor("#D0D0D0")
    val pureSoft = Color.parseColor("#F5F5F5")
    val pureAlt = Color.parseColor("#FAFAFA")

    return if (monochromeMode) {
      ExportPalette(
        pageBg = Color.WHITE,
        cardBg = Color.WHITE,
        headerBg = pureSoft,
        altRowBg = pureAlt,
        vacantRowBg = pureSoft,
        ink = pureInk,
        body = pureBody,
        mute = pureMute,
        hairline = pureHairline,
        floorLine = pureInk,
        paidFg = pureInk,
        partialFg = pureBody,
        lateFg = pureInk,
        caisseBandBg = if (inkSaver) Color.WHITE else Color.BLACK,
        caisseBandFg = if (inkSaver) Color.BLACK else Color.WHITE,
        tfRegular = TypefaceCompat.create(context, ibmPlexBase, 400, false),
        tfMedium = TypefaceCompat.create(context, ibmPlexBase, 500, false),
        tfNumeric = TypefaceCompat.create(context, interBase, 500, false),
        isMonochrome = true,
      )
    } else {
      ExportPalette(
        pageBg = Color.WHITE,
        cardBg = Color.WHITE,
        headerBg = SURFACE_SOFT,
        altRowBg = Color.parseColor("#FAFCFE"),
        vacantRowBg = SURFACE_SOFT,
        ink = INK_COLOR,
        body = BODY_COLOR,
        mute = MUTE_COLOR,
        hairline = HAIRLINE_COLOR,
        floorLine = INK_COLOR,
        paidFg = SUCCESS_GREEN,
        partialFg = INFO_BLUE,
        lateFg = SIGNATURE_CORAL,
        caisseBandBg = if (inkSaver) SURFACE_STRONG else INK_COLOR,
        caisseBandFg = if (inkSaver) INK_COLOR else Color.WHITE,
        tfRegular = TypefaceCompat.create(context, ibmPlexBase, 400, false),
        tfMedium = TypefaceCompat.create(context, ibmPlexBase, 500, false),
        tfNumeric = TypefaceCompat.create(context, interBase, 500, false),
        isMonochrome = false,
      )
    }
  }

  private fun rtlAware(text: String, isRtl: Boolean): String {
    if (!isRtl || text.isEmpty()) return text
    return "$RLM$text"
  }

  private fun ellipsizeToWidth(text: String, paint: Paint, maxWidth: Float): String {
    if (text.isEmpty() || maxWidth <= 4f) return ""
    if (paint.measureText(text) <= maxWidth) return text
    val ellipsis = "…"
    if (paint.measureText(ellipsis) > maxWidth) return ""
    var low = 0
    var high = text.length
    while (low < high) {
      val mid = (low + high + 1) / 2
      val candidate = text.substring(0, mid).trimEnd() + ellipsis
      if (paint.measureText(candidate) <= maxWidth) {
        low = mid
      } else {
        high = mid - 1
      }
    }
    return if (low <= 0) ellipsis else text.substring(0, low).trimEnd() + ellipsis
  }

  private fun wrapTextLines(
    text: String,
    paint: Paint,
    maxWidth: Float,
    maxLines: Int = 2,
  ): List<String> {
    val clean = text.trim()
    if (clean.isEmpty() || maxWidth <= 4f) return listOf("")
    if (maxLines <= 1 || paint.measureText(clean) <= maxWidth) {
      return listOf(ellipsizeToWidth(clean, paint, maxWidth))
    }

    val tokens = clean.split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (tokens.size <= 1) {
      return listOf(ellipsizeToWidth(clean, paint, maxWidth))
    }

    val lines = mutableListOf<String>()
    var current = ""
    var idx = 0
    while (idx < tokens.size) {
      val word = tokens[idx]
      val next = if (current.isEmpty()) word else "$current $word"
      if (paint.measureText(next) <= maxWidth) {
        current = next
        idx++
      } else {
        if (current.isEmpty()) {
          lines.add(ellipsizeToWidth(word, paint, maxWidth))
          idx++
        } else {
          lines.add(current)
          current = ""
        }
        if (lines.size == maxLines - 1) {
          val rest = tokens.subList(idx, tokens.size).joinToString(" ")
          if (rest.isNotEmpty()) {
            lines.add(ellipsizeToWidth(rest, paint, maxWidth))
          }
          return lines
        }
      }
    }
    if (current.isNotEmpty() && lines.size < maxLines) {
      lines.add(ellipsizeToWidth(current, paint, maxWidth))
    }
    return lines.ifEmpty { listOf(ellipsizeToWidth(clean, paint, maxWidth)) }
  }

  private fun createScaledBitmap(
    pageWidth: Int,
    pageHeight: Int,
    scale: Float,
    forceScreen1080Px: Boolean,
  ): Pair<Bitmap, Float> {
    val effectiveScale = if (forceScreen1080Px) {
      1080f / pageWidth.toFloat()
    } else {
      scale
    }
    val bmpW = if (forceScreen1080Px) 1080 else (pageWidth * effectiveScale).roundToInt()
    val bmpH = (pageHeight * effectiveScale).roundToInt().coerceAtLeast(1)
    return Bitmap.createBitmap(bmpW, bmpH, Bitmap.Config.ARGB_8888) to effectiveScale
  }

  // =========================================================================
  // 8.1 AFFICHE DE COTISATION — FORMAT PORTRAIT A4 (595 × 842 pt) & MOBILE
  // =========================================================================

  fun generateOfficialPosterBitmap(
    context: Context,
    data: SyndicatDashboardData,
    options: CotisationExportOptions = CotisationExportOptions(),
    scale: Float = 2f,
  ): Bitmap {
    val activeCols = if (options.untilCutoffOnly) {
      data.config.cutoffMonthColumns.ifEmpty { data.config.activeMonthColumns }
    } else {
      data.config.activeMonthColumns
    }
    val rowCount = data.apartments.size.coerceAtLeast(1)
    val pageWidth = A4_PORTRAIT_WIDTH
    val rowHeight = when {
      rowCount <= 22 -> 25f
      rowCount <= 30 -> 21.5f
      else -> 20f
    }
    val pageHeight = maxOf(
      A4_PORTRAIT_HEIGHT,
      (178f + rowCount * rowHeight).roundToInt(),
    )

    val force1080 = options.variant == ExportVariant.SCREEN
    val (bitmap, effScale) = createScaledBitmap(pageWidth, pageHeight, scale, force1080)
    val canvas = Canvas(bitmap)
    canvas.scale(effScale, effScale)
    drawCotisationPosterCanvas(
      context = context,
      canvas = canvas,
      pageWidth = pageWidth,
      pageHeight = pageHeight,
      data = data,
      apartmentsSlice = data.apartments,
      activeCols = activeCols,
      options = options,
      pageIndex = 0,
      pageCount = 1,
      showGrandFooter = true,
    )
    return bitmap
  }

  fun shareOfficialPosterPdf(
    context: Context,
    data: SyndicatDashboardData,
    options: CotisationExportOptions = CotisationExportOptions(),
    onError: (String) -> Unit,
  ) {
    try {
      val activeCols = if (options.untilCutoffOnly) {
        data.config.cutoffMonthColumns.ifEmpty { data.config.activeMonthColumns }
      } else {
        data.config.activeMonthColumns
      }
      val chunks = data.apartments
        .chunked(COTISATION_MAX_ROWS_PER_A4_PAGE)
        .ifEmpty { listOf(emptyList()) }

      val document = PdfDocument()
      chunks.forEachIndexed { index, slice ->
        val pageInfo = PdfDocument.PageInfo.Builder(
          A4_PORTRAIT_WIDTH,
          A4_PORTRAIT_HEIGHT,
          index + 1,
        ).create()
        val page = document.startPage(pageInfo)
        drawCotisationPosterCanvas(
          context = context,
          canvas = page.canvas,
          pageWidth = A4_PORTRAIT_WIDTH,
          pageHeight = A4_PORTRAIT_HEIGHT,
          data = data,
          apartmentsSlice = slice,
          activeCols = activeCols,
          options = options,
          pageIndex = index,
          pageCount = chunks.size,
          showGrandFooter = index == chunks.lastIndex,
        )
        document.finishPage(page)
      }

      val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
      val dateStamp = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
      val outFile = File(exportDir, "Mulsaq_Musahamat_$dateStamp.pdf")
      FileOutputStream(outFile).use { document.writeTo(it) }
      document.close()

      shareFileUri(context, outFile, "application/pdf", data.config.buildingTitle)
    } catch (e: Exception) {
      onError(e.localizedMessage ?: "PDF export error")
    }
  }

  fun shareOfficialPosterImage(
    context: Context,
    data: SyndicatDashboardData,
    options: CotisationExportOptions = CotisationExportOptions(),
    onError: (String) -> Unit,
  ) {
    try {
      val bitmap = generateOfficialPosterBitmap(context, data, options, scale = 2.4f)
      val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
      val dateStamp = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
      val outFile = File(exportDir, "Mulsaq_Musahamat_$dateStamp.png")
      FileOutputStream(outFile).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
      bitmap.recycle()

      shareFileUri(context, outFile, "image/png", data.config.buildingTitle)
    } catch (e: Exception) {
      onError(e.localizedMessage ?: "PNG export error")
    }
  }

  // =========================================================================
  // 8.2 AFFICHE DE RELANCE — FORMAT PORTRAIT A4 (595 × 842 pt) & MOBILE
  // =========================================================================

  fun generateRelanceBitmap(
    context: Context,
    data: SyndicatDashboardData,
    options: RelanceExportOptions = RelanceExportOptions(),
    scale: Float = 2f,
  ): Bitmap {
    val sortedLate = data.lateApartments.sortedBy { it.number }
    val lateCount = sortedLate.size.coerceAtLeast(1)
    val pageWidth = A4_PORTRAIT_WIDTH
    val pageHeight = maxOf(A4_PORTRAIT_HEIGHT, 224 + lateCount * 34)
    val force1080 = options.variant == ExportVariant.SCREEN
    val (bitmap, effScale) = createScaledBitmap(pageWidth, pageHeight, scale, force1080)
    val canvas = Canvas(bitmap)
    canvas.scale(effScale, effScale)
    drawRelanceCanvas(
      context = context,
      canvas = canvas,
      pageWidth = pageWidth,
      pageHeight = pageHeight,
      data = data,
      lateSlice = sortedLate,
      options = options,
      pageIndex = 0,
      pageCount = 1,
      showTotalFooter = true,
    )
    return bitmap
  }

  fun shareRelanceListPdf(
    context: Context,
    data: SyndicatDashboardData,
    options: RelanceExportOptions = RelanceExportOptions(),
    onError: (String) -> Unit,
  ) {
    try {
      val strings = stringsForLanguage(data.config.language)
      val sortedLate = data.lateApartments.sortedBy { it.number }
      val chunks = sortedLate
        .chunked(RELANCE_MAX_ROWS_PER_A4_PAGE)
        .ifEmpty { listOf(emptyList()) }

      val document = PdfDocument()
      chunks.forEachIndexed { index, slice ->
        val pageInfo = PdfDocument.PageInfo.Builder(
          A4_PORTRAIT_WIDTH,
          A4_PORTRAIT_HEIGHT,
          index + 1,
        ).create()
        val page = document.startPage(pageInfo)
        drawRelanceCanvas(
          context = context,
          canvas = page.canvas,
          pageWidth = A4_PORTRAIT_WIDTH,
          pageHeight = A4_PORTRAIT_HEIGHT,
          data = data,
          lateSlice = slice,
          options = options,
          pageIndex = index,
          pageCount = chunks.size,
          showTotalFooter = index == chunks.lastIndex,
        )
        document.finishPage(page)
      }

      val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
      val dateStamp = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
      val outFile = File(exportDir, "Mulsaq_Tadkir_$dateStamp.pdf")
      FileOutputStream(outFile).use { document.writeTo(it) }
      document.close()

      shareFileUri(context, outFile, "application/pdf", strings.relanceDocTitle(data.config.residenceName))
    } catch (e: Exception) {
      onError(e.localizedMessage ?: "Relance PDF error")
    }
  }

  fun shareRelanceListImage(
    context: Context,
    data: SyndicatDashboardData,
    options: RelanceExportOptions = RelanceExportOptions(),
    onError: (String) -> Unit,
  ) {
    try {
      val strings = stringsForLanguage(data.config.language)
      val bitmap = generateRelanceBitmap(context, data, options, scale = 2.4f)
      val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
      val dateStamp = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
      val outFile = File(exportDir, "Mulsaq_Tadkir_$dateStamp.png")
      FileOutputStream(outFile).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
      bitmap.recycle()

      shareFileUri(context, outFile, "image/png", strings.relanceDocTitle(data.config.residenceName))
    } catch (e: Exception) {
      onError(e.localizedMessage ?: "Relance PNG error")
    }
  }

  // =========================================================================
  // 8.3 SITUATION DES DÉPENSES & ÉTAT DE CAISSE — PORTRAIT A4 (595 × 842 pt)
  // =========================================================================

  fun generateCaisseReportBitmap(
    context: Context,
    data: SyndicatDashboardData,
    inkSaver: Boolean = false,
    scale: Float = 2f,
    variant: ExportVariant = ExportVariant.PRINT,
    monochromeMode: Boolean = false,
  ): Bitmap {
    val expCount = data.expenses.size.coerceAtLeast(1)
    val catRows = ((data.expenses.map { it.category }.distinct().size + 1) / 2).coerceAtLeast(1)
    val pageWidth = A4_PORTRAIT_WIDTH
    val pageHeight = maxOf(A4_PORTRAIT_HEIGHT, 325 + expCount * 34 + catRows * 26 + 44)
    val force1080 = variant == ExportVariant.SCREEN
    val (bitmap, effScale) = createScaledBitmap(pageWidth, pageHeight, scale, force1080)
    val canvas = Canvas(bitmap)
    canvas.scale(effScale, effScale)
    drawCaisseReportCanvas(
      context = context,
      canvas = canvas,
      pageWidth = pageWidth,
      pageHeight = pageHeight,
      data = data,
      expensesSlice = data.expenses,
      inkSaver = inkSaver,
      showKpiHeader = true,
      showCategoryBreakdown = true,
      pageIndex = 0,
      pageCount = 1,
      monochromeMode = monochromeMode,
    )
    return bitmap
  }

  fun shareCaisseReportPdf(
    context: Context,
    data: SyndicatDashboardData,
    inkSaver: Boolean = false,
    onError: (String) -> Unit,
    @Suppress("UNUSED_PARAMETER") variant: ExportVariant = ExportVariant.PRINT,
    monochromeMode: Boolean = false,
  ) {
    try {
      val strings = stringsForLanguage(data.config.language)
      val allExpenses = data.expenses
      val pages = mutableListOf<List<ExpenseRecord>>()
      if (allExpenses.size <= CAISSE_FIRST_PAGE_MAX_EXPENSES) {
        pages.add(allExpenses)
      } else {
        pages.add(allExpenses.take(CAISSE_FIRST_PAGE_MAX_EXPENSES))
        allExpenses
          .drop(CAISSE_FIRST_PAGE_MAX_EXPENSES)
          .chunked(CAISSE_NEXT_PAGE_MAX_EXPENSES)
          .forEach { pages.add(it) }
      }

      val document = PdfDocument()
      pages.forEachIndexed { index, slice ->
        val pageInfo = PdfDocument.PageInfo.Builder(
          A4_PORTRAIT_WIDTH,
          A4_PORTRAIT_HEIGHT,
          index + 1,
        ).create()
        val page = document.startPage(pageInfo)
        drawCaisseReportCanvas(
          context = context,
          canvas = page.canvas,
          pageWidth = A4_PORTRAIT_WIDTH,
          pageHeight = A4_PORTRAIT_HEIGHT,
          data = data,
          expensesSlice = slice,
          inkSaver = inkSaver,
          showKpiHeader = index == 0,
          showCategoryBreakdown = index == pages.lastIndex,
          pageIndex = index,
          pageCount = pages.size,
          monochromeMode = monochromeMode,
        )
        document.finishPage(page)
      }

      val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
      val dateStamp = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
      val outFile = File(exportDir, "Wadiyat_Sandouq_$dateStamp.pdf")
      FileOutputStream(outFile).use { document.writeTo(it) }
      document.close()

      shareFileUri(context, outFile, "application/pdf", strings.caisseDocTitle(data.config.residenceName, data.config.year))
    } catch (e: Exception) {
      onError(e.localizedMessage ?: "Caisse PDF error")
    }
  }

  fun shareCaisseReportImage(
    context: Context,
    data: SyndicatDashboardData,
    inkSaver: Boolean = false,
    onError: (String) -> Unit,
    variant: ExportVariant = ExportVariant.PRINT,
    monochromeMode: Boolean = false,
  ) {
    try {
      val strings = stringsForLanguage(data.config.language)
      val bitmap = generateCaisseReportBitmap(context, data, inkSaver, scale = 2.4f, variant = variant, monochromeMode = monochromeMode)
      val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
      val dateStamp = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
      val outFile = File(exportDir, "Wadiyat_Sandouq_$dateStamp.png")
      FileOutputStream(outFile).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
      bitmap.recycle()

      shareFileUri(context, outFile, "image/png", strings.caisseDocTitle(data.config.residenceName, data.config.year))
    } catch (e: Exception) {
      onError(e.localizedMessage ?: "Caisse PNG error")
    }
  }

  // =========================================================================
  // 8.4 ÉTAT D'AVANCEMENT DES PROJETS — PORTRAIT A4 (595 × 842 pt)
  // =========================================================================

  private const val PROJECTS_MAX_ROWS_PER_A4_PAGE = 8

  fun generateProjectsReportBitmap(
    context: Context,
    data: SyndicatDashboardData,
    options: ProjectsExportOptions = ProjectsExportOptions(),
    scale: Float = 2f,
  ): Bitmap {
    val allProjects = sortProjectsByPriority(data.projects)
    val projCount = allProjects.size.coerceAtLeast(1)
    val pageWidth = A4_PORTRAIT_WIDTH
    val pageHeight = maxOf(A4_PORTRAIT_HEIGHT, 240 + projCount * 62 + 60)
    val force1080 = options.variant == ExportVariant.SCREEN
    val (bitmap, effScale) = createScaledBitmap(pageWidth, pageHeight, scale, force1080)
    val canvas = Canvas(bitmap)
    canvas.scale(effScale, effScale)
    drawProjectsReportCanvas(
      context = context,
      canvas = canvas,
      pageWidth = pageWidth,
      pageHeight = pageHeight,
      data = data,
      projectsSlice = allProjects,
      options = options,
      pageIndex = 0,
      pageCount = 1,
      showSummaryHeader = true,
      showSummaryFooter = true,
    )
    return bitmap
  }

  fun shareProjectsReportPdf(
    context: Context,
    data: SyndicatDashboardData,
    options: ProjectsExportOptions = ProjectsExportOptions(),
    onError: (String) -> Unit,
  ) {
    try {
      val strings = stringsForLanguage(data.config.language)
      val allProjects: List<ProjectRecord> = sortProjectsByPriority(data.projects)
      val pages: List<List<ProjectRecord>> = allProjects
        .chunked(PROJECTS_MAX_ROWS_PER_A4_PAGE)
        .ifEmpty { listOf(emptyList<ProjectRecord>()) }

      val document = PdfDocument()
      pages.forEachIndexed { index, slice ->
        val pageInfo = PdfDocument.PageInfo.Builder(
          A4_PORTRAIT_WIDTH,
          A4_PORTRAIT_HEIGHT,
          index + 1,
        ).create()
        val page = document.startPage(pageInfo)
        drawProjectsReportCanvas(
          context = context,
          canvas = page.canvas,
          pageWidth = A4_PORTRAIT_WIDTH,
          pageHeight = A4_PORTRAIT_HEIGHT,
          data = data,
          projectsSlice = slice,
          options = options,
          pageIndex = index,
          pageCount = pages.size,
          showSummaryHeader = index == 0,
          showSummaryFooter = index == pages.lastIndex,
        )
        document.finishPage(page)
      }

      val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
      val dateStamp = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
      val outFile = File(exportDir, "Mashari_AlImara_$dateStamp.pdf")
      FileOutputStream(outFile).use { document.writeTo(it) }
      document.close()

      shareFileUri(context, outFile, "application/pdf", strings.projectsDocTitle(data.config.residenceName, data.config.year))
    } catch (e: Exception) {
      onError(e.localizedMessage ?: "Projects PDF error")
    }
  }

  fun shareProjectsReportImage(
    context: Context,
    data: SyndicatDashboardData,
    options: ProjectsExportOptions = ProjectsExportOptions(),
    onError: (String) -> Unit,
  ) {
    try {
      val strings = stringsForLanguage(data.config.language)
      val bitmap = generateProjectsReportBitmap(context, data, options, scale = 2.4f)
      val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
      val dateStamp = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
      val outFile = File(exportDir, "Mashari_AlImara_$dateStamp.png")
      FileOutputStream(outFile).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
      bitmap.recycle()

      shareFileUri(context, outFile, "image/png", strings.projectsDocTitle(data.config.residenceName, data.config.year))
    } catch (e: Exception) {
      onError(e.localizedMessage ?: "Projects PNG error")
    }
  }

  /**
   * Génère le Pack « Bilan d'Assemblée Générale (AG) » en 1 PDF multi-pages :
   * 1) Tableau des Cotisations
   * 2) Situation de Caisse & Dépenses
   * 3) État d'avancement des Projets
   * 4) Liste des Relances / Impayés
   */
  fun shareAnnualAgPackPdf(
    context: Context,
    data: SyndicatDashboardData,
    monochromeMode: Boolean = false,
    inkSaver: Boolean = false,
    onError: (String) -> Unit,
  ) {
    try {
      val cfg = data.config
      val activeCols = cfg.activeMonthColumns
      val cotChunks = data.apartments
        .chunked(COTISATION_MAX_ROWS_PER_A4_PAGE)
        .ifEmpty { listOf(emptyList()) }

      val allExpenses = data.expenses
      val caisseChunks = mutableListOf<List<ExpenseRecord>>()
      if (allExpenses.size <= CAISSE_FIRST_PAGE_MAX_EXPENSES) {
        caisseChunks.add(allExpenses)
      } else {
        caisseChunks.add(allExpenses.take(CAISSE_FIRST_PAGE_MAX_EXPENSES))
        allExpenses
          .drop(CAISSE_FIRST_PAGE_MAX_EXPENSES)
          .chunked(CAISSE_NEXT_PAGE_MAX_EXPENSES)
          .forEach { caisseChunks.add(it) }
      }

      val allProjects = sortProjectsByPriority(data.projects)
      val projectChunks = allProjects
        .chunked(PROJECTS_MAX_ROWS_PER_A4_PAGE)
        .ifEmpty { listOf(emptyList()) }

      val sortedLate = data.lateApartments.sortedBy { it.number }
      val relanceChunks = if (sortedLate.isNotEmpty()) {
        sortedLate.chunked(RELANCE_MAX_ROWS_PER_A4_PAGE)
      } else {
        emptyList()
      }

      val cotOpts = CotisationExportOptions(
        showNames = true,
        untilCutoffOnly = false,
        inkSaver = inkSaver,
        monochromeMode = monochromeMode,
        variant = ExportVariant.PRINT,
      )
      val projOpts = ProjectsExportOptions(
        inkSaver = inkSaver,
        monochromeMode = monochromeMode,
        variant = ExportVariant.PRINT,
      )
      val relOpts = RelanceExportOptions(
        anonymousMode = false,
        inkSaver = inkSaver,
        monochromeMode = monochromeMode,
        variant = ExportVariant.PRINT,
      )

      val document = PdfDocument()
      var pageNumber = 1

      cotChunks.forEachIndexed { idx, slice ->
        val page = document.startPage(
          PdfDocument.PageInfo.Builder(A4_PORTRAIT_WIDTH, A4_PORTRAIT_HEIGHT, pageNumber++).create()
        )
        drawCotisationPosterCanvas(
          context = context,
          canvas = page.canvas,
          pageWidth = A4_PORTRAIT_WIDTH,
          pageHeight = A4_PORTRAIT_HEIGHT,
          data = data,
          apartmentsSlice = slice,
          activeCols = activeCols,
          options = cotOpts,
          pageIndex = idx,
          pageCount = cotChunks.size,
          showGrandFooter = idx == cotChunks.lastIndex,
        )
        document.finishPage(page)
      }

      caisseChunks.forEachIndexed { idx, slice ->
        val page = document.startPage(
          PdfDocument.PageInfo.Builder(A4_PORTRAIT_WIDTH, A4_PORTRAIT_HEIGHT, pageNumber++).create()
        )
        drawCaisseReportCanvas(
          context = context,
          canvas = page.canvas,
          pageWidth = A4_PORTRAIT_WIDTH,
          pageHeight = A4_PORTRAIT_HEIGHT,
          data = data,
          expensesSlice = slice,
          inkSaver = inkSaver,
          showKpiHeader = idx == 0,
          showCategoryBreakdown = idx == caisseChunks.lastIndex,
          pageIndex = idx,
          pageCount = caisseChunks.size,
          monochromeMode = monochromeMode,
        )
        document.finishPage(page)
      }

      projectChunks.forEachIndexed { idx, slice ->
        val page = document.startPage(
          PdfDocument.PageInfo.Builder(A4_PORTRAIT_WIDTH, A4_PORTRAIT_HEIGHT, pageNumber++).create()
        )
        drawProjectsReportCanvas(
          context = context,
          canvas = page.canvas,
          pageWidth = A4_PORTRAIT_WIDTH,
          pageHeight = A4_PORTRAIT_HEIGHT,
          data = data,
          projectsSlice = slice,
          options = projOpts,
          pageIndex = idx,
          pageCount = projectChunks.size,
          showSummaryHeader = idx == 0,
          showSummaryFooter = idx == projectChunks.lastIndex,
        )
        document.finishPage(page)
      }

      relanceChunks.forEachIndexed { idx, slice ->
        val page = document.startPage(
          PdfDocument.PageInfo.Builder(A4_PORTRAIT_WIDTH, A4_PORTRAIT_HEIGHT, pageNumber++).create()
        )
        drawRelanceCanvas(
          context = context,
          canvas = page.canvas,
          pageWidth = A4_PORTRAIT_WIDTH,
          pageHeight = A4_PORTRAIT_HEIGHT,
          data = data,
          lateSlice = slice,
          options = relOpts,
          pageIndex = idx,
          pageCount = relanceChunks.size,
          showTotalFooter = idx == relanceChunks.lastIndex,
        )
        document.finishPage(page)
      }

      val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
      val dateStamp = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
      val outFile = File(exportDir, "Bilan_AG_${cfg.year}_$dateStamp.pdf")
      FileOutputStream(outFile).use { document.writeTo(it) }
      document.close()

      val strings = stringsForLanguage(cfg.language)
      shareFileUri(context, outFile, "application/pdf", "${strings.exportCardAgPackTitle} – ${cfg.residenceName}")
    } catch (e: Exception) {
      onError(e.localizedMessage ?: "AG Pack PDF error")
    }
  }

  /**
   * Génère l'aperçu haute netteté de la 1re feuille du Pack Bilan d'Assemblée Générale (AG).
   */
  fun generateAgPackPreviewBitmap(
    context: Context,
    data: SyndicatDashboardData,
    monochromeMode: Boolean = false,
    inkSaver: Boolean = false,
    scale: Float = 2f,
  ): Bitmap {
    val firstSlice = data.apartments.take(COTISATION_MAX_ROWS_PER_A4_PAGE)
    val pageCount = data.apartments.chunked(COTISATION_MAX_ROWS_PER_A4_PAGE).size.coerceAtLeast(1)
    val pageWidth = A4_PORTRAIT_WIDTH
    val pageHeight = A4_PORTRAIT_HEIGHT
    val (bitmap, effScale) = createScaledBitmap(pageWidth, pageHeight, scale, forceScreen1080Px = false)
    val canvas = Canvas(bitmap)
    canvas.scale(effScale, effScale)
    drawCotisationPosterCanvas(
      context = context,
      canvas = canvas,
      pageWidth = pageWidth,
      pageHeight = pageHeight,
      data = data,
      apartmentsSlice = firstSlice,
      activeCols = data.config.activeMonthColumns,
      options = CotisationExportOptions(
        showNames = true,
        untilCutoffOnly = false,
        inkSaver = inkSaver,
        monochromeMode = monochromeMode,
        variant = ExportVariant.PRINT,
      ),
      pageIndex = 0,
      pageCount = pageCount,
      showGrandFooter = pageCount == 1,
    )
    return bitmap
  }

  private fun drawProjectsReportCanvas(
    context: Context,
    canvas: Canvas,
    pageWidth: Int,
    @Suppress("UNUSED_PARAMETER") pageHeight: Int,
    data: SyndicatDashboardData,
    projectsSlice: List<com.example.data.model.ProjectRecord>,
    options: ProjectsExportOptions,
    pageIndex: Int,
    pageCount: Int,
    showSummaryHeader: Boolean,
    showSummaryFooter: Boolean,
  ) {
    val cfg = data.config
    val strings = stringsForLanguage(cfg.language)
    val isRtl = strings.language == AppLanguage.AR
    val cur = cfg.currency
    val pal = buildPalette(context, inkSaver = options.inkSaver, monochromeMode = options.monochromeMode)

    canvas.drawColor(pal.pageBg)
    val margin = 22f
    val contentW = pageWidth - margin * 2
    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.STROKE
      strokeWidth = 1.0f
      color = pal.hairline
    }

    // En-tête titre A4
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = pal.ink
      textSize = 17f
      typeface = pal.tfRegular
      textAlign = Paint.Align.CENTER
    }
    val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = pal.mute
      textSize = 9.6f
      typeface = pal.tfRegular
      textAlign = Paint.Align.CENTER
    }
    val pageSuffix = if (pageCount > 1) " (${bidiLtr("${pageIndex + 1}/$pageCount")})" else ""
    val titleStr = ellipsizeToWidth(strings.projectsDocTitle(cfg.residenceName, cfg.year) + pageSuffix, titlePaint, contentW)
    canvas.drawText(rtlAware(titleStr, isRtl), pageWidth / 2f, 38f, titlePaint)

    val today = localizeDigits(SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Date()), cfg.digitsAr)
    val subLine = ellipsizeToWidth(
      buildList {
        add("${strings.datePrefix} $today")
        if (cfg.treasurerName.isNotBlank()) add(cfg.treasurerName)
      }.joinToString("  ·  "),
      subPaint,
      contentW,
    )
    canvas.drawText(rtlAware(subLine, isRtl), pageWidth / 2f, 56f, subPaint)
    canvas.drawLine(margin, 66f, pageWidth - margin, 66f, strokePaint)

    var y = 76f

    if (showSummaryHeader) {
      // 3 KPI Tiles côte à côte : Caisse disponible / En cours / Prêts à démarrer
      val tileCount = 3
      val gap = 8f
      val tileW = (contentW - gap * (tileCount - 1)) / tileCount

      // Tile 0 : Caisse actuelle
      val t0Left = if (isRtl) margin + (tileW + gap) * 2 else margin
      val r0 = RectF(t0Left, y, t0Left + tileW, y + 46f)
      fillPaint.color = if (options.monochromeMode || options.inkSaver) pal.cardBg else pal.caisseBandBg
      canvas.drawRoundRect(r0, 8f, 8f, fillPaint)
      canvas.drawRoundRect(r0, 8f, 8f, strokePaint)
      val kpiP0 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = if (options.monochromeMode || options.inkSaver) pal.body else Color.WHITE
        textSize = 8.8f
        typeface = pal.tfRegular
        textAlign = Paint.Align.CENTER
      }
      val valP0 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = if (options.monochromeMode || options.inkSaver) pal.ink else Color.WHITE
        textSize = 13.5f
        typeface = pal.tfNumeric
        textAlign = Paint.Align.CENTER
      }
      canvas.drawText(rtlAware(strings.projectsKpiCaisseLabel, isRtl), r0.centerX(), y + 16f, kpiP0)
      canvas.drawText(rtlAware(formatDh(data.cashNowDh, cur, cfg.digitsAr), isRtl), r0.centerX(), y + 36f, valP0)

      // Tile 1 : En cours
      val t1Left = margin + tileW + gap
      val r1 = RectF(t1Left, y, t1Left + tileW, y + 46f)
      fillPaint.color = if (options.monochromeMode || options.inkSaver) pal.cardBg else pal.headerBg
      canvas.drawRoundRect(r1, 8f, 8f, fillPaint)
      canvas.drawRoundRect(r1, 8f, 8f, strokePaint)
      val kpiP1 = Paint(kpiP0).apply { color = pal.body }
      val valP1 = Paint(valP0).apply { color = pal.ink }
      canvas.drawText(rtlAware(strings.projectsKpiOngoingLabel, isRtl), r1.centerX(), y + 16f, kpiP1)
      val ongoingCount = data.inProgressProjects.size
      canvas.drawText(bidiLtr(formatNumber(ongoingCount.toLong(), cfg.digitsAr)), r1.centerX(), y + 36f, valP1)

      // Tile 2 : Prêts
      val t2Left = if (isRtl) margin else margin + (tileW + gap) * 2
      val r2 = RectF(t2Left, y, t2Left + tileW, y + 46f)
      fillPaint.color = if (options.monochromeMode || options.inkSaver) pal.cardBg else pal.headerBg
      canvas.drawRoundRect(r2, 8f, 8f, fillPaint)
      canvas.drawRoundRect(r2, 8f, 8f, strokePaint)
      canvas.drawText(rtlAware(strings.projectsKpiReadyLabel, isRtl), r2.centerX(), y + 16f, kpiP1)
      canvas.drawText(bidiLtr(formatNumber(data.readyToStartProjectsCount.toLong(), cfg.digitsAr)), r2.centerX(), y + 36f, valP1)

      y += 56f
    }

    if (projectsSlice.isEmpty()) {
      val emptyH = 40f
      fillPaint.color = pal.cardBg
      canvas.drawRect(margin, y, margin + contentW, y + emptyH, fillPaint)
      canvas.drawRect(margin, y, margin + contentW, y + emptyH, strokePaint)
      val emptyP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = pal.mute
        textSize = 10f
        typeface = pal.tfRegular
        textAlign = Paint.Align.CENTER
      }
      canvas.drawText(rtlAware(strings.projectsEmptyTitle, isRtl), pageWidth / 2f, y + 25f, emptyP)
      y += emptyH
    } else {
      val cardH = 56f
      val cardPad = 8f
      val readinessMap = data.plannedReadinessList.associateBy { it.project.id }

      projectsSlice.forEachIndexed { _, proj ->
        val cardRect = RectF(margin, y, margin + contentW, y + cardH)
        fillPaint.color = pal.cardBg
        canvas.drawRoundRect(cardRect, 6f, 6f, fillPaint)
        canvas.drawRoundRect(cardRect, 6f, 6f, strokePaint)

        // Badge Statut & Priorité
        val statusText = strings.projectStatusLabel(proj.parsedStatus)
        val prioText = strings.projectPriorityShort(proj.parsedPriority)
        val catText = strings.categoryLabel(proj.category)
        val titleText = proj.displayTitle(isRtl)

        val titleP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          color = pal.ink
          textSize = 11.2f
          typeface = pal.tfMedium
          textAlign = if (isRtl) Paint.Align.RIGHT else Paint.Align.LEFT
        }
        val metaP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          color = pal.mute
          textSize = 8.6f
          typeface = pal.tfRegular
          textAlign = if (isRtl) Paint.Align.RIGHT else Paint.Align.LEFT
        }
        val valNumP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          color = pal.ink
          textSize = 10.5f
          typeface = pal.tfNumeric
          textAlign = if (isRtl) Paint.Align.LEFT else Paint.Align.RIGHT
        }

        val textStartX = if (isRtl) margin + contentW - 12f else margin + 12f
        val numbersX = if (isRtl) margin + 12f else margin + contentW - 12f

        // Ligne 1 : Titre du projet
        val leftAvailW = contentW * 0.58f
        val fittedTitle = ellipsizeToWidth(titleText, titleP, leftAvailW)
        canvas.drawText(rtlAware(fittedTitle, isRtl), textStartX, y + 18f, titleP)

        // Ligne 2 : Statut · Priorité · Catégorie
        val metaCombined = "$statusText  ·  $prioText  ·  $catText"
        val fittedMeta = ellipsizeToWidth(metaCombined, metaP, leftAvailW)
        canvas.drawText(rtlAware(fittedMeta, isRtl), textStartX, y + 33f, metaP)

        // Ligne 3 : Barre de progression (hauteur 3pt)
        val barTop = y + 42f
        val barW = leftAvailW
        val barLeft = if (isRtl) textStartX - barW else textStartX
        val barBgRect = RectF(barLeft, barTop, barLeft + barW, barTop + 4f)
        fillPaint.color = pal.hairline
        canvas.drawRoundRect(barBgRect, 2f, 2f, fillPaint)
        val pct = proj.clampedProgressPercent.coerceIn(0, 100) / 100f
        if (pct > 0f) {
          val barFillRect = if (isRtl) {
            RectF(barLeft + barW * (1f - pct), barTop, barLeft + barW, barTop + 4f)
          } else {
            RectF(barLeft, barTop, barLeft + barW * pct, barTop + 4f)
          }
          fillPaint.color = if (options.monochromeMode) pal.ink else pal.paidFg
          canvas.drawRoundRect(barFillRect, 2f, 2f, fillPaint)
        }

        // Côté chiffres : Budget total et Avance / Reste
        val budgetLabel = "${strings.projectTotalBudgetLabel}: ${formatDh(proj.targetBudgetDh, cur, cfg.digitsAr)}"
        canvas.drawText(rtlAware(ellipsizeToWidth(budgetLabel, valNumP, contentW * 0.38f), isRtl), numbersX, y + 18f, valNumP)

        val detailP = Paint(metaP).apply {
          textAlign = if (isRtl) Paint.Align.LEFT else Paint.Align.RIGHT
          color = if (proj.parsedStatus == com.example.data.model.ProjectStatus.COMPLETED) {
            pal.paidFg
          } else if (readinessMap[proj.id]?.canStartNowDirect == true) {
            if (options.monochromeMode) pal.ink else pal.paidFg
          } else {
            pal.body
          }
        }
        val detailLine = when (proj.parsedStatus) {
          com.example.data.model.ProjectStatus.COMPLETED -> "${strings.projectProgressStateLabel}: 100% ✓"
          com.example.data.model.ProjectStatus.IN_PROGRESS -> "${strings.projectProgressStateLabel}: ${proj.clampedProgressPercent}% · ${formatDh(proj.allocatedAmountDh, cur, cfg.digitsAr)}"
          else -> {
            val r = readinessMap[proj.id]
            if (r != null && r.canStartNowDirect) {
              strings.projectReadyDirectBadge
            } else {
              "${strings.projectRequiredAdvanceLabel}: ${formatDh(proj.effectiveMinCashToStartDh, cur, cfg.digitsAr)}"
            }
          }
        }
        canvas.drawText(rtlAware(ellipsizeToWidth(detailLine, detailP, contentW * 0.38f), isRtl), numbersX, y + 34f, detailP)

        y += cardH + cardPad
      }
    }

    if (showSummaryFooter) {
      y += 6f
      val footRect = RectF(margin, y, margin + contentW, y + 30f)
      fillPaint.color = pal.headerBg
      canvas.drawRoundRect(footRect, 8f, 8f, fillPaint)
      canvas.drawRoundRect(footRect, 8f, 8f, strokePaint)

      val footPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = pal.ink
        textSize = 9.8f
        typeface = pal.tfMedium
        textAlign = Paint.Align.CENTER
      }
      val totalBudget = data.projects.sumOf { it.targetBudgetDh }
      val footText = ellipsizeToWidth(
        "${strings.projectsPageTitle} (${data.projects.size})   |   ${strings.projectTotalBudgetLabel} : ${formatDh(totalBudget, cur, cfg.digitsAr)}",
        footPaint,
        contentW - 16f,
      )
      canvas.drawText(rtlAware(footText, isRtl), pageWidth / 2f, y + 19f, footPaint)
    }
  }

  // =========================================================================
  // REÇU DE PAIEMENT NUMÉROTÉ — FORMAT PORTRAIT (480 × 680 pt)
  // =========================================================================

  fun generatePaymentReceiptBitmap(
    context: Context,
    apartment: ApartmentRecord,
    data: SyndicatDashboardData,
    receiptNo: Long,
    scale: Float = 2.0f,
    variant: ExportVariant = ExportVariant.SCREEN,
    payment: PaymentRecord? = null,
    monochromeMode: Boolean = false,
  ): Bitmap {
    val w = RECEIPT_PORTRAIT_WIDTH
    val h = RECEIPT_PORTRAIT_HEIGHT
    val force1080 = variant == ExportVariant.SCREEN
    val (bitmap, effScale) = createScaledBitmap(w, h, scale = scale, forceScreen1080Px = force1080)
    val canvas = Canvas(bitmap)
    canvas.scale(effScale, effScale)
    drawPaymentReceiptCanvas(context, canvas, w, h, apartment, data, receiptNo, payment, monochromeMode = monochromeMode)
    return bitmap
  }

  fun sharePaymentReceiptImage(
    context: Context,
    apartment: ApartmentRecord,
    data: SyndicatDashboardData,
    receiptNo: Long,
    onError: (String) -> Unit,
    variant: ExportVariant = ExportVariant.SCREEN,
    payment: PaymentRecord? = null,
    monochromeMode: Boolean = false,
  ) {
    try {
      val strings = stringsForLanguage(data.config.language)
      val effReceiptNo = payment?.receiptNumber?.takeIf { it > 0L } ?: receiptNo
      val bitmap = generatePaymentReceiptBitmap(
        context = context,
        apartment = apartment,
        data = data,
        receiptNo = effReceiptNo,
        scale = 2.2f,
        variant = variant,
        payment = payment,
        monochromeMode = monochromeMode,
      )
      val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
      val yearSuffix = payment?.year ?: data.config.year
      val outFile = File(exportDir, "Recu_Appt_${apartment.displayNumber}_${yearSuffix}_No$effReceiptNo.png")
      FileOutputStream(outFile).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
      bitmap.recycle()

      shareFileUri(context, outFile, "image/png", "${strings.receiptDocTitle} #${"%03d".format(effReceiptNo)}")
    } catch (e: Exception) {
      onError(e.localizedMessage ?: "Receipt export error")
    }
  }

  fun sharePaymentReceiptViaWhatsApp(
    context: Context,
    apartment: ApartmentRecord,
    data: SyndicatDashboardData,
    receiptNo: Long,
    onError: (String) -> Unit,
    variant: ExportVariant = ExportVariant.SCREEN,
    payment: PaymentRecord? = null,
    monochromeMode: Boolean = false,
  ) {
    try {
      val cfg = data.config
      val strings = stringsForLanguage(cfg.language)
      val isAr = strings.language == AppLanguage.AR
      val effReceiptNo = payment?.receiptNumber?.takeIf { it > 0L } ?: receiptNo
      val bitmap = generatePaymentReceiptBitmap(
        context = context,
        apartment = apartment,
        data = data,
        receiptNo = effReceiptNo,
        scale = 2.2f,
        variant = variant,
        payment = payment,
        monochromeMode = monochromeMode,
      )
      val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
      val yearSuffix = payment?.year ?: cfg.year
      val outFile = File(exportDir, "Recu_WA_Appt_${apartment.displayNumber}_${yearSuffix}_No$effReceiptNo.png")
      FileOutputStream(outFile).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
      bitmap.recycle()

      val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outFile)
      val paidAmt = payment?.amountDh ?: apartment.totalForConfig(cfg)
      val owner = apartment.displayOwner(isAr)
      val caption = if (isAr) {
        buildString {
          appendLine("✅ ${strings.receiptDocTitle} #${"%03d".format(effReceiptNo)}")
          appendLine("🏢 ${cfg.residenceName} · الشقة ${apartment.displayNumber}${if (owner.isNotBlank()) " ($owner)" else ""}")
          append("💰 المبلغ المؤدى: ${formatDh(paidAmt, cfg.currency, cfg.digitsAr)}")
        }
      } else {
        buildString {
          appendLine("✅ ${strings.receiptDocTitle} #${"%03d".format(effReceiptNo)}")
          appendLine("🏢 ${cfg.residenceName} · Appt ${apartment.displayNumber}${if (owner.isNotBlank()) " ($owner)" else ""}")
          append("💰 Montant réglé : ${formatDh(paidAmt, cfg.currency, cfg.digitsAr)}")
        }
      }

      val waPackages = listOf("com.whatsapp", "com.whatsapp.w4b")
      var launched = false
      for (pkg in waPackages) {
        try {
          val waIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            setPackage(pkg)
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, caption)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          }
          context.startActivity(waIntent)
          launched = true
          break
        } catch (_: Exception) {
        }
      }
      if (!launched) {
        val fallback = Intent(Intent.ACTION_SEND).apply {
          type = "image/png"
          putExtra(Intent.EXTRA_STREAM, uri)
          putExtra(Intent.EXTRA_TEXT, caption)
          addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(fallback, "${strings.receiptDocTitle} #${"%03d".format(effReceiptNo)}").apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
      }
    } catch (e: Exception) {
      onError(e.localizedMessage ?: "WhatsApp receipt error")
    }
  }

  fun shareWhatsAppSummary(context: Context, data: SyndicatDashboardData) {
    val cfg = data.config
    val strings = stringsForLanguage(cfg.language)
    val isAr = strings.language == AppLanguage.AR
    val cur = cfg.currency
    val today = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Date())

    val summary = if (isAr) {
      val todayAr = bidiLtr(localizeDigits(today, cfg.digitsAr))
      buildString {
        appendLine("🏢 ${cfg.residenceName}")
        if (cfg.city.isNotBlank() || cfg.address.isNotBlank()) {
          appendLine("📍 ${listOf(cfg.address, cfg.city).filter { it.isNotBlank() }.joinToString(" - ")}")
        }
        appendLine("📅 الوضعية بتاريخ $todayAr")
        appendLine("━━━━━━━━━━━━━━")
        appendLine("• المتوفر في الصندوق الآن: ${formatDh(data.cashNowDh, cur, cfg.digitsAr)}")
        appendLine("• المداخيل المحصلة: ${formatDh(data.totalCaisseDh, cur, cfg.digitsAr)}")
        appendLine("• المصاريف: ${formatDh(data.totalExpensesDh, cur, cfg.digitsAr)}")
        appendLine("• نسبة التحصيل: ${bidiLtr("${localizeDigits(data.collectionRatePercent.toString(), cfg.digitsAr)}%")}")
        appendLine("• الشقق المؤدية: ✓ ${bidiLtr("${localizeDigits(data.upToDateCount.toString(), cfg.digitsAr)}/${localizeDigits(data.occupiedCount.toString(), cfg.digitsAr)}")}")
        if (data.lateApartments.isNotEmpty()) {
          appendLine("• ! المتبقي للتحصيل: ${formatDh(data.totalRemainingToCollectDh, cur, cfg.digitsAr)} (${formatNumber(data.lateApartments.size.toLong(), cfg.digitsAr)} شقق)")
        }
      }
    } else {
      buildString {
        appendLine("🏢 ${cfg.residenceName}")
        appendLine("📅 Situation au $today")
        appendLine("━━━━━━━━━━━━━━")
        appendLine("• En caisse maintenant : ${formatDh(data.cashNowDh, cur, cfg.digitsAr)}")
        appendLine("• Total encaissé : ${formatDh(data.totalCaisseDh, cur, cfg.digitsAr)}")
        appendLine("• Total dépenses : ${formatDh(data.totalExpensesDh, cur, cfg.digitsAr)}")
        appendLine("• Recouvrement : ${data.collectionRatePercent}%")
        appendLine("• À jour : ✓ ${data.upToDateCount}/${data.occupiedCount} appts")
      }
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
      type = "text/plain"
      putExtra(Intent.EXTRA_TEXT, summary)
    }
    val chooser = Intent.createChooser(intent, cfg.syndicatName).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(chooser)
  }

  // =========================================================================
  // CANVAS RENDERERS — PORTRAIT A4 (§7 : style éditorial « الأبيض الهادئ »)
  // =========================================================================

  private fun drawCotisationPosterCanvas(
    context: Context,
    canvas: Canvas,
    pageWidth: Int,
    pageHeight: Int,
    data: SyndicatDashboardData,
    apartmentsSlice: List<ApartmentRecord>,
    activeCols: List<MonthColumn>,
    options: CotisationExportOptions,
    pageIndex: Int,
    pageCount: Int,
    showGrandFooter: Boolean,
  ) {
    val cfg = data.config
    val strings = stringsForLanguage(cfg.language)
    val isRtl = strings.language == AppLanguage.AR
    val cur = cfg.currency
    val pal = buildPalette(context, inkSaver = options.inkSaver, monochromeMode = options.monochromeMode)

    canvas.drawColor(pal.pageBg)
    val margin = 20f
    val contentW = pageWidth - margin * 2

    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.STROKE
      strokeWidth = 1.0f
      color = pal.hairline
    }
    val floorDividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.STROKE
      strokeWidth = 1.5f
      color = pal.floorLine
    }

    // §7 Affiche de cotisation : Titre de l'immeuble en title (400) sur fond blanc, séparé par hairline
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = pal.ink
      textSize = 16.5f
      typeface = pal.tfRegular
      textAlign = Paint.Align.CENTER
    }
    val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = pal.mute
      textSize = 9.5f
      typeface = pal.tfRegular
      textAlign = Paint.Align.CENTER
    }

    val pageSuffix = if (pageCount > 1) " (${bidiLtr("${pageIndex + 1}/$pageCount")})" else ""
    val headerTitle = ellipsizeToWidth(cfg.buildingTitle + pageSuffix, titlePaint, contentW)
    canvas.drawText(rtlAware(headerTitle, isRtl), pageWidth / 2f, 34f, titlePaint)

    val today = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Date())
    val metaParts = buildList {
      if (cfg.address.isNotBlank()) add(cfg.address)
      if (cfg.city.isNotBlank()) add(cfg.city)
      if (cfg.treasurerName.isNotBlank()) add(cfg.treasurerName)
      add("${strings.datePrefix} ${bidiLtr(localizeDigits(today, cfg.digitsAr))}")
    }
    val metaLine = ellipsizeToWidth(metaParts.joinToString("  ·  "), subPaint, contentW)
    canvas.drawText(rtlAware(metaLine, isRtl), pageWidth / 2f, 52f, subPaint)
    canvas.drawLine(margin, 62f, pageWidth - margin, 62f, strokePaint)

    // Colonnes optimisées pour la largeur Portrait A4 (555 pt utiles)
    val monthCount = activeCols.size.coerceAtLeast(1)
    val isDenseMonths = monthCount > 8
    val floorW = if (isDenseMonths) 34f else 44f
    val apptW = if (isDenseMonths) 25f else 32f
    val ownerW = when {
      !options.showNames -> 0f
      isDenseMonths -> 86f
      else -> 108f
    }
    val totalW = if (isDenseMonths) 42f else 50f
    val monthW = (contentW - floorW - apptW - ownerW - totalW) / monthCount

    val orderedWidths = buildList {
      add(floorW)
      add(apptW)
      if (options.showNames) add(ownerW)
      activeCols.forEach { _ -> add(monthW) }
      add(totalW)
    }
    val colX = computeColumnStarts(margin, contentW, orderedWidths, isRtl)

    val tableStartY = 70f
    var currentY = tableStartY
    val headerH = if (isDenseMonths) 28f else 25f

    // En-tête du tableau en surfaceSoft (#F8FAFC), texte en caption (500, mute)
    fillPaint.color = pal.headerBg
    canvas.drawRect(margin, currentY, margin + contentW, currentY + headerH, fillPaint)

    val hdrTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = pal.mute
      textSize = if (isDenseMonths) 8.4f else 9.2f
      typeface = pal.tfMedium
      textAlign = Paint.Align.CENTER
    }
    val hdrNumPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = pal.mute
      textSize = 8.0f
      typeface = pal.tfNumeric
      textAlign = Paint.Align.CENTER
    }

    val singleHdrBaseline = currentY + headerH * 0.64f
    var cIdx = 0
    canvas.drawText(ellipsizeToWidth(strings.colFloor, hdrTextPaint, floorW - 3f), colX[cIdx] + floorW / 2f, singleHdrBaseline, hdrTextPaint); cIdx++
    canvas.drawText(ellipsizeToWidth(strings.colAppt, hdrTextPaint, apptW - 2f), colX[cIdx] + apptW / 2f, singleHdrBaseline, hdrTextPaint); cIdx++
    if (options.showNames) {
      canvas.drawText(ellipsizeToWidth(strings.colOwner, hdrTextPaint, ownerW - 6f), colX[cIdx] + ownerW / 2f, singleHdrBaseline, hdrTextPaint)
      cIdx++
    }
    activeCols.forEach { col ->
      val mFee = cfg.feeForMonth(col)
      val hdrColor = if (isSpecialCotisationAmount(mFee, cfg.defaultMonthlyFee)) pal.partialFg else pal.mute
      val centerX = colX[cIdx] + monthW / 2f
      if (isDenseMonths) {
        val numP = Paint(hdrNumPaint).apply { color = hdrColor }
        val subMP = Paint(hdrTextPaint).apply { color = hdrColor; textSize = 7.2f }
        val mNum = localizeDigits("%02d".format(col.monthNumber), cfg.digitsAr)
        canvas.drawText(mNum, centerX, currentY + 11.5f, numP)
        canvas.drawText(ellipsizeToWidth(strings.monthShort(col), subMP, monthW - 3f), centerX, currentY + 23f, subMP)
      } else {
        val mHdrPaint = Paint(hdrTextPaint).apply { color = hdrColor }
        canvas.drawText(ellipsizeToWidth(strings.monthShort(col), mHdrPaint, monthW - 4f), centerX, singleHdrBaseline, mHdrPaint)
      }
      cIdx++
    }
    canvas.drawText(ellipsizeToWidth(strings.colTotal, hdrTextPaint, totalW - 4f), colX[cIdx] + totalW / 2f, singleHdrBaseline, hdrTextPaint)
    canvas.drawRect(margin, currentY, margin + contentW, currentY + headerH, strokePaint)
    currentY += headerH

    val rowCount = apartmentsSlice.size.coerceAtLeast(1)
    val footerReserve = if (showGrandFooter) 72f else 28f
    val availableRowsH = (pageHeight - currentY - footerReserve).coerceAtLeast(rowCount * 18f)
    val rowH = (availableRowsH / rowCount).coerceIn(19f, 26f)

    val cellTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = pal.ink
      textSize = (rowH * 0.43f).coerceIn(8.2f, 9.6f)
      typeface = pal.tfRegular
      textAlign = Paint.Align.CENTER
    }
    val mediumCellPaint = Paint(cellTextPaint).apply { typeface = pal.tfMedium }
    val numericCellPaint = Paint(cellTextPaint).apply {
      typeface = pal.tfNumeric
      textSize = if (isDenseMonths) (rowH * 0.40f).coerceIn(7.8f, 9.0f) else cellTextPaint.textSize
    }

    apartmentsSlice.forEachIndexed { idx, appt ->
      val isNewFloor = idx > 0 && apartmentsSlice[idx - 1].floor != appt.floor
      val vacant = appt.isVacantForConfig(cfg)

      fillPaint.color = when {
        vacant -> pal.vacantRowBg
        idx % 2 == 1 -> pal.altRowBg
        else -> pal.cardBg
      }
      canvas.drawRect(margin, currentY, margin + contentW, currentY + rowH, fillPaint)

      val textY = currentY + rowH * 0.66f
      var colCursor = 0

      // Étage
      val floorText = ellipsizeToWidth(strings.floorDisplay(appt.floor), mediumCellPaint, floorW - 4f)
      canvas.drawText(floorText, colX[colCursor] + floorW / 2f, textY, mediumCellPaint)
      colCursor++

      // Appartement
      canvas.drawText(
        localizeDigits(appt.displayNumber, cfg.digitsAr),
        colX[colCursor] + apptW / 2f,
        textY,
        numericCellPaint,
      )
      colCursor++

      // Propriétaire
      if (options.showNames) {
        val rawOwner = appt.displayOwner(isRtl).ifBlank { "—" }
        val ownerPaint = Paint(cellTextPaint).apply {
          textAlign = if (isRtl) Paint.Align.RIGHT else Paint.Align.LEFT
          color = if (vacant) pal.mute else pal.ink
          typeface = if (vacant) pal.tfRegular else pal.tfMedium
        }
        val fittedOwner = ellipsizeToWidth(rawOwner, ownerPaint, ownerW - 10f)
        val ownerX = if (isRtl) colX[colCursor] + ownerW - 5f else colX[colCursor] + 5f
        canvas.drawText(fittedOwner, ownerX, textY, ownerPaint)
        colCursor++
      }

      // Mois : payés en success (#006400), partiels/spéciaux en info (#254FAD), manquants en signatureCoral (#AA2D00)
      activeCols.forEach { col ->
        val amt = appt.amountFor(col)
        val expected = appt.expectedFeeForMonth(col, cfg)
        val cellCenter = colX[colCursor] + monthW / 2f
        if (amt > 0L) {
          val isPartial = expected > 0L && amt < expected
          val amtPaint = Paint(numericCellPaint).apply {
            color = when {
              isPartial || isSpecialCotisationAmount(amt, cfg.defaultMonthlyFee) -> pal.partialFg
              else -> pal.paidFg
            }
          }
          val glyph = if (isPartial) "◐" else "✓"
          val numStr = formatNumber(amt, cfg.digitsAr)
          val fullCell = "$glyph $numStr"
          val compactCell = "$glyph$numStr"
          val cellLabel = when {
            amtPaint.measureText(fullCell) <= monthW - 3f -> fullCell
            amtPaint.measureText(compactCell) <= monthW - 2f -> compactCell
            else -> ellipsizeToWidth(numStr, amtPaint, monthW - 2f)
          }
          canvas.drawText(cellLabel, cellCenter, textY, amtPaint)
        } else if (!vacant && col.monthNumber <= cfg.cutoffMonth.toInt()) {
          val missPaint = Paint(numericCellPaint).apply { color = pal.lateFg }
          canvas.drawText("!", cellCenter, textY, missPaint)
        } else {
          val mutePaint = Paint(cellTextPaint).apply { color = pal.mute }
          canvas.drawText(if (vacant) "—" else "·", cellCenter, textY, mutePaint)
        }
        colCursor++
      }

      // Total ligne
      val rowTot = appt.totalForConfig(cfg)
      val totPaint = Paint(numericCellPaint).apply {
        color = if (rowTot > 0L) pal.ink else pal.mute
      }
      val totStr = ellipsizeToWidth(formatNumber(rowTot, cfg.digitsAr), totPaint, totalW - 4f)
      canvas.drawText(totStr, colX[colCursor] + totalW / 2f, textY, totPaint)

      canvas.drawRect(margin, currentY, margin + contentW, currentY + rowH, strokePaint)
      if (isNewFloor) {
        canvas.drawLine(margin, currentY, margin + contentW, currentY, floorDividerPaint)
      }
      currentY += rowH
    }

    // Lignes verticales de grille
    orderedWidths.indices.forEach { i ->
      val xLine = colX[i]
      canvas.drawLine(xLine, tableStartY, xLine, currentY, strokePaint)
    }
    canvas.drawLine(margin + contentW, tableStartY, margin + contentW, currentY, strokePaint)

    if (showGrandFooter) {
      // Bandeau total caisse en surfaceDark (#181D26) texte blanc, ou surfaceStrong (#E0E2E6) en mode économie d'encre
      val footerTop = currentY + 8f
      val footerRect = RectF(margin, footerTop, margin + contentW, footerTop + 30f)
      fillPaint.color = pal.caisseBandBg
      canvas.drawRoundRect(footerRect, 8f, 8f, fillPaint)
      if (options.inkSaver) {
        canvas.drawRoundRect(footerRect, 8f, 8f, strokePaint)
      }

      val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = pal.caisseBandFg
        textSize = 10.8f
        typeface = pal.tfMedium
        textAlign = Paint.Align.CENTER
      }
      val collectedOnCols = data.apartments.sumOf { appt -> activeCols.sumOf { appt.amountFor(it) } }
      val footerLabel = ellipsizeToWidth(
        "${strings.rowCaisse} : ${formatDh(collectedOnCols, cur, cfg.digitsAr)}   |   " +
          "${strings.dashCashNowLabel} : ${formatDh(data.cashNowDh, cur, cfg.digitsAr)}",
        footerPaint,
        contentW - 16f,
      )
      canvas.drawText(rtlAware(footerLabel, isRtl), pageWidth / 2f, footerTop + 19.5f, footerPaint)

      if (cfg.footerNote.isNotBlank()) {
        val notePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          color = pal.body
          textSize = 8.6f
          typeface = pal.tfRegular
          textAlign = Paint.Align.CENTER
        }
        val fittedNote = ellipsizeToWidth(cfg.footerNote, notePaint, contentW)
        canvas.drawText(rtlAware(fittedNote, isRtl), pageWidth / 2f, footerTop + 46f, notePaint)
      }
    }
  }

  private fun drawRelanceCanvas(
    context: Context,
    canvas: Canvas,
    pageWidth: Int,
    @Suppress("UNUSED_PARAMETER") pageHeight: Int,
    data: SyndicatDashboardData,
    lateSlice: List<ApartmentRecord>,
    options: RelanceExportOptions,
    pageIndex: Int,
    pageCount: Int,
    showTotalFooter: Boolean,
  ) {
    val cfg = data.config
    val strings = stringsForLanguage(cfg.language)
    val isRtl = strings.language == AppLanguage.AR
    val cur = cfg.currency
    val pal = buildPalette(context, inkSaver = options.inkSaver, monochromeMode = options.monochromeMode)

    canvas.drawColor(pal.pageBg)
    val margin = 22f
    val contentW = pageWidth - margin * 2

    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.STROKE
      strokeWidth = 1.0f
      color = pal.hairline
    }
    val floorDividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.STROKE
      strokeWidth = 1.5f
      color = pal.floorLine
    }

    // §7 Affiche de relance : En-tête blanc propre avec titre en title (400) + hairline
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = pal.ink
      textSize = 17f
      typeface = pal.tfRegular
      textAlign = Paint.Align.CENTER
    }
    val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = pal.mute
      textSize = 9.8f
      typeface = pal.tfRegular
      textAlign = Paint.Align.CENTER
    }

    val pageSuffix = if (pageCount > 1) " (${bidiLtr("${pageIndex + 1}/$pageCount")})" else ""
    val docTitle = ellipsizeToWidth(strings.relanceDocTitle(cfg.residenceName) + pageSuffix, titlePaint, contentW)
    canvas.drawText(rtlAware(docTitle, isRtl), pageWidth / 2f, 38f, titlePaint)

    val today = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Date())
    val firstCol = cfg.cutoffMonthColumns.firstOrNull() ?: cfg.activeMonthColumns.first()
    val cutoffCol = data.cutoffMonthColumn
    val docSub = ellipsizeToWidth(
      strings.relanceDocSubtitle(localizeDigits(today, cfg.digitsAr), strings.monthShort(firstCol), strings.monthShort(cutoffCol), cfg.year),
      subPaint,
      contentW,
    )
    canvas.drawText(rtlAware(docSub, isRtl), pageWidth / 2f, 56f, subPaint)

    if (cfg.relanceHeaderNote.isNotBlank()) {
      val headerNotePaint = Paint(subPaint).apply {
        color = pal.body
        textSize = 9.2f
      }
      val fittedHdrNote = ellipsizeToWidth(cfg.relanceHeaderNote, headerNotePaint, contentW)
      canvas.drawText(rtlAware(fittedHdrNote, isRtl), pageWidth / 2f, 73f, headerNotePaint)
    }
    canvas.drawLine(margin, 83f, pageWidth - margin, 83f, strokePaint)

    val totalLateCount = data.lateApartments.size
    val totalRemStr = formatDh(data.totalRemainingToCollectDh, cur, cfg.digitsAr)

    // Bandeau récapitulatif (surfaceSoft #F8FAFC, montant en signatureCoral #AA2D00)
    val stripRect = RectF(margin, 92f, pageWidth - margin, 120f)
    fillPaint.color = pal.headerBg
    canvas.drawRoundRect(stripRect, 8f, 8f, fillPaint)
    canvas.drawRoundRect(stripRect, 8f, 8f, strokePaint)
    val stripPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = pal.lateFg
      textSize = 10.6f
      typeface = pal.tfMedium
      textAlign = Paint.Align.CENTER
    }
    val bannerTxt = ellipsizeToWidth(
      "! ${strings.relanceBannerSummary(totalLateCount, totalRemStr)}",
      stripPaint,
      contentW - 16f,
    )
    canvas.drawText(rtlAware(bannerTxt, isRtl), pageWidth / 2f, 110f, stripPaint)

    // Liste Portrait aérée : Appt+Étage empilés, Propriétaire, Mois impayés sur 2 lignes, Payé, Reste dû
    val unitW = 74f
    val ownerW = if (options.anonymousMode) 0f else 124f
    val paidW = 74f
    val remW = 90f
    val monthsW = contentW - unitW - ownerW - paidW - remW

    val orderedWidths = buildList {
      add(unitW)
      if (!options.anonymousMode) add(ownerW)
      add(monthsW)
      add(paidW)
      add(remW)
    }
    val colX = computeColumnStarts(margin, contentW, orderedWidths, isRtl)

    val tableTop = 128f
    var y = tableTop
    val headerH = 26f
    fillPaint.color = pal.headerBg
    canvas.drawRect(margin, y, margin + contentW, y + headerH, fillPaint)

    val hdrTxt = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = pal.mute
      textSize = 9.4f
      typeface = pal.tfMedium
      textAlign = Paint.Align.CENTER
    }

    var cIdx = 0
    canvas.drawText(ellipsizeToWidth("${strings.colAppt} / ${strings.colFloor}", hdrTxt, unitW - 4f), colX[cIdx] + unitW / 2f, y + 17f, hdrTxt); cIdx++
    if (!options.anonymousMode) {
      canvas.drawText(ellipsizeToWidth(strings.colOwner, hdrTxt, ownerW - 6f), colX[cIdx] + ownerW / 2f, y + 17f, hdrTxt)
      cIdx++
    }
    canvas.drawText(ellipsizeToWidth(strings.colMissingMonths, hdrTxt, monthsW - 6f), colX[cIdx] + monthsW / 2f, y + 17f, hdrTxt); cIdx++
    canvas.drawText(ellipsizeToWidth(strings.colPaid, hdrTxt, paidW - 4f), colX[cIdx] + paidW / 2f, y + 17f, hdrTxt); cIdx++
    canvas.drawText(ellipsizeToWidth(strings.colRemaining, hdrTxt, remW - 4f), colX[cIdx] + remW / 2f, y + 17f, hdrTxt)
    canvas.drawRect(margin, y, margin + contentW, y + headerH, strokePaint)
    y += headerH

    val rowH = 34f
    val unitMainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = pal.ink
      textSize = 10.0f
      typeface = pal.tfNumeric
      textAlign = Paint.Align.CENTER
    }
    val unitSubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = pal.mute
      textSize = 8.4f
      typeface = pal.tfRegular
      textAlign = Paint.Align.CENTER
    }
    val ownerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = pal.ink
      textSize = 9.6f
      typeface = pal.tfMedium
      textAlign = if (isRtl) Paint.Align.RIGHT else Paint.Align.LEFT
    }
    val numericCell = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = pal.ink
      textSize = 9.8f
      typeface = pal.tfNumeric
      textAlign = Paint.Align.CENTER
    }

    lateSlice.forEachIndexed { idx, appt ->
      val isNewFloor = idx > 0 && lateSlice[idx - 1].floor != appt.floor
      fillPaint.color = if (idx % 2 == 1) pal.altRowBg else pal.cardBg
      canvas.drawRect(margin, y, margin + contentW, y + rowH, fillPaint)

      var col = 0
      // 1. Appartement + Étage empilés verticalement pour une lecture mobile claire
      val unitCenter = colX[col] + unitW / 2f
      val apptLabel = "${strings.colAppt} ${localizeDigits(appt.displayNumber, cfg.digitsAr)}"
      canvas.drawText(ellipsizeToWidth(apptLabel, unitMainPaint, unitW - 6f), unitCenter, y + 14.5f, unitMainPaint)
      canvas.drawText(ellipsizeToWidth(strings.floorDisplay(appt.floor), unitSubPaint, unitW - 6f), unitCenter, y + 27.5f, unitSubPaint)
      col++

      // 2. Propriétaire (jusqu'à 2 lignes si nécessaire)
      if (!options.anonymousMode) {
        val rawOwner = appt.displayOwner(isRtl).ifBlank { "—" }
        val ownerLines = wrapTextLines(rawOwner, ownerPaint, ownerW - 12f, maxLines = 2)
        val ox = if (isRtl) colX[col] + ownerW - 6f else colX[col] + 6f
        if (ownerLines.size == 1) {
          canvas.drawText(ownerLines[0], ox, y + 20.5f, ownerPaint)
        } else {
          canvas.drawText(ownerLines[0], ox, y + 14.5f, ownerPaint)
          canvas.drawText(ownerLines[1], ox, y + 27.5f, ownerPaint)
        }
        col++
      }

      // 3. Mois manquants sur 1 ou 2 lignes lisibles
      val paidTotal = appt.totalForConfig(cfg)
      val statusGlyph = if (paidTotal > 0L) "◐ " else "! "
      val missingStr = statusGlyph + appt.missingMonths(cfg).joinToString(" · ") { strings.monthShort(it) }
      val missPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = if (paidTotal > 0L) pal.partialFg else pal.lateFg
        textSize = 8.8f
        typeface = pal.tfMedium
        textAlign = Paint.Align.CENTER
      }
      val missLines = wrapTextLines(missingStr, missPaint, monthsW - 10f, maxLines = 2)
      val mCenter = colX[col] + monthsW / 2f
      if (missLines.size == 1) {
        canvas.drawText(missLines[0], mCenter, y + 20.5f, missPaint)
      } else {
        canvas.drawText(missLines[0], mCenter, y + 14.5f, missPaint)
        canvas.drawText(missLines[1], mCenter, y + 27.5f, missPaint)
      }
      col++

      // 4. Montant réglé
      val paidPaint = Paint(numericCell).apply { color = pal.paidFg }
      val paidStr = ellipsizeToWidth(formatDh(paidTotal, cur, cfg.digitsAr), paidPaint, paidW - 6f)
      canvas.drawText(rtlAware(paidStr, isRtl), colX[col] + paidW / 2f, y + 20.5f, paidPaint)
      col++

      // 5. Reste dû en signatureCoral (#AA2D00)
      val remPaint = Paint(numericCell).apply { color = pal.lateFg }
      val remStr = ellipsizeToWidth("! ${formatDh(appt.remainingDh(cfg), cur, cfg.digitsAr)}", remPaint, remW - 6f)
      canvas.drawText(rtlAware(remStr, isRtl), colX[col] + remW / 2f, y + 20.5f, remPaint)

      canvas.drawRect(margin, y, margin + contentW, y + rowH, strokePaint)
      if (isNewFloor) {
        canvas.drawLine(margin, y, margin + contentW, y, floorDividerPaint)
      }
      y += rowH
    }

    // Lignes verticales du tableau
    orderedWidths.indices.forEach { i ->
      canvas.drawLine(colX[i], tableTop, colX[i], y, strokePaint)
    }
    canvas.drawLine(margin + contentW, tableTop, margin + contentW, y, strokePaint)

    if (showTotalFooter) {
      // Bandeau Total restant en signatureCoral (#AA2D00) ou blanc à contour en mode économie d'encre
      val footRect = RectF(margin, y + 10f, margin + contentW, y + 40f)
      if (options.inkSaver) {
        fillPaint.color = Color.WHITE
        canvas.drawRoundRect(footRect, 8f, 8f, fillPaint)
        val coralStroke = Paint(strokePaint).apply { color = SIGNATURE_CORAL; strokeWidth = 1.2f }
        canvas.drawRoundRect(footRect, 8f, 8f, coralStroke)
      } else {
        fillPaint.color = SIGNATURE_CORAL
        canvas.drawRoundRect(footRect, 8f, 8f, fillPaint)
      }
      val footPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = if (options.inkSaver) SIGNATURE_CORAL else Color.WHITE
        textSize = 11.2f
        typeface = pal.tfMedium
        textAlign = Paint.Align.CENTER
      }
      val footText = ellipsizeToWidth(
        "! ${strings.relanceTotalRemaining} : $totalRemStr",
        footPaint,
        contentW - 16f,
      )
      canvas.drawText(rtlAware(footText, isRtl), pageWidth / 2f, y + 29f, footPaint)
    }
  }

  private fun drawCaisseReportCanvas(
    context: Context,
    canvas: Canvas,
    pageWidth: Int,
    @Suppress("UNUSED_PARAMETER") pageHeight: Int,
    data: SyndicatDashboardData,
    expensesSlice: List<ExpenseRecord>,
    inkSaver: Boolean,
    showKpiHeader: Boolean,
    showCategoryBreakdown: Boolean,
    pageIndex: Int,
    pageCount: Int,
    monochromeMode: Boolean = false,
  ) {
    val cfg = data.config
    val strings = stringsForLanguage(cfg.language)
    val isRtl = strings.language == AppLanguage.AR
    val cur = cfg.currency
    val pal = buildPalette(context, inkSaver = inkSaver, monochromeMode = monochromeMode)

    canvas.drawColor(pal.pageBg)
    val margin = 22f
    val contentW = pageWidth - margin * 2
    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.STROKE
      strokeWidth = 1.0f
      color = pal.hairline
    }
    val inkBorderPaint = Paint(strokePaint).apply { color = INK_COLOR; strokeWidth = 1.2f }

    // §7 Situation & Caisse : En-tête blanc propre en title (400) + hairline
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = pal.ink
      textSize = 17f
      typeface = pal.tfRegular
      textAlign = Paint.Align.CENTER
    }
    val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = pal.mute
      textSize = 9.6f
      typeface = pal.tfRegular
      textAlign = Paint.Align.CENTER
    }
    val pageSuffix = if (pageCount > 1) " (${bidiLtr("${pageIndex + 1}/$pageCount")})" else ""
    val titleStr = ellipsizeToWidth(strings.caisseDocTitle(cfg.residenceName, cfg.year) + pageSuffix, titlePaint, contentW)
    canvas.drawText(rtlAware(titleStr, isRtl), pageWidth / 2f, 38f, titlePaint)

    val today = localizeDigits(SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Date()), cfg.digitsAr)
    val subLine = ellipsizeToWidth(
      buildList {
        add(strings.caisseIssuedOn(today))
        if (cfg.treasurerName.isNotBlank()) add(cfg.treasurerName)
      }.joinToString("  ·  "),
      subPaint,
      contentW,
    )
    canvas.drawText(rtlAware(subLine, isRtl), pageWidth / 2f, 56f, subPaint)
    canvas.drawLine(margin, 66f, pageWidth - margin, 66f, strokePaint)

    var y = 76f

    if (showKpiHeader) {
      val usePlainCard = inkSaver || monochromeMode
      // 1. Carte Forêt (#0A2E0E) en haut pour la caisse — ou carte blanche à contour si inkSaver / N&B (§7)
      val forestRect = RectF(margin, y, margin + contentW, y + 56f)
      fillPaint.color = if (usePlainCard) Color.WHITE else SIGNATURE_FOREST
      canvas.drawRoundRect(forestRect, 10f, 10f, fillPaint)
      if (usePlainCard) canvas.drawRoundRect(forestRect, 10f, 10f, inkBorderPaint)

      val forestLblPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = if (usePlainCard) pal.body else Color.WHITE
        textSize = 10.2f
        typeface = pal.tfRegular
        textAlign = Paint.Align.CENTER
      }
      val forestValPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = if (usePlainCard) pal.ink else Color.WHITE
        textSize = 17.5f
        typeface = pal.tfNumeric
        textAlign = Paint.Align.CENTER
      }
      val cashStr = if (data.cashNowDh < 0L) {
        "▼ ${formatDh(data.cashNowDh, cur, cfg.digitsAr)}"
      } else {
        formatDh(data.cashNowDh, cur, cfg.digitsAr)
      }
      canvas.drawText(rtlAware(strings.caisseAvailableTitle, isRtl), forestRect.centerX(), y + 20f, forestLblPaint)
      canvas.drawText(rtlAware(cashStr, isRtl), forestRect.centerX(), y + 43f, forestValPaint)
      y += 64f

      // 2. Cartes Menthe (#A8D8C4, Recettes) & Pêche (#FCAB79, Dépenses) côte à côte (ou blanc bordé en N&B)
      val halfW = (contentW - 10f) / 2f
      val mintLeft = if (isRtl) margin + halfW + 10f else margin
      val peachLeft = if (isRtl) margin else margin + halfW + 10f

      val mintRect = RectF(mintLeft, y, mintLeft + halfW, y + 48f)
      fillPaint.color = if (usePlainCard) Color.WHITE else SIGNATURE_MINT
      canvas.drawRoundRect(mintRect, 10f, 10f, fillPaint)
      if (usePlainCard) canvas.drawRoundRect(mintRect, 10f, 10f, strokePaint)

      val peachRect = RectF(peachLeft, y, peachLeft + halfW, y + 48f)
      fillPaint.color = if (usePlainCard) Color.WHITE else SIGNATURE_PEACH
      canvas.drawRoundRect(peachRect, 10f, 10f, fillPaint)
      if (usePlainCard) canvas.drawRoundRect(peachRect, 10f, 10f, strokePaint)

      val kpiLblP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = pal.body
        textSize = 9.2f
        typeface = pal.tfRegular
        textAlign = Paint.Align.CENTER
      }
      val kpiValP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = pal.ink
        textSize = 13.0f
        typeface = pal.tfNumeric
        textAlign = Paint.Align.CENTER
      }
      canvas.drawText(rtlAware(ellipsizeToWidth(strings.caisseTotalReceipts, kpiLblP, halfW - 12f), isRtl), mintRect.centerX(), y + 18f, kpiLblP)
      canvas.drawText(rtlAware(formatDh(data.totalCaisseDh, cur, cfg.digitsAr), isRtl), mintRect.centerX(), y + 38f, kpiValP)

      canvas.drawText(rtlAware(ellipsizeToWidth(strings.caisseTotalExpenses, kpiLblP, halfW - 12f), isRtl), peachRect.centerX(), y + 18f, kpiLblP)
      canvas.drawText(rtlAware(formatDh(data.totalExpensesDh, cur, cfg.digitsAr), isRtl), peachRect.centerX(), y + 38f, kpiValP)
      y += 56f

      // 3. Ligne de synthèse comptable complémentaire
      val thirdW = (contentW - 12f) / 3f
      val secondaryKpis = listOf(
        strings.caisseOpeningBalance to formatDh(cfg.openingBalanceDh, cur, cfg.digitsAr),
        strings.caisseUnpaidSuppliers to formatDh(data.unpaidExpensesDh, cur, cfg.digitsAr),
        strings.caisseAccountingBalance to formatDh(data.balanceDh, cur, cfg.digitsAr),
      )
      val kpiStarts = computeColumnStarts(margin, contentW, listOf(thirdW, thirdW, thirdW), isRtl)
      secondaryKpis.forEachIndexed { idx, (lbl, vStr) ->
        val left = kpiStarts[idx] + when (idx) {
          0 -> if (isRtl) 4f else 0f
          2 -> if (isRtl) 0f else 4f
          else -> 2f
        }
        val boxW = thirdW - 4f
        val r = RectF(left, y, left + boxW, y + 38f)
        fillPaint.color = Color.WHITE
        canvas.drawRoundRect(r, 8f, 8f, fillPaint)
        canvas.drawRoundRect(r, 8f, 8f, strokePaint)
        val slP = Paint(kpiLblP).apply { textSize = 8.2f; color = pal.mute }
        val svP = Paint(kpiValP).apply {
          textSize = 10.2f
          color = if (idx == 1 && data.unpaidExpensesDh > 0L) pal.lateFg else pal.ink
        }
        canvas.drawText(rtlAware(ellipsizeToWidth(lbl, slP, boxW - 8f), isRtl), r.centerX(), y + 14.5f, slP)
        canvas.drawText(rtlAware(ellipsizeToWidth(vStr, svP, boxW - 8f), isRtl), r.centerX(), y + 30.5f, svP)
      }
      y += 48f
    }

    // 4. Liste Portrait des dépenses (4 colonnes claires au lieu de 6 colonnes tassées :
    //    Désignation + Catégorie/Prestataire empilés | Date | Montant | Réglé + Reste éventuel)
    val descW = 245f
    val dateW = 76f
    val amtW = 108f
    val paidW = contentW - descW - dateW - amtW
    val widths = listOf(descW, dateW, amtW, paidW)
    val colX = computeColumnStarts(margin, contentW, widths, isRtl)

    val tableTop = y
    val headerH = 25f
    fillPaint.color = pal.headerBg
    canvas.drawRect(margin, y, margin + contentW, y + headerH, fillPaint)
    val hPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = pal.mute
      textSize = 9.2f
      typeface = pal.tfMedium
      textAlign = Paint.Align.CENTER
    }
    val headers = listOf(
      strings.expFieldTitle,
      if (isRtl) "التاريخ" else "Date",
      if (isRtl) "المبلغ" else "Montant",
      strings.colPaid,
    )
    headers.forEachIndexed { i, h ->
      canvas.drawText(ellipsizeToWidth(h, hPaint, widths[i] - 6f), colX[i] + widths[i] / 2f, y + 16.5f, hPaint)
    }
    canvas.drawRect(margin, y, margin + contentW, y + headerH, strokePaint)
    y += headerH

    val titleCellP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = pal.ink
      textSize = 9.8f
      typeface = pal.tfMedium
      textAlign = if (isRtl) Paint.Align.RIGHT else Paint.Align.LEFT
    }
    val metaCellP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = pal.mute
      textSize = 8.4f
      typeface = pal.tfRegular
      textAlign = if (isRtl) Paint.Align.RIGHT else Paint.Align.LEFT
    }
    val cellNumeric = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = pal.ink
      textSize = 9.6f
      typeface = pal.tfNumeric
      textAlign = Paint.Align.CENTER
    }

    if (expensesSlice.isEmpty()) {
      val emptyH = 34f
      fillPaint.color = pal.cardBg
      canvas.drawRect(margin, y, margin + contentW, y + emptyH, fillPaint)
      val emptyP = Paint(metaCellP).apply { textAlign = Paint.Align.CENTER; textSize = 9.5f }
      canvas.drawText(rtlAware(strings.expEmptyTitle, isRtl), pageWidth / 2f, y + 21f, emptyP)
      canvas.drawRect(margin, y, margin + contentW, y + emptyH, strokePaint)
      y += emptyH
    } else {
      val rH = 34f
      expensesSlice.forEachIndexed { idx, exp ->
        fillPaint.color = if (idx % 2 == 1) pal.altRowBg else pal.cardBg
        canvas.drawRect(margin, y, margin + contentW, y + rH, fillPaint)

        // Colonne 0 : Libellé (ligne 1) + Catégorie · Prestataire (ligne 2)
        val descX = if (isRtl) colX[0] + descW - 8f else colX[0] + 8f
        val fittedExpTitle = ellipsizeToWidth(exp.title, titleCellP, descW - 16f)
        val subMeta = buildList {
          add(strings.categoryLabel(exp.category))
          if (exp.supplier.isNotBlank()) add(exp.supplier)
        }.joinToString("  ·  ")
        val fittedSubMeta = ellipsizeToWidth(subMeta, metaCellP, descW - 16f)
        canvas.drawText(rtlAware(fittedExpTitle, isRtl), descX, y + 14.5f, titleCellP)
        canvas.drawText(rtlAware(fittedSubMeta, isRtl), descX, y + 27.5f, metaCellP)

        // Colonne 1 : Date
        val datePaint = Paint(cellNumeric).apply { textSize = 9.0f; color = pal.body }
        canvas.drawText(
          bidiLtr(localizeDigits(exp.dateLabel, cfg.digitsAr)),
          colX[1] + widths[1] / 2f,
          y + 20.5f,
          datePaint,
        )

        // Colonne 2 : Montant total
        val amtStr = ellipsizeToWidth(formatDh(exp.amountDh, cur, cfg.digitsAr), cellNumeric, widths[2] - 8f)
        canvas.drawText(rtlAware(amtStr, isRtl), colX[2] + widths[2] / 2f, y + 20.5f, cellNumeric)

        // Colonne 3 : Montant réglé (+ reste prestataire éventuel en dessous)
        val hasRem = exp.remainingToPayDh > 0L
        val paidColorPaint = Paint(cellNumeric).apply {
          color = if (hasRem) pal.partialFg else pal.paidFg
        }
        val paidPrefix = if (hasRem) "◐ " else "✓ "
        val paidStr = ellipsizeToWidth(
          "$paidPrefix${formatDh(exp.effectivePaidDh, cur, cfg.digitsAr)}",
          paidColorPaint,
          widths[3] - 8f,
        )
        if (hasRem) {
          canvas.drawText(rtlAware(paidStr, isRtl), colX[3] + widths[3] / 2f, y + 14.5f, paidColorPaint)
          val remSubP = Paint(cellNumeric).apply {
            color = pal.lateFg
            textSize = 8.0f
          }
          val remLabel = if (isRtl) {
            "! الباقي ${formatNumber(exp.remainingToPayDh, cfg.digitsAr)}"
          } else {
            "! Reste ${formatNumber(exp.remainingToPayDh, cfg.digitsAr)}"
          }
          canvas.drawText(
            rtlAware(ellipsizeToWidth(remLabel, remSubP, widths[3] - 8f), isRtl),
            colX[3] + widths[3] / 2f,
            y + 27.5f,
            remSubP,
          )
        } else {
          canvas.drawText(rtlAware(paidStr, isRtl), colX[3] + widths[3] / 2f, y + 20.5f, paidColorPaint)
        }

        canvas.drawRect(margin, y, margin + contentW, y + rH, strokePaint)
        y += rH
      }

      // Lignes verticales de séparation
      widths.indices.forEach { i ->
        canvas.drawLine(colX[i], tableTop, colX[i], y, strokePaint)
      }
      canvas.drawLine(margin + contentW, tableTop, margin + contentW, y, strokePaint)
    }

    // 5. Répartition par catégorie en grille Portrait 2 colonnes (au lieu d'une ligne horizontale tronquée)
    if (showCategoryBreakdown) {
      val byCat = data.expenses
        .groupBy { it.category }
        .mapValues { (_, list) -> list.sumOf { it.amountDh } }
        .entries
        .toList()
      if (byCat.isNotEmpty()) {
        y += 12f
        val catColW = (contentW - 10f) / 2f
        val catLblP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          color = pal.body
          textSize = 8.8f
          typeface = pal.tfMedium
          textAlign = if (isRtl) Paint.Align.RIGHT else Paint.Align.LEFT
        }
        val catValP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          color = pal.ink
          textSize = 9.0f
          typeface = pal.tfNumeric
          textAlign = if (isRtl) Paint.Align.LEFT else Paint.Align.RIGHT
        }
        byCat.chunked(2).forEach { pair ->
          pair.forEachIndexed { idxInPair, (cat, sum) ->
            val colLeft = if (isRtl) {
              if (idxInPair == 0) margin + catColW + 10f else margin
            } else {
              if (idxInPair == 0) margin else margin + catColW + 10f
            }
            val box = RectF(colLeft, y, colLeft + catColW, y + 22f)
            fillPaint.color = pal.headerBg
            canvas.drawRoundRect(box, 6f, 6f, fillPaint)
            canvas.drawRoundRect(box, 6f, 6f, strokePaint)
            val lx = if (isRtl) box.right - 8f else box.left + 8f
            val vx = if (isRtl) box.left + 8f else box.right - 8f
            val cName = ellipsizeToWidth(strings.categoryLabel(cat), catLblP, catColW * 0.56f)
            val cVal = ellipsizeToWidth(formatDh(sum, cur, cfg.digitsAr), catValP, catColW * 0.40f)
            canvas.drawText(rtlAware(cName, isRtl), lx, y + 14.5f, catLblP)
            canvas.drawText(rtlAware(cVal, isRtl), vx, y + 14.5f, catValP)
          }
          y += 26f
        }
      }
    }
  }

  private fun drawPaymentReceiptCanvas(
    context: Context,
    canvas: Canvas,
    width: Int,
    height: Int,
    apartment: ApartmentRecord,
    data: SyndicatDashboardData,
    receiptNo: Long,
    payment: PaymentRecord? = null,
    monochromeMode: Boolean = false,
  ) {
    val cfg = data.config
    val strings = stringsForLanguage(cfg.language)
    val isRtl = strings.language == AppLanguage.AR
    val pal = buildPalette(context, inkSaver = false, monochromeMode = monochromeMode)
    val cornerR = 12f
    val outerPad = 20f
    val effectivePayment = payment ?: data.paymentsForApartment(apartment.number, cfg.year).firstOrNull()
    val receiptYear = payment?.year ?: cfg.year
    val paidAmountDh = payment?.amountDh ?: apartment.totalForConfig(cfg)

    canvas.drawColor(pal.pageBg)
    val cardRect = RectF(outerPad, outerPad, width - outerPad, height - outerPad)
    val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = pal.cardBg; style = Paint.Style.FILL }
    val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = pal.hairline
      style = Paint.Style.STROKE
      strokeWidth = 1.2f
    }
    canvas.drawRoundRect(cardRect, cornerR, cornerR, fill)
    canvas.drawRoundRect(cardRect, cornerR, cornerR, stroke)

    // Bandeau supérieur en surfaceSoft (#F8FAFC)
    val topRect = RectF(outerPad, outerPad, width - outerPad, 114f)
    fill.color = pal.headerBg
    canvas.drawRoundRect(topRect, cornerR, cornerR, fill)
    canvas.drawRoundRect(topRect, cornerR, cornerR, stroke)

    val titleP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = pal.ink
      textSize = 18f
      typeface = pal.tfRegular
      textAlign = Paint.Align.CENTER
    }
    val subP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = pal.mute
      textSize = 11f
      typeface = pal.tfRegular
      textAlign = Paint.Align.CENTER
    }
    val innerW = width - outerPad * 2 - 32f
    canvas.drawText(
      rtlAware(ellipsizeToWidth("${strings.receiptDocTitle}  ${bidiLtr("#${"%03d".format(receiptNo)}")}", titleP, innerW), isRtl),
      width / 2f,
      58f,
      titleP,
    )
    canvas.drawText(
      rtlAware(ellipsizeToWidth("${cfg.residenceName} · ${bidiLtr(localizeDigits(receiptYear.toString(), cfg.digitsAr))}", subP, innerW), isRtl),
      width / 2f,
      86f,
      subP,
    )

    // Encart montant réglé (Menthe signature ou gris doux en N&B)
    val paidBox = RectF(38f, 134f, width - 38f, 214f)
    fill.color = if (monochromeMode) pal.headerBg else SIGNATURE_MINT
    canvas.drawRoundRect(paidBox, 10f, 10f, fill)
    if (monochromeMode) {
      canvas.drawRoundRect(paidBox, 10f, 10f, stroke)
    }
    val paidLblP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = BODY_COLOR
      textSize = 11f
      typeface = pal.tfMedium
      textAlign = Paint.Align.CENTER
    }
    val paidValP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = INK_COLOR
      textSize = 24f
      typeface = pal.tfNumeric
      textAlign = Paint.Align.CENTER
    }
    canvas.drawText(rtlAware(strings.colPaid, isRtl), paidBox.centerX(), 160f, paidLblP)
    canvas.drawText(
      rtlAware("✓ ${formatDh(paidAmountDh, cfg.currency, cfg.digitsAr)}", isRtl),
      paidBox.centerX(),
      194f,
      paidValP,
    )

    val labelP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = pal.mute
      textSize = 11.2f
      typeface = pal.tfRegular
      textAlign = if (isRtl) Paint.Align.RIGHT else Paint.Align.LEFT
    }
    val valP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = pal.ink
      textSize = 12.2f
      typeface = pal.tfMedium
      textAlign = if (isRtl) Paint.Align.LEFT else Paint.Align.RIGHT
    }
    val valNumP = Paint(valP).apply { typeface = pal.tfNumeric }

    val startX = if (isRtl) width - 40f else 40f
    val endX = if (isRtl) 40f else width - 40f
    val valueMaxW = (width - 80f) * 0.62f
    val rawDate = effectivePayment?.paymentDate?.takeIf { it.isNotBlank() }
      ?: SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Date())
    val formattedDate = bidiLtr(localizeDigits(rawDate, cfg.digitsAr))
    val paidMonths = if (payment != null && payment.effectiveMonthsCovered.isNotEmpty()) {
      payment.effectiveMonthsCovered.joinToString(" · ") { strings.monthShort(it) }
    } else {
      cfg.activeMonthColumns.filter { apartment.amountFor(it) > 0L }
        .joinToString(" · ") { strings.monthShort(it) }
        .ifBlank { "—" }
    }
    val methodLabel = strings.paymentMethodLabel(effectivePayment?.paymentMethod ?: "cash")

    val remDh = apartment.remainingDh(cfg)
    val remFormatted = if (remDh > 0L) {
      "! ${formatDh(remDh, cfg.currency, cfg.digitsAr)}"
    } else {
      "✓ ${formatDh(0L, cfg.currency, cfg.digitsAr)}"
    }

    var ry = 236f
    fun drawReceiptRow(label: String, value: String, valuePaint: Paint, allowWrap: Boolean = false) {
      canvas.drawText(rtlAware(label, isRtl), startX, ry, labelP)
      if (allowWrap) {
        val lines = wrapTextLines(value, valuePaint, valueMaxW, maxLines = 2)
        lines.forEachIndexed { i, ln ->
          canvas.drawText(rtlAware(ln, isRtl), endX, ry + i * 15f, valuePaint)
        }
        ry += 16f + (lines.size - 1) * 15f
      } else {
        canvas.drawText(rtlAware(ellipsizeToWidth(value, valuePaint, valueMaxW), isRtl), endX, ry, valuePaint)
        ry += 16f
      }
      canvas.drawLine(40f, ry - 5f, width - 40f, ry - 5f, stroke)
      ry += 16f
    }

    drawReceiptRow(
      strings.colAppt,
      "${bidiLtr(localizeDigits(apartment.displayNumber, cfg.digitsAr))} (${strings.floorWithPrefix(apartment.floor)})",
      valP,
    )
    drawReceiptRow(
      strings.colOwner,
      apartment.displayOwner(isRtl).ifBlank { "—" },
      valP,
      allowWrap = true,
    )
    drawReceiptRow(
      if (isRtl) "الأشهر المؤداة" else "Mois réglés",
      paidMonths,
      valP,
      allowWrap = true,
    )
    drawReceiptRow(
      strings.receiptMethodRowLabel,
      methodLabel,
      valP,
    )
    val remRowPaint = Paint(valNumP).apply {
      color = if (remDh > 0L) pal.lateFg else pal.paidFg
    }
    drawReceiptRow(strings.colRemaining, remFormatted, remRowPaint)
    drawReceiptRow(strings.datePrefix, formattedDate, valNumP)

    // Bloc QR Code vérifiable au-dessus du cachet inférieur (de y ≈ 495 à 585)
    val verifyCode = "SYN-A${apartment.number}-R${"%03d".format(receiptNo)}-$receiptYear-${paidAmountDh}DH"
    val qrPayload = "SYNDICAT|${cfg.residenceName.take(18)}|APT=${apartment.number}|NO=$receiptNo|AMT=$paidAmountDh|DATE=$rawDate"
    val qrBoxTop = maxOf(ry - 2f, height - 178f)
    val qrBoxBottom = height - 88f
    val qrBoxRect = RectF(38f, qrBoxTop, width - 38f, qrBoxBottom)
    fill.color = pal.headerBg
    canvas.drawRoundRect(qrBoxRect, 8f, 8f, fill)
    canvas.drawRoundRect(qrBoxRect, 8f, 8f, stroke)

    val qrMatrix = QrCodeGenerator.encode(qrPayload)
    val qrModules = qrMatrix.size
    val qrDrawSize = (qrBoxBottom - qrBoxTop - 12f).coerceIn(58f, 76f)
    val qrStartX = if (isRtl) qrBoxRect.left + 10f else qrBoxRect.right - qrDrawSize - 10f
    val qrStartY = qrBoxRect.centerY() - qrDrawSize / 2f
    val quietBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; style = Paint.Style.FILL }
    canvas.drawRect(qrStartX - 3f, qrStartY - 3f, qrStartX + qrDrawSize + 3f, qrStartY + qrDrawSize + 3f, quietBg)
    val modSize = qrDrawSize / qrModules.toFloat()
    val qrDarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = if (monochromeMode) Color.BLACK else INK_COLOR
      style = Paint.Style.FILL
    }
    for (r in 0 until qrModules) {
      for (c in 0 until qrModules) {
        if (qrMatrix[r][c]) {
          val left = qrStartX + c * modSize
          val top = qrStartY + r * modSize
          canvas.drawRect(left, top, left + modSize + 0.35f, top + modSize + 0.35f, qrDarkPaint)
        }
      }
    }

    val qrTextAlign = if (isRtl) Paint.Align.RIGHT else Paint.Align.LEFT
    val qrTextX = if (isRtl) qrBoxRect.right - 12f else qrBoxRect.left + 12f
    val qrAvailTextW = (qrBoxRect.width() - qrDrawSize - 32f).coerceAtLeast(100f)
    val qrLblP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = pal.ink
      textSize = 10.2f
      typeface = pal.tfMedium
      textAlign = qrTextAlign
    }
    val qrCodeP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = pal.mute
      textSize = 8.8f
      typeface = pal.tfNumeric
      textAlign = qrTextAlign
    }
    canvas.drawText(
      rtlAware(ellipsizeToWidth(strings.receiptQrVerifyLabel, qrLblP, qrAvailTextW), isRtl),
      qrTextX,
      qrBoxRect.centerY() - 10f,
      qrLblP,
    )
    canvas.drawText(
      bidiLtr(ellipsizeToWidth(verifyCode, qrCodeP, qrAvailTextW)),
      qrTextX,
      qrBoxRect.centerY() + 8f,
      qrCodeP,
    )
    canvas.drawText(
      rtlAware(ellipsizeToWidth("${strings.appBrandName} · ${strings.appPublisherShort}", qrCodeP, qrAvailTextW), isRtl),
      qrTextX,
      qrBoxRect.centerY() + 23f,
      qrCodeP,
    )

    // Cachet inférieur éditorial en surfaceDark (#181D26) texte blanc (ou noir pur en N&B)
    val badgeRect = RectF(38f, height - 78f, width - 38f, height - 38f)
    fill.color = if (monochromeMode) Color.BLACK else INK_COLOR
    canvas.drawRoundRect(badgeRect, 8f, 8f, fill)
    val stampP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.WHITE
      textSize = 11.2f
      typeface = pal.tfMedium
      textAlign = Paint.Align.CENTER
    }
    val sig = if (cfg.treasurerName.isNotBlank()) "  ·  ${cfg.treasurerName}" else ""
    val stampText = ellipsizeToWidth("${cfg.syndicatName}$sig", stampP, width - 96f)
    canvas.drawText(rtlAware(stampText, isRtl), width / 2f, height - 53f, stampP)
  }

  private fun computeColumnStarts(
    margin: Float,
    contentW: Float,
    orderedWidths: List<Float>,
    isRtl: Boolean,
  ): List<Float> {
    val starts = FloatArray(orderedWidths.size)
    if (!isRtl) {
      var cursor = margin
      for (i in orderedWidths.indices) {
        starts[i] = cursor
        cursor += orderedWidths[i]
      }
    } else {
      var cursor = margin + contentW
      for (i in orderedWidths.indices) {
        cursor -= orderedWidths[i]
        starts[i] = cursor
      }
    }
    return starts.toList()
  }

  private fun shareFileUri(
    context: Context,
    file: File,
    mimeType: String,
    subject: String,
  ) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
      type = mimeType
      putExtra(Intent.EXTRA_STREAM, uri)
      putExtra(Intent.EXTRA_SUBJECT, subject)
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooser = Intent.createChooser(shareIntent, subject).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(chooser)
  }
}
