package dev.koukeneko.opass

import HomeScreen
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.koukeneko.opass.screens.ScheduleScreen
import dev.koukeneko.opass.screens.WebViewScreen
import dev.koukeneko.opass.ui.theme.OPassTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //if sdk version > 29
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.R) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        } else {
            enableEdgeToEdge()
        }

        setContent {
            OPassTheme {
                val lightTheme = !isSystemInDarkTheme()
                val barColor = MaterialTheme.colorScheme.background.toArgb()
                LaunchedEffect(lightTheme) {
                    if (lightTheme) {
                        enableEdgeToEdge(
                            statusBarStyle = SystemBarStyle.light(
                                barColor, barColor,
                            ),
                            navigationBarStyle = SystemBarStyle.light(
                                barColor, barColor,
                            ),
                        )
                    } else {
                        enableEdgeToEdge(
                            statusBarStyle = SystemBarStyle.dark(
                                barColor,
                            ),
                            navigationBarStyle = SystemBarStyle.dark(
                                barColor,
                            ),
                        )
                    }
                }




                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    AppContent()
                }
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent() {
    val navController = rememberNavController()
    NavHost(
        navController = navController, startDestination = "home", modifier = Modifier.fillMaxSize()
    ) {
        composable("home") {
            HomeScreen(
                navController = navController,
            )
        }
        composable(
            "web_view/{title}/{url}",
            arguments = listOf(navArgument("url") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType })
        ) { backStackEntry ->
            val url = backStackEntry.arguments?.getString("url") ?: ""
            val title = backStackEntry.arguments?.getString("title") ?: ""
            Log.d("WebViewScreen", "url: $url, title: $title")
            WebViewScreen(
                navController = navController, url = url, title = title
            )
        }
        composable("schedule") {
            ScheduleScreen(
                navController = navController,
            )
        }


    }

}