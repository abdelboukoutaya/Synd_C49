package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.minimumInteractiveComponentSize
import com.example.data.local.AgMinutesDraft
import com.example.data.local.AgResolutionRecord
import com.example.data.local.IncidentRecord
import com.example.data.local.SpecialCallRecord
import com.example.data.model.PROJECT_CATEGORIES
import com.example.data.model.PaymentBadgeStatus
import com.example.data.model.ProjectCaisseReadiness
import com.example.data.model.ProjectPriority
import com.example.data.model.ProjectRecord
import com.example.data.model.ProjectStatus
import com.example.util.ExportVariant
import com.example.util.PdfPosterExporter
import com.example.util.ProjectsExportOptions
import com.example.util.SyndicatReportsExporter
import com.example.ui.components.AirtableCreamCalloutCard
import com.example.ui.components.AirtablePastelKpiCard
import com.example.ui.components.AirtableSignatureCard
import com.example.ui.components.AirtableSoftCard
import com.example.ui.components.FintechTextField
import com.example.ui.components.FintechWhiteCard
import com.example.ui.components.LimePrimaryButton
import com.example.ui.components.LocalPrivacyMode
import com.example.ui.components.MetaCobaltBuyButton
import com.example.ui.components.MetaSegmentedThreePillRow
import com.example.ui.components.ReadOnlyCouncilBanner
import com.example.ui.components.RoundIconCircleButton
import com.example.ui.components.SageSecondaryButton
import com.example.ui.components.StatusPillBadge
import com.example.ui.components.SyndicatDialog
import com.example.ui.components.SyndicatEmptyStateCard
import com.example.ui.components.SyndicatFilterChip
import com.example.ui.components.SyndicatProgressBar
import com.example.ui.components.maskSensitiveDh
import com.example.ui.components.maskSensitiveName
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalStrings
import com.example.ui.i18n.bidiLtr
import com.example.ui.i18n.formatNumber
import com.example.ui.i18n.localizeDigits
import com.example.ui.theme.LocalSyndicatTokens
import com.example.ui.theme.MetaCobaltTintLight
import com.example.ui.theme.MetaCritical
import com.example.ui.theme.MetaInkDeep
import com.example.ui.theme.MetaPrimaryCobalt
import com.example.ui.theme.MetaSuccess
import com.example.ui.theme.MetaSuccessTintLight
import com.example.ui.theme.MetaWarning
import com.example.ui.viewmodel.SyndicatDashboardData
import com.example.ui.viewmodel.formatDh
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private enum class ProjectFilterTab {
  ALL,
  IN_PROGRESS,
  PLANNED,
  COMPLETED,
}

