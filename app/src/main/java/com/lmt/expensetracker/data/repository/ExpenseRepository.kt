package com.lmt.expensetracker.data.repository

import android.content.Context
import com.lmt.expensetracker.data.dao.AppDao
import com.lmt.expensetracker.data.entities.ExpenseEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing [ExpenseEntity] data with local Room persistence
 * and Firebase cloud synchronization via [ProjectRepository.syncAllToCloud].
 *
 * @param dao               The Room DAO for local database operations.
 * @param projectRepository Used to trigger cloud sync after local mutations.
 */
class ExpenseRepository(
    private val dao: AppDao,
    private val projectRepository: ProjectRepository
) {

    // ==================== MUTATING OPERATIONS (with auto-sync) ====================

    /**
     * Inserts an expense locally and triggers a cloud sync.
     * Local insert always succeeds; sync failure is reported via [Result].
     */
    suspend fun insertExpense(expense: ExpenseEntity, context: Context): Result<Unit> {
        dao.insertExpense(expense)
        return projectRepository.syncAllToCloud(context)
    }

    /**
     * Updates an expense locally and triggers a cloud sync.
     */
    suspend fun updateExpense(expense: ExpenseEntity, context: Context): Result<Unit> {
        dao.updateExpense(expense)
        return projectRepository.syncAllToCloud(context)
    }

    /**
     * Deletes an expense locally and triggers a cloud sync.
     */
    suspend fun deleteExpense(expense: ExpenseEntity, context: Context): Result<Unit> {
        dao.deleteExpense(expense)
        return projectRepository.syncAllToCloud(context)
    }

    /**
     * Deletes an expense by ID locally and triggers a cloud sync.
     */
    suspend fun deleteExpenseById(expenseId: String, context: Context): Result<Unit> {
        dao.deleteExpenseById(expenseId)
        return projectRepository.syncAllToCloud(context)
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
