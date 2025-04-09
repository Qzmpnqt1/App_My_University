plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.dagger.hilt.android") version "2.46.1"
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

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
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

    // Jetpack Compose
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)

    // Навигация с Compose
    implementation(libs.androidx.navigation.compose)

    // Hilt для dependency injection
    implementation(libs.hilt.android)
    kapt(libs.google.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Retrofit и OkHttp для работы с REST API
    implementation(libs.retrofit)
    implementation(libs.converter.moshi)
    implementation(libs.logging.interceptor)

    // Kotlin Coroutines для асинхронных операций
    implementation(libs.kotlinx.coroutines.android)

    // Moshi для работы с JSON
    implementation(libs.moshi)
    kapt(libs.moshi.kotlin.codegen)

    // Room для локального хранилища
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    // Coil для загрузки изображений в Compose
    implementation(libs.coil.compose)

    // Тестовые зависимости
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.v121)
    androidTestImplementation(libs.androidx.espresso.core.v361)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Тестирование для Hilt
    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.google.hilt.android.compiler)
}
