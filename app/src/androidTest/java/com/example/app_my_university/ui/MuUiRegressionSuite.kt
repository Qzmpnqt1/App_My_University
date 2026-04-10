package com.example.app_my_university.ui

import com.example.app_my_university.ui.flows.AuthFlowInstrumentedTest
import com.example.app_my_university.ui.flows.ModalSheetsInstrumentedTest
import com.example.app_my_university.ui.flows.StudentFlowInstrumentedTest
import com.example.app_my_university.ui.flows.TeacherAndAdminFlowInstrumentedTest
import com.example.app_my_university.ui.flows.TeacherBottomNavInstrumentedTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Регрессия по основным Compose-flow (без дублирования smoke-классов).
 *
 * Предпочтительно: `./gradlew :app:connectedRegressionDebugAndroidTest --no-daemon`
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    AuthFlowInstrumentedTest::class,
    StudentFlowInstrumentedTest::class,
    TeacherAndAdminFlowInstrumentedTest::class,
    TeacherBottomNavInstrumentedTest::class,
    ModalSheetsInstrumentedTest::class,
)
class MuUiRegressionSuite
