package com.lmt.expensetracker.navigation

import androidx.compose.runtime.Composable
import com.lmt.expensetracker.data.database.AppDatabase
import com.lmt.expensetracker.ui.screens.HomeScreen

@Composable
fun AppNavigation(
    database: AppDatabase
) {
    HomeScreen(
        database = database
    )
}
