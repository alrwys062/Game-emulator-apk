package com.example.ui.theme

import android.os.Build
import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

@OptIn(ExperimentalTextApi::class)
private fun cairoFont(weight: FontWeight, weightValue: Int): Font {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Font(
            resId = R.font.cairo,
            weight = weight,
            variationSettings = FontVariation.Settings(FontVariation.weight(weightValue))
        )
    } else {
        Font(resId = R.font.cairo, weight = weight)
    }
}

val CairoFontFamily = FontFamily(
    cairoFont(FontWeight.Normal, 400),
    cairoFont(FontWeight.SemiBold, 600),
    cairoFont(FontWeight.Bold, 700),
    cairoFont(FontWeight.ExtraBold, 800)
)

val JetBrainsMonoFontFamily = FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Normal),
    Font(R.font.jetbrains_mono, FontWeight.Bold)
)

val RetroTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 26.sp,
        lineHeight = 34.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 30.sp
    ),
    titleLarge = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 18.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp
    ),
    labelLarge = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 15.sp
    ),
    labelSmall = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        lineHeight = 14.sp
    )
)
