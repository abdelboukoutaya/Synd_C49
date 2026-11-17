package com.example.data.local

import android.content.Context
import com.example.data.model.ApartmentRecord
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class SpecialCallPayment(
  val apartmentNumber: Long,
  val paidAmountDh: Long,
  val paymentDate: String,
  val notes: String = "",
)

data class SpecialCallRecord(
  val id: String,
  val titleFr: String,
  val titleAr: String,
  val projectId: String = "",
  val amountPerAptDh: Long,
  val createdDate: String,
  val dueDateLabel: String = "",
  val payments: Map<Long, SpecialCallPayment> = emptyMap(),
) {
  fun displayTitle(isAr: Boolean): String {
    val primary = if (isAr) titleAr.trim() else titleFr.trim()
    val fallback = if (isAr) titleFr.trim() else titleAr.trim()
    return primary.ifBlank { fallback }.ifBlank { id }
  }

  fun eligibleApartments(apartments: List<ApartmentRecord>): List<ApartmentRecord> {
    return apartments.filter { it.status == "occupied" }
  }

  fun expectedTotalDh(apartments: List<ApartmentRecord>): Long {
    val count = eligibleApartments(apartments).size.toLong()
    return amountPerAptDh.coerceAtLeast(0L) * count
  }

  fun collectedTotalDh(apartments: List<ApartmentRecord>): Long {
    val eligibleNums = eligibleApartments(apartments).map { it.number }.toSet()
    return payments.entries
      .filter { it.key in eligibleNums || eligibleNums.isEmpty() }
      .sumOf { it.value.paidAmountDh.coerceAtLeast(0L) }
  }

  fun paidApartmentsCount(apartments: List<ApartmentRecord>): Int {
    val eligibleNums = eligibleApartments(apartments).map { it.number }.toSet()
    return payments.entries.count { (num, pay) ->
      (eligibleNums.isEmpty() || num in eligibleNums) && pay.paidAmountDh >= amountPerAptDh.coerceAtLeast(1L)
    }
  }

  fun isApartmentPaid(apartmentNumber: Long): Boolean {
    val p = payments[apartmentNumber] ?: return false
    return p.paidAmountDh >= amountPerAptDh.coerceAtLeast(1L)
  }
}

enum class AuditActionType(val code: String) {
  PAYMENT("payment"),
  PAYMENT_DELETED("payment_deleted"),
  EXPENSE_ADDED("expense_added"),
  EXPENSE_UPDATED("expense_updated"),
  EXPENSE_DELETED("expense_deleted"),
  PROJECT_ADDED("project_added"),
  PROJECT_UPDATED("project_updated"),
  PROJECT_DELETED("project_deleted"),
  SPECIAL_CALL("special_call"),
  BACKUP_RESTORE("backup_restore"),
  RESET("reset"),
  CONFIG_SAVED("config_saved");

  companion object {
    fun fromCode(raw: String?): AuditActionType {
      return entries.firstOrNull { it.code.equals(raw, ignoreCase = true) } ?: CONFIG_SAVED
    }
  }
}

data class AuditLogEntry(
  val id: String,
  val timestampMs: Long,
  val actionType: AuditActionType,
  val summaryFr: String,
  val summaryAr: String,
  val amountDeltaDh: Long = 0L,
) {
  fun displaySummary(isAr: Boolean): String {
    val p = if (isAr) summaryAr.trim() else summaryFr.trim()
    val f = if (isAr) summaryFr.trim() else summaryAr.trim()
    return p.ifBlank { f }
  }

  fun formattedDateTime(): String {
    return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE).format(Date(timestampMs))
  }
}

