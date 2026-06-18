package com.team.smartnutrition.analytics

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.team.smartnutrition.common.components.PlaceholderScreen

/**
 * ═══════════════════════════════════════════
 * MODULE 5 - TV5: CÀI ĐẶT
 * ═══════════════════════════════════════════
 *
 * TODO cho TV5:
 * 1. Dropdown ngôn ngữ (Tiếng Việt / English) → Locale
 * 2. Switch Dark/Light Mode → AppCompatDelegate
 * 3. Toggle Offline Sync → Firestore Persistence
 * 4. Nút Đăng xuất → Firebase Auth signOut()
 * 5. Hiển thị phiên bản app
 */
@Composable
fun SettingsScreen(navController: NavController) {
    PlaceholderScreen(
        moduleName = "Cài đặt",
        moduleNumber = 5,
        assignedTo = "TV5",
        description = "Ngôn ngữ (VI/EN), Dark/Light Mode, Offline Sync, Đăng xuất, Phiên bản app.",
        icon = Icons.Filled.Settings
    )
}
