package test.raku

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = Color(0xFF8BC7FF),       // Bright Arctic blue (lighter)
    primaryVariant = Color(0xFF3A9EFF),  // Vibrant Arctic blue
    secondary = Color(0xFF7BE0FF)       // Icy cyan accent
)

private val LightColorPalette = lightColors(
    primary = Color(0xFF0066CC),       // Deep Arctic blue
    primaryVariant = Color(0xFF004C99),  // Darker Arctic blue
    secondary = Color(0xFF00B4D8)       // Refreshing teal accent
)

@Composable
fun RakuTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content
    )
}