object SyndicatExtrasCodec {
  fun specialCallsToJsonArray(calls: List<SpecialCallRecord>): JSONArray {
    val arr = JSONArray()
    calls.forEach { call ->
      val payObj = JSONObject()
      call.payments.forEach { (aptNum, pay) ->
        payObj.put(
          aptNum.toString(),
          JSONObject().apply {
            put("apartmentNumber", pay.apartmentNumber)
            put("paidAmountDh", pay.paidAmountDh)
            put("paymentDate", pay.paymentDate)
            put("notes", pay.notes)
          },
        )
      }
      arr.put(
        JSONObject().apply {
          put("id", call.id)
          put("titleFr", call.titleFr)
          put("titleAr", call.titleAr)
          put("projectId", call.projectId)
          put("amountPerAptDh", call.amountPerAptDh)
          put("createdDate", call.createdDate)
          put("dueDateLabel", call.dueDateLabel)
          put("payments", payObj)
        },
      )
    }
    return arr
  }

  fun parseSpecialCallsJsonArray(arr: JSONArray?): List<SpecialCallRecord> {
    if (arr == null) return emptyList()
    val out = mutableListOf<SpecialCallRecord>()
    for (i in 0 until arr.length()) {
      val obj = arr.optJSONObject(i) ?: continue
      val payMap = linkedMapOf<Long, SpecialCallPayment>()
      val payObj = obj.optJSONObject("payments")
      if (payObj != null) {
        val keys = payObj.keys()
        while (keys.hasNext()) {
          val k = keys.next()
          val aptNum = k.toLongOrNull() ?: continue
          val pJson = payObj.optJSONObject(k) ?: continue
          val paid = pJson.optLong("paidAmountDh", 0L).coerceAtLeast(0L)
          if (paid > 0L) {
            payMap[aptNum] = SpecialCallPayment(
              apartmentNumber = aptNum,
              paidAmountDh = paid,
              paymentDate = pJson.optString("paymentDate", ""),
              notes = pJson.optString("notes", ""),
            )
          }
        }
      }
      val id = obj.optString("id", "sc_$i").ifBlank { "sc_$i" }
      out.add(
        SpecialCallRecord(
          id = id,
          titleFr = obj.optString("titleFr", "").take(150),
          titleAr = obj.optString("titleAr", "").take(150),
          projectId = obj.optString("projectId", "").take(80),
          amountPerAptDh = obj.optLong("amountPerAptDh", 0L).coerceIn(0L, 1_000_000L),
          createdDate = obj.optString("createdDate", "").take(30),
          dueDateLabel = obj.optString("dueDateLabel", "").take(30),
          payments = payMap,
        ),
      )
    }
    return out
  }

  fun auditLogsToJsonArray(logs: List<AuditLogEntry>): JSONArray {
    val arr = JSONArray()
    logs.forEach { entry ->
      arr.put(
        JSONObject().apply {
          put("id", entry.id)
          put("timestampMs", entry.timestampMs)
          put("actionType", entry.actionType.code)
          put("summaryFr", entry.summaryFr)
          put("summaryAr", entry.summaryAr)
          put("amountDeltaDh", entry.amountDeltaDh)
        },
      )
    }
    return arr
  }

  fun parseAuditLogsJsonArray(arr: JSONArray?): List<AuditLogEntry> {
    if (arr == null) return emptyList()
    val out = mutableListOf<AuditLogEntry>()
    for (i in 0 until arr.length()) {
      val obj = arr.optJSONObject(i) ?: continue
      out.add(
        AuditLogEntry(
          id = obj.optString("id", "audit_$i").ifBlank { "audit_$i" },
          timestampMs = obj.optLong("timestampMs", 0L),
          actionType = AuditActionType.fromCode(obj.optString("actionType", "config_saved")),
          summaryFr = obj.optString("summaryFr", "").take(300),
          summaryAr = obj.optString("summaryAr", "").take(300),
          amountDeltaDh = obj.optLong("amountDeltaDh", 0L),
        ),
      )
    }
    return out.sortedByDescending { it.timestampMs }
  }
}

class SyndicatExtrasStore private constructor(appContext: Context) {

  private val baseDir = File(appContext.filesDir, "local_store").apply { mkdirs() }
  private val globalPrefsFile = File(baseDir, "syndicat_buildings_meta.json")
  private val lock = Any()

  private var _activeBuildingId: MutableStateFlow<String>
  val activeBuildingId: StateFlow<String>

  private val _buildings: MutableStateFlow<List<BuildingProfile>>
  val buildings: StateFlow<List<BuildingProfile>>

