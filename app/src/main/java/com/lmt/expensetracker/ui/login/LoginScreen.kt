package com.lmt.expensetracker.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions // Cần nếu bạn chỉnh kiểu bàn phím email/số
import androidx.compose.material3.*
import androidx.compose.runtime.* // Quan trọng nhất để dùng State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation // Cần để ẩn mật khẩu
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

import androidx.lifecycle.viewmodel.compose.viewModel // Thằng này là hàm giúp ViewModel hoạt động với Compose

@Composable
fun LoginScreen(viewModel: LoginViewModel = viewModel()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            value = viewModel.email, // Lấy từ ViewModel
            onValueChange = { viewModel.email = it },
            label = { Text("Email") }
        )

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            value = viewModel.password,
            onValueChange = { viewModel.password = it },
            label = { Text("Password") }
        )

        Button(
            onClick = { viewModel.onLoginClick() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary, // Set the background color
                contentColor = MaterialTheme.colorScheme.onPrimary)    // Set the text/content color
        ) {
            Text("Login")
        }

        if (viewModel.loginStatus.isNotEmpty()) {
            Text(text = viewModel.loginStatus, modifier = Modifier.padding(top = 16.dp))
        }
    }
}