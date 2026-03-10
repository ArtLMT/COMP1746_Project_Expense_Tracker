package com.lmt.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.lmt.expensetracker.data.database.AppDatabase
import com.lmt.expensetracker.data.repository.ExpenseRepository
import com.lmt.expensetracker.data.repository.ProjectRepository
import com.lmt.expensetracker.navigation.AppNavigation
import com.lmt.expensetracker.ui.theme.ExpenseTrackerTheme
import com.lmt.expensetracker.viewmodel.ExpenseViewModel
import com.lmt.expensetracker.viewmodel.ProjectViewModel

class MainActivity : ComponentActivity() {
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize database
        database = AppDatabase.getDatabase(this)

        // Create repositories
        val projectRepository = ProjectRepository(database.appDao())
        val expenseRepository = ExpenseRepository(database.appDao())

        // Create ViewModels via factory (DI entry-point — easily replaced with Hilt later)
        val projectViewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return ProjectViewModel(projectRepository) as T
                }
            }
        )[ProjectViewModel::class.java]

        val expenseViewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return ExpenseViewModel(expenseRepository) as T
                }
            }
        )[ExpenseViewModel::class.java]

        setContent {
            ExpenseTrackerTheme {
                AppNavigation(
                    projectViewModel = projectViewModel,
                    expenseViewModel = expenseViewModel
                )
            }
        }
    }
}