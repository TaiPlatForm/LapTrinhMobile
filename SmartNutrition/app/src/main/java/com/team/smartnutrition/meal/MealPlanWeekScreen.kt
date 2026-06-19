package com.team.smartnutrition.meal

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.team.smartnutrition.common.components.ErrorCard
import com.team.smartnutrition.common.components.GradientButton
import com.team.smartnutrition.common.components.LoadingScreen
import com.team.smartnutrition.meal.model.DayPlan
import com.team.smartnutrition.meal.model.Meal
import com.team.smartnutrition.meal.util.WeekUtils
import com.team.smartnutrition.meal.viewmodel.MealPlanUiState
import com.team.smartnutrition.meal.viewmodel.MealPlanViewModel
import com.team.smartnutrition.navigation.Screen

/**
 * ═══════════════════════════════════════════
 * MODULE 3 - THỰC ĐƠN AI - Màn hình tổng quan tuần
 * ═══════════════════════════════════════════
 *
 * States:
 *   isLoading → LoadingScreen
 *   mealPlan == null → EmptyMealPlanContent (CTA generate)
 *   mealPlan != null → MealPlanContent (TabRow + Cards)
 *
 * Overlays:
 *   isGenerating → GeneratingDialog (animated AI loading)
 *   errorMessage != null → ErrorCard (bottom)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanWeekScreen(
    navController: NavController,
    viewModel: MealPlanViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> LoadingScreen("Đang tải thực đơn...")
            uiState.mealPlan == null -> EmptyMealPlanContent(
                onGenerateClick = { viewModel.generateMealPlan() }
            )
            else -> MealPlanContent(
                uiState = uiState,
                onDaySelected = { viewModel.selectDay(it) },
                onMealClick = { dayIndex, mealType ->
                    navController.navigate(Screen.MealDetail.createRoute(dayIndex, mealType))
                },
                onRegenerateClick = { viewModel.generateMealPlan() }
            )
        }

        // Error snackbar / card (bottom)
        uiState.errorMessage?.let { error ->
            ErrorCard(
                message = error,
                onRetry = {
                    viewModel.clearError()
                    viewModel.generateMealPlan()
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        // Full-screen loading dialog overlay khi đang gọi Gemini AI
        if (uiState.isGenerating) {
            GeneratingDialog(message = uiState.loadingMessage)
        }
    }
}

// ═══════════════════════════════════════════════════════════
// EMPTY STATE - Chưa có plan
// ═══════════════════════════════════════════════════════════

@Composable
private fun EmptyMealPlanContent(onGenerateClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.RestaurantMenu,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Chưa có thực đơn",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Để AI gợi ý thực đơn 7 ngày\ndựa trên kho thực phẩm và chỉ số dinh dưỡng của bạn",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Spacer(Modifier.height(24.dp))
        GradientButton(
            text = "🤖 Lên thực đơn bằng AI",
            onClick = onGenerateClick,
            icon = Icons.Filled.AutoAwesome
        )
    }
}

// ═══════════════════════════════════════════════════════════
// MAIN CONTENT - Có plan
// ═══════════════════════════════════════════════════════════

@Composable
private fun MealPlanContent(
    uiState: MealPlanUiState,
    onDaySelected: (Int) -> Unit,
    onMealClick: (dayIndex: Int, mealType: String) -> Unit,
    onRegenerateClick: () -> Unit
) {
    val plan = uiState.mealPlan ?: return
    val selectedDay = plan.days.getOrNull(uiState.selectedDayIndex) ?: return

    Column(modifier = Modifier.fillMaxSize()) {
        // Nút regenerate
        GradientButton(
            text = "🔄 Đổi thực đơn tuần mới",
            onClick = onRegenerateClick,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Tab row chọn ngày (7 tab rút gọn)
        DayTabRow(
            days = plan.days,
            selectedIndex = uiState.selectedDayIndex,
            onDaySelected = onDaySelected
        )

        // Calorie + protein summary bar
        CalorieSummaryBar(
            consumed = selectedDay.totalCalories,
            target = plan.calorieTarget,
            protein = selectedDay.totalProtein
        )

        // 3 meal cards (Sáng / Trưa / Tối)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(WeekUtils.mealTypeOrder) { mealType ->
                val meal = selectedDay.meals[mealType]
                if (meal != null) {
                    MealCard(
                        mealType = mealType,
                        meal = meal,
                        onClick = { onMealClick(uiState.selectedDayIndex, mealType) }
                    )
                }
            }
            // Bottom spacing
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// DAY TAB ROW
// ═══════════════════════════════════════════════════════════

@Composable
private fun DayTabRow(
    days: List<DayPlan>,
    selectedIndex: Int,
    onDaySelected: (Int) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        edgePadding = 16.dp
    ) {
        days.forEachIndexed { index, day ->
            Tab(
                selected = selectedIndex == index,
                onClick = { onDaySelected(index) },
                text = {
                    // Rút gọn: "Thứ 2" → "T2", "Chủ Nhật" → "CN"
                    val shortLabel = when {
                        day.dayLabel.startsWith("Chủ") -> "CN"
                        else -> "T${day.dayLabel.last()}"
                    }
                    Text(shortLabel)
                }
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// CALORIE SUMMARY BAR
// ═══════════════════════════════════════════════════════════

@Composable
private fun CalorieSummaryBar(consumed: Int, target: Int, protein: Int) {
    val rawProgress = if (target > 0) consumed.toFloat() / target else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress.coerceIn(0f, 1.2f),
        animationSpec = tween(600),
        label = "calorie_progress"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "🔥 $consumed / $target kcal",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "💪 ${protein}g protein",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { animatedProgress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (rawProgress > 1f) MaterialTheme.colorScheme.error
                         else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// MEAL CARD
// ═══════════════════════════════════════════════════════════

@Composable
private fun MealCard(
    mealType: String,
    meal: Meal,
    onClick: () -> Unit
) {
    val label = WeekUtils.mealTypeLabels[mealType] ?: mealType

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = meal.name,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "🔥 ${meal.totalCalories} kcal",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "💪 ${meal.totalProtein}g protein",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// GENERATING DIALOG - Overlay khi đang gọi AI
// ═══════════════════════════════════════════════════════════

@Composable
private fun GeneratingDialog(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp),
                    strokeWidth = 4.dp
                )
                Spacer(Modifier.height(20.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Quá trình này mất khoảng 10-15 giây",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