  private val _roleMode: MutableStateFlow<SyndicatRoleMode>
  val roleMode: StateFlow<SyndicatRoleMode>

  init {
    val (active, list, role) = loadBuildingsMetaFromDisk()
    _activeBuildingId = MutableStateFlow(active)
    activeBuildingId = _activeBuildingId.asStateFlow()
    _buildings = MutableStateFlow(list)
    buildings = _buildings.asStateFlow()
    _roleMode = MutableStateFlow(role)
    roleMode = _roleMode.asStateFlow()
  }

  private fun extrasDirFor(buildingId: String = _activeBuildingId.value): File {
    val clean = buildingId.trim().replace(Regex("[^a-zA-Z0-9_-]"), "_").ifBlank { "default" }
    return if (clean == "default") {
      baseDir
    } else {
      File(baseDir, "extras_$clean").apply { mkdirs() }
    }
  }

  private fun callsFile() = File(extrasDirFor(), "syndicat_special_calls.json")
  private fun auditFile() = File(extrasDirFor(), "syndicat_audit_log.json")
  private fun recurringFile() = File(extrasDirFor(), "syndicat_recurring_templates.json")
  private fun vendorsFile() = File(extrasDirFor(), "syndicat_vendors.json")
  private fun incidentsFile() = File(extrasDirFor(), "syndicat_incidents.json")
  private fun agMinutesFile() = File(extrasDirFor(), "syndicat_ag_minutes.json")

  private val _specialCalls = MutableStateFlow<List<SpecialCallRecord>>(loadSpecialCallsFromDisk())
  val specialCalls: StateFlow<List<SpecialCallRecord>> = _specialCalls.asStateFlow()

  private val _auditLogs = MutableStateFlow<List<AuditLogEntry>>(loadAuditLogsFromDisk())
  val auditLogs: StateFlow<List<AuditLogEntry>> = _auditLogs.asStateFlow()

  private val _recurringTemplates = MutableStateFlow<List<RecurringExpenseTemplate>>(loadRecurringFromDisk())
  val recurringTemplates: StateFlow<List<RecurringExpenseTemplate>> = _recurringTemplates.asStateFlow()

  private val _vendors = MutableStateFlow<List<VendorRecord>>(loadVendorsFromDisk())
  val vendors: StateFlow<List<VendorRecord>> = _vendors.asStateFlow()

  private val _incidents = MutableStateFlow<List<IncidentRecord>>(loadIncidentsFromDisk())
  val incidents: StateFlow<List<IncidentRecord>> = _incidents.asStateFlow()

  private val _agMinutes = MutableStateFlow(loadAgMinutesFromDisk())
  val agMinutes: StateFlow<AgMinutesDraft> = _agMinutes.asStateFlow()

  private fun loadBuildingsMetaFromDisk(): Triple<String, List<BuildingProfile>, SyndicatRoleMode> {
    return try {
      if (!globalPrefsFile.exists()) {
        return Triple("default", listOf(BuildingProfile("default", "Résidence Principale")), SyndicatRoleMode.ADMIN)
      }
      val raw = globalPrefsFile.readText(Charsets.UTF_8)
      val obj = JSONObject(raw)
      val (active, list) = SyndicatAdvancedCodec.parseBuildingsJsonObject(obj)
      val role = SyndicatRoleMode.fromCode(obj.optString("roleMode", "admin"))
      Triple(active, list, role)
    } catch (_: Exception) {
      Triple("default", listOf(BuildingProfile("default", "Résidence Principale")), SyndicatRoleMode.ADMIN)
    }
  }

  private fun persistBuildingsMeta(activeId: String, list: List<BuildingProfile>, role: SyndicatRoleMode) {
    try {
      val obj = SyndicatAdvancedCodec.buildingsToJsonObject(activeId, list).apply {
        put("roleMode", role.code)
      }
      globalPrefsFile.writeText(obj.toString(2), Charsets.UTF_8)
    } catch (_: Exception) {
    }
  }

