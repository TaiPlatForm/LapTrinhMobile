package com.team.smartnutrition.pantry

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.team.smartnutrition.common.components.PlaceholderScreen

/**
 * ═══════════════════════════════════════════
 * MODULE 2 - TV2: DANH SÁCH KHO THỰC PHẨM
 * ═══════════════════════════════════════════
 *
 * TODO cho TV2:
 * 1. LazyColumn hiển thị danh sách thực phẩm từ Firestore users/{uid}/pantry
 * 2. FAB button "Thêm" → CameraCaptureScreen
 * 3. Bộ lọc: Tất cả / Sắp hết hạn / Đã hết hạn
 * 4. Badge màu theo trạng thái (Xanh/Vàng/Đỏ)
 * 5. Swipe để xóa, click vào item → FoodDetailScreen
 * 6. Ô tìm kiếm thực phẩm
 */
@Composable
fun PantryListScreen(navController: NavController) {
    PlaceholderScreen(
        moduleName = "Kho thực phẩm",
        moduleNumber = 2,
        assignedTo = "TV2 (Nhóm trưởng)",
        description = "Danh sách thực phẩm trong kho với badge hạn sử dụng (Xanh/Vàng/Đỏ). FAB chụp ảnh AI hoặc quét barcode.",
        icon = Icons.Filled.Kitchen
    )
}
