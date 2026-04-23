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
    suspend fun deleteExpense(expenseId: String, context: Context): Result<Unit> {
//        dao.deleteExpense(expense)
        dao.softDeleteExpense(expenseId)
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
}
