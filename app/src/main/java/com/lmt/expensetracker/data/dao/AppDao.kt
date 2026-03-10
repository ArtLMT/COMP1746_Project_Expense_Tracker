package com.lmt.expensetracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lmt.expensetracker.data.entities.ExpenseEntity
import com.lmt.expensetracker.data.entities.ProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // ==================== PROJECT OPERATIONS ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE projectId = :projectId")
    suspend fun deleteProjectById(projectId: String)

    @Query("SELECT * FROM projects")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE projectId = :projectId")
    fun getProjectById(projectId: String): Flow<ProjectEntity?>

    @Query("SELECT COUNT(*) FROM projects")
    fun getProjectCount(): Flow<Int>

    // Search projects by name or description (real-time)
    @Query("""
        SELECT * FROM projects 
        WHERE name LIKE '%' || :searchQuery || '%' 
        OR description LIKE '%' || :searchQuery || '%'
        ORDER BY startDate DESC
    """)
    fun searchProjects(searchQuery: String): Flow<List<ProjectEntity>>

    // Filter projects by status
    @Query("SELECT * FROM projects WHERE status = :status ORDER BY startDate DESC")
    fun getProjectsByStatus(status: String): Flow<List<ProjectEntity>>

    // Filter projects by manager
    @Query("SELECT * FROM projects WHERE manager = :manager ORDER BY startDate DESC")
    fun getProjectsByManager(manager: String): Flow<List<ProjectEntity>>

    // Filter projects by date range
    @Query("""
        SELECT * FROM projects 
        WHERE startDate >= :startDate AND endDate <= :endDate
        ORDER BY startDate DESC
    """)
    fun getProjectsByDateRange(startDate: String, endDate: String): Flow<List<ProjectEntity>>

    // ==================== PROJECT WITH SPENT AGGREGATION ====================

    @Query("""
        SELECT p.*, COALESCE(SUM(e.amount), 0.0) as spentAmount 
        FROM projects p 
        LEFT JOIN expenses e ON p.projectId = e.projectId 
        GROUP BY p.projectId
        ORDER BY p.startDate DESC
    """)
    fun getAllProjectsWithSpent(): Flow<List<com.lmt.expensetracker.data.entities.ProjectWithSpent>>

    @Query("""
        SELECT p.*, COALESCE(SUM(e.amount), 0.0) as spentAmount 
        FROM projects p 
        LEFT JOIN expenses e ON p.projectId = e.projectId 
        WHERE p.name LIKE '%' || :searchQuery || '%' 
        OR p.description LIKE '%' || :searchQuery || '%'
        GROUP BY p.projectId
        ORDER BY p.startDate DESC
    """)
    fun searchProjectsWithSpent(searchQuery: String): Flow<List<com.lmt.expensetracker.data.entities.ProjectWithSpent>>

    // ==================== EXPENSE OPERATIONS ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE expenseId = :expenseId")
    suspend fun deleteExpenseById(expenseId: String)

    @Query("SELECT * FROM expenses")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE expenseId = :expenseId")
    fun getExpenseById(expenseId: String): Flow<ExpenseEntity?>

    @Query("SELECT * FROM expenses WHERE projectId = :projectId ORDER BY date DESC")
    fun getExpensesByProjectId(projectId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT COUNT(*) FROM expenses WHERE projectId = :projectId")
    fun getExpenseCountByProject(projectId: String): Flow<Int>

    @Query("SELECT SUM(amount) FROM expenses WHERE projectId = :projectId")
    fun getTotalExpensesByProject(projectId: String): Flow<Double?>

    @Query("SELECT SUM(amount) FROM expenses WHERE projectId = :projectId AND status = :status")
    fun getTotalExpensesByProjectAndStatus(projectId: String, status: String): Flow<Double?>

    // Search expenses by description
    @Query("""
        SELECT * FROM expenses 
        WHERE description LIKE '%' || :searchQuery || '%'
        OR location LIKE '%' || :searchQuery || '%'
        OR claimant LIKE '%' || :searchQuery || '%'
        ORDER BY date DESC
    """)
    fun searchExpenses(searchQuery: String): Flow<List<ExpenseEntity>>

    // Filter expenses by status
    @Query("SELECT * FROM expenses WHERE status = :status ORDER BY date DESC")
    fun getExpensesByStatus(status: String): Flow<List<ExpenseEntity>>

    // Filter expenses by type
    @Query("SELECT * FROM expenses WHERE type = :type ORDER BY date DESC")
    fun getExpensesByType(type: String): Flow<List<ExpenseEntity>>

    // Filter expenses by date range
    @Query("""
        SELECT * FROM expenses 
        WHERE date >= :startDate AND date <= :endDate
        ORDER BY date DESC
    """)
    fun getExpensesByDateRange(startDate: String, endDate: String): Flow<List<ExpenseEntity>>

    // Filter expenses by project and status
    @Query("""
        SELECT * FROM expenses 
        WHERE projectId = :projectId AND status = :status
        ORDER BY date DESC
    """)
    fun getExpensesByProjectAndStatus(projectId: String, status: String): Flow<List<ExpenseEntity>>

    // ==================== AGGREGATION QUERIES ====================

    @Query("SELECT COUNT(*) FROM expenses")
    fun getExpenseCount(): Flow<Int>

    @Query("SELECT SUM(amount) FROM expenses")
    fun getTotalExpenses(): Flow<Double?>

    @Query("SELECT SUM(budget) FROM projects")
    fun getTotalBudget(): Flow<Double?>
}
