package com.lmt.expensetracker.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey
    val projectId: String,
    val name: String,
    val description: String,
    val startDate: String, // LocalDate in ISO format (YYYY-MM-DD)
    val endDate: String,   // LocalDate in ISO format (YYYY-MM-DD)
    val manager: String,   // Manager name/ID
    val status: String,    // "Active", "Completed", "On Hold"
    val budget: Double,    // Budget amount
    val specialRequirements: String = "", // Optional
    val clientDepartmentInfo: String = "" // Optional
)
