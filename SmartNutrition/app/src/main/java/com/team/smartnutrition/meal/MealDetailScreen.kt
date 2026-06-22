package com.team.smartnutrition.meal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.team.smartnutrition.R
import com.team.smartnutrition.common.components.SmartTopBar
import com.team.smartnutrition.meal.model.Ingredient
import com.team.smartnutrition.meal.model.Meal
import com.team.smartnutrition.meal.viewmodel.MealPlanViewModel
import androidx.compose.runtime.LaunchedEffect

/**
 * ═══════════════════════════════════════════
 * MODULE 3 - Chi tiết bữa ăn
 * ═══════════════════════════════════════════
 *
 * Nhận dayIndex + mealType từ nav args.
 * Đọc Meal từ shared ViewModel (data load từ Firestore cache → gần instant).
 *
 * Layout:
 *   SmartTopBar ← back
 *   LazyColumn:
 *     - Tên món (Headline)
 *     - Chips: calo + protein
 *     - Section Nguyên liệu
 *     - Section Cách nấu (multi-line)
 */
@Composable
fun MealDetailScreen(
    navController: NavController,
    dayIndex: Int,
    mealType: String,
    viewModel: MealPlanViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val meal = uiState.mealPlan?.days?.getOrNull(dayIndex)?.meals?.get(mealType)
    val dayLabelRaw = uiState.mealPlan?.days?.getOrNull(dayIndex)?.dayLabel ?: ""

    val isEnglish = stringResource(R.string.filter_all) == "All"
    val dayLabel = if (isEnglish) {
        when {
            dayLabelRaw.startsWith("Chủ") || dayLabelRaw.contains("Sunday", ignoreCase = true) -> "Sunday"
            dayLabelRaw.endsWith("2") || dayLabelRaw.contains("Monday", ignoreCase = true) -> "Monday"
            dayLabelRaw.endsWith("3") || dayLabelRaw.contains("Tuesday", ignoreCase = true) -> "Tuesday"
            dayLabelRaw.endsWith("4") || dayLabelRaw.contains("Wednesday", ignoreCase = true) -> "Wednesday"
            dayLabelRaw.endsWith("5") || dayLabelRaw.contains("Thursday", ignoreCase = true) -> "Thursday"
            dayLabelRaw.endsWith("6") || dayLabelRaw.contains("Friday", ignoreCase = true) -> "Friday"
            dayLabelRaw.endsWith("7") || dayLabelRaw.contains("Saturday", ignoreCase = true) -> "Saturday"
            else -> dayLabelRaw
        }
    } else {
        when {
            dayLabelRaw.startsWith("Chủ") || dayLabelRaw.contains("Sunday", ignoreCase = true) -> "Chủ Nhật"
            dayLabelRaw.endsWith("2") || dayLabelRaw.contains("Monday", ignoreCase = true) -> "Thứ 2"
            dayLabelRaw.endsWith("3") || dayLabelRaw.contains("Tuesday", ignoreCase = true) -> "Thứ 3"
            dayLabelRaw.endsWith("4") || dayLabelRaw.contains("Wednesday", ignoreCase = true) -> "Thứ 4"
            dayLabelRaw.endsWith("5") || dayLabelRaw.contains("Thursday", ignoreCase = true) -> "Thứ 5"
            dayLabelRaw.endsWith("6") || dayLabelRaw.contains("Friday", ignoreCase = true) -> "Thứ 6"
            dayLabelRaw.endsWith("7") || dayLabelRaw.contains("Saturday", ignoreCase = true) -> "Thứ 7"
            else -> dayLabelRaw
        }
    }

    val mealLabel = when (mealType.lowercase()) {
        "breakfast" -> stringResource(R.string.breakfast)
        "lunch" -> stringResource(R.string.lunch)
        "dinner" -> stringResource(R.string.dinner)
        else -> mealType
    }

    // Tự động gọi tải chi tiết món ăn khi màn hình được tạo
    LaunchedEffect(dayIndex, mealType) {
        viewModel.loadMealDetail(dayIndex, mealType)
    }

    Scaffold(
        topBar = {
            SmartTopBar(
                title = "$mealLabel — $dayLabel",
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        when {
            uiState.isGeneratingDetail -> {
                // Đang gọi AI sinh chi tiết công thức
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.size(44.dp))
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.ai_detail_loading),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            uiState.detailErrorMessage != null -> {
                // Lỗi khi tải chi tiết
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.detailErrorMessage ?: stringResource(R.string.detail_error_title),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Button(
                            onClick = { viewModel.loadMealDetail(dayIndex, mealType) }
                        ) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
            }
            meal == null -> {
                // Fallback khi không tìm thấy thông tin bữa ăn
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_meal_info),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                // Thành công -> hiển thị giao diện
                MealDetailContent(
                    meal = meal,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// DETAIL CONTENT
// ═══════════════════════════════════════════════════════════

@Composable
private fun MealDetailContent(meal: Meal, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tên món ăn (Headline)
        item {
            Text(
                text = meal.name,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Nutrition chips: calo + protein
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NutritionChip(
                    label = "🔥 ${meal.totalCalories} kcal",
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
                NutritionChip(
                    label = "💪 ${meal.totalProtein}g protein",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            }
        }

        // Section: Nguyên liệu header
        item {
            Text(
                text = "📝 " + stringResource(R.string.ingredients_section),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Ingredient rows
        items(meal.ingredients) { ingredient ->
            IngredientRow(ingredient)
        }

        // Divider
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }

        // Section: Cách nấu header
        item {
            Text(
                text = "👨‍🍳 " + stringResource(R.string.recipe_section),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Recipe multi-line text
        item {
            Text(
                text = meal.recipe,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 24.sp
            )
        }

        // Bottom spacing
        item { Spacer(Modifier.height(24.dp)) }
    }
}

// ═══════════════════════════════════════════════════════════
// SUB-COMPONENTS
// ═══════════════════════════════════════════════════════════

@Composable
private fun NutritionChip(label: String, containerColor: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun IngredientRow(ingredient: Ingredient) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "• ${ingredient.name}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = ingredient.amount,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}
