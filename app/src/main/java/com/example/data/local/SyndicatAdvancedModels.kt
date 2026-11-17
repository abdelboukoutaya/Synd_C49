package com.example.data.local

import com.example.data.model.normalizeSyndicatCategory
import org.json.JSONArray
import org.json.JSONObject

data class RecurringExpenseTemplate(
  val id: String,
  val title: String,
  val category: String,
  val amountDh: Long,
  val supplier: String = "",
  val dayOfMonth: Int = 5,
  val lastAppliedMonthKey: String = "",
  val active: Boolean = true,
) {
  fun isAppliedForMonth(monthKey: String): Boolean = lastAppliedMonthKey == monthKey
}

data class VendorRecord(
  val id: String,
  val name: String,
  val specialty: String,
  val phone: String = "",
  val monthlyFeeDh: Long = 0L,
  val contractEndDate: String = "",
  val notes: String = "",
)

enum class IncidentSeverity(val code: String) {
  LOW("low"),
  MEDIUM("medium"),
  HIGH("high");

  companion object {
    fun fromCode(raw: String?): IncidentSeverity =
      entries.firstOrNull { it.code.equals(raw, ignoreCase = true) } ?: MEDIUM
  }
}

enum class IncidentStatus(val code: String) {
  REPORTED("reported"),
  IN_PROGRESS("in_progress"),
  RESOLVED("resolved");

  fun next(): IncidentStatus = when (this) {
    REPORTED -> IN_PROGRESS
    IN_PROGRESS -> RESOLVED
    RESOLVED -> REPORTED
  }

  companion object {
    fun fromCode(raw: String?): IncidentStatus =
      entries.firstOrNull { it.code.equals(raw, ignoreCase = true) } ?: REPORTED
  }
}

data class IncidentRecord(
  val id: String,
  val title: String,
  val locationFloor: String = "",
  val description: String = "",
  val severity: IncidentSeverity = IncidentSeverity.MEDIUM,
  val status: IncidentStatus = IncidentStatus.REPORTED,
  val reportedDate: String = "",
  val photoUri: String = "",
  val linkedExpenseId: String = "",
)

data class AgResolutionRecord(
  val id: String,
  val title: String,
  val votesFor: Int = 0,
  val votesAgainst: Int = 0,
  val votesAbstain: Int = 0,
) {
  fun isAdopted(): Boolean = votesFor > votesAgainst
}

data class AgMinutesDraft(
  val meetingDate: String = "",
  val location: String = "",
  val agenda: String = "",
  val presentAptNumbers: Set<Long> = emptySet(),
  val resolutions: List<AgResolutionRecord> = emptyList(),
)

data class BuildingProfile(
  val id: String,
  val name: String,
  val city: String = "",
)

enum class SyndicatRoleMode(val code: String) {
  ADMIN("admin"),
  READ_ONLY_COUNCIL("read_only_council");

  companion object {
    fun fromCode(raw: String?): SyndicatRoleMode =
      entries.firstOrNull { it.code.equals(raw, ignoreCase = true) } ?: ADMIN
  }
}

object SyndicatAdvancedCodec {
  fun starterRecurringTemplates(isAr: Boolean): List<RecurringExpenseTemplate> = listOf(
    RecurringExpenseTemplate(
      id = "rec_guard",
      title = if (isAr) "حراسة العمارة" else "Gardiennage immeuble",
      category = "Gardiennage",
      amountDh = 1200L,
      supplier = if (isAr) "الحارس" else "Gardien",
      dayOfMonth = 1,
    ),
    RecurringExpenseTemplate(
      id = "rec_clean",
      title = if (isAr) "تنظيف الدرج والمدخل" else "Ménage parties communes",
      category = "Nettoyage",
      amountDh = 450L,
      supplier = if (isAr) "شركة التنظيف" else "Équipe Nettoyage",
      dayOfMonth = 5,
    ),
    RecurringExpenseTemplate(
      id = "rec_elec",
      title = if (isAr) "كهرباء الممرات" else "Électricité parties communes",
      category = "Électricité",
      amountDh = 220L,
      supplier = "ONEE / Lydec",
      dayOfMonth = 10,
    ),
    RecurringExpenseTemplate(
      id = "rec_elev",
      title = if (isAr) "صيانة المصعد" else "Maintenance Ascenseur",
      category = "Ascenseur",
      amountDh = 600L,
      supplier = "Atlas Lift",
      dayOfMonth = 15,
    ),
  )

