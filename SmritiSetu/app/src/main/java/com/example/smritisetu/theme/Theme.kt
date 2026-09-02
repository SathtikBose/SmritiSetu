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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.example.smritisetu.data.AppThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = TealPrimaryLight,
    onPrimary = Color(0xFF003731),
    primaryContainer = Color(0xFF005048),
    onPrimaryContainer = TealContainerLight,
    secondary = SecondaryWarmLight,
    onSecondary = Color(0xFF1C3531),
    tertiary = TertiaryGoldLight,
    onTertiary = Color(0xFF412D00),
    background = BgGradientStartDark,
    surface = Color(0xFF14221F),
    onBackground = Color(0xFFE1E3E2),
    onSurface = Color(0xFFE1E3E2),
    surfaceVariant = Color(0xFF263633),
    onSurfaceVariant = Color(0xFFBFC9C6)
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
    background = BgGradientStartLight,
    surface = Color(0xFFF0F7F5),
    onBackground = Color(0xFF191C1B),
    onSurface = Color(0xFF191C1B),
    surfaceVariant = Color(0xFFDBE5E2),
    onSurfaceVariant = Color(0xFF3F4947)
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
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = Color.Black
)

@Composable
fun getGlassGradientBrush(darkTheme: Boolean): Brush {
    return if (darkTheme) {
        Brush.verticalGradient(listOf(BgGradientStartDark, BgGradientEndDark))
    } else {
        Brush.verticalGradient(listOf(BgGradientStartLight, BgGradientEndLight))
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
    val backgroundColor = if (darkTheme) GlassDarkSurface else GlassLightSurface
    val borderColor = if (darkTheme) GlassBorderDark else GlassBorderLight

    val clickableModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else modifier

    Surface(
        modifier = clickableModifier
            .shadow(
                elevation = 6.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.05f),
                spotColor = Color.Black.copy(alpha = 0.1f)
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
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.HIGH_CONTRAST -> false
    }

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

    CompositionLocalProvider(LocalDensity provides customDensity) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
