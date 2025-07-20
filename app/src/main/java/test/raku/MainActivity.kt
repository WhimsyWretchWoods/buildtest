package test.raku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RakuTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var hasVideoPermission by remember { mutableStateOf(false) }

                    if (hasVideoPermission) {
                        AppNav()
                    } else {
                        RequestVideoPermission { isGranted ->
                            hasVideoPermission = isGranted
                        }
                        
                    }
                }
            }
        }
    }
}
