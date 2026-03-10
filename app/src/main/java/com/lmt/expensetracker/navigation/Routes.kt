package com.lmt.expensetracker.navigation

object Routes {
    const val PROJECT_LIST = "projects"
    const val PROJECT_FORM = "project_form"
    const val EXPENSE_LIST = "expenses"
    const val EXPENSE_FORM = "expense_form/{projectId}"

    fun getExpenseFormRoute(projectId: String) = "expense_form/$projectId"
}
