package app.breeze

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import app.breeze.data.AppTheme
import app.breeze.data.ThemePreferenceRepository
import app.breeze.theme.Breeze

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val themeRepository = ThemePreferenceRepository(applicationContext)

        setContent {
            val currentAppTheme by themeRepository.appTheme.collectAsState(initial = AppTheme.SYSTEM_DEFAULT)

            val useDarkTheme = when (currentAppTheme) {
                AppTheme.DARK -> true
                AppTheme.LIGHT -> false
                AppTheme.SYSTEM_DEFAULT -> isSystemInDarkTheme()
            }

            val view = LocalView.current
            DisposableEffect(useDarkTheme) {
                val window = (view.context as ComponentActivity).window
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !useDarkTheme
                    isAppearanceLightNavigationBars = !useDarkTheme
                }
                onDispose { }
            }

            Breeze(darkTheme = useDarkTheme) {
                BreezeApp(themeRepository = themeRepository)
            }
        }
    }
}
