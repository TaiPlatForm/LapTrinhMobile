package com.team.smartnutrition.habit.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.team.smartnutrition.MainActivity
import com.team.smartnutrition.R
import com.team.smartnutrition.habit.data.ReminderPrefs
import com.team.smartnutrition.habit.util.AlarmScheduler

/**
 * ═══════════════════════════════════════════
 * VITAMIN REMINDER RECEIVER
 * ═══════════════════════════════════════════
 *
 * Được kích hoạt bởi AlarmManager mỗi ngày 1 lần theo giờ cố định.
 * Hiển thị notification đơn giản (không có action button).
 * Click → mở app. Tự reschedule cho ngày mai.
 */
class VitaminReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // 1. Tạo NotificationChannel
        createVitaminChannel(context)

        // 2. PendingIntent khi click → mở app
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context, OPEN_APP_REQUEST_CODE, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Build notification
        val notification = NotificationCompat.Builder(context, VITAMIN_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("💊 Nhớ uống vitamin nhé!")
            .setContentText("Uống vitamin mỗi ngày giúp tăng cường sức khỏe")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .build()

        // 4. Hiển thị notification (check permission Android 13+)
        if (Build.VERSION.SDK_INT < 33 ||
            ActivityCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(VITAMIN_NOTIFICATION_ID, notification)
        }

        // 5. Reschedule cho ngày mai
        val prefs = ReminderPrefs(context)
        if (prefs.vitaminReminderEnabled) {
            AlarmScheduler.scheduleNextVitaminAlarm(context, prefs.vitaminHour, prefs.vitaminMinute)
        }
    }

    private fun createVitaminChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                VITAMIN_CHANNEL_ID,
                "Nhắc uống vitamin",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Nhắc nhở uống vitamin hàng ngày"
            }
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    companion object {
        const val VITAMIN_CHANNEL_ID = "vitamin_reminder_channel"
        const val VITAMIN_NOTIFICATION_ID = 1002
        private const val OPEN_APP_REQUEST_CODE = 3002
    }
}
