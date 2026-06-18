package com.team.smartnutrition.pantry

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.team.smartnutrition.common.components.PlaceholderScreen

/**
 * ═══════════════════════════════════════════
 * MODULE 2 - TV2: CHI TIẾT THỰC PHẨM
 * ═══════════════════════════════════════════
 *
 * TODO cho TV2:
 * 1. Đọc data từ Firestore users/{uid}/pantry/{itemId}
 * 2. Hiển thị chi tiết: tên, calo, protein, số lượng, hạn SD
 * 3. Nút sửa số lượng
 * 4. Nút xóa thực phẩm (confirm dialog)
 * 5. Hiển thị ngày thêm
 */
@Composable
fun FoodDetailScreen(
    navController: NavController,
    itemId: String
) {
    PlaceholderScreen(
        moduleName = "Chi tiết thực phẩm",
        moduleNumber = 2,
        assignedTo = "TV2 (Nhóm trưởng)",
        description = "Chi tiết 1 thực phẩm: tên, calo, protein, số lượng, hạn sử dụng. Sửa/Xóa.\nItem ID: $itemId",
        icon = Icons.Filled.Info
    )
}
