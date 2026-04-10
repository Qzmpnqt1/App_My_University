package com.example.app_my_university.ui.flows

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.app_my_university.ui.mockGradeRepository
import com.example.app_my_university.ui.mockProfileRepository
import com.example.app_my_university.ui.mockScheduleRepository
import com.example.app_my_university.ui.mockStatisticsRepository
import com.example.app_my_university.ui.mockThemePreferenceRepository
import com.example.app_my_university.ui.muSetContent
import com.example.app_my_university.ui.navigation.AppRole
import com.example.app_my_university.ui.navigation.Screen
import com.example.app_my_university.ui.screens.TeacherHomeScreen
import com.example.app_my_university.ui.test.UiTestTags
import com.example.app_my_university.ui.TestModelFixtures
import com.example.app_my_university.ui.viewmodel.HomeDashboardViewModel
import com.example.app_my_university.ui.viewmodel.ProfileViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TeacherBottomNavInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun bottomNav_scheduleTab_navigatesToScheduleStub() {
        muSetContent(composeRule, AppRole.Teacher) {
            val nav = rememberNavController()
            NavHost(navController = nav, startDestination = Screen.TeacherHome.route) {
                composable(Screen.TeacherHome.route) {
                    TeacherHomeScreen(
                        navController = nav,
                        profileViewModel = ProfileViewModel(
                            mockProfileRepository(TestModelFixtures.teacherUserProfile()),
                            mockThemePreferenceRepository(),
                        ),
                        dashboardViewModel = HomeDashboardViewModel(
                            mockScheduleRepository(),
                            mockGradeRepository(),
                            mockStatisticsRepository(),
                        ),
                    )
                }
                composable(Screen.Schedule.route) {
                    Text("STUB_TEACHER_SCHEDULE_ROUTE")
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 20_000) {
            runCatching { composeRule.onNodeWithTag(UiTestTags.Screen.TEACHER_HOME).fetchSemanticsNode(); true }
                .getOrDefault(false)
        }
        composeRule.onNodeWithTag(UiTestTags.BottomNav.TEACHER_SCHEDULE).performClick()
        composeRule.waitUntil(timeoutMillis = 15_000) {
            runCatching { composeRule.onNodeWithText("STUB_TEACHER_SCHEDULE_ROUTE").fetchSemanticsNode(); true }
                .getOrDefault(false)
        }
        composeRule.onNodeWithText("STUB_TEACHER_SCHEDULE_ROUTE").assertIsDisplayed()
    }
}
