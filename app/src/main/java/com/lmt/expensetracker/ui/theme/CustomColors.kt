package com.lmt.expensetracker.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// COLOR PALETTE - TÍCH HỢP TỪ TAILWIND & STATUS COLORS
// ============================================================================
object CustomColors {
    // Brand Colors
    val Primary = Color(0xFF19E65E)          // Neon Green
    val PrimaryDark = Color(0xFF12B84A)      // Darker Green for pressed states

    // Background & Surface Colors
    val BackgroundLight = Color(0xFFF6F8F6)  // Mới thêm từ file cũ
    val BackgroundDark = Color(0xFF112116)   // Dark background
    val SurfaceLight = Color(0xFFFFFFFF)
    val SurfaceDark = Color(0xFF1A3222)      // Card/Surface dark

    // Text & Neutral Colors
    val MutedText = Color(0xFF999999)        // Muted gray text
    val White = Color(0xFFFFFFFF)
    val Black = Color(0xFF000000)

    // Status Colors (Project & General)
    val StatusActive = Color(0xFF19E65E)
    val StatusAtRisk = Color(0xFFEAB308)     // Yellow-500
    val StatusAtRiskBg = Color(0x33EAB308)   // Yellow-500/20
    val StatusNew = Color(0xFF22C55E)        // Green-500
    val StatusNewBg = Color(0x1A22C55E)      // Green-500/10
    val StatusPending = Color(0xFF6B7280)    // Gray-500
    val StatusPendingBg = Color(0x1A6B7280)  // Gray-500/10

    val StatusGreen = Color(0xFF10B981)
    val StatusYellow = Color(0xFFF59E0B)
    val StatusRed = Color(0xFFEF4444)
    val StatusBlue = Color(0xFF3B82F6)

    val BorderDark = Color(0xFF1F2937)       // border-gray-800
    val TextMuted = Color(0xFF9CA3AF)        // text-gray-400 (Dùng chung cho icon và subtext)
    val TextHighlight = Color(0xFFE5E7EB)    // text-slate-200 (Dùng cho số tiền nổi bật)
    val ProgressTrack = Color(0xFF1F1F1F)    // Màu nền tối của thanh tiến độ
}