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
import com.lmt.expensetracker.ui.theme.CustomColors
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
            .background(CustomColors.BackgroundDark),
        containerColor = CustomColors.BackgroundDark,
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
                containerColor = CustomColors.Primary,
                contentColor = CustomColors.BackgroundDark,
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Add Project",
                    modifier = Modifier.size(32.dp),
                    tint = CustomColors.BackgroundDark
                )
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
                    navController.navigate(Routes.EXPENSE_LIST) {
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

            // All Expenses List
            composable(Routes.EXPENSE_LIST) {
                ExpenseListScreen(
                    viewModel = expenseViewModel,
                    projectId = null,
                    onNavigateToCreateExpense = {
                        expenseViewModel.resetForm()
                        expenseViewModel.setEditMode(false)
                        navController.navigate(Routes.expenseForm(""))
                    },
                    onNavigateToEditExpense = { expenseId ->
                        expenseViewModel.loadExpenseForEdit(expenseId)
                        expenseViewModel.setEditMode(true)
                        navController.navigate(Routes.expenseForm(""))
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
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