private enum class ProjectsSubTab {
  WORKS,
  CALLS,
  INCIDENTS,
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProjectsScreen(
  data: SyndicatDashboardData,
  onAddProject: (ProjectRecord) -> Unit,
  onUpdateProject: (ProjectRecord, ProjectRecord?) -> Unit,
  onDeleteProject: (ProjectRecord) -> Unit,
  onLoadStarterProjects: () -> Unit,
  specialCalls: List<SpecialCallRecord> = emptyList(),
  onCreateSpecialCall: (titleFr: String, titleAr: String, amountPerAptDh: Long, projectId: String, dueDateLabel: String) -> Unit = { _, _, _, _, _ -> },
  onToggleSpecialCallPayment: (callId: String, aptNum: Long, paid: Boolean) -> Unit = { _, _, _ -> },
  onDeleteSpecialCall: (callId: String) -> Unit = {},
  onExportError: (String) -> Unit = {},
  initialFocusSpecialCalls: Boolean = false,
  incidents: List<IncidentRecord> = emptyList(),
  onUpsertIncident: (IncidentRecord) -> Unit = {},
  onCycleIncidentStatus: (String) -> Unit = {},
  onDeleteIncident: (String) -> Unit = {},
  onConvertIncidentToExpense: (IncidentRecord) -> Unit = {},
  agMinutes: AgMinutesDraft = AgMinutesDraft(),
  onSaveAgMinutes: (AgMinutesDraft) -> Unit = {},
  isReadOnlyMode: Boolean = false,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val haptics = LocalHapticFeedback.current
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val isPrivacy = LocalPrivacyMode.current
  val cfg = data.config
  val cur = cfg.currency
  val dig = cfg.digitsAr
  val isAr = strings.language == AppLanguage.AR
  val numericFont = tokens.numericFontFamily ?: tokens.monoFontFamily

  var subTab by rememberSaveable(initialFocusSpecialCalls) {
    mutableStateOf(if (initialFocusSpecialCalls) ProjectsSubTab.CALLS else ProjectsSubTab.WORKS)
  }
  LaunchedEffect(initialFocusSpecialCalls) {
    if (initialFocusSpecialCalls) subTab = ProjectsSubTab.CALLS
  }
  var selectedTab by rememberSaveable { mutableStateOf(ProjectFilterTab.ALL) }
  var showEditorDialog by remember { mutableStateOf(false) }
  var editingProject by remember { mutableStateOf<ProjectRecord?>(null) }
  var showExportDialog by remember { mutableStateOf(false) }
  var showNewSpecialCallDialog by remember { mutableStateOf(false) }
  var trackingSpecialCallId by remember { mutableStateOf<String?>(null) }
  var showQuorumDialog by remember { mutableStateOf(false) }
  var showIncidentDialog by remember { mutableStateOf(false) }
  var localAgMinutes by remember(agMinutes) { mutableStateOf(agMinutes) }

  val ongoingList = data.inProgressProjects
  val plannedReadiness = data.plannedReadinessList
  val completedList = data.completedProjects

  if (showIncidentDialog && !isReadOnlyMode) {
    IncidentEditorDialog(
      onDismiss = { showIncidentDialog = false },
      onSave = { inc ->
        onUpsertIncident(inc)
        showIncidentDialog = false
      },
    )
  }

  if (showQuorumDialog) {
    AgQuorumVoteDialog(
      data = data,
      apartments = data.apartments.filter { !it.isVacantForConfig(cfg) },
      isAr = isAr,
      digitsAr = dig,
      draft = localAgMinutes,
      isReadOnlyMode = isReadOnlyMode,
      onUpdateDraft = { updated ->
        localAgMinutes = updated
        onSaveAgMinutes(updated)
      },
      onReset = {
        val resetDraft = localAgMinutes.copy(presentAptNumbers = emptySet())
        localAgMinutes = resetDraft
        onSaveAgMinutes(resetDraft)
      },
      onExportPvPdf = { mono ->
        SyndicatReportsExporter.exportAgMinutesPdf(
          context = context,
          data = data,
          draft = localAgMinutes,
          monochromeMode = mono,
          onError = onExportError,
        )
      },
      onDismiss = { showQuorumDialog = false },
    )
  }

  if (showExportDialog) {
    ProjectsExportOptionsDialog(
      onDismiss = { showExportDialog = false },
      onExportPdf = { monoMode ->
        showExportDialog = false
        PdfPosterExporter.shareProjectsReportPdf(
          context = context,
          data = data,
          options = ProjectsExportOptions(
            monochromeMode = monoMode,
            variant = ExportVariant.PRINT,
          ),
          onError = onExportError,
        )
      },
      onExportPng = { monoMode ->
        showExportDialog = false
        PdfPosterExporter.shareProjectsReportImage(
          context = context,
          data = data,
          options = ProjectsExportOptions(
            monochromeMode = monoMode,
            variant = ExportVariant.SCREEN,
          ),
          onError = onExportError,
        )
      },
    )
  }

  if (showNewSpecialCallDialog) {
    SpecialCallEditorDialog(
      projects = data.projects,
      currency = cur,
      isAr = isAr,
      onDismiss = { showNewSpecialCallDialog = false },
      onSave = { titleFr, titleAr, amountDh, projId, dueLabel ->
        onCreateSpecialCall(titleFr, titleAr, amountDh, projId, dueLabel)
        showNewSpecialCallDialog = false
      },
    )
  }

  trackingSpecialCallId?.let { targetId ->
    val activeCall = specialCalls.firstOrNull { it.id == targetId }
    if (activeCall == null) {
      trackingSpecialCallId = null
    } else {
      SpecialCallApartmentsDialog(
        call = activeCall,
        apartments = activeCall.eligibleApartments(data.apartments),
        currency = cur,
        digitsAr = dig,
        isAr = isAr,
        onDismiss = { trackingSpecialCallId = null },
        onToggleAptPaid = { aptNum, paid ->
          onToggleSpecialCallPayment(activeCall.id, aptNum, paid)
        },
      )
    }
  }

  if (showEditorDialog) {
    ProjectEditorDialog(
      initialProject = editingProject,
      currency = cur,
      onDismiss = {
        showEditorDialog = false
        editingProject = null
      },
      onSave = { saved ->
        val prev = editingProject
        if (prev == null) {
          onAddProject(saved)
        } else {
          onUpdateProject(saved, prev)
        }
        showEditorDialog = false
        editingProject = null
      },
      onDelete = editingProject?.let { existing ->
        {
          onDeleteProject(existing)
          showEditorDialog = false
          editingProject = null
        }
      },
    )
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(tokens.screenBg)
      .testTag("projects_screen"),
    contentPadding = PaddingValues(
      start = 16.dp,
      end = 16.dp,
      top = 16.dp,
      bottom = 32.dp,
    ),
    verticalArrangement = Arrangement.spacedBy(20.dp),
  ) {
    // 1. Meta Editorial Header + Overview Primary Black Pill (« إضافة مشروع / Nouveau projet »)
    item {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (isReadOnlyMode) {
          ReadOnlyCouncilBanner()
        }
        Text(
          text = strings.projectsPageTitle,
          style = MaterialTheme.typography.headlineLarge,
          fontWeight = FontWeight.Medium,
          color = tokens.inkDeep,
        )
        Text(
          text = strings.projectsPageSub,
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Light,
          color = tokens.body,
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          if (!isReadOnlyMode) {
            LimePrimaryButton(
              text = strings.projectsAddBtn,
              icon = Icons.Default.Add,
              onClick = {
                editingProject = null
                showEditorDialog = true
              },
              modifier = Modifier
                .weight(1.3f)
                .testTag("add_project_fab"),
            )
          }
          SageSecondaryButton(
            text = strings.exportPdfPrimaryBtn,
            icon = Icons.Default.PictureAsPdf,
            onClick = { showExportDialog = true },
            modifier = Modifier
              .weight(1f)
              .testTag("export_projects_top_action_pdf"),
          )
        }
        val callsBadgeSuffix = if (specialCalls.isNotEmpty()) {
          " (${formatNumber(specialCalls.size.toLong(), dig)})"
        } else {
          ""
        }
        val incBadgeSuffix = if (incidents.isNotEmpty()) {
          " (${formatNumber(incidents.size.toLong(), dig)})"
        } else {
          ""
        }
        MetaSegmentedThreePillRow(
          labels = Triple(
            strings.projectsViewWorksTab,
            "${strings.projectsViewCallsTab}$callsBadgeSuffix",
            "${strings.projectsViewIncidentsTab}$incBadgeSuffix",
          ),
          selectedIndex = subTab.ordinal,
          onSelectIndex = { idx -> subTab = ProjectsSubTab.entries[idx] },
          tags = Triple("projects_tab_works", "projects_tab_calls", "projects_tab_incidents"),
          modifier = Modifier.fillMaxWidth(),
        )
        SageSecondaryButton(
          text = strings.agQuorumOpenBtn,
          icon = Icons.Default.Groups,
          compact = true,
          onClick = { showQuorumDialog = true },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("ag_quorum_btn"),
        )
      }
    }

    // 2. Meta Showcase Card (#0A1317 32dp) + 2 Soft Cloud KPI Tiles (24dp)
    if (subTab == ProjectsSubTab.WORKS) {
    item {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AirtableSignatureCard(
          backgroundColor = tokens.signatureForest,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("projects_caisse_card"),
          padding = 24.dp,
        ) {
          Text(
            text = strings.projectsKpiCaisseLabel,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Light,
            color = Color.White.copy(alpha = 0.90f),
          )
          Spacer(modifier = Modifier.height(6.dp))
          val cashText = formatDh(data.cashNowDh, cur, dig)
          Text(
            text = maskSensitiveDh(cashText, isPrivacy),
            style = MaterialTheme.typography.displayMedium.copy(
              fontFamily = numericFont,
              fontFeatureSettings = "tnum, ss01, ss02",
            ),
            fontWeight = FontWeight.Medium,
            color = Color.White,
            modifier = Modifier.testTag("projects_caisse_value"),
          )
        }

        val avgOngoingPct = if (ongoingList.isEmpty()) {
          0
        } else {
          ongoingList.map { it.clampedProgressPercent }.average().toInt()
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          AirtablePastelKpiCard(
            backgroundColor = MetaPrimaryCobalt,
            label = strings.projectsKpiOngoingLabel,
            valueText = formatNumber(ongoingList.size.toLong(), dig),
            subtitle = if (ongoingList.isNotEmpty()) {
              "${strings.projectProgressStateLabel}: ${bidiLtr("${localizeDigits(avgOngoingPct.toString(), dig)}%")}"
            } else {
              "—"
            },
            modifier = Modifier
              .weight(1f)
              .testTag("projects_kpi_ongoing"),
          )
          AirtablePastelKpiCard(
            backgroundColor = MetaSuccess,
            label = strings.projectsKpiReadyLabel,
            valueText = formatNumber(data.readyToStartProjectsCount.toLong(), dig),
            subtitle = strings.projectsKpiPlannedSub(plannedReadiness.size),
            modifier = Modifier
              .weight(1f)
              .testTag("projects_kpi_ready"),
          )
        }
      }
    }

    // 3. Filtres par statut (Tous / En cours / Futurs / Terminés)
    item {
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        SyndicatFilterChip(
          selected = selectedTab == ProjectFilterTab.ALL,
          onClick = { selectedTab = ProjectFilterTab.ALL },
          label = strings.projectsFilterAll(data.projects.size),
          modifier = Modifier.testTag("projects_filter_all"),
        )
        SyndicatFilterChip(
          selected = selectedTab == ProjectFilterTab.IN_PROGRESS,
          onClick = { selectedTab = ProjectFilterTab.IN_PROGRESS },
          label = strings.projectsFilterOngoing(ongoingList.size),
          modifier = Modifier.testTag("projects_filter_ongoing"),
        )
        SyndicatFilterChip(
          selected = selectedTab == ProjectFilterTab.PLANNED,
          onClick = { selectedTab = ProjectFilterTab.PLANNED },
          label = strings.projectsFilterPlanned(plannedReadiness.size),
          modifier = Modifier.testTag("projects_filter_planned"),
        )
        SyndicatFilterChip(
          selected = selectedTab == ProjectFilterTab.COMPLETED,
          onClick = { selectedTab = ProjectFilterTab.COMPLETED },
          label = strings.projectsFilterCompleted(completedList.size),
          modifier = Modifier.testTag("projects_filter_completed"),
        )
      }
    }

    // État vide si aucun projet
    if (data.projects.isEmpty()) {
      item {
        SyndicatEmptyStateCard(
          icon = Icons.Default.Construction,
          title = strings.projectsEmptyTitle,
          subtitle = strings.projectsEmptySub,
          actionLabel = strings.projectsLoadSamplesBtn,
          actionIcon = Icons.Default.Construction,
          actionTag = "load_sample_projects_btn",
          onAction = onLoadStarterProjects,
          modifier = Modifier.testTag("projects_empty_state"),
        )
      }
    }

