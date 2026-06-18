package com.team.smartnutrition

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.team.smartnutrition.navigation.BottomNavBar
import com.team.smartnutrition.navigation.SmartNutritionNavHost
import com.team.smartnutrition.navigation.bottomNavItems
import com.team.smartnutrition.ui.theme.SmartNutritionTheme

/**
 * MainActivity - Activity DUY NHẤT của app.
 * Tất cả UI được quản lý bằng Compose Navigation.
 * KHÔNG CẦN TẠO THÊM Activity khác.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartNutritionTheme {
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
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
