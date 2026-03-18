package com.lmt.expensetracker.data.repository

import android.content.Context
import com.lmt.expensetracker.data.dao.AppDao
import com.lmt.expensetracker.data.entities.ProjectEntity
import com.lmt.expensetracker.data.entities.ProjectWithSpent
import com.lmt.expensetracker.data.remote.FirebaseService
import com.lmt.expensetracker.data.remote.ProjectSyncModel
import com.lmt.expensetracker.utils.NetworkUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository for managing [ProjectEntity] data with local Room persistence
 * and Firebase cloud synchronization.
 *
 * @param dao             The Room DAO for local database operations.
 * @param firebaseService The service responsible for uploading data to Firebase.
 * @param ioDispatcher    The [CoroutineDispatcher] used for I/O-bound work
 *                        (defaults to [Dispatchers.IO]).
 */
class ProjectRepository(
    private val dao: AppDao,
    private val firebaseService: FirebaseService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    // ==================== CLOUD SYNCHRONIZATION ====================

    /**
     * Synchronizes all local projects and their associated expenses to Firebase
     * in a single upload operation.
     *
     * **Workflow:**
     * 1. Checks for an active Wi-Fi or Cellular connection via [NetworkUtils].
     * 2. Fetches one-shot snapshots of all projects and expenses from Room.
     * 3. Groups expenses under their parent project using [ProjectSyncModel].
     * 4. Uploads the complete data map to Firebase in a single request.
     *
     * @param context The [Context] used to check network availability.
     * @return [Result.success] if the sync completes, or [Result.failure] with
     *         a descriptive exception on network or upload errors.
     */
    suspend fun syncAllToCloud(context: Context): Result<Unit> = withContext(ioDispatcher) {
        // 1. Verify network connectivity before attempting upload
        if (!NetworkUtils.isNetworkAvailable(context)) {
            return@withContext Result.failure(
                Exception("No internet connection available. Please check your Wi-Fi or mobile data.")
            )
        }

        try {
            // 2. Fetch static snapshots from Room (one-shot, not Flow)
            val projects = dao.getAllProjectsStatic()
            val allExpenses = dao.getAllExpensesStatic()

            // 3. Map each project to a ProjectSyncModel containing its expenses
            val dataToSync = projects.associate { project ->
                project.projectId to ProjectSyncModel(
                    details = project,
                    expenses = allExpenses.filter { it.projectId == project.projectId }
                )
            }

            // 4. Upload the entire data map to Firebase in a single request
            firebaseService.uploadEverything(dataToSync)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== MUTATING OPERATIONS (with auto-sync) ====================

    /**
     * Inserts a project into the local database and triggers a cloud sync.
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
     *
     * @return [Result.success] if both the local update and cloud sync succeed,
     *         or [Result.failure] if the sync fails (local update is still persisted).
     */
    suspend fun updateProject(project: ProjectEntity, context: Context): Result<Unit> {
        dao.updateProject(project)
        return syncAllToCloud(context)
    }

    /**
     * Deletes a project from the local database and triggers a cloud sync.
     *
     * @return [Result.success] if both the local delete and cloud sync succeed,
     *         or [Result.failure] if the sync fails (local delete is still persisted).
     */
    suspend fun deleteProject(project: ProjectEntity, context: Context): Result<Unit> {
        dao.deleteProject(project)
        return syncAllToCloud(context)
    }

    /**
     * Deletes a project by its ID from the local database and triggers a cloud sync.
     *
     * @return [Result.success] if both the local delete and cloud sync succeed,
     *         or [Result.failure] if the sync fails (local delete is still persisted).
     */
    suspend fun deleteProjectById(projectId: String, context: Context): Result<Unit> {
        dao.deleteProjectById(projectId)
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
