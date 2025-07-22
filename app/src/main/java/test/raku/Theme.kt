package test.raku

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme()

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val colors = DarkColorScheme

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
