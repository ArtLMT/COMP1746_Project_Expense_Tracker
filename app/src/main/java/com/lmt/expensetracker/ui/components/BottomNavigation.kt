package com.lmt.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lmt.expensetracker.navigation.Routes
import com.lmt.expensetracker.ui.theme.CustomColors

// ============================================================================
// CUSTOM BOTTOM NAVIGATION
// ============================================================================
@Composable
fun CustomBottomNavigation(
    currentRoute: String?,
    onHomeClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(80.dp)
//            .background(CustomColors.BackgroundDark)
//    ) {
        BottomAppBar(
            modifier = Modifier
                .fillMaxWidth()
//                .height(80.dp)
//                .align(Alignment.BottomCenter)
                .drawBehind {
                    val strokeWidth = 1.dp.toPx()
                    val y = 0f
                    drawLine(
                        color = Color(0xFF244730),
                        start = androidx.compose.ui.geometry.Offset(0f, y),
                        end = androidx.compose.ui.geometry.Offset(size.width, y),
                        strokeWidth = strokeWidth
                    )
                },
            containerColor = Color(0xFF1A3222),
            contentPadding = PaddingValues(0.dp),
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
                // XÓA: horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Phần 1: Home Button (Chiếm 1/3)
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    NavigationItem(
                        icon = Icons.Outlined.Home,
                        label = "Home",
                        isSelected = currentRoute == Routes.DASHBOARD,
                        onClick = onHomeClick
                    )
                }

                // Phần 2: Khoảng trống cho FAB ở giữa (Chiếm 1/3)
                // Khoảng trống này để "nhường chỗ" cho cái nút tròn màu xanh nổi lên từ Scaffold
                Spacer(modifier = Modifier.weight(1f))

                // Phần 3: Settings Button (Chiếm 1/3)
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    NavigationItem(
                        icon = Icons.Outlined.Settings,
                        label = "Setting",
                        isSelected = currentRoute == Routes.SETTING,
                        onClick = onSettingsClick
                    )
                }
            }
        }
//    }
}

@Composable
private fun NavigationItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 8.dp)
            .width(64.dp), // w-16
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(28.dp),
            tint = if (isSelected) CustomColors.Primary else Color(0xFF93C8A5) // #93c8a5
        )
        Text(
            text = label.uppercase(),
            fontSize = 16.sp, // text-[10px]
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp, // tracking-tighter
//            color = if (isSelected) CustomColors.Primary else Color
            color = CustomColors.White
        )
    }
}
