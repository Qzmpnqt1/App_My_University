import com.android.build.gradle.AppExtension
import java.io.File
import org.gradle.api.tasks.Exec
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.kapt")
    id("org.jetbrains.kotlin.android") version "2.0.21"
    id("com.google.dagger.hilt.android") version "2.51"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
    jacoco
}

// При true задача :hiltAggregateDeps* падает с NoSuchMethodError на JavaPoet ClassName.canonicalName()
// (конфликт версий javapoet на classpath плагина + Kotlin 2.x). Явный hiltViewModel(backStackEntry) в NavHost
// компенсирует отключённую агрегацию для @HiltViewModel.
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

    signingConfigs {
        val storePath = System.getenv("ANDROID_SIGNING_STORE_FILE")?.trim().orEmpty()
        if (storePath.isNotEmpty()) {
            create("release") {
                storeFile = file(storePath)
                storePassword = System.getenv("ANDROID_SIGNING_STORE_PASSWORD") ?: ""
                keyAlias = System.getenv("ANDROID_SIGNING_KEY_ALIAS") ?: ""
                keyPassword = System.getenv("ANDROID_SIGNING_KEY_PASSWORD") ?: ""
            }
        }
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
            signingConfigs.findByName("release")?.let { signingConfig = it }

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
        // Debug: HTTPS на 8443. По умолчанию — LAN-IP для физического телефона в Wi‑Fi (не 10.0.2.2).
        // Смените fallback или задайте в gradle.properties: apiBaseUrl=https://<IPv4_ПК>:8443/
        // Для эмулятора: apiBaseUrl=https://10.0.2.2:8443/
        debug {
            isDebuggable = true
            enableUnitTestCoverage = true
            val raw = (project.findProperty("apiBaseUrl") as String?)?.trim().orEmpty()
            val baseUrl = normalizedHttpsApiBaseUrl(raw, "https://192.168.200.168:8443/")
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

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-notice.md"
        }
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
    testImplementation(libs.mockwebserver)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.androidx.junit.v121)
    androidTestImplementation(libs.androidx.espresso.core.v361)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Тестирование для Hilt
    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.google.hilt.android.compiler)
}

tasks.withType<Test>().configureEach {
    if (name.contains("UnitTest", ignoreCase = true)) {
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
    }
}

// Поднаборы instrumented-тестов через adb: JUnit @Suite на устройстве часто ведёт себя нестабильно;
// список классов совпадает с MuUiSmokeSuite / MuUiRegressionSuite.
val androidAppExt = extensions.getByType(AppExtension::class.java)
val adbExecutable = androidAppExt.sdkDirectory.resolve("platform-tools").let { dir ->
    val name = if (System.getProperty("os.name", "").lowercase().contains("windows")) "adb.exe" else "adb"
    File(dir, name)
}
val muSmokeInstrumentedClasses = listOf(
    "com.example.app_my_university.ExampleInstrumentedTest",
    "com.example.app_my_university.ui.screens.LoginScreenInstrumentedTest",
    "com.example.app_my_university.ui.flows.StudentBottomNavInstrumentedTest",
).joinToString(",")
val muRegressionInstrumentedClasses = listOf(
    "com.example.app_my_university.ui.flows.AuthFlowInstrumentedTest",
    "com.example.app_my_university.ui.flows.StudentFlowInstrumentedTest",
    "com.example.app_my_university.ui.flows.TeacherAndAdminFlowInstrumentedTest",
    "com.example.app_my_university.ui.flows.TeacherBottomNavInstrumentedTest",
    "com.example.app_my_university.ui.flows.ModalSheetsInstrumentedTest",
).joinToString(",")

fun muListAdbDeviceIds(): List<String> {
    val out = ProcessBuilder(adbExecutable.absolutePath, "devices")
        .redirectErrorStream(true)
        .start()
        .inputStream
        .bufferedReader()
        .readText()
    return out.lineSequence()
        .map { it.trim() }
        .filter { it.endsWith("\tdevice") }
        .map { line -> line.substringBefore("\t").trim() }
        .filter { it.isNotEmpty() }
        .toList()
}

fun muAdbSerialPrefixArgs(): List<String> {
    val fromEnv = System.getenv("ANDROID_SERIAL")?.trim().orEmpty()
    if (fromEnv.isNotEmpty()) {
        return listOf("-s", fromEnv)
    }
    val ids = muListAdbDeviceIds()
    return if (ids.size == 1) listOf("-s", ids[0]) else emptyList()
}

