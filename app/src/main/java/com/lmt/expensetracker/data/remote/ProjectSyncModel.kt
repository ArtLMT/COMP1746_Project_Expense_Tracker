package com.lmt.expensetracker.data.remote

import com.lmt.expensetracker.data.entities.ExpenseEntity
import com.lmt.expensetracker.data.entities.ProjectEntity


// Thằng này đóng vai trò như DTO
data class ProjectSyncModel (
    val details: ProjectEntity? = null,
    val expenses: List<ExpenseEntity> = emptyList()
)