package com.example.data.repository

import com.example.data.model.ApartmentRecord
import com.example.data.model.ExpenseRecord
import com.example.data.model.PaymentRecord
import com.example.data.model.ProjectRecord
import com.example.data.model.SyndicatConfig
import com.example.util.ParsedOwnerRow
import com.example.util.ParsedSyndicatBackup
import kotlinx.coroutines.flow.Flow

const val LOCAL_DEVICE_USER_ID = "local_device"

/**
 * Unified data source contract shared between:
 * - [com.example.data.local.LocalSyndicatRepository] (account-free on-device phone storage)
 * - [SyndicatRepository] (optional Google Sign-In cloud synchronization via Firestore)
 */
interface SyndicatDataSource {
  fun observeConfig(userId: String): Flow<SyndicatConfig?>
  fun observeApartments(userId: String): Flow<List<ApartmentRecord>>
  fun observePayments(userId: String): Flow<List<PaymentRecord>>
  fun observeExpenses(userId: String): Flow<List<ExpenseRecord>>
  fun observeProjects(userId: String): Flow<List<ProjectRecord>>

  suspend fun ensureSeeded(forceReset: Boolean = false, preserveLanguage: String? = null)

  suspend fun saveFullConfiguration(
    config: SyndicatConfig,
    resetBlankBuilding: Boolean = false,
    explicitApartments: List<ApartmentRecord>? = null,
  )

  suspend fun restoreFromBackup(backup: ParsedSyndicatBackup)
  suspend fun importOwnerRows(rows: List<ParsedOwnerRow>)

  suspend fun updateDisplayPrefs(
    digitsAr: Boolean? = null,
    themeStyle: String? = null,
    themeMode: String? = null,
    fontScale: String? = null,
  )

  suspend fun updateLanguage(languageCode: String)
  suspend fun updateWhatsAppTemplate(template: String)

  suspend fun updateApartment(apartment: ApartmentRecord)
  suspend fun markReminderSent(apartmentId: String)
  suspend fun incrementReceiptCount(apartment: ApartmentRecord): Long

  suspend fun addPayment(
    payment: PaymentRecord,
    explicitId: String? = null,
  ): String

  suspend fun deletePayment(paymentId: String)

  suspend fun addExpense(
    title: String,
    category: String,
    amountDh: Long,
    paidAmountDh: Long = -1L,
    supplier: String = "",
    dateLabel: String,
    notes: String,
    explicitId: String? = null,
  ): String

  suspend fun updateExpense(expense: ExpenseRecord)
  suspend fun deleteExpense(expenseId: String)

  suspend fun seedStarterProjects()
  suspend fun addProject(project: ProjectRecord, explicitId: String? = null): String
  suspend fun updateProject(project: ProjectRecord)
  suspend fun deleteProject(projectId: String)
}
