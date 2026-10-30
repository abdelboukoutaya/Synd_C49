package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.local.LocalSyndicatRepository
import com.example.data.repository.LOCAL_DEVICE_USER_ID
import com.example.data.repository.SyndicatDataSource
import com.example.data.repository.SyndicatRepository
import com.example.ui.components.LimePrimaryButton
import com.example.ui.components.PinLockGateScreen
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LanguagePreferences
import com.example.ui.i18n.LocalStrings
import com.example.ui.i18n.SyndicatStrings
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
import com.example.ui.viewmodel.SyndicatViewModel
import com.example.ui.viewmodel.UiState
import com.example.util.BackupManager
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

enum class AppDestination(
  val route: String,
  val filledIcon: ImageVector,
  val outlinedIcon: ImageVector,
) {
  DASHBOARD("dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
  APARTMENTS("apartments", Icons.Filled.Apartment, Icons.Outlined.Apartment),
  EXPENSES("expenses", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
  PROJECTS("projects", Icons.Filled.Construction, Icons.Outlined.Construction),
  POSTER("poster", Icons.Filled.PictureAsPdf, Icons.Outlined.PictureAsPdf),
  SETTINGS("settings", Icons.Filled.Settings, Icons.Outlined.Settings);

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
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      SyndicatRootApp()
    }
  }
}

@Composable
fun SyndicatRootApp() {
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
  val snackbarHostState = remember { SnackbarHostState() }

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
  LaunchedEffect(uiState, isCloudSynced) {
    val state = uiState
    if (state is UiState.Success) {
      val cfg = state.data.config
      if (cfg.seeded) {
        onLanguageSynced(AppLanguage.fromCode(cfg.language))
        onDisplayPrefsSynced(cfg.themeStyle, cfg.themeMode, cfg.fontScale)
        if (!cfg.onboardingCompleted) {
          showOnboardingWizard = true
        }
        BackupManager.saveAutoLocalSnapshot(context, state.data)
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

  Scaffold(
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
            androidx.compose.foundation.layout.Row(
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
    bottomBar = {
      Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(thickness = 1.dp, color = tokens.divider)
        NavigationBar(
          containerColor = tokens.navBg,
          tonalElevation = 0.dp,
          windowInsets = WindowInsets.navigationBars,
        ) {
          AppDestination.entries.forEach { dest ->
            val selected = currentDestination == dest
            val label = dest.label(strings)
            NavigationBarItem(
              selected = selected,
              onClick = { currentDestination = dest },
              icon = {
                Icon(
                  imageVector = if (selected) dest.filledIcon else dest.outlinedIcon,
                  contentDescription = label,
                )
              },
              label = {
                Text(
                  text = label,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                  maxLines = 1,
                  softWrap = false,
                  overflow = TextOverflow.Ellipsis,
                )
              },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = tokens.linkColor,
                selectedTextColor = tokens.inkDeep,
                indicatorColor = tokens.primaryTint,
                unselectedIconColor = tokens.mute,
                unselectedTextColor = tokens.mute,
              ),
              modifier = Modifier.testTag("nav_${dest.route}"),
            )
          }
        }
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
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(tokens.screenBg)
            .padding(innerPadding)
            .padding(horizontal = 24.dp),
          contentAlignment = Alignment.Center,
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            CircularProgressIndicator(color = tokens.ink)
            Text(
              text = strings.loadingSync,
              style = MaterialTheme.typography.bodyMedium,
              color = tokens.body,
            )
            if (showSlowSyncEscape) {
              Spacer(modifier = Modifier.height(8.dp))
              com.example.ui.components.SageSecondaryButton(
                text = strings.signOut,
                onClick = {
                  signOutUser(credentialManager, scope, onSignOutComplete, context)
                },
                modifier = Modifier.testTag("loading_escape_sign_out_btn"),
              )
            }
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
        when (currentDestination) {
          AppDestination.DASHBOARD -> {
            DashboardScreen(
              data = data,
              isBackupOverdue = isBackupOverdue,
              onTriggerBackupJson = {
                BackupManager.exportAndShareJsonBackup(context, data) { err ->
                  scope.launch { snackbarHostState.showSnackbar(err) }
                }
                isBackupOverdue = false
              },
              onOpenWizard = { showOnboardingWizard = true },
              onMarkReminded = { apt, updatedPhone, updatedTemplate ->
                viewModel.markWhatsAppReminderSent(apt, data.config, updatedPhone, updatedTemplate)
              },
              onNavigateToPosters = { currentDestination = AppDestination.POSTER },
              onNavigateToProjects = { currentDestination = AppDestination.PROJECTS },
              onNavigateToApartments = {
                viewModel.selectStatusFilter(StatusFilter.ALL)
                currentDestination = AppDestination.APARTMENTS
              },
              onNavigateToExpenses = { currentDestination = AppDestination.EXPENSES },
              onNavigateToLateApartments = {
                viewModel.selectStatusFilter(StatusFilter.LATE)
                currentDestination = AppDestination.APARTMENTS
              },
              onInfoMessage = { msg -> viewModel.showToastMessage(msg) },
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
              modifier = contentMod,
            )
          }
        }
      }
    }
  }
}
