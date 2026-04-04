package com.example.app_my_university.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.app_my_university.ui.screens.*
import com.example.app_my_university.ui.viewmodel.LoginViewModel

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object UniversitySelection : Screen("university_selection")
    object Login : Screen("login")
    object Registration : Screen("registration")
    object StudentHome : Screen("student_home")
    object TeacherHome : Screen("teacher_home")
    object AdminHome : Screen("admin_home")
    object Profile : Screen("profile")
    object Schedule : Screen("schedule")
    object GradeBook : Screen("gradebook")
    object TeacherGrades : Screen("teacher_grades")
    object Dialogs : Screen("dialogs")
    object ChatContacts : Screen("chat_contacts")
    object Chat : Screen("chat/{conversationId}/{participantName}/{participantId}") {
        fun createRoute(conversationId: String, participantName: String, participantId: Long): String =
            "chat/$conversationId/${java.net.URLEncoder.encode(participantName, "UTF-8")}/$participantId"
    }
    object AdminRequests : Screen("admin_requests")
    object AdminUsers : Screen("admin_users")
    object AdminUniversities : Screen("admin_universities")
    object AdminSchedule : Screen("admin_schedule")
    object AdminSubjects : Screen("admin_subjects")
    object AdminGroups : Screen("admin_groups")
    object RegistrationStatus : Screen("registration_status")
    object AdminAudit : Screen("admin_audit")
    object AdminStatistics : Screen("admin_statistics")
    object StudentPerformance : Screen("student_performance")
    /** Хаб учебной структуры (вуз, группы, дисциплины, аудитории, назначения). */
    object AdminStructure : Screen("admin_structure")
    object AdminClassrooms : Screen("admin_classrooms")
    object AdminTeacherSubjects : Screen("admin_teacher_subjects")
    object AdminMore : Screen("admin_more")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val loginViewModel: LoginViewModel = hiltViewModel()
    val loginState by loginViewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route
    ) {
        composable(Screen.Welcome.route) {
            OnboardingWelcomeScreen(
                onNavigateToUniversitySelection = {
                    navController.navigate(Screen.UniversitySelection.route)
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.UniversitySelection.route) {
            UniversitySelectionScreen(
                onNavigateBack = { navController.navigateUp() },
                onContinueToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = loginViewModel,
                onNavigateToRegistration = {
                    navController.navigate(Screen.Registration.route)
                },
                onNavigateToRegistrationStatus = {
                    navController.navigate(Screen.RegistrationStatus.route)
                },
                onLoginSuccess = { userType ->
                    val dest = when (userType) {
                        "TEACHER" -> Screen.TeacherHome.route
                        "ADMIN" -> Screen.AdminHome.route
                        else -> Screen.StudentHome.route
                    }
                    navController.navigate(dest) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Registration.route) {
            RegistrationScreen(
                onNavigateBack = { navController.navigateUp() },
                onRegistrationSuccess = { navController.navigateUp() }
            )
        }

        composable(Screen.RegistrationStatus.route) {
            RegistrationStatusScreen(onNavigateBack = { navController.navigateUp() })
        }

        composable(Screen.StudentHome.route) {
            HomeScreen(navController = navController)
        }

        composable(Screen.TeacherHome.route) {
            TeacherHomeScreen(navController = navController)
        }

        composable(Screen.AdminHome.route) {
            AdminHomeScreen(navController = navController)
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onLogout = {
                    loginViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(Screen.Schedule.route) {
            ScheduleScreen(onNavigateBack = { navController.navigateUp() })
        }

        composable(Screen.GradeBook.route) {
            GradeBookScreen(onNavigateBack = { navController.navigateUp() })
        }

        composable(Screen.TeacherGrades.route) { backStackEntry ->
            TeacherGradesScreen(
                onNavigateBack = { navController.navigateUp() },
                viewModel = hiltViewModel(backStackEntry),
            )
        }

        composable(Screen.Dialogs.route) {
            MessagesScreen(
                onChatSelected = { conversationId, participantName, participantId ->
                    navController.navigate(Screen.Chat.createRoute(conversationId, participantName, participantId))
                },
                onNavigateBack = { navController.navigateUp() },
                onStartNewChat = { navController.navigate(Screen.ChatContacts.route) }
            )
        }

        composable(Screen.ChatContacts.route) {
            ChatContactsScreen(
                onNavigateBack = { navController.navigateUp() },
                onContactSelected = { userId, displayName ->
                    navController.navigate(Screen.Chat.createRoute("NEW", displayName, userId))
                }
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("conversationId") { type = NavType.StringType },
                navArgument("participantName") { type = NavType.StringType },
                navArgument("participantId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
            val participantName = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("participantName") ?: "", "UTF-8"
            )
            val participantId = backStackEntry.arguments?.getLong("participantId") ?: 0L
            ChatScreen(
                conversationId = conversationId,
                participantName = participantName,
                participantId = participantId,
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(Screen.AdminRequests.route) {
            RegistrationRequestsScreen(
                navController = navController,
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(Screen.AdminUsers.route) {
            UserManagementScreen(onNavigateBack = { navController.navigateUp() })
        }

        composable(Screen.AdminUniversities.route) {
            UniversityManagementScreen(onNavigateBack = { navController.navigateUp() })
        }

        composable(Screen.AdminSchedule.route) {
            ScheduleManagementScreen(
                navController = navController,
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(Screen.AdminStructure.route) {
            AdminStructureHubScreen(navController = navController)
        }

        composable(Screen.AdminClassrooms.route) {
            AdminClassroomManagementScreen(onNavigateBack = { navController.navigateUp() })
        }

        composable(Screen.AdminTeacherSubjects.route) { backStackEntry ->
            AdminTeacherAssignmentScreen(
                onNavigateBack = { navController.navigateUp() },
                viewModel = hiltViewModel(backStackEntry),
            )
        }

        composable(Screen.AdminMore.route) {
            AdminMoreScreen(navController = navController)
        }

        composable(Screen.AdminSubjects.route) {
            SubjectManagementScreen(onNavigateBack = { navController.navigateUp() })
        }

        composable(Screen.AdminGroups.route) {
            GroupManagementScreen(onNavigateBack = { navController.navigateUp() })
        }

        composable(Screen.AdminAudit.route) {
            AdminAuditLogsScreen(onNavigateBack = { navController.navigateUp() })
        }

        composable(Screen.AdminStatistics.route) {
            AdminStatisticsScreen(onNavigateBack = { navController.navigateUp() })
        }

        composable(Screen.StudentPerformance.route) {
            StudentPerformanceScreen(onNavigateBack = { navController.navigateUp() })
        }
    }

    LaunchedEffect(loginState.isLoggedIn, loginState.userType) {
        if (loginState.isLoggedIn && loginState.userType != null) {
            val currentRoute = navController.currentDestination?.route
            val atAuthFlow = currentRoute == Screen.Login.route ||
                currentRoute == Screen.Welcome.route ||
                currentRoute == Screen.UniversitySelection.route ||
                currentRoute == Screen.Registration.route
            if (atAuthFlow) {
                val dest = when (loginState.userType) {
                    "TEACHER" -> Screen.TeacherHome.route
                    "ADMIN" -> Screen.AdminHome.route
                    else -> Screen.StudentHome.route
                }
                navController.navigate(dest) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }
}
