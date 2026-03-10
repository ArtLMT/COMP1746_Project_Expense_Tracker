package com.lmt.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lmt.expensetracker.ui.theme.CustomColors
import com.lmt.expensetracker.viewmodel.StatusCounts

// ============================================================================
// HEADER SECTION
// ============================================================================
@Composable
fun HeaderSection(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    statusCounts: StatusCounts
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        // Greeting & Notification
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Good morning, Alex",
                    fontSize = 14.sp,
                    color = Color(0xFF9CA3AF),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Dashboard",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                    color = CustomColors.White
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = Color(0xFF1E293B), // Match Tailwind gray-800
                        shape = RoundedCornerShape(50.dp)
                    )
                    .clickable { }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFFD1D5DB) // Match Tailwind gray-300
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color(0x0DFFFFFF), // Match white/5
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            listOf(
                "All" to (statusCounts.active + statusCounts.completed + statusCounts.onHold),
                "Active" to statusCounts.active,
                "Completed" to statusCounts.completed,
                "On Hold" to statusCounts.onHold
            ).forEachIndexed { index, (label, count) ->
                StatusTab(
                    label = label,
                    count = count,
                    isSelected = selectedTab == index,
                    modifier = Modifier.weight(1f),
                    onClick = { onTabSelected(index) }
                )
            }
        }
    }
}

@Composable
fun StatusTab(
    label: String,
    count: Int,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                color = if (isSelected) CustomColors.Primary else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label.uppercase(),
            fontSize = 12.sp, // Match text-xs
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp, // tracking-wider
            color = if (isSelected) CustomColors.Black else Color(0xFF9CA3AF) // gray-400
        )
        Text(
            text = count.toString(),
            fontSize = 18.sp, // Match text-lg
            fontWeight = FontWeight.Bold,
            color = if (isSelected) CustomColors.Black else CustomColors.White
        )
    }
}
