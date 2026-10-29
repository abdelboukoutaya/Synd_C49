package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.CoinbaseSurfaceDark
import com.example.ui.theme.LocalSyndicatTokens
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Gère l'image personnalisée de la résidence choisie par l'utilisateur via le sélecteur
 * photo système Android sans permission (`ActivityResultContracts.PickVisualMedia`).
 * Conserve une copie redimensionnée localement dans `filesDir` afin de fonctionner hors ligne.
 */
object ResidencePhotoManager {
  private const val PREFS_NAME = "syndicat_locale_prefs"
  private const val KEY_PHOTO_VERSION = "custom_residence_photo_version"
  private const val CUSTOM_FILE_NAME = "residence_header_custom.jpg"
  private const val MAX_DECODE_DIMENSION_PX = 1440

  private val _photoVersion = MutableStateFlow(-1L)
  val photoVersion: StateFlow<Long> = _photoVersion.asStateFlow()

  fun ensureInitialized(context: Context) {
    if (_photoVersion.value < 0L) {
      val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      val file = customFile(context)
      val savedVersion = prefs.getLong(KEY_PHOTO_VERSION, 0L)
      _photoVersion.value = if (file.exists() && file.length() > 0L) max(savedVersion, 1L) else 0L
    }
  }

  private fun customFile(context: Context): File {
    return File(context.applicationContext.filesDir, CUSTOM_FILE_NAME)
  }

  suspend fun savePickedPhoto(context: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
    try {
      val appCtx = context.applicationContext
      val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
      appCtx.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, boundsOptions)
      }
      val rawW = boundsOptions.outWidth
      val rawH = boundsOptions.outHeight
      if (rawW <= 0 || rawH <= 0) return@withContext false

      var sampleSize = 1
      while (max(rawW / sampleSize, rawH / sampleSize) > MAX_DECODE_DIMENSION_PX * 2) {
        sampleSize *= 2
      }

      val decodeOptions = BitmapFactory.Options().apply {
        inSampleSize = sampleSize
        inPreferredConfig = Bitmap.Config.ARGB_8888
      }
      val decoded = appCtx.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, decodeOptions)
      } ?: return@withContext false

      val maxSide = max(decoded.width, decoded.height)
      val finalBitmap = if (maxSide > MAX_DECODE_DIMENSION_PX) {
        val ratio = MAX_DECODE_DIMENSION_PX.toFloat() / maxSide.toFloat()
        val targetW = (decoded.width * ratio).toInt().coerceAtLeast(1)
        val targetH = (decoded.height * ratio).toInt().coerceAtLeast(1)
        Bitmap.createScaledBitmap(decoded, targetW, targetH, true)
      } else {
        decoded
      }

      val dest = customFile(appCtx)
      FileOutputStream(dest).use { out ->
        finalBitmap.compress(Bitmap.CompressFormat.JPEG, 86, out)
        out.flush()
      }

      val now = System.currentTimeMillis()
      appCtx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putLong(KEY_PHOTO_VERSION, now)
        .apply()
      _photoVersion.value = now
      true
    } catch (_: Exception) {
      false
    }
  }

  suspend fun clearCustomPhoto(context: Context) = withContext(Dispatchers.IO) {
    val appCtx = context.applicationContext
    runCatching { customFile(appCtx).delete() }
    appCtx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      .edit()
      .putLong(KEY_PHOTO_VERSION, 0L)
      .apply()
    _photoVersion.value = 0L
  }

  suspend fun loadCustomBitmap(context: Context, version: Long): ImageBitmap? = withContext(Dispatchers.IO) {
    if (version <= 0L) return@withContext null
    val file = customFile(context)
    if (!file.exists() || file.length() <= 0L) return@withContext null
    runCatching {
      BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
    }.getOrNull()
  }
}

/**
 * En-tête visuel de la résidence affichant la photo personnalisée de l'immeuble (ou l'illustration
 * architecturale par défaut `img_residence_hero`), le nom de la résidence et un bouton direct
 * pour personnaliser l'image sans aucune permission système.
 */
