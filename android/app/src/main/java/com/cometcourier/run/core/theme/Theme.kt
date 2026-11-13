package com.cometcourier.run.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalMotionEnabled = staticCompositionLocalOf { true }
private val CourierColors = darkColorScheme(
    primary = CourierGreen, onPrimary = Ink, secondary = SoftMint, onSecondary = Ink,
    tertiary = ActionCherry, onTertiary = MintBackground, background = SpaceTop,
    onBackground = MintBackground, surface = SpaceSurface, onSurface = MintBackground,
    surfaceVariant = SpaceIndigo, onSurfaceVariant = SoftMint, outline = SoftMint,
    error = ActionCherry, onError = MintBackground
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = CourierColors, typography = AppTypography, content = content)
}
