package com.lmt.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lmt.expensetracker.ui.components.HeaderSection
import com.lmt.expensetracker.ui.components.SearchBarSection
import com.lmt.expensetracker.ui.theme.CustomColors
import com.lmt.expensetracker.viewmodel.ProjectViewModel

// ============================================================================
// DASHBOARD CONTENT - MAIN UI FROM HTML
// ============================================================================
@Composable
fun DashboardContent(
    modifier: Modifier = Modifier,
    viewModel: ProjectViewModel,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToExpenses: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    val statusCounts by viewModel.statusCounts.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CustomColors.BackgroundDark)
    ) {
        // Header Section
        HeaderSection(
            selectedTab = selectedTab,
            onTabSelected = { index ->
                selectedTab = index
                when(index) {
                    0 -> viewModel.filterByStatus(null)
                    1 -> viewModel.filterByStatus("Active")
                    2 -> viewModel.filterByStatus("Completed")
                    3 -> viewModel.filterByStatus("On Hold")
                }
            },
            statusCounts = statusCounts
        )

        // Search Bar
        SearchBarSection(searchQuery = searchQuery, onSearchChange = { searchQuery = it })

        // Delegate rendering projects to ProjectListScreen!
        ProjectListScreen(
            viewModel = viewModel,
            onNavigateToCreateProject = {}, // handled by FAB in AppNavigation
            onNavigateToEditProject = onNavigateToEdit,
            onNavigateToProjectDetail = onNavigateToExpenses,
            modifier = Modifier.weight(1f)
        )
    }
}
