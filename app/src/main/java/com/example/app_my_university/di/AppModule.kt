package com.example.app_my_university.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Основной модуль для предоставления зависимостей на уровне приложения.
 * 
 * Важно: для Hilt модули должны быть статическими классами (object в Kotlin)
 * и должны иметь статические методы предоставления (обозначенные @Provides).
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Простой метод для проверки работы Hilt
     */
    @Provides
    @Singleton
    fun provideString(): String {
        return "AppModule is working"
    }
} 