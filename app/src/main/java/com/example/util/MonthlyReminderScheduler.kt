package com.example.util

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LanguagePreferences
import java.util.Calendar

/**
 * Récepteur de rappel mensuel automatique invitant le syndic à vérifier les cotisations
 * en retard, lancer la relance WhatsApp et actualiser l'affiche du hall.
 */
class MonthlyReminderReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent?) {
    MonthlyReminderScheduler.showReminderNotificationNow(context)
  }
}

object MonthlyReminderScheduler {
  private const val CHANNEL_ID = "syndicat_monthly_reminders"
  private const val NOTIFICATION_ID = 202601
  private const val REQUEST_CODE = 202602

  fun hasNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS,
      ) == PackageManager.PERMISSION_GRANTED
    } else {
      true
    }
  }

  fun ensureChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val lang = LanguagePreferences.getSavedLanguage(context)
      val isAr = lang == AppLanguage.AR
      val name = if (isAr) "تذكير السانديك الشهري" else "Rappels mensuels du syndic"
      val desc = if (isAr) {
        "تنبيهات شهرية لتحديث إعلان المساهمات وتذكير المتأخرين."
      } else {
        "Alertes mensuelles pour mettre à jour l'affiche et relancer les retards."
      }
      val channel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT).apply {
        description = desc
      }
      val mgr = context.getSystemService(NotificationManager::class.java)
      mgr?.createNotificationChannel(channel)
    }
  }

  fun scheduleMonthlyReminder(context: Context, dayOfMonth: Int, enabled: Boolean) {
    val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val intent = Intent(context, MonthlyReminderReceiver::class.java)
    val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    val pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, flags)

    if (!enabled) {
      alarmMgr.cancel(pendingIntent)
      return
    }

    ensureChannel(context)
    val safeDay = dayOfMonth.coerceIn(1, 28)
    val cal = Calendar.getInstance().apply {
      set(Calendar.DAY_OF_MONTH, safeDay)
      set(Calendar.HOUR_OF_DAY, 10)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
      if (timeInMillis <= System.currentTimeMillis()) {
        add(Calendar.MONTH, 1)
      }
    }

    // Use inexact repeating (~30 days) so zero restricted alarm permissions are needed
    alarmMgr.setInexactRepeating(
      AlarmManager.RTC_WAKEUP,
      cal.timeInMillis,
      AlarmManager.INTERVAL_DAY * 30L,
      pendingIntent,
    )
  }

  fun showReminderNotificationNow(context: Context, lateCountHint: Int? = null): Boolean {
    if (!hasNotificationPermission(context)) return false
    ensureChannel(context)

    val isAr = LanguagePreferences.getSavedLanguage(context) == AppLanguage.AR
    val title = if (isAr) {
      "SYNDICAT · حان موعد التذكير الشهري"
    } else {
      "SYNDICAT · Rappel mensuel des cotisations"
    }
    val body = if (isAr) {
      if (lateCountHint != null && lateCountHint > 0) {
        "يوجد حالياً $lateCountHint شقق متأخرة. افتح التطبيق لتصدير الملصق أو إرسال تذكير واتساب."
      } else {
        "قم بمراجعة المساهمات الشهرية، إرسال تذكير واتساب للمتأخرين وتحديث إعلان العمارة."
      }
    } else {
      if (lateCountHint != null && lateCountHint > 0) {
        "$lateCountHint appartements en retard. Ouvrez l'app pour exporter l'affiche ou relancer sur WhatsApp."
      } else {
        "Vérifiez les cotisations du mois, lancez les relances WhatsApp et mettez à jour l'affiche du hall."
      }
    }

    val openIntent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val contentPending = PendingIntent.getActivity(
      context,
      REQUEST_CODE + 1,
      openIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(R.mipmap.ic_launcher)
      .setContentTitle(title)
      .setContentText(body)
      .setStyle(NotificationCompat.BigTextStyle().bigText(body))
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .setAutoCancel(true)
      .setContentIntent(contentPending)
      .build()

    return try {
      NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
      true
    } catch (_: SecurityException) {
      false
    }
  }
}
