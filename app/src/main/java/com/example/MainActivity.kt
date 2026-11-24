package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.auth.signOutUser
import com.example.data.local.IncidentRecord
import com.example.data.local.LocalSyndicatRepository
import com.example.data.local.SyndicatExtrasStore
import com.example.data.local.SyndicatRoleMode
import com.example.data.model.ApartmentRecord
import com.example.data.model.PaymentBadgeStatus
import com.example.data.repository.LOCAL_DEVICE_USER_ID
import com.example.data.repository.SyndicatDataSource
import com.example.data.repository.SyndicatRepository
import com.example.ui.components.LimePrimaryButton
import com.example.ui.components.LocalPrivacyMode
import com.example.ui.components.LocalSyndicatAdaptiveSpec
import com.example.ui.components.PinLockGateScreen
import com.example.ui.components.SageSecondaryButton
import com.example.ui.components.StatusPillBadge
import com.example.ui.components.SyndicatDialog
import com.example.ui.components.SyndicatFloatingBottomDock
import com.example.ui.components.SyndicatNavIcons
import com.example.ui.components.SyndicatNavItemSpec
import com.example.ui.components.SyndicatNavigationRailPane
import com.example.ui.components.SyndicatShimmerLoadingScreen
import com.example.ui.components.computeSyndicatAdaptiveSpec
import com.example.ui.components.maskSensitiveDh
import com.example.ui.components.maskSensitiveName
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LanguagePreferences
import com.example.ui.i18n.LocalStrings
import com.example.ui.i18n.SyndicatStrings
import com.example.ui.i18n.formatNumber
import com.example.ui.i18n.localizeDigits
import com.example.ui.i18n.stringsForLanguage
import androidx.compose.ui.text.style.TextOverflow
import com.example.ui.screens.ApartmentsScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExpensesScreen
import com.example.ui.screens.OnboardingWizardScreen
import com.example.ui.screens.PosterScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AppTheme
import com.example.ui.theme.LocalSyndicatTokens
import com.example.ui.theme.MetaCanvas
import com.example.ui.theme.MetaInkDeep
import com.example.ui.theme.sanitizeThemeStyle
import com.example.ui.viewmodel.StatusFilter
import com.example.ui.viewmodel.SyndicatDashboardData
import com.example.ui.viewmodel.SyndicatViewModel
import com.example.ui.viewmodel.UiState
import com.example.ui.viewmodel.formatDh
import com.example.util.BackupManager
import com.example.util.BiometricHelper
import com.example.widget.SyndicatWidgetSnapshot
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

enum class AppDestination(
  val route: String,
  val filledIcon: ImageVector,
  val outlinedIcon: ImageVector,
) {
  DASHBOARD("dashboard", SyndicatNavIcons.DashboardFilled, SyndicatNavIcons.DashboardOutlined),
  APARTMENTS("apartments", SyndicatNavIcons.ApartmentsFilled, SyndicatNavIcons.ApartmentsOutlined),
  EXPENSES("expenses", SyndicatNavIcons.ExpensesFilled, SyndicatNavIcons.ExpensesOutlined),
  PROJECTS("projects", SyndicatNavIcons.ProjectsFilled, SyndicatNavIcons.ProjectsOutlined),
  POSTER("poster", SyndicatNavIcons.PosterFilled, SyndicatNavIcons.PosterOutlined),
  SETTINGS("settings", SyndicatNavIcons.SettingsFilled, SyndicatNavIcons.SettingsOutlined);

  fun label(strings: SyndicatStrings): String = when (this) {
    DASHBOARD -> strings.navBarDashboard
    APARTMENTS -> strings.navBarApartments
    EXPENSES -> strings.navExpenses
    PROJECTS -> strings.navProjects
    POSTER -> strings.navPoster
    SETTINGS -> strings.navSettings
  }
}

class MainActivity : ComponentActivity() {
  companion object {
    const val EXTRA_OPEN_QUICK_PAY = "open_quick_pay"
    const val EXTRA_WIDGET_DESTINATION = "widget_destination"
    const val WIDGET_ROUTE_STUDIO = "widget_studio"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    val openQuickPayFromWidget = intent?.getBooleanExtra(EXTRA_OPEN_QUICK_PAY, false) == true
    val widgetDest = intent?.getStringExtra(EXTRA_WIDGET_DESTINATION)
    setContent {
      SyndicatRootApp(
        initialOpenQuickPay = openQuickPayFromWidget,
        initialWidgetDestination = widgetDest,
      )
    }
  }
}

