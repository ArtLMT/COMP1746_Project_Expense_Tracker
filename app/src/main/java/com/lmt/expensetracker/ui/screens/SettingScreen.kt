package com.lmt.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lmt.expensetracker.viewmodel.ProjectViewModel

@Composable
fun SettingScreen(
    viewModel: ProjectViewModel
) {
    // Lắng nghe trạng thái Theme từ ViewModel
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val syncUiState by viewModel.syncUiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            // Tự động lật màu nền
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Settings",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                // Card nổi bọc quanh cái nút gạt
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dark Mode",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Switch(
                checked = isDarkTheme,
                onCheckedChange = { viewModel.toggleTheme() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { viewModel.syncNow() },
            enabled = !syncUiState.isSyncing,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (syncUiState.isSyncing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Syncing...")
            } else {
                Text("Sync Local Data to Firebase")
            }
        }

        if (syncUiState.message != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = syncUiState.message ?: "",
                color = if (syncUiState.isError) MaterialTheme.colorScheme.error else Color(0xFF2E7D32),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}