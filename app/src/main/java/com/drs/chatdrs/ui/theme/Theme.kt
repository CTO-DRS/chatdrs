package com.drs.chatdrs.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val ChatDrsDarkColorScheme = darkColorScheme(
    primary = PurplePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2E275C),
    onPrimaryContainer = Color(0xFFD6D0FF),
    secondary = PurpleGradientEnd,
    onSecondary = Color.White,
    background = BackgroundDark,
    onBackground = TextOtherDark,
    surface = SurfaceDark,
    onSurface = TextOtherDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextMutedDark,
    outline = DividerDark,
    error = ErrorRed,
    onError = Color.White
)

private val ChatDrsLightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFECE9FE),
    onPrimaryContainer = PurplePrimary,
    secondary = PurpleGradientEnd,
    onSecondary = Color.White,
    background = BackgroundLight,
    onBackground = TextOtherLight,
    surface = SurfaceLight,
    onSurface = TextOtherLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextMutedLight,
    outline = DividerLight,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun ChatDrsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> ChatDrsDarkColorScheme
        else -> ChatDrsLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
