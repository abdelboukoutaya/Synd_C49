package com.example.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.R
import com.example.ui.i18n.LanguagePreferences
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import java.security.MessageDigest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

private const val AUTH_TAG = "Auth"
private const val SILENT_SIGN_IN_TIMEOUT_MS = 4_000L
private const val FIREBASE_TOKEN_EXCHANGE_TIMEOUT_MS = 25_000L

/**
 * Empreintes du certificat Debug enregistrées sur le projet Firebase `gen-lang-client-0219823413`.
 * Permet de détecter automatiquement à l'exécution si un APK exporté (ex: Release signé avec une
 * autre clé) nécessite l'ajout de son empreinte SHA-1 / SHA-256 dans la console Firebase.
 */
const val REGISTERED_FIREBASE_DEBUG_SHA1 = "1F:F2:4F:D9:DA:D1:9E:DA:14:D3:48:B4:BB:88:BD:5C:08:33:2F:6A"
const val REGISTERED_FIREBASE_DEBUG_SHA256 = "08:7D:77:B7:24:5F:E3:50:22:92:44:AC:26:49:91:C0:BD:CA:43:28:4E:26:5C:40:D2:66:80:E5:EA:9B:C6:B1"

/**
 * Verrou global sérialisant tous les appels à [CredentialManager.getCredential].
 * Empêche toute collision entre une restauration silencieuse (`HiddenActivity`) et l'ouverture
 * interactive du sélecteur de compte Google (collision qui provoque `RESULT_CANCELED` immédiat).
 */
private val credentialRequestMutex = Mutex()

data class ApkAuthDiagnostics(
  val packageName: String,
  val firebaseProjectId: String,
  val webClientIdMasked: String,
  val isWebClientConfigured: Boolean,
  val runtimeSha1: String,
  val runtimeSha256: String,
  val matchesRegisteredFirebaseCert: Boolean,
)

/**
 * Inspecte à l'exécution le nom du paquet, l'ID client Web OAuth (`default_web_client_id`) et les
 * empreintes SHA-1 / SHA-256 réelles de la clé ayant signé l'APK installé.
 */
fun inspectApkAuthDiagnostics(context: Context): ApkAuthDiagnostics {
  val pkg = context.packageName
  val projectId = try {
    context.getString(R.string.project_id).trim()
  } catch (_: Exception) {
    "gen-lang-client-0219823413"
  }
  val webClientId = try {
    context.getString(R.string.default_web_client_id).trim()
  } catch (_: Exception) {
    ""
  }
  val isWebValid = webClientId.isNotBlank() && webClientId.contains("-")
  val maskedWebId = if (webClientId.length > 26) {
    "${webClientId.take(18)}…${webClientId.takeLast(12)}"
  } else {
    webClientId.ifBlank { "—" }
  }

  var sha1 = ""
  var sha256 = ""
  try {
    val certBytes: ByteArray? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      val pkgInfo = context.packageManager.getPackageInfo(
        pkg,
        PackageManager.GET_SIGNING_CERTIFICATES,
      )
      val signingInfo = pkgInfo.signingInfo
      val signers = if (signingInfo != null) {
        if (signingInfo.hasMultipleSigners()) {
          signingInfo.apkContentsSigners
        } else {
          signingInfo.signingCertificateHistory
        }
      } else {
        null
      }
      signers?.firstOrNull()?.toByteArray()
    } else {
      @Suppress("DEPRECATION")
      val pkgInfo = context.packageManager.getPackageInfo(pkg, PackageManager.GET_SIGNATURES)
      @Suppress("DEPRECATION")
      pkgInfo.signatures?.firstOrNull()?.toByteArray()
    }

    if (certBytes != null && certBytes.isNotEmpty()) {
      sha1 = MessageDigest.getInstance("SHA-1")
        .digest(certBytes)
        .joinToString(":") { "%02X".format(it) }
      sha256 = MessageDigest.getInstance("SHA-256")
        .digest(certBytes)
        .joinToString(":") { "%02X".format(it) }
    }
  } catch (e: Exception) {
    Log.w(AUTH_TAG, "Unable to read runtime APK signing certificate", e)
  }

  val matchesCert = sha1.isEmpty() || sha1.equals(REGISTERED_FIREBASE_DEBUG_SHA1, ignoreCase = true)

  return ApkAuthDiagnostics(
    packageName = pkg,
    firebaseProjectId = projectId,
    webClientIdMasked = maskedWebId,
    isWebClientConfigured = isWebValid,
    runtimeSha1 = sha1,
    runtimeSha256 = sha256,
    matchesRegisteredFirebaseCert = matchesCert,
  )
}