  fun recurringToJsonArray(list: List<RecurringExpenseTemplate>): JSONArray {
    val arr = JSONArray()
    list.forEach { t ->
      arr.put(
        JSONObject().apply {
          put("id", t.id)
          put("title", t.title)
          put("category", t.category)
          put("amountDh", t.amountDh)
          put("supplier", t.supplier)
          put("dayOfMonth", t.dayOfMonth)
          put("lastAppliedMonthKey", t.lastAppliedMonthKey)
          put("active", t.active)
        },
      )
    }
    return arr
  }

  fun parseRecurringJsonArray(arr: JSONArray?): List<RecurringExpenseTemplate> {
    if (arr == null) return emptyList()
    val out = mutableListOf<RecurringExpenseTemplate>()
    for (i in 0 until arr.length()) {
      val o = arr.optJSONObject(i) ?: continue
      out.add(
        RecurringExpenseTemplate(
          id = o.optString("id", "rec_$i").ifBlank { "rec_$i" },
          title = o.optString("title", "").take(120),
          category = normalizeSyndicatCategory(o.optString("category", "Divers")),
          amountDh = o.optLong("amountDh", 0L).coerceIn(0L, 1_000_000L),
          supplier = o.optString("supplier", "").take(100),
          dayOfMonth = o.optInt("dayOfMonth", 5).coerceIn(1, 28),
          lastAppliedMonthKey = o.optString("lastAppliedMonthKey", "").take(12),
          active = o.optBoolean("active", true),
        ),
      )
    }
    return out
  }

  fun vendorsToJsonArray(list: List<VendorRecord>): JSONArray {
    val arr = JSONArray()
    list.forEach { v ->
      arr.put(
        JSONObject().apply {
          put("id", v.id)
          put("name", v.name)
          put("specialty", v.specialty)
          put("phone", v.phone)
          put("monthlyFeeDh", v.monthlyFeeDh)
          put("contractEndDate", v.contractEndDate)
          put("notes", v.notes)
        },
      )
    }
    return arr
  }

  fun parseVendorsJsonArray(arr: JSONArray?): List<VendorRecord> {
    if (arr == null) return emptyList()
    val out = mutableListOf<VendorRecord>()
    for (i in 0 until arr.length()) {
      val o = arr.optJSONObject(i) ?: continue
      out.add(
        VendorRecord(
          id = o.optString("id", "vnd_$i").ifBlank { "vnd_$i" },
          name = o.optString("name", "").take(100),
          specialty = o.optString("specialty", "").take(80),
          phone = o.optString("phone", "").take(30),
          monthlyFeeDh = o.optLong("monthlyFeeDh", 0L).coerceIn(0L, 1_000_000L),
          contractEndDate = o.optString("contractEndDate", "").take(30),
          notes = o.optString("notes", "").take(250),
        ),
      )
    }
    return out
  }

  fun incidentsToJsonArray(list: List<IncidentRecord>): JSONArray {
    val arr = JSONArray()
    list.forEach { inc ->
      arr.put(
        JSONObject().apply {
          put("id", inc.id)
          put("title", inc.title)
          put("locationFloor", inc.locationFloor)
          put("description", inc.description)
          put("severity", inc.severity.code)
          put("status", inc.status.code)
          put("reportedDate", inc.reportedDate)
          put("photoUri", inc.photoUri)
          put("linkedExpenseId", inc.linkedExpenseId)
        },
      )
    }
    return arr
  }

