package com.app.toonplay.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val ToonPlayDarkColorScheme =
  darkColorScheme(
    primary = ToonVioletPrimary,
    onPrimary = TextPrimary,
    primaryContainer = ToonVioletSoft,
    onPrimaryContainer = TextPrimary,
    secondary = ToonIndigoAccent,
    onSecondary = TextPrimary,
    secondaryContainer = ObsidianElevated,
    onSecondaryContainer = TextPrimary,
    tertiary = ToonPurpleGlow,
    onTertiary = ObsidianBg,
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = ObsidianSurface,
    onSurface = TextPrimary,
    surfaceVariant = ObsidianCard,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = ObsidianCard,
    surfaceContainerHigh = ObsidianElevated,
    outline = BorderSubtle,
    outlineVariant = BorderBright,
  )

private val ToonPlayShapes =
  Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
  )

@Composable
fun ToonPlayTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = ToonPlayDarkColorScheme,
    typography = ToonPlayTypography,
    shapes = ToonPlayShapes,
    content = content,
  )
}
