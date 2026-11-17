package com.example.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.MainActivity
import com.example.ui.viewmodel.SyndicatDashboardData
import com.example.ui.viewmodel.formatDh
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class WidgetFormatPreset(val code: String) {
  COMPACT("compact"),
  CLASSIC("classic"),
  DETAILED("detailed");

  companion object {
    fun fromCode(raw: String?): WidgetFormatPreset =
      entries.firstOrNull { it.code.equals(raw, ignoreCase = true) } ?: CLASSIC
  }
}

enum class WidgetThemePreset(val code: String) {
  NAVY_PRO("navy"),
  LIGHT_CLEAN("light"),
  AMOLED_DARK("dark"),
  EMERALD_FINTECH("emerald");

  companion object {
    fun fromCode(raw: String?): WidgetThemePreset =
      entries.firstOrNull { it.code.equals(raw, ignoreCase = true) } ?: NAVY_PRO
  }
}

enum class WidgetQuickAction(val code: String) {
  QUICK_PAY("quick_pay"),
  LATE_LIST("late"),
  ADD_EXPENSE("expenses"),
  OPEN_DASHBOARD("dashboard");

  companion object {
    fun fromCode(raw: String?): WidgetQuickAction =
      entries.firstOrNull { it.code.equals(raw, ignoreCase = true) } ?: QUICK_PAY
  }
}

data class SyndicatWidgetPrefs(
  val theme: WidgetThemePreset = WidgetThemePreset.NAVY_PRO,
  val primaryAction: WidgetQuickAction = WidgetQuickAction.QUICK_PAY,
  val privacyMask: Boolean = false,
  val showRate: Boolean = true,
  val showLateCount: Boolean = true,
  val showExpenses: Boolean = true,
  val showLatePreviewList: Boolean = true,
  val previewFormat: WidgetFormatPreset = WidgetFormatPreset.CLASSIC,
)

data class WidgetSnapshotState(
  val residenceName: String,
  val cashText: String,
  val collectedText: String,
  val unpaidExpensesText: String,
  val collectionRateText: String,
  val lateCount: Int,
  val latePreviewLines: List<String>,
  val isArabic: Boolean,
  val prefs: SyndicatWidgetPrefs = SyndicatWidgetPrefs(),
) {
  val displayedCashText: String
    get() = if (prefs.privacyMask) "••••••" else cashText

  val displayedCollectedText: String
    get() = if (prefs.privacyMask) "••••" else collectedText

  val displayedExpensesText: String
    get() = if (prefs.privacyMask) "••••" else unpaidExpensesText
}

data class WidgetColorPalette(
  val bg: Color,
  val surfaceSoft: Color,
  val primaryText: Color,
  val secondaryText: Color,
  val ctaBg: Color,
  val ctaText: Color,
  val positiveText: Color,
  val warningText: Color,
)

fun widgetPaletteFor(preset: WidgetThemePreset): WidgetColorPalette = when (preset) {
  WidgetThemePreset.NAVY_PRO -> WidgetColorPalette(
    bg = Color(0xFF0E1B33),
    surfaceSoft = Color(0xFF17294A),
    primaryText = Color.White,
    secondaryText = Color(0xFFC2D1EB),
    ctaBg = Color(0xFF0052FF),
    ctaText = Color.White,
    positiveText = Color(0xFF4ADE80),
    warningText = Color(0xFFFBBF24),
  )
  WidgetThemePreset.LIGHT_CLEAN -> WidgetColorPalette(
    bg = Color(0xFFFFFFFF),
    surfaceSoft = Color(0xFFF3F6FA),
    primaryText = Color(0xFF0A1317),
    secondaryText = Color(0xFF4E5B6B),
    ctaBg = Color(0xFF0064E0),
    ctaText = Color.White,
    positiveText = Color(0xFF098551),
    warningText = Color(0xFFB45309),
  )
  WidgetThemePreset.AMOLED_DARK -> WidgetColorPalette(
    bg = Color(0xFF0A0B0D),
    surfaceSoft = Color(0xFF16181C),
    primaryText = Color(0xFFF5F7FA),
    secondaryText = Color(0xFF9BA5B4),
    ctaBg = Color(0xFF2970FF),
    ctaText = Color.White,
    positiveText = Color(0xFF34D399),
    warningText = Color(0xFFF59E0B),
  )
  WidgetThemePreset.EMERALD_FINTECH -> WidgetColorPalette(
    bg = Color(0xFF0A2E25),
    surfaceSoft = Color(0xFF124135),
    primaryText = Color.White,
    secondaryText = Color(0xFFB8E2D4),
    ctaBg = Color(0xFF9FE870),
    ctaText = Color(0xFF0A2E25),
    positiveText = Color(0xFF9FE870),
    warningText = Color(0xFFFDE047),
  )
}

