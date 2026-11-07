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
    primary = ToastAmber,
    secondary = ButterYellow,
    tertiary = ToastGold,
    background = BakerySurfaceDark,
    surface = Color(0xFF292524),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = BakeryOnSurfaceDark,
    onSurface = BakeryOnSurfaceDark
)

private val LightColorScheme = lightColorScheme(
    primary = ToastGold,
    secondary = ToastAmber,
    tertiary = CrustBrown,
    background = BakerySurfaceLight,
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = BakeryOnSurfaceLight,
    onSurface = BakeryOnSurfaceLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep intentional warm bakery palette
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
