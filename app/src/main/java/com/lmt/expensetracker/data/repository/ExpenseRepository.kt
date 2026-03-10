package com.lmt.expensetracker.data.repository

import com.lmt.expensetracker.data.dao.AppDao
import com.lmt.expensetracker.data.entities.ExpenseEntity
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val dao: AppDao) {

    // Insert a new expense
    suspend fun insertExpense(expense: ExpenseEntity) {
        dao.insertExpense(expense)
    }

    // Update existing expense
    suspend fun updateExpense(expense: ExpenseEntity) {
        dao.updateExpense(expense)
    }

    // Delete expense
    suspend fun deleteExpense(expense: ExpenseEntity) {
        dao.deleteExpense(expense)
    }

    // Delete expense by ID
    suspend fun deleteExpenseById(expenseId: String) {
        dao.deleteExpenseById(expenseId)
    }

    // Get all expenses
    fun getAllExpenses(): Flow<List<ExpenseEntity>> {
        return dao.getAllExpenses()
    }

    // Get expense by ID
    fun getExpenseById(expenseId: String): Flow<ExpenseEntity?> {
        return dao.getExpenseById(expenseId)
    }

    // Get expenses by project ID
    fun getExpensesByProjectId(projectId: String): Flow<List<ExpenseEntity>> {
        return dao.getExpensesByProjectId(projectId)
    }

    // Get expense count
    fun getExpenseCount(): Flow<Int> {
        return dao.getExpenseCount()
    }

    // Get expense count by project
    fun getExpenseCountByProject(projectId: String): Flow<Int> {
        return dao.getExpenseCountByProject(projectId)
    }

    // Get total expenses
    fun getTotalExpenses(): Flow<Double?> {
        return dao.getTotalExpenses()
    }

    // Get total expenses by project
    fun getTotalExpensesByProject(projectId: String): Flow<Double?> {
        return dao.getTotalExpensesByProject(projectId)
    }

    // Get total expenses by project and status
    fun getTotalExpensesByProjectAndStatus(projectId: String, status: String): Flow<Double?> {
        return dao.getTotalExpensesByProjectAndStatus(projectId, status)
    }

    // Search expenses by description/location/claimant (real-time)
    fun searchExpenses(searchQuery: String): Flow<List<ExpenseEntity>> {
        return if (searchQuery.isEmpty()) {
            getAllExpenses()
        } else {
            dao.searchExpenses(searchQuery)
        }
    }

    // Filter expenses by status
    fun getExpensesByStatus(status: String): Flow<List<ExpenseEntity>> {
        return dao.getExpensesByStatus(status)
    }

    // Filter expenses by type
    fun getExpensesByType(type: String): Flow<List<ExpenseEntity>> {
        return dao.getExpensesByType(type)
    }

    // Filter expenses by date range
    fun getExpensesByDateRange(startDate: String, endDate: String): Flow<List<ExpenseEntity>> {
        return dao.getExpensesByDateRange(startDate, endDate)
    }

    // Filter expenses by project and status
    fun getExpensesByProjectAndStatus(projectId: String, status: String): Flow<List<ExpenseEntity>> {
        return dao.getExpensesByProjectAndStatus(projectId, status)
    }
}
