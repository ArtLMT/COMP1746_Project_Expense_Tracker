package com.lmt.expensetracker.data.entities

import androidx.room.Embedded
import androidx.room.Relation

data class ProjectWithSpent(
    @Embedded val project: ProjectEntity,
    val spentAmount: Double
)
