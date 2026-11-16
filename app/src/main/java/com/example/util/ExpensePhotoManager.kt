package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Gestionnaire local des justificatifs photo (factures, bons de caisse, devis) attachés aux dépenses.
 * Utilise le sélecteur photo Android sans permission (PickVisualMedia) et stocke une copie JPEG
 * optimisée dans `filesDir/expense_proofs/`.
 */
object ExpensePhotoManager {

  private const val DIR_NAME = "expense_proofs"

  private fun proofsDir(context: Context): File {
    return File(context.applicationContext.filesDir, DIR_NAME).apply {
      if (!exists()) mkdirs()
    }
  }

  fun photoFileFor(context: Context, expenseId: String): File {
    val cleanId = expenseId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
    return File(proofsDir(context), "proof_$cleanId.jpg")
  }

  fun hasPhoto(context: Context, expenseId: String): Boolean {
    if (expenseId.isBlank()) return false
    val f = photoFileFor(context, expenseId)
    return f.exists() && f.length() > 0L
  }

  suspend fun savePhotoFromUri(context: Context, expenseId: String, uri: Uri): Boolean = withContext(Dispatchers.IO) {
    if (expenseId.isBlank()) return@withContext false
    try {
      val appCtx = context.applicationContext
      @Suppress("DEPRECATION")
      val rawBitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(appCtx.contentResolver, uri)
        ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
          decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
          decoder.isMutableRequired = false
        }
      } else {
        MediaStore.Images.Media.getBitmap(appCtx.contentResolver, uri)
      }

      val maxDim = 1280
      val scaled = if (rawBitmap.width > maxDim || rawBitmap.height > maxDim) {
        val ratio = minOf(maxDim.toFloat() / rawBitmap.width, maxDim.toFloat() / rawBitmap.height)
        Bitmap.createScaledBitmap(
          rawBitmap,
          (rawBitmap.width * ratio).toInt().coerceAtLeast(1),
          (rawBitmap.height * ratio).toInt().coerceAtLeast(1),
          true,
        )
      } else {
        rawBitmap
      }

      val destFile = photoFileFor(appCtx, expenseId)
      FileOutputStream(destFile).use { out ->
        scaled.compress(Bitmap.CompressFormat.JPEG, 82, out)
      }
      if (scaled !== rawBitmap) scaled.recycle()
      rawBitmap.recycle()
      true
    } catch (_: Exception) {
      false
    }
  }

  suspend fun promoteDraftPhoto(context: Context, draftKey: String, finalExpenseId: String) = withContext(Dispatchers.IO) {
    if (draftKey.isBlank() || finalExpenseId.isBlank() || draftKey == finalExpenseId) return@withContext
    val src = photoFileFor(context, draftKey)
    if (!src.exists() || src.length() == 0L) return@withContext
    val dst = photoFileFor(context, finalExpenseId)
    try {
      src.copyTo(dst, overwrite = true)
      src.delete()
    } catch (_: Exception) {
    }
  }

  suspend fun loadBitmap(context: Context, expenseId: String): ImageBitmap? = withContext(Dispatchers.IO) {
    if (expenseId.isBlank()) return@withContext null
    val f = photoFileFor(context, expenseId)
    if (!f.exists() || f.length() == 0L) return@withContext null
    try {
      BitmapFactory.decodeFile(f.absolutePath)?.asImageBitmap()
    } catch (_: Exception) {
      null
    }
  }

  suspend fun deletePhoto(context: Context, expenseId: String) = withContext(Dispatchers.IO) {
    if (expenseId.isBlank()) return@withContext
    try {
      val f = photoFileFor(context, expenseId)
      if (f.exists()) f.delete()
    } catch (_: Exception) {
    }
  }
}
