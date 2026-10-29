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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.PROJECT_CATEGORIES
import com.example.data.model.PaymentBadgeStatus
import com.example.data.model.ProjectCaisseReadiness
import com.example.data.model.ProjectPriority
import com.example.data.model.ProjectRecord
import com.example.data.model.ProjectStatus
import com.example.ui.components.AirtableCreamCalloutCard
import com.example.ui.components.AirtablePastelKpiCard
import com.example.ui.components.AirtableSignatureCard
import com.example.ui.components.AirtableSoftCard
import com.example.ui.components.FintechTextField
import com.example.ui.components.FintechWhiteCard
import com.example.ui.components.LimePrimaryButton
import com.example.ui.components.MetaCobaltBuyButton
import com.example.ui.components.SageSecondaryButton
import com.example.ui.components.StatusPillBadge
import com.example.ui.components.SyndicatDialog
import com.example.ui.components.SyndicatFilterChip
import com.example.ui.components.SyndicatProgressBar
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

private enum class ProjectFilterTab {
  ALL,
  IN_PROGRESS,
  PLANNED,
  COMPLETED,
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProjectsScreen(
  data: SyndicatDashboardData,
  onAddProject: (ProjectRecord) -> Unit,
  onUpdateProject: (ProjectRecord, ProjectRecord?) -> Unit,
  onDeleteProject: (ProjectRecord) -> Unit,
  onLoadStarterProjects: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val cfg = data.config
  val cur = cfg.currency
  val dig = cfg.digitsAr
  val isAr = strings.language == AppLanguage.AR
  val numericFont = tokens.numericFontFamily ?: tokens.monoFontFamily

  var selectedTab by rememberSaveable { mutableStateOf(ProjectFilterTab.ALL) }
  var showEditorDialog by remember { mutableStateOf(false) }
  var editingProject by remember { mutableStateOf<ProjectRecord?>(null) }

  val ongoingList = data.inProgressProjects
  val plannedReadiness = data.plannedReadinessList
  val completedList = data.completedProjects

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
        LimePrimaryButton(
          text = strings.projectsAddBtn,
          icon = Icons.Default.Add,
          onClick = {
            editingProject = null
            showEditorDialog = true
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("add_project_fab"),
        )
      }
    }

    // 2. Meta Showcase Card (#0A1317 32dp) + 2 Soft Cloud KPI Tiles (24dp)
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
          val cashText = if (data.cashNowDh < 0L) {
            "▼ ${formatDh(data.cashNowDh, cur, dig)}"
          } else {
            formatDh(data.cashNowDh, cur, dig)
          }
          Text(
            text = cashText,
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
        AirtableSoftCard(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("projects_empty_state"),
          padding = 24.dp,
        ) {
          Icon(
            imageVector = Icons.Default.Construction,
            contentDescription = null,
            tint = tokens.ink,
            modifier = Modifier.size(32.dp),
          )
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = strings.projectsEmptyTitle,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Normal,
            color = tokens.ink,
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = strings.projectsEmptySub,
            style = MaterialTheme.typography.bodySmall,
            color = tokens.body,
          )
          Spacer(modifier = Modifier.height(16.dp))
          SageSecondaryButton(
            text = strings.projectsLoadSamplesBtn,
            icon = Icons.Default.Construction,
            onClick = onLoadStarterProjects,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("load_sample_projects_btn"),
          )
        }
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