  private fun loadSpecialCallsFromDisk(): List<SpecialCallRecord> = readJsonArraySafe(callsFile()) {
    SyndicatExtrasCodec.parseSpecialCallsJsonArray(it)
  }

  private fun loadAuditLogsFromDisk(): List<AuditLogEntry> = readJsonArraySafe(auditFile()) {
    SyndicatExtrasCodec.parseAuditLogsJsonArray(it)
  }

  private fun loadRecurringFromDisk(): List<RecurringExpenseTemplate> = readJsonArraySafe(recurringFile()) {
    SyndicatAdvancedCodec.parseRecurringJsonArray(it)
  }

  private fun loadVendorsFromDisk(): List<VendorRecord> = readJsonArraySafe(vendorsFile()) {
    SyndicatAdvancedCodec.parseVendorsJsonArray(it)
  }

  private fun loadIncidentsFromDisk(): List<IncidentRecord> = readJsonArraySafe(incidentsFile()) {
    SyndicatAdvancedCodec.parseIncidentsJsonArray(it)
  }

  private fun loadAgMinutesFromDisk(): AgMinutesDraft {
    return try {
      val f = agMinutesFile()
      if (!f.exists()) return AgMinutesDraft()
      val raw = f.readText(Charsets.UTF_8)
      if (raw.isBlank()) return AgMinutesDraft()
      SyndicatAdvancedCodec.parseAgMinutesJsonObject(JSONObject(raw))
    } catch (_: Exception) {
      AgMinutesDraft()
    }
  }

  private inline fun <T> readJsonArraySafe(file: File, parser: (JSONArray) -> List<T>): List<T> {
    return try {
      if (!file.exists()) return emptyList()
      val raw = file.readText(Charsets.UTF_8)
      if (raw.isBlank()) return emptyList()
      parser(JSONArray(raw))
    } catch (_: Exception) {
      emptyList()
    }
  }

  private fun writeTextSafe(file: File, content: String) {
    try {
      file.parentFile?.mkdirs()
      file.writeText(content, Charsets.UTF_8)
    } catch (_: Exception) {
    }
  }

  private fun reloadBuildingScopedFlows() {
    _specialCalls.value = loadSpecialCallsFromDisk()
    _auditLogs.value = loadAuditLogsFromDisk()
    _recurringTemplates.value = loadRecurringFromDisk()
    _vendors.value = loadVendorsFromDisk()
    _incidents.value = loadIncidentsFromDisk()
    _agMinutes.value = loadAgMinutesFromDisk()
  }

  // --- Multi-immeubles & rôle Conseil Syndical ---

  fun selectBuilding(buildingId: String) {
    synchronized(lock) {
      val valid = _buildings.value.firstOrNull { it.id == buildingId }?.id ?: return
      if (_activeBuildingId.value == valid) return
      _activeBuildingId.value = valid
      persistBuildingsMeta(valid, _buildings.value, _roleMode.value)
      reloadBuildingScopedFlows()
    }
  }

  fun addBuilding(name: String, city: String = ""): BuildingProfile {
    val profile = BuildingProfile(
      id = "bld_${UUID.randomUUID().toString().take(8)}",
      name = name.trim().ifBlank { "Nouvel Immeuble" }.take(100),
      city = city.trim().take(60),
    )
    synchronized(lock) {
      val next = _buildings.value + profile
      _buildings.value = next
      _activeBuildingId.value = profile.id
      persistBuildingsMeta(profile.id, next, _roleMode.value)
      reloadBuildingScopedFlows()
    }
    return profile
  }

  fun renameActiveBuilding(newName: String, newCity: String = "") {
    val cleanName = newName.trim().take(100)
    if (cleanName.isBlank()) return
    synchronized(lock) {
      val active = _activeBuildingId.value
      val next = _buildings.value.map { b ->
        if (b.id == active) b.copy(name = cleanName, city = newCity.trim().take(60)) else b
      }
      _buildings.value = next
      persistBuildingsMeta(active, next, _roleMode.value)
    }
  }