  fun parseIncidentsJsonArray(arr: JSONArray?): List<IncidentRecord> {
    if (arr == null) return emptyList()
    val out = mutableListOf<IncidentRecord>()
    for (i in 0 until arr.length()) {
      val o = arr.optJSONObject(i) ?: continue
      out.add(
        IncidentRecord(
          id = o.optString("id", "inc_$i").ifBlank { "inc_$i" },
          title = o.optString("title", "").take(140),
          locationFloor = o.optString("locationFloor", "").take(60),
          description = o.optString("description", "").take(400),
          severity = IncidentSeverity.fromCode(o.optString("severity", "medium")),
          status = IncidentStatus.fromCode(o.optString("status", "reported")),
          reportedDate = o.optString("reportedDate", "").take(30),
          photoUri = o.optString("photoUri", "").take(300),
          linkedExpenseId = o.optString("linkedExpenseId", "").take(80),
        ),
      )
    }
    return out
  }

  fun agMinutesToJsonObject(draft: AgMinutesDraft): JSONObject = JSONObject().apply {
    put("meetingDate", draft.meetingDate)
    put("location", draft.location)
    put("agenda", draft.agenda)
    put(
      "presentAptNumbers",
      JSONArray().apply { draft.presentAptNumbers.sorted().forEach { put(it) } },
    )
    put(
      "resolutions",
      JSONArray().apply {
        draft.resolutions.forEach { r ->
          put(
            JSONObject().apply {
              put("id", r.id)
              put("title", r.title)
              put("votesFor", r.votesFor)
              put("votesAgainst", r.votesAgainst)
              put("votesAbstain", r.votesAbstain)
            },
          )
        }
      },
    )
  }

  fun parseAgMinutesJsonObject(obj: JSONObject?): AgMinutesDraft {
    if (obj == null) return AgMinutesDraft()
    val presentSet = linkedSetOf<Long>()
    val pArr = obj.optJSONArray("presentAptNumbers")
    if (pArr != null) {
      for (i in 0 until pArr.length()) {
        val n = pArr.optLong(i, -1L)
        if (n > 0L) presentSet.add(n)
      }
    }
    val resList = mutableListOf<AgResolutionRecord>()
    val rArr = obj.optJSONArray("resolutions")
    if (rArr != null) {
      for (i in 0 until rArr.length()) {
        val ro = rArr.optJSONObject(i) ?: continue
        resList.add(
          AgResolutionRecord(
            id = ro.optString("id", "res_$i").ifBlank { "res_$i" },
            title = ro.optString("title", "").take(200),
            votesFor = ro.optInt("votesFor", 0).coerceAtLeast(0),
            votesAgainst = ro.optInt("votesAgainst", 0).coerceAtLeast(0),
            votesAbstain = ro.optInt("votesAbstain", 0).coerceAtLeast(0),
          ),
        )
      }
    }
    return AgMinutesDraft(
      meetingDate = obj.optString("meetingDate", "").take(30),
      location = obj.optString("location", "").take(120),
      agenda = obj.optString("agenda", "").take(500),
      presentAptNumbers = presentSet,
      resolutions = resList,
    )
  }

  fun buildingsToJsonObject(activeId: String, list: List<BuildingProfile>): JSONObject = JSONObject().apply {
    put("activeId", activeId)
    put(
      "buildings",
      JSONArray().apply {
        list.forEach { b ->
          put(
            JSONObject().apply {
              put("id", b.id)
              put("name", b.name)
              put("city", b.city)
            },
          )
        }
      },
    )
  }

  fun parseBuildingsJsonObject(obj: JSONObject?): Pair<String, List<BuildingProfile>> {
    val defaultProfile = BuildingProfile(id = "default", name = "Résidence Principale")
    if (obj == null) return "default" to listOf(defaultProfile)
    val arr = obj.optJSONArray("buildings")
    val list = mutableListOf<BuildingProfile>()
    if (arr != null) {
      for (i in 0 until arr.length()) {
        val o = arr.optJSONObject(i) ?: continue
        val id = o.optString("id", "").trim()
        val name = o.optString("name", "").trim()
        if (id.isNotBlank() && name.isNotBlank()) {
          list.add(BuildingProfile(id = id, name = name.take(100), city = o.optString("city", "").take(60)))
        }
      }
    }
    if (list.none { it.id == "default" }) {
      list.add(0, defaultProfile)
    }
    val active = obj.optString("activeId", "default").takeIf { id -> list.any { it.id == id } } ?: "default"
    return active to list
  }
}
