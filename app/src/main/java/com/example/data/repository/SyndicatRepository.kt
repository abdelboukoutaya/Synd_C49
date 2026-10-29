package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.error.OperationType
import com.example.data.error.handleFirestoreError
import com.example.data.model.ApartmentRecord
import com.example.data.model.DEFAULT_RELANCE_HEADER_AR
import com.example.data.model.DEFAULT_WHATSAPP_TEMPLATE_AR
import com.example.data.model.EXPENSE_CATEGORIES
import com.example.data.model.ExpenseRecord
import com.example.data.model.MonthColumn
import com.example.data.model.PROJECT_CATEGORIES
import com.example.data.model.ProjectRecord
import com.example.data.model.SyndicatConfig
import com.example.data.model.sortProjectsByPriority
import com.example.data.seed.InitialSyndicatData
import com.example.util.ParsedOwnerRow
import com.example.util.ParsedSyndicatBackup
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class SyndicatRepository(
  private val db: FirebaseFirestore,
  private val auth: FirebaseAuth = Firebase.auth,
) {
  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    ),
    Firebase.auth,
  )

  private fun requireUserId(): String {
    return auth.currentUser?.uid
      ?: throw IllegalStateException("User must be signed in with Google before modifying Firestore data.")
  }

  fun observeConfig(userId: String): Flow<SyndicatConfig?> {
    val docRef = db.collection("users")
      .document(userId)
      .collection("settings")
      .document(CONFIG_DOC_ID)
    return docRef.snapshots()
      .map { snapshot ->
        if (snapshot.exists()) {
          snapshot.toObject(SyndicatConfig::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } else {
          null
        }
      }
      .catch { error ->
        if (error is Exception) {
          handleFirestoreError(error, OperationType.GET, docRef.path)
        }
        throw error
      }
  }

  fun observeApartments(userId: String): Flow<List<ApartmentRecord>> {
    val collectionPath = "users/$userId/apartments"
    return db.collection("users")
      .document(userId)
      .collection("apartments")
      .whereEqualTo("userId", userId)
      .snapshots()
      .map { snapshot ->
        snapshot.toObjects(
          ApartmentRecord::class.java,
          DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
        ).sortedBy { it.number }
      }
      .catch { error ->
        if (error is Exception) {
          handleFirestoreError(error, OperationType.LIST, collectionPath)
        }
        throw error
      }
  }

  fun observeExpenses(userId: String): Flow<List<ExpenseRecord>> {
    val collectionPath = "users/$userId/expenses"
    return db.collection("users")
      .document(userId)
      .collection("expenses")
      .whereEqualTo("userId", userId)
      .snapshots()
      .map { snapshot ->
        snapshot.toObjects(
          ExpenseRecord::class.java,
          DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
        ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
      }
      .catch { error ->
        if (error is Exception) {
          handleFirestoreError(error, OperationType.LIST, collectionPath)
        }
        throw error
      }
  }

  fun observeProjects(userId: String): Flow<List<ProjectRecord>> {
    val collectionPath = "users/$userId/projects"
    return db.collection("users")
      .document(userId)
      .collection("projects")
      .whereEqualTo("userId", userId)
      .snapshots()
      .map { snapshot ->
        sortProjectsByPriority(
          snapshot.toObjects(
            ProjectRecord::class.java,
            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
          )
        )
      }
      .catch { error ->
        if (error is Exception) {
          handleFirestoreError(error, OperationType.LIST, collectionPath)
        }
        throw error
      }
  }

  suspend fun getApartmentById(targetUserId: String, apartmentId: String): ApartmentRecord? {
    val docRef = db.collection("users")
      .document(targetUserId)
      .collection("apartments")
      .document(apartmentId)
    return try {
      val snapshot = docRef.get().await()
      snapshot.toObject(ApartmentRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.GET, docRef.path)
      throw e
    }
  }

  suspend fun getProjectById(targetUserId: String, projectId: String): ProjectRecord? {
    val docRef = db.collection("users")
      .document(targetUserId)
      .collection("projects")
      .document(projectId)
    return try {
      val snapshot = docRef.get().await()
      snapshot.toObject(ProjectRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.GET, docRef.path)
      throw e
    }
  }

  suspend fun ensureSeeded(forceReset: Boolean = false, preserveLanguage: String? = null) {
    val uid = requireUserId()
    val userRef = db.collection("users").document(uid)
    val configRef = userRef.collection("settings").document(CONFIG_DOC_ID)

    try {
      val existingConfig = configRef.get().await()
      if (!forceReset && existingConfig.exists() && existingConfig.getBoolean("seeded") == true) {
        return
      }

      val existingLang = preserveLanguage
        ?: existingConfig.getString("language")?.takeIf { it in listOf("fr", "ar") }
        ?: "ar"

      val existingApptsSnapshot = userRef.collection("apartments")
        .whereEqualTo("userId", uid)
        .get()
        .await()
      val existingApptIds = existingApptsSnapshot.documents.map { it.id }.toSet()

      val existingExpensesSnapshot = userRef.collection("expenses")
        .whereEqualTo("userId", uid)
        .get()
        .await()
      val existingExpenseIds = existingExpensesSnapshot.documents.map { it.id }.toSet()

      val batch = db.batch()
      val seedDocIds = InitialSyndicatData.apartments.map { it.docId }.toSet()

      if (forceReset) {
        for (doc in existingApptsSnapshot.documents) {
          if (doc.id !in seedDocIds) {
            batch.delete(doc.reference)
          }
        }
      }

      for (seed in InitialSyndicatData.apartments) {
        val apptRef = userRef.collection("apartments").document(seed.docId)
        val payload = mapOf(
          "floor" to seed.floor,
          "status" to seed.status,
          "ownerName" to seed.ownerName,
          "ownerNameAr" to seed.ownerNameAr,
          "jan" to seed.jan,
          "feb" to seed.feb,
          "mar" to seed.mar,
          "apr" to seed.apr,
          "may" to seed.may,
          "juin" to seed.juin,
          "juil" to seed.juil,
          "aout" to seed.aout,
          "sept" to seed.sept,
          "oct" to seed.oct,
          "nov" to seed.nov,
          "dec" to seed.dec,
          "updatedAt" to FieldValue.serverTimestamp(),
        )
        if (seed.docId in existingApptIds) {
          batch.update(apptRef, payload)
        } else {
          batch.set(
            apptRef,
            payload + mapOf(
              "userId" to uid,
              "number" to seed.number,
              "customNumberLabel" to "",
              "phone" to "",
              "isTenant" to false,
              "customMonthlyFee" to -1L,
              "lastReminderAt" to 0L,
              "receiptCount" to 0L,
              "createdAt" to FieldValue.serverTimestamp(),
            ),
          )
        }
      }

      if (forceReset) {
        for (doc in existingExpensesSnapshot.documents) {
          if (doc.id != InitialSyndicatData.INITIAL_EXPENSE_ID) {
            batch.delete(doc.reference)
          }
        }
      }

      val initialExpRef = userRef.collection("expenses").document(InitialSyndicatData.INITIAL_EXPENSE_ID)
      if (InitialSyndicatData.INITIAL_EXPENSE_ID in existingExpenseIds) {
        if (forceReset) {
          batch.update(
            initialExpRef,
            mapOf(
              "title" to InitialSyndicatData.INITIAL_EXPENSE_TITLE,
              "category" to "Historique",
              "amountDh" to InitialSyndicatData.INITIAL_EXPENSE_AMOUNT_DH,
              "paidAmountDh" to InitialSyndicatData.INITIAL_EXPENSE_AMOUNT_DH,
              "supplier" to "",
              "dateLabel" to InitialSyndicatData.INITIAL_EXPENSE_DATE,
              "notes" to InitialSyndicatData.INITIAL_EXPENSE_NOTES,
              "updatedAt" to FieldValue.serverTimestamp(),
            ),
          )
        }
      } else if (existingExpenseIds.isEmpty() || forceReset) {
        batch.set(
          initialExpRef,
          mapOf(
            "userId" to uid,
            "title" to InitialSyndicatData.INITIAL_EXPENSE_TITLE,
            "category" to "Historique",
            "amountDh" to InitialSyndicatData.INITIAL_EXPENSE_AMOUNT_DH,
            "paidAmountDh" to InitialSyndicatData.INITIAL_EXPENSE_AMOUNT_DH,
            "supplier" to "",
            "dateLabel" to InitialSyndicatData.INITIAL_EXPENSE_DATE,
            "notes" to InitialSyndicatData.INITIAL_EXPENSE_NOTES,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
          ),
        )
      }

      val existingProjectsSnapshot = userRef.collection("projects")
        .whereEqualTo("userId", uid)
        .get()
        .await()
      val existingProjectIds = existingProjectsSnapshot.documents.map { it.id }.toSet()
      val seedProjectIds = InitialSyndicatData.projects.map { it.docId }.toSet()

      if (forceReset) {
        for (doc in existingProjectsSnapshot.documents) {
          if (doc.id !in seedProjectIds) {
            batch.delete(doc.reference)
          }
        }
      }

      if (existingProjectIds.isEmpty() || forceReset) {
        for (seedProj in InitialSyndicatData.projects) {
          val projRef = userRef.collection("projects").document(seedProj.docId)
          val projPayload = mapOf(
            "titleAr" to seedProj.titleAr,
            "titleFr" to seedProj.titleFr,
            "descriptionAr" to seedProj.descriptionAr,
            "descriptionFr" to seedProj.descriptionFr,
            "category" to seedProj.category,
            "priority" to seedProj.priority,
            "status" to seedProj.status,
            "targetBudgetDh" to seedProj.targetBudgetDh,
            "minCashToStartDh" to seedProj.minCashToStartDh,
            "allocatedAmountDh" to seedProj.allocatedAmountDh,
            "progressPercent" to seedProj.progressPercent,
            "contractor" to seedProj.contractor,
            "targetDateLabel" to seedProj.targetDateLabel,
            "updatedAt" to FieldValue.serverTimestamp(),
          )
          if (seedProj.docId in existingProjectIds) {
            batch.update(projRef, projPayload)
          } else {
            batch.set(
              projRef,
              projPayload + mapOf(
                "userId" to uid,
                "createdAt" to FieldValue.serverTimestamp(),
              ),
            )
          }
        }
      }

      val baseConfigPayload = buildConfigPayloadMap(
        SyndicatConfig(
          userId = uid,
          language = existingLang,
          digitsAr = false,
          themeStyle = "airtable",
          themeMode = "system",
          fontScale = "normal",
          syndicatName = InitialSyndicatData.DEFAULT_SYNDICAT_NAME,
          residenceName = InitialSyndicatData.DEFAULT_RESIDENCE_NAME,
          address = InitialSyndicatData.DEFAULT_ADDRESS,
          city = "",
          treasurerName = "",
          buildingTitle = InitialSyndicatData.DEFAULT_BUILDING_TITLE,
          currency = if (existingLang == "fr") "DH" else InitialSyndicatData.DEFAULT_CURRENCY,
          year = InitialSyndicatData.DEFAULT_YEAR,
          openingBalanceDh = 0L,
          dueDayOfMonth = 5L,
          minReminderAmountDh = 20L,
          whatsappTemplate = DEFAULT_WHATSAPP_TEMPLATE_AR,
          relanceHeaderNote = DEFAULT_RELANCE_HEADER_AR,
          includeRdc = true,
          floorsCount = 5L,
          apartmentsCount = 30L,
          floorAptCounts = listOf(6L, 6L, 6L, 6L, 6L),
          defaultMonthlyFee = 50L,
          cutoffMonth = 10L,
          activeMonths = InitialSyndicatData.DEFAULT_ACTIVE_MONTHS,
          feeJan = 50L,
          feeFeb = 50L,
          feeMar = 50L,
          feeApr = 50L,
          feeMay = 50L,
          feeJuin = 100L,
          feeJuil = 100L,
          feeAout = 50L,
          feeSept = 50L,
          feeOct = 50L,
          feeNov = 50L,
          feeDec = 50L,
          footerNote = InitialSyndicatData.DEFAULT_FOOTER_NOTE,
          onboardingCompleted = true,
          seeded = true,
        )
      )

      if (existingConfig.exists()) {
        batch.update(configRef, baseConfigPayload)
      } else {
        batch.set(
          configRef,
          baseConfigPayload + mapOf(
            "userId" to uid,
            "createdAt" to FieldValue.serverTimestamp(),
          ),
        )
      }

      batch.commit().await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, "users/$uid")
      throw e
    }
  }

  suspend fun saveFullConfiguration(
    config: SyndicatConfig,
    resetBlankBuilding: Boolean = false,
    explicitApartments: List<ApartmentRecord>? = null,
  ) {
    val uid = requireUserId()
    val userRef = db.collection("users").document(uid)
    val configRef = userRef.collection("settings").document(CONFIG_DOC_ID)

    val safeConfig = sanitizeConfig(config, uid)

    try {
      val existingConfigSnap = configRef.get().await()
      val existingApptsSnap = userRef.collection("apartments")
        .whereEqualTo("userId", uid)
        .get()
        .await()
      val existingById = existingApptsSnap.documents.associateBy { it.id }

      val batch = db.batch()
      val configMap = buildConfigPayloadMap(safeConfig)
      if (existingConfigSnap.exists()) {
        batch.update(configRef, configMap)
      } else {
        batch.set(
          configRef,
          configMap + mapOf(
            "userId" to uid,
            "createdAt" to FieldValue.serverTimestamp(),
          ),
        )
      }

      val targetCount = safeConfig.apartmentsCount.toInt()
      val targetDocIds = mutableSetOf<String>()
      val explicitByNum = explicitApartments?.associateBy { it.number.toInt() } ?: emptyMap()

      for (num in 1..targetCount) {
        val docId = "appt_%02d".format(num)
        targetDocIds.add(docId)
        val expectedFloor = safeConfig.floorForApartmentNumber(num)
        val apptRef = userRef.collection("apartments").document(docId)
        val existingDoc = existingById[docId]
        val explicitAppt = explicitByNum[num]

        if (explicitAppt != null) {
          val apptPayload = buildApartmentUpdateMap(
            explicitAppt.copy(
              number = num.toLong(),
              floor = expectedFloor,
            )
          )
          if (existingDoc != null) {
            batch.update(apptRef, apptPayload)
          } else {
            batch.set(
              apptRef,
              apptPayload + mapOf(
                "userId" to uid,
                "createdAt" to FieldValue.serverTimestamp(),
              ),
            )
          }
        } else if (existingDoc != null) {
          if (resetBlankBuilding) {
            batch.update(
              apptRef,
              buildApartmentUpdateMap(
                ApartmentRecord(
                  number = num.toLong(),
                  floor = expectedFloor,
                  status = "occupied",
                )
              ),
            )
          } else if (existingDoc.getString("floor") != expectedFloor || existingDoc.getLong("number") != num.toLong()) {
            batch.update(
              apptRef,
              mapOf(
                "number" to num.toLong(),
                "floor" to expectedFloor,
                "updatedAt" to FieldValue.serverTimestamp(),
              ),
            )
          }
        } else {
          batch.set(
            apptRef,
            buildApartmentUpdateMap(
              ApartmentRecord(
                number = num.toLong(),
                floor = expectedFloor,
                status = "occupied",
              )
            ) + mapOf(
              "userId" to uid,
              "createdAt" to FieldValue.serverTimestamp(),
            ),
          )
        }
      }

      for ((docId, docSnap) in existingById) {
        if (docId !in targetDocIds) {
          batch.delete(docSnap.reference)
        }
      }

      if (resetBlankBuilding) {
        val existingExpenses = userRef.collection("expenses")
          .whereEqualTo("userId", uid)
          .get()
          .await()
        for (expDoc in existingExpenses.documents) {
          batch.delete(expDoc.reference)
        }
        val existingProjects = userRef.collection("projects")
          .whereEqualTo("userId", uid)
          .get()
          .await()
        for (projDoc in existingProjects.documents) {
          batch.delete(projDoc.reference)
        }
      }

      batch.commit().await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, "users/$uid/settings/$CONFIG_DOC_ID")
      throw e
    }
  }

  suspend fun restoreFromBackup(backup: ParsedSyndicatBackup) {
    val uid = requireUserId()
    val userRef = db.collection("users").document(uid)
    val configRef = userRef.collection("settings").document(CONFIG_DOC_ID)
    val safeConfig = sanitizeConfig(backup.config, uid)

    try {
      val existingConfigSnap = configRef.get().await()
      val existingAppts = userRef.collection("apartments").whereEqualTo("userId", uid).get().await()
      val existingExps = userRef.collection("expenses").whereEqualTo("userId", uid).get().await()

      val existingProjects = userRef.collection("projects").whereEqualTo("userId", uid).get().await()

      val batch = db.batch()
      for (doc in existingAppts.documents) batch.delete(doc.reference)
      for (doc in existingExps.documents) batch.delete(doc.reference)
      if (backup.projects.isNotEmpty()) {
        for (doc in existingProjects.documents) batch.delete(doc.reference)
      }

      val cfgPayload = buildConfigPayloadMap(safeConfig)
      if (existingConfigSnap.exists()) {
        batch.update(configRef, cfgPayload)
      } else {
        batch.set(configRef, cfgPayload + mapOf("userId" to uid, "createdAt" to FieldValue.serverTimestamp()))
      }

      for (appt in backup.apartments) {
        val docId = appt.id.ifBlank { "appt_%02d".format(appt.number) }
        val ref = userRef.collection("apartments").document(docId)
        batch.set(
          ref,
          buildApartmentUpdateMap(appt) + mapOf(
            "userId" to uid,
            "createdAt" to FieldValue.serverTimestamp(),
          ),
        )
      }

      for ((idx, exp) in backup.expenses.withIndex()) {
        val docId = exp.id.ifBlank { "exp_restored_$idx" }
        val ref = userRef.collection("expenses").document(docId)
        val cat = if (exp.category in EXPENSE_CATEGORIES) exp.category else "Divers"
        batch.set(
          ref,
          mapOf(
            "userId" to uid,
            "title" to exp.title.trim().ifBlank { "مصروف" }.take(150),
            "category" to cat,
            "amountDh" to exp.amountDh.coerceIn(0L, 10_000_000L),
            "paidAmountDh" to exp.paidAmountDh.coerceIn(-1L, 10_000_000L),
            "supplier" to exp.supplier.trim().take(100),
            "dateLabel" to exp.dateLabel.trim().ifBlank { "01/01/2026" }.take(30),
            "notes" to exp.notes.trim().take(300),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
          ),
        )
      }

      for ((idx, proj) in backup.projects.withIndex()) {
        val docId = proj.id.ifBlank { "proj_restored_$idx" }
        val ref = userRef.collection("projects").document(docId)
        batch.set(
          ref,
          buildProjectUpdateMap(proj) + mapOf(
            "userId" to uid,
            "createdAt" to FieldValue.serverTimestamp(),
          ),
        )
      }

      batch.commit().await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, "users/$uid")
      throw e
    }
  }

  suspend fun importOwnerRows(rows: List<ParsedOwnerRow>) {
    if (rows.isEmpty()) return
    val uid = requireUserId()
    val userRef = db.collection("users").document(uid)
    try {
      val existingAppts = userRef.collection("apartments").whereEqualTo("userId", uid).get().await()
      val byNumber = existingAppts.documents.associateBy { (it.getLong("number") ?: 0L).toInt() }
      val batch = db.batch()
      for (row in rows) {
        val doc = byNumber[row.apartmentNumber] ?: continue
        val updates = mutableMapOf<String, Any>(
          "updatedAt" to FieldValue.serverTimestamp(),
          "status" to "occupied",
        )
        if (row.ownerNameAr.isNotBlank()) updates["ownerNameAr"] = row.ownerNameAr.take(80)
        if (row.ownerNameLatin.isNotBlank()) updates["ownerName"] = row.ownerNameLatin.take(80)
        if (row.phone.isNotBlank()) updates["phone"] = row.phone.take(30)
        batch.update(doc.reference, updates)
      }
      batch.commit().await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, "users/$uid/apartments")
      throw e
    }
  }

  suspend fun updateDisplayPrefs(
    languageCode: String? = null,
    digitsAr: Boolean? = null,
    themeStyle: String? = null,
    themeMode: String? = null,
    fontScale: String? = null,
  ) {
    val uid = requireUserId()
    val docRef = db.collection("users")
      .document(uid)
      .collection("settings")
      .document(CONFIG_DOC_ID)
    try {
      val snap = docRef.get().await()
      if (!snap.exists()) return
      val updates = mutableMapOf<String, Any>("updatedAt" to FieldValue.serverTimestamp())
      languageCode?.let { updates["language"] = if (it == "fr") "fr" else "ar" }
      digitsAr?.let { updates["digitsAr"] = it }
      themeStyle?.let { updates["themeStyle"] = "airtable" }
      themeMode?.let { if (it in listOf("system", "light", "dark")) updates["themeMode"] = it }
      fontScale?.let { if (it in listOf("small", "normal", "large")) updates["fontScale"] = it }
      docRef.update(updates).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  suspend fun updateLanguage(languageCode: String) {
    updateDisplayPrefs(languageCode = languageCode)
  }

  suspend fun updateWhatsAppTemplate(template: String) {
    val uid = requireUserId()
    val docRef = db.collection("users")
      .document(uid)
      .collection("settings")
      .document(CONFIG_DOC_ID)
    try {
      val snap = docRef.get().await()
      if (!snap.exists()) return
      docRef.update(
        mapOf(
          "whatsappTemplate" to template.trim().ifBlank { DEFAULT_WHATSAPP_TEMPLATE_AR }.take(1000),
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  suspend fun updateApartment(apartment: ApartmentRecord) {
    val uid = requireUserId()
    require(apartment.id.isNotBlank()) { "Apartment ID is required." }
    val docRef = db.collection("users")
      .document(uid)
      .collection("apartments")
      .document(apartment.id)
    try {
      docRef.update(buildApartmentUpdateMap(apartment)).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  suspend fun markReminderSent(apartmentId: String) {
    if (apartmentId.isBlank()) return
    val uid = requireUserId()
    val docRef = db.collection("users")
      .document(uid)
      .collection("apartments")
      .document(apartmentId)
    try {
      docRef.update(
        mapOf(
          "lastReminderAt" to System.currentTimeMillis(),
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
    }
  }

  suspend fun incrementReceiptCount(apartment: ApartmentRecord): Long {
    val nextNo = (apartment.receiptCount + 1L).coerceIn(1L, 100_000L)
    if (apartment.id.isBlank()) return nextNo
    val uid = requireUserId()
    val docRef = db.collection("users")
      .document(uid)
      .collection("apartments")
      .document(apartment.id)
    try {
      docRef.update(
        mapOf(
          "receiptCount" to nextNo,
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
    }
    return nextNo
  }

  suspend fun addExpense(
    title: String,
    category: String,
    amountDh: Long,
    paidAmountDh: Long = amountDh,
    supplier: String = "",
    dateLabel: String,
    notes: String,
    explicitId: String? = null,
  ): String {
    val uid = requireUserId()
    val expenseId = explicitId?.takeIf { it.isNotBlank() }
      ?: "exp_${UUID.randomUUID().toString().replace("-", "").take(16)}"
    val docRef = db.collection("users")
      .document(uid)
      .collection("expenses")
      .document(expenseId)
    val safeCat = if (category in EXPENSE_CATEGORIES) category else "Divers"
    val safeAmount = amountDh.coerceIn(0L, 10_000_000L)
    val safePaid = paidAmountDh.coerceIn(0L, safeAmount)
    try {
      docRef.set(
        mapOf(
          "userId" to uid,
          "title" to title.trim().take(150),
          "category" to safeCat,
          "amountDh" to safeAmount,
          "paidAmountDh" to safePaid,
          "supplier" to supplier.trim().take(100),
          "dateLabel" to dateLabel.trim().take(30),
          "notes" to notes.trim().take(300),
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      ).await()
      return expenseId
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      throw e
    }
  }

  suspend fun updateExpense(expense: ExpenseRecord) {
    val uid = requireUserId()
    require(expense.id.isNotBlank()) { "Expense ID is required." }
    val docRef = db.collection("users")
      .document(uid)
      .collection("expenses")
      .document(expense.id)
    val safeCat = if (expense.category in EXPENSE_CATEGORIES) expense.category else "Divers"
    val safeAmount = expense.amountDh.coerceIn(0L, 10_000_000L)
    val safePaid = expense.effectivePaidDh.coerceIn(0L, safeAmount)
    try {
      docRef.update(
        mapOf(
          "title" to expense.title.trim().take(150),
          "category" to safeCat,
          "amountDh" to safeAmount,
          "paidAmountDh" to safePaid,
          "supplier" to expense.supplier.trim().take(100),
          "dateLabel" to expense.dateLabel.trim().take(30),
          "notes" to expense.notes.trim().take(300),
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      ).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  suspend fun deleteExpense(expenseId: String) {
    val uid = requireUserId()
    val docRef = db.collection("users")
      .document(uid)
      .collection("expenses")
      .document(expenseId)
    try {
      docRef.delete().await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  suspend fun seedStarterProjects() {
    val uid = requireUserId()
    val userRef = db.collection("users").document(uid)
    try {
      val existingSnap = userRef.collection("projects").whereEqualTo("userId", uid).get().await()
      val existingIds = existingSnap.documents.map { it.id }.toSet()
      val batch = db.batch()
      for (seedProj in InitialSyndicatData.projects) {
        if (seedProj.docId in existingIds) continue
        val ref = userRef.collection("projects").document(seedProj.docId)
        batch.set(
          ref,
          mapOf(
            "userId" to uid,
            "titleAr" to seedProj.titleAr,
            "titleFr" to seedProj.titleFr,
            "descriptionAr" to seedProj.descriptionAr,
            "descriptionFr" to seedProj.descriptionFr,
            "category" to seedProj.category,
            "priority" to seedProj.priority,
            "status" to seedProj.status,
            "targetBudgetDh" to seedProj.targetBudgetDh,
            "minCashToStartDh" to seedProj.minCashToStartDh,
            "allocatedAmountDh" to seedProj.allocatedAmountDh,
            "progressPercent" to seedProj.progressPercent,
            "contractor" to seedProj.contractor,
            "targetDateLabel" to seedProj.targetDateLabel,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
          ),
        )
      }
      batch.commit().await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, "users/$uid/projects")
      throw e
    }
  }

  suspend fun addProject(
    project: ProjectRecord,
    explicitId: String? = null,
  ): String {
    val uid = requireUserId()
    val projectId = explicitId?.takeIf { it.isNotBlank() }
      ?: "proj_${UUID.randomUUID().toString().replace("-", "").take(16)}"
    val docRef = db.collection("users")
      .document(uid)
      .collection("projects")
      .document(projectId)
    try {
      docRef.set(
        buildProjectUpdateMap(project) + mapOf(
          "userId" to uid,
          "createdAt" to FieldValue.serverTimestamp(),
        ),
      ).await()
      return projectId
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      throw e
    }
  }

  suspend fun updateProject(project: ProjectRecord) {
    val uid = requireUserId()
    require(project.id.isNotBlank()) { "Project ID is required." }
    val docRef = db.collection("users")
      .document(uid)
      .collection("projects")
      .document(project.id)
    try {
      docRef.update(buildProjectUpdateMap(project)).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  suspend fun deleteProject(projectId: String) {
    val uid = requireUserId()
    val docRef = db.collection("users")
      .document(uid)
      .collection("projects")
      .document(projectId)
    try {
      docRef.delete().await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  private fun buildProjectUpdateMap(project: ProjectRecord): Map<String, Any> {
    val safeCat = if (project.category in PROJECT_CATEGORIES) project.category else "Divers"
    val safeStatus = if (project.status in listOf("in_progress", "planned", "completed")) project.status else "planned"
    val cleanTitleAr = project.titleAr.trim().take(150)
    val cleanTitleFr = project.titleFr.trim().take(150)
    val finalTitleAr = if (cleanTitleAr.isBlank() && cleanTitleFr.isBlank()) "مشروع العمارة" else cleanTitleAr
    val safeBudget = project.targetBudgetDh.coerceIn(0L, 10_000_000L)
    val safeMinCash = project.minCashToStartDh.coerceIn(0L, 10_000_000L)
    val safeAllocated = project.allocatedAmountDh.coerceIn(0L, 10_000_000L)
    val safeProgress = when (safeStatus) {
      "completed" -> 100L
      else -> project.progressPercent.coerceIn(0L, 100L)
    }
    return mapOf(
      "titleAr" to finalTitleAr,
      "titleFr" to cleanTitleFr,
      "descriptionAr" to project.descriptionAr.trim().take(400),
      "descriptionFr" to project.descriptionFr.trim().take(400),
      "category" to safeCat,
      "priority" to project.priority.coerceIn(1L, 4L),
      "status" to safeStatus,
      "targetBudgetDh" to safeBudget,
      "minCashToStartDh" to safeMinCash,
      "allocatedAmountDh" to safeAllocated,
      "progressPercent" to safeProgress,
      "contractor" to project.contractor.trim().take(100),
      "targetDateLabel" to project.targetDateLabel.trim().take(30),
      "updatedAt" to FieldValue.serverTimestamp(),
    )
  }

  private fun sanitizeConfig(config: SyndicatConfig, uid: String): SyndicatConfig {
    val sanitizedActive = config.activeMonths
      .map { it.coerceIn(1L, 12L) }
      .distinct()
      .sorted()
      .ifEmpty { MonthColumn.DEFAULT_C49_ACTIVE_NUMBERS }
    val fCount = config.floorsCount.coerceIn(1L, 20L).toInt()
    val sanitizedFloorCounts = config.floorAptCounts
      .take(fCount)
      .map { it.coerceIn(0L, 120L) }
    val computedTotalAppts = if (sanitizedFloorCounts.size == fCount && sanitizedFloorCounts.sum() in 1L..120L) {
      sanitizedFloorCounts.sum()
    } else {
      config.apartmentsCount.coerceIn(1L, 120L)
    }

    return config.copy(
      userId = uid,
      language = if (config.language == "fr") "fr" else "ar",
      themeStyle = "airtable",
      themeMode = if (config.themeMode in listOf("system", "light", "dark")) config.themeMode else "system",
      fontScale = if (config.fontScale in listOf("small", "normal", "large")) config.fontScale else "normal",
      syndicatName = config.syndicatName.trim().ifBlank { "إدارة السانديك" }.take(100),
      residenceName = config.residenceName.trim().ifBlank { "الإقامة" }.take(120),
      address = config.address.trim().take(200),
      city = config.city.trim().take(100),
      treasurerName = config.treasurerName.trim().take(100),
      buildingTitle = config.buildingTitle.trim().ifBlank { config.residenceName.trim().ifBlank { "المساهمة الشهرية" } }.take(180),
      currency = config.currency.trim().ifBlank { "درهم" }.take(15),
      year = config.year.coerceIn(2020L, 2100L),
      openingBalanceDh = config.openingBalanceDh.coerceIn(-10_000_000L, 100_000_000L),
      dueDayOfMonth = config.dueDayOfMonth.coerceIn(1L, 31L),
      minReminderAmountDh = config.minReminderAmountDh.coerceIn(0L, 100_000L),
      whatsappTemplate = config.whatsappTemplate.trim().ifBlank { DEFAULT_WHATSAPP_TEMPLATE_AR }.take(1000),
      relanceHeaderNote = config.relanceHeaderNote.trim().ifBlank { DEFAULT_RELANCE_HEADER_AR }.take(300),
      floorsCount = fCount.toLong(),
      apartmentsCount = computedTotalAppts,
      floorAptCounts = if (sanitizedFloorCounts.size == fCount && sanitizedFloorCounts.sum() == computedTotalAppts) sanitizedFloorCounts else emptyList(),
      defaultMonthlyFee = config.defaultMonthlyFee.coerceIn(0L, 100_000L),
      cutoffMonth = config.cutoffMonth.coerceIn(1L, 12L),
      activeMonths = sanitizedActive,
      footerNote = config.footerNote.trim().ifBlank { "—" }.take(300),
      onboardingCompleted = true,
      seeded = true,
    )
  }

  private fun buildApartmentUpdateMap(apartment: ApartmentRecord): Map<String, Any> = mapOf(
    "number" to apartment.number.coerceIn(1L, 200L),
    "customNumberLabel" to apartment.customNumberLabel.trim().take(20),
    "floor" to apartment.floor.trim().ifBlank { "RDC" }.take(25),
    "status" to apartment.status.let { if (it in listOf("occupied", "empty", "exempt")) it else "occupied" },
    "ownerName" to apartment.ownerName.trim().take(80),
    "ownerNameAr" to apartment.ownerNameAr.trim().take(80),
    "phone" to apartment.phone.trim().take(30),
    "isTenant" to apartment.isTenant,
    "customMonthlyFee" to apartment.customMonthlyFee.coerceIn(-1L, 100_000L),
    "lastReminderAt" to apartment.lastReminderAt.coerceAtLeast(0L),
    "receiptCount" to apartment.receiptCount.coerceIn(0L, 100_000L),
    "jan" to apartment.jan.coerceIn(0L, 100_000L),
    "feb" to apartment.feb.coerceIn(0L, 100_000L),
    "mar" to apartment.mar.coerceIn(0L, 100_000L),
    "apr" to apartment.apr.coerceIn(0L, 100_000L),
    "may" to apartment.may.coerceIn(0L, 100_000L),
    "juin" to apartment.juin.coerceIn(0L, 100_000L),
    "juil" to apartment.juil.coerceIn(0L, 100_000L),
    "aout" to apartment.aout.coerceIn(0L, 100_000L),
    "sept" to apartment.sept.coerceIn(0L, 100_000L),
    "oct" to apartment.oct.coerceIn(0L, 100_000L),
    "nov" to apartment.nov.coerceIn(0L, 100_000L),
    "dec" to apartment.dec.coerceIn(0L, 100_000L),
    "updatedAt" to FieldValue.serverTimestamp(),
  )

  private fun buildConfigPayloadMap(cfg: SyndicatConfig): Map<String, Any> = mapOf(
    "language" to cfg.language,
    "digitsAr" to cfg.digitsAr,
    "themeStyle" to cfg.themeStyle,
    "themeMode" to cfg.themeMode,
    "fontScale" to cfg.fontScale,
    "syndicatName" to cfg.syndicatName,
    "residenceName" to cfg.residenceName,
    "address" to cfg.address,
    "city" to cfg.city,
    "treasurerName" to cfg.treasurerName,
    "buildingTitle" to cfg.buildingTitle,
    "currency" to cfg.currency,
    "year" to cfg.year,
    "openingBalanceDh" to cfg.openingBalanceDh,
    "dueDayOfMonth" to cfg.dueDayOfMonth,
    "minReminderAmountDh" to cfg.minReminderAmountDh,
    "whatsappTemplate" to cfg.whatsappTemplate,
    "relanceHeaderNote" to cfg.relanceHeaderNote,
    "includeRdc" to cfg.includeRdc,
    "floorsCount" to cfg.floorsCount,
    "apartmentsCount" to cfg.apartmentsCount,
    "floorAptCounts" to cfg.floorAptCounts,
    "defaultMonthlyFee" to cfg.defaultMonthlyFee,
    "cutoffMonth" to cfg.cutoffMonth,
    "activeMonths" to cfg.activeMonths,
    "feeJan" to cfg.feeJan.coerceIn(0L, 100_000L),
    "feeFeb" to cfg.feeFeb.coerceIn(0L, 100_000L),
    "feeMar" to cfg.feeMar.coerceIn(0L, 100_000L),
    "feeApr" to cfg.feeApr.coerceIn(0L, 100_000L),
    "feeMay" to cfg.feeMay.coerceIn(0L, 100_000L),
    "feeJuin" to cfg.feeJuin.coerceIn(0L, 100_000L),
    "feeJuil" to cfg.feeJuil.coerceIn(0L, 100_000L),
    "feeAout" to cfg.feeAout.coerceIn(0L, 100_000L),
    "feeSept" to cfg.feeSept.coerceIn(0L, 100_000L),
    "feeOct" to cfg.feeOct.coerceIn(0L, 100_000L),
    "feeNov" to cfg.feeNov.coerceIn(0L, 100_000L),
    "feeDec" to cfg.feeDec.coerceIn(0L, 100_000L),
    "footerNote" to cfg.footerNote,
    "onboardingCompleted" to cfg.onboardingCompleted,
    "seeded" to cfg.seeded,
    "updatedAt" to FieldValue.serverTimestamp(),
  )

  companion object {
    const val CONFIG_DOC_ID = "syndicat_config"
  }
}
