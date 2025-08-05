package com.example.app_my_university.data.network

import android.util.Log
import com.example.app_my_university.data.api.UniversityApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    private const val TAG = "NetworkModule"
    
    @Provides
    @Singleton
    fun provideAuthInterceptor(): AuthInterceptor {
        return AuthInterceptor()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val builder = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                try {
                    val request = chain.request()
                    Log.d(TAG, "Sending request to ${request.url}")
                    val response = chain.proceed(request)
                    Log.d(TAG, "Received response from ${request.url}, code: ${response.code}")
                    response
                } catch (e: Exception) {
                    Log.e(TAG, "Error during network request: ${e.message}", e)
                    throw e
                }
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        return builder.build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        Log.d(TAG, "Creating Retrofit instance with base URL: ${ApiConfig.BASE_URL}")
        return Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideUniversityApiService(retrofit: Retrofit): UniversityApiService {
        return retrofit.create(UniversityApiService::class.java)
    }
}