package test.raku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.navigation.compose.*
import android.net.Uri
import androidx.navigation.NavType
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContent {
			RakuTheme {
				Surface(modifier = Modifier.fillMaxSize()) {
					val navController = rememberNavController()
					NavHost(navController, startDestination = "videos") {
						composable("videos") {
							Videos(navController)
						}
						composable(
							"player/{uri}",
							arguments = listOf(navArgument("uri") { type = NavType.StringType })
						) { backStackEntry ->
							val uriStr = backStackEntry.arguments?.getString("uri")
							val uri = Uri.parse(uriStr)
							Player(uri = uri, navController = navController)
						}
					}
				}
			}
		}
	}
}
