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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
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
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // Переносим тяжелые инициализации в фон
        withContext(Dispatchers.IO) {
            // Имитация загрузки/инициализации данных
            delay(1500)
        }
        showSplash = false
    }

    Surface {
        if (showSplash) {
            // Упрощенный сплеш-скрин
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            AppNavigation()
        }
    }
}