object SyndicatWidgetSnapshot {
  private const val PREFS = "syndicat_widget_snapshot"
  private const val KEY_RESIDENCE = "residence"
  private const val KEY_CASH = "cash"
  private const val KEY_COLLECTED = "collected"
  private const val KEY_EXPENSES = "unpaid_expenses"
  private const val KEY_RATE = "rate"
  private const val KEY_LATE = "late"
  private const val KEY_LATE_LIST = "late_list"
  private const val KEY_IS_AR = "is_ar"

  // Customization preferences
  private const val KEY_OPT_THEME = "opt_theme"
  private const val KEY_OPT_ACTION = "opt_action"
  private const val KEY_OPT_PRIVACY = "opt_privacy"
  private const val KEY_OPT_SHOW_RATE = "opt_show_rate"
  private const val KEY_OPT_SHOW_LATE_COUNT = "opt_show_late_count"
  private const val KEY_OPT_SHOW_EXPENSES = "opt_show_expenses"
  private const val KEY_OPT_SHOW_LATE_LIST = "opt_show_late_list"
  private const val KEY_OPT_PREVIEW_FORMAT = "opt_preview_format"

  fun loadPrefs(context: Context): SyndicatWidgetPrefs {
    val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    return SyndicatWidgetPrefs(
      theme = WidgetThemePreset.fromCode(prefs.getString(KEY_OPT_THEME, null)),
      primaryAction = WidgetQuickAction.fromCode(prefs.getString(KEY_OPT_ACTION, null)),
      privacyMask = prefs.getBoolean(KEY_OPT_PRIVACY, false),
      showRate = prefs.getBoolean(KEY_OPT_SHOW_RATE, true),
      showLateCount = prefs.getBoolean(KEY_OPT_SHOW_LATE_COUNT, true),
      showExpenses = prefs.getBoolean(KEY_OPT_SHOW_EXPENSES, true),
      showLatePreviewList = prefs.getBoolean(KEY_OPT_SHOW_LATE_LIST, true),
      previewFormat = WidgetFormatPreset.fromCode(prefs.getString(KEY_OPT_PREVIEW_FORMAT, null)),
    )
  }

  fun savePrefs(context: Context, next: SyndicatWidgetPrefs) {
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
      .edit()
      .putString(KEY_OPT_THEME, next.theme.code)
      .putString(KEY_OPT_ACTION, next.primaryAction.code)
      .putBoolean(KEY_OPT_PRIVACY, next.privacyMask)
      .putBoolean(KEY_OPT_SHOW_RATE, next.showRate)
      .putBoolean(KEY_OPT_SHOW_LATE_COUNT, next.showLateCount)
      .putBoolean(KEY_OPT_SHOW_EXPENSES, next.showExpenses)
      .putBoolean(KEY_OPT_SHOW_LATE_LIST, next.showLatePreviewList)
      .putString(KEY_OPT_PREVIEW_FORMAT, next.previewFormat.code)
      .apply()
    refreshAllWidgets(context)
  }

