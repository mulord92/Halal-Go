package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = PrimaryEmerald,
    primaryContainer = PrimaryContainer,
    onPrimary = OnPrimary,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = GoldSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondary = OnSecondary,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = TertiaryEmerald,
    tertiaryContainer = TertiaryContainer,
    onTertiary = OnTertiary,
    background = DarkBackground,
    onBackground = OnSurfaceText,
    surface = DarkSurface,
    onSurface = OnSurfaceText,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = OnSurfaceVariantText,
    error = ErrorRed,
    errorContainer = ErrorContainerRed,
    onError = OnError,
    onErrorContainer = OnErrorContainer
  )

private val LightColorScheme = DarkColorScheme // Reusable dark theme as default for Halal Go style

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force premium dark mode-first experience
  dynamicColor: Boolean = false, // Use our handcrafted luxury brand colors instead of default system colors
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
