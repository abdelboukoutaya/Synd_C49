package com.example.util

import android.util.Base64
import com.example.data.local.SpecialCallRecord
import com.example.data.repository.LOCAL_DEVICE_USER_ID
import com.example.ui.viewmodel.SyndicatDashboardData
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * Code compact de partage pour le Conseil Syndical (Lecteur / Co-gestionnaire).
 * Compresse les données de l'immeuble en un code `SYNDICAT-CONSEIL-1:...` facile à envoyer
 * sur WhatsApp et à importer en mode Lecture Seule ou Gestionnaire sur un autre téléphone.
 */
object SyndicatCouncilShareCodec {

  private const val PREFIX = "SYNDICAT-CONSEIL-1:"

  fun encodeShareCode(
    data: SyndicatDashboardData,
    specialCalls: List<SpecialCallRecord> = emptyList(),
  ): String {
    val json = BackupManager.serializeBackupJson(data, specialCalls)
    val baos = ByteArrayOutputStream()
    GZIPOutputStream(baos).use { gzip ->
      gzip.write(json.toByteArray(Charsets.UTF_8))
    }
    val b64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
    return "$PREFIX$b64"
  }

  fun decodeShareCode(rawInput: String, targetUserId: String = LOCAL_DEVICE_USER_ID): ParsedSyndicatBackup {
    val trimmed = rawInput.trim()
    val jsonString = if (trimmed.startsWith(PREFIX)) {
      val b64 = trimmed.removePrefix(PREFIX).trim()
      val compressed = Base64.decode(b64, Base64.DEFAULT)
      GZIPInputStream(ByteArrayInputStream(compressed)).bufferedReader(Charsets.UTF_8).use { it.readText() }
    } else {
      trimmed
    }
    return BackupManager.parseBackupJson(jsonString, targetUserId)
  }
}
