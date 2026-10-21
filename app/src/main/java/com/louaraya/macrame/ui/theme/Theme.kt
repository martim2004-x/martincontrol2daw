package com.louaraya.macrame.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Paleta "Verde Salvia y Ceniza": natural, sobria y elegante. */
object MacrameColors {
    // Colores base definidos para el proyecto
    val Ash = Color(0xFFF4F6F4)            // Fondo de la app
    val Card = Color(0xFFFFFFFF)           // Fondo de tarjetas
    val Forest = Color(0xFF2D4030)         // Color principal / botón activo
    val Ink = Color(0xFF1F2923)            // Texto principal
    val Muted = Color(0xFF697A70)          // Texto secundario
    val Inactive = Color(0xFFE8ECE9)       // Botones inactivos
    val InactiveText = Color(0xFF4F5E54)   // Texto de botones inactivos

    // Apoyo derivado de la misma familia
    val Outline = Color(0xFFD3DBD5)
    val Sage = Color(0xFF7F9885)
    val SageTint = Color(0xFFDDE6DF)

    // Estados
    val Error = Color(0xFFB3472F)
    val ErrorTint = Color(0xFFF7E4DF)
    val Success = Color(0xFF3E7A52)
    val SuccessTint = Color(0xFFDDEEE2)
    val Warning = Color(0xFF8F6317)
    val WarningTint = Color(0xFFF6EBD3)
}

private val MacrameColorScheme = lightColorScheme(
    primary = MacrameColors.Forest,
    onPrimary = Color.White,
    primaryContainer = MacrameColors.SageTint,
    onPrimaryContainer = MacrameColors.Forest,
    secondary = MacrameColors.Sage,
    onSecondary = Color.White,
    secondaryContainer = MacrameColors.Inactive,
    onSecondaryContainer = MacrameColors.InactiveText,
    tertiary = MacrameColors.Sage,
    background = MacrameColors.Ash,
    onBackground = MacrameColors.Ink,
    surface = MacrameColors.Card,
    onSurface = MacrameColors.Ink,
    surfaceVariant = MacrameColors.Inactive,
    onSurfaceVariant = MacrameColors.Muted,
    surfaceTint = Color.Transparent,
    outline = MacrameColors.Outline,
    outlineVariant = MacrameColors.Outline,
    error = MacrameColors.Error,
    onError = Color.White,
    errorContainer = MacrameColors.ErrorTint,
    onErrorContainer = MacrameColors.Error,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = MacrameColors.Inactive
)

private val Serif = FontFamily.Serif

private val MacrameTypography = Typography(
    displaySmall = TextStyle(fontFamily = Serif, fontWeight = FontWeight.SemiBold, fontSize = 34.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = Serif, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 34.sp),
    headlineSmall = TextStyle(fontFamily = Serif, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontFamily = Serif, fontWeight = FontWeight.SemiBold, fontSize = 21.sp, lineHeight = 27.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 17.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 15.sp)
)

private val MacrameShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun MacrameTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MacrameColorScheme,
        typography = MacrameTypography,
        shapes = MacrameShapes,
        content = content
    )
}
