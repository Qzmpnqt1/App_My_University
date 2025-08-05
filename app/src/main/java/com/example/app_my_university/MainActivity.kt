package com.example.app_my_university

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.example.app_my_university.ui.navigation.AppNavigation
import com.example.app_my_university.ui.navigation.Screen
import com.example.app_my_university.ui.theme.AppMyUniversityTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Включаем edge-to-edge отображение
        enableEdgeToEdge()
        
        super.onCreate(savedInstanceState)
        
        setContent {
            AppMyUniversityTheme {
                // Управление цветом системной строки состояния
                val systemUiController = rememberSystemUiController()
                val useDarkIcons = !isSystemInDarkTheme()
                val backgroundColor = MaterialTheme.colorScheme.background
                
                DisposableEffect(systemUiController, useDarkIcons) {
                    // Обновляем цвет строки состояния для соответствия теме
                    systemUiController.setSystemBarsColor(
                        color = Color.Transparent,
                        darkIcons = useDarkIcons
                    )
                    onDispose {}
                }
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = backgroundColor
                ) {
                    val navController = rememberNavController()
                    AppNavigation(
                        navController = navController,
                        startDestination = Screen.OnboardingWelcome.route
                    )
                }
            }
        }
    }
}