@Composable
fun ResidenceHeroBanner(
  residenceName: String,
  subtitle: String,
  badgeText: String? = null,
  compact: Boolean = false,
  onPhotoFeedback: ((String) -> Unit)? = null,
  modifier: Modifier = Modifier,
  trailingActions: (@Composable RowScope.() -> Unit)? = null,
) {
  val context = LocalContext.current
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val scope = rememberCoroutineScope()

  LaunchedEffect(context) {
    ResidencePhotoManager.ensureInitialized(context)
  }

  val photoVersion by ResidencePhotoManager.photoVersion.collectAsStateWithLifecycle()
  val customBitmap by produceState<ImageBitmap?>(initialValue = null, context, photoVersion) {
    value = ResidencePhotoManager.loadCustomBitmap(context, photoVersion)
  }

  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia(),
  ) { uri ->
    if (uri != null) {
      scope.launch {
        val ok = ResidencePhotoManager.savePickedPhoto(context, uri)
        if (ok) {
          onPhotoFeedback?.invoke(strings.residencePhotoSavedMsg)
        }
      }
    }
  }

  val bannerHeight = if (compact) 156.dp else 208.dp
  val shape = RoundedCornerShape(tokens.featureCardRadiusDp.dp)

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .height(bannerHeight)
      .testTag("residence_hero_banner"),
    shape = shape,
    color = CoinbaseSurfaceDark,
    border = BorderStroke(1.dp, tokens.cardBorder),
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      val bmp = customBitmap
      if (bmp != null) {
        Image(
          bitmap = bmp,
          contentDescription = residenceName,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize(),
        )
      } else {
        Image(
          painter = painterResource(id = R.drawable.img_residence_hero),
          contentDescription = residenceName,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize(),
        )
      }

      // Dégradé sombre éditorial assurant un contraste optimal sur toute photo claire ou sombre
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              0.0f to CoinbaseSurfaceDark.copy(alpha = 0.36f),
              0.45f to CoinbaseSurfaceDark.copy(alpha = 0.42f),
              1.0f to CoinbaseSurfaceDark.copy(alpha = 0.88f),
            )
          ),
      )

      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.SpaceBetween,
      ) {
        // Barre supérieure : Badge résidence + actions (Personnaliser photo + actions écran)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          if (!badgeText.isNullOrBlank()) {
            Surface(
              shape = RoundedCornerShape(100.dp),
              color = CoinbaseSurfaceDark.copy(alpha = 0.62f),
              border = BorderStroke(1.dp, Color.White.copy(alpha = 0.24f)),
              tonalElevation = 0.dp,
              shadowElevation = 0.dp,
            ) {
              Text(
                text = badgeText,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
              )
            }
          } else {
            Spacer(modifier = Modifier.width(1.dp))
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            if (bmp != null) {
              HeroOverlayCircleButton(
                icon = Icons.Filled.RestartAlt,
                contentDescription = strings.residenceResetPhotoLabel,
                onClick = {
                  scope.launch {
                    ResidencePhotoManager.clearCustomPhoto(context)
                    onPhotoFeedback?.invoke(strings.residencePhotoResetMsg)
                  }
                },
                modifier = Modifier.testTag("reset_residence_photo_btn"),
              )
            }

            // Bouton pilule / icône pour choisir une photo personnalisée de la résidence
            Surface(
              shape = RoundedCornerShape(100.dp),
              color = CoinbaseSurfaceDark.copy(alpha = 0.68f),
              border = BorderStroke(1.dp, Color.White.copy(alpha = 0.30f)),
              tonalElevation = 0.dp,
              shadowElevation = 0.dp,
              modifier = Modifier
                .minimumInteractiveComponentSize()
                .clip(RoundedCornerShape(100.dp))
                .clickable {
                  photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                  )
                }
                .testTag("customize_residence_photo_btn"),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
              ) {
                Icon(
                  imageVector = Icons.Filled.AddAPhoto,
                  contentDescription = strings.residenceCustomizePhotoLabel,
                  tint = Color.White,
                  modifier = Modifier.size(16.dp),
                )
                if (!compact) {
                  Text(
                    text = strings.residenceCustomizePhotoShort,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                  )
                }
              }
            }

            if (trailingActions != null) {
              trailingActions()
            }
          }
        }

        // Partie basse : Nom de la résidence & sous-titre
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = residenceName,
            style = if (compact) {
              MaterialTheme.typography.titleLarge
            } else {
              MaterialTheme.typography.headlineSmall
            },
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
          if (subtitle.isNotBlank()) {
            Text(
              text = subtitle,
              style = MaterialTheme.typography.bodySmall,
              color = Color.White.copy(alpha = 0.86f),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }
    }
  }
}

@Composable
fun HeroOverlayCircleButton(
  icon: ImageVector,
  contentDescription: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier
      .minimumInteractiveComponentSize()
      .size(48.dp)
      .clip(CircleShape)
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center,
  ) {
    Surface(
      shape = CircleShape,
      color = CoinbaseSurfaceDark.copy(alpha = 0.68f),
      border = BorderStroke(1.dp, Color.White.copy(alpha = 0.30f)),
      tonalElevation = 0.dp,
      shadowElevation = 0.dp,
      modifier = Modifier.size(40.dp),
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
          imageVector = icon,
          contentDescription = contentDescription,
          tint = Color.White,
          modifier = Modifier.size(19.dp),
        )
      }
    }
  }
}