@Composable
fun SyndicatRootApp(
  initialOpenQuickPay: Boolean = false,
  initialWidgetDestination: String? = null,
) {
  val context = LocalContext.current
  var currentLanguage by remember {
    mutableStateOf(LanguagePreferences.getSavedLanguage(context))
  }
  var themeStyle by remember {
    mutableStateOf(LanguagePreferences.getThemeStyle(context))
  }
  var themeMode by remember { mutableStateOf("system") }
  var fontScale by remember { mutableStateOf("normal") }
  var savedPin by remember { mutableStateOf(LanguagePreferences.getPinCode(context)) }
  var biometricEnabled by remember { mutableStateOf(LanguagePreferences.isBiometricUnlockEnabled(context)) }
  val biometricAvailable = remember { BiometricHelper.isBiometricAvailable(context) }
  var pinUnlocked by rememberSaveable { mutableStateOf(false) }
  var showOptionalAuthScreen by rememberSaveable { mutableStateOf(false) }

  var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

  DisposableEffect(Unit) {
    val listener = FirebaseAuth.AuthStateListener { auth ->
      currentUser = auth.currentUser
    }
    Firebase.auth.addAuthStateListener(listener)
    onDispose {
      Firebase.auth.removeAuthStateListener(listener)
    }
  }

  val activeStrings = stringsForLanguage(currentLanguage.code)
  val layoutDir = if (currentLanguage.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
  val systemDark = isSystemInDarkTheme()
  val useDarkTheme = when (themeMode) {
    "light" -> false
    "dark" -> true
    else -> systemDark
  }

  AppTheme(
    darkTheme = useDarkTheme,
    isArabic = currentLanguage == AppLanguage.AR,
    fontScale = fontScale,
    themeStyle = themeStyle,
  ) {
    CompositionLocalProvider(
      LocalStrings provides activeStrings,
      LocalLayoutDirection provides layoutDir,
    ) {
      val user = currentUser
      if (savedPin.isNotBlank() && !pinUnlocked) {
        PinLockGateScreen(
          expectedPin = savedPin,
          onUnlocked = { pinUnlocked = true },
          biometricEnabled = biometricEnabled && biometricAvailable,
          onTriggerBiometric = {
            BiometricHelper.promptUnlock(
              context = context,
              title = activeStrings.biometricPromptTitle,
              subtitle = activeStrings.biometricPromptSubtitle,
              negativeButtonText = activeStrings.apptCancel,
              onSucceeded = { pinUnlocked = true },
            )
          },
        )
      } else if (showOptionalAuthScreen && user == null) {
        BackHandler {
          showOptionalAuthScreen = false
        }
        AuthScreen(
          currentLanguage = currentLanguage,
          onSelectLanguage = { newLang ->
            currentLanguage = newLang
            LanguagePreferences.saveLanguage(context, newLang)
          },
          onAuthSuccess = {
            currentUser = Firebase.auth.currentUser
            showOptionalAuthScreen = false
          },
          onContinueWithoutAccount = {
            showOptionalAuthScreen = false
          },
        )
      } else {
        val activeUserId = user?.uid ?: LOCAL_DEVICE_USER_ID
        val isCloudSynced = user != null
        AuthenticatedSyndicatApp(
          initialOpenQuickPay = initialOpenQuickPay,
          initialWidgetDestination = initialWidgetDestination,
          currentUserId = activeUserId,
          userEmail = user?.email,
          isCloudSynced = isCloudSynced,
          currentLanguage = currentLanguage,
          onLanguageSynced = { syncedLang ->
            if (currentLanguage != syncedLang) {
              currentLanguage = syncedLang
              LanguagePreferences.saveLanguage(context, syncedLang)
            }
          },
          onDisplayPrefsSynced = { syncedStyle, syncedTheme, syncedScale ->
            val cleanStyle = sanitizeThemeStyle(syncedStyle, themeStyle)
            themeStyle = cleanStyle
            LanguagePreferences.saveThemeStyle(context, cleanStyle)
            themeMode = syncedTheme
            fontScale = syncedScale
          },
          onUserSelectedLanguage = { newLang ->
            currentLanguage = newLang
            LanguagePreferences.saveLanguage(context, newLang)
          },
          onPinUpdated = { newPin ->
            savedPin = newPin
            biometricEnabled = LanguagePreferences.isBiometricUnlockEnabled(context) && newPin.isNotBlank()
            pinUnlocked = true
          },
          onRequestGoogleSync = {
            showOptionalAuthScreen = true
          },
          onSignOutComplete = {
            LanguagePreferences.setHasPreviousGoogleSession(context, false)
            currentUser = null
            showOptionalAuthScreen = false
          },
        )
      }
    }
  }
}

@Composable
private fun AuthenticatedSyndicatApp(
  initialOpenQuickPay: Boolean = false,
  initialWidgetDestination: String? = null,
  currentUserId: String,
  userEmail: String?,
  isCloudSynced: Boolean,
  currentLanguage: AppLanguage,
  onLanguageSynced: (AppLanguage) -> Unit,
  onDisplayPrefsSynced: (String, String, String) -> Unit,
  onUserSelectedLanguage: (AppLanguage) -> Unit,
  onPinUpdated: (String) -> Unit,
  onRequestGoogleSync: () -> Unit,
  onSignOutComplete: () -> Unit,
) {
  val context = LocalContext.current
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }
  val localRepo = remember(context) { LocalSyndicatRepository.getInstance(context) }
  val extrasStore = remember(context) { SyndicatExtrasStore.getInstance(context) }
  val specialCalls by extrasStore.specialCalls.collectAsStateWithLifecycle()
  val auditLogs by extrasStore.auditLogs.collectAsStateWithLifecycle()
  val buildings by extrasStore.buildings.collectAsStateWithLifecycle()
  val activeBuildingId by extrasStore.activeBuildingId.collectAsStateWithLifecycle()
  val roleMode by extrasStore.roleMode.collectAsStateWithLifecycle()
  val recurringTemplates by extrasStore.recurringTemplates.collectAsStateWithLifecycle()
  val vendors by extrasStore.vendors.collectAsStateWithLifecycle()
  val incidents by extrasStore.incidents.collectAsStateWithLifecycle()
  val agMinutes by extrasStore.agMinutes.collectAsStateWithLifecycle()
  val isReadOnlyMode = roleMode == SyndicatRoleMode.READ_ONLY_COUNCIL

  val viewModelKey = if (isCloudSynced) "syndicat_vm_cloud_$currentUserId" else "syndicat_vm_local"
  val viewModel: SyndicatViewModel = viewModel(
    key = viewModelKey,
    factory = object : ViewModelProvider.Factory {
      @Suppress("UNCHECKED_CAST")
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val dataSource: SyndicatDataSource = if (isCloudSynced) {
          SyndicatRepository(context.applicationContext)
        } else {
          localRepo
        }
        return SyndicatViewModel(
          repository = dataSource,
          currentUserId = currentUserId,
          extrasStore = extrasStore,
        ) as T
      }
    },
  )

  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val selectedFloor by viewModel.selectedFloor.collectAsStateWithLifecycle()
  val statusFilter by viewModel.statusFilter.collectAsStateWithLifecycle()
  val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()

  var currentDestination by rememberSaveable { mutableStateOf(AppDestination.DASHBOARD) }
  var showOnboardingWizard by rememberSaveable { mutableStateOf(false) }
  var isBackupOverdue by remember { mutableStateOf(LanguagePreferences.isBackupOverdue(context)) }
  var expensesInitialUnpaidOnly by rememberSaveable { mutableStateOf(false) }
  var projectsInitialCallsOnly by rememberSaveable { mutableStateOf(false) }
  var quickPayTargetAptNumber by remember { mutableStateOf<Long?>(null) }
  var showQuickPayPickerDialog by remember { mutableStateOf(false) }
  var consumedQuickPayIntent by rememberSaveable { mutableStateOf(false) }
  var prefillIncidentForExpense by remember { mutableStateOf<IncidentRecord?>(null) }
  var isPrivacyMode by rememberSaveable { mutableStateOf(false) }
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(activeBuildingId) {
    if (!isCloudSynced) {
      localRepo.switchBuilding(activeBuildingId, preserveLanguage = currentLanguage.code)
    }
  }

  LaunchedEffect(initialOpenQuickPay, initialWidgetDestination, uiState, isReadOnlyMode) {
    if (!consumedQuickPayIntent && uiState is UiState.Success) {
      when {
        (initialOpenQuickPay || initialWidgetDestination == "quick_pay") && !isReadOnlyMode -> {
          consumedQuickPayIntent = true
          showQuickPayPickerDialog = true
        }
        initialWidgetDestination == "late" -> {
          consumedQuickPayIntent = true
          viewModel.selectStatusFilter(StatusFilter.LATE)
          viewModel.selectFloor(null)
          currentDestination = AppDestination.APARTMENTS
        }
        initialWidgetDestination == "expenses" -> {
          consumedQuickPayIntent = true
          currentDestination = AppDestination.EXPENSES
        }
        initialWidgetDestination == MainActivity.WIDGET_ROUTE_STUDIO -> {
          consumedQuickPayIntent = true
          currentDestination = AppDestination.SETTINGS
        }
      }
    }
  }

  val selectDestination: (AppDestination) -> Unit = { dest ->
    if (dest != AppDestination.EXPENSES) expensesInitialUnpaidOnly = false
    if (dest != AppDestination.PROJECTS) projectsInitialCallsOnly = false
    currentDestination = dest
  }

  // When signing in with Google for the first time on an empty cloud profile, migrate local phone data automatically
  LaunchedEffect(isCloudSynced, currentUserId) {
    if (isCloudSynced && localRepo.hasLocalUserData()) {
      try {
        val cloudRepo = SyndicatRepository(context.applicationContext)
        if (!cloudRepo.hasExistingCloudConfig(currentUserId)) {
          val localSnapshot = localRepo.exportCurrentBackup(currentUserId)
          if (localSnapshot != null) {
            cloudRepo.restoreFromBackup(localSnapshot)
            viewModel.showToastMessage(strings.syncLocalMigratedToast)
          }
        }
      } catch (_: Exception) {
      }
    }
  }

  // Sync language, themeStyle, themeMode, fontScale & mirror cloud state to phone local storage
  LaunchedEffect(uiState, isCloudSynced, specialCalls) {
    val state = uiState
    if (state is UiState.Success) {
      val cfg = state.data.config
      if (cfg.seeded) {
        onLanguageSynced(AppLanguage.fromCode(cfg.language))
        onDisplayPrefsSynced(cfg.themeStyle, cfg.themeMode, cfg.fontScale)
        if (!cfg.onboardingCompleted) {
          showOnboardingWizard = true
        }
        BackupManager.saveAutoLocalSnapshot(context, state.data, specialCalls)
        SyndicatWidgetSnapshot.saveAndRefresh(context, state.data)
        if (isCloudSynced) {
          localRepo.mirrorFromCloud(state.data)
        }
      }
    }
  }

  // Snackbar / Toast with "تراجع" (Undo) support
  LaunchedEffect(viewModel) {
    viewModel.snackbarEvents.collect { evt ->
      val result = snackbarHostState.showSnackbar(
        message = evt.message,
        actionLabel = evt.undoLabel,
        duration = SnackbarDuration.Short,
      )
      if (result == SnackbarResult.ActionPerformed && evt.onUndo != null) {
        viewModel.triggerUndoAction(evt.onUndo)
      }
    }
  }

  BackHandler(enabled = showOnboardingWizard || currentDestination != AppDestination.DASHBOARD) {
    if (showOnboardingWizard) {
      showOnboardingWizard = false
    } else {
      currentDestination = AppDestination.DASHBOARD
    }
  }

  if (showOnboardingWizard && uiState is UiState.Success) {
    val dashData = (uiState as UiState.Success).data
    Scaffold(
      containerColor = tokens.screenBg,
      contentWindowInsets = WindowInsets.safeDrawing,
    ) { innerPadding ->
      Box(modifier = Modifier.padding(innerPadding)) {
        OnboardingWizardScreen(
          initialConfig = dashData.config,
          existingApartments = dashData.apartments,
          onCancel = { showOnboardingWizard = false },
          onComplete = { updatedCfg, explicitApts, resetBlank ->
            viewModel.saveFullConfiguration(
              updatedConfig = updatedCfg,
              resetBlankBuilding = resetBlank,
              explicitApartments = explicitApts,
              onSuccess = {
                showOnboardingWizard = false
                currentDestination = AppDestination.DASHBOARD
              },
            )
          },
        )
      }
    }
    return
  }

  val successData = (uiState as? UiState.Success)?.data
  val lateBadgeCount = successData?.lateApartments?.size ?: 0
  val unpaidBadgeCount = successData?.unpaidExpensesCount ?: 0

  if (showQuickPayPickerDialog && successData != null) {
    QuickPayApartmentPickerDialog(
      data = successData,
      onDismiss = { showQuickPayPickerDialog = false },
      onSelectApartment = { apt ->
        showQuickPayPickerDialog = false
        quickPayTargetAptNumber = apt.number
        selectDestination(AppDestination.APARTMENTS)
      },
    )
  }

  CompositionLocalProvider(LocalPrivacyMode provides isPrivacyMode) {
  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(tokens.screenBg),
  ) {
    val adaptiveSpec = computeSyndicatAdaptiveSpec(maxWidth = maxWidth, maxHeight = maxHeight)
    val useRail = adaptiveSpec.useRail
    val navItems = AppDestination.entries.map { dest ->
      val badge = when (dest) {
        AppDestination.APARTMENTS -> lateBadgeCount
        AppDestination.EXPENSES -> unpaidBadgeCount
        else -> 0
      }
      SyndicatNavItemSpec(
        route = dest.route,
        label = dest.label(strings),
        filledIcon = dest.filledIcon,
        outlinedIcon = dest.outlinedIcon,
        badgeCount = badge,
        isSelected = currentDestination == dest,
        onClick = { selectDestination(dest) },
      )
    }

    CompositionLocalProvider(LocalSyndicatAdaptiveSpec provides adaptiveSpec) {
    Row(modifier = Modifier.fillMaxSize()) {
      if (useRail) {
        SyndicatNavigationRailPane(items = navItems)
      }

      Scaffold(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight(),
        containerColor = tokens.screenBg,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = {
          SnackbarHost(
            hostState = snackbarHostState,
            snackbar = { snackData ->
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = tokens.cardBg,
                border = BorderStroke(1.dp, tokens.divider),
                tonalElevation = 0.dp,
                shadowElevation = 2.dp,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 16.dp, vertical = 8.dp),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                  Text(
                    text = snackData.visuals.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.inkDeep,
                    modifier = Modifier.weight(1f),
                  )
                  val actLabel = snackData.visuals.actionLabel
                  if (!actLabel.isNullOrBlank()) {
                    TextButton(onClick = { snackData.performAction() }) {
                      Text(
                        text = actLabel,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = tokens.linkColor,
                      )
                    }
                  }
                }
              }
            },
          )
        },
        floatingActionButton = {
          if (!isReadOnlyMode && successData != null && (currentDestination == AppDestination.DASHBOARD || currentDestination == AppDestination.APARTMENTS)) {
            ExtendedFloatingActionButton(
              onClick = { showQuickPayPickerDialog = true },
              shape = CircleShape,
              icon = {
                Icon(
                  imageVector = Icons.Default.Payments,
                  contentDescription = strings.navQuickPayFab,
                )
              },
              text = {
                Text(
                  text = strings.navQuickPayFab,
                  fontWeight = FontWeight.SemiBold,
                )
              },
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary,
              elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 4.dp,
                pressedElevation = 8.dp,
              ),
              modifier = Modifier.testTag("global_quick_pay_fab"),
            )
          }
        },
        bottomBar = {
          if (!useRail) {
            SyndicatFloatingBottomDock(items = navItems)
          }
        },
      ) { innerPadding ->
    when (val state = uiState) {
      is UiState.Loading -> {
        var showSlowSyncEscape by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
          kotlinx.coroutines.delay(10_000L)
          showSlowSyncEscape = true
        }
        SyndicatShimmerLoadingScreen(
          message = strings.loadingSync,
          modifier = Modifier.padding(innerPadding),
        ) {
          if (showSlowSyncEscape) {
            Spacer(modifier = Modifier.height(6.dp))
            SageSecondaryButton(
              text = strings.signOut,
              onClick = {
                signOutUser(credentialManager, scope, onSignOutComplete, context)
              },
              modifier = Modifier.testTag("loading_escape_sign_out_btn"),
            )
          }
        }
      }

      is UiState.Error -> {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(tokens.screenBg)
            .padding(innerPadding)
            .padding(24.dp),
          contentAlignment = Alignment.Center,
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = state.message,
              style = MaterialTheme.typography.bodyLarge,
              color = tokens.negativeDeep,
            )
            Spacer(modifier = Modifier.height(16.dp))
            LimePrimaryButton(
              text = strings.signOut,
              onClick = {
                signOutUser(credentialManager, scope, onSignOutComplete, context)
              },
            )
          }
        }
      }

      is UiState.Success -> {
        val data = state.data
        val contentMod = Modifier.padding(innerPadding)
        Crossfade(
          targetState = currentDestination,
          animationSpec = tween(durationMillis = 210),
          label = "nav_crossfade",
        ) { dest ->
        when (dest) {
          AppDestination.DASHBOARD -> {
            DashboardScreen(
              data = data,
              isBackupOverdue = isBackupOverdue,
              onTriggerBackupJson = {
                BackupManager.exportAndShareJsonBackup(
                  context = context,
                  data = data,
                  onError = { err ->
                    scope.launch { snackbarHostState.showSnackbar(err) }
                  },
                  specialCalls = specialCalls,
                )
                isBackupOverdue = false
              },
              onOpenWizard = { showOnboardingWizard = true },
              onMarkReminded = { apt, updatedPhone, updatedTemplate ->
                viewModel.markWhatsAppReminderSent(apt, data.config, updatedPhone, updatedTemplate)
              },
              onNavigateToPosters = { selectDestination(AppDestination.POSTER) },
              onNavigateToProjects = {
                projectsInitialCallsOnly = false
                selectDestination(AppDestination.PROJECTS)
              },
              onNavigateToApartments = {
                viewModel.selectStatusFilter(StatusFilter.ALL)
                selectDestination(AppDestination.APARTMENTS)
              },
              onNavigateToExpenses = {
                expensesInitialUnpaidOnly = false
                selectDestination(AppDestination.EXPENSES)
              },
              onNavigateToLateApartments = {
                viewModel.selectStatusFilter(StatusFilter.LATE)
                selectDestination(AppDestination.APARTMENTS)
              },
              onInfoMessage = { msg -> viewModel.showToastMessage(msg) },
              isCloudSynced = isCloudSynced,
              specialCalls = specialCalls,
              onNavigateToUnpaidExpenses = {
                expensesInitialUnpaidOnly = true
                currentDestination = AppDestination.EXPENSES
              },
              onNavigateToSpecialCalls = {
                projectsInitialCallsOnly = true
                currentDestination = AppDestination.PROJECTS
              },
              onQuickPayTap = { showQuickPayPickerDialog = true },
              isPrivacyMode = isPrivacyMode,
              onTogglePrivacy = { isPrivacyMode = !isPrivacyMode },
              onSearchSelectApartment = { apt ->
                viewModel.updateSearchQuery(apt.displayNumber)
                viewModel.selectStatusFilter(StatusFilter.ALL)
                viewModel.selectFloor(null)
                selectDestination(AppDestination.APARTMENTS)
              },
              buildings = buildings,
              activeBuildingId = activeBuildingId,
              onSelectBuilding = { id ->
                extrasStore.selectBuilding(id)
                scope.launch { localRepo.switchBuilding(id, preserveLanguage = currentLanguage.code) }
              },
              isReadOnlyMode = isReadOnlyMode,
              modifier = contentMod,
            )
          }

          AppDestination.APARTMENTS -> {
            ApartmentsScreen(
              data = data,
              searchQuery = searchQuery,
              selectedFloor = selectedFloor,
              statusFilter = statusFilter,
              onSearchQueryChange = viewModel::updateSearchQuery,
              onSelectFloor = viewModel::selectFloor,
              onSelectStatusFilter = viewModel::selectStatusFilter,
              onSaveApartment = { updated, previous ->
                viewModel.saveApartment(
                  apartment = updated,
                  previousSnapshot = previous,
                  config = data.config,
                  baseYear = data.baseYear,
                )
              },
              onSaveApartmentWithPayment = { updated, previous, methodCode, dateStr, notes ->
                viewModel.saveApartment(
                  apartment = updated,
                  previousSnapshot = previous,
                  config = data.config,
                  baseYear = data.baseYear,
                  paymentMethod = methodCode,
                  paymentDate = dateStr,
                  paymentNotes = notes,
                )
              },
              onSelectYear = viewModel::selectYear,
              onDeletePayment = { payment ->
                viewModel.deletePaymentEntry(
                  payment = payment,
                  baseYear = data.baseYear,
                  baseApartments = data.baseApartments,
                  config = data.config,
                )
              },
              onMarkReminded = { apt, updatedPhone, updatedTemplate ->
                viewModel.markWhatsAppReminderSent(apt, data.config, updatedPhone, updatedTemplate)
              },
              onRequestReceiptNo = { apt -> viewModel.nextReceiptNumber(apt) },
              onExportError = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } },
              initialQuickPayAptNumber = quickPayTargetAptNumber,
              onConsumeQuickPayTarget = { quickPayTargetAptNumber = null },
              specialCalls = specialCalls,
              isReadOnlyMode = isReadOnlyMode,
              modifier = contentMod,
            )
          }

          AppDestination.EXPENSES -> {
            ExpensesScreen(
              data = data,
              onAddExpense = { title, cat, amt, paid, supplier, date, notes ->
                viewModel.addExpense(title, cat, amt, paid, supplier, date, notes, data.config)
              },
              onUpdateExpense = { exp -> viewModel.updateExpense(exp, data.config) },
              onDeleteExpense = { exp -> viewModel.deleteExpense(exp, data.config) },
              onExportError = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } },
              onAddExpenseWithId = { title, cat, amt, paid, supplier, date, notes, explicitId ->
                viewModel.addExpense(title, cat, amt, paid, supplier, date, notes, data.config, explicitId = explicitId)
              },
              initialUnpaidOnly = expensesInitialUnpaidOnly,
              recurringTemplates = recurringTemplates,
              vendors = vendors,
              onSeedRecurringPresets = {
                extrasStore.seedStarterRecurringTemplates(currentLanguage == AppLanguage.AR)
              },
              onUpsertRecurring = { tpl -> extrasStore.upsertRecurringTemplate(tpl) },
              onDeleteRecurring = { id -> extrasStore.deleteRecurringTemplate(id) },
              onMarkRecurringApplied = { ids, mKey -> extrasStore.markRecurringTemplatesApplied(ids, mKey) },
              onUpsertVendor = { v -> extrasStore.upsertVendor(v) },
              onDeleteVendor = { id -> extrasStore.deleteVendor(id) },
              prefillIncidentDraft = prefillIncidentForExpense,
              onIncidentConverted = { inc, expId -> extrasStore.linkIncidentExpense(inc.id, expId) },
              onConsumePrefillIncident = { prefillIncidentForExpense = null },
              onInfoMessage = { msg -> viewModel.showToastMessage(msg) },
              isReadOnlyMode = isReadOnlyMode,
              modifier = contentMod,
            )
          }

          AppDestination.PROJECTS -> {
            ProjectsScreen(
              data = data,
              onAddProject = { proj -> viewModel.addProject(proj, data.config) },
              onUpdateProject = { proj, prev -> viewModel.updateProject(proj, prev, data.config) },
              onDeleteProject = { proj -> viewModel.deleteProject(proj, data.config) },
              onLoadStarterProjects = { viewModel.seedStarterProjects(data.config) },
              specialCalls = specialCalls,
              onCreateSpecialCall = { titleFr, titleAr, amountPerAptDh, projectId, dueDateLabel ->
                extrasStore.createSpecialCall(titleFr, titleAr, amountPerAptDh, projectId, dueDateLabel)
              },
              onToggleSpecialCallPayment = { callId, aptNum, paid ->
                extrasStore.toggleSpecialCallApartmentPayment(callId, aptNum, paid)
              },
              onDeleteSpecialCall = { callId ->
                extrasStore.deleteSpecialCall(callId)
              },
              onExportError = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } },
              initialFocusSpecialCalls = projectsInitialCallsOnly,
              incidents = incidents,
              onUpsertIncident = { inc -> extrasStore.upsertIncident(inc) },
              onCycleIncidentStatus = { id -> extrasStore.cycleIncidentStatus(id) },
              onDeleteIncident = { id -> extrasStore.deleteIncident(id) },
              onConvertIncidentToExpense = { inc ->
                prefillIncidentForExpense = inc
                selectDestination(AppDestination.EXPENSES)
              },
              agMinutes = agMinutes,
              onSaveAgMinutes = { draft -> extrasStore.updateAgMinutes { draft } },
              isReadOnlyMode = isReadOnlyMode,
              modifier = contentMod,
            )
          }

          AppDestination.POSTER -> {
            PosterScreen(
              data = data,
              onExportError = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } },
              modifier = contentMod,
            )
          }

          AppDestination.SETTINGS -> {
            SettingsScreen(
              data = data,
              currentUserId = currentUserId,
              userEmail = userEmail,
              currentLanguage = currentLanguage,
              isSaving = isSaving,
              onSelectLanguage = { newLang ->
                onUserSelectedLanguage(newLang)
                viewModel.updateLanguageInstant(newLang.code)
              },
              onUpdateDisplayPrefs = { digitsAr, thStyle, thMode, fScale ->
                val nextStyle = sanitizeThemeStyle(thStyle ?: data.config.themeStyle, data.config.themeStyle)
                val nextMode = thMode ?: data.config.themeMode
                val nextScale = fScale ?: data.config.fontScale
                onDisplayPrefsSynced(nextStyle, nextMode, nextScale)
                viewModel.updateDisplayPrefsInstant(digitsAr, thStyle, thMode, fScale)
              },
              onOpenWizard = { showOnboardingWizard = true },
              onSaveConfig = { updatedCfg, resetBlank ->
                viewModel.saveFullConfiguration(updatedCfg, resetBlankBuilding = resetBlank)
              },
              onRestoreBackup = { parsed ->
                viewModel.restoreBackupData(parsed, currentLanguage == AppLanguage.AR)
              },
              onImportCsvOwners = { rows ->
                viewModel.importBulkOwners(rows, currentLanguage == AppLanguage.AR)
              },
              onResetC49 = {
                viewModel.resetToBaselinePdf(preserveLanguage = currentLanguage.code)
              },
              onBackupExported = {
                isBackupOverdue = false
              },
              onPinUpdated = onPinUpdated,
              onSignOut = {
                signOutUser(credentialManager, scope, onSignOutComplete, context)
              },
              onMessage = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } },
              isCloudSynced = isCloudSynced,
              onConnectGoogleSync = onRequestGoogleSync,
              onPushLocalToCloud = {
                val localSnap = localRepo.exportCurrentBackup(currentUserId)
                if (localSnap != null) {
                  viewModel.restoreBackupData(localSnap, currentLanguage == AppLanguage.AR)
                }
              },
              auditLogs = auditLogs,
              buildings = buildings,
              activeBuildingId = activeBuildingId,
              onSelectBuilding = { id ->
                extrasStore.selectBuilding(id)
                scope.launch { localRepo.switchBuilding(id, preserveLanguage = currentLanguage.code) }
              },
              onCreateBuilding = { name, city ->
                val created = extrasStore.addBuilding(name, city)
                scope.launch {
                  localRepo.switchBuilding(
                    created.id,
                    defaultBuildingName = created.name,
                    preserveLanguage = currentLanguage.code,
                  )
                }
              },
              onDeleteBuilding = { id ->
                extrasStore.deleteBuilding(id)
                scope.launch {
                  localRepo.deleteBuildingStorage(id)
                  localRepo.switchBuilding(extrasStore.activeBuildingId.value, preserveLanguage = currentLanguage.code)
                }
              },
              roleMode = roleMode,
              onSetRoleMode = { mode -> extrasStore.setRoleMode(mode) },
              specialCalls = specialCalls,
              onImportCouncilSnapshot = { parsed ->
                viewModel.restoreBackupData(parsed, currentLanguage == AppLanguage.AR)
              },
              modifier = contentMod,
            )
          }
        }
        } // end Crossfade
      }
        } // end when(uiState)
      } // end Scaffold
    } // end Row
    } // end CompositionLocalProvider(LocalSyndicatAdaptiveSpec)
  } // end BoxWithConstraints
  } // end CompositionLocalProvider(LocalPrivacyMode)
}

