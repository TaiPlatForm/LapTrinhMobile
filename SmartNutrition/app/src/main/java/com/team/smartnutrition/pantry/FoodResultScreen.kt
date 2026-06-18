package com.team.smartnutrition.pantry

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.team.smartnutrition.common.components.PlaceholderScreen

/**
 * ═══════════════════════════════════════════
 * MODULE 2 - TV2: KẾT QUẢ NHẬN DIỆN AI
 * ═══════════════════════════════════════════
 *
 * TODO cho TV2:
 * 1. Hiển thị kết quả từ Gemini (tên, calo, protein)
 * 2. Cho user chỉnh sửa (edit text)
 * 3. Chọn số lượng (gram) + ngày hết hạn (DatePicker)
 * 4. Nút "Lưu vào kho" → Firestore users/{uid}/pantry
 */
@Composable
fun FoodResultScreen(navController: NavController) {
    PlaceholderScreen(
        moduleName = "Kết quả nhận diện",
        moduleNumber = 2,
        assignedTo = "TV2 (Nhóm trưởng)",
        description = "Hiển thị kết quả AI: tên thực phẩm, calo, protein. User chỉnh sửa → chọn số lượng, hạn sử dụng → lưu kho.",
        icon = Icons.Filled.Fastfood
    )
}
