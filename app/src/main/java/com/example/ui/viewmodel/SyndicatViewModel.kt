package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ApartmentRecord
import com.example.data.model.ExpenseRecord
import com.example.data.model.MonthColumn
import com.example.data.model.ProjectCaisseReadiness
import com.example.data.model.ProjectRecord
import com.example.data.model.ProjectStatus
import com.example.data.model.SyndicatConfig
import com.example.data.model.evaluatePlannedProjectsReadiness
import com.example.data.model.sortProjectsByPriority
import com.example.data.repository.SyndicatRepository
import com.example.ui.i18n.formatNumber
import com.example.util.ParsedOwnerRow
import com.example.util.ParsedSyndicatBackup
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
  data object Loading : UiState<Nothing>
  data class Success<T>(val data: T) : UiState<T>
  data class Error(val message: String) : UiState<Nothing>
}

enum class StatusFilter {
  ALL,
  UP_TO_DATE,
  LATE,
  VACANT,
}

data class SnackbarUndoMessage(
  val message: String,
  val undoLabel: String? = null,
  val onUndo: (suspend () -> Unit)? = null,
)

data class SyndicatDashboardData(
  val config: SyndicatConfig,
  val apartments: List<ApartmentRecord>,
  val expenses: List<ExpenseRecord>,
  val projects: List<ProjectRecord> = emptyList(),
) {
  /** Somme de tous les paiements encaissés sur les mois actifs */
  val totalCaisseDh: Long
    get() = apartments.sumOf { it.totalForConfig(config) }

  /** Total des dépenses engagées */
  val totalExpensesDh: Long
    get() = expenses.sumOf { it.amountDh }

  /** Total des dépenses effectivement réglées aux prestataires */
  val totalExpensesPaidDh: Long
    get() = expenses.sumOf { it.effectivePaidDh }

  /** Reste à payer aux prestataires = dépenses − payé */
  val unpaidExpensesDh: Long
    get() = (totalExpensesDh - totalExpensesPaidDh).coerceAtLeast(0L)

  /** Solde comptable (recettes + solde initial − dépenses engagées) */
  val balanceDh: Long
    get() = totalCaisseDh + config.openingBalanceDh - totalExpensesDh

  /** En caisse maintenant = encaissé + solde initial − dépenses payées */
  val cashNowDh: Long
    get() = totalCaisseDh + config.openingBalanceDh - totalExpensesPaidDh

  /** Montant total restant à recouvrer sur les appartements en retard */
  val totalRemainingToCollectDh: Long
    get() = apartments.sumOf { it.remainingDh(config) }

  /** Total dû par tous les appartements cotisants à fin du mois M */
  val totalDueAtCutoffDh: Long
    get() = apartments.sumOf { it.dueAtCutoffDh(config) }

  /** Taux de recouvrement en % (0..100) */
  val collectionRatePercent: Int
    get() {
      val due = totalDueAtCutoffDh
      if (due <= 0L) return 100
      val collectedOnDue = (due - totalRemainingToCollectDh).coerceAtLeast(0L)
      return ((collectedOnDue.toDouble() / due.toDouble()) * 100.0).roundToInt().coerceIn(0, 100)
    }

  val occupiedCount: Int
    get() = apartments.count { !it.isVacantForConfig(config) }

  val upToDateCount: Int
    get() = apartments.count { it.isUpToDate(config) }

  val lateApartments: List<ApartmentRecord>
    get() = apartments
      .filter { !it.isVacantForConfig(config) && !it.isUpToDate(config) }
      .sortedByDescending { it.remainingDh(config) }

  val reminderQueueApartments: List<ApartmentRecord>
    get() = lateApartments.filter { it.remainingDh(config) >= config.minReminderAmountDh }

  val orderedFloors: List<String>
    get() {
      val configured = config.generatedFloors
      val actualExtras = apartments.map { it.floor }.distinct().filter { it !in configured }
      return configured + actualExtras
    }

  val cutoffMonthColumn: MonthColumn
    get() = MonthColumn.fromNumber(config.cutoffMonth.toInt())

  val sortedProjects: List<ProjectRecord>
    get() = sortProjectsByPriority(projects)

  val inProgressProjects: List<ProjectRecord>
    get() = sortedProjects.filter { it.parsedStatus == ProjectStatus.IN_PROGRESS }

  val plannedProjects: List<ProjectRecord>
    get() = sortedProjects.filter { it.parsedStatus == ProjectStatus.PLANNED }

  val completedProjects: List<ProjectRecord>
    get() = sortedProjects.filter { it.parsedStatus == ProjectStatus.COMPLETED }

  val expectedMonthlyIncomeDh: Long
    get() = apartments.sumOf { it.expectedFeeForMonth(cutoffMonthColumn, config) }

  val plannedReadinessList: List<ProjectCaisseReadiness>
    get() = evaluatePlannedProjectsReadiness(
      plannedProjects = plannedProjects,
      availableCashDh = cashNowDh,
      expectedMonthlyIncomeDh = expectedMonthlyIncomeDh,
    )

  val readyToStartProjectsCount: Int
    get() = plannedReadinessList.count { it.canStartNowDirect }
}

