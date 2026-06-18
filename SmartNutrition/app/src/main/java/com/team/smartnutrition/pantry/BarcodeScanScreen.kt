package com.team.smartnutrition.pantry

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.team.smartnutrition.common.components.PlaceholderScreen

/**
 * ═══════════════════════════════════════════
 * MODULE 2 - TV2: QUÉT MÃ VẠCH
 * ═══════════════════════════════════════════
 *
 * TODO cho TV2:
 * 1. ML Kit Barcode Scanner real-time (dùng CameraX + AndroidView)
 * 2. Quét barcode → lấy rawValue (chuỗi số)
 * 3. Tra cứu: HashMap local hoặc Open Food Facts API
 * 4. Hiển thị kết quả → user xác nhận → lưu Firestore
 */
@Composable
fun BarcodeScanScreen(navController: NavController) {
    PlaceholderScreen(
        moduleName = "Quét mã vạch",
        moduleNumber = 2,
        assignedTo = "TV2 (Nhóm trưởng)",
        description = "Camera quét barcode real-time bằng ML Kit. Tra cứu sản phẩm → hiển thị thông tin → lưu kho.",
        icon = Icons.Filled.QrCodeScanner
    )
}
