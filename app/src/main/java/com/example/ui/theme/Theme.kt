package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.ui.i18n.AppStrings
import com.example.ui.i18n.ArabicStrings
import com.example.ui.i18n.EnglishStrings

enum class AppThemeMode(val id: String) {
    DARK("dark"),
    OLED("oled"),
    NEON("neon");

    companion object {
        fun fromId(id: String): AppThemeMode = entries.firstOrNull { it.id == id } ?: DARK
    }
}

val LocalAppStrings = staticCompositionLocalOf<AppStrings> { ArabicStrings }
val LocalIsArabic = staticCompositionLocalOf { true }

@Composable
fun RetroVerseTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    isArabic: Boolean = true,
    content: @Composable () -> Unit
) {
    val bgColor = when (themeMode) {
        AppThemeMode.DARK -> BackgroundDark
        AppThemeMode.OLED -> BackgroundOled
        AppThemeMode.NEON -> BackgroundNeon
    }
    val surfaceColor = when (themeMode) {
        AppThemeMode.DARK -> SurfaceDark
        AppThemeMode.OLED -> Color(0xFF0A0B10)
        AppThemeMode.NEON -> Color(0xFF120D24)
    }
    val surfaceVariantColor = when (themeMode) {
        AppThemeMode.DARK -> SurfaceElevated
        AppThemeMode.OLED -> Color(0xFF141620)
        AppThemeMode.NEON -> Color(0xFF1C1435)
    }

    val colorScheme = darkColorScheme(
        primary = PrimaryPurple,
        onPrimary = Color.White,
        secondary = SecondaryCyan,
        onSecondary = Color(0xFF05131A),
        tertiary = SuccessGreen,
        background = bgColor,
        onBackground = TextPrimary,
        surface = surfaceColor,
        onSurface = TextPrimary,
        surfaceVariant = surfaceVariantColor,
        onSurfaceVariant = TextSecondary,
        outline = BorderSubtle,
        error = ErrorRose
    )

    val layoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr
    val strings = if (isArabic) ArabicStrings else EnglishStrings

    CompositionLocalProvider(
        LocalLayoutDirection provides layoutDirection,
        LocalAppStrings provides strings,
        LocalIsArabic provides isArabic
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = RetroTypography,
            content = content
        )
    }
}