@Composable
private fun QuickPayApartmentPickerDialog(
  data: SyndicatDashboardData,
  onDismiss: () -> Unit,
  onSelectApartment: (ApartmentRecord) -> Unit,
) {
  val strings = LocalStrings.current
  val tokens = LocalSyndicatTokens.current
  val cfg = data.config
  val cur = cfg.currency
  val dig = cfg.digitsAr
  val isAr = strings.language == AppLanguage.AR

  val sortedApartments = remember(data.apartments, cfg) {
    data.apartments
      .filter { !it.isVacantForConfig(cfg) }
      .sortedWith(
        compareByDescending<ApartmentRecord> { it.remainingDh(cfg) }
          .thenBy { it.number },
      )
  }

  SyndicatDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = strings.quickPayPickerTitle,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        color = tokens.inkDeep,
      )
    },
    text = {
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 360.dp)
          .testTag("quick_pay_picker_list"),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 2.dp),
      ) {
        items(sortedApartments, key = { it.number }) { apt ->
          val rem = apt.remainingDh(cfg)
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (rem > 0L) tokens.surfaceSoft else tokens.cardBg,
            border = BorderStroke(tokens.borderStrokeDp.dp, tokens.divider),
            modifier = Modifier
              .fillMaxWidth()
              .minimumInteractiveComponentSize()
              .clip(RoundedCornerShape(14.dp))
              .clickable { onSelectApartment(apt) }
              .testTag("quick_pay_pick_apt_${apt.number}"),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Surface(
                shape = CircleShape,
                color = if (rem > 0L) tokens.primaryTint else tokens.surfaceStrong,
                modifier = Modifier.size(38.dp),
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                    text = localizeDigits(apt.displayNumber, dig),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (rem > 0L) tokens.linkColor else tokens.ink,
                  )
                }
              }
              Column(modifier = Modifier.weight(1f)) {
                val isPrivacy = LocalPrivacyMode.current
                val rawOwner = apt.displayOwner(isAr).ifBlank { "${strings.colAppt} ${localizeDigits(apt.displayNumber, dig)}" }
                Text(
                  text = maskSensitiveName(rawOwner, isPrivacy),
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Medium,
                  color = tokens.inkDeep,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                val paidCount = cfg.activeMonthColumns.count { col ->
                  val exp = apt.expectedFeeForMonth(col, cfg)
                  exp > 0L && apt.amountFor(col) >= exp
                }
                Text(
                  text = "${strings.floorDisplay(apt.floor)} · ${formatNumber(paidCount.toLong(), dig)}/${formatNumber(cfg.activeMonthColumns.size.toLong(), dig)}",
                  style = MaterialTheme.typography.labelSmall,
                  color = tokens.mute,
                )
              }
              if (rem > 0L) {
                Text(
                  text = maskSensitiveDh(formatDh(rem, cur, dig), LocalPrivacyMode.current),
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = tokens.negativeDeep,
                )
              } else {
                StatusPillBadge(status = PaymentBadgeStatus.PAID)
              }
            }
          }
        }
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
