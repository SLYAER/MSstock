package com.example.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = CyanPrimaryDark,
    onPrimary = Color(0xFF00344F),
    primaryContainer = CyanContainerDark,
    onPrimaryContainer = Color(0xFFCBE6FF),
    secondary = AmberSecondaryDark,
    onSecondary = Color(0xFF452B00),
    secondaryContainer = AmberContainerDark,
    onSecondaryContainer = Color(0xFFFFDDB3),
    tertiary = IndigoTertiaryDark,
    background = TechDarkBackground,
    surface = TechDarkSurface,
    surfaceVariant = TechDarkSurfaceVariant,
    onBackground = TechDarkOnSurface,
    onSurface = TechDarkOnSurface
)

private val LightColorScheme = lightColorScheme(
    primary = CyanPrimary,
    onPrimary = Color.White,
    primaryContainer = CyanContainer,
    onPrimaryContainer = Color(0xFF001F30),
    secondary = AmberSecondary,
    onSecondary = Color.White,
    secondaryContainer = AmberContainer,
    onSecondaryContainer = Color(0xFF2E1500),
    tertiary = IndigoTertiary,
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    surfaceVariant = Color(0xFFF1F5F9),
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A)
)

@Composable
fun ElectroStockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
