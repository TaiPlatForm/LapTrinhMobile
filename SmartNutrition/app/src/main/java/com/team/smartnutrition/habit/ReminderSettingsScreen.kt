package com.team.smartnutrition.habit

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.team.smartnutrition.common.components.SmartTopBar
import com.team.smartnutrition.habit.viewmodel.HabitUiState
import com.team.smartnutrition.habit.viewmodel.HabitViewModel

/**
 * ═══════════════════════════════════════════
 * MODULE 4 - REMINDER SETTINGS SCREEN
 * ═══════════════════════════════════════════
 *
 * Cài đặt:
 * - Mục tiêu cốc nước/ngày (5/6/7/8/10)
 * - Nhắc uống nước: switch + interval (1h/2h/3h) + start/end hour
 * - Nhắc uống vitamin: switch + giờ cố định
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderSettingsScreen(
    navController: NavController,
    viewModel: HabitViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Permission launcher cho POST_NOTIFICATIONS (Android 13+)
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pendingAction?.invoke()
        }
        pendingAction = null
    }

    /** Check và xin quyền trước khi bật nhắc nhở */
    fun withNotificationPermission(action: () -> Unit) {
        if (Build.VERSION.SDK_INT >= 33) {
            val permissionState = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionState == PackageManager.PERMISSION_GRANTED) {
                action()
            } else {
                pendingAction = action
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            action()
        }
    }

    Scaffold(
        topBar = {
            SmartTopBar(
                title = "Cài đặt nhắc nhở",
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // Exact alarm permission warning (Android 12+)
            ExactAlarmWarning(context)

            // Water goal selector
            WaterGoalSelector(
                currentGoal = uiState.waterGoal,
                onGoalSelected = { viewModel.setWaterGoal(it) }
            )

            // Water reminder section
            WaterReminderSection(
                uiState = uiState,
                onEnabledChanged = { enabled ->
                    if (enabled) {
                        withNotificationPermission {
                            viewModel.setWaterReminderEnabled(true)
                        }
                    } else {
                        viewModel.setWaterReminderEnabled(false)
                    }
                },
                onIntervalChanged = { viewModel.setWaterInterval(it) },
                onStartHourChanged = { viewModel.setWaterStartHour(it) },
                onEndHourChanged = { viewModel.setWaterEndHour(it) }
            )

            // Vitamin reminder section
            VitaminReminderSection(
                uiState = uiState,
                onEnabledChanged = { enabled ->
                    if (enabled) {
                        withNotificationPermission {
                            viewModel.setVitaminReminderEnabled(true)
                        }
                    } else {
                        viewModel.setVitaminReminderEnabled(false)
                    }
                },
                onTimeChanged = { hour, minute -> viewModel.setVitaminTime(hour, minute) }
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════
// EXACT ALARM WARNING (Android 12+)
// ═══════════════════════════════════════════════════════════

@Composable
private fun ExactAlarmWarning(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (!alarmManager.canScheduleExactAlarms()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚠️ Cần cấp quyền đặt báo thức",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Để nhắc nhở hoạt động chính xác, bạn cần cấp quyền trong cài đặt hệ thống.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(Modifier.height(8.dp))
                    TextButton(
                        onClick = {
                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                            context.startActivity(intent)
                        }
                    ) {
                        Text("Mở cài đặt →")
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// WATER GOAL SELECTOR
// ═══════════════════════════════════════════════════════════

@Composable
private fun WaterGoalSelector(
    currentGoal: Int,
    onGoalSelected: (Int) -> Unit
) {
    val options = listOf(5, 6, 7, 8, 10)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🎯 Mục tiêu nước (cốc/ngày)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { goal ->
                    val isSelected = goal == currentGoal
                    FilterChip(
                        selected = isSelected,
                        onClick = { onGoalSelected(goal) },
                        label = {
                            Text(
                                text = "$goal",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "= ${currentGoal * 250}ml / ngày",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// WATER REMINDER SECTION
// ═══════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WaterReminderSection(
    uiState: HabitUiState,
    onEnabledChanged: (Boolean) -> Unit,
    onIntervalChanged: (Int) -> Unit,
    onStartHourChanged: (Int) -> Unit,
    onEndHourChanged: (Int) -> Unit
) {
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Switch header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💧 Nhắc uống nước",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Switch(
                    checked = uiState.waterReminderEnabled,
                    onCheckedChange = { onEnabledChanged(it) }
                )
            }

            // Sub-settings (hiện khi bật)
            AnimatedVisibility(visible = uiState.waterReminderEnabled) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(12.dp))

                    // Interval selector
                    Text(
                        text = "Chu kỳ nhắc:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1, 2, 3).forEach { interval ->
                            FilterChip(
                                selected = uiState.waterIntervalHours == interval,
                                onClick = { onIntervalChanged(interval) },
                                label = { Text("Mỗi ${interval}h") }
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Giờ bắt đầu / kết thúc
                    Text(
                        text = "Khung giờ nhắc:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showStartTimePicker = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Từ ${uiState.waterStartHour}:00")
                        }
                        OutlinedButton(
                            onClick = { showEndTimePicker = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Đến ${uiState.waterEndHour}:00")
                        }
                    }
                }
            }
        }
    }

    // TimePicker Dialogs
    if (showStartTimePicker) {
        TimePickerDialog(
            initialHour = uiState.waterStartHour,
            initialMinute = 0,
            onConfirm = { hour, _ ->
                onStartHourChanged(hour)
                showStartTimePicker = false
            },
            onDismiss = { showStartTimePicker = false }
        )
    }
    if (showEndTimePicker) {
        TimePickerDialog(
            initialHour = uiState.waterEndHour,
            initialMinute = 0,
            onConfirm = { hour, _ ->
                onEndHourChanged(hour)
                showEndTimePicker = false
            },
            onDismiss = { showEndTimePicker = false }
        )
    }
}

// ═══════════════════════════════════════════════════════════
// VITAMIN REMINDER SECTION
// ═══════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VitaminReminderSection(
    uiState: HabitUiState,
    onEnabledChanged: (Boolean) -> Unit,
    onTimeChanged: (hour: Int, minute: Int) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
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
                    text = "💊 Nhắc uống vitamin",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Switch(
                    checked = uiState.vitaminReminderEnabled,
                    onCheckedChange = { onEnabledChanged(it) }
                )
            }

            AnimatedVisibility(visible = uiState.vitaminReminderEnabled) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Giờ nhắc:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "⏰ ${String.format("%02d:%02d", uiState.vitaminHour, uiState.vitaminMinute)}",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            initialHour = uiState.vitaminHour,
            initialMinute = uiState.vitaminMinute,
            onConfirm = { hour, minute ->
                onTimeChanged(hour, minute)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

// ═══════════════════════════════════════════════════════════
// TIME PICKER DIALOG - Helper
// ═══════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int = 0,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chọn giờ") },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(state = timePickerState)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(timePickerState.hour, timePickerState.minute) }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
