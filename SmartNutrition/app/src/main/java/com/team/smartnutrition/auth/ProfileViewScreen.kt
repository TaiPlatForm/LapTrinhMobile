package com.team.smartnutrition.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.team.smartnutrition.common.components.PlaceholderScreen

/**
 * ═══════════════════════════════════════════
 * MODULE 1 - TV1: XEM THÔNG TIN CÁ NHÂN
 * ═══════════════════════════════════════════
 *
 * TODO cho TV1:
 * 1. Đọc user data từ Firestore users/{uid}
 * 2. Hiển thị: tên, email, BMI, BMR, TDEE, macros
 * 3. Nút chỉnh sửa → form sửa profile
 * 4. Nút "Nhật ký cân nặng" → navigate WeightLog
 */
@Composable
fun ProfileViewScreen(navController: NavController) {
    PlaceholderScreen(
        moduleName = "Hồ sơ cá nhân",
        moduleNumber = 1,
        assignedTo = "TV1",
        description = "Dashboard cá nhân hiển thị tên, email, chỉ số sức khỏe (BMI, TDEE, Macros). Nút chỉnh sửa và nhật ký cân nặng.",
        icon = Icons.Filled.Person
    )
}
