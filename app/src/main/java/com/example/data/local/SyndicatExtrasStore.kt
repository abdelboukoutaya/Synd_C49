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

  private val storeDir = File(appContext.filesDir, "local_store").apply { mkdirs() }
  private val callsFile = File(storeDir, "syndicat_special_calls.json")
  private val auditFile = File(storeDir, "syndicat_audit_log.json")
  private val lock = Any()

  private val _specialCalls = MutableStateFlow<List<SpecialCallRecord>>(loadSpecialCallsFromDisk())
  val specialCalls: StateFlow<List<SpecialCallRecord>> = _specialCalls.asStateFlow()

  private val _auditLogs = MutableStateFlow<List<AuditLogEntry>>(loadAuditLogsFromDisk())
  val auditLogs: StateFlow<List<AuditLogEntry>> = _auditLogs.asStateFlow()

  private fun loadSpecialCallsFromDisk(): List<SpecialCallRecord> {
    return try {
      if (!callsFile.exists()) return emptyList()
      val raw = callsFile.readText(Charsets.UTF_8)
      if (raw.isBlank()) return emptyList()
      SyndicatExtrasCodec.parseSpecialCallsJsonArray(JSONArray(raw))
    } catch (_: Exception) {
      emptyList()
    }
  }

  private fun persistSpecialCalls(list: List<SpecialCallRecord>) {
    try {
      callsFile.writeText(SyndicatExtrasCodec.specialCallsToJsonArray(list).toString(2), Charsets.UTF_8)
    } catch (_: Exception) {
    }
  }

  private fun loadAuditLogsFromDisk(): List<AuditLogEntry> {
    return try {
      if (!auditFile.exists()) return emptyList()
      val raw = auditFile.readText(Charsets.UTF_8)
      if (raw.isBlank()) return emptyList()
      SyndicatExtrasCodec.parseAuditLogsJsonArray(JSONArray(raw))
    } catch (_: Exception) {
      emptyList()
    }
  }

  private fun persistAuditLogs(list: List<AuditLogEntry>) {
    try {
      auditFile.writeText(SyndicatExtrasCodec.auditLogsToJsonArray(list).toString(2), Charsets.UTF_8)
    } catch (_: Exception) {
    }
  }

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
      persistSpecialCalls(next)
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
      persistSpecialCalls(next)
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
      persistSpecialCalls(next)
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
      persistSpecialCalls(calls)
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
      persistAuditLogs(next)
    }
  }

  fun clearAuditLogs() {
    synchronized(lock) {
      _auditLogs.value = emptyList()
      persistAuditLogs(emptyList())
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
