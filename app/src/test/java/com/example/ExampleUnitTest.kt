package com.example

import com.example.data.local.SpecialCallPayment
import com.example.data.local.SpecialCallRecord
import com.example.data.model.ApartmentRecord
import com.example.data.model.ExpenseRecord
import com.example.data.model.PaymentRecord
import com.example.data.model.SyndicatConfig
import com.example.ui.viewmodel.SyndicatDashboardData
import com.example.util.BackupManager
import com.example.util.InvoiceOcrHelper
import com.example.util.QrCodeGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Local JVM unit tests for SYNDICAT v2.0 utilities and calculations.
 */
class ExampleUnitTest {

  @Test
  fun qrCodeGenerator_encodesVersion4MatrixWithFinderPatterns() {
    val matrix = QrCodeGenerator.encode("SYNDICAT|C49|APT=1|NO=12|AMT=50")
    assertEquals(33, matrix.size)
    matrix.forEach { row -> assertEquals(33, row.size) }

    // Top-left 7x7 finder outer border is dark and inner ring (1,1) is light
    assertTrue(matrix[0][0])
    assertTrue(matrix[0][6])
    assertTrue(matrix[6][0])
    assertFalse(matrix[1][1])
    assertTrue(matrix[3][3])

    // Top-right and bottom-left finder corners
    assertTrue(matrix[0][26])
    assertTrue(matrix[0][32])
    assertTrue(matrix[26][0])
    assertTrue(matrix[32][0])

    // Timing patterns alternate on row 6 and col 6
    assertTrue(matrix[6][8])
    assertFalse(matrix[6][9])
    assertTrue(matrix[8][6])
    assertFalse(matrix[9][6])
  }

  @Test
  fun csvExports_includeUtf8BomAndEscapeSpecialCharacters() {
    val config = SyndicatConfig(
      userId = "test",
      language = "fr",
      currency = "DH",
      year = 2026L,
    )
    val apt = ApartmentRecord(
      id = "apt_01",
      userId = "test",
      number = 1L,
      floor = "RDC",
      ownerName = "Ali; Ben \"Syndic\"",
      ownerNameAr = "علي",
    )
    val payment = PaymentRecord(
      id = "pay_1",
      userId = "test",
      apartmentNumber = 1L,
      year = 2026L,
      amountDh = 150L,
      monthsCovered = listOf(1L, 2L, 3L),
      paymentMethod = "transfer",
      paymentDate = "01/03/2026",
      receiptNumber = 7L,
      notes = "Trimestre 1",
    )
    val expense = ExpenseRecord(
      id = "exp_1",
      userId = "test",
      title = "Caméra; hall",
      category = "security",
      amountDh = 1200L,
      paidAmountDh = 800L,
      supplier = "SecuMaroc",
      dateLabel = "05/03/2026",
    )
    val data = SyndicatDashboardData(
      config = config,
      apartments = listOf(apt),
      expenses = listOf(expense),
      projects = emptyList(),
      payments = listOf(payment),
      baseApartments = listOf(apt),
      baseYear = 2026L,
    )

    val paymentsCsv = BackupManager.generatePaymentsCsv(data)
    assertTrue(paymentsCsv.startsWith("\uFEFFReceiptNo;"))
    assertTrue(paymentsCsv.contains("#007"))
    assertTrue(paymentsCsv.contains("\"Ali; Ben \"\"Syndic\"\"\""))

    val expensesCsv = BackupManager.generateExpensesCsv(data)
    assertTrue(expensesCsv.startsWith("\uFEFFTitle;"))
    assertTrue(expensesCsv.contains("\"Caméra; hall\""))
    assertTrue(expensesCsv.contains("1200;800;400"))
  }

  @Test
  fun specialCallRecord_calculatesEligibleExpectedAndCollectedTotals() {
    val apartments = listOf(
      ApartmentRecord(id = "apt_01", number = 1L, status = "occupied"),
      ApartmentRecord(id = "apt_02", number = 2L, status = "occupied"),
      ApartmentRecord(id = "apt_03", number = 3L, status = "empty"),
      ApartmentRecord(id = "apt_04", number = 4L, status = "exempt"),
    )
    val call = SpecialCallRecord(
      id = "sc_1",
      titleFr = "Peinture façade",
      titleAr = "صباغة الواجهة",
      projectId = "proj_1",
      amountPerAptDh = 300L,
      createdDate = "10/03/2026",
      payments = mapOf(
        1L to SpecialCallPayment(apartmentNumber = 1L, paidAmountDh = 300L, paymentDate = "11/03/2026"),
      ),
    )

    assertEquals(2, call.eligibleApartments(apartments).size)
    assertEquals(600L, call.expectedTotalDh(apartments))
    assertEquals(300L, call.collectedTotalDh(apartments))
    assertEquals(1, call.paidApartmentsCount(apartments))
    assertTrue(call.isApartmentPaid(1L))
    assertFalse(call.isApartmentPaid(2L))
  }

  @Test
  fun invoiceOcrHelper_extractsTotalTtcAmountDateAndSupplierAndIgnoresPlainYears() {
    val invoiceText = """
      STE ATLAS ELEVATOR
      Facture N° 104
      Date: 15-03-2026
      Entretien mensuel: 600 DH
      Total TTC: 1 450,00 DH
    """.trimIndent()
    val parsed = InvoiceOcrHelper.parseInvoiceText(invoiceText)
    assertEquals("STE ATLAS ELEVATOR", parsed.detectedSupplier)
    assertEquals("15/03/2026", parsed.detectedDate)
    assertEquals(1450L, parsed.detectedAmountDh)

    val plainYearText = """
      Plomberie Al Amal
      Intervention exercice 2026
      Main d'oeuvre 320
    """.trimIndent()
    val parsedPlainYear = InvoiceOcrHelper.parseInvoiceText(plainYearText)
    assertEquals(320L, parsedPlainYear.detectedAmountDh)
    assertEquals("Plomberie Al Amal", parsedPlainYear.detectedSupplier)
  }
}
