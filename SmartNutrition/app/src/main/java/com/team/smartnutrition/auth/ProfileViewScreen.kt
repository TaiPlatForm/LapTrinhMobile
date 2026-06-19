package com.team.smartnutrition.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.team.smartnutrition.auth.util.HealthCalculator
import com.team.smartnutrition.auth.viewmodel.ProfileViewUiState
import com.team.smartnutrition.auth.viewmodel.ProfileViewViewModel
import com.team.smartnutrition.auth.viewmodel.activityLevelOptions
import com.team.smartnutrition.auth.viewmodel.goalOptions
import com.team.smartnutrition.common.components.ErrorCard
import com.team.smartnutrition.common.components.LoadingScreen
import com.team.smartnutrition.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileViewScreen(
    navController: NavController,
    viewModel: ProfileViewViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSignedOut) {
        if (uiState.isSignedOut) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
            viewModel.onSignedOut()
        }
    }

    when {
        uiState.isLoading -> LoadingScreen()
        uiState.errorMessage != null && uiState.user == null -> {
            ErrorCard(message = uiState.errorMessage ?: "Lỗi", onRetry = { viewModel.loadProfile() })
        }
        uiState.isEditing -> EditProfileContent(uiState, viewModel)
        else -> ViewProfileContent(uiState, viewModel, navController)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ViewProfileContent(
    uiState: ProfileViewUiState,
    viewModel: ProfileViewViewModel,
    navController: NavController
) {
    val user = uiState.user ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hồ sơ cá nhân") },
                actions = {
                    IconButton(onClick = { viewModel.startEditing() }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Chỉnh sửa")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Surface(modifier = Modifier.size(80.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                Box(contentAlignment = Alignment.Center) {
                    Text(user.displayName.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(user.displayName, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
            Text(user.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(20.dp))

            // Info Card
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Thông tin", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                    Spacer(Modifier.height(12.dp))
                    InfoRow("Giới tính", if (user.gender == "male") "🚹 Nam" else "🚺 Nữ")
                    InfoRow("Tuổi", "${HealthCalculator.calculateAge(user.birthYear)} (${user.birthYear})")
                    InfoRow("Chiều cao", "${user.heightCm} cm")
                    InfoRow("Cân nặng", "${"%.1f".format(user.weightKg)} kg")
                    InfoRow("Mục tiêu", goalOptions.find { it.value == user.goal }?.let { "${it.emoji} ${it.label}" } ?: user.goal)
                    InfoRow("Vận động", activityLevelOptions.find { it.value == user.activityLevel }?.let { "${it.emoji} ${it.label}" } ?: "${user.activityLevel}")
                }
            }
            Spacer(Modifier.height(16.dp))

            // Health Metrics
            Text("📊 Chỉ số sức khỏe", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard("BMI", "${"%.1f".format(user.bmi)}", HealthCalculator.getBmiCategory(user.bmi), Modifier.weight(1f))
                MetricCard("BMR", "${"%.0f".format(user.bmr)}", "kcal", Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard("TDEE", "${"%.0f".format(user.tdee)}", "kcal/ngày", Modifier.weight(1f))
                MetricCard("Mục tiêu", "${user.calorieTarget}", "kcal/ngày", Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))

            // Macros
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(16.dp)) {
                    Text("🍽️ Macros mục tiêu/ngày", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                    Spacer(Modifier.height(12.dp))
                    MacroBar("Protein", user.proteinTarget, 200, MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    MacroBar("Carbs", user.carbTarget, 400, MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.height(8.dp))
                    MacroBar("Fat", user.fatTarget, 120, MaterialTheme.colorScheme.tertiary)
                }
            }
            Spacer(Modifier.height(20.dp))

            // Actions
            OutlinedButton(onClick = { navController.navigate(Screen.WeightLog.route) },
                modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Filled.MonitorWeight, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp)); Text("Nhật ký cân nặng")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { navController.navigate(Screen.Settings.route) },
                modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Filled.Settings, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp)); Text("Cài đặt")
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { viewModel.signOut() }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Logout, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp)); Text("Đăng xuất", color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileContent(uiState: ProfileViewUiState, viewModel: ProfileViewViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) { uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) } }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Chỉnh sửa hồ sơ") },
                navigationIcon = { IconButton(onClick = { viewModel.cancelEditing() }) { Icon(Icons.Filled.Close, "Hủy") } },
                actions = {
                    TextButton(onClick = { viewModel.saveChanges() }, enabled = !uiState.isSaving) {
                        if (uiState.isSaving) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                        else Text("Lưu", fontWeight = FontWeight.Bold)
                    }
                })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value = uiState.editDisplayName, onValueChange = { viewModel.updateEditDisplayName(it) },
                label = { Text("Tên hiển thị") }, leadingIcon = { Icon(Icons.Filled.Person, null) },
                shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(), singleLine = true)

            Text("Giới tính", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(selected = uiState.editGender == "male", onClick = { viewModel.updateEditGender("male") }, label = { Text("🚹 Nam") })
                FilterChip(selected = uiState.editGender == "female", onClick = { viewModel.updateEditGender("female") }, label = { Text("🚺 Nữ") })
            }

            Text("Năm sinh: ${uiState.editBirthYear}", style = MaterialTheme.typography.titleMedium)
            Slider(value = uiState.editBirthYear.toFloat(), onValueChange = { viewModel.updateEditBirthYear(it.toInt()) }, valueRange = 1940f..2015f)

            Text("Chiều cao: ${uiState.editHeightCm} cm", style = MaterialTheme.typography.titleMedium)
            Slider(value = uiState.editHeightCm.toFloat(), onValueChange = { viewModel.updateEditHeightCm(it.toInt()) }, valueRange = 100f..250f)

            Text("Cân nặng: ${"%.1f".format(uiState.editWeightKg)} kg", style = MaterialTheme.typography.titleMedium)
            Slider(value = uiState.editWeightKg.toFloat(), onValueChange = { viewModel.updateEditWeightKg((it * 10).toInt() / 10.0) }, valueRange = 30f..200f)

            Text("Mục tiêu", style = MaterialTheme.typography.titleMedium)
            goalOptions.forEach { option ->
                FilterChip(selected = uiState.editGoal == option.value, onClick = { viewModel.updateEditGoal(option.value) },
                    label = { Text("${option.emoji} ${option.label}") })
            }

            Text("Mức độ vận động", style = MaterialTheme.typography.titleMedium)
            activityLevelOptions.forEach { option ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = uiState.editActivityLevel == option.value, onClick = { viewModel.updateEditActivityLevel(option.value) })
                    Text("${option.emoji} ${option.label}")
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
    }
}

@Composable
private fun MetricCard(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
            if (unit.isNotBlank()) Text(unit, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun MacroBar(label: String, value: Int, maxValue: Int, color: androidx.compose.ui.graphics.Color) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text("${value}g", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { if (maxValue > 0) (value.toFloat() / maxValue).coerceIn(0f, 1f) else 0f },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = color, trackColor = color.copy(alpha = 0.2f))
    }
}
