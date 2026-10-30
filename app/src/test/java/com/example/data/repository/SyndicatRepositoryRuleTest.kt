package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.ApartmentRecord
import com.example.data.model.MonthColumn
import com.example.data.model.PaymentRecord
import com.example.data.model.ProjectRecord
import com.example.data.model.SyndicatConfig
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class SyndicatRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun ensureSeeded_populatesExactPdfTotalsAndSupportsVariableFloorsAndPartialExpenses() = runBlocking {
    val uniqueEmail = "alice_${UUID.randomUUID().toString().take(8)}@test.com"
    val aliceUid = signInTestUser(uniqueEmail)
    val repository = SyndicatRepository(firestore, auth)

    withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.ensureSeeded(forceReset = true)
    }

    val initialConfig = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeConfig(aliceUid).first { it != null }!!
    }
    assertEquals("ar", initialConfig.language)

    val apartments = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeApartments(aliceUid).first { it.size == 30 }
    }
    assertEquals(30, apartments.size)
    assertEquals(8200L, apartments.sumOf { it.totalForConfig(initialConfig) })
    assertEquals("عبد الفتاح", apartments.first { it.number == 1L }.ownerNameAr)

    // Test partial supplier expense (amountDh = 1000, paidAmountDh = 600 -> remaining 400)
    val expId = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.addExpense(
        title = "صيانة المصعد",
        category = "Ascenseur",
        amountDh = 1000L,
        paidAmountDh = 600L,
        supplier = "شركة المصاعد",
        dateLabel = "15/10/2026",
        notes = "دفعة أولى",
      )
    }
    assertTrue(expId.isNotBlank())

    val expenses = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeExpenses(aliceUid).first { list -> list.any { it.id == expId } }
    }
    val addedExp = expenses.first { it.id == expId }
    assertEquals(600L, addedExp.effectivePaidDh)
    assertEquals(400L, addedExp.remainingToPayDh)

    // Reconfigure building with variable floor counts (4 + 6 + 6 + 6 + 6 + 4 = 32 apartments)
    val customConfig = SyndicatConfig(
      userId = aliceUid,
      language = "ar",
      digitsAr = true,
      themeMode = "system",
      fontScale = "normal",
      syndicatName = "إدارة السانديك",
      residenceName = "إقامة الياسمين",
      address = "شارع الزرقطوني",
      city = "الدار البيضاء",
      treasurerName = "أحمد العلوي",
      buildingTitle = "المساهمة الشهرية 2026 – إقامة الياسمين",
      currency = "درهم",
      year = 2026L,
      openingBalanceDh = 1500L,
      dueDayOfMonth = 5L,
      minReminderAmountDh = 20L,
      includeRdc = true,
      floorsCount = 6L,
      apartmentsCount = 32L,
      floorAptCounts = listOf(4L, 6L, 6L, 6L, 6L, 4L),
      defaultMonthlyFee = 50L,
      cutoffMonth = 10L,
      activeMonths = MonthColumn.FULL_YEAR_NUMBERS,
      feeMar = 150L,
      feeJuin = 100L,
      feeJuil = 100L,
      footerNote = "تحديث أسبوعي",
      onboardingCompleted = true,
      seeded = true,
    )

    withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.saveFullConfiguration(customConfig, resetBlankBuilding = false)
    }

    val updatedAppts = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeApartments(aliceUid).first { it.size == 32 }
    }
    assertEquals(32, updatedAppts.size)
    // First 4 apartments on RDC, apartment 5 on 1er
    assertEquals("RDC", updatedAppts.first { it.number == 4L }.floor)
    assertEquals("1er", updatedAppts.first { it.number == 5L }.floor)
    assertEquals("5e", updatedAppts.last().floor)

    // Test marking reminder & receipt increment
    withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.markReminderSent("appt_01")
      val nextNo = repository.incrementReceiptCount(updatedAppts.first { it.number == 1L })
      assertEquals(1L, nextNo)
    }

    val savedCfg = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeConfig(aliceUid).first { it?.openingBalanceDh == 1500L }!!
    }
    assertEquals(1500L, savedCfg.openingBalanceDh)
    assertEquals(150L, savedCfg.feeMar)
    assertEquals(32L, savedCfg.apartmentsCount)
    assertEquals("coinbase", savedCfg.themeStyle)

    for (targetStyle in listOf("meta", "minimax", "coinbase")) {
      withTimeout(DEFAULT_TIMEOUT_MS) {
        repository.updateDisplayPrefs(themeStyle = targetStyle, themeMode = "dark")
      }
      val styledCfg = withTimeout(FLOW_TIMEOUT_MS) {
        repository.observeConfig(aliceUid).first { it?.themeStyle == targetStyle && it.themeMode == "dark" }!!
      }
      assertEquals("dark", styledCfg.themeMode)
      assertEquals(targetStyle, styledCfg.themeStyle)
    }

    // Verify seeded projects & ProjectRecord CRUD + priority sorting
    val seededProjects = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeProjects(aliceUid).first { it.size >= 4 }
    }
    assertEquals(1L, seededProjects.first().priority)

    val newProjId = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.addProject(
        ProjectRecord(
          titleAr = "تجديد مصابيح السطح",
          titleFr = "Éclairage LED terrasse",
          descriptionAr = "كشافات شمسية",
          descriptionFr = "Projecteurs solaires",
          category = "Électricité",
          priority = 1L,
          status = "planned",
          targetBudgetDh = 900L,
          minCashToStartDh = 450L,
          allocatedAmountDh = 0L,
          progressPercent = 0L,
          contractor = "ElectroCasa",
          targetDateLabel = "20/12/2026",
        )
      )
    }
    assertTrue(newProjId.isNotBlank())

    val withAdded = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeProjects(aliceUid).first { list -> list.any { it.id == newProjId } }
    }
    val createdProj = withAdded.first { it.id == newProjId }
    assertEquals(450L, createdProj.effectiveMinCashToStartDh)

    withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.updateProject(
        createdProj.copy(
          status = "in_progress",
          progressPercent = 50L,
          allocatedAmountDh = 450L,
        )
      )
    }

    val updatedProjList = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeProjects(aliceUid).first { list ->
        list.any { it.id == newProjId && it.progressPercent == 50L }
      }
    }
    assertEquals("in_progress", updatedProjList.first { it.id == newProjId }.status)

    withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.deleteProject(newProjId)
    }
  }

  @Test
  fun crossUserAccess_toAnotherUsersApartmentsAndProjects_failsWithPermissionDenied() = runBlocking {
    val aliceUid = signInTestUser("owner_${UUID.randomUUID().toString().take(8)}@test.com")
    val aliceRepo = SyndicatRepository(firestore, auth)
    withTimeout(DEFAULT_TIMEOUT_MS) {
      aliceRepo.ensureSeeded()
    }

    signInTestUser("intruder_${UUID.randomUUID().toString().take(8)}@test.com")
    val intruderRepo = SyndicatRepository(firestore, auth)

    try {
      withTimeout(DEFAULT_TIMEOUT_MS) {
        intruderRepo.getApartmentById(aliceUid, "appt_01")
      }
      fail("Expected FirebaseFirestoreException PERMISSION_DENIED for cross-user apartment access")
    } catch (e: FirebaseFirestoreException) {
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
    }

    try {
      withTimeout(DEFAULT_TIMEOUT_MS) {
        intruderRepo.getProjectById(aliceUid, "proj_01")
      }
      fail("Expected FirebaseFirestoreException PERMISSION_DENIED for cross-user project access")
    } catch (e: FirebaseFirestoreException) {
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
    }
  }

  @Test
  fun unauthenticatedUser_observingApartmentsAndProjects_failsWithPermissionDenied() = runBlocking {
    auth.signOut()
    val repository = SyndicatRepository(firestore, auth)

    try {
      withTimeout(FLOW_TIMEOUT_MS) {
        repository.observeApartments("unauth_target_uid").first()
      }
      fail("Expected PERMISSION_DENIED when unauthenticated on apartments")
    } catch (e: Exception) {
      val firestoreEx = e.unwrapFirestoreException()
      assertNotNull("Expected nested FirebaseFirestoreException, got $e", firestoreEx)
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx!!.code)
    }

    try {
      withTimeout(FLOW_TIMEOUT_MS) {
        repository.observeProjects("unauth_target_uid").first()
      }
      fail("Expected PERMISSION_DENIED when unauthenticated on projects")
    } catch (e: Exception) {
      val firestoreEx = e.unwrapFirestoreException()
      assertNotNull("Expected nested FirebaseFirestoreException, got $e", firestoreEx)
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx!!.code)
    }
  }

  @Test
  fun unifiedExpenseCategories_multiYearPayments_andAmountAndApartmentIdCoherence() = runBlocking {
    val aliceUid = signInTestUser("syndic_items456_${UUID.randomUUID().toString().take(8)}@test.com")
    val repository = SyndicatRepository(firestore, auth)

    withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.ensureSeeded(forceReset = true)
    }

    // Item 4: Expense with "Sécurité" (cameras) and "Peinture" must succeed without falling back to "Divers"
    val cameraExpId = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.addExpense(
        title = "Caméras de surveillance entrée",
        category = "Sécurité",
        amountDh = 1800L,
        paidAmountDh = 1200L,
        supplier = "SecurMaroc",
        dateLabel = "18/10/2026",
        notes = "Acompte caméras",
      )
    }
    val paintExpId = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.addExpense(
        title = "Retouche peinture cage d'escalier",
        category = "Peinture",
        amountDh = 650L,
        paidAmountDh = 650L,
        supplier = "Artisan Peintre",
        dateLabel = "20/10/2026",
        notes = "",
      )
    }

    val expenses = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeExpenses(aliceUid).first { list ->
        list.any { it.id == cameraExpId } && list.any { it.id == paintExpId }
      }
    }
    assertEquals("Sécurité", expenses.first { it.id == cameraExpId }.category)
    assertEquals("Peinture", expenses.first { it.id == paintExpId }.category)

    // Item 6a: paidAmountDh > amountDh must be rejected
    try {
      repository.addExpense(
        title = "Facture incohérente",
        category = "Sécurité",
        amountDh = 500L,
        paidAmountDh = 750L,
        supplier = "Test",
        dateLabel = "21/10/2026",
        notes = "",
      )
      fail("Expected IllegalArgumentException when paidAmountDh > amountDh")
    } catch (_: IllegalArgumentException) {
      // Expected
    }

    // Item 6b: Apartment ID mismatching apartment number must be rejected
    try {
      repository.updateApartment(
        ApartmentRecord(
          id = "appt_09",
          userId = aliceUid,
          number = 1L,
          floor = "RDC",
          status = "occupied",
        )
      )
      fail("Expected IllegalArgumentException when apartment document ID does not match number")
    } catch (_: IllegalArgumentException) {
      // Expected
    }

    // Item 5: Multi-year PaymentRecord table with date, mode, receiptNumber & apartment coherence
    val pay2026Id = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.addPayment(
        PaymentRecord(
          apartmentId = "appt_01",
          apartmentNumber = 1L,
          year = 2026L,
          month = 11L,
          monthsCovered = listOf(11L, 12L),
          amountDh = 100L,
          paymentDate = "05/11/2026",
          paymentMethod = "transfer",
          receiptNumber = 12L,
          notes = "Virement Nov+Déc 2026",
        )
      )
    }
    val pay2027Id = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.addPayment(
        PaymentRecord(
          apartmentId = "appt_01",
          apartmentNumber = 1L,
          year = 2027L,
          month = 1L,
          monthsCovered = listOf(1L, 2L, 3L),
          amountDh = 150L,
          paymentDate = "10/01/2027",
          paymentMethod = "check",
          receiptNumber = 13L,
          notes = "Trimestre 1 2027",
        )
      )
    }

    val observedPayments = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observePayments(aliceUid).first { list ->
        list.any { it.id == pay2026Id } && list.any { it.id == pay2027Id }
      }
    }
    val p26 = observedPayments.first { it.id == pay2026Id }
    val p27 = observedPayments.first { it.id == pay2027Id }
    assertEquals("transfer", p26.paymentMethod)
    assertEquals("05/11/2026", p26.paymentDate)
    assertEquals(2026L, p26.year)
    assertEquals("check", p27.paymentMethod)
    assertEquals(2027L, p27.year)
    assertEquals(listOf(1L, 2L, 3L), p27.monthsCovered)

    withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.deletePayment(pay2027Id)
    }
    val afterDelete = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observePayments(aliceUid).first { list -> list.none { it.id == pay2027Id } }
    }
    assertTrue(afterDelete.any { it.id == pay2026Id })
  }

  private companion object {
    const val DEFAULT_TIMEOUT_MS = 10000L
    const val FLOW_TIMEOUT_MS = 5000L
  }
}
