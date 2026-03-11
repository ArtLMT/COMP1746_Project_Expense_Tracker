package com.lmt.expensetracker.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lmt.expensetracker.ui.theme.BudgetStatus

// ============================================================================
// BUDGET COMPARISON CARD
// ============================================================================
@SuppressLint("DefaultLocale")
@Composable
fun BudgetComparisonCard(
    totalExpenses: Double,
    projectBudget: Double?,
    modifier: Modifier = Modifier
) {
    // Compute budget status
    val fraction = if (projectBudget != null && projectBudget > 0) {
        (totalExpenses / projectBudget).toFloat()
    } else {
        0f
    }

    val budgetStatus = when {
        projectBudget == null || projectBudget <= 0 -> null
        fraction >= 1.0f -> BudgetStatus.OVER_BUDGET
        fraction >= 0.8f -> BudgetStatus.AT_RISK
        else -> BudgetStatus.ON_TRACK
    }

    val amountColor = when (budgetStatus) {
        BudgetStatus.OVER_BUDGET -> MaterialTheme.colorScheme.error
        BudgetStatus.AT_RISK -> BudgetStatus.AT_RISK.textColor
        else -> MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row: labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total Expenses",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            if (projectBudget != null && projectBudget > 0) {
                Text(
                    text = "Budget",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        // Row: amounts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$${String.format("%.2f", totalExpenses)}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
            if (projectBudget != null && projectBudget > 0) {
                Text(
                    text = "$${String.format("%.2f", projectBudget)}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Progress bar (only when budget is available)
        if (projectBudget != null && projectBudget > 0 && budgetStatus != null) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction.coerceAtMost(1f))
                            .clip(CircleShape)
                            .background(budgetStatus.progressColor)
                    )
                }

                // Status label
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = budgetStatus.backgroundColor,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = budgetStatus.displayName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = budgetStatus.textColor
                        )
                    }
                    Text(
                        text = "${String.format("%.0f", fraction * 100)}% used",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
