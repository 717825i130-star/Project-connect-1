package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Base Brand Colors
val PrimaryBlue = Color(0xFF1E40AF)         // Deep Royal Blue
val PrimaryBlueVariant = Color(0xFF1E3A8A)  // Darker Navy Blue
val PrimaryBlueLight = Color(0xFF3B82F6)    // Bright Accent Blue
val PrimaryBlueContainer = Color(0xFFDBEAFE)// Soft Ice Blue Tint

val SecondaryGreen = Color(0xFF059669)       // Emerald Green
val SecondaryGreenVariant = Color(0xFF047857)// Deeper Forest Emerald
val SecondaryGreenLight = Color(0xFF10B981)  // Vibrant Mint Green
val SecondaryGreenContainer = Color(0xFFD1FAE5) // Soft Mint Tint

// -------------------------------------------------------------
// STUDENT THEME: LUXURY MARBLE GREEN WITH MIXED BLUE ACCENTS
// -------------------------------------------------------------
val StudentMarbleDark = Color(0xFF064E3B)       // Deep Jade Green
val StudentMarblePrimary = Color(0xFF047857)    // Rich Malachite Emerald
val StudentMarbleTeal = Color(0xFF0D9488)       // Oceanic Marble Teal
val StudentMarbleMint = Color(0xFF10B981)       // Bright Mint Vein
val StudentMarbleVein = Color(0xFFA7F3D0)       // Pale Mint Marble Vein
val StudentMarbleBg = Color(0xFFF2FAF5)         // Soft Marble Green Tinted Background
val StudentMarbleSurface = Color(0xFFFFFFFF)    // Crisp White

val StudentBlueAccent = Color(0xFF2563EB)       // Vibrant Mixed Blue Accent
val StudentBlueHighlight = Color(0xFF38BDF8)    // Sky Mixed Blue Highlight
val StudentBlueContainer = Color(0xFFE0F2FE)    // Mixed Blue Soft Tint

// Student Marble Gradient Brushes
val StudentMarbleHeroBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF047857), // Malachite Green
        Color(0xFF065F46), // Deep Forest
        Color(0xFF0D9488), // Marble Teal
        Color(0xFF1D4ED8)  // Mixed Royal Blue Accent Swirl
    )
)

val StudentMarbleCardBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFFECFDF5),
        Color(0xFFF0FDF4),
        Color(0xFFEFF6FF)  // Soft mixed blue reflection
    )
)

val StudentMarbleAccentBrush = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF059669),
        Color(0xFF2563EB)
    )
)

// -------------------------------------------------------------
// STAFF THEME: LUXURY MARBLE BLUE WITH MIXED GREEN ACCENTS
// -------------------------------------------------------------
val StaffMarbleDark = Color(0xFF0F172A)         // Deep Obsidian Blue
val StaffMarblePrimary = Color(0xFF1E3A8A)      // Royal Lapis Blue
val StaffMarbleOcean = Color(0xFF0369A1)        // Mediterranean Blue
val StaffMarbleSky = Color(0xFF38BDF8)          // Azure Marble Vein
val StaffMarbleVein = Color(0xFFBAE6FD)         // Ice Blue Vein
val StaffMarbleBg = Color(0xFFF0F7FF)           // Soft Marble Blue Tinted Background
val StaffMarbleSurface = Color(0xFFFFFFFF)      // Pure White

val StaffGreenAccent = Color(0xFF059669)        // Vibrant Mixed Emerald Accent
val StaffGreenHighlight = Color(0xFF10B981)     // Mint Green Highlight
val StaffGreenContainer = Color(0xFFD1FAE5)     // Mixed Green Soft Tint

// Staff Marble Gradient Brushes
val StaffMarbleHeroBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF1E3A8A), // Deep Lapis Blue
        Color(0xFF1E40AF), // Royal Blue
        Color(0xFF0284C7), // Ocean Blue
        Color(0xFF059669)  // Mixed Emerald Green Accent Swirl
    )
)

val StaffMarbleCardBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFFEFF6FF),
        Color(0xFFF0F9FF),
        Color(0xFFECFDF5)  // Soft mixed green reflection
    )
)

val StaffMarbleAccentBrush = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF1E40AF),
        Color(0xFF059669)
    )
)

// Status & Indicators
val StatusAvailable = Color(0xFF2563EB)
val StatusPending = Color(0xFFD97706)
val StatusApproved = Color(0xFF059669)
val StatusProposalNeeded = Color(0xFF7C3AED)
val StatusInProgress = Color(0xFF0284C7)
val StatusNeedsRevision = Color(0xFFDC2626)
val StatusCompleted = Color(0xFF059669)

// Neutrals
val BackgroundLight = Color(0xFFF8FAFC)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFF1F5F9)
val TextPrimary = Color(0xFF0F172A)
val TextSecondary = Color(0xFF475569)
val TextTertiary = Color(0xFF64748B)
val BorderStroke = Color(0xFFCBD5E1)
