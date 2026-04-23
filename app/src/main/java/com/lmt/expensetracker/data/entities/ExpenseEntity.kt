package com.lmt.expensetracker.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["projectId"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("projectId"),
        Index("date"),
        Index("status")
    ]
)
data class ExpenseEntity(
    @PrimaryKey
    val expenseId: String,
    val projectId: String, // Foreign key
    val date: String, // ISO format (YYYY-MM-DD)
    val amount: Double,
    val currency: String = "USD",
    val type: String, // "Travel", "Equipment", "Materials", "Services", "Software/Licenses", "Labour costs", "Utilities", "Miscellaneous"
    val paymentMethod: String, // "Cash", "Credit Card", "Bank Transfer", "Cheque"
    val claimant: String, // Person claiming the expense
    val status: String = "Pending", // "Paid", "Pending", "Reimbursed"
    val description: String = "",    // Optional
    val location: String = "",          // Optional
    // ── Sync / Tombstone fields ──────────────────────────────────────────
    val updatedAt: Long = System.currentTimeMillis(), // Epoch ms — Last-Write-Wins conflict resolution
    val isDeleted: Boolean = false                    // true = soft-deleted tombstone; excluded from UI
)
