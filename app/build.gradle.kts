plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.kapt")
    id("org.jetbrains.kotlin.android") version "2.0.21"
    id("com.google.dagger.hilt.android") version "2.51"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
    alias(libs.plugins.google.services)
}

hilt {
    enableAggregatingTask = false
}

android {
    namespace = "com.example.app_my_university"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.app_my_university"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    fun normalizedHttpsApiBaseUrl(raw: String, fallbackHttps: String): String {
        val chosen = raw.trim().ifEmpty { fallbackHttps }
        val baseUrl = when {
            chosen.endsWith("/") -> chosen
            else -> "$chosen/"
        }
        if (!baseUrl.startsWith("https://", ignoreCase = true)) {
            throw GradleException("apiBaseUrl / apiBaseUrlRelease must use HTTPS only (HTTP cleartext is disabled). Got: $baseUrl")
        }
        return baseUrl
    }

    // Настраиваем сборки
    buildTypes {
        // Релизная сборка (Release build)
        release {
            // Отключаем отладочную сборку
            isDebuggable = false

            // Включаем R8 / ProGuard
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // Базовый URL API только HTTPS (-PapiBaseUrlRelease=https://api.example.com/)
            val raw = (project.findProperty("apiBaseUrlRelease") as String?)?.trim().orEmpty()
            val baseUrl = normalizedHttpsApiBaseUrl(raw, "https://example.com/")
            buildConfigField(
                "String",
                "API_BASE_URL",
                "\"${baseUrl.replace("\\", "\\\\").replace("\"", "\\\"")}\""
            )

            // При необходимости можно добавить signingConfig signingConfigs.release
            // для автоматической подписи APK.
        }
        // Debug: сервер Spring Boot с TLS на 8443 (см. Server_My_University application.properties).
        // Эмулятор: https://10.0.2.2:8443/  Телефон в Wi‑Fi: https://<IPv4_ПК>:8443/ (gradle.properties: apiBaseUrl=...)
        debug {
            isDebuggable = true
            val raw = (project.findProperty("apiBaseUrl") as String?)?.trim().orEmpty()
            val baseUrl = normalizedHttpsApiBaseUrl(raw, "https://10.0.2.2:8443/")
            buildConfigField(
                "String",
                "API_BASE_URL",
                "\"${baseUrl.replace("\\", "\\\\").replace("\"", "\\\"")}\""
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        // Оптимизация производительности для Kotlin
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xcontext-receivers"
        )
    }

    buildFeatures {
        compose = true
        // Оптимизация для улучшения скорости компиляции
        buildConfig = true
    }

    composeOptions {
        // Обновленная версия, совместимая с Kotlin 2.0.21
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {
    // AndroidX и базовые библиотеки
    implementation(libs.androidx.core.ktx.v1160)
    implementation(libs.androidx.appcompat.v170)
    implementation(libs.material.v190)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.constraintlayout.v221)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.material3)

    // Профилирование и оптимизация производительности
    implementation("androidx.profileinstaller:profileinstaller:1.3.1")
    implementation("androidx.startup:startup-runtime:1.1.1")

    // Jetpack Compose с оптимизированными зависимостями
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)

    // Добавляем animation для более плавных переходов
    implementation("androidx.compose.animation:animation:1.6.0")

    // Улучшение запуска и инициализации Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.compose.runtime:runtime-livedata:1.6.0")
    implementation("androidx.compose.runtime:runtime-tracing:1.0.0-beta01")

    // Навигация с Compose
    implementation(libs.androidx.navigation.compose)

    // Hilt для dependency injection
    implementation(libs.hilt.android)
    kapt(libs.google.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Retrofit и OkHttp для работы с REST API
    implementation(libs.retrofit)
    implementation(libs.converter.gson)  // Используем Gson вместо Moshi для совместимости с API
    implementation(libs.logging.interceptor)

    // Kotlin Coroutines для асинхронных операций
    implementation(libs.kotlinx.coroutines.android)

    // DataStore для хранения токена и настроек
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Moshi для работы с JSON
    implementation(libs.moshi)
    kapt(libs.moshi.kotlin.codegen)

    // Room для локального хранилища
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    // Coil для загрузки изображений в Compose
    implementation(libs.coil.compose)
    
    // System UI Controller для управления системными интерфейсами
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.33.2-alpha")
    
    // Core для работы с инсетами в Compose
    implementation("androidx.core:core-splashscreen:1.0.1")

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    kapt(libs.androidx.hilt.compiler)

    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)

    // Тестовые зависимости
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.androidx.junit.v121)
    androidTestImplementation(libs.androidx.espresso.core.v361)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Тестирование для Hilt
    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.google.hilt.android.compiler)
}