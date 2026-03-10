package com.lmt.expensetracker.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// COLOR PALETTE - TÍCH HỢP TỪ TAILWIND & STATUS COLORS
// ============================================================================
object CustomColors {
    // Primary
    val PrimaryLight = Color(0xFF16A34A)     // Softer green
    val PrimaryDark = Color(0xFF19E65E)      // Neon green

    // Background
    val BackgroundLight = Color(0xFFF6F8F6)
    val BackgroundDark = Color(0xFF112116)

    // Surface
    val SurfaceLight = Color(0xFFFFFFFF)
    val SurfaceDark = Color(0xFF1A3222)

    // Text
    val TextPrimaryLight = Color(0xFF111827)   // gray-900 (Đen chữ chính)
    val TextSecondaryLight = Color(0xFF6B7280) // gray-500 (Xám chữ phụ)
    val TextPrimaryDark = Color(0xFFE5E7EB)    // gray-200 (Trắng chữ chính)
    val TextSecondaryDark = Color(0xFF9CA3AF)  // gray-400 (Xám chữ phụ cho Dark mode)

    // --- GIỮ LẠI CÁC STATUS & BORDER COLORS CHO ỨNG DỤNG ---
    val StatusActive = Color(0xFF19E65E)
    val StatusAtRisk = Color(0xFFEAB308)
    val StatusAtRiskBg = Color(0x33EAB308)
    val StatusNew = Color(0xFF22C55E)
    val StatusNewBg = Color(0x1A22C55E)
    val StatusPending = Color(0xFF6B7280)
    val StatusPendingBg = Color(0x1A6B7280)

    val StatusGreen = Color(0xFF10B981)
    val StatusYellow = Color(0xFFF59E0B)
    val StatusRed = Color(0xFFEF4444)
    val StatusBlue = Color(0xFF3B82F6)

    val BorderDark = Color(0xFF1F2937)
    val ProgressTrack = Color(0xFF1F1F1F)
}