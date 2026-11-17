package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.IncidentRecord
import com.example.data.local.IncidentSeverity
import com.example.data.local.IncidentStatus
import com.example.ui.components.FintechTextField
import com.example.ui.components.FintechWhiteCard
import com.example.ui.components.MetaCobaltBuyButton
import com.example.ui.components.SageSecondaryButton
import com.example.ui.components.SyndicatDialog
import com.example.ui.components.SyndicatFilterChip
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.LocalSyndicatTokens

@Composable
fun IncidentItemCard(
  incident: IncidentRecord,
  isReadOnlyMode: Boolean,
  onCycleStatus: () -> Unit,
  onConvertToExpense: () -> Unit,
  onDelete: () -> Unit,
) {
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current

  val sevLabel = when (incident.severity) {
    IncidentSeverity.LOW -> strings.incidentSeverityLow
    IncidentSeverity.MEDIUM -> strings.incidentSeverityMedium
    IncidentSeverity.HIGH -> strings.incidentSeverityUrgent
  }
  val sevColor = when (incident.severity) {
    IncidentSeverity.LOW -> tokens.linkColor
    IncidentSeverity.MEDIUM -> tokens.warningContent
    IncidentSeverity.HIGH -> tokens.negativeDeep
  }
  val statusLabel = when (incident.status) {
    IncidentStatus.REPORTED -> strings.incidentStatusOpen
    IncidentStatus.IN_PROGRESS -> strings.incidentStatusInProgress
    IncidentStatus.RESOLVED -> strings.incidentStatusResolved
  }
  val statusBg = when (incident.status) {
    IncidentStatus.REPORTED -> tokens.negativeBg
    IncidentStatus.IN_PROGRESS -> tokens.warning.copy(alpha = 0.15f)
    IncidentStatus.RESOLVED -> tokens.positive.copy(alpha = 0.15f)
  }
  val statusFg = when (incident.status) {
    IncidentStatus.REPORTED -> tokens.negativeDeep
    IncidentStatus.IN_PROGRESS -> tokens.warningContent
    IncidentStatus.RESOLVED -> tokens.positiveDeep
  }

  FintechWhiteCard(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("incident_card_${incident.id}"),
    padding = 16.dp,
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Icon(
            imageVector = Icons.Default.ReportProblem,
            contentDescription = null,
            tint = sevColor,
            modifier = Modifier.size(18.dp),
          )
          Text(
            text = sevLabel,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = sevColor,
          )
          if (incident.reportedDate.isNotBlank()) {
            Text(
              text = "· ${incident.reportedDate}",
              style = MaterialTheme.typography.labelSmall,
              color = tokens.mute,
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(100.dp),
          color = statusBg,
          border = BorderStroke(1.dp, statusFg.copy(alpha = 0.35f)),
          modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .then(if (!isReadOnlyMode) Modifier.clickable(onClick = onCycleStatus) else Modifier)
            .testTag("incident_status_${incident.id}"),
        ) {
          Box(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) {
            Text(
              text = statusLabel,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = statusFg,
            )
          }
        }
      }

      Text(
        text = incident.title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = tokens.inkDeep,
      )
      if (incident.locationFloor.isNotBlank()) {
        Text(
          text = incident.locationFloor,
          style = MaterialTheme.typography.labelLarge,
          color = tokens.linkColor,
        )
      }
      if (incident.description.isNotBlank()) {
        Text(
          text = incident.description,
          style = MaterialTheme.typography.bodySmall,
          color = tokens.body,
        )
      }

      if (!isReadOnlyMode) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          if (incident.linkedExpenseId.isBlank()) {
            SageSecondaryButton(
              text = strings.incidentConvertExpenseBtn,
              icon = Icons.Default.Payments,
              compact = true,
              onClick = onConvertToExpense,
              modifier = Modifier
                .weight(1f)
                .testTag("convert_incident_expense_${incident.id}"),
            )
          } else {
            Surface(
              shape = RoundedCornerShape(100.dp),
              color = tokens.positive.copy(alpha = 0.15f),
              modifier = Modifier.weight(1f),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.Build,
                  contentDescription = null,
                  tint = tokens.positiveDeep,
                  modifier = Modifier.size(16.dp),
                )
                Text(
                  text = "${strings.incidentStatusResolved} · ✓",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = tokens.positiveDeep,
                )
              }
            }
          }
          IconButton(
            onClick = onDelete,
            modifier = Modifier.size(48.dp),
          ) {
            Icon(
              imageVector = Icons.Default.DeleteOutline,
              contentDescription = strings.expDelete,
              tint = tokens.negativeDeep,
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IncidentEditorDialog(
  onDismiss: () -> Unit,
  onSave: (IncidentRecord) -> Unit,
) {
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  var title by remember { mutableStateOf("") }
  var floorLoc by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var severity by remember { mutableStateOf(IncidentSeverity.MEDIUM) }

  SyndicatDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = strings.incidentAddBtn,
        style = MaterialTheme.typography.titleLarge,
        color = tokens.inkDeep,
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        FintechTextField(
          value = title,
          onValueChange = { title = it.take(140) },
          label = strings.expFieldTitle,
          placeholder = "Ex: Fuite colonne d'eau 3e étage",
          modifier = Modifier.fillMaxWidth().testTag("incident_title_input"),
        )
        FintechTextField(
          value = floorLoc,
          onValueChange = { floorLoc = it.take(60) },
          label = strings.colFloor,
          placeholder = "Ex: Étage 3 / Ascenseur",
          modifier = Modifier.fillMaxWidth().testTag("incident_location_input"),
        )
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          listOf(
            IncidentSeverity.LOW to strings.incidentSeverityLow,
            IncidentSeverity.MEDIUM to strings.incidentSeverityMedium,
            IncidentSeverity.HIGH to strings.incidentSeverityUrgent,
          ).forEach { (sev, lbl) ->
            SyndicatFilterChip(
              selected = severity == sev,
              onClick = { severity = sev },
              label = lbl,
            )
          }
        }
        FintechTextField(
          value = description,
          onValueChange = { description = it.take(360) },
          label = strings.expFieldNotes,
          singleLine = false,
          minLines = 2,
          maxLines = 4,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    },
    confirmButton = {
      MetaCobaltBuyButton(
        text = strings.apptSave,
        compact = true,
        enabled = title.isNotBlank(),
        onClick = {
          onSave(
            IncidentRecord(
              id = "",
              title = title.trim(),
              locationFloor = floorLoc.trim(),
              description = description.trim(),
              severity = severity,
              status = IncidentStatus.REPORTED,
            ),
          )
        },
        modifier = Modifier.testTag("save_incident_btn"),
      )
    },
    dismissButton = {
      SageSecondaryButton(text = strings.apptCancel, compact = true, onClick = onDismiss)
    },
  )
}
