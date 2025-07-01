package com.example.app_my_university

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Основной класс приложения, который инициализирует Hilt.
 * 
 * HiltAndroidApp аннотация генерирует все необходимые компоненты Dagger
 * и запускает контейнер внедрения зависимостей.
 */
@HiltAndroidApp
class MyUniversityApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
    }
}