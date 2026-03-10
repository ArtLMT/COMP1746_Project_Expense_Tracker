package com.lmt.expensetracker.navigation

object Routes {
    const val DASHBOARD = "dashboard"
    const val SETTING = "settings"
    const val PROJECT_FORM = "project_form"
    const val EXPENSE_LIST = "expenses"
    const val EXPENSE_FORM = "expense_form/{projectId}"
    const val EXPENSE_LIST_FOR_PROJECT = "expenses/{projectId}"
    const val EXPENSE_FORM_FOR_PROJECT = "expense_form_project/{projectId}"


    fun expenseListForProject(projectId: String) = "expenses/$projectId"
    fun expenseFormForProject(projectId: String) = "expense_form_project/$projectId"
    fun expenseForm(projectId: String) = "expense_form/$projectId"
}
