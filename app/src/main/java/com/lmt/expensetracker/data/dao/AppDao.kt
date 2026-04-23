package com.lmt.expensetracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.lmt.expensetracker.data.entities.ExpenseEntity
import com.lmt.expensetracker.data.entities.ProjectEntity
import com.lmt.expensetracker.data.entities.ProjectWithSpent
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // ==================== PROJECT OPERATIONS ====================

//    @Insert(onConflict = OnConflictStrategy.REPLACE)
    // bản chất thằng này là nó sẽ xóa thằng cũ đi nếu có conflict rồi tạo lại cái mới
    // Vì thế mà nó sẽ xóa luôn mấy cái khóa ngoại, nếu có On Delete Cascade nữa là bay hết luôn
    @Upsert
    suspend fun insertProject(project: ProjectEntity)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE projectId = :projectId")
    suspend fun deleteProjectById(projectId: String)

    @Query("SELECT * FROM projects WHERE isDeleted = 0")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE projectId = :projectId")
    fun getProjectById(projectId: String): Flow<ProjectEntity?>

    @Query("SELECT COUNT(*) FROM projects WHERE isDeleted = 0")
    fun getProjectCount(): Flow<Int>

    // Search projects by name or description (real-time)
    @Query("""
        SELECT * FROM projects
        WHERE isDeleted = 0
        AND (name LIKE '%' || :searchQuery || '%'
        OR description LIKE '%' || :searchQuery || '%')
        ORDER BY startDate DESC
    """)
    fun searchProjects(searchQuery: String): Flow<List<ProjectEntity>>

    // Filter projects by status
    @Query("SELECT * FROM projects WHERE isDeleted = 0 AND status = :status ORDER BY startDate DESC")
    fun getProjectsByStatus(status: String): Flow<List<ProjectEntity>>

    // Filter projects by manager
    @Query("SELECT * FROM projects WHERE isDeleted = 0 AND manager = :manager ORDER BY startDate DESC")
    fun getProjectsByManager(manager: String): Flow<List<ProjectEntity>>

    // Filter projects by date range
    @Query("""
        SELECT * FROM projects
        WHERE isDeleted = 0
        AND startDate >= :startDate AND endDate <= :endDate
        ORDER BY startDate DESC
    """)
    fun getProjectsByDateRange(startDate: String, endDate: String): Flow<List<ProjectEntity>>

    /** One-shot fetch excluding tombstones – for legacy use. */
    @Query("SELECT * FROM projects WHERE isDeleted = 0")
    suspend fun getAllProjectsStatic(): List<ProjectEntity>

    /** One-shot fetch of ALL rows including tombstones – used by syncAllToCloud(). */
    @Query("SELECT * FROM projects")
    suspend fun getAllProjectsStaticRaw(): List<ProjectEntity>

    /**
     * Soft-deletes a project by marking it as deleted with a timestamp.
     * The tombstone row will be pushed to Firestore by the next syncAllToCloud().
     */
    @Query("UPDATE projects SET isDeleted = 1, updatedAt = :ts WHERE projectId = :id")
    suspend fun softDeleteProject(id: String, ts: Long = System.currentTimeMillis())

    /** Hard-deletes a project row. Called only by the sync layer to apply cloud tombstones. */
    @Query("DELETE FROM projects WHERE projectId = :id")
    suspend fun hardDeleteProject(id: String)

    /** Static single-row lookup for conflict resolution in restoreFromCloud(). */
    @Query("SELECT * FROM projects WHERE projectId = :id LIMIT 1")
    suspend fun getProjectByIdStatic(id: String): ProjectEntity?

    // ==================== PROJECT WITH SPENT AGGREGATION ====================

    @Query("""
        SELECT p.*, COALESCE(SUM(e.amount), 0.0) as spentAmount
        FROM projects p
        LEFT JOIN expenses e ON p.projectId = e.projectId AND e.isDeleted = 0
        WHERE p.isDeleted = 0
        GROUP BY p.projectId
        ORDER BY p.startDate DESC
    """)
    fun getAllProjectsWithSpent(): Flow<List<ProjectWithSpent>>

    @Query("""
        SELECT p.*, COALESCE(SUM(e.amount), 0.0) as spentAmount
        FROM projects p
        LEFT JOIN expenses e ON p.projectId = e.projectId AND e.isDeleted = 0
        WHERE p.isDeleted = 0
        AND (p.name LIKE '%' || :searchQuery || '%'
        OR p.description LIKE '%' || :searchQuery || '%')
        GROUP BY p.projectId
        ORDER BY p.startDate DESC
    """)
    fun searchProjectsWithSpent(searchQuery: String): Flow<List<ProjectWithSpent>>

    // ==================== EXPENSE OPERATIONS ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE expenseId = :expenseId")
    suspend fun deleteExpenseById(expenseId: String)

    @Query("SELECT * FROM expenses WHERE isDeleted = 0")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE expenseId = :expenseId")
    fun getExpenseById(expenseId: String): Flow<ExpenseEntity?>

    @Query("SELECT * FROM expenses WHERE isDeleted = 0 AND projectId = :projectId ORDER BY date DESC")
    fun getExpensesByProjectId(projectId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT COUNT(*) FROM expenses WHERE isDeleted = 0 AND projectId = :projectId")
    fun getExpenseCountByProject(projectId: String): Flow<Int>

    @Query("SELECT SUM(amount) FROM expenses WHERE isDeleted = 0 AND projectId = :projectId")
    fun getTotalExpensesByProject(projectId: String): Flow<Double?>

    @Query("SELECT SUM(amount) FROM expenses WHERE isDeleted = 0 AND projectId = :projectId AND status = :status")
    fun getTotalExpensesByProjectAndStatus(projectId: String, status: String): Flow<Double?>

    // Search expenses by description
    @Query("""
        SELECT * FROM expenses
        WHERE isDeleted = 0
        AND (description LIKE '%' || :searchQuery || '%'
        OR location LIKE '%' || :searchQuery || '%'
        OR claimant LIKE '%' || :searchQuery || '%')
        ORDER BY date DESC
    """)
    fun searchExpenses(searchQuery: String): Flow<List<ExpenseEntity>>

    // Filter expenses by status
    @Query("SELECT * FROM expenses WHERE isDeleted = 0 AND status = :status ORDER BY date DESC")
    fun getExpensesByStatus(status: String): Flow<List<ExpenseEntity>>

    // Filter expenses by type
    @Query("SELECT * FROM expenses WHERE isDeleted = 0 AND type = :type ORDER BY date DESC")
    fun getExpensesByType(type: String): Flow<List<ExpenseEntity>>

    // Filter expenses by date range
    @Query("""
        SELECT * FROM expenses
        WHERE isDeleted = 0
        AND date >= :startDate AND date <= :endDate
        ORDER BY date DESC
    """)
    fun getExpensesByDateRange(startDate: String, endDate: String): Flow<List<ExpenseEntity>>

    // Filter expenses by project and status
    @Query("""
        SELECT * FROM expenses
        WHERE isDeleted = 0
        AND projectId = :projectId AND status = :status
        ORDER BY date DESC
    """)
    fun getExpensesByProjectAndStatus(projectId: String, status: String): Flow<List<ExpenseEntity>>

    // Static fetch for Firebase sync (excludes tombstones – legacy compat)
    @Query("SELECT * FROM expenses WHERE isDeleted = 0")
    suspend fun getAllExpensesStatic(): List<ExpenseEntity>

    /** One-shot fetch of ALL rows including tombstones – used by syncAllToCloud(). */
    @Query("SELECT * FROM expenses")
    suspend fun getAllExpensesStaticRaw(): List<ExpenseEntity>

    /**
     * Soft-deletes an expense by marking it as deleted with a timestamp.
     * The tombstone row will be pushed to Firestore by the next syncAllToCloud().
     */
    @Query("UPDATE expenses SET isDeleted = 1, updatedAt = :ts WHERE expenseId = :id")
    suspend fun softDeleteExpense(id: String, ts: Long = System.currentTimeMillis())

    /** Hard-deletes an expense row. Called only by the sync layer to apply cloud tombstones. */
    @Query("DELETE FROM expenses WHERE expenseId = :id")
    suspend fun hardDeleteExpense(id: String)

    /** Static single-row lookup for conflict resolution in restoreFromCloud(). */
    @Query("SELECT * FROM expenses WHERE expenseId = :id LIMIT 1")
    suspend fun getExpenseByIdStatic(id: String): ExpenseEntity?

    // ==================== BULK UPSERT (Restore from Cloud) ====================

    /**
     * Upserts a list of projects. Inserts new records and updates existing
     * ones matched by [ProjectEntity.projectId].
     * Does NOT delete unmatched local records – avoids cascading FK issues.
     */
    @Upsert
    suspend fun upsertProjects(projects: List<ProjectEntity>)

    /**
     * Upserts a list of expenses. Inserts new records and updates existing
     * ones matched by [ExpenseEntity.expenseId].
     * Does NOT delete unmatched local records – avoids cascading FK issues.
     */
    @Upsert
    suspend fun upsertExpenses(expenses: List<ExpenseEntity>)

    // ==================== AGGREGATION QUERIES ====================

    @Query("SELECT COUNT(*) FROM expenses WHERE isDeleted = 0")
    fun getExpenseCount(): Flow<Int>

    @Query("SELECT SUM(amount) FROM expenses WHERE isDeleted = 0")
    fun getTotalExpenses(): Flow<Double?>

    @Query("SELECT SUM(budget) FROM projects WHERE isDeleted = 0")
    fun getTotalBudget(): Flow<Double?>
}
