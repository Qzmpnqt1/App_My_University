package com.example.app_my_university.ui.flows

import androidx.activity.ComponentActivity
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
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.app_my_university.ui.adminViewModelCampusAdmin
import com.example.app_my_university.ui.adminViewModelSuperAdmin
import com.example.app_my_university.ui.adminViewModelSuperAdminScoped
import com.example.app_my_university.ui.mockChatRepository
import com.example.app_my_university.ui.mockAuditRepository
import com.example.app_my_university.ui.mockEducationRepository
import com.example.app_my_university.ui.mockGradeRepository
import com.example.app_my_university.ui.mockProfileRepository
import com.example.app_my_university.ui.mockScheduleRepository
import com.example.app_my_university.ui.mockStatisticsRepository
import com.example.app_my_university.ui.mockThemePreferenceRepository
import com.example.app_my_university.ui.mockTokenManager
import com.example.app_my_university.ui.muSetContent
import com.example.app_my_university.ui.navigation.AppRole
import com.example.app_my_university.ui.navigation.Screen
import com.example.app_my_university.ui.screens.AdminAuditLogsScreen
import com.example.app_my_university.ui.screens.AdminClassroomManagementScreen
import com.example.app_my_university.ui.screens.AdminHomeScreen
import com.example.app_my_university.ui.screens.AdminMoreScreen
import com.example.app_my_university.ui.screens.AdminStatisticsScreen
import com.example.app_my_university.ui.screens.AdminStructureHubScreen
import com.example.app_my_university.ui.screens.AdminTeacherAssignmentScreen
import com.example.app_my_university.ui.screens.DirectionManagementScreen
import com.example.app_my_university.ui.screens.GroupManagementScreen
import com.example.app_my_university.ui.screens.RegistrationRequestsScreen
import com.example.app_my_university.ui.screens.ScheduleManagementScreen
import com.example.app_my_university.ui.screens.SubjectInDirectionManagementScreen
import com.example.app_my_university.ui.screens.SubjectManagementScreen
import com.example.app_my_university.ui.screens.TeacherGradesScreen
import com.example.app_my_university.ui.screens.TeacherHomeScreen
import com.example.app_my_university.ui.screens.TeacherStatisticsScreen
import com.example.app_my_university.ui.screens.UniversityInstitutesScreen
import com.example.app_my_university.ui.screens.UniversityManagementScreen
import com.example.app_my_university.ui.screens.UserManagementScreen
import com.example.app_my_university.ui.test.UiTestTags
import com.example.app_my_university.ui.viewmodel.AdminAuditViewModel
import com.example.app_my_university.ui.viewmodel.AdminStatisticsViewModel
import com.example.app_my_university.ui.viewmodel.AdminTeacherAssignmentViewModel
import com.example.app_my_university.ui.viewmodel.HomeDashboardViewModel
import com.example.app_my_university.ui.viewmodel.ProfileViewModel
import com.example.app_my_university.ui.viewmodel.ScheduleViewModel
import com.example.app_my_university.ui.viewmodel.TeacherGradingViewModel
import com.example.app_my_university.ui.viewmodel.TeacherStatisticsViewModel
import com.example.app_my_university.ui.viewmodel.UniversityInstitutesViewModel
import com.example.app_my_university.ui.viewmodel.AdminViewModel
import com.example.app_my_university.ui.TestModelFixtures
import com.example.app_my_university.data.api.model.RegistrationRequestResponse
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.mockk.coEvery
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TeacherAndAdminFlowInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun teacherProfileVm() = ProfileViewModel(
        mockProfileRepository(TestModelFixtures.teacherUserProfile()),
        mockThemePreferenceRepository(),
    )

    @Test
    fun teacherHome_showsTag() {
        muSetContent(composeRule, AppRole.Teacher) {
            val nav = rememberNavController()
            NavHost(nav, Screen.TeacherHome.route) {
                composable(Screen.TeacherHome.route) {
                    TeacherHomeScreen(
                        navController = nav,
                        profileViewModel = teacherProfileVm(),
                        dashboardViewModel = HomeDashboardViewModel(
                            mockScheduleRepository(),
                            mockGradeRepository(),
                            mockStatisticsRepository(),
                        ),
                    )
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 20_000) {
            runCatching { composeRule.onNodeWithTag(UiTestTags.Screen.TEACHER_HOME).fetchSemanticsNode(); true }
                .getOrDefault(false)
        }
        composeRule.onNodeWithTag(UiTestTags.Screen.TEACHER_HOME).assertIsDisplayed()
    }

    @Test
    fun teacherGrades_showsTag() {
        muSetContent(composeRule, AppRole.Teacher) {
            val nav = rememberNavController()
            NavHost(nav, Screen.TeacherGrades.route) {
                composable(Screen.TeacherGrades.route) {
                    TeacherGradesScreen(
                        navController = nav,
                        onNavigateBack = {},
                        viewModel = TeacherGradingViewModel(mockGradeRepository()),
                    )
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 20_000) {
            runCatching { composeRule.onNodeWithTag(UiTestTags.Screen.TEACHER_GRADES).fetchSemanticsNode(); true }
                .getOrDefault(false)
        }
        composeRule.onNodeWithTag(UiTestTags.Screen.TEACHER_GRADES).assertIsDisplayed()
        composeRule.onNodeWithText("ИИТ", substring = true).performScrollTo().performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun teacherStatistics_showsTag() {
        muSetContent(composeRule, AppRole.Teacher) {
            val nav = rememberNavController()
            NavHost(nav, Screen.TeacherStatistics.route) {
                composable(Screen.TeacherStatistics.route) {
                    TeacherStatisticsScreen(
                        navController = nav,
                        onNavigateBack = {},
                        viewModel = TeacherStatisticsViewModel(
                            mockProfileRepository(TestModelFixtures.teacherUserProfile()),
                            mockGradeRepository(),
                            mockStatisticsRepository(),
                        ),
                    )
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 20_000) {
            runCatching { composeRule.onNodeWithTag(UiTestTags.Screen.TEACHER_STATISTICS).fetchSemanticsNode(); true }
                .getOrDefault(false)
        }
        composeRule.onNodeWithTag(UiTestTags.Screen.TEACHER_STATISTICS).assertIsDisplayed()
        composeRule.onNodeWithText("Дисциплина", substring = true).performScrollTo().performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun adminHome_showsTag() {
        muSetContent(composeRule, AppRole.Admin) {
            val nav = rememberNavController()
            NavHost(nav, Screen.AdminHome.route) {
                composable(Screen.AdminHome.route) {
                    AdminHomeScreen(navController = nav, adminViewModel = adminViewModelCampusAdmin())
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 20_000) {
            runCatching { composeRule.onNodeWithTag(UiTestTags.Screen.ADMIN_HOME).fetchSemanticsNode(); true }
                .getOrDefault(false)
        }
        composeRule.onNodeWithTag(UiTestTags.Screen.ADMIN_HOME).assertIsDisplayed()
        composeRule.onAllNodesWithText("Тестовый вуз", substring = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun adminHome_superAdminWithoutScope_showsAllUniversitiesLabel() {
        muSetContent(composeRule, AppRole.Admin) {
            val nav = rememberNavController()
            NavHost(nav, Screen.AdminHome.route) {
                composable(Screen.AdminHome.route) {
                    AdminHomeScreen(navController = nav, adminViewModel = adminViewModelSuperAdmin())
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 20_000) {
            runCatching { composeRule.onNodeWithTag(UiTestTags.Screen.ADMIN_HOME).fetchSemanticsNode(); true }
                .getOrDefault(false)
        }
        composeRule.onNodeWithTag(UiTestTags.Screen.ADMIN_HOME).assertIsDisplayed()
        composeRule.onAllNodesWithText("Все вузы", substring = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun adminHome_superAdminWithScope_showsUniversityNameNotGlobalLabel() {
        muSetContent(composeRule, AppRole.Admin) {
            val nav = rememberNavController()
            NavHost(nav, Screen.AdminHome.route) {
                composable(Screen.AdminHome.route) {
                    AdminHomeScreen(navController = nav, adminViewModel = adminViewModelSuperAdminScoped(1L))
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 20_000) {
            runCatching { composeRule.onNodeWithTag(UiTestTags.Screen.ADMIN_HOME).fetchSemanticsNode(); true }
                .getOrDefault(false)
        }
        composeRule.onNodeWithTag(UiTestTags.Screen.ADMIN_HOME).assertIsDisplayed()
        composeRule.onAllNodesWithText("Тестовый вуз", substring = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun adminStructureHub_showsTag() {
        muSetContent(composeRule, AppRole.Admin) {
            val nav = rememberNavController()
            NavHost(nav, Screen.AdminStructure.route) {
                composable(Screen.AdminStructure.route) {
                    AdminStructureHubScreen(navController = nav)
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 15_000) {
            runCatching { composeRule.onNodeWithTag(UiTestTags.Screen.ADMIN_STRUCTURE).fetchSemanticsNode(); true }
                .getOrDefault(false)
        }
        composeRule.onNodeWithTag(UiTestTags.Screen.ADMIN_STRUCTURE).assertIsDisplayed()
        composeRule.onNodeWithText("Вуз и институты").assertIsDisplayed()
        composeRule.onNodeWithText("Преподаватели и дисциплины", substring = true).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun adminMore_showsTag() {
        muSetContent(composeRule, AppRole.Admin) {
            val nav = rememberNavController()
            NavHost(nav, Screen.AdminMore.route) {
                composable(Screen.AdminMore.route) {
                    AdminMoreScreen(navController = nav)
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 15_000) {
            runCatching { composeRule.onNodeWithTag(UiTestTags.Screen.ADMIN_MORE).fetchSemanticsNode(); true }
                .getOrDefault(false)
        }
        composeRule.onNodeWithTag(UiTestTags.Screen.ADMIN_MORE).assertIsDisplayed()
        composeRule.onNodeWithText("Профиль").assertIsDisplayed()
        composeRule.onNodeWithText("Аудит", substring = true).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun adminRequests_showsTag() {
        val edu = mockEducationRepository()
        // EducationRepository.getRegistrationRequests(status, userType, universityId, instituteId) — нужен полный матч,
        // иначе срабатывает coEvery из mockEducationRepository() и остаётся пустой список.
        coEvery { edu.getRegistrationRequests(any(), any(), any(), any()) } returns Result.success(
            listOf(
                RegistrationRequestResponse(
                    id = 1L,
                    email = "req@test.ru",
                    firstName = "Новый",
                    lastName = "Пользователь",
                    middleName = null,
                    userType = "STUDENT",
                    status = "PENDING",
                    rejectionReason = null,
                    universityId = 1L,
                    universityName = "Тестовый вуз",
                    groupId = null,
                    groupName = null,
                    instituteId = null,
                    instituteName = null,
                    createdAt = "2026-01-01T10:00:00",
                ),
            ),
        )
        val vm = AdminViewModel(
            edu,
            mockChatRepository(),
            mockProfileRepository(TestModelFixtures.adminUserProfile()),
            mockTokenManager("ADMIN", 1L, null),
        )
        muSetContent(composeRule, AppRole.Admin) {
            val nav = rememberNavController()
            NavHost(nav, Screen.AdminRequests.route) {
                composable(Screen.AdminRequests.route) {
                    RegistrationRequestsScreen(
                        navController = nav,
                        onNavigateBack = {},
                        viewModel = vm,
                    )
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 20_000) {
            runCatching { composeRule.onNodeWithTag(UiTestTags.Screen.ADMIN_REQUESTS).fetchSemanticsNode(); true }
                .getOrDefault(false)
        }
        composeRule.onNodeWithTag(UiTestTags.Screen.ADMIN_REQUESTS).assertIsDisplayed()
        composeRule.waitUntil(timeoutMillis = 20_000) {
            runCatching { composeRule.onNodeWithText("Пользователь", substring = true).fetchSemanticsNode(); true }
                .getOrDefault(false)
        }
        composeRule.onAllNodesWithText("На рассмотрении", substring = true).onFirst().performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun adminUsers_showsTag() {
        muSetContent(composeRule, AppRole.Admin) {
            val nav = rememberNavController()
            NavHost(nav, Screen.AdminUsers.route) {
                composable(Screen.AdminUsers.route) {
                    UserManagementScreen(
                        navController = nav,
                        onNavigateBack = {},
                        viewModel = adminViewModelCampusAdmin(),
                    )
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 20_000) {
            runCatching { composeRule.onNodeWithTag(UiTestTags.Screen.ADMIN_USERS).fetchSemanticsNode(); true }
                .getOrDefault(false)
        }
        composeRule.onNodeWithTag(UiTestTags.Screen.ADMIN_USERS).assertIsDisplayed()
        composeRule.onNodeWithText("Имя или email").performTextInput("test")
        composeRule.waitForIdle()
    }

    @Test
    fun adminUsers_superAdmin_canSelectAdminsFilterTab() {
        muSetContent(composeRule, AppRole.Admin) {
            val nav = rememberNavController()
            NavHost(nav, Screen.AdminUsers.route) {
                composable(Screen.AdminUsers.route) {
                    UserManagementScreen(
                        navController = nav,
                        onNavigateBack = {},
                        viewModel = adminViewModelSuperAdmin(),
                    )
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 20_000) {
            runCatching { composeRule.onNodeWithTag(UiTestTags.Screen.ADMIN_USERS).fetchSemanticsNode(); true }
                .getOrDefault(false)
        }
        composeRule.onNodeWithText("Админы", substring = true).performScrollTo().performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun adminUniversities_superAdmin_showsTag() {
        muSetContent(composeRule, AppRole.Admin) {
            val nav = rememberNavController()
            NavHost(nav, Screen.AdminUniversities.route) {
                composable(Screen.AdminUniversities.route) {
                    UniversityManagementScreen(
                        navController = nav,
                        onNavigateBack = {},
                        viewModel = adminViewModelSuperAdmin(),
                    )
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 20_000) {
            runCatching { composeRule.onNodeWithTag(UiTestTags.Screen.ADMIN_UNIVERSITIES).fetchSemanticsNode(); true }
                .getOrDefault(false)
        }
        composeRule.onNodeWithTag(UiTestTags.Screen.ADMIN_UNIVERSITIES).assertIsDisplayed()
        composeRule.onNodeWithText("Поиск по названию, сокращению, городу").performTextInput("Тест")
        composeRule.waitForIdle()
    }

    @Test
    fun adminUniversities_superAdmin_opensCreateDialogAndDismisses() {
        muSetContent(composeRule, AppRole.Admin) {
            val nav = rememberNavController()
            NavHost(nav, Screen.AdminUniversities.route) {
                composable(Screen.AdminUniversities.route) {
                    UniversityManagementScreen(
                        navController = nav,
                        onNavigateBack = {},
                        viewModel = adminViewModelSuperAdmin(),
                    )
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 20_000) {
            runCatching { composeRule.onNodeWithTag(UiTestTags.Screen.ADMIN_UNIVERSITIES).fetchSemanticsNode(); true }
                .getOrDefault(false)
        }
        composeRule.onNodeWithContentDescription("Создать вуз").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("Новый вуз", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onAllNodesWithText("Отмена", substring = true).onFirst().performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun adminUniversityInstitutes_showsTag() {
        muSetContent(composeRule, AppRole.Admin) {
            val nav = rememberNavController()
            NavHost(nav, "uni_inst_test") {
                composable("uni_inst_test") {
                    UniversityInstitutesScreen(
                        navController = nav,
                        universityId = 1L,
                        onNavigateBack = {},
                        viewModel = UniversityInstitutesViewModel(mockEducationRepository()),
                    )
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 20_000) {
            runCatching { composeRule.onNodeWithTag(UiTestTags.Screen.ADMIN_UNIVERSITY_INSTITUTES).fetchSemanticsNode(); true }
                .getOrDefault(false)
        }
        composeRule.onNodeWithTag(UiTestTags.Screen.ADMIN_UNIVERSITY_INSTITUTES).assertIsDisplayed()
        composeRule.onNodeWithText("Поиск по институтам").performTextInput("Ин")
        composeRule.waitForIdle()
    }

    @Test
    fun adminScheduleManagement_showsTag() {
        muSetContent(composeRule, AppRole.Admin) {
            val nav = rememberNavController()
            NavHost(nav, Screen.AdminSchedule.route) {
                composable(Screen.AdminSchedule.route) {
                    ScheduleManagementScreen(
                        navController = nav,
                        onNavigateBack = {},
                        viewModel = ScheduleViewModel(
                            mockScheduleRepository(),
                            mockEducationRepository(),
                            mockTokenManager("ADMIN", 1L),
                        ),
                        adminViewModel = adminViewModelCampusAdmin(),
                    )
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 25_000) {
            runCatching { composeRule.onNodeWithTag(UiTestTags.Screen.ADMIN_SCHEDULE).fetchSemanticsNode(); true }
                .getOrDefault(false)
        }
        composeRule.onNodeWithTag(UiTestTags.Screen.ADMIN_SCHEDULE).assertIsDisplayed()
        composeRule.onNodeWithText("Неделя 2", substring = true).performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun adminClassrooms_showsTag() {
        muSetContent(composeRule, AppRole.Admin) {
            val nav = rememberNavController()
            NavHost(nav, Screen.AdminClassrooms.route) {
                composable(Screen.AdminClassrooms.route) {
                    AdminClassroomManagementScreen(
                        navController = nav,
                        onNavigateBack = {},
                        viewModel = adminViewModelCampusAdmin(),
                    )
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 20_000) {
            runCatching { composeRule.onNodeWithTag(UiTestTags.Screen.ADMIN_CLASSROOMS).fetchSemanticsNode(); true }
                .getOrDefault(false)
        }
        composeRule.onNodeWithTag(UiTestTags.Screen.ADMIN_CLASSROOMS).assertIsDisplayed()
        composeRule.onNodeWithText("Поиск по корпусу или номеру").performTextInput("1")
        composeRule.waitForIdle()
    }

    @Test
    fun adminTeacherAssignment_showsTag() {
        muSetContent(composeRule, AppRole.Admin) {
            val nav = rememberNavController()
            NavHost(nav, Screen.AdminTeacherSubjects.route) {
                composable(Screen.AdminTeacherSubjects.route) {
                    AdminTeacherAssignmentScreen(
                        navController = nav,
                        onNavigateBack = {},
                        viewModel = AdminTeacherAssignmentViewModel(
                            mockEducationRepository(),
                            mockProfileRepository(TestModelFixtures.adminUserProfile()),
                            mockTokenManager("ADMIN", 1L),
                        ),
                    )
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 25_000) {
            runCatching { composeRule.onNodeWithTag(UiTestTags.Screen.ADMIN_TEACHER_SUBJECTS).fetchSemanticsNode(); true }
                .getOrDefault(false)
        }
        composeRule.onNodeWithTag(UiTestTags.Screen.ADMIN_TEACHER_SUBJECTS).assertIsDisplayed()
        composeRule.onNodeWithText("Поиск преподавателя").performTextInput("Пре")
        composeRule.waitForIdle()
    }

    @Test
    fun adminSubjects_showsTag() {
        muSetContent(composeRule, AppRole.Admin) {
            val nav = rememberNavController()
            NavHost(nav, Screen.AdminSubjects.route) {
                composable(Screen.AdminSubjects.route) {
                    SubjectManagementScreen(
                        navController = nav,
                        onNavigateBack = {},
                        viewModel = adminViewModelCampusAdmin(),
                    )
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 20_000) {
            runCatching { composeRule.onNodeWithTag(UiTestTags.Screen.ADMIN_SUBJECTS).fetchSemanticsNode(); true }
                .getOrDefault(false)
        }
        composeRule.onNodeWithTag(UiTestTags.Screen.ADMIN_SUBJECTS).assertIsDisplayed()
        composeRule.onNodeWithText("Поиск по названию").performTextInput("Дис")
        composeRule.waitForIdle()
    }

    @Test
    fun adminSubjectPlan_showsTag() {
        muSetContent(composeRule, AppRole.Admin) {
            val nav = rememberNavController()
            NavHost(nav, Screen.AdminSubjectPlan.route) {
                composable(Screen.AdminSubjectPlan.route) {
                    SubjectInDirectionManagementScreen(
                        navController = nav,
                        onNavigateBack = {},
                        viewModel = adminViewModelCampusAdmin(),
                    )
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 20_000) {
            runCatching { composeRule.onNodeWithTag(UiTestTags.Screen.ADMIN_SUBJECT_PLAN).fetchSemanticsNode(); true }
                .getOrDefault(false)
        }
        composeRule.onNodeWithTag(UiTestTags.Screen.ADMIN_SUBJECT_PLAN).assertIsDisplayed()
        composeRule.onAllNodesWithText("Поиск").onFirst().performTextInput("пр")
        composeRule.waitForIdle()
    }

    @Test
    fun adminDirections_showsTag() {
        muSetContent(composeRule, AppRole.Admin) {
            val nav = rememberNavController()
            NavHost(nav, Screen.AdminDirections.route) {
                composable(Screen.AdminDirections.route) {
                    DirectionManagementScreen(
                        navController = nav,
                        onNavigateBack = {},
                        viewModel = adminViewModelCampusAdmin(),
                    )
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 20_000) {
            runCatching { composeRule.onNodeWithTag(UiTestTags.Screen.ADMIN_DIRECTIONS).fetchSemanticsNode(); true }
                .getOrDefault(false)
        }
        composeRule.onNodeWithTag(UiTestTags.Screen.ADMIN_DIRECTIONS).assertIsDisplayed()
        composeRule.onNodeWithText("Поиск").performTextInput("и")
        composeRule.waitForIdle()
    }

    @Test
    fun adminGroups_showsTag() {
        muSetContent(composeRule, AppRole.Admin) {
            val nav = rememberNavController()
            NavHost(nav, Screen.AdminGroups.route) {
                composable(Screen.AdminGroups.route) {
                    GroupManagementScreen(
                        navController = nav,
                        onNavigateBack = {},
                        viewModel = adminViewModelCampusAdmin(),
                    )
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 20_000) {
            runCatching { composeRule.onNodeWithTag(UiTestTags.Screen.ADMIN_GROUPS).fetchSemanticsNode(); true }
                .getOrDefault(false)
        }
        composeRule.onNodeWithTag(UiTestTags.Screen.ADMIN_GROUPS).assertIsDisplayed()
        composeRule.onNodeWithText("Поиск по группе или направлению").performTextInput("ИВТ")
        composeRule.waitForIdle()
    }

    @Test
    fun adminAudit_showsTag() {
        muSetContent(composeRule, AppRole.Admin) {
            val nav = rememberNavController()
            NavHost(nav, Screen.AdminAudit.route) {
                composable(Screen.AdminAudit.route) {
                    AdminAuditLogsScreen(
                        navController = nav,
                        onNavigateBack = {},
                        viewModel = AdminAuditViewModel(mockAuditRepository(), mockTokenManager("ADMIN", 1L)),
                    )
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 20_000) {
            runCatching { composeRule.onNodeWithTag(UiTestTags.Screen.ADMIN_AUDIT).fetchSemanticsNode(); true }
                .getOrDefault(false)
        }
        composeRule.onNodeWithTag(UiTestTags.Screen.ADMIN_AUDIT).assertIsDisplayed()
        composeRule.onAllNodesWithText("Применить фильтр", substring = true).onFirst().performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun adminStatistics_showsTag() {
        muSetContent(composeRule, AppRole.Admin) {
            val nav = rememberNavController()
            NavHost(nav, Screen.AdminStatistics.route) {
                composable(Screen.AdminStatistics.route) {
                    AdminStatisticsScreen(
                        navController = nav,
                        onNavigateBack = {},
                        viewModel = AdminStatisticsViewModel(
                            mockStatisticsRepository(),
                            mockEducationRepository(),
                            mockProfileRepository(TestModelFixtures.adminUserProfile()),
                            mockTokenManager("ADMIN", 1L),
                        ),
                    )
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 25_000) {
            runCatching { composeRule.onNodeWithTag(UiTestTags.Screen.ADMIN_STATISTICS).fetchSemanticsNode(); true }
                .getOrDefault(false)
        }
        composeRule.onNodeWithTag(UiTestTags.Screen.ADMIN_STATISTICS).assertIsDisplayed()
        composeRule.onNodeWithText("Аналитика").assertIsDisplayed()
        composeRule.onNodeWithTag("${UiTestTags.AdminStat.TAB_PREFIX}UNIVERSITY").performScrollTo().performClick()
        composeRule.waitUntil(timeoutMillis = 20_000) {
            runCatching {
                composeRule.onNodeWithText("Загрузить статистику вуза", substring = false).fetchSemanticsNode()
                true
            }.getOrDefault(false)
        }
        composeRule.onNodeWithText("Загрузить статистику вуза", substring = false).performScrollTo().performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun adminStatistics_superAdminGlobal_universityTabShowsScopeHint() {
        muSetContent(composeRule, AppRole.Admin) {
            val nav = rememberNavController()
            NavHost(nav, Screen.AdminStatistics.route) {
                composable(Screen.AdminStatistics.route) {
                    AdminStatisticsScreen(
                        navController = nav,
                        onNavigateBack = {},
                        viewModel = AdminStatisticsViewModel(
                            mockStatisticsRepository(),
                            mockEducationRepository(),
                            mockProfileRepository(TestModelFixtures.superAdminUserProfile()),
                            mockTokenManager("SUPER_ADMIN", 2L, null),
                        ),
                    )
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 25_000) {
            runCatching { composeRule.onNodeWithTag(UiTestTags.Screen.ADMIN_STATISTICS).fetchSemanticsNode(); true }
                .getOrDefault(false)
        }
        composeRule.onNodeWithTag("${UiTestTags.AdminStat.TAB_PREFIX}UNIVERSITY").performScrollTo().performClick()
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodesWithText("выберите вуз на главной", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("выберите вуз на главной", substring = true).assertIsDisplayed()
    }
}