class SyndicatViewModel(
  private val repository: SyndicatRepository,
  private val currentUserId: String,
) : ViewModel() {

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _selectedFloor = MutableStateFlow<String?>(null)
  val selectedFloor: StateFlow<String?> = _selectedFloor.asStateFlow()

  private val _statusFilter = MutableStateFlow(StatusFilter.ALL)
  val statusFilter: StateFlow<StatusFilter> = _statusFilter.asStateFlow()

  private val _isSaving = MutableStateFlow(false)
  val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

  private val _snackbarEvents = MutableSharedFlow<SnackbarUndoMessage>(extraBufferCapacity = 4)
  val snackbarEvents = _snackbarEvents.asSharedFlow()

  val uiState: StateFlow<UiState<SyndicatDashboardData>> = combine(
    repository.observeConfig(currentUserId),
    repository.observeApartments(currentUserId),
    repository.observeExpenses(currentUserId),
    repository.observeProjects(currentUserId),
  ) { config, apartments, expenses, projects ->
    SyndicatDashboardData(
      config = config ?: SyndicatConfig(userId = currentUserId),
      apartments = apartments,
      expenses = expenses,
      projects = projects,
    )
  }
    .map<SyndicatDashboardData, UiState<SyndicatDashboardData>> { data ->
      UiState.Success(data)
    }
    .catch { error ->
      Log.w(TAG, "Erreur lors de l'observation Firestore", error)
      emit(UiState.Error(error.localizedMessage ?: "تعذر تحميل بيانات السانديك."))
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
      initialValue = UiState.Loading,
    )

  init {
    seedIfNeeded()
  }

  private fun seedIfNeeded() {
    viewModelScope.launch {
      try {
        repository.ensureSeeded(forceReset = false)
      } catch (e: Exception) {
        Log.e(TAG, "Erreur d'initialisation des données du syndicat", e)
        _snackbarEvents.tryEmit(
          SnackbarUndoMessage(e.localizedMessage ?: "خطأ في التهيئة الأولية.")
        )
      }
    }
  }

  fun updateSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun selectFloor(floor: String?) {
    _selectedFloor.value = floor
  }

  fun selectStatusFilter(filter: StatusFilter) {
    _statusFilter.value = filter
  }

  fun triggerUndoAction(action: suspend () -> Unit) {
    viewModelScope.launch {
      try {
        action()
      } catch (e: Exception) {
        _snackbarEvents.tryEmit(SnackbarUndoMessage(e.localizedMessage ?: "تعذر التراجع."))
      }
    }
  }

  fun saveApartment(
    apartment: ApartmentRecord,
    previousSnapshot: ApartmentRecord? = null,
    config: SyndicatConfig,
    onSuccess: () -> Unit = {},
  ) {
    viewModelScope.launch {
      _isSaving.value = true
      try {
        repository.updateApartment(apartment)
        val isAr = config.language != "fr"
        val total = formatDh(apartment.totalForConfig(config), config.currency, config.digitsAr)
        val msg = if (isAr) {
          "تم حفظ الشقة ${apartment.displayNumber} ($total)"
        } else {
          "Appartement ${apartment.displayNumber} mis à jour ($total)"
        }
        val undoLabel = if (isAr) "تراجع" else "Annuler"
        _snackbarEvents.tryEmit(
          SnackbarUndoMessage(
            message = msg,
            undoLabel = if (previousSnapshot != null) undoLabel else null,
            onUndo = previousSnapshot?.let { prev -> { repository.updateApartment(prev) } },
          )
        )
        onSuccess()
      } catch (e: Exception) {
        _snackbarEvents.tryEmit(SnackbarUndoMessage(e.localizedMessage ?: "خطأ في الحفظ."))
      } finally {
        _isSaving.value = false
      }
    }
  }

  fun markWhatsAppReminderSent(
    apartment: ApartmentRecord,
    config: SyndicatConfig,
    updatedPhone: String? = null,
    updatedTemplateToSave: String? = null,
  ) {
    viewModelScope.launch {
      try {
        val cleanPhone = updatedPhone?.trim()
        if (!cleanPhone.isNullOrBlank() && cleanPhone != apartment.phone.trim() && apartment.id.isNotBlank()) {
          repository.updateApartment(
            apartment.copy(
              phone = cleanPhone.take(30),
              lastReminderAt = System.currentTimeMillis(),
            )
          )
        } else {
          repository.markReminderSent(apartment.id)
        }

        if (!updatedTemplateToSave.isNullOrBlank() && updatedTemplateToSave.trim() != config.whatsappTemplate.trim()) {
          repository.updateWhatsAppTemplate(updatedTemplateToSave)
        }

        val isAr = config.language != "fr"
        val msg = if (isAr) {
          "تم تسجيل إرسال التذكير للشقة ${apartment.displayNumber}"
        } else {
          "Relance enregistrée pour l'appartement ${apartment.displayNumber}"
        }
        _snackbarEvents.tryEmit(SnackbarUndoMessage(msg))
      } catch (e: Exception) {
        Log.w(TAG, "Erreur lors de l'enregistrement de la relance WhatsApp", e)
      }
    }
  }

  fun showToastMessage(message: String) {
    _snackbarEvents.tryEmit(SnackbarUndoMessage(message))
  }

  suspend fun nextReceiptNumber(apartment: ApartmentRecord): Long {
    return repository.incrementReceiptCount(apartment)
  }

  fun addExpense(
    title: String,
    category: String,
    amountDh: Long,
    paidAmountDh: Long,
    supplier: String,
    dateLabel: String,
    notes: String,
    config: SyndicatConfig,
    onSuccess: () -> Unit = {},
  ) {
    viewModelScope.launch {
      _isSaving.value = true
      try {
        val createdId = repository.addExpense(
          title = title,
          category = category,
          amountDh = amountDh,
          paidAmountDh = paidAmountDh,
          supplier = supplier,
          dateLabel = dateLabel,
          notes = notes,
        )
        val isAr = config.language != "fr"
        val amtText = formatDh(amountDh, config.currency, config.digitsAr)
        val msg = if (isAr) "تمت إضافة المصروف ($amtText)" else "Dépense ajoutée ($amtText)"
        val undoLabel = if (isAr) "تراجع" else "Annuler"
        _snackbarEvents.tryEmit(
          SnackbarUndoMessage(
            message = msg,
            undoLabel = undoLabel,
            onUndo = { repository.deleteExpense(createdId) },
          )
        )
        onSuccess()
      } catch (e: Exception) {
        _snackbarEvents.tryEmit(SnackbarUndoMessage(e.localizedMessage ?: "تعذر إضافة المصروف."))
      } finally {
        _isSaving.value = false
      }
    }
  }

  fun updateExpense(
    expense: ExpenseRecord,
    config: SyndicatConfig,
    onSuccess: () -> Unit = {},
  ) {
    viewModelScope.launch {
      _isSaving.value = true
      try {
        repository.updateExpense(expense)
        val isAr = config.language != "fr"
        val amtText = formatDh(expense.amountDh, config.currency, config.digitsAr)
        val msg = if (isAr) "تم تعديل المصروف ($amtText)" else "Dépense modifiée ($amtText)"
        _snackbarEvents.tryEmit(SnackbarUndoMessage(msg))
        onSuccess()
      } catch (e: Exception) {
        _snackbarEvents.tryEmit(SnackbarUndoMessage(e.localizedMessage ?: "تعذر تعديل المصروف."))
      } finally {
        _isSaving.value = false
      }
    }
  }

  fun deleteExpense(expense: ExpenseRecord, config: SyndicatConfig) {
    viewModelScope.launch {
      try {
        repository.deleteExpense(expense.id)
        val isAr = config.language != "fr"
        val msg = if (isAr) "تم حذف المصروف: ${expense.title}" else "Dépense supprimée : ${expense.title}"
        val undoLabel = if (isAr) "تراجع" else "Annuler"
        _snackbarEvents.tryEmit(
          SnackbarUndoMessage(
            message = msg,
            undoLabel = undoLabel,
            onUndo = {
              repository.addExpense(
                title = expense.title,
                category = expense.category,
                amountDh = expense.amountDh,
                paidAmountDh = expense.effectivePaidDh,
                supplier = expense.supplier,
                dateLabel = expense.dateLabel,
                notes = expense.notes,
                explicitId = expense.id,
              )
            },
          )
        )
      } catch (e: Exception) {
        _snackbarEvents.tryEmit(SnackbarUndoMessage(e.localizedMessage ?: "تعذر الحذف."))
      }
    }
  }

  fun addProject(
    project: ProjectRecord,
    config: SyndicatConfig,
    onSuccess: () -> Unit = {},
  ) {
    viewModelScope.launch {
      _isSaving.value = true
      try {
        val createdId = repository.addProject(project)
        val isAr = config.language != "fr"
        val title = project.displayTitle(isAr)
        val msg = if (isAr) "تمت إضافة المشروع: $title" else "Projet ajouté : $title"
        val undoLabel = if (isAr) "تراجع" else "Annuler"
        _snackbarEvents.tryEmit(
          SnackbarUndoMessage(
            message = msg,
            undoLabel = undoLabel,
            onUndo = { repository.deleteProject(createdId) },
          )
        )
        onSuccess()
      } catch (e: Exception) {
        _snackbarEvents.tryEmit(SnackbarUndoMessage(e.localizedMessage ?: "تعذر إضافة المشروع."))
      } finally {
        _isSaving.value = false
      }
    }
  }

  fun updateProject(
    project: ProjectRecord,
    previousSnapshot: ProjectRecord? = null,
    config: SyndicatConfig,
    onSuccess: () -> Unit = {},
  ) {
    viewModelScope.launch {
      _isSaving.value = true
      try {
        repository.updateProject(project)
        val isAr = config.language != "fr"
        val title = project.displayTitle(isAr)
        val msg = if (isAr) {
          "تم تحيين المشروع: $title (${project.clampedProgressPercent}%)"
        } else {
          "Projet mis à jour : $title (${project.clampedProgressPercent}%)"
        }
        val undoLabel = if (isAr) "تراجع" else "Annuler"
        _snackbarEvents.tryEmit(
          SnackbarUndoMessage(
            message = msg,
            undoLabel = if (previousSnapshot != null) undoLabel else null,
            onUndo = previousSnapshot?.let { prev -> { repository.updateProject(prev) } },
          )
        )
        onSuccess()
      } catch (e: Exception) {
        _snackbarEvents.tryEmit(SnackbarUndoMessage(e.localizedMessage ?: "تعذر تحيين المشروع."))
      } finally {
        _isSaving.value = false
      }
    }
  }

  fun deleteProject(project: ProjectRecord, config: SyndicatConfig) {
    viewModelScope.launch {
      try {
        repository.deleteProject(project.id)
        val isAr = config.language != "fr"
        val title = project.displayTitle(isAr)
        val msg = if (isAr) "تم حذف المشروع: $title" else "Projet supprimé : $title"
        val undoLabel = if (isAr) "تراجع" else "Annuler"
        _snackbarEvents.tryEmit(
          SnackbarUndoMessage(
            message = msg,
            undoLabel = undoLabel,
            onUndo = { repository.addProject(project = project, explicitId = project.id) },
          )
        )
      } catch (e: Exception) {
        _snackbarEvents.tryEmit(SnackbarUndoMessage(e.localizedMessage ?: "تعذر حذف المشروع."))
      }
    }
  }

  fun seedStarterProjects(config: SyndicatConfig) {
    viewModelScope.launch {
      _isSaving.value = true
      try {
        repository.seedStarterProjects()
        val isAr = config.language != "fr"
        val msg = if (isAr) {
          "تم تحميل المشاريع النموذجية للعمارة."
        } else {
          "Projets types de l'immeuble chargés."
        }
        _snackbarEvents.tryEmit(SnackbarUndoMessage(msg))
      } catch (e: Exception) {
        _snackbarEvents.tryEmit(SnackbarUndoMessage(e.localizedMessage ?: "تعذر تحميل المشاريع."))
      } finally {
        _isSaving.value = false
      }
    }
  }

  fun updateLanguageInstant(languageCode: String) {
    viewModelScope.launch {
      try {
        repository.updateLanguage(languageCode)
      } catch (e: Exception) {
        Log.w(TAG, "Échec de synchronisation de la langue", e)
      }
    }
  }

  fun updateDisplayPrefsInstant(
    digitsAr: Boolean? = null,
    themeStyle: String? = null,
    themeMode: String? = null,
    fontScale: String? = null,
  ) {
    viewModelScope.launch {
      try {
        repository.updateDisplayPrefs(
          digitsAr = digitsAr,
          themeStyle = themeStyle,
          themeMode = themeMode,
          fontScale = fontScale,
        )
      } catch (e: Exception) {
        Log.w(TAG, "Échec de mise à jour des préférences d'affichage", e)
      }
    }
  }

  fun saveFullConfiguration(
    updatedConfig: SyndicatConfig,
    resetBlankBuilding: Boolean = false,
    explicitApartments: List<ApartmentRecord>? = null,
    onSuccess: () -> Unit = {},
  ) {
    viewModelScope.launch {
      _isSaving.value = true
      try {
        repository.saveFullConfiguration(
          config = updatedConfig,
          resetBlankBuilding = resetBlankBuilding,
          explicitApartments = explicitApartments,
        )
        _selectedFloor.value = null
        val isAr = updatedConfig.language != "fr"
        val msg = when {
          resetBlankBuilding && isAr -> "تمت تهيئة العمارة الجديدة بنجاح (${updatedConfig.apartmentsCount} شقة)."
          resetBlankBuilding -> "Immeuble vierge initialisé (${updatedConfig.apartmentsCount} appartements)."
          isAr -> "تم حفظ إعدادات السانديك بنجاح."
          else -> "Configuration enregistrée avec succès."
        }
        _snackbarEvents.tryEmit(SnackbarUndoMessage(msg))
        onSuccess()
      } catch (e: Exception) {
        _snackbarEvents.tryEmit(SnackbarUndoMessage(e.localizedMessage ?: "تعذر حفظ الإعدادات."))
      } finally {
        _isSaving.value = false
      }
    }
  }

  fun restoreBackupData(backup: ParsedSyndicatBackup, isAr: Boolean) {
    viewModelScope.launch {
      _isSaving.value = true
      try {
        repository.restoreFromBackup(backup)
        _selectedFloor.value = null
        val msg = if (isAr) {
          "تم استرجاع النسخة الاحتياطية بنجاح (${backup.apartments.size} شقة)."
        } else {
          "Sauvegarde restaurée avec succès (${backup.apartments.size} appartements)."
        }
        _snackbarEvents.tryEmit(SnackbarUndoMessage(msg))
      } catch (e: Exception) {
        _snackbarEvents.tryEmit(SnackbarUndoMessage(e.localizedMessage ?: "فشل استرجاع النسخة الاحتياطية."))
      } finally {
        _isSaving.value = false
      }
    }
  }

  fun importBulkOwners(rows: List<ParsedOwnerRow>, isAr: Boolean) {
    viewModelScope.launch {
      _isSaving.value = true
      try {
        repository.importOwnerRows(rows)
        val msg = if (isAr) {
          "تم استيراد بيانات ${rows.size} شقق بنجاح."
        } else {
          "${rows.size} propriétaires importés avec succès."
        }
        _snackbarEvents.tryEmit(SnackbarUndoMessage(msg))
      } catch (e: Exception) {
        _snackbarEvents.tryEmit(SnackbarUndoMessage(e.localizedMessage ?: "تعذر استيراد اللائحة."))
      } finally {
        _isSaving.value = false
      }
    }
  }

  fun resetToBaselinePdf(preserveLanguage: String = "ar") {
    viewModelScope.launch {
      _isSaving.value = true
      try {
        repository.ensureSeeded(forceReset = true, preserveLanguage = preserveLanguage)
        _selectedFloor.value = null
        val msg = if (preserveLanguage == "fr") {
          "Données C49 restaurées (Caisse ${formatDh(8200L, "DH", false)})."
        } else {
          "تمت استعادة بيانات C49 النموذجية (الصندوق ${formatDh(8200L, "درهم", false)})."
        }
        _snackbarEvents.tryEmit(SnackbarUndoMessage(msg))
      } catch (e: Exception) {
        _snackbarEvents.tryEmit(SnackbarUndoMessage(e.localizedMessage ?: "تعذر الاسترجاع."))
      } finally {
        _isSaving.value = false
      }
    }
  }

  companion object {
    private const val TAG = "SyndicatVM"
    private const val STOP_TIMEOUT_MILLIS = 5000L
  }
}

fun formatDh(
  amount: Long,
  currency: String = "درهم",
  digitsAr: Boolean = false,
  explicitPlus: Boolean = false,
): String {
  val localizedNum = formatNumber(
    amount = amount,
    digitsAr = digitsAr,
    explicitPlus = explicitPlus,
  )
  val cleanCurrency = currency.trim()
  return if (cleanCurrency.isEmpty()) localizedNum else "$localizedNum $cleanCurrency"
}
