package com.team.smartnutrition.meal

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.team.smartnutrition.common.components.PlaceholderScreen

/**
 * ═══════════════════════════════════════════
 * MODULE 3 - TV3: CHI TIẾT BỮA ĂN
 * ═══════════════════════════════════════════
 *
 * TODO cho TV3:
 * 1. Hiển thị tên món, tổng calo, tổng protein
 * 2. Danh sách nguyên liệu + định lượng
 * 3. Công thức nấu step-by-step
 */
@Composable
fun MealDetailScreen(
    navController: NavController,
    dayIndex: Int,
    mealType: String
) {
    PlaceholderScreen(
        moduleName = "Chi tiết bữa ăn",
        moduleNumber = 3,
        assignedTo = "TV3",
        description = "Chi tiết 1 bữa: tên món, nguyên liệu, công thức nấu, calo/protein.\nNgày: $dayIndex | Bữa: $mealType",
        icon = Icons.Filled.MenuBook
    )
}
