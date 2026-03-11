package com.lmt.expensetracker.ui.theme

import androidx.compose.ui.graphics.Color


enum class BudgetStatus(
    val displayName: String,
    val textColor: Color,
    val backgroundColor: Color,
    val progressColor: Color
) {

    ON_TRACK(
        "On Track",
        Color(0xFF10B981),
        Color(0x1A10B981),
        Color(0xFF10B981)
    ),

    AT_RISK(
        "At Risk",
        Color(0xFFF59E0B),
        Color(0x1AF59E0B),
        Color(0xFFF59E0B)
    ),

    OVER_BUDGET(
        "Over Budget",
        Color(0xFFEF4444),
        Color(0x1AEF4444),
        Color(0xFFEF4444)
    )
}
