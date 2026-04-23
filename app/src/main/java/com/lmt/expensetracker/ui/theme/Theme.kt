package com.lmt.expensetracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CustomColors.PrimaryDark,      // Neon Green
    primaryContainer = CustomColors.SurfaceDark,
    background = CustomColors.BackgroundDark,
    surface = CustomColors.SurfaceDark,
    onPrimary = Color.Black,
    onBackground = CustomColors.TextPrimaryDark,
    onSurface = CustomColors.TextPrimaryDark,
    surfaceVariant = CustomColors.SurfaceDark
)

private val LightColorScheme = lightColorScheme(
    primary = CustomColors.PrimaryLight,     // Softer Green
    primaryContainer = CustomColors.SurfaceLight,
    background = CustomColors.BackgroundLight,
    surface = CustomColors.SurfaceLight,
    onPrimary = Color.White,
    onBackground = CustomColors.TextPrimaryLight,
    onSurface = CustomColors.TextPrimaryLight,
    surfaceVariant = Color(0xFFE8F5E9)
)

@Composable
fun ExpenseTrackerTheme(
    darkTheme: Boolean = true, // track if user click on dark theme, imported by ViewModel.isDarkTheme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}