  fun load(context: Context): WidgetSnapshotState {
    val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    val rawLines = prefs.getString(KEY_LATE_LIST, "") ?: ""
    val lines = rawLines.split("\n").map { it.trim() }.filter { it.isNotBlank() }.take(3)
    return WidgetSnapshotState(
      residenceName = prefs.getString(KEY_RESIDENCE, "SYNDICAT") ?: "SYNDICAT",
      cashText = prefs.getString(KEY_CASH, "0 DH") ?: "0 DH",
      collectedText = prefs.getString(KEY_COLLECTED, "0 DH") ?: "0 DH",
      unpaidExpensesText = prefs.getString(KEY_EXPENSES, "0 DH") ?: "0 DH",
      collectionRateText = prefs.getString(KEY_RATE, "0%") ?: "0%",
      lateCount = prefs.getInt(KEY_LATE, 0),
      latePreviewLines = lines,
      isArabic = prefs.getBoolean(KEY_IS_AR, false),
      prefs = loadPrefs(context),
    )
  }

  fun saveAndRefresh(context: Context, data: SyndicatDashboardData) {
    val cfg = data.config
    val cur = cfg.currency
    val dig = cfg.digitsAr
    val isAr = cfg.language == "ar"
    val cashStr = formatDh(data.cashNowDh, cur, dig)
    val collectedStr = formatDh(data.totalCaisseDh, cur, dig)
    val unpaidExpStr = formatDh(data.unpaidExpensesDh, cur, dig)
    val rateStr = "${data.collectionRatePercent}%"
    val lateApts = data.lateApartments
    val topLate = lateApts
      .sortedByDescending { it.remainingDh(cfg) }
      .take(3)
      .joinToString("\n") { apt ->
        val owner = apt.displayOwner(isAr).ifBlank { if (isAr) "مالك" else "Occupant" }
        val rem = formatDh(apt.remainingDh(cfg), cur, dig)
        val unitLabel = if (isAr) "شقة ${apt.displayNumber}" else "Appt ${apt.displayNumber}"
        "$unitLabel · $owner : $rem"
      }

    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
      .edit()
      .putString(KEY_RESIDENCE, cfg.residenceName.ifBlank { "SYNDICAT" })
      .putString(KEY_CASH, cashStr)
      .putString(KEY_COLLECTED, collectedStr)
      .putString(KEY_EXPENSES, unpaidExpStr)
      .putString(KEY_RATE, rateStr)
      .putInt(KEY_LATE, lateApts.size)
      .putString(KEY_LATE_LIST, topLate)
      .putBoolean(KEY_IS_AR, isAr)
      .apply()

    refreshAllWidgets(context)
  }

  fun refreshAllWidgets(context: Context) {
    CoroutineScope(Dispatchers.Default).launch {
      runCatching { SyndicatCompactWidget().updateAll(context) }
      runCatching { SyndicatGlanceWidget().updateAll(context) }
      runCatching { SyndicatDetailedWidget().updateAll(context) }
    }
  }

  fun pinWidget(context: Context, format: WidgetFormatPreset): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
    val mgr = AppWidgetManager.getInstance(context)
    if (!mgr.isRequestPinAppWidgetSupported) return false
    val receiverCls = when (format) {
      WidgetFormatPreset.COMPACT -> SyndicatCompactWidgetReceiver::class.java
      WidgetFormatPreset.CLASSIC -> SyndicatGlanceWidgetReceiver::class.java
      WidgetFormatPreset.DETAILED -> SyndicatDetailedWidgetReceiver::class.java
    }
    return runCatching {
      mgr.requestPinAppWidget(ComponentName(context, receiverCls), null, null)
    }.getOrDefault(false)
  }
}

private fun buildWidgetIntent(
  context: Context,
  action: WidgetQuickAction? = null,
  customRoute: String? = null,
): Intent {
  val route = customRoute ?: action?.code ?: WidgetQuickAction.OPEN_DASHBOARD.code
  return Intent(context, MainActivity::class.java).apply {
    this.action = "com.example.syndicat.WIDGET_$route"
    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    if (route == WidgetQuickAction.QUICK_PAY.code) {
      putExtra(MainActivity.EXTRA_OPEN_QUICK_PAY, true)
    }
    putExtra(MainActivity.EXTRA_WIDGET_DESTINATION, route)
  }
}

