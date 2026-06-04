package com.ubusmobilidade.ubus.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import ubus.composeapp.generated.resources.Res
import ubus.composeapp.generated.resources.inter_regular
import ubus.composeapp.generated.resources.inter_medium
import ubus.composeapp.generated.resources.inter_semibold
import ubus.composeapp.generated.resources.inter_bold
import ubus.composeapp.generated.resources.outfit_semibold
import ubus.composeapp.generated.resources.outfit_bold
import ubus.composeapp.generated.resources.outfit_extrabold
import ubus.composeapp.generated.resources.outfit_black

// ── Light color scheme matching ubus-front ──
private val UbusLightColorScheme = lightColorScheme(
    primary = UbusPrimary,
    onPrimary = UbusOnPrimary,
    primaryContainer = UbusPrimaryContainer,
    onPrimaryContainer = UbusText,
    secondary = UbusSecondary,
    onSecondary = UbusOnPrimary,
    secondaryContainer = UbusPrimaryContainer,
    onSecondaryContainer = UbusText,
    tertiary = UbusPrimaryLight,
    onTertiary = UbusOnPrimary,
    background = UbusBackground,
    onBackground = UbusText,
    surface = UbusSurface,
    onSurface = UbusText,
    surfaceVariant = UbusSurface2,
    onSurfaceVariant = UbusText2,
    outline = UbusBorder,
    outlineVariant = UbusBorder,
    error = UbusDestructive,
    onError = UbusOnPrimary,
)

// ── Shapes matching ubus-front border-radius tokens ──
val UbusShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),   // --radius-sm
    small      = RoundedCornerShape(12.dp),  // --radius-md
    medium     = RoundedCornerShape(18.dp),  // --radius-lg
    large      = RoundedCornerShape(24.dp),  // --radius-xl
    extraLarge = RoundedCornerShape(32.dp),  // --radius-2xl
)

// ── Font families (loaded from composeResources/font/) ──
@Composable
fun InterFontFamily() = FontFamily(
    Font(Res.font.inter_regular, FontWeight.Normal),
    Font(Res.font.inter_medium, FontWeight.Medium),
    Font(Res.font.inter_semibold, FontWeight.SemiBold),
    Font(Res.font.inter_bold, FontWeight.Bold),
)

@Composable
fun OutfitFontFamily() = FontFamily(
    Font(Res.font.outfit_semibold, FontWeight.SemiBold),
    Font(Res.font.outfit_bold, FontWeight.Bold),
    Font(Res.font.outfit_extrabold, FontWeight.ExtraBold),
    Font(Res.font.outfit_black, FontWeight.Black),
)

// Placeholder families used by UbusTypography definition (overridden at runtime)
val UbusDisplayFontFamily = FontFamily.Default
val UbusBodyFontFamily = FontFamily.Default

@Composable
private fun ubusTypography(): Typography {
    val display = OutfitFontFamily()
    val body = InterFontFamily()
    return Typography(
        displayLarge = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.Black,
            fontSize = 42.sp,
            lineHeight = 48.sp,
            letterSpacing = (-1.5).sp,
        ),
        displayMedium = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 38.sp,
            letterSpacing = (-0.5).sp,
        ),
        displaySmall = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            lineHeight = 34.sp,
        ),
        headlineLarge = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 30.sp,
        ),
        headlineMedium = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            lineHeight = 26.sp,
        ),
        headlineSmall = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 24.sp,
        ),
        titleLarge = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 24.sp,
        ),
        titleMedium = TextStyle(
            fontFamily = body,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 22.sp,
        ),
        titleSmall = TextStyle(
            fontFamily = body,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        ),
        bodyLarge = TextStyle(
            fontFamily = body,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = body,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        ),
        bodySmall = TextStyle(
            fontFamily = body,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
        ),
        labelLarge = TextStyle(
            fontFamily = body,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        ),
        labelMedium = TextStyle(
            fontFamily = body,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp,
        ),
        labelSmall = TextStyle(
            fontFamily = body,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            letterSpacing = 0.5.sp,
        ),
    )
}

@Composable
fun UbusTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = UbusLightColorScheme,
        typography = ubusTypography(),
        shapes = UbusShapes,
        content = content,
    )
}
