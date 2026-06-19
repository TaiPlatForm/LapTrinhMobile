package com.team.smartnutrition.habit.data

import android.content.Context
import android.content.SharedPreferences

/**
 * ═══════════════════════════════════════════
 * REMINDER PREFS - SharedPreferences wrapper
 * ═══════════════════════════════════════════
 *
 * Lưu cài đặt nhắc nhở LOCAL (không lên Firestore).
 * Dùng bởi:
 * - HabitViewModel: đọc/ghi settings
 * - AlarmScheduler: đọc settings khi schedule
 * - BootReceiver: đọc settings khi khôi phục alarms
 */
class ReminderPrefs(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("habit_reminder_prefs", Context.MODE_PRIVATE)

    // ═══════════════════════════════════════════
    // WATER REMINDER SETTINGS
    // ═══════════════════════════════════════════

    /** Bật/tắt nhắc nhở uống nước */
    var waterReminderEnabled: Boolean
        get() = prefs.getBoolean("water_reminder_enabled", false)
        set(v) = prefs.edit().putBoolean("water_reminder_enabled", v).apply()

    /** Chu kỳ nhắc: mỗi 1h, 2h, hoặc 3h */
    var waterIntervalHours: Int
        get() = prefs.getInt("water_interval_hours", 2)
        set(v) = prefs.edit().putInt("water_interval_hours", v).apply()

    /** Giờ bắt đầu nhắc (0-23, default 8:00) */
    var waterStartHour: Int
        get() = prefs.getInt("water_start_hour", 8)
        set(v) = prefs.edit().putInt("water_start_hour", v).apply()

    /** Giờ kết thúc nhắc (0-23, default 22:00) */
    var waterEndHour: Int
        get() = prefs.getInt("water_end_hour", 22)
        set(v) = prefs.edit().putInt("water_end_hour", v).apply()

    /** Mục tiêu số cốc nước / ngày (default 8) */
    var waterGoal: Int
        get() = prefs.getInt("water_goal", 8)
        set(v) = prefs.edit().putInt("water_goal", v).apply()

    // ═══════════════════════════════════════════
    // VITAMIN REMINDER SETTINGS
    // ═══════════════════════════════════════════

    /** Bật/tắt nhắc nhở uống vitamin */
    var vitaminReminderEnabled: Boolean
        get() = prefs.getBoolean("vitamin_reminder_enabled", false)
        set(v) = prefs.edit().putBoolean("vitamin_reminder_enabled", v).apply()

    /** Giờ nhắc vitamin (0-23, default 7:00 sáng) */
    var vitaminHour: Int
        get() = prefs.getInt("vitamin_hour", 7)
        set(v) = prefs.edit().putInt("vitamin_hour", v).apply()

    /** Phút nhắc vitamin (0-59, default 0) */
    var vitaminMinute: Int
        get() = prefs.getInt("vitamin_minute", 0)
        set(v) = prefs.edit().putInt("vitamin_minute", v).apply()
}
