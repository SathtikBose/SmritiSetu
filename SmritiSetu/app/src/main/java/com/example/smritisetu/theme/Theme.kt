package com.example.smritisetu.theme

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.example.smritisetu.data.AppLanguage
import com.example.smritisetu.data.AppThemeMode
import com.example.smritisetu.data.LocalAppStrings
import com.example.smritisetu.data.getStringsForLanguage

private val DarkColorScheme = darkColorScheme(
    primary = TealPrimaryLight,
    onPrimary = Color(0xFF003731),
    primaryContainer = Color(0xFF005048),
    onPrimaryContainer = TealContainerLight,
    secondary = SecondaryWarmLight,
    onSecondary = Color(0xFF1C3531),
    secondaryContainer = Color(0xFF233E3A),
    onSecondaryContainer = Color(0xFFD4EDE7),
    tertiary = TertiaryGoldLight,
    onTertiary = Color(0xFF412D00),
    background = Color(0xFF0D1816),
    surface = Color(0xFF142421),
    onBackground = Color(0xFFF1F5F4),
    onSurface = Color(0xFFF1F5F4),
    surfaceVariant = Color(0xFF263A36),
    onSurfaceVariant = Color(0xFFCBD5D2)
)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = TealContainerLight,
    onPrimaryContainer = OnTealContainerLight,
    secondary = SecondaryWarm,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCE8E2),
    onSecondaryContainer = Color(0xFF05201C),
    tertiary = TertiaryGold,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDEA3),
    onTertiaryContainer = Color(0xFF271900),
    background = Color(0xFFF5FAF8),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF111D1B),
    onSurface = Color(0xFF111D1B),
    surfaceVariant = Color(0xFFE2EBE8),
    onSurfaceVariant = Color(0xFF3B4D49)
)

private val HighContrastColorScheme = lightColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF004D40),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF004D40),
    onSecondary = Color.White,
    background = Color.White,
    surface = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFE5E5E5),
    onSurfaceVariant = Color.Black
)

@Composable
fun getGlassGradientBrush(darkTheme: Boolean): Brush {
    return if (darkTheme) {
        Brush.verticalGradient(listOf(Color(0xFF0B1614), Color(0xFF142421), Color(0xFF0F1C19)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFE3F3EF), Color(0xFFF4FAF8), Color(0xFFEBF7F4)))
    }
}

@Composable
fun isAppInDarkTheme(themeMode: AppThemeMode): Boolean {
    return when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.HIGH_CONTRAST -> false
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    darkTheme: Boolean = isSystemInDarkTheme(),
    borderWidth: Float = 1.2f,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val backgroundColor = if (darkTheme) Color(0xCC1A2B27) else Color(0xE6FFFFFF)
    val borderColor = if (darkTheme) Color(0x33FFFFFF) else Color(0x66FFFFFF)

    val clickableModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else modifier

    Surface(
        modifier = clickableModifier
            .shadow(
                elevation = 8.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = if (darkTheme) 0.3f else 0.06f),
                spotColor = Color.Black.copy(alpha = if (darkTheme) 0.5f else 0.12f)
            ),
        shape = shape,
        color = backgroundColor,
        border = BorderStroke(borderWidth.dp, borderColor)
    ) {
        Box(content = content)
    }
}

@Composable
fun SmritiSetuTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    fontScale: Float = 1.0f,
    selectedLanguage: AppLanguage = AppLanguage.ASSAMESE,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val darkTheme = isAppInDarkTheme(themeMode)

    val colorScheme = when {
        themeMode == AppThemeMode.HIGH_CONTRAST -> HighContrastColorScheme
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val currentDensity = LocalDensity.current
    val customDensity = Density(
        density = currentDensity.density,
        fontScale = currentDensity.fontScale * fontScale
    )

    val currentStrings = getStringsForLanguage(selectedLanguage)

    CompositionLocalProvider(
        LocalDensity provides customDensity,
        LocalAppStrings provides currentStrings
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
