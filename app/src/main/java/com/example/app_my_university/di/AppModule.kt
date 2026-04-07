package com.example.app_my_university.di

import android.content.Context
import androidx.room.Room
import com.example.app_my_university.BuildConfig
import com.example.app_my_university.core.database.AppDatabase
import com.example.app_my_university.core.database.CacheDao
import com.example.app_my_university.data.api.ApiService
import com.example.app_my_university.data.network.ApiBaseUrlValidator
import com.example.app_my_university.data.network.AuthInterceptor
import com.example.app_my_university.data.network.UnauthorizedResponseInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import com.example.app_my_university.data.network.SafeHttpLoggingInterceptor
import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "moi_vuz_cache.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideCacheDao(db: AppDatabase): CacheDao = db.cacheDao()

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        unauthorizedResponseInterceptor: UnauthorizedResponseInterceptor
    ): OkHttpClient {
        val loggingInterceptor = SafeHttpLoggingInterceptor()
        return OkHttpClient.Builder()
            .addInterceptor(unauthorizedResponseInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        ApiBaseUrlValidator.requireHttpsBaseUrl(BuildConfig.API_BASE_URL)
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}
