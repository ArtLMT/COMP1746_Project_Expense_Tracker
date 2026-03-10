package com.lmt.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.lmt.expensetracker.data.database.AppDatabase
import com.lmt.expensetracker.navigation.AppNavigation
import com.lmt.expensetracker.ui.theme.ExpenseTrackerTheme

class MainActivity : ComponentActivity() {
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize database
        database = AppDatabase.getDatabase(this)

        setContent {
            ExpenseTrackerTheme {
                AppNavigation(
                    database = database
                )
            }
        }
    }
}