  fun deleteBuilding(buildingId: String) {
    if (buildingId == "default") return
    synchronized(lock) {
      val next = _buildings.value.filterNot { it.id == buildingId }
      val nextActive = if (_activeBuildingId.value == buildingId) "default" else _activeBuildingId.value
      _buildings.value = next
      _activeBuildingId.value = nextActive
      persistBuildingsMeta(nextActive, next, _roleMode.value)
      File(baseDir, "extras_$buildingId").deleteRecursively()
      reloadBuildingScopedFlows()
    }
  }

  fun setRoleMode(mode: SyndicatRoleMode) {
    synchronized(lock) {
      _roleMode.value = mode
      persistBuildingsMeta(_activeBuildingId.value, _buildings.value, mode)
    }
  }

  // --- Abonnements / Dépenses récurrentes ---

  fun seedStarterRecurringTemplates(isAr: Boolean) {
    synchronized(lock) {
      if (_recurringTemplates.value.isNotEmpty()) return
      val starters = SyndicatAdvancedCodec.starterRecurringTemplates(isAr)
      _recurringTemplates.value = starters
      writeTextSafe(recurringFile(), SyndicatAdvancedCodec.recurringToJsonArray(starters).toString(2))
    }
  }

  fun upsertRecurringTemplate(template: RecurringExpenseTemplate) {
    val item = template.copy(
      id = template.id.ifBlank { "rec_${UUID.randomUUID().toString().take(8)}" },
      title = template.title.trim().take(120),
      supplier = template.supplier.trim().take(100),
      amountDh = template.amountDh.coerceIn(0L, 1_000_000L),
      dayOfMonth = template.dayOfMonth.coerceIn(1, 28),
    )
    synchronized(lock) {
      val exists = _recurringTemplates.value.any { it.id == item.id }
      val next = if (exists) {
        _recurringTemplates.value.map { if (it.id == item.id) item else it }
      } else {
        listOf(item) + _recurringTemplates.value
      }
      _recurringTemplates.value = next
      writeTextSafe(recurringFile(), SyndicatAdvancedCodec.recurringToJsonArray(next).toString(2))
    }
  }

  fun deleteRecurringTemplate(templateId: String) {
    synchronized(lock) {
      val next = _recurringTemplates.value.filterNot { it.id == templateId }
      _recurringTemplates.value = next
      writeTextSafe(recurringFile(), SyndicatAdvancedCodec.recurringToJsonArray(next).toString(2))
    }
  }

  fun markRecurringTemplatesApplied(templateIds: Set<String>, monthKey: String) {
    if (templateIds.isEmpty()) return
    synchronized(lock) {
      val next = _recurringTemplates.value.map { t ->
        if (t.id in templateIds) t.copy(lastAppliedMonthKey = monthKey) else t
      }
      _recurringTemplates.value = next
      writeTextSafe(recurringFile(), SyndicatAdvancedCodec.recurringToJsonArray(next).toString(2))
    }
  }

  // --- Carnet des Prestataires & Contrats ---

  fun upsertVendor(vendor: VendorRecord) {
    val item = vendor.copy(
      id = vendor.id.ifBlank { "vnd_${UUID.randomUUID().toString().take(8)}" },
      name = vendor.name.trim().take(100),
      specialty = vendor.specialty.trim().take(80),
      phone = vendor.phone.trim().take(30),
      monthlyFeeDh = vendor.monthlyFeeDh.coerceIn(0L, 1_000_000L),
      contractEndDate = vendor.contractEndDate.trim().take(30),
      notes = vendor.notes.trim().take(250),
    )
    synchronized(lock) {
      val exists = _vendors.value.any { it.id == item.id }
      val next = if (exists) {
        _vendors.value.map { if (it.id == item.id) item else it }
      } else {
        listOf(item) + _vendors.value
      }
      _vendors.value = next
      writeTextSafe(vendorsFile(), SyndicatAdvancedCodec.vendorsToJsonArray(next).toString(2))
    }
  }

  fun deleteVendor(vendorId: String) {
    synchronized(lock) {
      val next = _vendors.value.filterNot { it.id == vendorId }
      _vendors.value = next
      writeTextSafe(vendorsFile(), SyndicatAdvancedCodec.vendorsToJsonArray(next).toString(2))
    }
  }

