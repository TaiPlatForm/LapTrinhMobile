package com.team.smartnutrition.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * ═══════════════════════════════════════════
 * ĐỊNH NGHĨA TẤT CẢ ROUTES (đường dẫn màn hình)
 * ═══════════════════════════════════════════
 *
 * Khi cần navigate đến màn hình nào, dùng:
 *   navController.navigate(Screen.Login.route)
 *   navController.navigate(Screen.FoodDetail.createRoute("abc123"))
 */
sealed class Screen(val route: String) {
    // ═══ AUTH (Module 1 - TV1) ═══
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object ProfileSetup : Screen("profile_setup")

    // ═══ BOTTOM NAV TABS (5 tab chính) ═══
    data object Home : Screen("home")             // Tab 1 - Profile/Dashboard
    data object Pantry : Screen("pantry")          // Tab 2 - Kho thực phẩm
    data object MealPlan : Screen("meal_plan")     // Tab 3 - Thực đơn AI
    data object Habit : Screen("habit")            // Tab 4 - Thói quen
    data object Analytics : Screen("analytics")    // Tab 5 - Thống kê

    // ═══ DETAIL SCREENS (Module 1 - TV1) ═══
    data object WeightLog : Screen("weight_log")
    data object ProfileView : Screen("profile_view")

    // ═══ DETAIL SCREENS (Module 2 - TV2) ═══
    data object CameraCapture : Screen("camera_capture")
    data object FoodResult : Screen("food_result")
    data object BarcodeScan : Screen("barcode_scan")
    data object FoodDetail : Screen("food_detail/{itemId}") {
        fun createRoute(itemId: String) = "food_detail/$itemId"
    }

    // ═══ DETAIL SCREENS (Module 3 - TV3) ═══
    data object MealDetail : Screen("meal_detail/{dayIndex}/{mealType}") {
        fun createRoute(dayIndex: Int, mealType: String) = "meal_detail/$dayIndex/$mealType"
    }

    // ═══ DETAIL SCREENS (Module 4 - TV4) ═══
    data object ReminderSettings : Screen("reminder_settings")

    // ═══ DETAIL SCREENS (Module 5 - TV5) ═══
    data object Settings : Screen("settings")
}

/**
 * Data class cho Bottom Navigation items.
 */
data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

/**
 * Danh sách 5 tab Bottom Navigation.
 */
val bottomNavItems = listOf(
    BottomNavItem(
        route = Screen.Home.route,
        label = "Trang chủ",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomNavItem(
        route = Screen.Pantry.route,
        label = "Kho TP",
        selectedIcon = Icons.Filled.Kitchen,
        unselectedIcon = Icons.Outlined.Kitchen
    ),
    BottomNavItem(
        route = Screen.MealPlan.route,
        label = "Thực đơn",
        selectedIcon = Icons.Filled.RestaurantMenu,
        unselectedIcon = Icons.Outlined.RestaurantMenu
    ),
    BottomNavItem(
        route = Screen.Habit.route,
        label = "Thói quen",
        selectedIcon = Icons.Filled.WaterDrop,
        unselectedIcon = Icons.Outlined.WaterDrop
    ),
    BottomNavItem(
        route = Screen.Analytics.route,
        label = "Thống kê",
        selectedIcon = Icons.Filled.BarChart,
        unselectedIcon = Icons.Outlined.BarChart
    )
)
