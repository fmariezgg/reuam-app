package ni.edu.uam.reuam.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = ReUAMGreen,
    secondary = ReUAMSecondary,
    background = ReUAMBackground,
    surface = ReUAMSurface,
    error = ReUAMError,
    onPrimary = ReUAMSurface,
    onSecondary = ReUAMTextPrimary,
    onBackground = ReUAMTextPrimary,
    onSurface = ReUAMTextPrimary,
    onError = ReUAMSurface
)

private val DarkColorScheme = darkColorScheme(
    primary = ReUAMGreenLight,
    secondary = ReUAMSecondary,
    background = ReUAMTextPrimary,
    surface = ColorDarkSurface,
    error = ReUAMError,
    onPrimary = ReUAMTextPrimary,
    onSecondary = ReUAMTextPrimary,
    onBackground = ReUAMSurface,
    onSurface = ReUAMSurface,
    onError = ReUAMSurface
)

@Composable
fun ReUAMTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}