package com.example.app_my_university.ui

import com.example.app_my_university.ExampleInstrumentedTest
import com.example.app_my_university.ui.flows.AuthFlowInstrumentedTest
import com.example.app_my_university.ui.flows.ModalSheetsInstrumentedTest
import com.example.app_my_university.ui.flows.StudentBottomNavInstrumentedTest
import com.example.app_my_university.ui.flows.StudentFlowInstrumentedTest
import com.example.app_my_university.ui.flows.TeacherAndAdminFlowInstrumentedTest
import com.example.app_my_university.ui.screens.LoginScreenInstrumentedTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Полный прогон всех UI-классов из этого пакета (full UI suite).
 * См. также [MuUiSmokeSuite], [MuUiRegressionSuite].
 *
 * `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.app_my_university.ui.MuUiInstrumentationSuite`
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    ExampleInstrumentedTest::class,
    LoginScreenInstrumentedTest::class,
    AuthFlowInstrumentedTest::class,
    StudentFlowInstrumentedTest::class,
    StudentBottomNavInstrumentedTest::class,
    TeacherAndAdminFlowInstrumentedTest::class,
    ModalSheetsInstrumentedTest::class,
)
class MuUiInstrumentationSuite
