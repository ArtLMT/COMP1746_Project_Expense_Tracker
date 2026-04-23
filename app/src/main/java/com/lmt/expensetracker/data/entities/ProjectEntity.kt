package com.lmt.expensetracker.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


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
    val specialRequirements: String = "",    // Optional
    val clientDepartmentInfo: String = "",   // Optional
    // ── Sync / Tombstone fields ──────────────────────────────────────────
    val updatedAt: Long = System.currentTimeMillis(), // Epoch ms — used for Last-Write-Wins conflict resolution
    val isDeleted: Boolean = false                    // true = soft-deleted tombstone; excluded from UI queries
)
