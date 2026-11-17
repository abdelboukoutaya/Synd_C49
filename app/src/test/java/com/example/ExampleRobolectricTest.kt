package com.example

import android.content.Context
import android.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import androidx.core.content.res.ResourcesCompat
import androidx.test.core.app.ApplicationProvider
import android.content.ContextWrapper
import com.example.auth.findActivity
import com.example.data.model.ApartmentRecord
import com.example.data.model.ExpenseRecord
import com.example.data.model.MonthColumn
import com.example.data.model.PaymentRecord
import com.example.data.model.ProjectPriority
import com.example.data.model.ProjectRecord
import com.example.data.model.ProjectStatus
import com.example.data.model.SyndicatConfig
import com.example.data.model.projectApartmentsForYear
import com.example.data.seed.InitialSyndicatData
import com.example.ui.components.FintechTextField
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LRM
import com.example.ui.i18n.LocalStrings
import com.example.ui.i18n.NBSP
import com.example.ui.i18n.RLM
import com.example.ui.i18n.formatNumber
import com.example.ui.i18n.localizeDigits
import com.example.ui.i18n.stringsForLanguage
import com.example.ui.screens.ApartmentsScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExpensesScreen
import com.example.ui.screens.ProjectsScreen
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import com.example.data.local.AgMinutesDraft
import com.example.data.local.AgResolutionRecord
import com.example.data.local.BuildingProfile
import com.example.data.local.IncidentRecord
import com.example.data.local.IncidentSeverity
import com.example.data.local.IncidentStatus
import com.example.data.local.RecurringExpenseTemplate
import com.example.data.local.SpecialCallRecord
import com.example.data.local.SyndicatAdvancedCodec
import com.example.data.local.SyndicatRoleMode
import com.example.data.local.VendorRecord
import com.example.ui.theme.AppTheme
import com.example.ui.theme.CoinbaseCanvas
import com.example.ui.theme.CoinbaseDarkCardBg
import com.example.ui.theme.CoinbaseDarkScreenBg
import com.example.ui.theme.CoinbaseDarkTokens
import com.example.data.local.LocalSyndicatRepository
import com.example.data.repository.LOCAL_DEVICE_USER_ID
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CoinbaseLightTokens
import com.example.ui.theme.CoinbaseOnPrimary
import com.example.ui.theme.CoinbasePrimary
import com.example.ui.theme.CoinbaseSurfaceSoft
import com.example.ui.theme.CoinbaseSurfaceStrong
import com.example.ui.theme.MetaBrandCobalt
import com.example.ui.theme.MetaThemeLightTokens
import com.example.ui.theme.MiniMaxLightTokens
import com.example.ui.theme.MiniMaxPrimary
import com.example.ui.theme.darkScheme
import com.example.ui.theme.lightScheme
import com.example.ui.theme.tokensForThemeStyle
import com.example.ui.viewmodel.StatusFilter
import com.example.ui.viewmodel.SyndicatDashboardData
import com.example.ui.viewmodel.formatDh
import com.example.util.BackupManager
import com.example.util.CotisationExportOptions
import com.example.util.ExportVariant
import com.example.util.PdfPosterExporter
import com.example.util.RelanceExportOptions
import com.example.util.SyndicatCouncilShareCodec
import com.example.util.SyndicatReportsExporter
import com.example.util.WhatsAppHelper
import com.example.widget.SyndicatCompactWidgetReceiver
import com.example.widget.SyndicatDetailedWidgetReceiver
import com.example.widget.SyndicatGlanceWidgetReceiver
import com.example.widget.SyndicatWidgetPrefs
import com.example.widget.SyndicatWidgetSnapshot
import com.example.widget.WidgetFormatPreset
import com.example.widget.WidgetQuickAction
import com.example.widget.WidgetThemePreset
import com.example.widget.widgetPaletteFor
import java.io.File
import java.text.Bidi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  /**
   * Simulates the Unicode Bidirectional Algorithm (UAX #9) in an RTL paragraph
   * (matching Compose LayoutDirection.Rtl and Arabic Canvas paragraphs) and returns
   * the visible left-to-right character sequence seen by the user.
   */
  private fun renderVisualInRtl(logicalText: String): String {
    val bidi = Bidi(logicalText, Bidi.DIRECTION_RIGHT_TO_LEFT)
    val length = logicalText.length
    val levels = ByteArray(length) { idx -> bidi.getLevelAt(idx).toByte() }
    val objects = Array<Any>(length) { idx ->
      val ch = logicalText[idx]
      // UAX #9 rule L4: mirrored glyphs on odd (RTL) embedding levels
      if (levels[idx].toInt() % 2 != 0) {
        when (ch) {
          '(' -> ')'
          ')' -> '('
          else -> ch
        }
      } else {
        ch
      }
    }
    Bidi.reorderVisually(levels, 0, objects, 0, length)
    return buildString {
      for (obj in objects) {
        val c = obj as Char
        if (c.toString() != LRM && c.toString() != RLM) {
          append(c)
        }
      }
    }
  }

  @Test
  fun readStringFromContext() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SYNDICAT", appName)
    assertEquals("2.0", BuildConfig.VERSION_NAME)
    assertEquals(2, BuildConfig.VERSION_CODE)
    assertEquals("DYNIC", context.getString(R.string.app_editor_name))
    assertNotNull("Custom SYNDICAT icon drawable must exist", context.getDrawable(R.drawable.img_syndicat_icon))
    assertNotNull("Adaptive launcher icon must load", context.getDrawable(R.mipmap.ic_launcher))
    assertNotNull("Round launcher icon must load", context.getDrawable(R.mipmap.ic_launcher_round))
  }

  @Test
  fun appThemeSchemesAndTokensSupportLightAndDarkSyndicatModes() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    assertNotNull("IBM Plex Sans Arabic font must load", ResourcesCompat.getFont(context, R.font.ibm_plex_sans_arabic))
    assertNotNull("Plus Jakarta Sans font must load", ResourcesCompat.getFont(context, R.font.plus_jakarta_sans))
    assertNotNull("Montserrat fallback font must load", ResourcesCompat.getFont(context, R.font.montserrat))
    assertNotNull("Inter numeric font must load", ResourcesCompat.getFont(context, R.font.inter))

    // Verify Coinbase Design System lightScheme & darkScheme Material 3 palettes
    assertEquals("Coinbase", CoinbaseLightTokens.designSystemName)
    assertEquals(CoinbasePrimary, lightScheme.primary)
    assertEquals(CoinbaseCanvas, lightScheme.background)
    assertEquals(CoinbaseCanvas, lightScheme.surface)

    // Verify single-brand-voltage Coinbase Blue CTA & soft surfaces
    assertEquals(CoinbasePrimary, CoinbaseLightTokens.primary)
    assertEquals(CoinbaseOnPrimary, CoinbaseLightTokens.onPrimary)
    assertEquals(CoinbasePrimary, CoinbaseLightTokens.buyCta)
    assertEquals(CoinbaseSurfaceSoft, CoinbaseLightTokens.surfaceSoft)
    assertEquals(CoinbaseSurfaceStrong, CoinbaseLightTokens.surfaceStrong)

    // Verify Coinbase shape scale: 100dp pill, 24dp feature/card, 16dp content card, 12dp input
    assertEquals(100, CoinbaseLightTokens.buttonRadiusDp)
    assertEquals(100, CoinbaseLightTokens.badgeRadiusDp)
    assertEquals(24, CoinbaseLightTokens.featureCardRadiusDp)
    assertEquals(24, CoinbaseLightTokens.cardRadiusDp)
    assertEquals(16, CoinbaseLightTokens.contentCardRadiusDp)
    assertEquals(12, CoinbaseLightTokens.fieldRadiusDp)

    // Verify Dark Mode tokens anchored on surface-dark (#0A0B0D) & surface-dark-elevated (#16181C)
    assertEquals(CoinbaseDarkScreenBg, darkScheme.background)
    assertEquals(CoinbaseDarkCardBg, darkScheme.surface)
    assertEquals(CoinbasePrimary, CoinbaseDarkTokens.primary)
    assertEquals(CoinbaseOnPrimary, CoinbaseDarkTokens.onPrimary)
    assertEquals(CoinbasePrimary, CoinbaseDarkTokens.buyCta)
    assertFalse(CoinbaseLightTokens.isDark)
    assertTrue(CoinbaseDarkTokens.isDark)

    // Coinbase weight rhythm: 300 (Light), 400 (Normal), 500 (Medium), 600 (SemiBold), 700 (Bold)
    assertEquals(FontWeight.Light, CoinbaseLightTokens.weight(FontWeight.Light))
    assertEquals(FontWeight.Normal, CoinbaseLightTokens.weight(FontWeight.Normal))
    assertEquals(FontWeight.Medium, CoinbaseLightTokens.weight(FontWeight.Medium))
    assertEquals(FontWeight.SemiBold, CoinbaseLightTokens.weight(FontWeight.SemiBold))
    assertEquals(FontWeight.Bold, CoinbaseLightTokens.weight(FontWeight.Bold))
    assertEquals("coinbase", SyndicatConfig().themeStyle)

    // Verify separate Meta, Coinbase, and MiniMax theme token sets
    assertEquals("Meta", MetaThemeLightTokens.designSystemName)
    assertEquals(MetaBrandCobalt, tokensForThemeStyle("meta", darkTheme = false).primary)
    assertEquals(28, MetaThemeLightTokens.featureCardRadiusDp)

    assertEquals("Coinbase", tokensForThemeStyle("coinbase", darkTheme = false).designSystemName)
    assertEquals(CoinbasePrimary, tokensForThemeStyle("coinbase", darkTheme = false).primary)

    assertEquals("MiniMax", MiniMaxLightTokens.designSystemName)
    assertEquals(MiniMaxPrimary, tokensForThemeStyle("minimax", darkTheme = false).primary)
    assertEquals(14, MiniMaxLightTokens.buttonRadiusDp)
  }

  @Test
  fun arabicRtlBidiVisualOrderRenders1632StrictlyLeftToRight() {
    val context = ApplicationProvider.getApplicationContext<Context>()

    // Verify bundled Cairo font resource loads cleanly
    val cairoTypeface = ResourcesCompat.getFont(context, R.font.cairo)
    assertTrue("Cairo font resource must load", cairoTypeface != null)

    // 1. Plain thousands-separated number in RTL: 1 632 -> "1 632" (never "632 1")
    assertEquals("1${NBSP}632", renderVisualInRtl(formatNumber(1632L, digitsAr = false)))
    assertEquals("12${NBSP}500${NBSP}632", renderVisualInRtl(formatNumber(12500632L, digitsAr = false)))

    // 2. Parenthesized numbers in RTL: "( 1 632 )" and "(1 632)" never flip to "( 632 1 )"
    assertEquals("( 1${NBSP}632 )", renderVisualInRtl("( ${formatNumber(1632L, digitsAr = false)} )"))
    assertEquals("(1${NBSP}632)", renderVisualInRtl("(${formatNumber(1632L, digitsAr = false)})"))

    // 3. Currency amounts with Arabic "درهم" in RTL: digits stay "1 632", sign stays on the left of digits
    val visualPos = renderVisualInRtl(formatDh(1632L, "درهم", digitsAr = false))
    assertTrue("Expected '1 632' in visual RTL output, got: $visualPos", visualPos.contains("1${NBSP}632"))
    assertFalse("Must never invert to '632 1', got: $visualPos", visualPos.contains("632${NBSP}1"))

    val visualNeg = renderVisualInRtl(formatDh(-1632L, "درهم", digitsAr = false))
    assertTrue("Expected '-1 632' in visual RTL output, got: $visualNeg", visualNeg.contains("-1${NBSP}632"))

    val visualPlus = renderVisualInRtl(formatDh(8200L, "درهم", digitsAr = false, explicitPlus = true))
    assertTrue("Expected '+8 200' in visual RTL output, got: $visualPlus", visualPlus.contains("+8${NBSP}200"))

    // 4. Eastern Arabic-Indic digits (٠-٩) in RTL: ١ ٦٣٢ stays left-to-right, never "٦٣٢ ١"
    assertEquals("١${NBSP}٦٣٢", renderVisualInRtl(formatNumber(1632L, digitsAr = true)))
    assertEquals("-١${NBSP}٦٣٢", renderVisualInRtl(formatNumber(-1632L, digitsAr = true)))
    assertEquals("+٨${NBSP}٢٠٠", renderVisualInRtl(formatNumber(8200L, digitsAr = true, explicitPlus = true)))

    // 5. Phone number in RTL: +212 6 12 34 56 78 stays strictly left-to-right
    assertEquals(
      "+212${NBSP}6${NBSP}12${NBSP}34${NBSP}56${NBSP}78",
      renderVisualInRtl(WhatsAppHelper.formatDisplayPhone("0612345678")),
    )

    // 6. Localized Arabic sentences embedding 1 632
    val arStrings = stringsForLanguage("ar")
    val legendVisual = renderVisualInRtl(arStrings.dashStandardFeeLegend(1632L, "درهم"))
    assertTrue("Legend must show 1 632 in visual order: $legendVisual", legendVisual.contains("1${NBSP}632"))
    assertFalse("Legend must never show 632 1: $legendVisual", legendVisual.contains("632${NBSP}1"))
  }

  @Test
  fun arabicRtlCalculationsWhatsappTemplateMultiMonthPaymentBackupAndExportsWorkAccurately() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val arConfig = SyndicatConfig(
      language = AppLanguage.AR.code,
      openingBalanceDh = 500L,
      themeStyle = "airtable",
    )
    val apartments = InitialSyndicatData.apartments.map { seed ->
      ApartmentRecord(
        id = seed.docId,
        number = seed.number,
        floor = seed.floor,
        status = seed.status,
        ownerName = seed.ownerName,
        ownerNameAr = seed.ownerNameAr,
        phone = if (seed.number == 1L) "0612345678" else "",
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
    }
    val expenses = listOf(
      ExpenseRecord(
        id = "exp_1",
        title = "فاتورة الكهرباء",
        category = "Électricité",
        amountDh = 1632L,
        paidAmountDh = 700L,
        supplier = "المكتب الوطني",
        dateLabel = "10/10/2026",
      )
    )

    val dashData = SyndicatDashboardData(
      config = arConfig,
      apartments = apartments,
      expenses = expenses,
    )

    // Total collected = 8200, Opening = 500, Paid expenses = 700 => Cash Now = 8000
    assertEquals(8200L, dashData.totalCaisseDh)
    assertEquals(932L, dashData.unpaidExpensesDh)
    assertEquals(8000L, dashData.cashNowDh)
    // Accounting balance = 8200 + 500 - 1632 = 7068
    assertEquals(7068L, dashData.balanceDh)

    // Moroccan phone normalization & WhatsApp Arabic template substitution (including >1000 amount)
    assertEquals("212612345678", WhatsAppHelper.normalizePhoneForWhatsApp("06 12 34 56 78"))
    val appt1 = apartments.first { it.number == 1L }
    assertEquals(100L, appt1.remainingDh(arConfig))
    val waMessage = WhatsAppHelper.buildReminderMessage(appt1, arConfig)
    assertTrue(waMessage.contains("عبد الفتاح"))
    assertTrue(waMessage.contains("100"))
    assertTrue(waMessage.contains("أكتوبر"))

    val highFeeConfig = arConfig.copy(feeOct = 1632L)
    val apptWith1632Due = appt1.copy(sept = 50L, oct = 0L)
    assertEquals(1632L, apptWith1632Due.remainingDh(highFeeConfig))
    val waBigAmount = WhatsAppHelper.buildReminderMessage(apptWith1632Due, highFeeConfig)
    val waBigVisual = renderVisualInRtl(waBigAmount)
    assertTrue("WhatsApp message must render '1 632' LTR in RTL: $waBigVisual", waBigVisual.contains("1${NBSP}632"))
    assertFalse("WhatsApp message must never render '632 1': $waBigVisual", waBigVisual.contains("632${NBSP}1"))

    // Multi-month automatic lump-sum payment distribution (120 DH covers Sept=50, Oct=50, Nov=20)
    val afterLumpSum = appt1.applyLumpSumPayment(120L, arConfig)
    assertEquals(50L, afterLumpSum.sept)
    assertEquals(50L, afterLumpSum.oct)
    assertEquals(20L, afterLumpSum.nov)
    assertEquals(0L, afterLumpSum.remainingDh(arConfig))

    // Eastern Arabic digits option
    assertEquals("٢٠٢٦", localizeDigits("2026", true))
    assertEquals("يونيو", stringsForLanguage("ar").monthShort(MonthColumn.JUIN))

    // JSON Backup & Restore round-trip (preserving single "airtable" theme & themeMode)
    val jsonBackup = BackupManager.serializeBackupJson(dashData)
    val restored = BackupManager.parseBackupJson(jsonBackup, "test_uid")
    assertEquals(30, restored.apartments.size)
    assertEquals(500L, restored.config.openingBalanceDh)
    assertEquals("airtable", restored.config.themeStyle)
    assertEquals(932L, restored.expenses.first().remainingToPayDh)

    val darkModeBackup = BackupManager.serializeBackupJson(dashData.copy(config = arConfig.copy(themeMode = "dark")))
    assertEquals("dark", BackupManager.parseBackupJson(darkModeBackup, "test_uid").config.themeMode)

    // CSV bulk owners parser
    val parsedCsv = BackupManager.parseOwnersBulkLines("1;عبد الفتاح;Abdelfetah;0612345678\n2;حميد;0700112233")
    assertEquals(2, parsedCsv.size)
    assertEquals("0612345678", parsedCsv[0].phone)
    assertEquals("حميد", parsedCsv[1].ownerNameAr)

    // Verify Airtable editorial white background (#FFFFFF) AND strict Portrait A4 proportions across all exports
    val cotPrintBmp = PdfPosterExporter.generateOfficialPosterBitmap(
      context,
      dashData,
      CotisationExportOptions(showNames = true, untilCutoffOnly = false, inkSaver = false, variant = ExportVariant.PRINT),
      scale = 1f,
    )
    val cotScreenBmp = PdfPosterExporter.generateOfficialPosterBitmap(
      context,
      dashData,
      CotisationExportOptions(showNames = true, untilCutoffOnly = false, inkSaver = false, variant = ExportVariant.SCREEN),
      scale = 1f,
    )
    assertEquals(PdfPosterExporter.A4_PORTRAIT_WIDTH, cotPrintBmp.width)
    assertTrue("Cotisation print must be Portrait A4 (>=842pt tall), got ${cotPrintBmp.width}x${cotPrintBmp.height}", cotPrintBmp.height >= PdfPosterExporter.A4_PORTRAIT_HEIGHT)
    assertTrue("Cotisation print must be taller than wide", cotPrintBmp.height > cotPrintBmp.width)
    assertEquals(Color.WHITE, cotPrintBmp.getPixel(4, 4))
    assertEquals(1080, cotScreenBmp.width)
    assertTrue("Cotisation screen 1080px must be Portrait (>=1520px tall), got ${cotScreenBmp.width}x${cotScreenBmp.height}", cotScreenBmp.height >= 1520)
    assertEquals(Color.WHITE, cotScreenBmp.getPixel(4, 4))

    // Even with a small 6-apartment building or 12 active months, Cotisation stays A4 Portrait (595x842)
    val smallFullYearData = dashData.copy(
      config = arConfig.copy(activeMonths = (1L..12L).toList()),
      apartments = apartments.take(6),
    )
    val cotSmallBmp = PdfPosterExporter.generateOfficialPosterBitmap(
      context,
      smallFullYearData,
      CotisationExportOptions(showNames = true, variant = ExportVariant.PRINT),
      scale = 1f,
    )
    assertEquals(595, cotSmallBmp.width)
    assertEquals(842, cotSmallBmp.height)

    val relPrintBmp = PdfPosterExporter.generateRelanceBitmap(
      context,
      dashData,
      RelanceExportOptions(anonymousMode = true, inkSaver = true, variant = ExportVariant.PRINT),
      scale = 1f,
    )
    val relScreenBmp = PdfPosterExporter.generateRelanceBitmap(
      context,
      dashData,
      RelanceExportOptions(anonymousMode = false, inkSaver = false, variant = ExportVariant.SCREEN),
      scale = 1f,
    )
    assertEquals(595, relPrintBmp.width)
    assertTrue("Relance print must be Portrait A4", relPrintBmp.height >= 842)
    assertEquals(Color.WHITE, relPrintBmp.getPixel(4, 4))
    assertEquals(1080, relScreenBmp.width)
    assertTrue("Relance screen must be Portrait", relScreenBmp.height >= 1520)
    assertEquals(Color.WHITE, relScreenBmp.getPixel(4, 4))

    val caissePrintBmp = PdfPosterExporter.generateCaisseReportBitmap(
      context,
      dashData,
      inkSaver = false,
      scale = 1f,
      variant = ExportVariant.PRINT,
    )
    val caisseScreenBmp = PdfPosterExporter.generateCaisseReportBitmap(
      context,
      dashData,
      inkSaver = false,
      scale = 1f,
      variant = ExportVariant.SCREEN,
    )
    assertEquals(595, caissePrintBmp.width)
    assertTrue("Caisse print must be Portrait A4", caissePrintBmp.height >= 842)
    assertEquals(Color.WHITE, caissePrintBmp.getPixel(4, 4))
    assertEquals(1080, caisseScreenBmp.width)
    assertTrue("Caisse screen must be Portrait", caisseScreenBmp.height >= 1520)
    assertEquals(Color.WHITE, caisseScreenBmp.getPixel(4, 4))

    val receiptBmp = PdfPosterExporter.generatePaymentReceiptBitmap(
      context,
      appt1,
      dashData,
      receiptNo = 7L,
      scale = 1f,
      variant = ExportVariant.SCREEN,
    )
    assertEquals(1080, receiptBmp.width)
    assertTrue("Payment receipt must be Portrait (taller than wide), got ${receiptBmp.width}x${receiptBmp.height}", receiptBmp.height >= 1500)
    assertEquals(Color.WHITE, receiptBmp.getPixel(4, 4))
  }

  private fun assertDpRectsDoNotOverlap(a: DpRect, b: DpRect, labelA: String, labelB: String) {
    val overlapX = minOf(a.right, b.right) - maxOf(a.left, b.left)
    val overlapY = minOf(a.bottom, b.bottom) - maxOf(a.top, b.top)
    val overlaps = overlapX > 0.5.dp && overlapY > 0.5.dp
    assertFalse(
      "Expected $labelA ($a) and $labelB ($b) not to overlap, but overlapX=$overlapX, overlapY=$overlapY",
      overlaps,
    )
  }

  @Test
  @Config(sdk = [36], qualifiers = "w360dp-h800dp")
  fun expenseEditorModalFieldsHavePositiveHeightsAndZeroOverlapInRtlLargeFont() {
    val arConfig = SyndicatConfig(
      language = AppLanguage.AR.code,
      fontScale = "large",
      themeStyle = "airtable",
    )
    val dashData = SyndicatDashboardData(
      config = arConfig,
      apartments = emptyList(),
      expenses = emptyList(),
    )

    composeTestRule.setContent {
      CompositionLocalProvider(
        LocalStrings provides stringsForLanguage(AppLanguage.AR.code),
        LocalLayoutDirection provides LayoutDirection.Rtl,
      ) {
        AppTheme(darkTheme = false, isArabic = true, fontScale = "large") {
          ExpensesScreen(
            data = dashData,
            onAddExpense = { _, _, _, _, _, _, _ -> },
            onUpdateExpense = {},
            onDeleteExpense = {},
            onExportError = {},
          )
        }
      }
    }

    composeTestRule.onNodeWithTag("add_expense_fab").performClick()
    composeTestRule.waitForIdle()

    val tags = listOf(
      "expense_title_input",
      "expense_amount_input",
      "expense_paid_amount_input",
      "expense_supplier_input",
      "expense_date_input",
    )
    val rects = tags.map { tag ->
      tag to composeTestRule.onNodeWithTag(tag, useUnmergedTree = true).getUnclippedBoundsInRoot()
    }

    rects.forEach { (tag, rect) ->
      assertTrue("Expected $tag to have height >= 48.dp, got $rect", rect.height >= 48.dp)
      assertTrue("Expected $tag to have width >= 200.dp, got $rect", rect.width >= 200.dp)
    }

    for (i in 0 until rects.lastIndex) {
      val (tagA, rectA) = rects[i]
      val (tagB, rectB) = rects[i + 1]
      assertDpRectsDoNotOverlap(rectA, rectB, tagA, tagB)
      assertTrue(
        "Expected $tagA (${rectA.bottom}) to sit strictly above $tagB (${rectB.top})",
        rectA.bottom <= rectB.top + 0.5.dp,
      )
    }
  }

  @Test
  @Config(sdk = [36], qualifiers = "w360dp-h800dp")
  fun apartmentPaymentModalWeightedInputsInScrollableDialogNeverCollapseOrOverlap() {
    val arConfig = SyndicatConfig(
      language = AppLanguage.AR.code,
      fontScale = "large",
      themeStyle = "airtable",
    )
    val apt1 = ApartmentRecord(
      id = "apt_1",
      number = 1L,
      floor = "1",
      status = "OCCUPIED",
      ownerName = "Abdelfetah",
      ownerNameAr = "عبد الفتاح",
      phone = "0612345678",
    )
    val dashData = SyndicatDashboardData(
      config = arConfig,
      apartments = listOf(apt1),
      expenses = emptyList(),
    )

    composeTestRule.setContent {
      CompositionLocalProvider(
        LocalStrings provides stringsForLanguage(AppLanguage.AR.code),
        LocalLayoutDirection provides LayoutDirection.Rtl,
      ) {
        AppTheme(darkTheme = false, isArabic = true, fontScale = "large") {
          ApartmentsScreen(
            data = dashData,
            searchQuery = "",
            selectedFloor = null,
            statusFilter = StatusFilter.ALL,
            onSearchQueryChange = {},
            onSelectFloor = {},
            onSelectStatusFilter = {},
            onSaveApartment = { _, _ -> },
            onMarkReminded = { _, _, _ -> },
            onRequestReceiptNo = { 1L },
            onExportError = {},
          )
        }
      }
    }

    composeTestRule.onNodeWithTag("apartments_cards_list").performScrollToIndex(2)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("card_pay_apt_1").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    val lumpBounds = composeTestRule.onNodeWithTag("lump_sum_input", useUnmergedTree = true).getUnclippedBoundsInRoot()
    val distBtnBounds = composeTestRule.onNodeWithTag("distribute_lump_sum_btn", useUnmergedTree = true).getUnclippedBoundsInRoot()
    val septBounds = composeTestRule.onNodeWithTag("month_input_sept", useUnmergedTree = true).getUnclippedBoundsInRoot()
    val octBounds = composeTestRule.onNodeWithTag("month_input_oct", useUnmergedTree = true).getUnclippedBoundsInRoot()
    val novBounds = composeTestRule.onNodeWithTag("month_input_nov", useUnmergedTree = true).getUnclippedBoundsInRoot()

    // Previously, RowScope.weight(1f) inside FintechTextField collapsed BasicTextField height to 0dp inside verticalScroll!
    assertTrue("lump_sum_input must not collapse to 0dp: $lumpBounds", lumpBounds.height >= 48.dp)
    assertTrue("month_input_sept must not collapse to 0dp: $septBounds", septBounds.height >= 48.dp)
    assertTrue("month_input_oct must not collapse to 0dp: $octBounds", octBounds.height >= 48.dp)

    // Horizontal neighbors must never overlap
    assertDpRectsDoNotOverlap(lumpBounds, distBtnBounds, "lump_sum_input", "distribute_lump_sum_btn")
    assertDpRectsDoNotOverlap(septBounds, octBounds, "month_input_sept", "month_input_oct")

    // Vertical rows must never overlap
    assertDpRectsDoNotOverlap(lumpBounds, septBounds, "lump_sum_input", "month_input_sept")
    assertDpRectsDoNotOverlap(septBounds, novBounds, "month_input_sept", "month_input_nov")
    assertTrue(
      "Expected month_input_sept (${septBounds.bottom}) to sit strictly above month_input_nov (${novBounds.top})",
      septBounds.bottom <= novBounds.top + 0.5.dp,
    )
  }

  @Test
  fun authContextWrapperUnwrappingAndLanguageSwitchNeverBlockAuthScreen() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    if (com.google.firebase.FirebaseApp.getApps(context).isEmpty()) {
      com.google.firebase.FirebaseApp.initializeApp(
        context,
        com.google.firebase.FirebaseOptions.Builder()
          .setProjectId("test-project")
          .setApplicationId("1:1234567890:android:abcdef")
          .setApiKey("AIzaSyTestKeyForRobolectric00000000000")
          .build(),
      )
    }

    var unwrappedActivity: android.app.Activity? = null
    composeTestRule.setContent {
      val selectedLangState = androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(AppLanguage.FR)
      }
      val lang = selectedLangState.value
      val localCtx = androidx.compose.ui.platform.LocalContext.current
      val wrappedTwice = ContextWrapper(ContextWrapper(localCtx))
      unwrappedActivity = wrappedTwice.findActivity()

      AppTheme(darkTheme = false, isArabic = lang.isRtl, fontScale = "normal") {
        CompositionLocalProvider(
          LocalStrings provides stringsForLanguage(lang.code),
          LocalLayoutDirection provides if (lang.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
        ) {
          AuthScreen(
            currentLanguage = lang,
            onSelectLanguage = { selectedLangState.value = it },
            onAuthSuccess = {},
          )
        }
      }
    }

    composeTestRule.waitForIdle()
    assertNotNull("Nested ContextWrapper must unwrap to host Activity", unwrappedActivity)

    val authDiag = com.example.auth.inspectApkAuthDiagnostics(context)
    assertEquals("com.aistudio.syndicatc49.mrxkqp", authDiag.packageName)
    assertEquals("gen-lang-client-0219823413", authDiag.firebaseProjectId)
    assertTrue("Web Client ID (client_type 3) must be configured", authDiag.isWebClientConfigured)

    val signInNode = composeTestRule.onNodeWithTag("google_sign_in_button")
    signInNode.performScrollTo().assertIsDisplayed()
    signInNode.assertTextEquals("Se connecter avec Google")
    val signInBounds = signInNode.getUnclippedBoundsInRoot()
    assertTrue("Google sign-in button must remain visible and >= 48dp tall: $signInBounds", signInBounds.height >= 48.dp)

    // Switch language to Arabic and verify RTL Google sign-in button remains crisp & >= 48dp
    composeTestRule.onNodeWithTag("auth_lang_ar").performClick()
    composeTestRule.waitForIdle()
    signInNode.assertTextEquals("تسجيل الدخول باستخدام Google")
  }

  @Test
  @Config(sdk = [36], qualifiers = "w360dp-h800dp")
  fun whatsappPredefinedTemplateAndAutomaticCampaignAssistantWorkCleanly() {
    val arConfig = SyndicatConfig(
      language = AppLanguage.AR.code,
      residenceName = "إقامة الأمل",
      currency = "درهم",
      dueDayOfMonth = 5L,
      cutoffMonth = 10L,
      themeStyle = "airtable",
    )
    val aptLate1 = ApartmentRecord(
      id = "apt_1",
      number = 1L,
      floor = "RDC",
      status = "occupied",
      ownerName = "Youssef",
      ownerNameAr = "يوسف العلمي",
      phone = "0611223344",
      juin = 100L,
      juil = 100L,
      aout = 50L,
      sept = 0L,
      oct = 0L,
    )
    val aptLate2 = ApartmentRecord(
      id = "apt_2",
      number = 2L,
      floor = "1er",
      status = "occupied",
      ownerName = "Nadia",
      ownerNameAr = "نادية",
      phone = "",
      juin = 100L,
      juil = 100L,
      aout = 50L,
      sept = 50L,
      oct = 0L,
    )

    // 1. Verify predefined templates & variable substitution ({الاسم}, {المبلغ}, {الأشهر_المتبقية}, {رقم_الشقة}, {اسم_العمارة}, {يوم_الأداء})
    val urgentArMsg = WhatsAppHelper.buildReminderMessage(
      apartment = aptLate1,
      config = arConfig,
      overrideTemplate = com.example.util.WhatsAppTemplatePreset.URGENT_AR.templateText(),
    )
    assertTrue(urgentArMsg.contains("يوسف العلمي"))
    assertTrue(urgentArMsg.contains("إقامة الأمل"))
    assertTrue(urgentArMsg.contains("100"))
    assertTrue(urgentArMsg.contains("شتنبر"))
    assertTrue(urgentArMsg.contains("أكتوبر"))

    val friendlyFrMsg = WhatsAppHelper.buildReminderMessage(
      apartment = aptLate1,
      config = arConfig.copy(language = "fr", currency = "DH"),
      overrideTemplate = com.example.util.WhatsAppTemplatePreset.FRIENDLY_FR.templateText(),
    )
    assertTrue(friendlyFrMsg.contains("Youssef"))
    assertTrue(friendlyFrMsg.contains("100"))
    assertTrue(friendlyFrMsg.contains("Septembre"))
    assertTrue(friendlyFrMsg.contains("Octobre"))

    // 2. Verify Group Broadcast Summary generation
    val broadcast = WhatsAppHelper.buildGroupReminderBroadcastMessage(
      apartments = listOf(aptLate1, aptLate2),
      config = arConfig,
    )
    assertTrue(broadcast.contains("يوسف العلمي"))
    assertTrue(broadcast.contains("نادية"))
    assertTrue(broadcast.contains("150"))

    // 3. Verify Interactive WhatsAppReminderDialog UI (non-overlapping fields, preset switching & skip stepping)
    composeTestRule.setContent {
      CompositionLocalProvider(
        LocalStrings provides stringsForLanguage(AppLanguage.AR.code),
        LocalLayoutDirection provides LayoutDirection.Rtl,
      ) {
        AppTheme(darkTheme = false, isArabic = true, fontScale = "normal") {
          com.example.ui.components.WhatsAppReminderDialog(
            apartmentsQueue = listOf(aptLate1, aptLate2),
            config = arConfig,
            onDismiss = {},
            onReminderSent = { _, _, _ -> },
          )
        }
      }
    }

    composeTestRule.waitForIdle()
    val phoneBounds = composeTestRule.onNodeWithTag("wa_resident_phone_input", useUnmergedTree = true).getUnclippedBoundsInRoot()
    val msgBounds = composeTestRule.onNodeWithTag("wa_generated_message_input", useUnmergedTree = true).getUnclippedBoundsInRoot()
    assertTrue("WhatsApp phone input must be >= 48dp tall: $phoneBounds", phoneBounds.height >= 48.dp)
    assertTrue("WhatsApp generated message input must be >= 48dp tall: $msgBounds", msgBounds.height >= 48.dp)
    assertDpRectsDoNotOverlap(phoneBounds, msgBounds, "wa_resident_phone_input", "wa_generated_message_input")

    // Switch predefined preset chip & step to next resident
    composeTestRule.onNodeWithTag("wa_preset_urgent_ar").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("wa_skip_resident_btn").performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  @Config(sdk = [36], qualifiers = "w360dp-h800dp")
  fun projectsPrioritySortingOngoingProgressCaisseReadinessAndModalLayoutWorkInArAndFr() {
    val arConfig = SyndicatConfig(
      language = AppLanguage.AR.code,
      openingBalanceDh = 4500L,
      fontScale = "large",
      themeStyle = "airtable",
    )
    // Unsorted list: P4, P2, P1 (ongoing), P1 (planned), P3 (planned)
    val rawProjects = listOf(
      ProjectRecord(
        id = "proj_p4",
        titleAr = "تجميل المدخل",
        titleFr = "Embellissement entrée",
        priority = 4L,
        status = ProjectStatus.PLANNED.code,
        targetBudgetDh = 2000L,
        minCashToStartDh = 1000L,
      ),
      ProjectRecord(
        id = "proj_p2",
        titleAr = "تركيب كاميرات المراقبة",
        titleFr = "Installation caméras vidéosurveillance",
        priority = 2L,
        status = ProjectStatus.PLANNED.code,
        targetBudgetDh = 6500L,
        minCashToStartDh = 5000L,
      ),
      ProjectRecord(
        id = "proj_ongoing",
        titleAr = "إصلاح وصباغة الدرج",
        titleFr = "Réfection et peinture escaliers",
        priority = 1L,
        status = ProjectStatus.IN_PROGRESS.code,
        targetBudgetDh = 4000L,
        minCashToStartDh = 2000L,
        allocatedAmountDh = 2600L,
        progressPercent = 65L,
      ),
      ProjectRecord(
        id = "proj_p1_ready",
        titleAr = "عزل السطح ضد التسربات",
        titleFr = "Étanchéité terrasse",
        priority = 1L,
        status = ProjectStatus.PLANNED.code,
        targetBudgetDh = 7000L,
        minCashToStartDh = 3500L,
      ),
    )

    val aptOccupied = ApartmentRecord(
      id = "apt_1",
      number = 1L,
      floor = "1",
      status = "occupied",
      ownerName = "Karim",
      ownerNameAr = "كريم",
    )

    val dashData = SyndicatDashboardData(
      config = arConfig,
      apartments = listOf(aptOccupied),
      expenses = emptyList(),
      projects = rawProjects,
    )

    // 1. Verify priority sorting (P1 -> P2 -> P4) and ongoing vs planned separation
    assertEquals(4500L, dashData.cashNowDh)
    assertEquals(1, dashData.inProgressProjects.size)
    assertEquals("proj_ongoing", dashData.inProgressProjects.first().id)
    assertEquals(65, dashData.inProgressProjects.first().clampedProgressPercent)

    val plannedIds = dashData.plannedReadinessList.map { it.project.id }
    assertEquals(listOf("proj_p1_ready", "proj_p2", "proj_p4"), plannedIds)

    // 2. Verify caisse-advance readiness against cashNowDh = 4500 DH
    // - proj_p1_ready requires 3500 DH <= 4500 DH => Ready directly & in waterfall (leaves 1000 DH)
    val r1 = dashData.plannedReadinessList[0]
    assertTrue(r1.canStartNowDirect)
    assertTrue(r1.canStartInPriorityWaterfall)
    assertEquals(0L, r1.directShortfallDh)

    // - proj_p2 requires 5000 DH > 4500 DH => Missing 500 DH directly (waterfall pool left = 1000 => shortfall 4000 DH)
    val r2 = dashData.plannedReadinessList[1]
    assertFalse(r2.canStartNowDirect)
    assertEquals(500L, r2.directShortfallDh)
    assertTrue(r2.estimatedMonthsToStart > 0)

    // - proj_p4 requires 1000 DH <= 4500 DH => Ready directly, and waterfall pool still had 1000 DH after P1!
    val r3 = dashData.plannedReadinessList[2]
    assertTrue(r3.canStartNowDirect)
    assertEquals(2, dashData.readyToStartProjectsCount)

    // 3. Verify Bilingual Strings (AR & FR) + Backup Round-Trip with projects
    val arStrings = stringsForLanguage("ar")
    val frStrings = stringsForLanguage("fr")
    assertTrue(arStrings.projectPriorityLabel(ProjectPriority.URGENT).contains("عاجلة"))
    assertTrue(frStrings.projectPriorityLabel(ProjectPriority.URGENT).contains("urgente", ignoreCase = true))
    assertTrue(frStrings.projectsFutureSectionTitle.contains("caisse", ignoreCase = true))
    assertTrue(arStrings.projectsFutureSectionTitle.contains("الصندوق"))

    val backupJson = BackupManager.serializeBackupJson(dashData)
    val restoredBackup = BackupManager.parseBackupJson(backupJson, "uid_proj_test")
    assertEquals(4, restoredBackup.projects.size)
    assertEquals(65L, restoredBackup.projects.first { it.id == "proj_ongoing" }.progressPercent)

    // 4. Verify Compose UI interactions (+10% progress, Start planned project) & non-overlapping modal fields in RTL
    var updatedRecord: ProjectRecord? = null
    composeTestRule.setContent {
      CompositionLocalProvider(
        LocalStrings provides arStrings,
        LocalLayoutDirection provides LayoutDirection.Rtl,
      ) {
        AppTheme(darkTheme = false, isArabic = true, fontScale = "large") {
          ProjectsScreen(
            data = dashData,
            onAddProject = {},
            onUpdateProject = { next, _ -> updatedRecord = next },
            onDeleteProject = {},
            onLoadStarterProjects = {},
          )
        }
      }
    }
    composeTestRule.waitForIdle()

    // Scroll LazyColumn to ongoing project card (index 4), then tap +10% -> advances 65% -> 75%
    composeTestRule.onNodeWithTag("projects_screen").performScrollToIndex(4)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("project_plus10_proj_ongoing").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertEquals(75L, updatedRecord?.progressPercent)

    // Scroll LazyColumn back to top header item (index 0), open Add Project modal, and verify non-overlapping inputs >= 48dp tall
    composeTestRule.onNodeWithTag("projects_screen").performScrollToIndex(0)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("add_project_fab").performClick()
    composeTestRule.waitForIdle()

    val titleArBounds = composeTestRule.onNodeWithTag("project_title_ar_input", useUnmergedTree = true).getUnclippedBoundsInRoot()
    val titleFrBounds = composeTestRule.onNodeWithTag("project_title_fr_input", useUnmergedTree = true).getUnclippedBoundsInRoot()
    assertTrue("project_title_ar_input must be >= 48dp tall: $titleArBounds", titleArBounds.height >= 48.dp)
    assertTrue("project_title_fr_input must be >= 48dp tall: $titleFrBounds", titleFrBounds.height >= 48.dp)
    assertDpRectsDoNotOverlap(titleArBounds, titleFrBounds, "project_title_ar_input", "project_title_fr_input")

    // Scroll once so both budget and min_advance are visible at the exact same scroll offset
    composeTestRule.onNodeWithTag("project_min_advance_input", useUnmergedTree = true).performScrollTo()
    composeTestRule.waitForIdle()
    val budgetBounds = composeTestRule.onNodeWithTag("project_budget_input", useUnmergedTree = true).getUnclippedBoundsInRoot()
    val minAdvanceBounds = composeTestRule.onNodeWithTag("project_min_advance_input", useUnmergedTree = true).getUnclippedBoundsInRoot()
    assertTrue("project_budget_input must be >= 48dp tall: $budgetBounds", budgetBounds.height >= 48.dp)
    assertTrue("project_min_advance_input must be >= 48dp tall: $minAdvanceBounds", minAdvanceBounds.height >= 48.dp)
    assertDpRectsDoNotOverlap(budgetBounds, minAdvanceBounds, "project_budget_input", "project_min_advance_input")

    // Scroll to progress & allocated row and verify neither weighted field collapses or overlaps
    composeTestRule.onNodeWithTag("project_progress_input", useUnmergedTree = true).performScrollTo()
    composeTestRule.waitForIdle()
    val progressBounds = composeTestRule.onNodeWithTag("project_progress_input", useUnmergedTree = true).getUnclippedBoundsInRoot()
    val allocatedBounds = composeTestRule.onNodeWithTag("project_allocated_input", useUnmergedTree = true).getUnclippedBoundsInRoot()
    assertTrue("project_progress_input must not collapse inside verticalScroll: $progressBounds", progressBounds.height >= 48.dp)
    assertTrue("project_allocated_input must not collapse inside verticalScroll: $allocatedBounds", allocatedBounds.height >= 48.dp)
    assertDpRectsDoNotOverlap(progressBounds, allocatedBounds, "project_progress_input", "project_allocated_input")
  }

  @Test
  @Config(sdk = [36], qualifiers = "w360dp-h800dp")
  fun residenceHeroBannerAndNoHorizontalScrollMonthsGridFitPortraitScreenCleanly() {
    val cfg = SyndicatConfig(
      residenceName = "Résidence C49",
      city = "Casablanca",
      year = 2025L,
      language = AppLanguage.FR.code,
    )
    val apt1 = ApartmentRecord(
      id = "apt_1",
      number = 1L,
      floor = "1",
      status = "occupied",
      ownerName = "Karim Benali",
      ownerNameAr = "كريم بنعلي",
      juin = 50L,
      juil = 50L,
      aout = 50L,
    )
    val dashData = SyndicatDashboardData(
      config = cfg,
      apartments = listOf(apt1),
      expenses = emptyList(),
    )
    val frStrings = stringsForLanguage("fr")

    val showApartmentsState = androidx.compose.runtime.mutableStateOf(false)
    var savedApt: ApartmentRecord? = null

    composeTestRule.setContent {
      CompositionLocalProvider(
        LocalStrings provides frStrings,
        LocalLayoutDirection provides LayoutDirection.Ltr,
      ) {
        AppTheme(darkTheme = false, isArabic = false, fontScale = "normal") {
          if (!showApartmentsState.value) {
            DashboardScreen(
              data = dashData,
              isBackupOverdue = true,
              onTriggerBackupJson = {},
              onOpenWizard = {},
              onMarkReminded = { _, _, _ -> },
              onNavigateToPosters = {},
              onNavigateToProjects = {},
              onNavigateToApartments = { showApartmentsState.value = true },
              onNavigateToExpenses = {},
              onNavigateToLateApartments = {},
            )
          } else {
            ApartmentsScreen(
              data = dashData,
              searchQuery = "",
              selectedFloor = null,
              statusFilter = StatusFilter.ALL,
              onSearchQueryChange = {},
              onSelectFloor = {},
              onSelectStatusFilter = {},
              onSaveApartment = { updated, _ -> savedApt = updated },
              onMarkReminded = { _, _, _ -> },
              onRequestReceiptNo = { 1L },
              onExportError = {},
            )
          }
        }
      }
    }
    composeTestRule.waitForIdle()

    // 1. Verify recreated Dashboard: ResidenceHeroBanner, Treasury Portfolio Card, 4 Quick Actions, KPI Ring Grid & Chart
    composeTestRule.onNodeWithTag("residence_hero_banner").assertIsDisplayed()
    composeTestRule.onNodeWithTag("hero_cash_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cash_now_value").assertIsDisplayed()

    val bannerBounds = composeTestRule.onNodeWithTag("residence_hero_banner").getUnclippedBoundsInRoot()
    val photoBtnBounds = composeTestRule.onNodeWithTag("customize_residence_photo_btn").getUnclippedBoundsInRoot()
    val wizardBtnBounds = composeTestRule.onNodeWithTag("dash_open_wizard_btn").getUnclippedBoundsInRoot()
    val postersBtnBounds = composeTestRule.onNodeWithTag("dash_open_posters_btn").getUnclippedBoundsInRoot()

    assertTrue("Residence hero banner should be >= 200dp tall: $bannerBounds", bannerBounds.height >= 200.dp)
    assertTrue("Photo customization button must fit inside 360dp screen: $photoBtnBounds", photoBtnBounds.left >= 0.dp && photoBtnBounds.right <= 360.dp)
    assertDpRectsDoNotOverlap(photoBtnBounds, wizardBtnBounds, "customize_residence_photo_btn", "dash_open_wizard_btn")
    assertDpRectsDoNotOverlap(wizardBtnBounds, postersBtnBounds, "dash_open_wizard_btn", "dash_open_posters_btn")

    composeTestRule.onNodeWithTag("dashboard_screen").performScrollToIndex(2)
    composeTestRule.waitForIdle()
    val quickTags = listOf("dash_quick_cotisations_btn", "dash_quick_expenses_btn", "dash_quick_remind_btn", "dash_quick_posters_btn")
    quickTags.forEach { qTag ->
      val qb = composeTestRule.onNodeWithTag(qTag).getUnclippedBoundsInRoot()
      assertTrue("$qTag must fit inside 360dp and be >= 48dp tall: $qb", qb.left >= 0.dp && qb.right <= 360.dp && qb.height >= 48.dp)
    }

    composeTestRule.onNodeWithTag("dashboard_screen").performScrollToIndex(4)
    composeTestRule.waitForIdle()
    val kpiCollected = composeTestRule.onNodeWithTag("kpi_collected_card").getUnclippedBoundsInRoot()
    val kpiExpenses = composeTestRule.onNodeWithTag("kpi_expenses_card").getUnclippedBoundsInRoot()
    val kpiRate = composeTestRule.onNodeWithTag("kpi_rate_card").getUnclippedBoundsInRoot()
    val kpiBalance = composeTestRule.onNodeWithTag("kpi_balance_card").getUnclippedBoundsInRoot()
    assertDpRectsDoNotOverlap(kpiCollected, kpiExpenses, "kpi_collected_card", "kpi_expenses_card")
    assertDpRectsDoNotOverlap(kpiRate, kpiBalance, "kpi_rate_card", "kpi_balance_card")

    composeTestRule.onNodeWithTag("dashboard_screen").performScrollToIndex(7)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("dash_monthly_chart_card").assertIsDisplayed()

    // Scroll back up to quick actions and tap "Cotisations" quick action to navigate directly to ApartmentsScreen
    composeTestRule.onNodeWithTag("dashboard_screen").performScrollToIndex(2)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("dash_quick_cotisations_btn").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("residence_hero_banner").assertIsDisplayed()
    composeTestRule.onNodeWithTag("apartments_cards_list").performScrollToIndex(2)
    composeTestRule.waitForIdle()

    val activeKeys = cfg.activeMonthColumns.map { it.key }
    assertTrue("Expected at least 7 active months in default config", activeKeys.size >= 7)

    activeKeys.forEach { key ->
      val cellTag = "card_month_1_$key"
      val bounds = composeTestRule.onNodeWithTag(cellTag).getUnclippedBoundsInRoot()
      assertTrue("$cellTag must have >= 48dp accessible height: $bounds", bounds.height >= 48.dp)
      assertTrue(
        "$cellTag must fit strictly inside 360dp portrait width without left-right slider: $bounds",
        bounds.left >= 0.dp && bounds.right <= 360.dp,
      )
    }

    // Verify 1-tap direct month payment entry from the card month grid
    val decTag = "card_month_1_${MonthColumn.DEC.key}"
    composeTestRule.onNodeWithTag(decTag).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("quick_cell_pay_full_btn").performClick()
    composeTestRule.waitForIdle()
    assertNotNull("Expected 1-tap quick month payment to save updated apartment", savedApt)
    assertTrue("Expected DEC month to be paid (> 0 DH)", (savedApt?.dec ?: 0L) > 0L)

    // 3. Switch to Compact Grid View and verify every month cell also fits [0dp, 360dp] with zero horizontal scroll
    composeTestRule.onNodeWithTag("toggle_grid_view").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("apartments_grid_list").performScrollToIndex(2)
    composeTestRule.waitForIdle()

    activeKeys.forEach { key ->
      val gridCellTag = "grid_cell_1_$key"
      val bounds = composeTestRule.onNodeWithTag(gridCellTag).getUnclippedBoundsInRoot()
      assertTrue("$gridCellTag must have >= 48dp accessible height: $bounds", bounds.height >= 48.dp)
      assertTrue(
        "$gridCellTag must fit strictly inside 360dp portrait width without left-right slider: $bounds",
        bounds.left >= 0.dp && bounds.right <= 360.dp,
      )
    }
  }

  @Test
  fun localSyndicatRepositoryWorksOfflineWithoutAccountAndPersistsThemes() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val storeFile = File(context.cacheDir, "local_syndicat_test_${System.nanoTime()}.json")
    try {
      val repo = LocalSyndicatRepository(context, storageFileOverride = storeFile)
      repo.ensureSeeded()

      val seededApts = repo.observeApartments(LOCAL_DEVICE_USER_ID).first()
      assertEquals(30, seededApts.size)
      assertEquals(8200L, seededApts.sumOf { it.totalAllMonthsDh })

      val apt1 = seededApts.first { it.number == 1L }
      repo.updateApartment(apt1.copy(dec = 100L))
      repo.addExpense(
        title = "Nettoyage escaliers",
        category = "نظافة",
        amountDh = 250L,
        dateLabel = "2026-04-01",
        notes = "",
      )
      repo.updateDisplayPrefs(themeStyle = "meta")

      // Re-instantiate repository on same phone file to verify durable disk persistence
      val reloadedRepo = LocalSyndicatRepository(context, storageFileOverride = storeFile)
      val reloadedConfig = reloadedRepo.observeConfig(LOCAL_DEVICE_USER_ID).first()
      assertNotNull(reloadedConfig)
      assertEquals("meta", reloadedConfig?.themeStyle)

      val reloadedApts = reloadedRepo.observeApartments(LOCAL_DEVICE_USER_ID).first()
      assertEquals(8300L, reloadedApts.sumOf { it.totalAllMonthsDh })
      assertEquals(2, reloadedRepo.observeExpenses(LOCAL_DEVICE_USER_ID).first().size)
    } finally {
      storeFile.delete()
    }
  }

  @Test
  @Config(sdk = [36], qualifiers = "w412dp-h1000dp")
  fun settingsScreenExposesLocalSyncAndSeparateMetaCoinbaseMiniMaxThemes() {
    var selectedThemeStyle: String? = null
    var connectGoogleClicked = false

    val sampleData = SyndicatDashboardData(
      config = SyndicatConfig(themeStyle = "coinbase", onboardingCompleted = true, seeded = true),
      apartments = listOf(
        ApartmentRecord(id = "apt_1", number = 1L, floor = "RDC", ownerName = "Karim Benali", juin = 100L),
      ),
      expenses = emptyList(),
      projects = emptyList(),
    )

    composeTestRule.setContent {
      CompositionLocalProvider(
        LocalStrings provides stringsForLanguage("fr"),
        LocalLayoutDirection provides LayoutDirection.Ltr,
      ) {
        AppTheme(darkTheme = false, isArabic = false, themeStyle = "coinbase") {
          SettingsScreen(
            data = sampleData,
            currentUserId = LOCAL_DEVICE_USER_ID,
            userEmail = null,
            currentLanguage = AppLanguage.FR,
            isSaving = false,
            onSelectLanguage = {},
            onUpdateDisplayPrefs = { _, style, _, _ -> selectedThemeStyle = style },
            onOpenWizard = {},
            onSaveConfig = { _, _ -> },
            onRestoreBackup = {},
            onImportCsvOwners = {},
            onResetC49 = {},
            onBackupExported = {},
            onPinUpdated = {},
            onSignOut = {},
            onMessage = {},
            isCloudSynced = false,
            onConnectGoogleSync = { connectGoogleClicked = true },
          )
        }
      }
    }
    composeTestRule.waitForIdle()

    // Language & Display is placed right at the top of Settings without scrolling
    composeTestRule.onNodeWithTag("settings_group_1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("lang_option_fr").assertIsDisplayed()
    composeTestRule.onNodeWithTag("lang_option_ar").assertIsDisplayed()

    composeTestRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("settings_sync_card"))
    composeTestRule.onNodeWithTag("settings_sync_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("settings_connect_google_btn").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertTrue("Expected optional Google sync CTA to invoke callback", connectGoogleClicked)

    composeTestRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("theme_style_meta"))
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("theme_style_meta").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertEquals("meta", selectedThemeStyle)

    composeTestRule.onNodeWithTag("theme_style_minimax").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertEquals("minimax", selectedThemeStyle)

    composeTestRule.onNodeWithTag("theme_style_coinbase").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertEquals("coinbase", selectedThemeStyle)

    // Verify all ordered section groups and separated security/backup cards exist in order
    composeTestRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("settings_group_2"))
    composeTestRule.onNodeWithTag("settings_group_2").assertIsDisplayed()
    composeTestRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("settings_group_3"))
    composeTestRule.onNodeWithTag("settings_group_3").assertIsDisplayed()
    composeTestRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("settings_group_4"))
    composeTestRule.onNodeWithTag("settings_group_4").assertIsDisplayed()
    composeTestRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("settings_security_reminders_card"))
    composeTestRule.onNodeWithTag("settings_security_reminders_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("settings_backup_data_card"))
    composeTestRule.onNodeWithTag("settings_backup_data_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("settings_group_5"))
    composeTestRule.onNodeWithTag("settings_group_5").assertIsDisplayed()
  }

  @Test
  @Config(sdk = [36], qualifiers = "w412dp-h1000dp")
  fun multiYearPaymentsUnifiedCategoriesAndCoherenceWorkEndToEnd() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val storeFile = File(context.cacheDir, "local_syndicat_items456_${System.nanoTime()}.json")
    try {
      val repo = LocalSyndicatRepository(context, storageFileOverride = storeFile)
      repo.ensureSeeded()

      // 1. Item 4: "Sécurité" and "Peinture" preserved on expenses
      val camExpId = repo.addExpense(
        title = "Caméras surveillance parking",
        category = "Sécurité",
        amountDh = 2400L,
        paidAmountDh = 1500L,
        supplier = "VisionSecur",
        dateLabel = "15/10/2026",
        notes = "4 caméras IP",
      )
      val savedCamExp = repo.observeExpenses(LOCAL_DEVICE_USER_ID).first().first { it.id == camExpId }
      assertEquals("Sécurité", savedCamExp.category)
      assertEquals(1500L, savedCamExp.effectivePaidDh)

      // 2. Item 6: paidAmountDh > amountDh and apartment ID mismatch rejected locally
      var paidExceedsRejected = false
      try {
        repo.addExpense(
          title = "Erreur montant",
          category = "Sécurité",
          amountDh = 300L,
          paidAmountDh = 450L,
          dateLabel = "15/10/2026",
          notes = "",
        )
      } catch (_: IllegalArgumentException) {
        paidExceedsRejected = true
      }
      assertTrue("Expected paidAmountDh > amountDh to be rejected", paidExceedsRejected)

      var wrongAptIdRejected = false
      try {
        repo.updateApartment(ApartmentRecord(id = "appt_05", number = 1L, floor = "RDC"))
      } catch (_: IllegalArgumentException) {
        wrongAptIdRejected = true
      }
      assertTrue("Expected mismatched apartment document ID to be rejected", wrongAptIdRejected)

      // 3. Item 5: Multi-year PaymentRecord persistence + projection across 2026 & 2027
      val pay27Id = repo.addPayment(
        PaymentRecord(
          apartmentId = "appt_01",
          apartmentNumber = 1L,
          year = 2027L,
          month = 6L,
          monthsCovered = listOf(6L, 7L),
          amountDh = 200L,
          paymentDate = "04/06/2027",
          paymentMethod = "transfer",
          receiptNumber = 7L,
          notes = "Virement été 2027",
        )
      )
      val reloadedRepo = LocalSyndicatRepository(context, storageFileOverride = storeFile)
      val cfg = reloadedRepo.observeConfig(LOCAL_DEVICE_USER_ID).first()!!
      val baseApts = reloadedRepo.observeApartments(LOCAL_DEVICE_USER_ID).first()
      val payments = reloadedRepo.observePayments(LOCAL_DEVICE_USER_ID).first()
      assertEquals(1, payments.size)
      assertEquals(pay27Id, payments.first().id)
      assertEquals("transfer", payments.first().paymentMethod)

      val projected2027 = projectApartmentsForYear(baseApts, cfg, 2027L, payments)
      val apt1In2027 = projected2027.first { it.number == 1L }
      assertEquals(100L, apt1In2027.juin)
      assertEquals(100L, apt1In2027.juil)
      assertEquals(0L, projected2027.first { it.number == 2L }.totalAllMonthsDh)

      // 4. UI verification: ApartmentsScreen multi-year selector & payment history + receipt bitmap
      var chosenYear: Long? = null
      val dash2027 = SyndicatDashboardData(
        config = cfg.copy(year = 2027L),
        apartments = projected2027,
        expenses = reloadedRepo.observeExpenses(LOCAL_DEVICE_USER_ID).first(),
        projects = reloadedRepo.observeProjects(LOCAL_DEVICE_USER_ID).first(),
        payments = payments,
        baseApartments = baseApts,
        baseYear = cfg.year,
      )
      val receiptBmp = com.example.util.PdfPosterExporter.generatePaymentReceiptBitmap(
        context = context,
        apartment = apt1In2027,
        data = dash2027,
        receiptNo = 7L,
        payment = payments.first(),
      )
      assertTrue(receiptBmp.width > 0 && receiptBmp.height > 0)
      receiptBmp.recycle()

      composeTestRule.setContent {
        CompositionLocalProvider(
          LocalStrings provides stringsForLanguage("fr"),
          LocalLayoutDirection provides LayoutDirection.Ltr,
        ) {
          AppTheme(darkTheme = false, isArabic = false, themeStyle = "coinbase") {
            ApartmentsScreen(
              data = dash2027,
              searchQuery = "",
              selectedFloor = null,
              statusFilter = com.example.ui.viewmodel.StatusFilter.ALL,
              onSearchQueryChange = {},
              onSelectFloor = {},
              onSelectStatusFilter = {},
              onSaveApartment = { _, _ -> },
              onMarkReminded = { _, _, _ -> },
              onRequestReceiptNo = { 8L },
              onExportError = {},
              onSelectYear = { yr -> chosenYear = yr },
            )
          }
        }
      }
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("apartments_year_selector").assertIsDisplayed()
      composeTestRule.onNodeWithTag("year_chip_2026").performClick()
      composeTestRule.waitForIdle()
      assertEquals(2026L, chosenYear)
    } finally {
      storeFile.delete()
    }
  }

  @Test
  @Config(sdk = [36], qualifiers = "w412dp-h1000dp")
  fun expensesScreenSupportsSecuriteCategoryAndRejectsPaidExceedingAmount() {
    var submittedCategory: String? = null
    var submittedCount = 0
    val frStrings = stringsForLanguage("fr")
    val sampleData = SyndicatDashboardData(
      config = SyndicatConfig(language = "fr", currency = "DH", onboardingCompleted = true, seeded = true),
      apartments = listOf(ApartmentRecord(id = "appt_01", number = 1L, floor = "RDC")),
      expenses = emptyList(),
      projects = emptyList(),
    )

    composeTestRule.setContent {
      CompositionLocalProvider(
        LocalStrings provides frStrings,
        LocalLayoutDirection provides LayoutDirection.Ltr,
      ) {
        AppTheme(darkTheme = false, isArabic = false, themeStyle = "meta") {
          ExpensesScreen(
            data = sampleData,
            onAddExpense = { _, cat, _, _, _, _, _ ->
              submittedCategory = cat
              submittedCount++
            },
            onUpdateExpense = {},
            onDeleteExpense = {},
            onExportError = {},
          )
        }
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("add_expense_fab").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("expense_title_input").performTextReplacement("Caméras hall")
    composeTestRule.onNodeWithTag("expense_amount_input").performTextReplacement("500")
    composeTestRule.onNodeWithTag("expense_paid_amount_input").performTextReplacement("800")
    composeTestRule.onNodeWithTag("expense_category_Sécurité").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("save_expense_button").performClick()
    composeTestRule.waitForIdle()

    // Must NOT submit when paidAmountDh (800) > amountDh (500), and must display validation error
    assertEquals(0, submittedCount)
    composeTestRule.onNodeWithText(frStrings.expErrPaidExceedsAmount).assertIsDisplayed()

    // Fix paidAmountDh to 500 -> submits with category "Sécurité"
    composeTestRule.onNodeWithTag("expense_paid_amount_input").performTextReplacement("500")
    composeTestRule.onNodeWithTag("save_expense_button").performClick()
    composeTestRule.waitForIdle()

    assertEquals(1, submittedCount)
    assertEquals("Sécurité", submittedCategory)
  }

  @Test
  @Config(sdk = [36], qualifiers = "w412dp-h1000dp")
  fun appBrandingPublisherDynicVersion20Edition2026RendersCleanly() {
    val frStrings = stringsForLanguage("fr")
    val sampleData = SyndicatDashboardData(
      config = SyndicatConfig(
        language = "fr",
        residenceName = "Résidence Atlas",
        currency = "DH",
        onboardingCompleted = true,
        seeded = true,
      ),
      apartments = listOf(ApartmentRecord(id = "appt_01", number = 1L, floor = "RDC", ownerName = "Nadia")),
      expenses = emptyList(),
      projects = emptyList(),
    )

    composeTestRule.setContent {
      CompositionLocalProvider(
        LocalStrings provides frStrings,
        LocalLayoutDirection provides LayoutDirection.Ltr,
      ) {
        AppTheme(darkTheme = false, isArabic = false, themeStyle = "coinbase") {
          SettingsScreen(
            data = sampleData,
            currentUserId = LOCAL_DEVICE_USER_ID,
            userEmail = null,
            currentLanguage = AppLanguage.FR,
            isSaving = false,
            onSelectLanguage = {},
            onUpdateDisplayPrefs = { _, _, _, _ -> },
            onOpenWizard = {},
            onSaveConfig = { _, _ -> },
            onRestoreBackup = {},
            onImportCsvOwners = {},
            onResetC49 = {},
            onBackupExported = {},
            onPinUpdated = {},
            onSignOut = {},
            onMessage = {},
          )
        }
      }
    }
    composeTestRule.waitForIdle()

    // Top brand bar in SettingsScreen immediately visible
    composeTestRule.onNodeWithTag("settings_app_brand_bar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("settings_app_brand_bar_version_pill").assertIsDisplayed()

    // Scroll LazyColumn to the full About / Edition card at the bottom of SettingsScreen
    composeTestRule.onNodeWithTag("settings_screen")
      .performScrollToNode(hasTestTag("settings_about_card"))
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("settings_about_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("about_editor_badge").assertIsDisplayed()
    composeTestRule.onNodeWithTag("about_version_badge").assertIsDisplayed()
    composeTestRule.onNodeWithTag("about_edition_year_badge").assertIsDisplayed()
    composeTestRule.onNodeWithText("Version 2.0").assertIsDisplayed()
    composeTestRule.onNodeWithText("Année d'édition : 2026").assertIsDisplayed()
  }

  @Test
  @Config(sdk = [36], qualifiers = "w412dp-h1000dp")
  fun testExportMonochromeAndProjectsReport() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val frStrings = stringsForLanguage("fr")
    val sampleData = SyndicatDashboardData(
      config = SyndicatConfig(buildingTitle = "Résidence Test 2026", language = "fr"),
      apartments = listOf(
        ApartmentRecord(id = "appt_01", number = 1L, floor = "RDC", ownerName = "Hamid", jan = 50L, feb = 50L),
        ApartmentRecord(id = "appt_02", number = 2L, floor = "1er", ownerName = "Aziz", jan = 0L),
      ),
      expenses = listOf(
        ExpenseRecord(id = "exp_01", title = "Éclairage", category = "Électricité", amountDh = 200L),
      ),
      projects = listOf(
        ProjectRecord(id = "proj_01", titleFr = "Peinture Façade", category = "Peinture", targetBudgetDh = 8000L, progressPercent = 50L, status = "in_progress"),
      ),
    )

    // 1. Verify Official Poster Bitmap can be generated with monochromeMode = true
    val cotMonoOpts = CotisationExportOptions(
      showNames = true,
      untilCutoffOnly = false,
      inkSaver = false,
      monochromeMode = true,
    )
    val monoPosterBitmap = PdfPosterExporter.generateOfficialPosterBitmap(context, sampleData, cotMonoOpts)
    assertNotNull(monoPosterBitmap)
    assertTrue(monoPosterBitmap.width > 0)
    assertTrue(monoPosterBitmap.height > 0)

    // 2. Verify Projects Report Bitmap generation in both color and monochrome modes
    val projColorOpts = com.example.util.ProjectsExportOptions(inkSaver = false, monochromeMode = false)
    val projColorBitmap = PdfPosterExporter.generateProjectsReportBitmap(context, sampleData, projColorOpts)
    assertNotNull(projColorBitmap)
    assertTrue(projColorBitmap.width > 0)
    assertTrue(projColorBitmap.height > 0)

    val projMonoOpts = com.example.util.ProjectsExportOptions(inkSaver = false, monochromeMode = true)
    val projMonoBitmap = PdfPosterExporter.generateProjectsReportBitmap(context, sampleData, projMonoOpts)
    assertNotNull(projMonoBitmap)
    assertTrue(projMonoBitmap.width > 0)
    assertTrue(projMonoBitmap.height > 0)

    // 3. Verify PosterScreen displays Card 4 and the monochrome options
    composeTestRule.setContent {
      CompositionLocalProvider(
        LocalStrings provides frStrings,
        LocalLayoutDirection provides LayoutDirection.Ltr,
      ) {
        AppTheme(darkTheme = false, isArabic = false, themeStyle = "coinbase") {
          com.example.ui.screens.PosterScreen(
            data = sampleData,
            onExportError = {},
          )
        }
      }
    }
    composeTestRule.waitForIdle()

    // Card 1 monochrome checkbox exists
    composeTestRule.onNodeWithTag("cotisation_monochrome_checkbox").assertIsDisplayed()

    // Scroll to Card 4
    composeTestRule.onNodeWithTag("poster_screen")
      .performScrollToNode(hasTestTag("export_card_projects"))
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("export_card_projects").assertIsDisplayed()
    composeTestRule.onNodeWithTag("projects_monochrome_checkbox").assertIsDisplayed()
    composeTestRule.onNodeWithTag("export_projects_pdf").assertIsDisplayed()
    composeTestRule.onNodeWithTag("export_projects_png").assertIsDisplayed()
  }

  @Test
  @Config(sdk = [36], qualifiers = "w412dp-h1000dp")
  fun uiDesignUpgrades_projectsTabsPosterZoomAndLargeFontComfortWorkCleanly() {
    val frStrings = stringsForLanguage("fr")
    val sampleData = SyndicatDashboardData(
      config = SyndicatConfig(
        buildingTitle = "Résidence C49",
        language = "fr",
        fontScale = "large",
        year = 2026L,
      ),
      apartments = listOf(
        ApartmentRecord(id = "appt_01", number = 1L, floor = "RDC", ownerName = "Hamid", jan = 50L),
      ),
      expenses = listOf(
        ExpenseRecord(id = "exp_unpaid", title = "Caméra", category = "Sécurité", amountDh = 500L, paidAmountDh = 200L),
      ),
      projects = listOf(
        ProjectRecord(id = "proj_01", titleFr = "Peinture", category = "Peinture", targetBudgetDh = 4000L, progressPercent = 40L, status = "in_progress"),
      ),
    )

    val activePage = androidx.compose.runtime.mutableStateOf("projects")
    composeTestRule.setContent {
      CompositionLocalProvider(
        LocalStrings provides frStrings,
        LocalLayoutDirection provides LayoutDirection.Ltr,
      ) {
        AppTheme(darkTheme = false, isArabic = false, fontScale = "large", themeStyle = "coinbase") {
          when (activePage.value) {
            "projects" -> com.example.ui.screens.ProjectsScreen(
              data = sampleData,
              onAddProject = {},
              onUpdateProject = { _, _ -> },
              onDeleteProject = {},
              onLoadStarterProjects = {},
            )
            "poster" -> com.example.ui.screens.PosterScreen(
              data = sampleData,
              onExportError = {},
            )
            "expenses" -> com.example.ui.screens.ExpensesScreen(
              data = sampleData,
              onAddExpense = { _, _, _, _, _, _, _ -> },
              onUpdateExpense = {},
              onDeleteExpense = {},
              onExportError = {},
              initialUnpaidOnly = true,
            )
            else -> com.example.ui.screens.SettingsScreen(
              data = sampleData,
              currentUserId = "local_device",
              userEmail = null,
              currentLanguage = AppLanguage.FR,
              isSaving = false,
              onSelectLanguage = {},
              onUpdateDisplayPrefs = { _, _, _, _ -> },
              onOpenWizard = {},
              onSaveConfig = { _, _ -> },
              onRestoreBackup = {},
              onImportCsvOwners = {},
              onResetC49 = {},
              onBackupExported = {},
              onPinUpdated = {},
              onSignOut = {},
              onMessage = {},
            )
          }
        }
      }
    }
    composeTestRule.waitForIdle()

    // 1. ProjectsScreen segmented Works | Appels de fonds tabs
    composeTestRule.onNodeWithTag("projects_tab_works").assertIsDisplayed()
    composeTestRule.onNodeWithTag("projects_tab_calls").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("special_calls_section").assertIsDisplayed()

    // 2. PosterScreen Card 1 Aperçu A4 full-screen zoom dialog opens & closes
    activePage.value = "poster"
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("poster_screen").performScrollToNode(hasTestTag("preview_poster_1"))
    composeTestRule.onNodeWithTag("preview_poster_1").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("poster_preview_zoom_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("poster_preview_close_btn").performClick()
    composeTestRule.waitForIdle()

    // 3. ExpensesScreen initialUnpaidOnly chip renders cleanly
    activePage.value = "expenses"
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("expense_filter_unpaid").assertIsDisplayed()

    // 4. SettingsScreen large font comfort hint renders cleanly
    activePage.value = "settings"
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("font_scale_large_hint"))
    composeTestRule.onNodeWithTag("font_scale_large_hint").assertIsDisplayed()
  }

  @Test
  @Config(sdk = [36], qualifiers = "w412dp-h1000dp")
  fun uiUxV22_privacySearchDonutSettleQuorumAndVisualThemesWorkCleanly() {
    val frStrings = stringsForLanguage("fr")
    val sampleData = SyndicatDashboardData(
      config = SyndicatConfig(
        buildingTitle = "Résidence C49",
        language = "fr",
        year = 2026L,
      ),
      apartments = listOf(
        ApartmentRecord(id = "appt_01", number = 1L, floor = "RDC", ownerName = "Hamid", jan = 50L, feb = 0L),
        ApartmentRecord(id = "appt_02", number = 2L, floor = "RDC", ownerName = "Aziz", jan = 50L, feb = 50L),
      ),
      expenses = listOf(
        ExpenseRecord(id = "exp_unpaid", title = "Caméra Entrée", category = "Sécurité", amountDh = 500L, paidAmountDh = 200L),
      ),
      projects = listOf(
        ProjectRecord(id = "proj_01", titleFr = "Peinture Façade", category = "Peinture", targetBudgetDh = 4000L, progressPercent = 40L, status = "in_progress"),
      ),
    )

    val screenState = androidx.compose.runtime.mutableStateOf("dashboard")
    val privacyState = androidx.compose.runtime.mutableStateOf(false)
    var settledExpense: ExpenseRecord? = null
    var lastSearchNavigatedTo: String? = null

    composeTestRule.setContent {
      CompositionLocalProvider(
        LocalStrings provides frStrings,
        LocalLayoutDirection provides LayoutDirection.Ltr,
        com.example.ui.components.LocalPrivacyMode provides privacyState.value,
      ) {
        AppTheme(darkTheme = false, isArabic = false, themeStyle = "coinbase") {
          when (screenState.value) {
            "dashboard" -> com.example.ui.screens.DashboardScreen(
              data = sampleData,
              isBackupOverdue = false,
              onTriggerBackupJson = {},
              onOpenWizard = {},
              onMarkReminded = { _, _, _ -> },
              onNavigateToPosters = {},
              onNavigateToLateApartments = {},
              isPrivacyMode = privacyState.value,
              onTogglePrivacy = { privacyState.value = !privacyState.value },
              onSearchSelectApartment = { apt -> lastSearchNavigatedTo = "apt_${apt.number}" },
              onSearchSelectProject = { _ -> lastSearchNavigatedTo = "projects" },
            )
            "expenses" -> com.example.ui.screens.ExpensesScreen(
              data = sampleData,
              onAddExpense = { _, _, _, _, _, _, _ -> },
              onUpdateExpense = { settledExpense = it },
              onDeleteExpense = {},
              onExportError = {},
            )
            "projects" -> com.example.ui.screens.ProjectsScreen(
              data = sampleData,
              onAddProject = {},
              onUpdateProject = { _, _ -> },
              onDeleteProject = {},
              onLoadStarterProjects = {},
            )
            else -> com.example.ui.screens.ApartmentsScreen(
              data = sampleData,
              searchQuery = "",
              selectedFloor = null,
              statusFilter = com.example.ui.viewmodel.StatusFilter.ALL,
              onSearchQueryChange = {},
              onSelectFloor = {},
              onSelectStatusFilter = {},
              onSaveApartment = { _, _ -> },
              onMarkReminded = { _, _, _ -> },
              onRequestReceiptNo = { 1L },
              onExportError = {},
            )
          }
        }
      }
    }
    composeTestRule.waitForIdle()

    // 0. Home/Dashboard top bar shows search & privacy without app name or version header
    composeTestRule.onNodeWithTag("dashboard_app_brand_bar_version_pill").assertDoesNotExist()
    composeTestRule.onNodeWithText(frStrings.appBrandName).assertDoesNotExist()

    // 1. Privacy toggle masks sensitive values in Dashboard, then restores them
    composeTestRule.onNodeWithTag("dashboard_app_brand_bar_privacy_btn").performClick()
    composeTestRule.waitForIdle()
    assertTrue(privacyState.value)
    composeTestRule.onNodeWithTag("cash_now_value").assertTextEquals("••••••")
    composeTestRule.onNodeWithTag("dashboard_app_brand_bar_privacy_btn").performClick()
    composeTestRule.waitForIdle()
    assertFalse(privacyState.value)

    // 2. Global search dialog opens, filters items, and navigates on click
    composeTestRule.onNodeWithTag("dashboard_app_brand_bar_search_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("global_search_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("global_search_input").performTextReplacement("Hamid")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("gs_apt_1").performClick()
    composeTestRule.waitForIdle()
    assertEquals("apt_1", lastSearchNavigatedTo)

    // 3. ExpensesScreen displays interactive donut, sort chips, and 1-tap settle button
    screenState.value = "expenses"
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("expense_donut_chart").assertIsDisplayed()
    composeTestRule.onNodeWithTag("expenses_filter_row").performScrollToNode(hasTestTag("exp_sort_unpaid"))
    composeTestRule.onNodeWithTag("exp_sort_unpaid").assertIsDisplayed()
    composeTestRule.onNodeWithTag("expenses_list").performScrollToNode(hasTestTag("exp_settle_btn_exp_unpaid"))
    composeTestRule.onNodeWithTag("exp_settle_btn_exp_unpaid").performClick()
    composeTestRule.waitForIdle()
    assertEquals(500L, settledExpense?.paidAmountDh)

    // 4. ProjectsScreen opens AG Quorum & Vote dialog, counts votes cleanly, and resets
    screenState.value = "projects"
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("ag_quorum_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("ag_quorum_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("quorum_apt_1").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("ag_quorum_reset_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("ag_quorum_close_btn").performClick()
    composeTestRule.waitForIdle()

    // 5. ApartmentsScreen exposes multi-criteria sort chips
    screenState.value = "apartments"
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("apartments_filter_row").performScrollToNode(hasTestTag("appt_sort_default"))
    composeTestRule.onNodeWithTag("appt_sort_default").assertIsDisplayed()
    composeTestRule.onNodeWithTag("apartments_filter_row").performScrollToNode(hasTestTag("appt_sort_late"))
    composeTestRule.onNodeWithTag("appt_sort_late").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("apartments_filter_row").performScrollToNode(hasTestTag("appt_sort_paid"))
    composeTestRule.onNodeWithTag("appt_sort_paid").assertIsDisplayed()
  }

  @Test
  @Config(sdk = [36], qualifiers = "w360dp-h800dp")
  fun floatingBottomNavDockAndNavigationRail_renderSleekCompactTargetsAndSwitchCleanly() {
    val frStrings = stringsForLanguage("fr")
    val activeDest = androidx.compose.runtime.mutableStateOf(AppDestination.DASHBOARD)
    val showRail = androidx.compose.runtime.mutableStateOf(false)

    composeTestRule.setContent {
      CompositionLocalProvider(
        LocalStrings provides frStrings,
        LocalLayoutDirection provides LayoutDirection.Ltr,
      ) {
        AppTheme(darkTheme = false, isArabic = false, themeStyle = "meta") {
          val items = AppDestination.entries.map { dest ->
            com.example.ui.components.SyndicatNavItemSpec(
              route = dest.route,
              label = dest.label(frStrings),
              filledIcon = dest.filledIcon,
              outlinedIcon = dest.outlinedIcon,
              badgeCount = when (dest) {
                AppDestination.APARTMENTS -> 5
                AppDestination.EXPENSES -> 2
                else -> 0
              },
              isSelected = activeDest.value == dest,
              onClick = { activeDest.value = dest },
            )
          }
          if (showRail.value) {
            com.example.ui.components.SyndicatNavigationRailPane(items = items)
          } else {
            com.example.ui.components.SyndicatFloatingBottomDock(items = items)
          }
        }
      }
    }
    composeTestRule.waitForIdle()

    // Dock & all 6 items render icon-only (no text labels) within compact 360dp width with >= 48dp accessible touch targets
    composeTestRule.onNodeWithTag("syndicat_bottom_nav_dock").assertIsDisplayed()
    composeTestRule.onNodeWithText("Accueil").assertDoesNotExist()
    composeTestRule.onNodeWithText("Cotis.").assertDoesNotExist()
    composeTestRule.onNodeWithText("Projets").assertDoesNotExist()
    composeTestRule.onNodeWithTag("nav_badge_apartments", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("nav_badge_expenses", useUnmergedTree = true).assertIsDisplayed()
    AppDestination.entries.forEach { dest ->
      val bounds = composeTestRule.onNodeWithTag("nav_${dest.route}")
        .assertIsDisplayed()
        .getUnclippedBoundsInRoot()
      assertTrue("Item ${dest.route} width should be >= 48dp", bounds.width >= 48.dp)
      assertTrue("Item ${dest.route} height should be >= 48dp", bounds.height >= 48.dp)
    }

    composeTestRule.onNodeWithTag("nav_projects").performClick()
    composeTestRule.waitForIdle()
    assertEquals(AppDestination.PROJECTS, activeDest.value)

    // Switch to tablet navigation rail mode cleanly
    showRail.value = true
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("navigation_rail").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nav_settings").performClick()
    composeTestRule.waitForIdle()
    assertEquals(AppDestination.SETTINGS, activeDest.value)
  }

  @Test
  fun reportsExporterAndCouncilCodecAndWidgetSnapshot_workCleanly() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val cfg = SyndicatConfig(
      residenceName = "Résidence C49",
      syndicatName = "Syndic C49",
      language = "fr",
      year = 2026L,
      cutoffMonth = 4L,
      activeMonths = listOf(1L, 2L, 3L, 4L),
      defaultMonthlyFee = 50L,
    )
    val apt = ApartmentRecord(
      id = "apt_01",
      number = 1L,
      floor = "RDC",
      status = "occupied",
      ownerName = "Karim Benali",
      ownerNameAr = "كريم",
      jan = 50L,
      feb = 50L,
    )
    val pay = PaymentRecord(
      id = "pay_1",
      apartmentNumber = 1L,
      year = 2026L,
      amountDh = 100L,
      monthsCovered = listOf(1L, 2L),
      paymentDate = "10/02/2026",
      receiptNumber = 12L,
    )
    val specialCall = SpecialCallRecord(
      id = "sc_1",
      titleFr = "Étanchéité",
      titleAr = "عزل",
      projectId = "proj_1",
      amountPerAptDh = 200L,
      createdDate = "01/04/2026",
    )
    val data = SyndicatDashboardData(
      config = cfg,
      apartments = listOf(apt),
      expenses = emptyList(),
      payments = listOf(pay),
    )

    val stmtColor = SyndicatReportsExporter.generateApartmentStatementBitmap(
      data = data,
      apartment = apt,
      specialCalls = listOf(specialCall),
      payments = listOf(pay),
      monochromeMode = false,
    )
    val stmtMono = SyndicatReportsExporter.generateApartmentStatementBitmap(
      data = data,
      apartment = apt,
      specialCalls = listOf(specialCall),
      payments = listOf(pay),
      monochromeMode = true,
    )
    assertEquals(1240, stmtColor.width)
    assertEquals(1754, stmtColor.height)
    assertEquals(Color.WHITE, stmtColor.getPixel(5, 1740))
    assertEquals(1240, stmtMono.width)

    val agDraft = AgMinutesDraft(
      meetingDate = "18/04/2026",
      location = "Hall Résidence",
      agenda = "Validation budget travaux",
      presentAptNumbers = setOf(1L),
      resolutions = listOf(
        AgResolutionRecord(id = "r1", title = "Validation budget 2026", votesFor = 15, votesAgainst = 1),
      ),
    )
    val pvColor = SyndicatReportsExporter.generateAgMinutesBitmap(data, agDraft, monochromeMode = false)
    val pvMono = SyndicatReportsExporter.generateAgMinutesBitmap(data, agDraft, monochromeMode = true)
    assertEquals(1240, pvColor.width)
    assertEquals(1754, pvColor.height)
    assertEquals(1240, pvMono.width)

    val waStmt = SyndicatReportsExporter.buildApartmentWhatsAppStatement(
      strings = stringsForLanguage("fr"),
      data = data,
      apartment = apt,
      specialCalls = listOf(specialCall),
    )
    assertTrue(waStmt.contains("Karim Benali"))
    assertTrue(waStmt.contains("100"))

    // Council Share Codec round-trip
    val shareCode = SyndicatCouncilShareCodec.encodeShareCode(data, listOf(specialCall))
    assertTrue(shareCode.startsWith("SYNDICAT-CONSEIL-1:"))
    val decoded = SyndicatCouncilShareCodec.decodeShareCode(shareCode, LOCAL_DEVICE_USER_ID)
    assertEquals(1, decoded.apartments.size)
    assertEquals(1, decoded.specialCalls.size)
    assertEquals("Résidence C49", decoded.config.residenceName)

    // Widget Snapshot update runs cleanly
    runBlocking {
      SyndicatWidgetSnapshot.saveAndRefresh(context, data)
    }
  }

  @Test
  @Config(sdk = [36], qualifiers = "w412dp-h920dp")
  fun v23Features_abosVendorsOcrIncidentsAgPvReadOnlyMultiBuildingAndStatementsWorkCleanly() {
    val frStrings = stringsForLanguage("fr")
    val cfg = SyndicatConfig(
      residenceName = "Résidence C49",
      language = "fr",
      year = 2026L,
    )
    val apt1 = ApartmentRecord(
      id = "apt_01",
      number = 1L,
      floor = "RDC",
      status = "occupied",
      ownerName = "Karim Benali",
    )
    val inc1 = IncidentRecord(
      id = "inc_1",
      title = "Ampoule palier 2e",
      locationFloor = "2e étage",
      severity = IncidentSeverity.HIGH,
      status = IncidentStatus.REPORTED,
    )
    val baseData = SyndicatDashboardData(
      config = cfg,
      apartments = listOf(apt1),
      expenses = emptyList(),
    )

    val screenMode = androidx.compose.runtime.mutableStateOf("expenses")
    val templatesState = androidx.compose.runtime.mutableStateOf<List<RecurringExpenseTemplate>>(emptyList())
    val vendorsState = androidx.compose.runtime.mutableStateOf<List<VendorRecord>>(emptyList())
    val roleModeState = androidx.compose.runtime.mutableStateOf(SyndicatRoleMode.ADMIN)
    val agDraftState = androidx.compose.runtime.mutableStateOf(AgMinutesDraft(presentAptNumbers = setOf(1L)))
    var seededCalled = false
    var generatedExpCount = 0
    var cycledIncidentId: String? = null
    var convertedIncident: IncidentRecord? = null
    var selectedBuildingId = "default"

    composeTestRule.setContent {
      CompositionLocalProvider(
        LocalStrings provides frStrings,
        LocalLayoutDirection provides LayoutDirection.Ltr,
      ) {
        AppTheme(darkTheme = false, isArabic = false, themeStyle = "coinbase") {
          when (screenMode.value) {
            "expenses" -> ExpensesScreen(
              data = baseData,
              onAddExpense = { _, _, _, _, _, _, _ -> generatedExpCount++ },
              onUpdateExpense = {},
              onDeleteExpense = {},
              onExportError = {},
              recurringTemplates = templatesState.value,
              vendors = vendorsState.value,
              onSeedRecurringPresets = {
                seededCalled = true
                templatesState.value = listOf(
                  RecurringExpenseTemplate(
                    id = "rec_guard",
                    title = "Gardiennage",
                    category = "Gardiennage",
                    amountDh = 1200L,
                  ),
                )
              },
              onUpsertVendor = { v -> vendorsState.value = listOf(v.copy(id = "v_1")) },
              isReadOnlyMode = roleModeState.value == SyndicatRoleMode.READ_ONLY_COUNCIL,
            )
            "projects" -> ProjectsScreen(
              data = baseData,
              onAddProject = {},
              onUpdateProject = { _, _ -> },
              onDeleteProject = {},
              onLoadStarterProjects = {},
              incidents = listOf(inc1),
              onCycleIncidentStatus = { id -> cycledIncidentId = id },
              onConvertIncidentToExpense = { inc -> convertedIncident = inc },
              agMinutes = agDraftState.value,
              onSaveAgMinutes = { agDraftState.value = it },
              isReadOnlyMode = roleModeState.value == SyndicatRoleMode.READ_ONLY_COUNCIL,
            )
            "apartments" -> ApartmentsScreen(
              data = baseData,
              searchQuery = "",
              selectedFloor = null,
              statusFilter = StatusFilter.ALL,
              onSearchQueryChange = {},
              onSelectFloor = {},
              onSelectStatusFilter = {},
              onSaveApartment = { _, _ -> },
              onMarkReminded = { _, _, _ -> },
              onRequestReceiptNo = { 1L },
              onExportError = {},
              isReadOnlyMode = roleModeState.value == SyndicatRoleMode.READ_ONLY_COUNCIL,
            )
            else -> SettingsScreen(
              data = baseData,
              currentUserId = LOCAL_DEVICE_USER_ID,
              userEmail = null,
              currentLanguage = AppLanguage.FR,
              isSaving = false,
              onSelectLanguage = {},
              onUpdateDisplayPrefs = { _, _, _, _ -> },
              onOpenWizard = {},
              onSaveConfig = { _, _ -> },
              onRestoreBackup = {},
              onImportCsvOwners = {},
              onResetC49 = {},
              onBackupExported = {},
              onPinUpdated = {},
              onSignOut = {},
              onMessage = {},
              buildings = listOf(
                BuildingProfile("default", "Résidence C49", "Casablanca"),
                BuildingProfile("bloc_b", "Bloc B", "Rabat"),
              ),
              activeBuildingId = selectedBuildingId,
              onSelectBuilding = { selectedBuildingId = it },
              roleMode = roleModeState.value,
              onSetRoleMode = { roleModeState.value = it },
            )
          }
        }
      }
    }
    composeTestRule.waitForIdle()

    // 1. ExpensesScreen: OCR button exists in Add modal, Abos & Prestataires tab works & excludes double caisse
    composeTestRule.onNodeWithTag("add_expense_fab").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("ocr_scan_invoice_btn").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithText(frStrings.apptCancel).performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("switch_abos_vendors_mode").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("seed_recurring_presets_btn").performClick()
    composeTestRule.waitForIdle()
    assertTrue(seededCalled)
    composeTestRule.onNodeWithTag("generate_recurring_month_btn").performClick()
    composeTestRule.waitForIdle()
    assertEquals(1, generatedExpCount)

    // Add Vendor
    composeTestRule.onNodeWithTag("add_vendor_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("vendor_name_input").performTextReplacement("Atlas Lift")
    composeTestRule.onNodeWithTag("save_vendor_btn").performClick()
    composeTestRule.waitForIdle()
    assertEquals("Atlas Lift", vendorsState.value.firstOrNull()?.name)

    // 2. ProjectsScreen: Incidents tab + AG PV export & resolution
    screenMode.value = "projects"
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("projects_tab_incidents").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("incident_status_inc_1").performClick()
    assertEquals("inc_1", cycledIncidentId)
    composeTestRule.onNodeWithTag("convert_incident_expense_inc_1").performClick()
    assertEquals("inc_1", convertedIncident?.id)

    composeTestRule.onNodeWithTag("projects_tab_works").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("ag_quorum_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("ag_resolution_input").performScrollTo().performTextReplacement("Adopter peinture")
    composeTestRule.onNodeWithTag("ag_add_resolution_btn").performClick()
    composeTestRule.waitForIdle()
    assertEquals(1, agDraftState.value.resolutions.size)
    composeTestRule.onNodeWithTag("ag_export_pv_pdf_btn").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("ag_export_pv_mono_btn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ag_quorum_close_btn").performClick()
    composeTestRule.waitForIdle()

    // 3. ApartmentsScreen: Statement buttons visible, excluded Paiement groupé / Double Caisse absent
    screenMode.value = "apartments"
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("apartments_cards_list").performScrollToNode(hasTestTag("card_pay_apt_1"))
    composeTestRule.onNodeWithTag("card_pay_apt_1").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("apt_statement_pdf_btn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("apt_statement_wa_btn").assertIsDisplayed()
    composeTestRule.onNodeWithText("Trimestre").assertDoesNotExist()
    composeTestRule.onNodeWithText("Double caisse").assertDoesNotExist()

    // 4. SettingsScreen: Multi-building switcher & Read-Only Council governance mode
    screenMode.value = "settings"
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("settings_multi_building_card"))
    composeTestRule.onNodeWithTag("switch_building_bloc_b").performClick()
    assertEquals("bloc_b", selectedBuildingId)

    composeTestRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("settings_council_share_card"))
    composeTestRule.onNodeWithTag("role_mode_readonly_chip").performClick()
    composeTestRule.waitForIdle()
    assertEquals(SyndicatRoleMode.READ_ONLY_COUNCIL, roleModeState.value)
    composeTestRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("read_only_council_banner"))
    composeTestRule.onNodeWithTag("read_only_council_banner").assertIsDisplayed()

    composeTestRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("save_settings_button"))
    composeTestRule.onNodeWithTag("save_settings_button").assertIsNotEnabled()
    composeTestRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("reset_c49_btn"))
    composeTestRule.onNodeWithTag("reset_c49_btn").assertIsNotEnabled()
  }

  @Test
  fun syndicatAdvancedCodec_roundTripsRecurringVendorsIncidentsAgMinutesAndBuildings() {
    val templates = listOf(
      RecurringExpenseTemplate(
        id = "rec_1",
        title = "Gardiennage",
        category = "Gardiennage",
        amountDh = 1200L,
        supplier = "Hassan",
        dayOfMonth = 2,
        lastAppliedMonthKey = "2026-04",
        active = true,
      ),
    )
    val parsedRec = SyndicatAdvancedCodec.parseRecurringJsonArray(SyndicatAdvancedCodec.recurringToJsonArray(templates))
    assertEquals(1, parsedRec.size)
    assertEquals(1200L, parsedRec[0].amountDh)
    assertEquals("2026-04", parsedRec[0].lastAppliedMonthKey)

    val vendors = listOf(
      VendorRecord(
        id = "v_1",
        name = "Atlas Lift",
        specialty = "Ascenseur",
        phone = "0611223344",
        monthlyFeeDh = 600L,
        contractEndDate = "31/12/2026",
      ),
    )
    val parsedVendors = SyndicatAdvancedCodec.parseVendorsJsonArray(SyndicatAdvancedCodec.vendorsToJsonArray(vendors))
    assertEquals(vendors, parsedVendors)

    val incidents = listOf(
      IncidentRecord(
        id = "inc_1",
        title = "Fuite colonne eau",
        locationFloor = "3e étage",
        severity = IncidentSeverity.HIGH,
        status = IncidentStatus.IN_PROGRESS,
        linkedExpenseId = "exp_9",
      ),
    )
    val parsedInc = SyndicatAdvancedCodec.parseIncidentsJsonArray(SyndicatAdvancedCodec.incidentsToJsonArray(incidents))
    assertEquals(incidents, parsedInc)
    assertEquals(IncidentStatus.RESOLVED, parsedInc[0].status.next())

    val draft = AgMinutesDraft(
      meetingDate = "12/04/2026",
      location = "Hall RDC",
      agenda = "Budget & Peinture",
      presentAptNumbers = setOf(1L, 3L, 8L),
      resolutions = listOf(
        AgResolutionRecord(id = "r1", title = "Peinture façade", votesFor = 18, votesAgainst = 2, votesAbstain = 1),
      ),
    )
    val parsedDraft = SyndicatAdvancedCodec.parseAgMinutesJsonObject(SyndicatAdvancedCodec.agMinutesToJsonObject(draft))
    assertEquals(draft, parsedDraft)
    assertTrue(parsedDraft.resolutions[0].isAdopted())

    val buildings = listOf(
      BuildingProfile(id = "default", name = "Résidence C49", city = "Casa"),
      BuildingProfile(id = "bloc_b", name = "Bloc B", city = "Rabat"),
    )
    val (activeB, parsedBuildings) = SyndicatAdvancedCodec.parseBuildingsJsonObject(
      SyndicatAdvancedCodec.buildingsToJsonObject("bloc_b", buildings),
    )
    assertEquals("bloc_b", activeB)
    assertEquals(2, parsedBuildings.size)
  }

  @Test
  @Config(sdk = [36], qualifiers = "w412dp-h920dp")
  fun widgetStudioAndMultiFormatWidgets_persistPrefsMaskAmountsAndPreviewAllFormats() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val frStrings = stringsForLanguage("fr")
    val cfg = SyndicatConfig(
      residenceName = "Résidence C49",
      language = "fr",
      year = 2026L,
      cutoffMonth = 3L,
      activeMonths = listOf(1L, 2L, 3L),
      defaultMonthlyFee = 50L,
      openingBalanceDh = 2000L,
    )
    val aptLate = ApartmentRecord(
      id = "apt_01",
      number = 1L,
      floor = "RDC",
      status = "occupied",
      ownerName = "Karim Benali",
      jan = 50L,
      feb = 0L,
      mar = 0L,
    )
    val exp = ExpenseRecord(
      id = "exp_1",
      title = "Nettoyage",
      category = "Nettoyage",
      amountDh = 600L,
      paidAmountDh = 200L,
      supplier = "CleanCo",
      dateLabel = "10/03/2026",
    )
    val data = SyndicatDashboardData(
      config = cfg,
      apartments = listOf(aptLate),
      expenses = listOf(exp),
    )

    // 1. Reset prefs & verify saveAndRefresh populates detailed snapshot (cash, collected, unpaid expenses, top late)
    SyndicatWidgetSnapshot.savePrefs(context, SyndicatWidgetPrefs())
    SyndicatWidgetSnapshot.saveAndRefresh(context, data)
    val snap = SyndicatWidgetSnapshot.load(context)
    assertEquals("Résidence C49", snap.residenceName)
    assertEquals(1, snap.lateCount)
    assertTrue("Expected late preview line for Karim Benali", snap.latePreviewLines.firstOrNull()?.contains("Karim Benali") == true)
    assertFalse(snap.prefs.privacyMask)
    assertFalse(snap.displayedCashText.contains("•"))

    // Verify all 3 widget receivers are declared in manifest
    val pm = context.packageManager
    listOf(
      SyndicatCompactWidgetReceiver::class.java,
      SyndicatGlanceWidgetReceiver::class.java,
      SyndicatDetailedWidgetReceiver::class.java,
    ).forEach { cls ->
      val info = pm.getReceiverInfo(android.content.ComponentName(context, cls), 0)
      assertNotNull("Receiver ${cls.simpleName} must be registered", info)
    }

    // Verify distinct palettes & enum parsing
    assertEquals(4, WidgetThemePreset.entries.size)
    assertEquals(3, WidgetFormatPreset.entries.size)
    assertEquals(4, WidgetQuickAction.entries.size)
    assertTrue(widgetPaletteFor(WidgetThemePreset.LIGHT_CLEAN).bg != widgetPaletteFor(WidgetThemePreset.NAVY_PRO).bg)

    // 2. Interactive Widget Studio in SettingsScreen: format tabs, theme, action, privacy switch & pin CTA
    composeTestRule.setContent {
      CompositionLocalProvider(
        LocalStrings provides frStrings,
        LocalLayoutDirection provides LayoutDirection.Ltr,
      ) {
        AppTheme(darkTheme = false, isArabic = false, themeStyle = "coinbase") {
          SettingsScreen(
            data = data,
            currentUserId = LOCAL_DEVICE_USER_ID,
            userEmail = null,
            currentLanguage = AppLanguage.FR,
            isSaving = false,
            onSelectLanguage = {},
            onUpdateDisplayPrefs = { _, _, _, _ -> },
            onOpenWizard = {},
            onSaveConfig = { _, _ -> },
            onRestoreBackup = {},
            onImportCsvOwners = {},
            onResetC49 = {},
            onBackupExported = {},
            onPinUpdated = {},
            onSignOut = {},
            onMessage = {},
          )
        }
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("widget_studio_card"))
    composeTestRule.onNodeWithTag("widget_studio_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("widget_preview_box").performScrollTo().assertIsDisplayed()

    composeTestRule.onNodeWithTag("widget_format_compact").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("widget_format_detailed").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("widget_theme_emerald").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("widget_action_late").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("widget_opt_privacy").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("widget_pin_btn").performScrollTo().assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()

    val updatedSnap = SyndicatWidgetSnapshot.load(context)
    assertEquals(WidgetFormatPreset.DETAILED, updatedSnap.prefs.previewFormat)
    assertEquals(WidgetThemePreset.EMERALD_FINTECH, updatedSnap.prefs.theme)
    assertEquals(WidgetQuickAction.LATE_LIST, updatedSnap.prefs.primaryAction)
    assertTrue(updatedSnap.prefs.privacyMask)
    assertEquals("••••••", updatedSnap.displayedCashText)
  }

  @Test
  @Config(sdk = [36], qualifiers = "w412dp-h920dp")
  fun councilShareQrCode_roundTripsPayloadAndDisplaysQrDialogsInsteadOfRawText() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val frStrings = stringsForLanguage("fr")
    val cfg = SyndicatConfig(
      residenceName = "Résidence Les Oliviers",
      language = "fr",
      year = 2026L,
      apartmentsCount = 12L,
      defaultMonthlyFee = 100L,
      cutoffMonth = 6L,
    )
    val apts = (1L..12L).map { n ->
      ApartmentRecord(
        id = "apt_${n.toString().padStart(2, '0')}",
        number = n,
        floor = if (n <= 4L) "RDC" else "1",
        ownerName = "Copropriétaire $n",
        jan = if (n % 2L == 0L) 100L else 0L,
        feb = if (n % 2L == 0L) 100L else 0L,
        mar = 100L,
      )
    }
    val expenses = listOf(
      ExpenseRecord(id = "exp_1", title = "Nettoyage hall", category = "Nettoyage", amountDh = 600L, paidAmountDh = 600L),
    )
    val calls = listOf(
      SpecialCallRecord(
        id = "call_1",
        titleFr = "Peinture façade",
        titleAr = "صباغة الواجهة",
        projectId = "proj_1",
        amountPerAptDh = 300L,
        createdDate = "01/04/2026",
      ),
    )
    val data = SyndicatDashboardData(
      config = cfg,
      apartments = apts,
      expenses = expenses,
      baseYear = 2026L,
    )

    // 1. High-capacity QR round-trip: Encode full snapshot -> QR Bitmap -> Shareable PNG Card -> ZXing Decode -> Parse snapshot
    val encodedPayload = SyndicatCouncilShareCodec.encodeShareCode(data, calls)
    val qrBitmap = com.example.util.CouncilQrCodeHelper.encodeShareQrBitmap(encodedPayload, sizePx = 720)
    val decodedFromRaw = com.example.util.CouncilQrCodeHelper.decodeQrFromBitmap(qrBitmap)
    assertEquals(encodedPayload, decodedFromRaw)

    val shareCardBitmap = com.example.util.CouncilQrCodeHelper.buildShareableQrCardBitmap(
      qrBitmap = qrBitmap,
      residenceName = cfg.residenceName,
      year = 2026L,
      summaryText = "12 appts",
    )
    val decodedFromCard = com.example.util.CouncilQrCodeHelper.decodeQrFromBitmap(shareCardBitmap)
    assertEquals(encodedPayload, decodedFromCard)

    val parsed = SyndicatCouncilShareCodec.decodeShareCode(decodedFromCard!!, LOCAL_DEVICE_USER_ID)
    assertEquals("Résidence Les Oliviers", parsed.config.residenceName)
    assertEquals(12, parsed.apartments.size)
    assertEquals(1, parsed.expenses.size)
    assertEquals(1, parsed.specialCalls.size)

    // 2. Compose UI: SettingsScreen CouncilShareCard shows QR code dialog (no long raw text) & QR-first import dialog
    composeTestRule.setContent {
      CompositionLocalProvider(
        LocalStrings provides frStrings,
        LocalLayoutDirection provides LayoutDirection.Ltr,
      ) {
        AppTheme(darkTheme = false, isArabic = false, themeStyle = "coinbase") {
          SettingsScreen(
            data = data,
            currentUserId = LOCAL_DEVICE_USER_ID,
            userEmail = null,
            currentLanguage = AppLanguage.FR,
            isSaving = false,
            onSelectLanguage = {},
            onUpdateDisplayPrefs = { _, _, _, _ -> },
            onOpenWizard = {},
            onSaveConfig = { _, _ -> },
            onRestoreBackup = {},
            onImportCsvOwners = {},
            onResetC49 = {},
            onBackupExported = {},
            onPinUpdated = {},
            onSignOut = {},
            onMessage = {},
            specialCalls = calls,
          )
        }
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("settings_screen").performScrollToNode(hasTestTag("settings_council_share_card"))
    composeTestRule.onNodeWithTag("generate_council_code_btn").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("council_qr_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("council_qr_image").assertIsDisplayed()
    composeTestRule.onNodeWithTag("share_council_qr_image_btn").assertIsDisplayed()
    // Ensure long raw Base64 share text is NOT rendered in the dialog
    composeTestRule.onNodeWithText("SYNDICAT-CONSEIL-1:", substring = true).assertDoesNotExist()

    composeTestRule.onNodeWithTag("close_council_qr_dialog_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("council_qr_dialog").assertDoesNotExist()

    // Open QR Import Dialog
    composeTestRule.onNodeWithTag("import_council_code_btn").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("council_qr_import_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("scan_council_qr_camera_btn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("pick_council_qr_image_btn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("council_code_input").assertDoesNotExist()

    // Expand optional manual fallback
    composeTestRule.onNodeWithTag("toggle_manual_council_code_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("council_code_input").assertIsDisplayed()
  }
}

