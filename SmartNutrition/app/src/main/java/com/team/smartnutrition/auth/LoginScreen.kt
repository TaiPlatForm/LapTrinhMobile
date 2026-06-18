package com.team.smartnutrition.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.team.smartnutrition.navigation.Screen

/**
 * ═══════════════════════════════════════════
 * MODULE 1 - TV1: MÀN HÌNH ĐĂNG NHẬP
 * ═══════════════════════════════════════════
 *
 * TODO cho TV1:
 * 1. Kết nối Firebase Auth (signInWithEmailAndPassword)
 * 2. Thêm Google Sign-In (CredentialManager)
 * 3. Kiểm tra user đã có profile chưa → navigate phù hợp
 * 4. Xử lý loading state và error messages
 */
@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo / App Name
        Text(
            text = "🥗",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Smart Nutrition",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Quản lý dinh dưỡng thông minh với AI",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
        )

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mật khẩu") },
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff,
                        contentDescription = "Toggle password"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Login Button
        Button(
            onClick = {
                // TODO: TV1 - Gọi Firebase Auth signInWithEmailAndPassword()
                // Thành công → navController.navigate(Screen.Home.route)
                // Lần đầu → navController.navigate(Screen.ProfileSetup.route)
                isLoading = true

                // DEMO: Navigate thẳng tới Home (xóa khi implement thật)
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Đăng nhập", style = MaterialTheme.typography.labelLarge)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Google Sign-In Button
        OutlinedButton(
            onClick = {
                // TODO: TV1 - Google Sign-In bằng CredentialManager
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("🔵 Đăng nhập bằng Google")
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Register Link
        TextButton(onClick = { navController.navigate(Screen.Register.route) }) {
            Text(
                "Chưa có tài khoản? Đăng ký ngay",
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
