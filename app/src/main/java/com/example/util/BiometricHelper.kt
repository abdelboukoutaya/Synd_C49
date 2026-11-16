package com.example.util

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import androidx.core.content.ContextCompat

/**
 * Assistant d'authentification biométrique (empreinte digitale / reconnaissance faciale)
 * utilisant l'API système `android.hardware.biometrics.BiometricPrompt` (Android 9+ / API 28+),
 * sans dépendance externe.
 */
object BiometricHelper {

  fun isBiometricAvailable(context: Context): Boolean {
    return when {
      Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
        val mgr = context.getSystemService(BiometricManager::class.java) ?: return false
        mgr.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
      }
      Build.VERSION.SDK_INT == Build.VERSION_CODES.Q -> {
        val mgr = context.getSystemService(BiometricManager::class.java) ?: return false
        @Suppress("DEPRECATION")
        mgr.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
      }
      Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> {
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
      }
      else -> false
    }
  }

  fun promptUnlock(
    context: Context,
    title: String,
    subtitle: String,
    negativeButtonText: String,
    onSucceeded: () -> Unit,
    onErrorOrCancel: (String?) -> Unit = {},
  ) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
      onErrorOrCancel(null)
      return
    }
    try {
      val executor = ContextCompat.getMainExecutor(context)
      val cancellation = CancellationSignal()
      val builder = BiometricPrompt.Builder(context)
        .setTitle(title)
        .setSubtitle(subtitle)
        .setNegativeButton(negativeButtonText, executor) { _, _ ->
          onErrorOrCancel(null)
        }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        builder.setConfirmationRequired(false)
      }
      val prompt = builder.build()
      prompt.authenticate(
        cancellation,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
          override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
            onSucceeded()
          }

          override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
            onErrorOrCancel(errString?.toString())
          }

          override fun onAuthenticationFailed() {
            // System keeps prompt open until max attempts
          }
        },
      )
    } catch (e: Exception) {
      onErrorOrCancel(e.localizedMessage)
    }
  }
}
