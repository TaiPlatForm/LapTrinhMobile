package com.team.smartnutrition.analytics

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.team.smartnutrition.common.components.PlaceholderScreen

/**
 * ═══════════════════════════════════════════
 * MODULE 5 - TV5: THỐNG KÊ BIỂU ĐỒ
 * ═══════════════════════════════════════════
 *
 * TODO cho TV5:
 * 1. LineChart cân nặng theo tuần/tháng (đọc users/{uid}/weightLog)
 * 2. BarChart calo mỗi ngày vs mục tiêu (đọc users/{uid}/mealPlans)
 * 3. Toggle tuần/tháng
 * 4. Nút "Xuất PDF" → PdfDocument API
 * 5. Nút "Cài đặt" → SettingsScreen
 *
 * THƯ VIỆN BIỂU ĐỒ:
 * - Dùng Vico (Compose-native): implementation("com.patrykandpatrick.vico:compose-m3:1.14.0")
 * - HOẶC wrap MPAndroidChart trong AndroidView
 */
@Composable
fun AnalyticsScreen(navController: NavController) {
    PlaceholderScreen(
        moduleName = "Thống kê & Biểu đồ",
        moduleNumber = 5,
        assignedTo = "TV5",
        description = "LineChart cân nặng + BarChart calo. Toggle tuần/tháng. Xuất PDF báo cáo dinh dưỡng.",
        icon = Icons.Filled.BarChart
    )
}
