package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val BackgroundDark = Color(0xFF07080D)
val BackgroundOled = Color(0xFF000000)
val BackgroundNeon = Color(0xFF0A0618)

val SurfaceDark = Color(0xFF10121A)
val SurfaceElevated = Color(0xFF171A25)
val SurfaceGlass = Color(0xE6121521)

val PrimaryPurple = Color(0xFF8B5CF6)
val SecondaryCyan = Color(0xFF22D3EE)
val SuccessGreen = Color(0xFF34D399)
val WarningAmber = Color(0xFFFBBF24)
val ErrorRose = Color(0xFFF43F5E)

val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

val BorderSubtle = Color(0x14FFFFFF)
val BorderGlass = Color(0x1FFFFFFF)

val RetroPrimaryGradient = Brush.horizontalGradient(
    colors = listOf(PrimaryPurple, SecondaryCyan)
)

val RetroVerticalGradient = Brush.verticalGradient(
    colors = listOf(PrimaryPurple, SecondaryCyan)
)

val RetroDiagonalGradient = Brush.linearGradient(
    colors = listOf(PrimaryPurple, SecondaryCyan)
)
