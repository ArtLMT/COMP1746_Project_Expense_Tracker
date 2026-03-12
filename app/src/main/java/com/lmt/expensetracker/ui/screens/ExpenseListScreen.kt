package com.lmt.expensetracker.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lmt.expensetracker.ui.components.BudgetComparisonCard
import com.lmt.expensetracker.ui.components.ExpenseCard
import com.lmt.expensetracker.ui.components.HeaderSection
import com.lmt.expensetracker.viewmodel.ExpenseViewModel

@SuppressLint("DefaultLocale")
@Composable
fun ExpenseListScreen(
    viewModel: ExpenseViewModel,
    projectId: String?,
    onNavigateToCreateExpense: () -> Unit,
    onNavigateToEditExpense: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val listState by viewModel.listState.collectAsStateWithLifecycle()

    // Local UI state for the header tabs
    var selectedTab by remember { mutableIntStateOf(0) }

    // Build status filters list from ViewModel counts
    val statusCounts = listState.statusCounts
    val allCount = statusCounts.pending + statusCounts.paid + statusCounts.reimbursed
    val statusFilters = listOf(
        "All" to allCount,
        "Pending" to statusCounts.pending,
        "Paid" to statusCounts.paid,
        "Reimbursed" to statusCounts.reimbursed
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateExpense,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Add Expense"
                )
            }
        }
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // ---- Generic Header: title, search, filter chips ----
            HeaderSection(
                title = if (projectId != null) "Project Expenses" else "All Expenses",
                searchQuery = listState.searchQuery,
                onSearchChange = { viewModel.searchExpenses(it) },
                searchPlaceholder = "Search expenses...",
                selectedTab = selectedTab,
                onTabSelected = { index ->
                    selectedTab = index
                    when (index) {
                        0 -> viewModel.filterByStatus(null)
                        1 -> viewModel.filterByStatus("Pending")
                        2 -> viewModel.filterByStatus("Paid")
                        3 -> viewModel.filterByStatus("Reimbursed")
                    }
                },
                statusFilters = statusFilters,
                onBackClick = if (projectId != null) onNavigateBack else null
            )

            // ---- Budget Comparison Card ----
            BudgetComparisonCard(
                totalExpenses = listState.totalAmount,
                projectBudget = listState.projectBudget,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ---- Expenses List ----
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
            ) {
                when {
                    listState.isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Loading expenses...")
                        }
                    }
                    listState.error != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Error: ${listState.error}")
                        }
                    }
                    listState.expenses.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No expenses found")
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            items(
                                items = listState.expenses,
                                key = { it.expenseId }
                            ) { expense ->
                                ExpenseCard(
                                    expense = expense,
                                    onEdit = { onNavigateToEditExpense(expense.expenseId) },
                                    onDelete = { viewModel.deleteExpense(expense) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
