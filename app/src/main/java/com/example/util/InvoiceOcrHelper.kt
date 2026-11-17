package com.example.util

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.math.roundToLong
import kotlinx.coroutines.suspendCancellableCoroutine

data class OcrInvoiceResult(
  val rawText: String = "",
  val detectedAmountDh: Long? = null,
  val detectedDate: String? = null,
  val detectedSupplier: String? = null,
  val errorMessage: String? = null,
)

/**
 * Lecture optique hors-ligne des factures et reçus via Google ML Kit Text Recognition.
 * Extrait automatiquement le montant TTC, la date et le fournisseur sans connexion internet.
 */
object InvoiceOcrHelper {

  private val dateRegex = Regex("""(\d{2})[/\-.](\d{2})[/\-.](\d{4})|(\d{4})[/\-.](\d{2})[/\-.](\d{2})""")
  private val moneyRegex = Regex("""(\d[\d\s]{0,9}(?:[.,]\d{1,2})?)""")
  private val priorityKeywordRegex = Regex("""(?i)(total|ttc|net\s*[àa]\s*payer|montant|dh|mad|dhs)""")

  suspend fun recognizeInvoiceFromUri(context: Context, uri: Uri): OcrInvoiceResult {
    return try {
      val image = InputImage.fromFilePath(context, uri)
      val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
      suspendCancellableCoroutine { cont ->
        recognizer.process(image)
          .addOnSuccessListener { visionText ->
            if (cont.isActive) {
              cont.resume(parseInvoiceText(visionText.text))
            }
          }
          .addOnFailureListener { exc ->
            if (cont.isActive) {
              cont.resume(OcrInvoiceResult(errorMessage = exc.localizedMessage ?: "OCR failed"))
            }
          }
      }
    } catch (e: IOException) {
      OcrInvoiceResult(errorMessage = e.localizedMessage ?: "Cannot read image")
    } catch (e: Exception) {
      OcrInvoiceResult(errorMessage = e.localizedMessage ?: "OCR error")
    }
  }

  fun parseInvoiceText(rawText: String): OcrInvoiceResult {
    val lines = rawText
      .lines()
      .map { it.trim() }
      .filter { it.isNotBlank() }

    if (lines.isEmpty()) return OcrInvoiceResult(rawText = rawText)

    val detectedDate = extractDate(lines)
    val detectedAmount = extractBestAmount(lines)
    val detectedSupplier = extractSupplier(lines)

    return OcrInvoiceResult(
      rawText = rawText,
      detectedAmountDh = detectedAmount,
      detectedDate = detectedDate,
      detectedSupplier = detectedSupplier,
    )
  }

  private fun extractDate(lines: List<String>): String? {
    for (line in lines) {
      val m = dateRegex.find(line) ?: continue
      val groups = m.groupValues
      return if (groups[1].isNotEmpty()) {
        "${groups[1]}/${groups[2]}/${groups[3]}"
      } else {
        "${groups[6]}/${groups[5]}/${groups[4]}"
      }
    }
    return null
  }

  private fun extractBestAmount(lines: List<String>): Long? {
    val keywordCandidates = mutableListOf<Long>()
    val fallbackCandidates = mutableListOf<Long>()

    for (line in lines) {
      val hasPriorityKeyword = priorityKeywordRegex.containsMatchIn(line)
      val withoutDates = dateRegex.replace(line, " ")
      for (match in moneyRegex.findAll(withoutDates)) {
        val normalized = match.groupValues[1]
          .replace(" ", "")
          .replace(',', '.')
        val value = normalized.toDoubleOrNull() ?: continue
        if (value <= 0.0 || value > 5_000_000.0) continue
        // Ignore lone 4-digit years like 2025/2026 unless accompanied by currency/total keyword
        val rounded = value.roundToLong()
        if (!hasPriorityKeyword && rounded in 2020L..2035L && !normalized.contains('.')) continue
        if (hasPriorityKeyword) {
          keywordCandidates.add(rounded)
        } else {
          fallbackCandidates.add(rounded)
        }
      }
    }
    return keywordCandidates.maxOrNull() ?: fallbackCandidates.maxOrNull()
  }

  private fun extractSupplier(lines: List<String>): String? {
    val noiseRegex = Regex("""(?i)^(facture|reçu|recu|ticket|devis|bon|date|total|ice|rc|if|patente|tel|tél|n°).*""")
    return lines.firstOrNull { line ->
      line.length in 3..60 &&
        line.any { it.isLetter() } &&
        line.count { it.isLetter() } >= line.length / 2 &&
        !noiseRegex.matches(line)
    }?.take(80)
  }
}
