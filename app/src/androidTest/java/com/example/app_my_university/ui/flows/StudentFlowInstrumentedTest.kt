package com.example.app_my_university.ui.flows

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.example.app_my_university.data.api.model.ChatContactResponse
import com.example.app_my_university.data.api.model.InAppNotificationResponse
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.app_my_university.ui.mockChatRepository
import com.example.app_my_university.ui.mockEducationRepository
import com.example.app_my_university.ui.mockGradeRepository
import com.example.app_my_university.ui.mockNotificationsRepository
import com.example.app_my_university.ui.mockProfileRepository
import com.example.app_my_university.ui.mockScheduleRepository
import com.example.app_my_university.ui.mockStatisticsRepository
import com.example.app_my_university.ui.mockThemePreferenceRepository
import com.example.app_my_university.ui.mockTokenManager
import com.example.app_my_university.ui.muSetContent
import com.example.app_my_university.ui.TestModelFixtures
import com.example.app_my_university.ui.navigation.AppRole
import com.example.app_my_university.ui.navigation.Screen
import com.example.app_my_university.ui.screens.ChatContactsScreen
import com.example.app_my_university.ui.screens.ChatScreen
import com.example.app_my_university.ui.screens.GradeBookScreen
import com.example.app_my_university.ui.screens.HomeScreen
import com.example.app_my_university.ui.screens.MessagesScreen
import com.example.app_my_university.ui.screens.NotificationsScreen
import com.example.app_my_university.ui.screens.ProfileScreen
import com.example.app_my_university.ui.screens.ScheduleScreen
import com.example.app_my_university.ui.screens.StudentPerformanceScreen
import com.example.app_my_university.ui.test.UiTestTags
import com.example.app_my_university.ui.viewmodel.ChatContactsViewModel
import com.example.app_my_university.ui.viewmodel.ChatViewModel
import com.example.app_my_university.ui.viewmodel.GradeBookViewModel
import com.example.app_my_university.ui.viewmodel.HomeDashboardViewModel
import com.example.app_my_university.ui.viewmodel.NotificationsViewModel
import com.example.app_my_university.ui.viewmodel.ProfileViewModel
import com.example.app_my_university.ui.viewmodel.ScheduleViewModel
import com.example.app_my_university.ui.viewmodel.StudentPerformanceViewModel
import io.mockk.coEvery
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StudentFlowInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun studentProfileVm() = ProfileViewModel(
        mockProfileRepository(TestModelFixtures.studentUserProfile()),
        mockThemePreferenceRepository(),
    )

    private fun studentDashboardVm(scheduleRepo: com.example.app_my_university.data.repository.ScheduleRepository = mockScheduleRepository()) =
        HomeDashboardViewModel(scheduleRepo, mockGradeRepository(), mockStatisticsRepository())

    @Test
    fun studentHome_showsScreenTagAndGreeting() {
        muSetContent(composeRule, AppRole.Student) {
            val nav = rememberNavController()
            NavHost(navController = nav, startDestination = Screen.StudentHome.route) {
                composable(Screen.StudentHome.route) {
                    HomeScreen(
                        navController = nav,
                        profileViewModel = studentProfileVm(),
                        dashboardViewModel = studentDashboardVm(),
                    )
                }
                composable(Screen.Schedule.route) {
                    Box(Modifier.fillMaxSize()) { Text("stub") }
                }
                composable(Screen.GradeBook.route) { Box(Modifier.fillMaxSize()) { Text("stub") } }
                composable(Screen.Dialogs.route) { Box(Modifier.fillMaxSize()) { Text("stub") } }
                composable(Screen.Profile.route) { Box(Modifier.fillMaxSize()) { Text("stub") } }
                composable(Screen.StudentPerformance.route) { Box(Modifier.fillMaxSize()) { Text("stub") } }
            }
        }
        composeRule.waitUntil(timeoutMillis = 15_000) {
            runCatching {
                composeRule.onNodeWithTag(UiTestTags.Screen.STUDENT_HOME).fetchSemanticsNode()
                true
            }.getOrDefault(false)
        }
        composeRule.onNodeWithTag(UiTestTags.Screen.STUDENT_HOME).assertIsDisplayed()
        composeRule.onNodeWithText("Студент Тестов", substring = true).assertIsDisplayed()
    }

    @Test
    fun studentHome_whenScheduleFails_showsErrorAndRetry() {
        val badSchedule = mockScheduleRepository()
        coEvery { badSchedule.getMySchedule(any(), any()) } returns Result.failure(Exception("offline"))
        muSetContent(composeRule, AppRole.Student) {
            val nav = rememberNavController()
            NavHost(navController = nav, startDestination = Screen.StudentHome.route) {
                composable(Screen.StudentHome.route) {
                    HomeScreen(
                        navController = nav,
                        profileViewModel = studentProfileVm(),
                        dashboardViewModel = studentDashboardVm(badSchedule),
                    )
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 15_000) {
            runCatching {
                composeRule.onNodeWithTag(UiTestTags.State.ERROR).fetchSemanticsNode()
                true
            }.getOrDefault(false)
        }
        composeRule.onNodeWithTag(UiTestTags.State.ERROR).assertIsDisplayed()
        composeRule.onNodeWithTag(UiTestTags.State.RETRY).performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun studentSchedule_showsScreenTag() {
        muSetContent(composeRule, AppRole.Student) {
            val nav = rememberNavController()
            NavHost(navController = nav, startDestination = Screen.Schedule.route) {
                composable(Screen.Schedule.route) {
                    ScheduleScreen(
                        navController = nav,
                        onNavigateBack = {},
                        viewModel = ScheduleViewModel(
                            mockScheduleRepository(),
                            mockEducationRepository(),
                            mockTokenManager("STUDENT", 10L),
                        ),
                    )
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 15_000) {
            runCatching {
                composeRule.onNodeWithTag(UiTestTags.Screen.SCHEDULE).fetchSemanticsNode()
                true
            }.getOrDefault(false)
        }
        composeRule.onNodeWithTag(UiTestTags.Screen.SCHEDULE).assertIsDisplayed()
        composeRule.onNodeWithText("Неделя 2").performClick()
        composeRule.waitForIdle()
    }

    /**
     * Инструментированно проверяем пустое состояние и действие «Обновить».
     * Сценарий с непустым списком оценок и фильтрами покрыт в [com.example.app_my_university.ui.viewmodel.GradeBookViewModelTest].
     */
    @Test
    fun gradeBook_showsScreenTagEmptyStateAndRefresh() {
        muSetContent(composeRule, AppRole.Student) {
            val nav = rememberNavController()
            NavHost(navController = nav, startDestination = Screen.GradeBook.route) {
                composable(Screen.GradeBook.route) {
                    GradeBookScreen(
                        navController = nav,
                        onNavigateBack = {},
                        viewModel = GradeBookViewModel(mockGradeRepository(), mockStatisticsRepository()),
                    )
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 20_000) {
            runCatching {
                composeRule.onNodeWithText("Зачётная книжка пуста", substring = true).fetchSemanticsNode()
                true
            }.getOrDefault(false)
        }
        composeRule.onNodeWithTag(UiTestTags.Screen.GRADEBOOK).assertIsDisplayed()
        composeRule.onNodeWithText("Зачётная книжка пуста", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("Обновить", substring = true).performClick()
        composeRule.waitForIdle()
    }

    /**
     * Content-сценарий без прокрутки LazyColumn: блок «Фактические результаты» виден сразу;
     * действие — обновление из top bar (поле поиска ниже по списку и может не попасть в viewport).
     * Фильтры и поиск — в [com.example.app_my_university.ui.viewmodel.GradeBookViewModelTest].
     */
    @Test
    fun gradeBook_withGrades_showsFactualSummaryAndRefresh() {
        val gradeRepo = mockGradeRepository()
        val sampleGrade = requireNotNull(TestModelFixtures.numericTeacherAssessment().finalGrade)
        coEvery { gradeRepo.getMyGrades() } returns Result.success(listOf(sampleGrade))
        muSetContent(composeRule, AppRole.Student) {
            val nav = rememberNavController()
            NavHost(navController = nav, startDestination = Screen.GradeBook.route) {
                composable(Screen.GradeBook.route) {
                    GradeBookScreen(
                        navController = nav,
                        onNavigateBack = {},
                        viewModel = GradeBookViewModel(gradeRepo, mockStatisticsRepository()),
                    )
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 25_000) {
            runCatching {
                composeRule.onNodeWithTag(UiTestTags.Screen.GRADEBOOK).fetchSemanticsNode()
                composeRule.onNodeWithText("Фактические результаты", substring = true).fetchSemanticsNode()
                true
            }.getOrDefault(false)
        }
        composeRule.onNodeWithTag(UiTestTags.Screen.GRADEBOOK).assertIsDisplayed()
        composeRule.onNodeWithText("Фактические результаты", substring = true).assertIsDisplayed()
        composeRule.onAllNodesWithText("Средний балл", substring = true).onFirst().assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Обновить").performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun studentPerformance_showsScreenTag() {
        muSetContent(composeRule, AppRole.Student) {
            val nav = rememberNavController()
            NavHost(navController = nav, startDestination = Screen.StudentPerformance.route) {
                composable(Screen.StudentPerformance.route) {
                    StudentPerformanceScreen(
                        navController = nav,
                        onNavigateBack = {},
                        viewModel = StudentPerformanceViewModel(mockStatisticsRepository()),
                    )
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 15_000) {
            runCatching {
                composeRule.onNodeWithTag(UiTestTags.Screen.STUDENT_PERFORMANCE).fetchSemanticsNode()
                true
            }.getOrDefault(false)
        }
        composeRule.onNodeWithTag(UiTestTags.Screen.STUDENT_PERFORMANCE).assertIsDisplayed()
        composeRule.onNodeWithText("Обновить", substring = true).performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun messages_showsScreenTagAndSearchField() {
        muSetContent(composeRule, AppRole.Student) {
            val nav = rememberNavController()
            NavHost(navController = nav, startDestination = Screen.Dialogs.route) {
                composable(Screen.Dialogs.route) {
                    MessagesScreen(
                        navController = nav,
                        onChatSelected = { _, _, _ -> },
                        onNavigateBack = {},
                        onStartNewChat = {},
                        viewModel = ChatViewModel(mockChatRepository(), mockTokenManager("STUDENT", 10L)),
                    )
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 15_000) {
            runCatching {
                composeRule.onNodeWithTag(UiTestTags.Screen.DIALOGS).fetchSemanticsNode()
                true
            }.getOrDefault(false)
        }
        composeRule.onNodeWithTag(UiTestTags.Screen.DIALOGS).assertIsDisplayed()
        composeRule.onNodeWithText("Поиск по имени или сообщению").assertIsDisplayed()
        composeRule.onNodeWithText("Поиск по имени или сообщению").performTextInput("т")
        composeRule.waitForIdle()
    }

    @Test
    fun chatContacts_showsScreenTag() {
        val chat = mockChatRepository()
        coEvery { chat.getChatContacts() } returns Result.success(
            listOf(
                ChatContactResponse(
                    id = 50L,
                    email = "peer@test.ru",
                    firstName = "Пётр",
                    lastName = "Собеседник",
                    middleName = null,
                    userType = "STUDENT",
                ),
            ),
        )
        muSetContent(composeRule, AppRole.Student) {
            val nav = rememberNavController()
            NavHost(navController = nav, startDestination = Screen.ChatContacts.route) {
                composable(Screen.ChatContacts.route) {
                    ChatContactsScreen(
                        navController = nav,
                        onNavigateBack = {},
                        onContactSelected = { _, _ -> },
                        viewModel = ChatContactsViewModel(chat),
                    )
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 15_000) {
            runCatching {
                composeRule.onNodeWithTag(UiTestTags.Screen.CHAT_CONTACTS).fetchSemanticsNode()
                true
            }.getOrDefault(false)
        }
        composeRule.onNodeWithTag(UiTestTags.Screen.CHAT_CONTACTS).assertIsDisplayed()
        composeRule.onNodeWithText("Собеседник", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("Поиск по имени или email").performTextInput("Пётр")
        composeRule.waitForIdle()
    }

    @Test
    fun chatNewConversation_showsInputAndScreenTag() {
        muSetContent(composeRule, AppRole.Student) {
            NavHost(
                navController = rememberNavController(),
                startDestination = "chat_new",
            ) {
                composable("chat_new") {
                    ChatScreen(
                        conversationId = "NEW",
                        participantName = "Контакт",
                        participantId = 99L,
                        onNavigateBack = {},
                        viewModel = ChatViewModel(mockChatRepository(), mockTokenManager("STUDENT", 10L)),
                    )
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 15_000) {
            runCatching {
                composeRule.onNodeWithTag(UiTestTags.Screen.CHAT).fetchSemanticsNode()
                true
            }.getOrDefault(false)
        }
        composeRule.onNodeWithTag(UiTestTags.Screen.CHAT).assertIsDisplayed()
        composeRule.onNodeWithText("Сообщение...").performTextInput("Тест")
        composeRule.waitForIdle()
    }

    @Test
    fun notifications_showsScreenTag() {
        val notifRepo = mockNotificationsRepository()
        coEvery { notifRepo.getMyNotifications() } returns Result.success(
            listOf(
                InAppNotificationResponse(
                    id = 1L,
                    kind = "INFO",
                    title = "Тестовое уведомление",
                    body = "Текст для UI-теста",
                    readAt = null,
                    createdAt = "2026-01-01T10:00:00",
                ),
            ),
        )
        muSetContent(composeRule, AppRole.Student) {
            val nav = rememberNavController()
            NavHost(navController = nav, startDestination = Screen.Notifications.route) {
                composable(Screen.Notifications.route) {
                    NotificationsScreen(
                        navController = nav,
                        onNavigateBack = {},
                        viewModel = NotificationsViewModel(notifRepo),
                    )
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 15_000) {
            runCatching {
                composeRule.onNodeWithTag(UiTestTags.Screen.NOTIFICATIONS).fetchSemanticsNode()
                true
            }.getOrDefault(false)
        }
        composeRule.onNodeWithTag(UiTestTags.Screen.NOTIFICATIONS).assertIsDisplayed()
        composeRule.onNodeWithText("Тестовое уведомление", substring = true).assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Прочитать все").performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun profile_showsScreenTag() {
        muSetContent(composeRule, AppRole.Student) {
            val nav = rememberNavController()
            NavHost(navController = nav, startDestination = Screen.Profile.route) {
                composable(Screen.Profile.route) {
                    ProfileScreen(
                        navController = nav,
                        onLogout = {},
                        onNavigateBack = {},
                        viewModel = studentProfileVm(),
                    )
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 15_000) {
            runCatching {
                composeRule.onNodeWithTag(UiTestTags.Screen.PROFILE).fetchSemanticsNode()
                true
            }.getOrDefault(false)
        }
        composeRule.onNodeWithTag(UiTestTags.Screen.PROFILE).assertIsDisplayed()
        composeRule.onNodeWithText("Светлая").performScrollTo().performClick()
        composeRule.waitForIdle()
    }
}
