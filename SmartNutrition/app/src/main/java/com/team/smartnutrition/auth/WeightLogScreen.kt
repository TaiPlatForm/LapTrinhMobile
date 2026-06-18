package com.team.smartnutrition.auth

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.team.smartnutrition.common.components.PlaceholderScreen

/**
 * ═══════════════════════════════════════════
 * MODULE 1 - TV1: NHẬT KÝ CÂN NẶNG
 * ═══════════════════════════════════════════
 *
 * TODO cho TV1:
 * 1. Input nhập cân nặng hôm nay
 * 2. Tính lại BMI theo cân nặng mới
 * 3. Lưu vào Firestore users/{uid}/weightLog/{date}
 * 4. Hiển thị danh sách lịch sử cân nặng (LazyColumn)
 */
@Composable
fun WeightLogScreen(navController: NavController) {
    PlaceholderScreen(
        moduleName = "Nhật ký cân nặng",
        moduleNumber = 1,
        assignedTo = "TV1",
        description = "Nhập cân nặng hàng ngày, tự tính lại BMI. Hiển thị lịch sử cân nặng theo ngày.",
        icon = Icons.Filled.MonitorWeight
    )
}