private fun quickActionLabel(action: WidgetQuickAction, isAr: Boolean): String = when (action) {
  WidgetQuickAction.QUICK_PAY -> if (isAr) "+ تحصيل" else "+ Encaisser"
  WidgetQuickAction.LATE_LIST -> if (isAr) "المتأخرون" else "Retards"
  WidgetQuickAction.ADD_EXPENSE -> if (isAr) "+ مصروف" else "+ Dépense"
  WidgetQuickAction.OPEN_DASHBOARD -> if (isAr) "فتح" else "Ouvrir"
}

class SyndicatCompactWidget : GlanceAppWidget() {
  override suspend fun provideGlance(context: Context, id: GlanceId) {
    val snap = SyndicatWidgetSnapshot.load(context)
    provideContent {
      GlanceTheme {
        SyndicatCompactWidgetContent(context = context, snap = snap)
      }
    }
  }
}

class SyndicatGlanceWidget : GlanceAppWidget() {
  companion object {
    private val COMPACT_SIZE = DpSize(130.dp, 56.dp)
    private val CLASSIC_SIZE = DpSize(240.dp, 96.dp)
    private val DETAILED_SIZE = DpSize(240.dp, 185.dp)
  }

  override val sizeMode = SizeMode.Responsive(
    setOf(COMPACT_SIZE, CLASSIC_SIZE, DETAILED_SIZE),
  )

  override suspend fun provideGlance(context: Context, id: GlanceId) {
    val snap = SyndicatWidgetSnapshot.load(context)
    provideContent {
      GlanceTheme {
        val size = LocalSize.current
        when {
          size.height < 85.dp || size.width < 200.dp ->
            SyndicatCompactWidgetContent(context = context, snap = snap)
          size.height >= 175.dp ->
            SyndicatDetailedWidgetContent(context = context, snap = snap)
          else ->
            SyndicatClassicWidgetContent(context = context, snap = snap)
        }
      }
    }
  }
}

class SyndicatDetailedWidget : GlanceAppWidget() {
  override suspend fun provideGlance(context: Context, id: GlanceId) {
    val snap = SyndicatWidgetSnapshot.load(context)
    provideContent {
      GlanceTheme {
        SyndicatDetailedWidgetContent(context = context, snap = snap)
      }
    }
  }
}

@Composable
private fun SyndicatCompactWidgetContent(
  context: Context,
  snap: WidgetSnapshotState,
) {
  val palette = widgetPaletteFor(snap.prefs.theme)
  val cardBg = ColorProvider(palette.bg)
  val pillBg = ColorProvider(palette.ctaBg)
  val primaryText = ColorProvider(palette.primaryText)
  val secondaryText = ColorProvider(palette.secondaryText)
  val ctaText = ColorProvider(palette.ctaText)
  val openIntent = buildWidgetIntent(context, customRoute = WidgetQuickAction.OPEN_DASHBOARD.code)
  val actionIntent = buildWidgetIntent(context, action = snap.prefs.primaryAction)

  Row(
    modifier = GlanceModifier
      .fillMaxSize()
      .background(cardBg)
      .cornerRadius(18.dp)
      .padding(horizontal = 12.dp, vertical = 10.dp)
      .clickable(actionStartActivity(openIntent)),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(modifier = GlanceModifier.defaultWeight()) {
      Text(
        text = snap.residenceName,
        style = TextStyle(color = secondaryText, fontSize = 11.sp, fontWeight = FontWeight.Medium),
        maxLines = 1,
      )
      Spacer(modifier = GlanceModifier.height(2.dp))
      Text(
        text = snap.displayedCashText,
        style = TextStyle(color = primaryText, fontSize = 17.sp, fontWeight = FontWeight.Bold),
        maxLines = 1,
      )
    }
    Spacer(modifier = GlanceModifier.width(8.dp))
    Box(
      modifier = GlanceModifier
        .background(pillBg)
        .cornerRadius(14.dp)
        .padding(horizontal = 11.dp, vertical = 8.dp)
        .clickable(actionStartActivity(actionIntent)),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = quickActionLabel(snap.prefs.primaryAction, snap.isArabic),
        style = TextStyle(color = ctaText, fontSize = 12.sp, fontWeight = FontWeight.Bold),
        maxLines = 1,
      )
    }
  }
}

