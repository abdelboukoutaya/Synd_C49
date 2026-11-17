package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.local.BuildingProfile
import com.example.data.local.SpecialCallRecord
import com.example.data.local.SyndicatRoleMode
import com.example.ui.components.AirtableSoftCard
import com.example.ui.components.FintechTextField
import com.example.ui.components.FintechWhiteCard
import com.example.ui.components.MetaCobaltBuyButton
import com.example.ui.components.SageSecondaryButton
import com.example.ui.components.SyndicatDialog
import com.example.ui.components.SyndicatFilterChip
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.LocalSyndicatTokens
import com.example.ui.viewmodel.SyndicatDashboardData
import com.example.ui.viewmodel.formatDh
import com.example.util.CouncilQrCodeHelper
import com.example.util.ParsedSyndicatBackup
import com.example.util.SyndicatCouncilShareCodec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MultiBuildingManagementCard(
  buildings: List<BuildingProfile>,
  activeBuildingId: String,
  isReadOnlyMode: Boolean,
  onSelectBuilding: (String) -> Unit,
  onCreateBuilding: (name: String, city: String) -> Unit,
  onDeleteBuilding: (String) -> Unit,
) {
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  var showAddDialog by remember { mutableStateOf(false) }

  AirtableSoftCard(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("settings_multi_building_card"),
    padding = 24.dp,
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Text(
        text = strings.multiBuildingTitle,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Normal,
        color = tokens.inkDeep,
      )
      Text(
        text = strings.multiBuildingSub,
        style = MaterialTheme.typography.bodySmall,
        color = tokens.body,
      )

      buildings.forEach { b ->
        val isActive = b.id == activeBuildingId
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onSelectBuilding(b.id) }
            .testTag("switch_building_${b.id}"),
          color = if (isActive) tokens.primaryTint.copy(alpha = 0.65f) else tokens.surfaceSoft,
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(
            width = if (isActive) 1.5.dp else 1.dp,
            color = if (isActive) tokens.primary else tokens.hairline,
          ),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(min = 48.dp)
              .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
              Text(
                text = b.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                color = tokens.inkDeep,
              )
              if (b.city.isNotBlank()) {
                Text(
                  text = b.city,
                  style = MaterialTheme.typography.bodySmall,
                  color = tokens.body,
                )
              }
            }
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              if (isActive) {
                Surface(
                  color = tokens.primary,
                  shape = RoundedCornerShape(999.dp),
                ) {
                  Text(
                    text = strings.multiBuildingActiveBadge,
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.onPrimary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                  )
                }
              }
              if (buildings.size > 1 && b.id != "default" && !isReadOnlyMode) {
                IconButton(
                  onClick = { onDeleteBuilding(b.id) },
                  modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("delete_building_${b.id}"),
                ) {
                  Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = strings.paymentDeleteDesc,
                    tint = tokens.negative,
                    modifier = Modifier.size(18.dp),
                  )
                }
              }
            }
          }
        }
      }

      if (!isReadOnlyMode) {
        SageSecondaryButton(
          text = strings.multiBuildingAddBtn,
          compact = true,
          onClick = { showAddDialog = true },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("add_building_btn"),
        )
      }
    }
  }

  if (showAddDialog) {
    var name by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    SyndicatDialog(
      onDismissRequest = { showAddDialog = false },
      title = {
        Text(
          text = strings.multiBuildingAddBtn,
          style = MaterialTheme.typography.titleLarge,
          color = tokens.inkDeep,
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          FintechTextField(
            value = name,
            onValueChange = { name = it },
            label = strings.settingsResidenceName,
            modifier = Modifier.fillMaxWidth().testTag("new_building_name_input"),
          )
          FintechTextField(
            value = city,
            onValueChange = { city = it },
            label = strings.settingsCity,
            modifier = Modifier.fillMaxWidth().testTag("new_building_city_input"),
          )
        }
      },
      confirmButton = {
        MetaCobaltBuyButton(
          text = strings.settingsConfirmBtn,
          compact = true,
          enabled = name.isNotBlank(),
          onClick = {
            onCreateBuilding(name.trim(), city.trim())
            showAddDialog = false
          },
          modifier = Modifier.testTag("confirm_add_building_btn"),
        )
      },
      dismissButton = {
        SageSecondaryButton(
          text = strings.apptCancel,
          compact = true,
          onClick = { showAddDialog = false },
        )
      },
    )
  }
}

