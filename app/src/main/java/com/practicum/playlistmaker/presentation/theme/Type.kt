package com.practicum.playlistmaker.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.practicum.playlistmaker.R

private val YsDisplayRegular = FontFamily(Font(R.font.ys_display_regular, FontWeight.Normal))
private val YsDisplayMedium = FontFamily(Font(R.font.ys_display_medium, FontWeight.Medium))

/** Размеры и начертания как в ресурсных стилях (TitleStyle, TextNoTitle и т.д.). */
val PlaylistTypography = Typography(
    titleLarge = TextStyle(
        fontFamily = YsDisplayMedium,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 26.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = YsDisplayMedium,
        fontWeight = FontWeight.Medium,
        fontSize = 19.sp,
        lineHeight = 24.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = YsDisplayRegular,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = YsDisplayRegular,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 18.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = YsDisplayMedium,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp,
    ),
)
