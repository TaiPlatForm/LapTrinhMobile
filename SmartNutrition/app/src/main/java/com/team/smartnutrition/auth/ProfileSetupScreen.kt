package com.team.smartnutrition.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.team.smartnutrition.common.components.PlaceholderScreen
import com.team.smartnutrition.navigation.Screen

/**
 * ═══════════════════════════════════════════
 * MODULE 1 - TV1: THIẾT LẬP THỂ TRẠNG
 * ═══════════════════════════════════════════
 *
 * TODO cho TV1:
 * 1. Form nhập: Tuổi (slider), Chiều cao (cm), Cân nặng (kg)
 * 2. Chọn giới tính (Card Nam/Nữ)
 * 3. Chọn mục tiêu: Tăng cơ / Giảm mỡ / Duy trì
 * 4. Chọn mức độ vận động (1.2 → 1.9)
 * 5. Tính BMI, BMR, TDEE, Macros
 * 6. Lưu Firestore users/{uid}
 * 7. Navigate → Home
 */
@Composable
fun ProfileSetupScreen(navController: NavController) {
    PlaceholderScreen(
        moduleName = "Thiết lập thể trạng",
        moduleNumber = 1,
        assignedTo = "TV1",
        description = "Form nhập chỉ số cơ thể (tuổi, chiều cao, cân nặng, giới tính, mục tiêu). Tự động tính BMI/BMR/TDEE/Macros và lưu Firestore.",
        icon = Icons.Filled.Person
    )
}
