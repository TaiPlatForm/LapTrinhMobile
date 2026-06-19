package com.team.smartnutrition.habit

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.team.smartnutrition.common.components.LoadingScreen
import com.team.smartnutrition.habit.viewmodel.HabitUiState
import com.team.smartnutrition.habit.viewmodel.HabitViewModel
import com.team.smartnutrition.navigation.Screen

/**
 * ═══════════════════════════════════════════
 * MODULE 4 - HABIT DASHBOARD SCREEN
 * ═══════════════════════════════════════════
 *
 * Màn hình chính theo dõi thói quen hàng ngày:
 * - CircularProgress: số cốc nước / mục tiêu
 * - Nút +1/-1 cốc nước
 * - Slider giờ ngủ (0-12h, step 0.5)
 * - Checkbox vitamin
 * - Nút navigate sang Settings
 */
@Composable
fun HabitDashboardScreen(
    navController: NavController,
    viewModel: HabitViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> LoadingScreen("Đang tải thói quen...")
            else -> HabitDashboardContent(
                uiState = uiState,
                onAddWater = { viewModel.addWaterCup() },
                onRemoveWater = { viewModel.removeWaterCup() },
                onSleepChanged = { viewModel.updateSleepHours(it) },
                onVitaminToggled = { viewModel.toggleVitamin() },
                onSettingsClick = { navController.navigate(Screen.ReminderSettings.route) }
            )
        }

        // Error card
        uiState.errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "⚠️ $error",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("Đóng")
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// MAIN CONTENT
// ═══════════════════════════════════════════════════════════

@Composable
private fun HabitDashboardContent(
    uiState: HabitUiState,
    onAddWater: () -> Unit,
    onRemoveWater: () -> Unit,
    onSleepChanged: (Float) -> Unit,
    onVitaminToggled: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val habit = uiState.habitDay ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Text(
            text = "💧 Theo dõi thói quen hôm nay",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            fontWeight = FontWeight.Bold
        )

        // Water Progress Section
        WaterProgressSection(
            waterCups = habit.waterCups,
            waterGoal = uiState.waterGoal,
            onAdd = onAddWater,
            onRemove = onRemoveWater
        )

        // Sleep Section
        SleepSection(
            sleepHours = habit.sleepHours,
            onSleepChanged = onSleepChanged
        )

        // Vitamin Section
        VitaminSection(
            vitaminTaken = habit.vitaminTaken,
            onToggle = onVitaminToggled
        )

        // Navigate to Settings
        OutlinedButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Settings, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("⚙️ Cài đặt nhắc nhở")
        }

        // Bottom spacing
        Spacer(Modifier.height(24.dp))
    }
}

// ═══════════════════════════════════════════════════════════
// WATER PROGRESS - Vòng tròn tiến trình
// ═══════════════════════════════════════════════════════════

@Composable
private fun WaterProgressSection(
    waterCups: Int,
    waterGoal: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    val progress = if (waterGoal > 0) waterCups.toFloat() / waterGoal else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(600),
        label = "water_progress"
    )
    val isGoalReached = waterCups >= waterGoal

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "💧 Lượng nước hôm nay",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(20.dp))

            // Circular Progress
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(160.dp)
            ) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(160.dp),
                    strokeWidth = 12.dp,
                    trackColor = MaterialTheme.colorScheme.surface,
                    color = if (isGoalReached) Color(0xFF4CAF50)
                    else MaterialTheme.colorScheme.primary
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$waterCups",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isGoalReached) Color(0xFF4CAF50)
                        else MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "/ $waterGoal cốc",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${waterCups * 250}ml",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Badge khi đạt mục tiêu
            if (isGoalReached) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "🎉 Đạt mục tiêu!",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(20.dp))

            // Nút +/- cốc
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(
                    onClick = onRemove,
                    enabled = waterCups > 0,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Remove, "Giảm 1 cốc")
                }

                Text(
                    text = "$waterCups cốc",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                FilledIconButton(
                    onClick = onAdd,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Add, "Thêm 1 cốc")
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// SLEEP SECTION - Slider giờ ngủ
// ═══════════════════════════════════════════════════════════

@Composable
private fun SleepSection(
    sleepHours: Float,
    onSleepChanged: (Float) -> Unit
) {
    // Local state để slider smooth khi kéo, chỉ save Firestore khi thả
    var localSleepHours by remember(sleepHours) { mutableFloatStateOf(sleepHours) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "😴 Giờ ngủ đêm qua",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${localSleepHours}h",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(8.dp))

            Slider(
                value = localSleepHours,
                onValueChange = { localSleepHours = it },
                onValueChangeFinished = { onSleepChanged(localSleepHours) },
                valueRange = 0f..12f,
                steps = 23, // 24 positions: 0.0, 0.5, 1.0, ..., 12.0
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("0h", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("6h", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("12h", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// VITAMIN SECTION - Checkbox
// ═══════════════════════════════════════════════════════════

@Composable
private fun VitaminSection(
    vitaminTaken: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (vitaminTaken)
                MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("💊", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Vitamin hôm nay",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (vitaminTaken) "Đã uống ✓" else "Chưa uống",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (vitaminTaken) Color(0xFF4CAF50)
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Checkbox(
                checked = vitaminTaken,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}
