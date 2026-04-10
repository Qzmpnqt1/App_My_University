package com.example.app_my_university.ui

import com.example.app_my_university.ExampleInstrumentedTest
import com.example.app_my_university.ui.flows.StudentBottomNavInstrumentedTest
import com.example.app_my_university.ui.screens.LoginScreenInstrumentedTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Минимальный smoke-набор для быстрой проверки сборки и базовой UI-инфраструктуры.
 *
 * Предпочтительно: `./gradlew :app:connectedSmokeDebugAndroidTest --no-daemon`
 * (JUnit @Suite на устройстве ненадёжен; классы те же, что в Suite.)
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    ExampleInstrumentedTest::class,
    LoginScreenInstrumentedTest::class,
    StudentBottomNavInstrumentedTest::class,
)
class MuUiSmokeSuite