    // 4. SECTION 1 : Projet(s) en cours avec état d'avancement (triés par priorité)
    val showOngoing = (selectedTab == ProjectFilterTab.ALL || selectedTab == ProjectFilterTab.IN_PROGRESS) &&
      ongoingList.isNotEmpty()
    if (showOngoing) {
      item {
        Text(
          text = strings.projectsOngoingSectionTitle,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Normal,
          color = tokens.ink,
          modifier = Modifier.testTag("ongoing_projects_header"),
        )
      }

      items(
        items = ongoingList,
        key = { "ongoing_${it.id}" },
      ) { proj ->
        OngoingProjectCard(
          project = proj,
          isAr = isAr,
          currency = cur,
          digitsAr = dig,
          onIncrementProgress = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            val nextPct = (proj.clampedProgressPercent + 10).coerceAtMost(100)
            val nextStatus = if (nextPct >= 100) ProjectStatus.COMPLETED.code else ProjectStatus.IN_PROGRESS.code
            val nextAllocated = if (proj.targetBudgetDh > 0L) {
              maxOf(proj.allocatedAmountDh, (proj.targetBudgetDh * nextPct.toLong()) / 100L)
            } else {
              proj.allocatedAmountDh
            }
            onUpdateProject(
              proj.copy(
                progressPercent = nextPct.toLong(),
                status = nextStatus,
                allocatedAmountDh = nextAllocated,
              ),
              proj,
            )
          },
          onMarkCompleted = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onUpdateProject(
              proj.copy(
                status = ProjectStatus.COMPLETED.code,
                progressPercent = 100L,
                allocatedAmountDh = maxOf(proj.allocatedAmountDh, proj.targetBudgetDh),
              ),
              proj,
            )
          },
          onEdit = {
            editingProject = proj
            showEditorDialog = true
          },
        )
      }
    }

    // 5. SECTION 2 : Projets futurs triés par priorité selon l'avance de caisse pour débuter
    val showPlanned = (selectedTab == ProjectFilterTab.ALL || selectedTab == ProjectFilterTab.PLANNED) &&
      plannedReadiness.isNotEmpty()
    if (showPlanned) {
      item {
        AirtableCreamCalloutCard(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("planned_projects_header"),
          padding = 20.dp,
        ) {
          Text(
            text = strings.projectsFutureSectionTitle,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium,
            color = tokens.inkDeep,
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = strings.projectsFutureSectionSub(
              formatDh(data.expectedMonthlyIncomeDh, cur, dig)
            ),
            style = MaterialTheme.typography.bodySmall,
            color = tokens.body,
          )
        }
      }

      items(
        items = plannedReadiness,
        key = { "planned_${it.project.id}" },
      ) { readiness ->
        val proj = readiness.project
        FutureProjectCard(
          readiness = readiness,
          isAr = isAr,
          currency = cur,
          digitsAr = dig,
          onStartProject = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            val initialAdvance = if (proj.allocatedAmountDh > 0L) {
              proj.allocatedAmountDh
            } else {
              minOf(readiness.requiredAdvanceDh, data.cashNowDh.coerceAtLeast(0L))
            }
            onUpdateProject(
              proj.copy(
                status = ProjectStatus.IN_PROGRESS.code,
                progressPercent = maxOf(proj.progressPercent, 10L),
                allocatedAmountDh = initialAdvance,
              ),
              proj,
            )
          },
          onEdit = {
            editingProject = proj
            showEditorDialog = true
          },
        )
      }
    }

    // 6. SECTION 3 : Projets terminés (triés par priorité)
    val showCompleted = (selectedTab == ProjectFilterTab.ALL || selectedTab == ProjectFilterTab.COMPLETED) &&
      completedList.isNotEmpty()
    if (showCompleted) {
      item {
        Text(
          text = strings.projectsCompletedSectionTitle,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Normal,
          color = tokens.ink,
          modifier = Modifier.testTag("completed_projects_header"),
        )
      }

      items(
        items = completedList,
        key = { "completed_${it.id}" },
      ) { proj ->
        CompletedProjectCard(
          project = proj,
          isAr = isAr,
          currency = cur,
          digitsAr = dig,
          onReopen = {
            onUpdateProject(
              proj.copy(
                status = ProjectStatus.IN_PROGRESS.code,
                progressPercent = 80L,
              ),
              proj,
            )
          },
          onEdit = {
            editingProject = proj
            showEditorDialog = true
          },
        )
      }
    }
    } // end if (subTab == ProjectsSubTab.WORKS)

    // 7. SECTION 4 : Appels de fonds exceptionnels (Travaux / Projets)
    if (subTab == ProjectsSubTab.CALLS) {
      item {
        AirtableSoftCard(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("special_calls_section"),
          padding = 18.dp,
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Icon(
                imageVector = Icons.Default.Campaign,
                contentDescription = null,
                tint = tokens.infoColor,
                modifier = Modifier.size(22.dp),
              )
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = strings.specialCallsSectionTitle,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = tokens.inkDeep,
                )
                Text(
                  text = strings.specialCallsSectionSub,
                  style = MaterialTheme.typography.bodySmall,
                  color = tokens.body,
                )
              }
            }

            if (!isReadOnlyMode) {
              LimePrimaryButton(
                text = strings.specialCallsNewBtn,
                icon = Icons.Default.Add,
                onClick = { showNewSpecialCallDialog = true },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("add_special_call_btn"),
              )
            }
          }
        }
      }

      if (specialCalls.isEmpty()) {
        item {
          SyndicatEmptyStateCard(
            icon = Icons.Default.Campaign,
            title = strings.specialCallsSectionTitle,
            subtitle = strings.specialCallsEmptyText,
            actionLabel = if (isReadOnlyMode) null else strings.specialCallsNewBtn,
            actionIcon = Icons.Default.Add,
            actionTag = "special_calls_empty_add_btn",
            onAction = { if (!isReadOnlyMode) showNewSpecialCallDialog = true },
          )
        }
      }

      items(
        items = specialCalls,
        key = { "special_call_${it.id}" },
      ) { call ->
        val linkedProject = data.projects.firstOrNull { it.id == call.projectId }
        SpecialCallCard(
          call = call,
          linkedProjectTitle = linkedProject?.displayTitle(isAr),
          apartments = data.apartments,
          currency = cur,
          digitsAr = dig,
          isAr = isAr,
          onTrackApartments = { trackingSpecialCallId = call.id },
          onDelete = { if (!isReadOnlyMode) onDeleteSpecialCall(call.id) },
        )
      }
    }

    // 8. SECTION 5 : Journal des Incidents & Réclamations
    if (subTab == ProjectsSubTab.INCIDENTS) {
      if (!isReadOnlyMode) {
        item(key = "incidents_header_action") {
          LimePrimaryButton(
            text = strings.incidentAddBtn,
            icon = Icons.Default.ReportProblem,
            onClick = { showIncidentDialog = true },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("add_incident_btn"),
          )
        }
      }

      if (incidents.isEmpty()) {
        item(key = "incidents_empty") {
          SyndicatEmptyStateCard(
            icon = Icons.Default.ReportProblem,
            title = strings.incidentEmptyTitle,
            subtitle = strings.incidentEmptySub,
            actionLabel = if (isReadOnlyMode) null else strings.incidentAddBtn,
            actionIcon = Icons.Default.Add,
            actionTag = "incidents_empty_add_btn",
            onAction = { if (!isReadOnlyMode) showIncidentDialog = true },
          )
        }
      } else {
        items(incidents, key = { "inc_${it.id}" }) { inc ->
          IncidentItemCard(
            incident = inc,
            isReadOnlyMode = isReadOnlyMode,
            onCycleStatus = { onCycleIncidentStatus(inc.id) },
            onConvertToExpense = { onConvertIncidentToExpense(inc) },
            onDelete = { onDeleteIncident(inc.id) },
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OngoingProjectCard(
  project: ProjectRecord,
  isAr: Boolean,
  currency: String,
  digitsAr: Boolean,
  onIncrementProgress: () -> Unit,
  onMarkCompleted: () -> Unit,
  onEdit: () -> Unit,
) {
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val numericFont = tokens.numericFontFamily ?: tokens.monoFontFamily
  val pct = project.clampedProgressPercent

  FintechWhiteCard(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("ongoing_project_card_${project.id}"),
    padding = 18.dp,
  ) {
    // Badges : Priorité + En cours + Catégorie
    FlowRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      PriorityPillBadge(priority = project.parsedPriority)
      StatusPillBadge(
        status = PaymentBadgeStatus.PARTIAL,
        customLabel = strings.projectStatusLabel(ProjectStatus.IN_PROGRESS),
      )
      CategoryPillBadge(label = strings.projectCategoryLabel(project.category))
    }

    Spacer(modifier = Modifier.height(12.dp))

    Text(
      text = project.displayTitle(isAr),
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Medium,
      color = tokens.ink,
    )
    val secTitle = project.secondaryTitle(isAr)
    if (secTitle.isNotBlank()) {
      Text(
        text = secTitle,
        style = MaterialTheme.typography.bodySmall,
        color = tokens.mute,
      )
    }

    val desc = project.displayDescription(isAr)
    if (desc.isNotBlank()) {
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = desc,
        style = MaterialTheme.typography.bodyMedium,
        color = tokens.body,
      )
    }

    if (project.contractor.isNotBlank() || project.targetDateLabel.isNotBlank()) {
      Spacer(modifier = Modifier.height(8.dp))
      val metaParts = buildList {
        if (project.contractor.isNotBlank()) add(project.contractor)
        if (project.targetDateLabel.isNotBlank()) {
          add("${strings.datePrefix} ${bidiLtr(localizeDigits(project.targetDateLabel, digitsAr))}")
        }
      }
      Text(
        text = metaParts.joinToString(" · "),
        style = MaterialTheme.typography.bodySmall,
        color = tokens.mute,
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Bloc État d'avancement & Budget
    AirtableSoftCard(
      modifier = Modifier.fillMaxWidth(),
      padding = 14.dp,
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = strings.projectProgressStateLabel,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Medium,
          color = tokens.ink,
        )
        Text(
          text = bidiLtr("${localizeDigits(pct.toString(), digitsAr)}%"),
          style = MaterialTheme.typography.headlineMedium.copy(
            fontFamily = numericFont ?: MaterialTheme.typography.headlineMedium.fontFamily,
            fontFeatureSettings = "tnum",
          ),
          fontWeight = FontWeight.Medium,
          color = tokens.positiveDeep,
          modifier = Modifier.testTag("project_progress_${project.id}"),
        )
      }

      Spacer(modifier = Modifier.height(8.dp))
      SyndicatProgressBar(
        progress = pct / 100f,
        modifier = Modifier.fillMaxWidth(),
      )

      Spacer(modifier = Modifier.height(10.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = strings.projectAllocatedVsBudget(
            formatDh(project.allocatedAmountDh, currency, digitsAr),
            formatDh(project.targetBudgetDh, currency, digitsAr),
          ),
          style = MaterialTheme.typography.bodySmall,
          color = tokens.body,
          modifier = Modifier.weight(1f),
        )
      }
      if (project.remainingBudgetDh > 0L) {
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = strings.projectRemainingBudget(
            formatDh(project.remainingBudgetDh, currency, digitsAr)
          ),
          style = MaterialTheme.typography.labelMedium,
          color = tokens.mute,
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Actions rapides secondaires (0 bouton noir supplémentaire)
    FlowRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      if (pct < 100) {
        SageSecondaryButton(
          text = strings.projectPlus10Btn,
          icon = Icons.AutoMirrored.Filled.TrendingUp,
          compact = true,
          onClick = onIncrementProgress,
          modifier = Modifier.testTag("project_plus10_${project.id}"),
        )
      }
      SageSecondaryButton(
        text = strings.projectMarkDoneBtn,
        icon = Icons.Default.CheckCircle,
        compact = true,
        onClick = onMarkCompleted,
        modifier = Modifier.testTag("project_complete_${project.id}"),
      )
      SageSecondaryButton(
        text = strings.projectEditBtn,
        icon = Icons.Default.Edit,
        compact = true,
        onClick = onEdit,
        modifier = Modifier.testTag("project_edit_${project.id}"),
      )
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FutureProjectCard(
  readiness: ProjectCaisseReadiness,
  isAr: Boolean,
  currency: String,
  digitsAr: Boolean,
  onStartProject: () -> Unit,
  onEdit: () -> Unit,
) {
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val numericFont = tokens.numericFontFamily ?: tokens.monoFontFamily
  val project = readiness.project

  FintechWhiteCard(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("planned_project_card_${project.id}"),
    padding = 18.dp,
  ) {
    FlowRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      PriorityPillBadge(priority = project.parsedPriority)
      CategoryPillBadge(label = strings.projectCategoryLabel(project.category))
      if (project.targetDateLabel.isNotBlank()) {
        CategoryPillBadge(label = bidiLtr(localizeDigits(project.targetDateLabel, digitsAr)))
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Text(
      text = project.displayTitle(isAr),
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Medium,
      color = tokens.ink,
    )
    val secTitle = project.secondaryTitle(isAr)
    if (secTitle.isNotBlank()) {
      Text(
        text = secTitle,
        style = MaterialTheme.typography.bodySmall,
        color = tokens.mute,
      )
    }

    val desc = project.displayDescription(isAr)
    if (desc.isNotBlank()) {
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = desc,
        style = MaterialTheme.typography.bodyMedium,
        color = tokens.body,
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Comparaison Budget vs Avance de caisse requise vs Caisse actuelle
    AirtableSoftCard(
      modifier = Modifier.fillMaxWidth(),
      padding = 14.dp,
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = strings.projectTotalBudgetLabel,
            style = MaterialTheme.typography.labelSmall,
            color = tokens.mute,
          )
          Text(
            text = formatDh(project.targetBudgetDh, currency, digitsAr),
            style = MaterialTheme.typography.titleMedium.copy(
              fontFamily = numericFont ?: MaterialTheme.typography.titleMedium.fontFamily,
              fontFeatureSettings = "tnum",
            ),
            fontWeight = FontWeight.Medium,
            color = tokens.ink,
          )
        }
        Column(
          modifier = Modifier.weight(1f),
          horizontalAlignment = Alignment.End,
        ) {
          Text(
            text = strings.projectRequiredAdvanceLabel,
            style = MaterialTheme.typography.labelSmall,
            color = tokens.mute,
          )
          Text(
            text = formatDh(readiness.requiredAdvanceDh, currency, digitsAr),
            style = MaterialTheme.typography.titleMedium.copy(
              fontFamily = numericFont ?: MaterialTheme.typography.titleMedium.fontFamily,
              fontFeatureSettings = "tnum",
            ),
            fontWeight = FontWeight.Medium,
            color = if (readiness.canStartNowDirect) tokens.positiveDeep else tokens.negativeDeep,
            modifier = Modifier.testTag("project_req_advance_${project.id}"),
          )
        }
      }

      HorizontalDivider(
        modifier = Modifier.padding(vertical = 10.dp),
        color = tokens.divider,
      )

      val pctStr = bidiLtr("${localizeDigits(readiness.directCoveragePercent.toString(), digitsAr)}%")
      Text(
        text = strings.projectCaisseCoverageLabel(pctStr),
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Medium,
        color = tokens.ink,
      )
      Spacer(modifier = Modifier.height(6.dp))
      SyndicatProgressBar(
        progress = readiness.directCoveragePercent / 100f,
        modifier = Modifier.fillMaxWidth(),
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Meta Caisse Readiness Banner ({rounded.xl} 16dp)
      if (readiness.canStartNowDirect) {
        val readyBg = if (tokens.isDark) tokens.surfaceStrong else MetaSuccessTintLight
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = readyBg,
          border = BorderStroke(1.dp, tokens.positive.copy(alpha = 0.38f)),
          tonalElevation = 0.dp,
          shadowElevation = 0.dp,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("project_ready_badge_${project.id}"),
        ) {
          Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(
              text = "✓ ${strings.projectReadyDirectBadge}",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = tokens.positiveDeep,
            )
            val subWaterfall = if (readiness.canStartInPriorityWaterfall) {
              strings.projectWaterfallReadyNote
            } else {
              strings.projectWaterfallWaitNote(
                formatDh(readiness.waterfallShortfallDh, currency, digitsAr)
              )
            }
            Text(
              text = subWaterfall,
              style = MaterialTheme.typography.bodySmall,
              color = tokens.inkDeep,
            )
          }
        }
      } else {
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = MetaWarning,
          tonalElevation = 0.dp,
          shadowElevation = 0.dp,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("project_shortfall_badge_${project.id}"),
        ) {
          Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(
              text = "! ${strings.projectMissingAdvanceBadge(formatDh(readiness.directShortfallDh, currency, digitsAr))}",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MetaInkDeep,
            )
            if (readiness.estimatedMonthsToStart > 0) {
              Text(
                text = strings.projectEstMonthsNote(readiness.estimatedMonthsToStart),
                style = MaterialTheme.typography.bodySmall,
                color = MetaInkDeep.copy(alpha = 0.86f),
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    FlowRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      SageSecondaryButton(
        text = strings.projectStartWorkBtn,
        icon = Icons.Default.PlayArrow,
        compact = true,
        onClick = onStartProject,
        modifier = Modifier.testTag("project_start_${project.id}"),
      )
      SageSecondaryButton(
        text = strings.projectEditBtn,
        icon = Icons.Default.Edit,
        compact = true,
        onClick = onEdit,
        modifier = Modifier.testTag("project_edit_${project.id}"),
      )
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CompletedProjectCard(
  project: ProjectRecord,
  isAr: Boolean,
  currency: String,
  digitsAr: Boolean,
  onReopen: () -> Unit,
  onEdit: () -> Unit,
) {
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current

  FintechWhiteCard(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("completed_project_card_${project.id}"),
    padding = 16.dp,
  ) {
    FlowRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      StatusPillBadge(
        status = PaymentBadgeStatus.PAID,
        customLabel = "${strings.projectStatusLabel(ProjectStatus.COMPLETED)} 100%",
      )
      PriorityPillBadge(priority = project.parsedPriority)
      CategoryPillBadge(label = strings.projectCategoryLabel(project.category))
    }

    Spacer(modifier = Modifier.height(10.dp))
    Text(
      text = project.displayTitle(isAr),
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Medium,
      color = tokens.ink,
    )
    Text(
      text = "${strings.projectTotalBudgetLabel}: ${formatDh(project.targetBudgetDh, currency, digitsAr)}",
      style = MaterialTheme.typography.bodySmall,
      color = tokens.body,
    )
    Spacer(modifier = Modifier.height(10.dp))
    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      SageSecondaryButton(
        text = strings.projectReopenBtn,
        compact = true,
        onClick = onReopen,
      )
      SageSecondaryButton(
        text = strings.projectEditBtn,
        icon = Icons.Default.Edit,
        compact = true,
        onClick = onEdit,
      )
    }
  }
}

@Composable
private fun PriorityPillBadge(priority: ProjectPriority) {
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val (bg, fg, hasBorder) = when (priority) {
    ProjectPriority.URGENT -> Triple(MetaCritical, Color.White, false)
    ProjectPriority.HIGH -> Triple(MetaWarning, MetaInkDeep, false)
    ProjectPriority.NORMAL -> Triple(
      if (tokens.isDark) tokens.surfaceStrong else MetaCobaltTintLight,
      tokens.infoColor,
      false,
    )
    ProjectPriority.LOW -> Triple(tokens.surfaceSoft, tokens.mute, true)
  }
  Surface(
    shape = RoundedCornerShape(100.dp),
    color = bg,
    border = if (hasBorder) BorderStroke(1.dp, tokens.divider) else null,
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Text(
      text = strings.projectPriorityShort(priority),
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.Bold,
      color = fg,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
    )
  }
}

@Composable
private fun CategoryPillBadge(label: String) {
  val tokens = LocalSyndicatTokens.current
  Surface(
    shape = RoundedCornerShape(100.dp),
    color = tokens.surfaceSoft,
    border = BorderStroke(1.dp, tokens.divider),
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.Bold,
      color = tokens.body,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProjectEditorDialog(
  initialProject: ProjectRecord?,
  currency: String,
  onDismiss: () -> Unit,
  onSave: (ProjectRecord) -> Unit,
  onDelete: (() -> Unit)?,
) {
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current

  val todayDefault = remember {
    SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date())
  }

  var titleAr by remember { mutableStateOf(initialProject?.titleAr ?: "") }
  var titleFr by remember { mutableStateOf(initialProject?.titleFr ?: "") }
  var priority by remember { mutableLongStateOf(initialProject?.priority ?: 1L) }
  var status by remember { mutableStateOf(initialProject?.status ?: ProjectStatus.PLANNED.code) }
  var category by remember { mutableStateOf(initialProject?.category ?: PROJECT_CATEGORIES.first()) }
  var budgetText by remember {
    mutableStateOf(initialProject?.targetBudgetDh?.takeIf { it > 0L }?.toString() ?: "")
  }
  var minAdvanceText by remember {
    mutableStateOf(initialProject?.minCashToStartDh?.takeIf { it > 0L }?.toString() ?: "")
  }
  var allocatedText by remember {
    mutableStateOf(initialProject?.allocatedAmountDh?.toString() ?: "0")
  }
  var progressText by remember {
    mutableStateOf(initialProject?.progressPercent?.toString() ?: "0")
  }
  var contractor by remember { mutableStateOf(initialProject?.contractor ?: "") }
  var targetDate by remember { mutableStateOf(initialProject?.targetDateLabel ?: todayDefault) }
  var descAr by remember { mutableStateOf(initialProject?.descriptionAr ?: "") }
  var descFr by remember { mutableStateOf(initialProject?.descriptionFr ?: "") }

  var titleError by remember { mutableStateOf<String?>(null) }
  var budgetError by remember { mutableStateOf<String?>(null) }
  var confirmDeleteTap by remember { mutableStateOf(false) }

  SyndicatDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (initialProject == null) strings.projectDialogAddTitle else strings.projectDialogEditTitle,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Normal,
        color = tokens.ink,
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        FintechTextField(
          value = titleAr,
          onValueChange = {
            titleAr = it
            titleError = null
          },
          label = strings.projectFieldTitleAr,
          placeholder = strings.projectFieldTitleArHint,
          errorText = titleError,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("project_title_ar_input"),
        )

        FintechTextField(
          value = titleFr,
          onValueChange = {
            titleFr = it
            titleError = null
          },
          label = strings.projectFieldTitleFr,
          placeholder = strings.projectFieldTitleFrHint,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("project_title_fr_input"),
        )

        // Priorité P1..P4
        Text(
          text = strings.projectFieldPriority,
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Medium,
          color = tokens.body,
        )
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          ProjectPriority.entries.forEach { p ->
            SyndicatFilterChip(
              selected = priority.toInt() == p.level,
              onClick = { priority = p.level.toLong() },
              label = strings.projectPriorityShort(p),
              modifier = Modifier.testTag("project_priority_${p.level}"),
            )
          }
        }

        // Statut
        Text(
          text = strings.projectFieldStatus,
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Medium,
          color = tokens.body,
        )
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          ProjectStatus.entries.forEach { st ->
            SyndicatFilterChip(
              selected = status == st.code,
              onClick = {
                status = st.code
                if (st == ProjectStatus.COMPLETED) {
                  progressText = "100"
                } else if (st == ProjectStatus.PLANNED) {
                  progressText = "0"
                }
              },
              label = strings.projectStatusLabel(st),
              modifier = Modifier.testTag("project_status_${st.code}"),
            )
          }
        }

        // Catégorie
        Text(
          text = strings.projectFieldCategory,
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Medium,
          color = tokens.body,
        )
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          PROJECT_CATEGORIES.forEach { cat ->
            SyndicatFilterChip(
              selected = category == cat,
              onClick = { category = cat },
              label = strings.projectCategoryLabel(cat),
            )
          }
        }

        // Budget total & Avance de caisse requise pour débuter (empilés sans superposition)
        FintechTextField(
          value = budgetText,
          onValueChange = { next ->
            budgetText = next.filter { it.isDigit() }.take(9)
            budgetError = null
          },
          label = strings.projectFieldBudget(currency),
          placeholder = "2500",
          keyboardType = KeyboardType.Number,
          errorText = budgetError,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("project_budget_input"),
        )

        FintechTextField(
          value = minAdvanceText,
          onValueChange = { next ->
            minAdvanceText = next.filter { it.isDigit() }.take(9)
          },
          label = strings.projectFieldMinAdvance(currency),
          placeholder = budgetText.ifBlank { "1200" },
          keyboardType = KeyboardType.Number,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("project_min_advance_input"),
        )

        val parsedBudget = budgetText.toLongOrNull() ?: 0L
        if (parsedBudget > 0L) {
          Text(
            text = strings.projectAdvancePresetsLabel,
            style = MaterialTheme.typography.bodySmall,
            color = tokens.mute,
          )
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            listOf(30, 50, 75, 100).forEach { pct ->
              val presetVal = (parsedBudget * pct) / 100L
              SyndicatFilterChip(
                selected = minAdvanceText == presetVal.toString(),
                onClick = { minAdvanceText = presetVal.toString() },
                label = "${pct}% (${formatNumber(presetVal)})",
                modifier = Modifier.testTag("project_advance_preset_$pct"),
              )
            }
          }
        }

        // État d'avancement (%) & Montant déjà versé
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.Bottom,
        ) {
          FintechTextField(
            value = progressText,
            onValueChange = { next ->
              val digits = next.filter { it.isDigit() }.take(3)
              val clamped = digits.toIntOrNull()?.coerceIn(0, 100)?.toString() ?: ""
              progressText = clamped
            },
            label = strings.projectFieldProgress,
            placeholder = "0",
            keyboardType = KeyboardType.Number,
            modifier = Modifier
              .weight(1f)
              .testTag("project_progress_input"),
          )
          FintechTextField(
            value = allocatedText,
            onValueChange = { next ->
              allocatedText = next.filter { it.isDigit() }.take(9)
            },
            label = strings.projectFieldAllocated(currency),
            placeholder = "0",
            keyboardType = KeyboardType.Number,
            modifier = Modifier
              .weight(1f)
              .testTag("project_allocated_input"),
          )
        }

        if (status == ProjectStatus.IN_PROGRESS.code) {
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            listOf(10, 25, 50, 75, 100).forEach { p ->
              SyndicatFilterChip(
                selected = progressText == p.toString(),
                onClick = { progressText = p.toString() },
                label = "$p%",
              )
            }
          }
        }

        FintechTextField(
          value = contractor,
          onValueChange = { contractor = it },
          label = strings.projectFieldContractor,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("project_contractor_input"),
        )

        FintechTextField(
          value = targetDate,
          onValueChange = { targetDate = it },
          label = strings.projectFieldTargetDate,
          placeholder = "15/12/2026",
          modifier = Modifier
            .fillMaxWidth()
            .testTag("project_date_input"),
        )

        FintechTextField(
          value = descAr,
          onValueChange = { descAr = it },
          label = strings.projectFieldDescAr,
          singleLine = false,
          minLines = 2,
          maxLines = 4,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("project_desc_ar_input"),
        )

        FintechTextField(
          value = descFr,
          onValueChange = { descFr = it },
          label = strings.projectFieldDescFr,
          singleLine = false,
          minLines = 2,
          maxLines = 4,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("project_desc_fr_input"),
        )

        if (onDelete != null) {
          Spacer(modifier = Modifier.height(4.dp))
          SageSecondaryButton(
            text = if (confirmDeleteTap) strings.expDeleteConfirmTap else strings.expDelete,
            icon = Icons.Default.DeleteOutline,
            onClick = {
              if (confirmDeleteTap) {
                onDelete()
              } else {
                confirmDeleteTap = true
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("delete_project_btn"),
          )
        }
      }
    },
    confirmButton = {
      MetaCobaltBuyButton(
        text = strings.apptSave,
        compact = true,
        onClick = {
          if (titleAr.trim().isBlank() && titleFr.trim().isBlank()) {
            titleError = strings.projectErrTitle
            return@MetaCobaltBuyButton
          }
          val budgetVal = budgetText.toLongOrNull()
          if (budgetVal == null || budgetVal < 0L) {
            budgetError = strings.projectErrBudget
            return@MetaCobaltBuyButton
          }
          val minAdvanceVal = minAdvanceText.toLongOrNull() ?: budgetVal
          val allocatedVal = allocatedText.toLongOrNull() ?: 0L
          val progressVal = (progressText.toLongOrNull() ?: 0L).coerceIn(0L, 100L)

          onSave(
            ProjectRecord(
              id = initialProject?.id ?: "",
              userId = initialProject?.userId ?: "",
              titleAr = titleAr.trim(),
              titleFr = titleFr.trim(),
              descriptionAr = descAr.trim(),
              descriptionFr = descFr.trim(),
              category = category,
              priority = priority.coerceIn(1L, 4L),
              status = status,
              targetBudgetDh = budgetVal,
              minCashToStartDh = minAdvanceVal,
              allocatedAmountDh = allocatedVal,
              progressPercent = if (status == ProjectStatus.COMPLETED.code) 100L else progressVal,
              contractor = contractor.trim(),
              targetDateLabel = targetDate.trim(),
              createdAt = initialProject?.createdAt,
              updatedAt = initialProject?.updatedAt,
            )
          )
        },
        modifier = Modifier.testTag("save_project_btn"),
      )
    },
    dismissButton = {
      SageSecondaryButton(
        text = strings.apptCancel,
        compact = true,
        onClick = onDismiss,
      )
    },
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProjectsExportOptionsDialog(
  onDismiss: () -> Unit,
  onExportPdf: (monochromeMode: Boolean) -> Unit,
  onExportPng: (monochromeMode: Boolean) -> Unit,
) {
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  var monochromeMode by remember { mutableStateOf(false) }

  SyndicatDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = strings.exportProjectsDialogTitle,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Normal,
        color = tokens.ink,
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          SyndicatFilterChip(
            selected = !monochromeMode,
            onClick = { monochromeMode = false },
            label = strings.exportModeColor,
            modifier = Modifier.testTag("proj_export_color_chip"),
          )
          SyndicatFilterChip(
            selected = monochromeMode,
            onClick = { monochromeMode = true },
            label = strings.exportModeMonochrome,
            modifier = Modifier.testTag("proj_export_bw_chip"),
          )
        }
        Text(
          text = strings.exportFormatLabel,
          style = MaterialTheme.typography.labelMedium,
          color = tokens.body,
        )
        MetaCobaltBuyButton(
          text = strings.exportPdfPrimaryBtn,
          icon = Icons.Default.PictureAsPdf,
          compact = true,
          onClick = { onExportPdf(monochromeMode) },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("proj_export_pdf_btn"),
        )
        SageSecondaryButton(
          text = strings.exportPngSecondaryBtn,
          icon = Icons.Default.Image,
          compact = true,
          onClick = { onExportPng(monochromeMode) },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("proj_export_png_btn"),
        )
      }
    },
    confirmButton = {},
    dismissButton = {
      SageSecondaryButton(
        text = strings.apptCancel,
        compact = true,
        onClick = onDismiss,
      )
    },
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SpecialCallCard(
  call: SpecialCallRecord,
  linkedProjectTitle: String?,
  apartments: List<com.example.data.model.ApartmentRecord>,
  currency: String,
  digitsAr: Boolean,
  isAr: Boolean,
  onTrackApartments: () -> Unit,
  onDelete: () -> Unit,
) {
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  var confirmDelete by remember(call.id) { mutableStateOf(false) }

  val eligibleCount = call.eligibleApartments(apartments).size
  val paidCount = call.paidApartmentsCount(apartments)
  val collected = call.collectedTotalDh(apartments)
  val expected = call.expectedTotalDh(apartments)
  val ratio = if (expected > 0L) (collected.toFloat() / expected.toFloat()).coerceIn(0f, 1f) else 0f

  FintechWhiteCard(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("special_call_card_${call.id}"),
    padding = 16.dp,
  ) {
    FlowRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      CategoryPillBadge(label = formatDh(call.amountPerAptDh, currency, digitsAr))
      if (!linkedProjectTitle.isNullOrBlank()) {
        CategoryPillBadge(label = linkedProjectTitle)
      }
      if (call.dueDateLabel.isNotBlank()) {
        CategoryPillBadge(label = bidiLtr(localizeDigits(call.dueDateLabel, digitsAr)))
      }
    }

    Spacer(modifier = Modifier.height(10.dp))
    Text(
      text = call.displayTitle(isAr),
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Medium,
      color = tokens.inkDeep,
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = strings.specialCallCollectedSummary(
        formatDh(collected, currency, digitsAr),
        formatDh(expected, currency, digitsAr),
        paidCount,
        eligibleCount,
      ),
      style = MaterialTheme.typography.bodySmall,
      color = tokens.body,
    )
    Spacer(modifier = Modifier.height(8.dp))
    SyndicatProgressBar(
      progress = ratio,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(modifier = Modifier.height(12.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      SageSecondaryButton(
        text = strings.specialCallTrackBtn,
        icon = Icons.Default.Check,
        compact = true,
        onClick = onTrackApartments,
        modifier = Modifier
          .weight(1f)
          .testTag("special_call_track_${call.id}"),
      )
      if (confirmDelete) {
        SageSecondaryButton(
          text = "! ${strings.settingsConfirmBtn}",
          icon = Icons.Default.DeleteOutline,
          compact = true,
          onClick = {
            confirmDelete = false
            onDelete()
          },
          modifier = Modifier.testTag("special_call_confirm_delete_${call.id}"),
        )
      } else {
        IconButton(
          onClick = { confirmDelete = true },
          modifier = Modifier
            .size(48.dp)
            .testTag("special_call_delete_${call.id}"),
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SpecialCallEditorDialog(
  projects: List<ProjectRecord>,
  currency: String,
  isAr: Boolean,
  onDismiss: () -> Unit,
  onSave: (titleFr: String, titleAr: String, amountPerAptDh: Long, projectId: String, dueDateLabel: String) -> Unit,
) {
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val defaultDue = remember { SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Date()) }

  var titleFr by remember { mutableStateOf("") }
  var titleAr by remember { mutableStateOf("") }
  var amountText by remember { mutableStateOf("") }
  var selectedProjectId by remember { mutableStateOf("") }
  var dueDateLabel by remember { mutableStateOf(defaultDue) }
  var errorMsg by remember { mutableStateOf<String?>(null) }

  SyndicatDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = strings.specialCallDialogAddTitle,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Normal,
        color = tokens.ink,
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        FintechTextField(
          value = titleFr,
          onValueChange = { titleFr = it.take(150); errorMsg = null },
          label = strings.specialCallTitleFrLabel,
          placeholder = "Ex: Cotisation exceptionnelle caméras",
          errorText = errorMsg,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("special_call_title_fr_input"),
        )
        FintechTextField(
          value = titleAr,
          onValueChange = { titleAr = it.take(150); errorMsg = null },
          label = strings.specialCallTitleArLabel,
          placeholder = "مثال: مساهمة استثنائية للكاميرات",
          modifier = Modifier
            .fillMaxWidth()
            .testTag("special_call_title_ar_input"),
        )
        FintechTextField(
          value = amountText,
          onValueChange = { amountText = it.filter { c -> c.isDigit() }.take(6); errorMsg = null },
          label = strings.specialCallAmountPerAptLabel(currency),
          placeholder = "300",
          keyboardType = KeyboardType.Number,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("special_call_amount_input"),
        )
        FintechTextField(
          value = dueDateLabel,
          onValueChange = { dueDateLabel = it.take(25) },
          label = strings.projectFieldTargetDate,
          modifier = Modifier.fillMaxWidth(),
        )

        if (projects.isNotEmpty()) {
          Text(
            text = strings.specialCallLinkedProjectLabel,
            style = MaterialTheme.typography.labelMedium,
            color = tokens.body,
          )
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            SyndicatFilterChip(
              selected = selectedProjectId.isBlank(),
              onClick = { selectedProjectId = "" },
              label = strings.specialCallNoProject,
            )
            projects.forEach { proj ->
              SyndicatFilterChip(
                selected = selectedProjectId == proj.id,
                onClick = { selectedProjectId = proj.id },
                label = proj.displayTitle(isAr),
              )
            }
          }
        }
      }
    },
    confirmButton = {
      MetaCobaltBuyButton(
        text = strings.apptSave,
        compact = true,
        onClick = {
          val amt = amountText.toLongOrNull() ?: 0L
          if (titleFr.isBlank() && titleAr.isBlank()) {
            errorMsg = strings.projectErrTitle
            return@MetaCobaltBuyButton
          }
          if (amt <= 0L) {
            errorMsg = strings.expErrAmount
            return@MetaCobaltBuyButton
          }
          onSave(titleFr.trim(), titleAr.trim(), amt, selectedProjectId, dueDateLabel.trim())
        },
        modifier = Modifier.testTag("save_special_call_btn"),
      )
    },
    dismissButton = {
      SageSecondaryButton(
        text = strings.apptCancel,
        compact = true,
        onClick = onDismiss,
      )
    },
  )
}

@Composable
private fun SpecialCallApartmentsDialog(
  call: SpecialCallRecord,
  apartments: List<com.example.data.model.ApartmentRecord>,
  currency: String,
  digitsAr: Boolean,
  isAr: Boolean,
  onDismiss: () -> Unit,
  onToggleAptPaid: (aptNum: Long, paid: Boolean) -> Unit,
) {
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current

  SyndicatDialog(
    onDismissRequest = onDismiss,
    title = {
      Column {
        Text(
          text = call.displayTitle(isAr),
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Normal,
          color = tokens.ink,
        )
        Text(
          text = formatDh(call.amountPerAptDh, currency, digitsAr),
          style = MaterialTheme.typography.labelLarge,
          color = tokens.positiveDeep,
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 380.dp)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        apartments.forEach { apt ->
          val isPaid = call.isApartmentPaid(apt.number)
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isPaid) tokens.surfaceSoft else tokens.cardBg,
            border = BorderStroke(1.dp, tokens.divider),
            modifier = Modifier
              .fillMaxWidth()
              .minimumInteractiveComponentSize()
              .heightIn(min = 48.dp)
              .clip(RoundedCornerShape(10.dp))
              .clickable { onToggleAptPaid(apt.number, !isPaid) }
              .testTag("sc_apt_toggle_${apt.number}"),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Checkbox(
                checked = isPaid,
                onCheckedChange = { onToggleAptPaid(apt.number, it) },
              )
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "${strings.colAppt} ${localizeDigits(apt.displayNumber, digitsAr)}",
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.SemiBold,
                  color = tokens.ink,
                )
                val owner = apt.displayOwner(isAr)
                if (owner.isNotBlank()) {
                  Text(
                    text = owner,
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.body,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
              }
              Text(
                text = if (isPaid) "✓ ${strings.badgePaid}" else formatDh(call.amountPerAptDh, currency, digitsAr),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isPaid) tokens.positiveDeep else tokens.ink,
              )
            }
          }
        }
      }
    },
    confirmButton = {
      SageSecondaryButton(
        text = strings.apptCancel,
        compact = true,
        onClick = onDismiss,
      )
    },
  )
}

