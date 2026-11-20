package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val TutorDeskLightColorScheme = lightColorScheme(
    primary = DeepSlateNavy,
    onPrimary = SurfaceWhite,
    primaryContainer = Slate900,
    onPrimaryContainer = SurfaceWhite,
    secondary = InteractiveBlue,
    onSecondary = SurfaceWhite,
    secondaryContainer = InteractiveBlueBg,
    onSecondaryContainer = InteractiveBlueDeep,
    tertiary = EmeraldGreen,
    onTertiary = SurfaceWhite,
    tertiaryContainer = EmeraldBg,
    onTertiaryContainer = EmeraldText,
    background = CanvasBase,
    onBackground = Slate900,
    surface = SurfaceWhite,
    onSurface = Slate900,
    surfaceVariant = SurfaceLow,
    onSurfaceVariant = Slate500,
    surfaceContainerLowest = SurfaceWhite,
    surfaceContainerLow = CanvasBase,
    surfaceContainer = SurfaceLow,
    surfaceContainerHigh = SurfaceContainer,
    surfaceContainerHighest = SurfaceContainerHigh,
    outline = Slate300,
    outlineVariant = Slate200,
    error = CoralRed,
    onError = SurfaceWhite,
    errorContainer = CoralBg,
    onErrorContainer = CoralText
)

val TutorDeskShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun TutorDeskTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TutorDeskLightColorScheme,
        typography = Typography,
        shapes = TutorDeskShapes,
        content = content
    )
}
