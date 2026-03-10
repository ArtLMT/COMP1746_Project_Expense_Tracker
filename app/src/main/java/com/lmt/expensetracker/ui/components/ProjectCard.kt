package com.lmt.expensetracker.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lmt.expensetracker.data.entities.ProjectWithSpent
import com.lmt.expensetracker.ui.theme.CustomColors

@Composable
fun ProjectCard(
    projectWithSpent: ProjectWithSpent,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCardClick: () -> Unit
) {
    val project = projectWithSpent.project
    val statusColor = when (project.status) {
        "On Track" -> CustomColors.StatusActive
        "At Risk" -> CustomColors.StatusAtRisk
        "New" -> CustomColors.StatusNew
        "Active" -> CustomColors.StatusActive
        "Pending" -> CustomColors.StatusPending
        else -> CustomColors.StatusPending
    }

    val statusBgColor = when (project.status) {
        "On Track" -> CustomColors.StatusActive
        "At Risk" -> CustomColors.StatusAtRiskBg
        "New" -> CustomColors.StatusNewBg
        "Active" -> CustomColors.StatusActive
        "Pending" -> CustomColors.StatusPendingBg
        else -> CustomColors.StatusPendingBg
    }

    val statusTextColor = when (project.status) {
        "On Track" -> CustomColors.Black
        "Active" -> CustomColors.Black
        else -> statusColor
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.98f else 1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .background(
                color = CustomColors.SurfaceDark,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = CustomColors.BorderDark,
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current
            ) { onCardClick() }
    ) {
        // Card Content
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Title & Status Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = project.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = CustomColors.White,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                Box(
                    modifier = Modifier
                        .background(
                            color = statusBgColor,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .then(
                            if (project.status == "At Risk") {
                                Modifier.border(1.dp, CustomColors.StatusAtRiskBg, RoundedCornerShape(4.dp))
                            } else Modifier
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = project.status.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusTextColor
                    )
                }
            }

            // Project Details Grid (2 columns)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Manager
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "Manager",
                            modifier = Modifier.size(16.dp),
                            tint = CustomColors.TextMuted
                        )
                        Text(
                            text = project.manager,
                            fontSize = 13.sp,
                            color = CustomColors.TextMuted
                        )
                    }

                    // Budget
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Payments,
                            contentDescription = "Budget",
                            modifier = Modifier.size(16.dp),
                            tint = CustomColors.TextMuted
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$${project.budget}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CustomColors.TextHighlight
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Date Range
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = "Date",
                            modifier = Modifier.size(16.dp),
                            tint = CustomColors.TextMuted
                        )
                        Text(
                            text = "${project.startDate} - ${project.endDate}",
                            fontSize = 12.sp,
                            color = CustomColors.TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(CustomColors.ProgressTrack)
            ) {
                val progressFraction = if (project.budget > 0) {
                    (projectWithSpent.spentAmount / project.budget).coerceIn(0.0, 1.0).toFloat()
                } else {
                    0f
                }

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progressFraction)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(CustomColors.Primary)
                )
            }
        }
    }
}