private data class CouncilQrDisplayState(
  val qrBitmap: Bitmap,
  val summaryText: String,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CouncilShareCard(
  data: SyndicatDashboardData,
  specialCalls: List<SpecialCallRecord>,
  roleMode: SyndicatRoleMode,
  currentUserId: String,
  onSetRoleMode: (SyndicatRoleMode) -> Unit,
  onImportSnapshot: (ParsedSyndicatBackup) -> Unit,
  onMessage: (String) -> Unit,
) {
  val context = LocalContext.current
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val scope = rememberCoroutineScope()

  var generatedQrState by remember { mutableStateOf<CouncilQrDisplayState?>(null) }
  var showImportDialog by remember { mutableStateOf(false) }
  var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

  fun importFromPayload(rawPayload: String) {
    runCatching {
      SyndicatCouncilShareCodec.decodeShareCode(rawPayload, currentUserId)
    }.onSuccess { parsed ->
      onImportSnapshot(parsed)
      onSetRoleMode(SyndicatRoleMode.READ_ONLY_COUNCIL)
      showImportDialog = false
      onMessage(strings.councilQrImportSuccess)
    }.onFailure { err ->
      onMessage(err.localizedMessage ?: strings.councilQrInvalidError)
    }
  }

  fun decodeFromUri(uri: Uri) {
    scope.launch {
      val decodedText = withContext(Dispatchers.IO) {
        runCatching { CouncilQrCodeHelper.decodeQrFromUri(context, uri) }.getOrNull()
      }
      if (decodedText.isNullOrBlank()) {
        onMessage(strings.councilQrInvalidError)
      } else {
        importFromPayload(decodedText)
      }
    }
  }

  val qrImagePicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia(),
  ) { uri ->
    if (uri != null) {
      decodeFromUri(uri)
    }
  }

  val cameraCaptureLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicture(),
  ) { success ->
    val uri = pendingCameraUri
    pendingCameraUri = null
    if (success && uri != null) {
      decodeFromUri(uri)
    }
  }

  val cameraPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
  ) { granted ->
    if (granted) {
      runCatching { CouncilQrCodeHelper.createCameraTempUri(context) }
        .onSuccess { uri ->
          pendingCameraUri = uri
          cameraCaptureLauncher.launch(uri)
        }
        .onFailure { err ->
          onMessage(err.localizedMessage ?: strings.councilQrInvalidError)
        }
    } else {
      onMessage(strings.councilQrCameraDenied)
    }
  }

  AirtableSoftCard(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("settings_council_share_card"),
    padding = 24.dp,
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Text(
        text = strings.councilShareTitle,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Normal,
        color = tokens.inkDeep,
      )
      Text(
        text = strings.councilShareSub,
        style = MaterialTheme.typography.bodySmall,
        color = tokens.body,
      )

      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        SyndicatFilterChip(
          selected = roleMode == SyndicatRoleMode.ADMIN,
          onClick = { onSetRoleMode(SyndicatRoleMode.ADMIN) },
          label = strings.councilModeAdmin,
          modifier = Modifier.testTag("role_mode_admin_chip"),
        )
        SyndicatFilterChip(
          selected = roleMode == SyndicatRoleMode.READ_ONLY_COUNCIL,
          onClick = { onSetRoleMode(SyndicatRoleMode.READ_ONLY_COUNCIL) },
          label = strings.councilModeReadOnly,
          modifier = Modifier.testTag("role_mode_readonly_chip"),
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        SageSecondaryButton(
          text = strings.councilGenerateCodeBtn,
          icon = Icons.Default.QrCode2,
          compact = true,
          onClick = {
            runCatching {
              val payload = SyndicatCouncilShareCodec.encodeShareCode(data, specialCalls)
              val qrBitmap = CouncilQrCodeHelper.encodeShareQrBitmap(payload)
              val cashText = formatDh(data.cashNowDh, data.config.currency, data.config.digitsAr)
              val summary = "${strings.dashUnitsSummary(data.upToDateCount, data.occupiedCount)} · $cashText"
              CouncilQrDisplayState(qrBitmap = qrBitmap, summaryText = summary)
            }.onSuccess { qrState ->
              generatedQrState = qrState
            }.onFailure { err ->
              onMessage(err.localizedMessage ?: "Erreur QR Code")
            }
          },
          modifier = Modifier.weight(1f).testTag("generate_council_code_btn"),
        )
        SageSecondaryButton(
          text = strings.councilImportCodeBtn,
          icon = Icons.Default.QrCodeScanner,
          compact = true,
          onClick = { showImportDialog = true },
          modifier = Modifier.weight(1f).testTag("import_council_code_btn"),
        )
      }
    }
  }

  generatedQrState?.let { qrState ->
    SyndicatDialog(
      onDismissRequest = { generatedQrState = null },
      title = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Icon(
            imageVector = Icons.Default.QrCode2,
            contentDescription = null,
            tint = tokens.primary,
          )
          Text(
            text = strings.councilQrDialogTitle,
            style = MaterialTheme.typography.titleLarge,
            color = tokens.inkDeep,
          )
        }
      },
      text = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("council_qr_dialog"),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          FintechWhiteCard(
            modifier = Modifier.fillMaxWidth(),
            padding = 12.dp,
          ) {
            Column(
              modifier = Modifier.fillMaxWidth(),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
              Text(
                text = "${data.config.residenceName} · ${data.baseYear}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = tokens.inkDeep,
                textAlign = TextAlign.Center,
              )
              Text(
                text = qrState.summaryText,
                style = MaterialTheme.typography.bodySmall,
                color = tokens.body,
                textAlign = TextAlign.Center,
              )
            }
          }

          Surface(
            color = Color.White,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, tokens.hairline),
            shadowElevation = 2.dp,
          ) {
            Box(
              modifier = Modifier
                .background(Color.White)
                .padding(14.dp),
              contentAlignment = Alignment.Center,
            ) {
              Image(
                bitmap = qrState.qrBitmap.asImageBitmap(),
                contentDescription = strings.councilQrImageDesc,
                filterQuality = FilterQuality.None,
                modifier = Modifier
                  .size(228.dp)
                  .testTag("council_qr_image"),
              )
            }
          }

          Text(
            text = strings.councilQrReadOnlyHint,
            style = MaterialTheme.typography.bodySmall,
            color = tokens.body,
            textAlign = TextAlign.Center,
          )

          MetaCobaltBuyButton(
            text = strings.councilQrShareImageBtn,
            icon = Icons.Default.Share,
            compact = true,
            onClick = {
              val card = CouncilQrCodeHelper.buildShareableQrCardBitmap(
                qrBitmap = qrState.qrBitmap,
                residenceName = data.config.residenceName,
                year = data.baseYear,
                summaryText = qrState.summaryText,
                badgeTitle = strings.councilQrDialogTitle,
                footerHint = strings.councilQrCardFooter,
              )
              CouncilQrCodeHelper.shareQrCardPng(
                context = context,
                cardBitmap = card,
                residenceName = data.config.residenceName,
                chooserTitle = strings.councilShareTitle,
                onError = onMessage,
              )
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("share_council_qr_image_btn"),
          )
        }
      },
      confirmButton = {
        SageSecondaryButton(
          text = strings.posterPreviewClose,
          compact = true,
          onClick = { generatedQrState = null },
          modifier = Modifier.testTag("close_council_qr_dialog_btn"),
        )
      },
    )
  }

  if (showImportDialog) {
    var showManualFallback by remember { mutableStateOf(false) }
    var codeInput by remember { mutableStateOf("") }

    SyndicatDialog(
      onDismissRequest = { showImportDialog = false },
      title = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Icon(
            imageVector = Icons.Default.QrCodeScanner,
            contentDescription = null,
            tint = tokens.primary,
          )
          Text(
            text = strings.councilImportCodeBtn,
            style = MaterialTheme.typography.titleLarge,
            color = tokens.inkDeep,
          )
        }
      },
      text = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("council_qr_import_dialog"),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Text(
            text = strings.councilQrImportSub,
            style = MaterialTheme.typography.bodySmall,
            color = tokens.body,
          )

          MetaCobaltBuyButton(
            text = strings.councilQrScanCameraBtn,
            icon = Icons.Default.CameraAlt,
            compact = true,
            onClick = {
              val hasCameraPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA,
              ) == PackageManager.PERMISSION_GRANTED
              if (hasCameraPermission) {
                runCatching { CouncilQrCodeHelper.createCameraTempUri(context) }
                  .onSuccess { uri ->
                    pendingCameraUri = uri
                    cameraCaptureLauncher.launch(uri)
                  }
                  .onFailure { err ->
                    onMessage(err.localizedMessage ?: strings.councilQrInvalidError)
                  }
              } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("scan_council_qr_camera_btn"),
          )

          SageSecondaryButton(
            text = strings.councilQrPickImageBtn,
            icon = Icons.Default.PhotoLibrary,
            compact = true,
            onClick = {
              qrImagePicker.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
              )
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("pick_council_qr_image_btn"),
          )

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .clickable { showManualFallback = !showManualFallback }
              .minimumInteractiveComponentSize()
              .padding(horizontal = 4.dp, vertical = 6.dp)
              .testTag("toggle_manual_council_code_btn"),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = strings.councilQrManualToggle,
              style = MaterialTheme.typography.labelMedium,
              color = tokens.primary,
            )
          }

          if (showManualFallback) {
            FintechTextField(
              value = codeInput,
              onValueChange = { codeInput = it },
              label = "SYNDICAT-CONSEIL-1:...",
              singleLine = false,
              minLines = 2,
              maxLines = 4,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("council_code_input"),
            )
          }
        }
      },
      confirmButton = {
        if (showManualFallback) {
          MetaCobaltBuyButton(
            text = strings.settingsConfirmBtn,
            compact = true,
            enabled = codeInput.isNotBlank(),
            onClick = { importFromPayload(codeInput) },
            modifier = Modifier.testTag("confirm_import_council_code_btn"),
          )
        }
      },
      dismissButton = {
        SageSecondaryButton(
          text = strings.apptCancel,
          compact = true,
          onClick = { showImportDialog = false },
        )
      },
    )
  }
}
