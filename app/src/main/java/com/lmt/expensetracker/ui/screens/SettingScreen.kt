package com.lmt.expensetracker.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lmt.expensetracker.viewmodel.ProjectViewModel
import com.lmt.expensetracker.viewmodel.SettingsEvent

@Composable
fun SettingScreen(
    viewModel: ProjectViewModel
) {
    // Lắng nghe trạng thái Theme từ ViewModel
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val syncUiState by viewModel.syncUiState.collectAsState()
    val restoreUiState by viewModel.restoreUiState.collectAsState()
    val context = LocalContext.current

    // Collect one-time events from the Channel.
    // LaunchedEffect(Unit) runs once per composition lifetime — it does NOT
    // re-execute on recomposition or when navigating back to this screen,
    // so Toast messages are guaranteed to appear exactly once per event.
    LaunchedEffect(Unit) {
        viewModel.settingsEvents.collect { event ->
            when (event) {
                is SettingsEvent.SyncResult -> Toast.makeText(
                    context, event.message, Toast.LENGTH_LONG
                ).show()
                is SettingsEvent.RestoreResult -> Toast.makeText(
                    context, event.message, Toast.LENGTH_LONG
                ).show()
            }
        }
    }

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

        // Sync Local to Firebase
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
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sync Local Data to Firebase")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Restore Firebase to Local
        OutlinedButton(
            onClick = { viewModel.restoreNow() },
            enabled = !restoreUiState.isRestoring,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (restoreUiState.isRestoring) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Restoring...")
            } else {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Restore Data from Firebase")
            }
        }
    }
}