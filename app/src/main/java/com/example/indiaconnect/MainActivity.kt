package com.example.indiaconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.indiaconnect.ui.*
import com.example.indiaconnect.ui.theme.IndiaConnectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IndiaConnectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    IndiaConnectApp()
                }
            }
        }
    }
}

@Composable
fun IndiaConnectApp() {
    var currentLanguage by remember { mutableStateOf("English") }
    val strings = TranslationMap.getStrings(currentLanguage)

    CompositionLocalProvider(LocalAppStrings provides strings) {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = "splash"
        ) {
            composable("splash") {
                SplashScreen {
                    navController.navigate("choose_language") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            }
            composable("choose_language") {
                ChooseLanguageScreen(
                    onLanguageSelected = { language ->
                        currentLanguage = language
                        navController.navigate("main_dashboard/$language") {
                            popUpTo("choose_language") { inclusive = true }
                        }
                    }
                )
            }
            composable(
                route = "main_dashboard/{language}",
                arguments = listOf(navArgument("language") { type = NavType.StringType })
            ) { backStackEntry ->
                val languageArg = backStackEntry.arguments?.getString("language") ?: "English"
                MainDashboardScreen(language = languageArg)
            }
        }
    }
}
