package com.team.smartnutrition.pantry

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.team.smartnutrition.common.components.PlaceholderScreen

/**
 * ═══════════════════════════════════════════
 * MODULE 2 - TV2: CHỤP ẢNH THỰC PHẨM
 * ═══════════════════════════════════════════
 *
 * TODO cho TV2:
 * 1. CameraX Preview toàn màn hình (dùng AndroidView trong Compose)
 * 2. Nút chụp ảnh → capture Bitmap
 * 3. Convert Bitmap → Base64 → gửi Gemini Vision API
 * 4. Nút chuyển sang BarcodeScanScreen
 * 5. Xin quyền CAMERA runtime
 */
@Composable
fun CameraCaptureScreen(navController: NavController) {
    PlaceholderScreen(
        moduleName = "Chụp ảnh thực phẩm",
        moduleNumber = 2,
        assignedTo = "TV2 (Nhóm trưởng)",
        description = "Camera CameraX chụp ảnh thực phẩm → gửi Gemini Vision AI nhận diện tên, calo, protein.",
        icon = Icons.Filled.CameraAlt
    )
}
