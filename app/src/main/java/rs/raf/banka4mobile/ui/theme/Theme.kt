package rs.raf.banka4mobile.ui.theme

import android.app.Activity
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
    primary = PrimaryBlueDark,
    onPrimary = Color(0xFF00294D),
    primaryContainer = Color(0xFF004B86),
    onPrimaryContainer = Color(0xFFD5E9FF),
    secondary = AccentBlueDark,
    onSecondary = Color(0xFF10233D),
    secondaryContainer = Color(0xFF1C335A),
    onSecondaryContainer = Color(0xFFD9E3FF),
    tertiary = GradientEndDark,
    onTertiary = Color(0xFF10233D),
    tertiaryContainer = Color(0xFF1E3B63),
    onTertiaryContainer = Color(0xFFD3E8FF),
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorderSoft,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = LightSurfaceTint,
    onPrimaryContainer = Color(0xFF00294D),
    secondary = AccentBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD9E4FF),
    onSecondaryContainer = Color(0xFF00325C),
    tertiary = AccentBlue,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE6F0FF),
    onTertiaryContainer = Color(0xFF00325C),
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    outlineVariant = LightBorderSoft,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFF9D8D6),
    onErrorContainer = Color(0xFF410002)
)

@Composable
fun Banka4MobileTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
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