tasks.register("muAssertConnectedDevice") {
    group = "verification"
    description =
        "Проверка adb get-state. Несколько устройств — задайте ANDROID_SERIAL (см. https://developer.android.com/studio/command-line/adb )."
    doLast {
        val fromEnv = System.getenv("ANDROID_SERIAL")?.trim().orEmpty()
        val ids = muListAdbDeviceIds()
        if (ids.isEmpty()) {
            throw GradleException(
                "Нет подключённых устройств в состоянии device. Выполните `adb devices` и запустите эмулятор или подключите телефон.",
            )
        }
        if (ids.size > 1 && fromEnv.isEmpty()) {
            throw GradleException(
                "Подключено устройств: ${ids.size} (${ids.joinToString()}). Задайте переменную окружения ANDROID_SERIAL " +
                    "(например ANDROID_SERIAL=${ids[0]}), иначе adb не знает, куда ставить APK.",
            )
        }
        val code = ProcessBuilder(listOf(adbExecutable.absolutePath) + muAdbSerialPrefixArgs() + listOf("get-state"))
            .inheritIO()
            .start()
            .waitFor()
        if (code != 0) {
            throw GradleException("adb get-state завершился с кодом $code")
        }
    }
}

/** Снимает debug-сборки с устройства перед install (часто снимает «not enough space» на AVD). */
tasks.register("muUninstallDebugPackages") {
    group = "verification"
    description =
        "adb uninstall app + androidTest-пакета; коды ошибок игнорируются. Отключить: -Pmu.skipInstrumentedUninstall=true"
    dependsOn("muAssertConnectedDevice")
    doLast {
        if (project.findProperty("mu.skipInstrumentedUninstall")?.toString() == "true") {
            logger.lifecycle("mu.skipInstrumentedUninstall=true — пропуск uninstall")
            return@doLast
        }
        val adb = listOf(adbExecutable.absolutePath) + muAdbSerialPrefixArgs()
        val appId = androidAppExt.defaultConfig.applicationId!!
        listOf(appId, "${appId}.test").forEach { pkg ->
            val code = ProcessBuilder(adb + listOf("uninstall", pkg))
                .inheritIO()
                .start()
                .waitFor()
            if (code != 0) {
                logger.lifecycle("adb uninstall $pkg завершился с кодом $code (ожидаемо, если пакет не установлен)")
            }
        }
    }
}

/**
 * Цепочка: assert → uninstall (опционально) → installDebug → installDebugAndroidTest.
 * Рекомендуется перед полным UI-прогоном при нехватке места на эмуляторе.
 */
tasks.register("muPrepareInstrumentedDevice") {
    group = "verification"
    description =
        "Подготовка эмулятора/устройства к instrumented: uninstall debug + свежая установка app и test APK."
}

fun registerMuAdbInstrumentedSubset(name: String, description: String, classCsv: String) {
    tasks.register<Exec>(name) {
        group = "verification"
        this.description = description
        dependsOn("muPrepareInstrumentedDevice")
        executable = adbExecutable.absolutePath
        args(
            muAdbSerialPrefixArgs() + listOf(
                "shell",
                "am",
                "instrument",
                "-w",
                "-r",
                "-e",
                "class",
                classCsv,
                "${androidAppExt.defaultConfig.applicationId}.test/androidx.test.runner.AndroidJUnitRunner",
            ),
        )
    }
}

registerMuAdbInstrumentedSubset(
    "connectedSmokeDebugAndroidTest",
    "Smoke: Example + Login + StudentBottomNav (adb -e class; ANDROID_SERIAL при нескольких девайсах).",
    muSmokeInstrumentedClasses,
)
registerMuAdbInstrumentedSubset(
    "connectedRegressionDebugAndroidTest",
    "Регрессия: Auth + Student + Teacher/Admin + ModalSheets (adb -e class).",
    muRegressionInstrumentedClasses,
)

tasks.register("connectedFullDebugAndroidTest") {
    group = "verification"
    description = "Полный instrumented run (то же, что connectedDebugAndroidTest)."
    dependsOn("connectedDebugAndroidTest")
}

afterEvaluate {
    val uninstall = tasks.named("muUninstallDebugPackages")
    tasks.named("installDebug").configure { mustRunAfter(uninstall) }
    tasks.named("installDebugAndroidTest").configure { mustRunAfter(tasks.named("installDebug")) }
    tasks.named("muPrepareInstrumentedDevice").configure {
        dependsOn(uninstall, tasks.named("installDebugAndroidTest"))
    }
    if (project.findProperty("mu.skipInstrumentedUninstall")?.toString() != "true") {
        tasks.named("connectedDebugAndroidTest").configure { dependsOn(uninstall.get()) }
    }
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.register<JacocoReport>("jacocoAndroidUnitTestReport") {
    dependsOn("testDebugUnitTest")
    group = "verification"
    description = "HTML/XML JaCoCo по JVM unit (:app:testDebugUnitTest). Без gate — только отчёт."
    executionData.setFrom(
        fileTree(layout.buildDirectory.get()) {
            include("jacoco/testDebugUnitTest.exec", "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
        },
    )
    classDirectories.setFrom(
        fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
            exclude(
                "**/R.class",
                "**/R\$*.class",
                "**/BuildConfig.*",
                "**/Manifest*.*",
            )
        },
    )
    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
    reports {
        xml.required.set(true)
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/jvmUnitTest/html"))
    }
}