@Composable
private fun SyndicatClassicWidgetContent(
  context: Context,
  snap: WidgetSnapshotState,
) {
  val palette = widgetPaletteFor(snap.prefs.theme)
  val cardBg = ColorProvider(palette.bg)
  val pillBg = ColorProvider(palette.ctaBg)
  val softBg = ColorProvider(palette.surfaceSoft)
  val primaryText = ColorProvider(palette.primaryText)
  val secondaryText = ColorProvider(palette.secondaryText)
  val ctaText = ColorProvider(palette.ctaText)
  val positiveText = ColorProvider(palette.positiveText)
  val warningText = ColorProvider(palette.warningText)

  val openIntent = buildWidgetIntent(context, customRoute = WidgetQuickAction.OPEN_DASHBOARD.code)
  val actionIntent = buildWidgetIntent(context, action = snap.prefs.primaryAction)
  val studioIntent = buildWidgetIntent(context, customRoute = MainActivity.WIDGET_ROUTE_STUDIO)

  Column(
    modifier = GlanceModifier
      .fillMaxSize()
      .background(cardBg)
      .cornerRadius(20.dp)
      .padding(14.dp)
      .clickable(actionStartActivity(openIntent)),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(
      modifier = GlanceModifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(modifier = GlanceModifier.defaultWeight()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = snap.residenceName,
            style = TextStyle(color = secondaryText, fontSize = 12.sp, fontWeight = FontWeight.Medium),
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight(),
          )
          Box(
            modifier = GlanceModifier
              .background(softBg)
              .cornerRadius(8.dp)
              .padding(horizontal = 6.dp, vertical = 2.dp)
              .clickable(actionStartActivity(studioIntent)),
          ) {
            Text(
              text = "⚙",
              style = TextStyle(color = secondaryText, fontSize = 11.sp),
            )
          }
        }
        Spacer(modifier = GlanceModifier.height(2.dp))
        Text(
          text = snap.displayedCashText,
          style = TextStyle(color = primaryText, fontSize = 22.sp, fontWeight = FontWeight.Bold),
          maxLines = 1,
        )
      }
      Spacer(modifier = GlanceModifier.width(8.dp))
      Box(
        modifier = GlanceModifier
          .background(pillBg)
          .cornerRadius(14.dp)
          .padding(horizontal = 12.dp, vertical = 9.dp)
          .clickable(actionStartActivity(actionIntent)),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = quickActionLabel(snap.prefs.primaryAction, snap.isArabic),
          style = TextStyle(color = ctaText, fontSize = 13.sp, fontWeight = FontWeight.Bold),
          maxLines = 1,
        )
      }
    }

    if (snap.prefs.showRate || snap.prefs.showLateCount) {
      Spacer(modifier = GlanceModifier.height(10.dp))
      Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        if (snap.prefs.showRate) {
          val rateLabel = if (snap.isArabic) "التحصيل: ${snap.collectionRateText}" else "Recouvrement : ${snap.collectionRateText}"
          Text(
            text = rateLabel,
            style = TextStyle(color = positiveText, fontSize = 12.sp, fontWeight = FontWeight.Medium),
            maxLines = 1,
          )
        }
        Spacer(modifier = GlanceModifier.defaultWeight())
        if (snap.prefs.showLateCount) {
          val lateLabel = if (snap.isArabic) "المتأخرون: ${snap.lateCount}" else "Retards : ${snap.lateCount}"
          Text(
            text = lateLabel,
            style = TextStyle(
              color = if (snap.lateCount > 0) warningText else positiveText,
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
            ),
            maxLines = 1,
          )
        }
      }
    }
  }
}

