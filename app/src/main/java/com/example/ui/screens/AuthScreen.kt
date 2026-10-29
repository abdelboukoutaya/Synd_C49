package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.auth.attemptAutoSignIn
import com.example.auth.onGoogleSignInClicked
import com.example.ui.components.MetaSegmentedPillRow
import com.example.ui.components.SageSecondaryButton
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.CoinbaseDarkErrorContainer
import com.example.ui.theme.CoinbaseErrorTintLight
import com.example.ui.theme.LocalSyndicatTokens
import kotlinx.coroutines.Job

/**
 * Écran de connexion Compose épuré suivant le Coinbase Design System :
 * - Fond pur (`#FFFFFF` clair / `#0A0B0D` sombre) avec marges système `WindowInsets.safeDrawing`
 * - Carte centrale encadrée d'un filet 1dp (`#DEE1E6` / `#2D3138`), rayon 24dp
 * - Un unique bouton d'action primaire pilule 100dp en Coinbase Blue (`#0052FF`) :
 *   « Se connecter avec Google » appelant directement le service d'authentification (`onGoogleSignInClicked`).
 */
@Composable
fun AuthScreen(
  currentLanguage: AppLanguage,
  onSelectLanguage: (AppLanguage) -> Unit,
  onAuthSuccess: () -> Unit,
) {
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }

  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
  var infoMessage by rememberSaveable { mutableStateOf<String?>(null) }
  var silentAuthJob by remember { mutableStateOf<Job?>(null) }
  var interactiveAuthJob by remember { mutableStateOf<Job?>(null) }

  LaunchedEffect(Unit) {
    silentAuthJob = attemptAutoSignIn(
      context = context,
      credentialManager = credentialManager,
      onAuthSuccess = onAuthSuccess,
      onUnauthenticated = {},
      scope = scope,
    )
  }

  // Ne jamais annuler interactiveAuthJob dans onDispose afin de ne pas refermer
  // le sélecteur de compte Google Play Services lors d'une recomposition.
  DisposableEffect(Unit) {
    onDispose {
      silentAuthJob?.cancel()
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(tokens.screenBg)
      .testTag("auth_screen"),
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .windowInsetsPadding(WindowInsets.safeDrawing)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp, vertical = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      // Sélecteur de langue discret (العربية / Français)
      MetaSegmentedPillRow(
        firstLabel = AppLanguage.AR.nativeLabel,
        secondLabel = AppLanguage.FR.nativeLabel,
        isFirstSelected = currentLanguage == AppLanguage.AR,
        onSelectFirst = { onSelectLanguage(AppLanguage.AR) },
        onSelectSecond = { onSelectLanguage(AppLanguage.FR) },
        firstTag = "auth_lang_${AppLanguage.AR.code}",
        secondTag = "auth_lang_${AppLanguage.FR.code}",
        modifier = Modifier.padding(bottom = 24.dp),
      )

      Surface(
        shape = RoundedCornerShape(tokens.cardRadiusDp.dp),
        color = tokens.cardBg,
        border = BorderStroke(1.dp, tokens.cardBorder),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 400.dp),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 32.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          // Emblème minimaliste Coinbase Blue sur pastille teintée
          Surface(
            shape = CircleShape,
            color = tokens.primaryTint,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            modifier = Modifier.size(56.dp),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Filled.Apartment,
                contentDescription = strings.authTitle,
                tint = tokens.linkColor,
                modifier = Modifier.size(26.dp),
              )
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          // Titre calme éditorial (Coinbase weight 400)
          Text(
            text = strings.authTitle,
            style = MaterialTheme.typography.headlineMedium,
            color = tokens.inkDeep,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Sous-titre concis
          Text(
            text = strings.authSubtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = tokens.body,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
          )

          if (infoMessage != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = tokens.primaryTint,
              border = BorderStroke(1.dp, tokens.infoBorder.copy(alpha = 0.25f)),
              tonalElevation = 0.dp,
              shadowElevation = 0.dp,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("auth_info_banner"),
            ) {
              Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
              ) {
                Icon(
                  imageVector = Icons.Filled.Info,
                  contentDescription = null,
                  tint = tokens.linkColor,
                  modifier = Modifier
                    .padding(top = 2.dp)
                    .size(18.dp),
                )
                Text(
                  text = infoMessage ?: "",
                  style = MaterialTheme.typography.bodySmall,
                  color = tokens.inkDeep,
                  modifier = Modifier.weight(1f),
                )
              }
            }
          }

          if (errorMessage != null) {
            Spacer(modifier = Modifier.height(20.dp))
            val errorContainerBg = if (tokens.isDark) CoinbaseDarkErrorContainer else CoinbaseErrorTintLight
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = errorContainerBg,
              border = BorderStroke(1.dp, tokens.statusLateText.copy(alpha = 0.30f)),
              tonalElevation = 0.dp,
              shadowElevation = 0.dp,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("auth_error_banner"),
            ) {
              Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
              ) {
                Icon(
                  imageVector = Icons.Filled.ErrorOutline,
                  contentDescription = null,
                  tint = tokens.statusLateText,
                  modifier = Modifier
                    .padding(top = 2.dp)
                    .size(18.dp),
                )
                Text(
                  text = errorMessage ?: "",
                  style = MaterialTheme.typography.bodySmall,
                  color = tokens.statusLateText,
                  modifier = Modifier.weight(1f),
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(28.dp))

          // Bouton pilule principal « Se connecter avec Google » (Coinbase Blue #0052FF)
          GoogleSignInPillButton(
            label = if (isLoading) strings.authConnectingGoogle else strings.authSignInGoogle,
            isLoading = isLoading,
            onClick = {
              val priorSilent = silentAuthJob
              silentAuthJob = null
              interactiveAuthJob?.cancel()
              errorMessage = null
              infoMessage = null
              isLoading = true

              interactiveAuthJob = onGoogleSignInClicked(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {
                  isLoading = false
                  onAuthSuccess()
                },
                onAuthError = { msg ->
                  isLoading = false
                  errorMessage = msg
                },
                scope = scope,
                onAuthCancelled = {
                  isLoading = false
                  infoMessage = strings.authCancelledHint
                },
                onNoAccountAvailable = {
                  isLoading = false
                  infoMessage = strings.authNoAccountHint
                },
                onTimeout = {
                  isLoading = false
                  errorMessage = strings.authTimeoutHint
                },
                priorJobToCancel = priorSilent,
              )
              if (interactiveAuthJob == null && errorMessage != null) {
                isLoading = false
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("google_sign_in_button"),
          )

          if (isLoading) {
            Spacer(modifier = Modifier.height(12.dp))
            SageSecondaryButton(
              text = strings.authCancelBtn,
              icon = Icons.Filled.Close,
              compact = true,
              onClick = {
                silentAuthJob?.cancel()
                interactiveAuthJob?.cancel()
                interactiveAuthJob = null
                isLoading = false
              },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("auth_cancel_button"),
            )
          }

          Spacer(modifier = Modifier.height(20.dp))

          Text(
            text = strings.authSecurityNote,
            style = MaterialTheme.typography.labelSmall,
            color = tokens.mute,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }
    }
  }
}

/**
 * Bouton pilule Coinbase Blue (`#0052FF`, pressé `#003ECC`, rayon 100dp, hauteur ≥ 52dp)
 * affichant le badge Google « G » et déclenchant le service d'authentification.
 */
@Composable
fun GoogleSignInPillButton(
  label: String,
  isLoading: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val tokens = LocalSyndicatTokens.current
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val containerColor = if (isPressed) tokens.primaryActive else tokens.primary

  Button(
    onClick = onClick,
    enabled = !isLoading,
    interactionSource = interactionSource,
    shape = RoundedCornerShape(tokens.buttonRadiusDp.dp),
    colors = ButtonDefaults.buttonColors(
      containerColor = containerColor,
      contentColor = tokens.onPrimary,
      disabledContainerColor = tokens.primary,
      disabledContentColor = tokens.onPrimary,
    ),
    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
    modifier = modifier
      .minimumInteractiveComponentSize()
      .heightIn(min = 52.dp),
  ) {
    if (isLoading) {
      CircularProgressIndicator(
        modifier = Modifier.size(20.dp),
        color = tokens.onPrimary,
        strokeWidth = 2.dp,
      )
      Spacer(modifier = Modifier.width(12.dp))
    } else {
      GoogleMarkBadge()
      Spacer(modifier = Modifier.width(12.dp))
    }
    Text(
      text = label,
      style = MaterialTheme.typography.labelLarge,
      fontWeight = FontWeight.SemiBold,
      color = tokens.onPrimary,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Composable
private fun GoogleMarkBadge() {
  Surface(
    shape = CircleShape,
    color = Color.White,
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
    modifier = Modifier.size(24.dp),
  ) {
    Canvas(
      modifier = Modifier
        .fillMaxSize()
        .padding(5.dp),
    ) {
      val stroke = size.minDimension * 0.22f
      val arcSize = Size(size.width - stroke, size.height - stroke)
      val topLeft = Offset(stroke / 2f, stroke / 2f)

      // Quatre segments chromatiques officiels du « G » Google
      drawArc(
        color = Color(0xFFEA4335),
        startAngle = -145f,
        sweepAngle = 100f,
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = Stroke(width = stroke, cap = StrokeCap.Butt),
      )
      drawArc(
        color = Color(0xFFFBBC05),
        startAngle = 135f,
        sweepAngle = 80f,
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = Stroke(width = stroke, cap = StrokeCap.Butt),
      )
      drawArc(
        color = Color(0xFF34A853),
        startAngle = 45f,
        sweepAngle = 90f,
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = Stroke(width = stroke, cap = StrokeCap.Butt),
      )
      drawArc(
        color = Color(0xFF4285F4),
        startAngle = -12f,
        sweepAngle = 57f,
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = Stroke(width = stroke, cap = StrokeCap.Butt),
      )
      // Barre horizontale centrale du « G »
      drawLine(
        color = Color(0xFF4285F4),
        start = Offset(size.width * 0.50f, size.height * 0.50f),
        end = Offset(size.width * 0.96f, size.height * 0.50f),
        strokeWidth = stroke,
        cap = StrokeCap.Butt,
      )
    }
  }
}
