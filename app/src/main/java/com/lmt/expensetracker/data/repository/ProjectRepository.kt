package com.lmt.expensetracker.data.repository

import android.content.Context
import android.util.Log
import com.lmt.expensetracker.data.dao.AppDao
import com.lmt.expensetracker.data.entities.ProjectEntity
import com.lmt.expensetracker.data.entities.ProjectWithSpent
import com.lmt.expensetracker.data.remote.FirestoreService
import com.lmt.expensetracker.utils.NetworkUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

/**
 * Repository for managing [ProjectEntity] data with local Room persistence
 * and Cloud Firestore synchronization.
 *
 * This replaces the previous Firebase Realtime Database integration.
 * Local Room remains the single source of truth; Firestore is the cloud
 * backup / sync target.
 *
 * @param dao              The Room DAO for local database operations.
 * @param firestoreService The service responsible for Firestore CRUD operations.
 * @param ioDispatcher     The [CoroutineDispatcher] used for I/O-bound work
 *                         (defaults to [Dispatchers.IO]).
 */
class ProjectRepository(
    private val dao: AppDao,
    private val firestoreService: FirestoreService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private companion object {
        const val SYNC_TIMEOUT_MS = 15000L
    }

    // ==================== CLOUD SYNCHRONIZATION ====================

    /**
     * Synchronizes all local projects and their associated expenses to
     * Cloud Firestore using a batched write.
     *
     * **Workflow:**
     * 1. Checks for an active Wi-Fi or Cellular connection via [NetworkUtils].
     * 2. Fetches one-shot snapshots of all projects and expenses from Room.
     * 3. Uploads everything to Firestore via [FirestoreService.syncAll].
     *
     * @param context The [Context] used to check network availability.
     * @return [Result.success] if the sync completes, or [Result.failure] with
     *         a descriptive exception on network or upload errors.
     */
    suspend fun syncAllToCloud(context: Context): Result<Unit> = withContext(ioDispatcher) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            return@withContext Result.failure(Exception("No internet connection"))
        }

        try {
            val projects = dao.getAllProjectsStaticRaw()   // raw: includes isDeleted=1 tombstones
            val allExpenses = dao.getAllExpensesStaticRaw() // raw: includes isDeleted=1 tombstones

            // Firestore batched write – atomic & efficient
            withTimeout(SYNC_TIMEOUT_MS) {
                firestoreService.syncAll(projects, allExpenses)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Pulls all projects and expenses from Cloud Firestore and upserts them
     * into the local Room database.
     *
     * **Strategy:** Insert-or-Update (Upsert). Existing local records that
     * match by primary key are updated; new records are inserted. Local-only
     * records that do not exist in Firestore are **not** deleted, avoiding
     * cascading FK issues with related entities.
     *
     * **Order:** Projects are upserted first because expenses carry a foreign
     * key to `projects.projectId`.
     *
     * @param context The [Context] used to check network availability.
     * @return [Result.success] with a summary message, or [Result.failure]
     *         with a descriptive exception on network or read errors.
     */
    suspend fun restoreFromCloud(context: Context): Result<String> = withContext(ioDispatcher) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            return@withContext Result.failure(Exception("No internet connection"))
        }

        try {
            // 1. Fetch ALL docs from Firestore (including tombstones with isDeleted=true)
            val (remoteProjects, remoteExpenses) = withTimeout(SYNC_TIMEOUT_MS) {
                val projects = firestoreService.getProjects()
                val expenses = firestoreService.getAllExpenses()
                projects to expenses
            }

            // 2. Merge projects using Last-Write-Wins (LWW) on updatedAt
            for (remote in remoteProjects) {
                if (remote.isDeleted) {
                    // Cloud has confirmed this deletion → hard-delete the local row
                    dao.hardDeleteProject(remote.projectId)
                } else {
                    val local = dao.getProjectByIdStatic(remote.projectId)
                    if (local == null || remote.updatedAt > local.updatedAt) {
                        // Remote is newer (or doesn't exist locally) → accept cloud version
                        dao.upsertProjects(listOf(remote))
                    }
                    // else: local is newer → keep it; syncAllToCloud() will push it up next time
                }
            }

            // 3. Merge expenses using Last-Write-Wins (LWW) on updatedAt
            for (remote in remoteExpenses) {
                if (remote.isDeleted) {
                    dao.hardDeleteExpense(remote.expenseId)
                } else {
                    val local = dao.getExpenseByIdStatic(remote.expenseId)
                    if (local == null || remote.updatedAt > local.updatedAt) {
                        dao.upsertExpenses(listOf(remote))
                    }
                }
            }

            val liveProjects = remoteProjects.count { !it.isDeleted }
            val liveExpenses = remoteExpenses.count { !it.isDeleted }
            Result.success("Restored $liveProjects projects and $liveExpenses expenses.")

        } catch (e: Exception) {
            Log.e("SYNC_DEBUG", "restoreFromCloud error: ", e)
            if (e is CancellationException) {
                return@withContext Result.failure(Exception("Đã hết thời gian chờ (Timeout)"))
            }
            Result.failure(e)
        }
    }

    // ==================== MUTATING OPERATIONS (with auto-sync) ====================

    /**
     * Inserts a project into the local database, syncs it to Firestore,
     * and triggers a full cloud sync.
     *
     * @return [Result.success] if both the local insert and cloud sync succeed,
     *         or [Result.failure] if the sync fails (local insert is still persisted).
     */
    suspend fun insertProject(project: ProjectEntity, context: Context): Result<Unit> {
        dao.insertProject(project)
        return syncAllToCloud(context)
    }

    /**
     * Updates an existing project in the local database and triggers a cloud sync.
     * Stamps [updatedAt] with the current timestamp so the LWW conflict resolver
     * in [restoreFromCloud] always treats this as the authoritative version.
     *
     * @return [Result.success] if both the local update and cloud sync succeed,
     *         or [Result.failure] if the sync fails (local update is still persisted).
     */
    suspend fun updateProject(project: ProjectEntity, context: Context): Result<Unit> {
        val stamped = project.copy(updatedAt = System.currentTimeMillis())
        dao.updateProject(stamped)
        return syncAllToCloud(context)
    }

    /**
     * Soft-deletes a project locally (sets isDeleted=true, stamps updatedAt),
     * then syncs the tombstone to Firestore. On the next [restoreFromCloud],
     * any device seeing the tombstone will also remove it.
     *
     * @return [Result.success] if both the soft-delete and cloud sync succeed,
     *         or [Result.failure] if the sync fails (soft-delete is still persisted).
     */
    suspend fun deleteProject(project: ProjectEntity, context: Context): Result<Unit> {
        dao.softDeleteProject(project.projectId)  // was: dao.deleteProject(project)
        return syncAllToCloud(context)
    }

    /**
     * Soft-deletes a project by its ID locally (sets isDeleted=true, stamps updatedAt),
     * then syncs the tombstone to Firestore.
     *
     * @return [Result.success] if both the soft-delete and cloud sync succeed,
     *         or [Result.failure] if the sync fails (soft-delete is still persisted).
     */
    suspend fun deleteProjectById(projectId: String, context: Context): Result<Unit> {
        dao.softDeleteProject(projectId)           // was: dao.deleteProjectById(projectId)
        return syncAllToCloud(context)
    }

    // ==================== READ OPERATIONS (local only) ====================

    /** Returns a [Flow] of all projects, continuously observed. */
    fun getAllProjects(): Flow<List<ProjectEntity>> {
        return dao.getAllProjects()
    }

    /** Returns a [Flow] of a single project by its ID. */
    fun getProjectById(projectId: String): Flow<ProjectEntity?> {
        return dao.getProjectById(projectId)
    }

    /** Returns a [Flow] of the total project count. */
    fun getProjectCount(): Flow<Int> {
        return dao.getProjectCount()
    }

    /** Searches projects by name or description. Returns all if query is empty. */
    fun searchProjects(searchQuery: String): Flow<List<ProjectEntity>> {
        return if (searchQuery.isEmpty()) {
            getAllProjects()
        } else {
            dao.searchProjects(searchQuery)
        }
    }

    /** Filters projects by their status. */
    fun getProjectsByStatus(status: String): Flow<List<ProjectEntity>> {
        return dao.getProjectsByStatus(status)
    }

    /** Filters projects by their manager. */
    fun getProjectsByManager(manager: String): Flow<List<ProjectEntity>> {
        return dao.getProjectsByManager(manager)
    }

    // ==================== PROJECT WITH SPENT AGGREGATION ====================

    /** Returns all projects with their calculated spent amounts. */
    fun getAllProjectsWithSpent(): Flow<List<ProjectWithSpent>> {
        return dao.getAllProjectsWithSpent()
    }

    /** Searches projects with spent amounts. Returns all if query is empty. */
    fun searchProjectsWithSpent(searchQuery: String): Flow<List<ProjectWithSpent>> {
        return if (searchQuery.isEmpty()) {
            getAllProjectsWithSpent()
        } else {
            dao.searchProjectsWithSpent(searchQuery)
        }
    }

    /** Filters projects by date range. */
    fun getProjectsByDateRange(startDate: String, endDate: String): Flow<List<ProjectEntity>> {
        return dao.getProjectsByDateRange(startDate, endDate)
    }

    /** Returns the total budget across all projects. */
    fun getTotalBudget(): Flow<Double?> {
        return dao.getTotalBudget()
    }
}
