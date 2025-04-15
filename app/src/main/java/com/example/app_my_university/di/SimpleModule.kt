package com.example.app_my_university.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Простой модуль для тестирования работы Hilt.
 * Если этот модуль работает, но AppModule вызывает ошибки,
 * это поможет определить проблему точнее.
 */
@Module
@InstallIn(SingletonComponent::class)
object SimpleModule {

    /**
     * Простой провайдер строки для тестирования.
     */
    @Provides
    @Singleton
    fun provideTestString(): String {
        return "Test string from Hilt"
    }
} 