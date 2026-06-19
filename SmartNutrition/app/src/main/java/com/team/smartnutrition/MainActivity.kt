package com.team.smartnutrition

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.team.smartnutrition.common.components.LoadingScreen
import com.team.smartnutrition.navigation.BottomNavBar
import com.team.smartnutrition.navigation.Screen
import com.team.smartnutrition.navigation.SmartNutritionNavHost
import com.team.smartnutrition.navigation.bottomNavItems
import com.team.smartnutrition.ui.theme.SmartNutritionTheme
import kotlinx.coroutines.tasks.await

/**
 * MainActivity - Activity DUY NHẤT của app.
 * Tất cả UI được quản lý bằng Compose Navigation.
 * KHÔNG CẦN TẠO THÊM Activity khác.
 *
 * Auth Flow:
 * 1. App launch → check currentUser
 * 2. null → Login screen
 * 3. exists → check Firestore profile → Home hoặc ProfileSetup
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Load and apply settings
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode", false)
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
            else androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
        )
        val lang = prefs.getString("language", "vi") ?: "vi"
        androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
            androidx.core.os.LocaleListCompat.forLanguageTags(lang)
        )

        enableEdgeToEdge()
        setContent {
            SmartNutritionTheme {
                var startDestination by remember { mutableStateOf<String?>(null) }
                var isCheckingAuth by remember { mutableStateOf(true) }

                // Check auth state on launch
                LaunchedEffect(Unit) {
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    startDestination = when {
                        currentUser == null -> Screen.Login.route
                        else -> {
                            // Check if user has profile in Firestore
                            try {
                                val doc = FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(currentUser.uid)
                                    .get()
                                    .await()
                                if (doc.exists()) Screen.Home.route
                                else Screen.ProfileSetup.route
                            } catch (e: Exception) {
                                // Offline or error → go to Home (Firestore offline cache may help)
                                Screen.Home.route
                            }
                        }
                    }
                    isCheckingAuth = false
                }

                if (isCheckingAuth) {
                    LoadingScreen(message = "Đang kiểm tra đăng nhập...")
                } else {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    // Chỉ hiện Bottom Nav khi ở các tab chính
                    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            if (showBottomBar) {
                                BottomNavBar(
                                    navController = navController,
                                    currentRoute = currentRoute
                                )
                            }
                        }
                    ) { innerPadding ->
                        SmartNutritionNavHost(
                            navController = navController,
                            modifier = Modifier.padding(innerPadding),
                            startDestination = startDestination ?: Screen.Login.route
                        )
                    }
                }
            }
        }
    }
}
