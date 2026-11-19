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

enum class AccentPreset(val id: String, val primary: Color, val secondary: Color, val labelAr: String, val labelEn: String) {
    PURPLE_CYAN("purple_cyan", Color(0xFF8B5CF6), Color(0xFF22D3EE), "بنفسجي سماوي", "Purple Cyan"),
    CRIMSON_AMBER("crimson_amber", Color(0xFFF43F5E), Color(0xFFFBBF24), "قرمزي ذهبي", "Crimson Amber"),
    EMERALD_TEAL("emerald_teal", Color(0xFF10B981), Color(0xFF22D3EE), "زمردي فيروزي", "Emerald Teal");

    companion object {
        fun fromId(id: String): AccentPreset = entries.firstOrNull { it.id == id } ?: PURPLE_CYAN
    }
}

val LocalAppStrings = staticCompositionLocalOf<AppStrings> { ArabicStrings }
val LocalIsArabic = staticCompositionLocalOf { true }
val LocalAccentPreset = staticCompositionLocalOf { AccentPreset.PURPLE_CYAN }

@Composable
fun RetroVerseTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    accentPreset: AccentPreset = AccentPreset.PURPLE_CYAN,
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
        primary = accentPreset.primary,
        onPrimary = Color.White,
        secondary = accentPreset.secondary,
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
        LocalIsArabic provides isArabic,
        LocalAccentPreset provides accentPreset
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = RetroTypography,
            content = content
        )
    }
}