/**
 * Unwraps nested [ContextWrapper] instances so [CredentialManager.getCredential]
 * always receives the host [Activity] needed to anchor the Play Services sheet.
 */
tailrec fun Context.findActivity(): Activity? = when (this) {
  is Activity -> this
  is ContextWrapper -> baseContext.findActivity()
  else -> null
}

fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
  val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
    trySend(firebaseAuth.currentUser)
  }
  addAuthStateListener(listener)
  awaitClose { removeAuthStateListener(listener) }
}

/**
 * Attempts silent restoration ONLY when a previous Google session was authorized on this install.
 * Avoids spawning a background Play Services `HiddenActivity` on fresh installs or after sign-out,
 * preventing race-condition cancellations when the user taps "Sign in with Google".
 */
fun attemptAutoSignIn(
  context: Context,
  credentialManager: CredentialManager,
  onAuthSuccess: () -> Unit,
  onUnauthenticated: () -> Unit,
  scope: CoroutineScope,
): Job? {
  if (Firebase.auth.currentUser != null) {
    LanguagePreferences.setHasPreviousGoogleSession(context, true)
    onAuthSuccess()
    return null
  }
  if (!LanguagePreferences.hasPreviousGoogleSession(context)) {
    onUnauthenticated()
    return null
  }
  val activity = context.findActivity()
  if (activity == null) {
    onUnauthenticated()
    return null
  }

  val clientId = try {
    context.getString(R.string.default_web_client_id).trim()
  } catch (e: Exception) {
    Log.w(AUTH_TAG, "default_web_client_id unavailable for silent sign-in", e)
    onUnauthenticated()
    return null
  }
  if (clientId.isEmpty()) {
    onUnauthenticated()
    return null
  }

  val googleIdOption = GetGoogleIdOption.Builder()
    .setFilterByAuthorizedAccounts(true)
    .setServerClientId(clientId)
    .setAutoSelectEnabled(true)
    .build()

  val request = GetCredentialRequest.Builder()
    .addCredentialOption(googleIdOption)
    .build()

  return scope.launch {
    try {
      val result = credentialRequestMutex.withLock {
        withTimeout(SILENT_SIGN_IN_TIMEOUT_MS) {
          credentialManager.getCredential(activity, request)
        }
      }
      val credential = result.credential
      if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
        val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
        withTimeout(FIREBASE_TOKEN_EXCHANGE_TIMEOUT_MS) {
          Firebase.auth.signInWithCredential(authCredential).await()
        }
        LanguagePreferences.setHasPreviousGoogleSession(context, true)
        onAuthSuccess()
      } else {
        onUnauthenticated()
      }
    } catch (e: CancellationException) {
      if (e is TimeoutCancellationException) {
        Log.i(AUTH_TAG, "Silent Google Sign-In timed out; awaiting interactive sign-in.")
        onUnauthenticated()
      } else {
        throw e
      }
    } catch (_: Exception) {
      onUnauthenticated()
    }
  }
}

/**
 * Launches interactive Google Sign-In via [GetSignInWithGoogleOption] using the host Activity's
 * lifecycleScope (when available) so Compose recompositions never cancel the open account picker.
 * Awaits clean teardown of any prior silent request before opening the Google picker sheet.
 */
