package com.example.app_my_university

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.example.app_my_university.ui.navigation.AppNavigation
import com.example.app_my_university.ui.theme.AppMyUniversityTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Improve window insets handling for better performance
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        super.onCreate(savedInstanceState)
        
        setContent {
            AppMyUniversityTheme {
                SplashScreenWithDelay()
            }
        }
    }
}

@Composable
fun SplashScreenWithDelay() {
    // Используем простой boolean вместо MutableTransitionState
    var showSplash by remember { mutableStateOf(true) }
    
    // Устанавливаем таймер для задержки
    LaunchedEffect(Unit) {
        // Показываем экран загрузки 1.5 секунды
        delay(1500)
        showSplash = false
    }
    
    // Простое переключение между экранами без анимации
    Surface(modifier = Modifier.fillMaxSize()) {
        if (showSplash) {
            // Экран загрузки
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                // Цветной круг вместо изображения логотипа
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                
                // Индикатор загрузки
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 64.dp),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        } else {
            // Основной контент
            AppNavigation()
        }
    }
}
