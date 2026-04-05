package com.example.app_my_university

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.app_my_university.worker.BackgroundSyncWorker
import java.util.concurrent.TimeUnit
import androidx.compose.ui.graphics.Color
import com.example.app_my_university.data.theme.AppThemePreference
import com.example.app_my_university.ui.navigation.AppNavigation
import com.example.app_my_university.ui.theme.AppMyUniversityTheme
import com.example.app_my_university.ui.viewmodel.RootThemeViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val rootThemeViewModel: RootThemeViewModel by viewModels()
            val themePreference by rootThemeViewModel.themePreference.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (themePreference) {
                AppThemePreference.LIGHT -> false
                AppThemePreference.DARK -> true
                AppThemePreference.SYSTEM -> systemDark
            }

            AppMyUniversityTheme(darkTheme = darkTheme, dynamicColor = false) {
                val systemUiController = rememberSystemUiController()
                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = Color.Transparent,
                        darkIcons = !darkTheme,
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LaunchedEffect(Unit) {
                        val request = PeriodicWorkRequestBuilder<BackgroundSyncWorker>(15, TimeUnit.MINUTES)
                            .build()
                        WorkManager.getInstance(this@MainActivity).enqueueUniquePeriodicWork(
                            BackgroundSyncWorker.UNIQUE_NAME,
                            ExistingPeriodicWorkPolicy.KEEP,
                            request
                        )
                    }
                    AppNavigation()
                }
            }
        }
    }
}
