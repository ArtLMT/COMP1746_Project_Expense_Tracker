package com.lmt.expensetracker.data.repository

import com.lmt.expensetracker.data.dao.AppDao
import com.lmt.expensetracker.data.entities.ProjectEntity
import kotlinx.coroutines.flow.Flow

class ProjectRepository(private val dao: AppDao) {

    // Insert a new project
    suspend fun insertProject(project: ProjectEntity) {
        dao.insertProject(project)
    }

    // Update existing project
    suspend fun updateProject(project: ProjectEntity) {
        dao.updateProject(project)
    }

    // Delete project
    suspend fun deleteProject(project: ProjectEntity) {
        dao.deleteProject(project)
    }

    // Delete project by ID
    suspend fun deleteProjectById(projectId: String) {
        dao.deleteProjectById(projectId)
    }

    // Get all projects
    fun getAllProjects(): Flow<List<ProjectEntity>> {
        return dao.getAllProjects()
    }

    // Get project by ID
    fun getProjectById(projectId: String): Flow<ProjectEntity?> {
        return dao.getProjectById(projectId)
    }

    // Get project count
    fun getProjectCount(): Flow<Int> {
        return dao.getProjectCount()
    }

    // Search projects by name or description (real-time)
    fun searchProjects(searchQuery: String): Flow<List<ProjectEntity>> {
        return if (searchQuery.isEmpty()) {
            getAllProjects()
        } else {
            dao.searchProjects(searchQuery)
        }
    }

    // Filter projects by status
    fun getProjectsByStatus(status: String): Flow<List<ProjectEntity>> {
        return dao.getProjectsByStatus(status)
    }

    // Filter projects by manager
    fun getProjectsByManager(manager: String): Flow<List<ProjectEntity>> {
        return dao.getProjectsByManager(manager)
    }

    // ==================== PROJECT WITH SPENT AGGREGATION ====================

    // Get all projects with exact spent amounts
    fun getAllProjectsWithSpent(): Flow<List<com.lmt.expensetracker.data.entities.ProjectWithSpent>> {
        return dao.getAllProjectsWithSpent()
    }

    // Search projects with spent amounts
    fun searchProjectsWithSpent(searchQuery: String): Flow<List<com.lmt.expensetracker.data.entities.ProjectWithSpent>> {
        return if (searchQuery.isEmpty()) {
            getAllProjectsWithSpent()
        } else {
            dao.searchProjectsWithSpent(searchQuery)
        }
    }

    // Filter projects by date range
    fun getProjectsByDateRange(startDate: String, endDate: String): Flow<List<ProjectEntity>> {
        return dao.getProjectsByDateRange(startDate, endDate)
    }

    // Get total budget
    fun getTotalBudget(): Flow<Double?> {
        return dao.getTotalBudget()
    }
}