fun onGoogleSignInClicked(
  context: Context,
  credentialManager: CredentialManager,
  onAuthSuccess: () -> Unit,
  onAuthError: (String) -> Unit,
  scope: CoroutineScope,
  onAuthCancelled: () -> Unit = {},
  onNoAccountAvailable: (() -> Unit)? = null,
  onTimeout: (() -> Unit)? = null,
  priorJobToCancel: Job? = null,
  onAuthDiagnosticDetail: ((String) -> Unit)? = null,
): Job? {
  val activity = context.findActivity()
  if (activity == null) {
    onAuthError("Activité Android introuvable pour l'authentification Google.")
    return null
  }

  val clientId = try {
    context.getString(R.string.default_web_client_id).trim()
  } catch (e: Exception) {
    Log.e(AUTH_TAG, "Missing default_web_client_id", e)
    onAuthError("Configuration Google Sign-In manquante (default_web_client_id).")
    return null
  }
  if (clientId.isEmpty()) {
    onAuthError("Identifiant client Web Google vide (default_web_client_id).")
    return null
  }

  val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
  val request = GetCredentialRequest.Builder()
    .addCredentialOption(signInOption)
    .build()

  val hostScope = (activity as? LifecycleOwner)?.lifecycleScope ?: scope

  return hostScope.launch {
    try {
      if (priorJobToCancel != null && priorJobToCancel.isActive) {
        priorJobToCancel.cancelAndJoin()
        // Laisse Play Services HiddenActivity finaliser son cycle onDestroy avant d'ouvrir la feuille
        delay(180L)
      }

      val result = credentialRequestMutex.withLock {
        credentialManager.getCredential(activity, request)
      }
      val credential = result.credential
      if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
        val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
        withTimeout(FIREBASE_TOKEN_EXCHANGE_TIMEOUT_MS) {
          Firebase.auth.signInWithCredential(authCredential).await()
        }
        LanguagePreferences.setHasPreviousGoogleSession(context, true)
        onAuthSuccess()
      } else {
        onAuthError("Type d'identifiant Google inattendu.")
      }
    } catch (e: GetCredentialCancellationException) {
      Log.w(AUTH_TAG, "Google Sign-In cancelled or dismissed: ${e.message}", e)
      onAuthDiagnosticDetail?.invoke("${e.javaClass.simpleName}: ${e.message ?: "RESULT_CANCELED"}")
      onAuthCancelled()
    } catch (e: NoCredentialException) {
      Log.w(AUTH_TAG, "No Google account available on device: ${e.message}", e)
      onAuthDiagnosticDetail?.invoke("${e.javaClass.simpleName}: ${e.message ?: "No credentials"}")
      if (onNoAccountAvailable != null) {
        onNoAccountAvailable()
      } else {
        onAuthError(e.localizedMessage ?: "Aucun compte Google disponible sur cet appareil.")
      }
    } catch (e: TimeoutCancellationException) {
      Log.w(AUTH_TAG, "Google Sign-In token exchange timed out: ${e.message}", e)
      onAuthDiagnosticDetail?.invoke("TimeoutCancellationException: ${e.message ?: ""}")
      if (onTimeout != null) {
        onTimeout()
      } else {
        onAuthError("Délai de connexion Google dépassé.")
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: GetCredentialException) {
      Log.e(AUTH_TAG, "CredentialManager error (${e.type}): ${e.message}", e)
      onAuthDiagnosticDetail?.invoke("${e.type}: ${e.message ?: ""}")
      onAuthError(e.localizedMessage ?: "Erreur Credential Manager (${e.type}).")
    } catch (e: Exception) {
      Log.e(AUTH_TAG, "Google Sign-In failed", e)
      onAuthDiagnosticDetail?.invoke("${e.javaClass.simpleName}: ${e.message ?: ""}")
      onAuthError(e.localizedMessage ?: "Échec de la connexion Google.")
    }
  }
}

fun signOutUser(
  credentialManager: CredentialManager,
  scope: CoroutineScope,
  onSignOutComplete: () -> Unit = {},
  context: Context? = null,
) {
  context?.let { LanguagePreferences.setHasPreviousGoogleSession(it, false) }
  Firebase.auth.signOut()
  scope.launch {
    try {
      withTimeout(5_000L) {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
      }
    } catch (e: Exception) {
      Log.e(AUTH_TAG, "Failed to clear CredentialManager state", e)
    } finally {
      onSignOutComplete()
    }
  }
}
