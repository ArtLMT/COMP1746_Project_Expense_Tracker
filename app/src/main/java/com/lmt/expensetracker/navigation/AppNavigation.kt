package com.lmt.expensetracker.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lmt.expensetracker.ui.components.CustomBottomNavigation
import com.lmt.expensetracker.ui.screens.DashboardContent
import com.lmt.expensetracker.ui.screens.ExpenseFormScreen
import com.lmt.expensetracker.ui.screens.ExpenseListScreen
import com.lmt.expensetracker.ui.screens.ProjectFormScreen
import com.lmt.expensetracker.ui.screens.SettingScreen
import com.lmt.expensetracker.viewmodel.ExpenseViewModel
import com.lmt.expensetracker.viewmodel.ProjectViewModel

@Composable
fun AppNavigation(
    projectViewModel: ProjectViewModel,
    expenseViewModel: ExpenseViewModel
) {
    val navController = rememberNavController()

    // Check backstack for current route
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    projectViewModel.resetForm()
                    projectViewModel.setEditMode(false)
                    navController.navigate(Routes.PROJECT_FORM)
                },
                modifier = Modifier
                    .size(56.dp)
                    .offset(y = 48.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Add Project",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimary                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            CustomBottomNavigation(
                currentRoute = currentRoute,
                onHomeClick = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                    }
                },
                onSettingsClick = {
                    navController.navigate(Routes.SETTING) {
                        popUpTo(Routes.DASHBOARD)
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Dashboard (Project List with Header + Search)
            composable(Routes.DASHBOARD) {
                DashboardContent(
                    viewModel = projectViewModel,
                    onNavigateToEdit = { projectId ->
                        projectViewModel.loadProjectForEdit(projectId)
                        projectViewModel.setEditMode(true)
                        navController.navigate(Routes.PROJECT_FORM)
                    },
                    onNavigateToExpenses = { projectId ->
                        navController.navigate(Routes.expenseListForProject(projectId))
                    }
                )
            }

            // Project Form (Create / Edit)
            composable(Routes.PROJECT_FORM) {
                ProjectFormScreen(
                    viewModel = projectViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                        projectViewModel.resetForm()
                    },
                    onSaveSuccess = {
                        navController.popBackStack()
                        projectViewModel.resetForm()
                    }
                )
            }

            // SETTING
            composable(Routes.SETTING) {
                SettingScreen(viewModel = projectViewModel)
            }

            // Expense Form (from All Expenses)
            composable(
                route = Routes.EXPENSE_FORM,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
                ExpenseFormScreen(
                    viewModel = expenseViewModel,
                    projectId = projectId,
                    onNavigateBack = {
                        navController.popBackStack()
                        expenseViewModel.resetForm()
                    },
                    onSaveSuccess = {
                        navController.popBackStack()
                        expenseViewModel.resetForm()
                    }
                )
            }

            // Expense List for a specific Project
            composable(
                route = Routes.EXPENSE_LIST_FOR_PROJECT,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
                ExpenseListScreen(
                    viewModel = expenseViewModel,
                    projectId = projectId,
                    onNavigateToCreateExpense = {
                        expenseViewModel.setProjectId(projectId)
                        expenseViewModel.resetForm()
                        expenseViewModel.setEditMode(false)
                        navController.navigate(Routes.expenseFormForProject(projectId))
                    },
                    onNavigateToEditExpense = { expenseId ->
                        expenseViewModel.loadExpenseForEdit(expenseId)
                        expenseViewModel.setEditMode(true)
                        navController.navigate(Routes.expenseFormForProject(projectId))
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Expense Form for a specific Project
            composable(
                route = Routes.EXPENSE_FORM_FOR_PROJECT,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
                ExpenseFormScreen(
                    viewModel = expenseViewModel,
                    projectId = projectId,
                    onNavigateBack = {
                        navController.popBackStack()
                        expenseViewModel.resetForm()
                    },
                    onSaveSuccess = {
                        navController.popBackStack()
                        expenseViewModel.resetForm()
                    }
                )
            }
        }
    }
}
