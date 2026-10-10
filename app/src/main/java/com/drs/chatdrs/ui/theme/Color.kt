package com.drs.chatdrs.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ChatDrs Brand Identity Colors
val PurplePrimary = Color(0xFF6C5CE7)
val PurpleGradientStart = Color(0xFF6C5CE7)
val PurpleGradientEnd = Color(0xFF8E7CF8)

// Bubble Colors
val BubbleOtherLight = Color(0xFFF1F2F6)
val TextOtherLight = Color(0xFF2D3436)

val BubbleOtherDark = Color(0xFF262B3E)
val TextOtherDark = Color(0xFFEAEAEA)

// Background & Surfaces
val BackgroundLight = Color(0xFFF8F9FD)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFF1F2F8)

val BackgroundDark = Color(0xFF0F1320)
val SurfaceDark = Color(0xFF161B2E)
val SurfaceVariantDark = Color(0xFF1F253C)

// Accents & Indicators
val OnlineGreen = Color(0xFF00CEC9)
val ErrorRed = Color(0xFFFF6B6B)
val TextMutedLight = Color(0xFF8395A7)
val TextMutedDark = Color(0xFF7F8C9F)
val DividerLight = Color(0xFFEAECEF)
val DividerDark = Color(0xFF22283E)

// Gradients
val ChatDrsGradient = Brush.linearGradient(
    colors = listOf(PurpleGradientStart, PurpleGradientEnd)
)

val StoryGradient = Brush.sweepGradient(
    colors = listOf(PurpleGradientStart, Color(0xFFA29BFE), PurpleGradientEnd, PurpleGradientStart)
)
