package com.example.app_my_university.di

import com.example.app_my_university.data.auth.TokenManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface NavigationEntryPoint {
    fun tokenManager(): TokenManager
}
