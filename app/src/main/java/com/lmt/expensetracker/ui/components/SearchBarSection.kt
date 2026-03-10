package com.lmt.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lmt.expensetracker.ui.theme.CustomColors

// ============================================================================
// SEARCH BAR SECTION
// ============================================================================
@Composable
fun SearchBarSection(
    searchQuery: String,
    onSearchChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(
                    color = CustomColors.SurfaceDark, // #1a3222
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Search",
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 8.dp),
                tint = Color(0xFF6B7280) // gray-500
            )

            BasicTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .weight(1f),
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = CustomColors.White
                ),
                singleLine = true,
                cursorBrush = SolidColor(CustomColors.Primary),
                decorationBox = { innerTextField ->
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search projects...",
                            fontSize = 16.sp,
                            color = Color(0xFF9CA3AF) // gray-400
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}
