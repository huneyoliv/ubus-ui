package com.ubusmobilidade.ubus.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val UbusDarkColorScheme = darkColorScheme(
    primary = UbusAccent,
    onPrimary = UbusForeground,
    primaryContainer = UbusAccentContainer,
    onPrimaryContainer = UbusForeground,
    secondary = UbusPrimary,
    onSecondary = UbusForeground,
    secondaryContainer = UbusCard,
    onSecondaryContainer = UbusForeground,
    tertiary = UbusAccentLight,
    onTertiary = UbusForeground,
    background = UbusBackground,
    onBackground = UbusForeground,
    surface = UbusPrimary,
    onSurface = UbusForeground,
    surfaceVariant = UbusCard,
    onSurfaceVariant = UbusMutedForeground,
    outline = UbusBorder,
    outlineVariant = UbusBorder,
    error = UbusDestructive,
    onError = UbusForeground,
)

// Using default sans-serif (Inter-like system font on most platforms)
// Calistoga used for display text via explicit fontFamily
val UbusDisplayFontFamily = FontFamily.Default // Replaced at platform level if needed
val UbusBodyFontFamily = FontFamily.Default

val UbusTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Black,
        fontSize = 42.sp,
        lineHeight = 48.sp,
        letterSpacing = (-1.5).sp,
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.5).sp,
    ),
    displaySmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp,
    ),
)

@Composable
fun UbusTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = UbusDarkColorScheme,
        typography = UbusTypography,
        content = content,
    )
}