@Composable
private fun SyndicatDetailedWidgetContent(
  context: Context,
  snap: WidgetSnapshotState,
) {
  val palette = widgetPaletteFor(snap.prefs.theme)
  val cardBg = ColorProvider(palette.bg)
  val softBg = ColorProvider(palette.surfaceSoft)
  val pillBg = ColorProvider(palette.ctaBg)
  val primaryText = ColorProvider(palette.primaryText)
  val secondaryText = ColorProvider(palette.secondaryText)
  val ctaText = ColorProvider(palette.ctaText)
  val positiveText = ColorProvider(palette.positiveText)
  val warningText = ColorProvider(palette.warningText)

  val openIntent = buildWidgetIntent(context, customRoute = WidgetQuickAction.OPEN_DASHBOARD.code)
  val primaryIntent = buildWidgetIntent(context, action = snap.prefs.primaryAction)
  val secondaryAction = if (snap.prefs.primaryAction == WidgetQuickAction.LATE_LIST) {
    WidgetQuickAction.ADD_EXPENSE
  } else {
    WidgetQuickAction.LATE_LIST
  }
  val secondaryIntent = buildWidgetIntent(context, action = secondaryAction)
  val studioIntent = buildWidgetIntent(context, customRoute = MainActivity.WIDGET_ROUTE_STUDIO)

  Column(
    modifier = GlanceModifier
      .fillMaxSize()
      .background(cardBg)
      .cornerRadius(22.dp)
      .padding(14.dp)
      .clickable(actionStartActivity(openIntent)),
  ) {
    // 1. Header: Residence + Customize gear + Primary CTA
    Row(
      modifier = GlanceModifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(modifier = GlanceModifier.defaultWeight()) {
        Text(
          text = snap.residenceName,
          style = TextStyle(color = secondaryText, fontSize = 12.sp, fontWeight = FontWeight.Medium),
          maxLines = 1,
        )
        Text(
          text = snap.displayedCashText,
          style = TextStyle(color = primaryText, fontSize = 23.sp, fontWeight = FontWeight.Bold),
          maxLines = 1,
        )
      }
      Box(
        modifier = GlanceModifier
          .background(softBg)
          .cornerRadius(10.dp)
          .padding(horizontal = 8.dp, vertical = 6.dp)
          .clickable(actionStartActivity(studioIntent)),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = "⚙",
          style = TextStyle(color = secondaryText, fontSize = 13.sp),
        )
      }
      Spacer(modifier = GlanceModifier.width(6.dp))
      Box(
        modifier = GlanceModifier
          .background(pillBg)
          .cornerRadius(14.dp)
          .padding(horizontal = 12.dp, vertical = 8.dp)
          .clickable(actionStartActivity(primaryIntent)),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = quickActionLabel(snap.prefs.primaryAction, snap.isArabic),
          style = TextStyle(color = ctaText, fontSize = 12.sp, fontWeight = FontWeight.Bold),
          maxLines = 1,
        )
      }
    }

    Spacer(modifier = GlanceModifier.height(10.dp))

    // 2. KPI Mini Tiles Row
    Row(modifier = GlanceModifier.fillMaxWidth()) {
      if (snap.prefs.showRate) {
        Column(
          modifier = GlanceModifier
            .defaultWeight()
            .background(softBg)
            .cornerRadius(12.dp)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        ) {
          Text(
            text = if (snap.isArabic) "التحصيل" else "Recouvrement",
            style = TextStyle(color = secondaryText, fontSize = 10.sp),
            maxLines = 1,
          )
          Text(
            text = snap.collectionRateText,
            style = TextStyle(color = positiveText, fontSize = 14.sp, fontWeight = FontWeight.Bold),
            maxLines = 1,
          )
        }
      }
      if (snap.prefs.showExpenses) {
        if (snap.prefs.showRate) Spacer(modifier = GlanceModifier.width(6.dp))
        Column(
          modifier = GlanceModifier
            .defaultWeight()
            .background(softBg)
            .cornerRadius(12.dp)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        ) {
          Text(
            text = if (snap.isArabic) "باقي المزودين" else "Reste dép.",
            style = TextStyle(color = secondaryText, fontSize = 10.sp),
            maxLines = 1,
          )
          Text(
            text = snap.displayedExpensesText,
            style = TextStyle(color = primaryText, fontSize = 14.sp, fontWeight = FontWeight.Bold),
            maxLines = 1,
          )
        }
      }
      if (snap.prefs.showLateCount) {
        if (snap.prefs.showRate || snap.prefs.showExpenses) Spacer(modifier = GlanceModifier.width(6.dp))
        Column(
          modifier = GlanceModifier
            .defaultWeight()
            .background(softBg)
            .cornerRadius(12.dp)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        ) {
          Text(
            text = if (snap.isArabic) "متأخرون" else "Retards",
            style = TextStyle(color = secondaryText, fontSize = 10.sp),
            maxLines = 1,
          )
          Text(
            text = "${snap.lateCount}",
            style = TextStyle(
              color = if (snap.lateCount > 0) warningText else positiveText,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
            ),
            maxLines = 1,
          )
        }
      }
    }

    // 3. Top Late Apartments List (optional)
    if (snap.prefs.showLatePreviewList) {
      Spacer(modifier = GlanceModifier.height(8.dp))
      Column(
        modifier = GlanceModifier
          .fillMaxWidth()
          .background(softBg)
          .cornerRadius(12.dp)
          .padding(horizontal = 10.dp, vertical = 8.dp),
      ) {
        if (snap.latePreviewLines.isEmpty()) {
          Text(
            text = if (snap.isArabic) "جميع الشقق مؤدّية ✓" else "Toutes les cotisations à jour ✓",
            style = TextStyle(color = positiveText, fontSize = 12.sp, fontWeight = FontWeight.Medium),
            maxLines = 1,
          )
        } else {
          snap.latePreviewLines.forEachIndexed { idx, line ->
            Text(
              text = if (snap.prefs.privacyMask) line.substringBefore(":") else line,
              style = TextStyle(color = primaryText, fontSize = 11.sp, fontWeight = FontWeight.Medium),
              maxLines = 1,
            )
            if (idx < snap.latePreviewLines.lastIndex) {
              Spacer(modifier = GlanceModifier.height(3.dp))
            }
          }
        }
      }
    }

    Spacer(modifier = GlanceModifier.defaultWeight())
    Spacer(modifier = GlanceModifier.height(6.dp))

    // 4. Secondary Quick-Action Bar
    Row(
      modifier = GlanceModifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      val collectedInfo = if (snap.isArabic) "المحصل: ${snap.displayedCollectedText}" else "Encaissé : ${snap.displayedCollectedText}"
      Text(
        text = collectedInfo,
        style = TextStyle(color = secondaryText, fontSize = 11.sp),
        maxLines = 1,
        modifier = GlanceModifier.defaultWeight(),
      )
      Box(
        modifier = GlanceModifier
          .background(softBg)
          .cornerRadius(10.dp)
          .padding(horizontal = 10.dp, vertical = 5.dp)
          .clickable(actionStartActivity(secondaryIntent)),
      ) {
        Text(
          text = quickActionLabel(secondaryAction, snap.isArabic),
          style = TextStyle(color = primaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold),
          maxLines = 1,
        )
      }
    }
  }
}

class SyndicatCompactWidgetReceiver : GlanceAppWidgetReceiver() {
  override val glanceAppWidget: GlanceAppWidget = SyndicatCompactWidget()
}

class SyndicatGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
  override val glanceAppWidget: GlanceAppWidget = SyndicatGlanceWidget()
}

class SyndicatDetailedWidgetReceiver : GlanceAppWidgetReceiver() {
  override val glanceAppWidget: GlanceAppWidget = SyndicatDetailedWidget()
}
