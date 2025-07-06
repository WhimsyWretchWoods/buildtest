package app.breeze

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import app.breeze.MainApp
import app.breeze.data.AppTheme
import app.breeze.data.ThemePreferenceRepository
import app.breeze.theme.Breeze

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val themeRepository = remember { ThemePreferenceRepository(context) }
            val currentAppTheme by themeRepository.appTheme.collectAsState(initial = AppTheme.SYSTEM_DEFAULT)

            val useDarkTheme = when (currentAppTheme) {
                AppTheme.SYSTEM_DEFAULT -> isSystemInDarkTheme()
                AppTheme.DARK -> true
                AppTheme.LIGHT -> false
            }

            Breeze(darkTheme = useDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainApp(themeRepository = themeRepository)
                }
            }
        }
    }
}
