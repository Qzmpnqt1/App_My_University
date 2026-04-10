package com.example.app_my_university

import com.example.app_my_university.data.repository.AuthRepositoryMockWebServerTest
import com.example.app_my_university.data.repository.GradeRepositoryMockWebServerTest
import com.example.app_my_university.data.repository.NotificationsRepositoryMockWebServerTest
import com.example.app_my_university.ui.viewmodel.GradeBookViewModelTest
import com.example.app_my_university.ui.viewmodel.LoginViewModelTest
import com.example.app_my_university.ui.viewmodel.NotificationsViewModelTest
import com.example.app_my_university.ui.viewmodel.ProfileViewModelPasswordTest
import com.example.app_my_university.ui.viewmodel.RegistrationViewModelTest
import com.example.app_my_university.ui.viewmodel.ScheduleViewModelTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Быстрый регрессионный набор JVM (unit + сеть): для отчёта и локального прогона одной конфигурацией.
 * Запуск: `./gradlew :app:testDebugUnitTest --tests "com.example.app_my_university.AndroidJvmRegressionSuite"`
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    LoginViewModelTest::class,
    RegistrationViewModelTest::class,
    ProfileViewModelPasswordTest::class,
    GradeBookViewModelTest::class,
    NotificationsViewModelTest::class,
    ScheduleViewModelTest::class,
    AuthRepositoryMockWebServerTest::class,
    GradeRepositoryMockWebServerTest::class,
    NotificationsRepositoryMockWebServerTest::class,
)
class AndroidJvmRegressionSuite
