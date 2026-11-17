package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.EncodeHintType
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.WriterException
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

/**
 * Encodage et décodage QR Code haute capacité (Versions 1 à 40 ISO/IEC 18004 via ZXing)
 * pour le partage Conseil Syndical (Lecture seule) sans exposer un long texte brut.
 */
object CouncilQrCodeHelper {

  fun encodeShareQrBitmap(
    payload: String,
    sizePx: Int = 720,
    foregroundColor: Int = 0xFF0A1628.toInt(),
    backgroundColor: Int = 0xFFFFFFFF.toInt(),
  ): Bitmap {
    val cleanPayload = payload.trim()
    require(cleanPayload.isNotEmpty()) { "Le contenu du QR Code est vide." }

    val writer = QRCodeWriter()
    val payloadBytes = cleanPayload.toByteArray(Charsets.UTF_8).size
    val preferredEcc = if (payloadBytes <= 1800) ErrorCorrectionLevel.M else ErrorCorrectionLevel.L

    val bitMatrix = try {
      writer.encode(
        cleanPayload,
        BarcodeFormat.QR_CODE,
        sizePx,
        sizePx,
        mapOf(
          EncodeHintType.CHARACTER_SET to "UTF-8",
          EncodeHintType.ERROR_CORRECTION to preferredEcc,
          EncodeHintType.MARGIN to 2,
        ),
      )
    } catch (_: WriterException) {
      writer.encode(
        cleanPayload,
        BarcodeFormat.QR_CODE,
        sizePx,
        sizePx,
        mapOf(
          EncodeHintType.CHARACTER_SET to "UTF-8",
          EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.L,
          EncodeHintType.MARGIN to 1,
        ),
      )
    }

    val width = bitMatrix.width
    val height = bitMatrix.height
    val pixels = IntArray(width * height)
    for (y in 0 until height) {
      val rowOffset = y * width
      for (x in 0 until width) {
        pixels[rowOffset + x] = if (bitMatrix.get(x, y)) foregroundColor else backgroundColor
      }
    }
    return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
      setPixels(pixels, 0, width, 0, 0, width, height)
    }
  }

  fun buildShareableQrCardBitmap(
    qrBitmap: Bitmap,
    residenceName: String,
    year: Long,
    summaryText: String,
    badgeTitle: String = "SYNDICAT · Conseil Syndical (Lecture seule)",
    footerHint: String = "Ouvrez SYNDICAT > Paramètres > Importer un QR Code",
  ): Bitmap {
    val width = 900
    val height = 1160
    val card = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(card)
    canvas.drawColor(0xFFFFFFFF.toInt())

    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.STROKE
      color = 0xFFE2E8F0.toInt()
      strokeWidth = 4f
    }
    canvas.drawRoundRect(RectF(24f, 24f, width - 24f, height - 24f), 36f, 36f, borderPaint)

    val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = 0xFFEAF2FF.toInt()
      style = Paint.Style.FILL
    }
    canvas.drawRoundRect(RectF(64f, 64f, width - 64f, 134f), 24f, 24f, pillPaint)

    val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = 0xFF0052FF.toInt()
      textSize = 26f
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      textAlign = Paint.Align.CENTER
    }
    canvas.drawText(badgeTitle, width / 2f, 108f, badgePaint)

    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = 0xFF0A1628.toInt()
      textSize = 40f
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      textAlign = Paint.Align.CENTER
    }
    canvas.drawText("$residenceName · $year", width / 2f, 200f, titlePaint)

    val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = 0xFF475569.toInt()
      textSize = 25f
      textAlign = Paint.Align.CENTER
    }
    canvas.drawText(summaryText, width / 2f, 246f, subPaint)

    val qrFramePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = 0xFFCBD5E1.toInt()
      style = Paint.Style.STROKE
      strokeWidth = 3f
    }
    val qrBox = Rect(110, 290, 790, 970)
    canvas.drawRoundRect(RectF(qrBox), 24f, 24f, qrFramePaint)

    val qrDest = Rect(130, 310, 770, 950)
    val qrPaint = Paint().apply {
      isFilterBitmap = false
      isAntiAlias = false
    }
    canvas.drawBitmap(qrBitmap, null, qrDest, qrPaint)

    val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = 0xFF64748B.toInt()
      textSize = 22f
      textAlign = Paint.Align.CENTER
    }
    canvas.drawText(footerHint, width / 2f, 1046f, footerPaint)

    val editionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = 0xFF94A3B8.toInt()
      textSize = 20f
      textAlign = Paint.Align.CENTER
    }
    canvas.drawText("SYNDICAT · DYNIC v2.0", width / 2f, 1094f, editionPaint)

    return card
  }

  fun shareQrCardPng(
    context: Context,
    cardBitmap: Bitmap,
    residenceName: String,
    chooserTitle: String,
    onError: (String) -> Unit,
  ) {
    runCatching {
      val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
      exportsDir.listFiles { _, name ->
        name.startsWith("syndicat_conseil_qr_") && name.endsWith(".png")
      }?.forEach { oldFile ->
        if (System.currentTimeMillis() - oldFile.lastModified() > 3_600_000L) {
          oldFile.delete()
        }
      }

      val safeResidence = residenceName
        .replace(Regex("[^A-Za-z0-9_-]+"), "_")
        .trim('_')
        .ifBlank { "residence" }
        .take(32)
      val outFile = File(exportsDir, "syndicat_conseil_qr_${safeResidence}_${System.currentTimeMillis()}.png")
      FileOutputStream(outFile).use { fos ->
        cardBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
      }
      val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outFile)
      val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, chooserTitle)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
      context.startActivity(
        Intent.createChooser(shareIntent, chooserTitle).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
      )
    }.onFailure { err ->
      onError(err.localizedMessage ?: "Impossible de partager l'image QR")
    }
  }

  fun createCameraTempUri(context: Context): Uri {
    val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
    val outFile = File(exportsDir, "council_qr_scan_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outFile)
  }

  fun decodeQrFromBitmap(bitmap: Bitmap): String? {
    val maxDim = max(bitmap.width, bitmap.height)
    val working = if (maxDim > 1400) {
      val scale = 1400f / maxDim.toFloat()
      val targetW = max(1, (bitmap.width * scale).toInt())
      val targetH = max(1, (bitmap.height * scale).toInt())
      Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
    } else if (bitmap.config != Bitmap.Config.ARGB_8888) {
      bitmap.copy(Bitmap.Config.ARGB_8888, false)
    } else {
      bitmap
    }

    val width = working.width
    val height = working.height
    if (width <= 0 || height <= 0) return null

    val pixels = IntArray(width * height)
    working.getPixels(pixels, 0, width, 0, 0, width, height)
    if (working !== bitmap) {
      working.recycle()
    }

    val source = RGBLuminanceSource(width, height, pixels)
    val hints = mapOf(
      DecodeHintType.TRY_HARDER to true,
      DecodeHintType.CHARACTER_SET to "UTF-8",
    )
    val reader = QRCodeReader()

    val candidates = listOf(
      BinaryBitmap(HybridBinarizer(source)),
      BinaryBitmap(GlobalHistogramBinarizer(source)),
      BinaryBitmap(HybridBinarizer(source.invert())),
    )

    for (candidate in candidates) {
      val decoded = runCatching {
        reader.reset()
        reader.decode(candidate, hints).text?.trim()
      }.getOrNull()
      if (!decoded.isNullOrBlank()) {
        return decoded
      }
    }
    return null
  }

  fun decodeQrFromUri(context: Context, uri: Uri): String? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { stream ->
      BitmapFactory.decodeStream(stream, null, bounds)
    }
    val rawMax = max(bounds.outWidth, bounds.outHeight)
    var sampleSize = 1
    while (rawMax / sampleSize > 1600) {
      sampleSize *= 2
    }

    val decodeOpts = BitmapFactory.Options().apply {
      inSampleSize = sampleSize
      inPreferredConfig = Bitmap.Config.ARGB_8888
    }
    val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
      BitmapFactory.decodeStream(stream, null, decodeOpts)
    } ?: return null

    return try {
      decodeQrFromBitmap(bitmap)
    } finally {
      bitmap.recycle()
    }
  }
}
