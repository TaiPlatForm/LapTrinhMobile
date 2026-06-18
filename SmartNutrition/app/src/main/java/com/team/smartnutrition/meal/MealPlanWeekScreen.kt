package com.team.smartnutrition.meal

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.team.smartnutrition.common.components.PlaceholderScreen

/**
 * ═══════════════════════════════════════════
 * MODULE 3 - TV3: LỊCH THỰC ĐƠN TUẦN
 * ═══════════════════════════════════════════
 *
 * TODO cho TV3:
 * 1. Nút "Lên thực đơn bằng AI" → query pantry + user profile → gửi prompt Gemini
 * 2. HorizontalPager/TabRow chọn ngày (T2→CN)
 * 3. Mỗi ngày hiện 3 card: Sáng/Trưa/Tối với tên món + kcal
 * 4. Tổng calo ngày vs mục tiêu (ProgressBar)
 * 5. Click vào card → MealDetailScreen
 * 6. Lưu/đọc Firestore users/{uid}/mealPlans/{weekId}
 */
@Composable
fun MealPlanWeekScreen(navController: NavController) {
    PlaceholderScreen(
        moduleName = "Thực đơn AI",
        moduleNumber = 3,
        assignedTo = "TV3",
        description = "Lịch ăn 7 ngày do AI gợi ý. Mỗi ngày 3 bữa (Sáng/Trưa/Tối). Ưu tiên nguyên liệu sắp hết hạn từ kho.",
        icon = Icons.Filled.RestaurantMenu
    )
}