@Composable
private fun AgQuorumVoteDialog(
  data: SyndicatDashboardData,
  apartments: List<com.example.data.model.ApartmentRecord>,
  isAr: Boolean,
  digitsAr: Boolean,
  draft: AgMinutesDraft,
  isReadOnlyMode: Boolean,
  onUpdateDraft: (AgMinutesDraft) -> Unit,
  onReset: () -> Unit,
  onExportPvPdf: (Boolean) -> Unit,
  onDismiss: () -> Unit,
) {
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val isPrivacy = LocalPrivacyMode.current
  val presentNumbers = draft.presentAptNumbers
  val totalLots = apartments.size.coerceAtLeast(1)
  val presentCount = apartments.count { it.number in presentNumbers }
  val requiredCount = (totalLots / 2) + 1
  val missingCount = (requiredCount - presentCount).coerceAtLeast(0)
  val hasQuorum = presentCount * 2 >= totalLots && apartments.isNotEmpty()
  val ratio = (presentCount.toFloat() / totalLots.toFloat()).coerceIn(0f, 1f)
  var newResolutionTitle by remember { mutableStateOf("") }
  val today = remember { SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Date()) }

  SyndicatDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = strings.agQuorumCardTitle,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        color = tokens.inkDeep,
        modifier = Modifier.testTag("ag_quorum_dialog"),
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 480.dp)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Text(
          text = strings.agQuorumCardSub,
          style = MaterialTheme.typography.bodySmall,
          color = tokens.body,
        )

        Surface(
          shape = RoundedCornerShape(14.dp),
          color = if (hasQuorum) {
            if (tokens.isDark) Color(0xFF0A2E1F) else MetaSuccessTintLight
          } else {
            tokens.surfaceSoft
          },
          border = BorderStroke(
            1.dp,
            if (hasQuorum) tokens.positive.copy(alpha = 0.45f) else tokens.divider,
          ),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = strings.agQuorumPresentCount(presentCount, apartments.size),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = tokens.inkDeep,
              )
              Text(
                text = bidiLtr("${localizeDigits(((ratio * 100f).toInt()).toString(), digitsAr)}%"),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (hasQuorum) tokens.positiveDeep else tokens.inkDeep,
              )
            }
            SyndicatProgressBar(
              progress = ratio,
              modifier = Modifier.fillMaxWidth(),
            )
            Text(
              text = if (hasQuorum) {
                strings.agQuorumReachedBadge
              } else {
                strings.agQuorumMissingBadge(missingCount)
              },
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              color = if (hasQuorum) tokens.positiveDeep else tokens.warningContent,
              modifier = Modifier.testTag("ag_quorum_status_text"),
            )
          }
        }

        // Date, lieu & ordre du jour du PV
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          FintechTextField(
            value = draft.meetingDate.ifBlank { today },
            onValueChange = { if (!isReadOnlyMode) onUpdateDraft(draft.copy(meetingDate = it.take(25))) },
            label = strings.agDateLabel,
            modifier = Modifier.weight(1f),
          )
          FintechTextField(
            value = draft.location,
            onValueChange = { if (!isReadOnlyMode) onUpdateDraft(draft.copy(location = it.take(100))) },
            label = strings.agLocationLabel,
            placeholder = data.config.residenceName,
            modifier = Modifier.weight(1f),
          )
        }
        FintechTextField(
          value = draft.agenda,
          onValueChange = { if (!isReadOnlyMode) onUpdateDraft(draft.copy(agenda = it.take(500))) },
          label = strings.agAgendaLabel,
          placeholder = strings.agAgendaHint,
          singleLine = false,
          minLines = 2,
          maxLines = 3,
          modifier = Modifier.fillMaxWidth(),
        )

        // Feuille de présence (lots présents ou représentés)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          apartments.forEach { apt ->
            val checked = apt.number in presentNumbers
            val ownerName = maskSensitiveName(apt.displayOwner(isAr), isPrivacy)
            val togglePresent: () -> Unit = {
              if (!isReadOnlyMode) {
                val nextSet = if (checked) presentNumbers - apt.number else presentNumbers + apt.number
                onUpdateDraft(draft.copy(presentAptNumbers = nextSet))
              }
            }
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (checked) tokens.primaryTint.copy(alpha = 0.45f) else tokens.cardBg,
              border = BorderStroke(1.dp, if (checked) tokens.linkColor.copy(alpha = 0.4f) else tokens.divider),
              modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = togglePresent)
                .testTag("quorum_apt_${apt.number}"),
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Checkbox(
                  checked = checked,
                  onCheckedChange = { togglePresent() },
                )
                Text(
                  text = "${strings.colAppt} ${localizeDigits(apt.displayNumber, digitsAr)}",
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.SemiBold,
                  color = tokens.inkDeep,
                )
                if (ownerName.isNotBlank()) {
                  Text(
                    text = ownerName,
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.body,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                  )
                }
              }
            }
          }
        }

        HorizontalDivider(color = tokens.divider)

        // Résolutions & Votes
        Text(
          text = strings.agResolutionsLabel,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = tokens.inkDeep,
        )
        if (!isReadOnlyMode) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            FintechTextField(
              value = newResolutionTitle,
              onValueChange = { newResolutionTitle = it.take(180) },
              label = strings.agNewResolutionHint,
              modifier = Modifier.weight(1f).testTag("ag_resolution_input"),
            )
            RoundIconCircleButton(
              icon = Icons.Default.Add,
              contentDescription = strings.agResolutionsLabel,
              onClick = {
                val clean = newResolutionTitle.trim()
                if (clean.isNotBlank()) {
                  val res = AgResolutionRecord(
                    id = "res_${UUID.randomUUID().toString().take(8)}",
                    title = clean,
                    votesFor = presentCount.coerceAtLeast(1),
                  )
                  onUpdateDraft(draft.copy(resolutions = draft.resolutions + res))
                  newResolutionTitle = ""
                }
              },
              modifier = Modifier.testTag("ag_add_resolution_btn"),
            )
          }
        }

        draft.resolutions.forEach { res ->
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = tokens.surfaceSoft,
            border = BorderStroke(1.dp, tokens.divider),
            modifier = Modifier.fillMaxWidth().testTag("ag_resolution_${res.id}"),
          ) {
            Column(
              modifier = Modifier.padding(10.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = res.title,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = tokens.inkDeep,
                  modifier = Modifier.weight(1f),
                )
                Text(
                  text = if (res.isAdopted()) strings.agResolutionAdopted else strings.agResolutionRejected,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = if (res.isAdopted()) tokens.positiveDeep else tokens.negativeDeep,
                )
              }
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = "${strings.agVoteFor}: ${res.votesFor} · ${strings.agVoteAgainst}: ${res.votesAgainst} · ${strings.agVoteAbstain}: ${res.votesAbstain}",
                  style = MaterialTheme.typography.labelMedium,
                  color = tokens.body,
                  modifier = Modifier.weight(1f),
                )
                if (!isReadOnlyMode) {
                  IconButton(
                    onClick = {
                      onUpdateDraft(
                        draft.copy(
                          resolutions = draft.resolutions.map {
                            if (it.id == res.id) it.copy(votesFor = it.votesFor + 1) else it
                          },
                        ),
                      )
                    },
                    modifier = Modifier.size(36.dp),
                  ) {
                    Icon(Icons.Default.Add, contentDescription = strings.agVoteFor, tint = tokens.positiveDeep)
                  }
                  IconButton(
                    onClick = {
                      onUpdateDraft(
                        draft.copy(
                          resolutions = draft.resolutions.map {
                            if (it.id == res.id) it.copy(votesAgainst = it.votesAgainst + 1) else it
                          },
                        ),
                      )
                    },
                    modifier = Modifier.size(36.dp),
                  ) {
                    Icon(Icons.Default.Remove, contentDescription = strings.agVoteAgainst, tint = tokens.negativeDeep)
                  }
                  IconButton(
                    onClick = {
                      onUpdateDraft(draft.copy(resolutions = draft.resolutions.filterNot { it.id == res.id }))
                    },
                    modifier = Modifier.size(36.dp),
                  ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = strings.expDelete, tint = tokens.mute)
                  }
                }
              }
            }
          }
        }

        // Export PV PDF (Couleur & N&B)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          MetaCobaltBuyButton(
            text = strings.agExportPvColorBtn,
            icon = Icons.Default.PictureAsPdf,
            compact = true,
            onClick = { onExportPvPdf(false) },
            modifier = Modifier.weight(1f).testTag("ag_export_pv_pdf_btn"),
          )
          SageSecondaryButton(
            text = strings.agExportPvMonoBtn,
            icon = Icons.Default.PictureAsPdf,
            compact = true,
            onClick = { onExportPvPdf(true) },
            modifier = Modifier.weight(1f).testTag("ag_export_pv_mono_btn"),
          )
        }
      }
    },
    confirmButton = {
      SageSecondaryButton(
        text = strings.apptCancel,
        compact = true,
        onClick = onDismiss,
        modifier = Modifier.testTag("ag_quorum_close_btn"),
      )
    },
    dismissButton = {
      if (presentCount > 0 && !isReadOnlyMode) {
        SageSecondaryButton(
          text = strings.agQuorumResetBtn,
          compact = true,
          onClick = onReset,
          modifier = Modifier.testTag("ag_quorum_reset_btn"),
        )
      }
    },
  )
}