  // --- Journal des incidents & réclamations ---

  fun upsertIncident(incident: IncidentRecord): IncidentRecord {
    val today = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Date())
    val item = incident.copy(
      id = incident.id.ifBlank { "inc_${UUID.randomUUID().toString().take(8)}" },
      title = incident.title.trim().take(140),
      locationFloor = incident.locationFloor.trim().take(60),
      description = incident.description.trim().take(400),
      reportedDate = incident.reportedDate.ifBlank { today }.take(30),
    )
    synchronized(lock) {
      val exists = _incidents.value.any { it.id == item.id }
      val next = if (exists) {
        _incidents.value.map { if (it.id == item.id) item else it }
      } else {
        listOf(item) + _incidents.value
      }
      _incidents.value = next
      writeTextSafe(incidentsFile(), SyndicatAdvancedCodec.incidentsToJsonArray(next).toString(2))
    }
    return item
  }

  fun cycleIncidentStatus(incidentId: String) {
    synchronized(lock) {
      val next = _incidents.value.map { inc ->
        if (inc.id == incidentId) inc.copy(status = inc.status.next()) else inc
      }
      _incidents.value = next
      writeTextSafe(incidentsFile(), SyndicatAdvancedCodec.incidentsToJsonArray(next).toString(2))
    }
  }

  fun linkIncidentExpense(incidentId: String, expenseId: String) {
    synchronized(lock) {
      val next = _incidents.value.map { inc ->
        if (inc.id == incidentId) inc.copy(linkedExpenseId = expenseId, status = IncidentStatus.RESOLVED) else inc
      }
      _incidents.value = next
      writeTextSafe(incidentsFile(), SyndicatAdvancedCodec.incidentsToJsonArray(next).toString(2))
    }
  }

  fun deleteIncident(incidentId: String) {
    synchronized(lock) {
      val next = _incidents.value.filterNot { it.id == incidentId }
      _incidents.value = next
      writeTextSafe(incidentsFile(), SyndicatAdvancedCodec.incidentsToJsonArray(next).toString(2))
    }
  }

  // --- Procès-Verbal d'Assemblée Générale (Quorum & Résolutions) ---

  fun updateAgMinutes(transform: (AgMinutesDraft) -> AgMinutesDraft) {
    synchronized(lock) {
      val updated = transform(_agMinutes.value)
      _agMinutes.value = updated
      writeTextSafe(agMinutesFile(), SyndicatAdvancedCodec.agMinutesToJsonObject(updated).toString(2))
    }
  }

  // --- Appels de fonds exceptionnels & Journal d'audit ---

  fun createSpecialCall(
    titleFr: String,
    titleAr: String,
    amountPerAptDh: Long,
    projectId: String = "",
    dueDateLabel: String = "",
  ): SpecialCallRecord {
    val today = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Date())
    val record = SpecialCallRecord(
      id = "sc_${UUID.randomUUID().toString().take(8)}",
      titleFr = titleFr.trim().ifBlank { titleAr.trim() }.take(150),
      titleAr = titleAr.trim().ifBlank { titleFr.trim() }.take(150),
      projectId = projectId.trim(),
      amountPerAptDh = amountPerAptDh.coerceIn(1L, 1_000_000L),
      createdDate = today,
      dueDateLabel = dueDateLabel.trim().take(30),
      payments = emptyMap(),
    )
    synchronized(lock) {
      val next = listOf(record) + _specialCalls.value
      _specialCalls.value = next
      writeTextSafe(callsFile(), SyndicatExtrasCodec.specialCallsToJsonArray(next).toString(2))
    }
    appendAuditLog(
      actionType = AuditActionType.SPECIAL_CALL,
      summaryFr = "Nouvel appel de fonds : ${record.displayTitle(false)} (${record.amountPerAptDh} DH/appt)",
      summaryAr = "نداء مساهمة استثنائي جديد: ${record.displayTitle(true)} (${record.amountPerAptDh} درهم/شقة)",
      amountDeltaDh = record.amountPerAptDh,
    )
    return record
  }

  fun toggleSpecialCallApartmentPayment(
    specialCallId: String,
    apartmentNumber: Long,
    markPaid: Boolean,
  ) {
    val today = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Date())
    var titleForLog: SpecialCallRecord? = null
    synchronized(lock) {
      val next = _specialCalls.value.map { call ->
        if (call.id != specialCallId) {
          call
        } else {
          titleForLog = call
          val updatedPayments = call.payments.toMutableMap()
          if (markPaid) {
            updatedPayments[apartmentNumber] = SpecialCallPayment(
              apartmentNumber = apartmentNumber,
              paidAmountDh = call.amountPerAptDh,
              paymentDate = today,
            )
          } else {
            updatedPayments.remove(apartmentNumber)
          }
          call.copy(payments = updatedPayments)
        }
      }
      _specialCalls.value = next
      writeTextSafe(callsFile(), SyndicatExtrasCodec.specialCallsToJsonArray(next).toString(2))
    }
    titleForLog?.let { call ->
      val signAmt = if (markPaid) call.amountPerAptDh else -call.amountPerAptDh
      appendAuditLog(
        actionType = AuditActionType.SPECIAL_CALL,
        summaryFr = if (markPaid) {
          "Appel « ${call.displayTitle(false)} » réglé par Appt $apartmentNumber"
        } else {
          "Annulation appel « ${call.displayTitle(false)} » Appt $apartmentNumber"
        },
        summaryAr = if (markPaid) {
          "أداء مساهمة « ${call.displayTitle(true)} » - الشقة $apartmentNumber"
        } else {
          "إلغاء أداء مساهمة « ${call.displayTitle(true)} » - الشقة $apartmentNumber"
        },
        amountDeltaDh = signAmt,
      )
    }
  }

  fun deleteSpecialCall(specialCallId: String) {
    var deleted: SpecialCallRecord? = null
    synchronized(lock) {
      val current = _specialCalls.value
      deleted = current.firstOrNull { it.id == specialCallId }
      val next = current.filterNot { it.id == specialCallId }
      _specialCalls.value = next
      writeTextSafe(callsFile(), SyndicatExtrasCodec.specialCallsToJsonArray(next).toString(2))
    }
    deleted?.let { call ->
      appendAuditLog(
        actionType = AuditActionType.SPECIAL_CALL,
        summaryFr = "Appel de fonds supprimé : ${call.displayTitle(false)}",
        summaryAr = "حذف نداء المساهمة الاستثنائي: ${call.displayTitle(true)}",
      )
    }
  }

  fun replaceSpecialCallsFromBackup(calls: List<SpecialCallRecord>) {
    synchronized(lock) {
      _specialCalls.value = calls
      writeTextSafe(callsFile(), SyndicatExtrasCodec.specialCallsToJsonArray(calls).toString(2))
    }
  }

  fun appendAuditLog(
    actionType: AuditActionType,
    summaryFr: String,
    summaryAr: String,
    amountDeltaDh: Long = 0L,
  ) {
    val entry = AuditLogEntry(
      id = "aud_${UUID.randomUUID().toString().take(8)}",
      timestampMs = System.currentTimeMillis(),
      actionType = actionType,
      summaryFr = summaryFr.take(280),
      summaryAr = summaryAr.take(280),
      amountDeltaDh = amountDeltaDh,
    )
    synchronized(lock) {
      val next = (listOf(entry) + _auditLogs.value).take(250)
      _auditLogs.value = next
      writeTextSafe(auditFile(), SyndicatExtrasCodec.auditLogsToJsonArray(next).toString(2))
    }
  }

  fun clearAuditLogs() {
    synchronized(lock) {
      _auditLogs.value = emptyList()
      writeTextSafe(auditFile(), "[]")
    }
  }

  companion object {
    @Volatile
    private var INSTANCE: SyndicatExtrasStore? = null

    fun getInstance(context: Context): SyndicatExtrasStore {
      return INSTANCE ?: synchronized(this) {
        INSTANCE ?: SyndicatExtrasStore(context.applicationContext).also { INSTANCE = it }
      }
    }
  }
}
