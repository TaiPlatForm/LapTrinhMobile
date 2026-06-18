package com.team.smartnutrition.habit

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.team.smartnutrition.common.components.PlaceholderScreen

/**
 * ═══════════════════════════════════════════
 * MODULE 4 - TV4: CÀI ĐẶT NHẮC NHỞ
 * ═══════════════════════════════════════════
 *
 * TODO cho TV4:
 * 1. Switch bật/tắt nhắc nước (SharedPreferences)
 * 2. Chọn interval: mỗi 1h/2h/3h
 * 3. Chọn giờ bắt đầu/kết thúc
 * 4. Switch bật/tắt nhắc vitamin + chọn giờ
 * 5. AlarmManager setExactAndAllowWhileIdle()
 * 6. Xin quyền POST_NOTIFICATIONS trên Android 13+
 */
@Composable
fun ReminderSettingsScreen(navController: NavController) {
    PlaceholderScreen(
        moduleName = "Cài đặt nhắc nhở",
        moduleNumber = 4,
        assignedTo = "TV4",
        description = "Bật/tắt nhắc nước (interval, giờ bắt đầu/kết thúc). Nhắc vitamin (giờ cố định). AlarmManager + Notification.",
        icon = Icons.Filled.Notifications
    )
}
