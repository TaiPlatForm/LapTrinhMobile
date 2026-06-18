package com.team.smartnutrition.habit

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.team.smartnutrition.common.components.PlaceholderScreen

/**
 * ═══════════════════════════════════════════
 * MODULE 4 - TV4: DASHBOARD THÓI QUEN
 * ═══════════════════════════════════════════
 *
 * TODO cho TV4:
 * 1. CircularProgress uống nước (VD: 5/8 cốc)
 * 2. Nút "+1 cốc" → update Firestore users/{uid}/habits/{date}
 * 3. Thẻ giờ ngủ (input giờ ngủ)
 * 4. Checkbox vitamin đã uống
 * 5. Nút "Cài đặt nhắc nhở" → ReminderSettingsScreen
 */
@Composable
fun HabitDashboardScreen(navController: NavController) {
    PlaceholderScreen(
        moduleName = "Theo dõi thói quen",
        moduleNumber = 4,
        assignedTo = "TV4",
        description = "Dashboard: vòng tròn uống nước, giờ ngủ, vitamin. Nút +1 cốc. Cài đặt nhắc nhở.",
        icon = Icons.Filled.WaterDrop
    )
}
