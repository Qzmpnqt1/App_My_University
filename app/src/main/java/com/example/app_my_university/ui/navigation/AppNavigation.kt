package com.example.app_my_university.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.app_my_university.ui.components.AdminBottomBar
import com.example.app_my_university.ui.components.StudentBottomBar
import com.example.app_my_university.ui.components.TeacherBottomBar
import com.example.app_my_university.ui.screens.AdminDashboardScreen
import com.example.app_my_university.ui.screens.ChatScreen
import com.example.app_my_university.ui.screens.GradeBookScreen
import com.example.app_my_university.ui.screens.LoginScreen
import com.example.app_my_university.ui.screens.MessagesScreen
import com.example.app_my_university.ui.screens.OnboardingWelcomeScreen
import com.example.app_my_university.ui.screens.ProfileScreen
import com.example.app_my_university.ui.screens.RegistrationRequestsScreen
import com.example.app_my_university.ui.screens.RegistrationScreen
import com.example.app_my_university.ui.screens.ScheduleManagementScreen
import com.example.app_my_university.ui.screens.ScheduleScreen
import com.example.app_my_university.ui.screens.TeacherGradesScreen
import com.example.app_my_university.ui.screens.UniversityManagementScreen
import com.example.app_my_university.ui.screens.UniversitySelectionScreen

// Define the navigation routes
sealed class Screen(val route: String) {
    object OnboardingWelcome : Screen("onboarding_welcome")
    object UniversitySelection : Screen("university_selection")
    object Login : Screen("login/{universityId}/{universityName}") {
        fun createRoute(universityId: String, universityName: String): String =
            "login/$universityId/$universityName"
    }
    object Registration : Screen("registration/{universityId}/{universityName}") {
        fun createRoute(universityId: String, universityName: String): String =
            "registration/$universityId/$universityName"
    }
    object Home : Screen("home")
    object Schedule : Screen("schedule")
    object GradeBook : Screen("gradebook")
    object News : Screen("news")
    object Messages : Screen("messages")
    object Chat : Screen("chat/{chatId}") {
        fun createRoute(chatId: String): String = "chat/$chatId"
    }
    object Profile : Screen("profile")

    // Экраны преподавателя
    object TeacherSchedule : Screen("teacher_schedule")
    object TeacherGrades : Screen("teacher_grades")
    object TeacherMessages : Screen("teacher_messages")
    object TeacherProfile : Screen("teacher_profile")

    // Экраны администратора
    object AdminDashboard : Screen("admin_dashboard")
    object UniversityManagement : Screen("university_management")
    object RegistrationRequests : Screen("registration_requests")
    object ScheduleManagement : Screen("schedule_management")
    object SubjectManagement : Screen("subject_management")
    object UserManagement : Screen("user_management")
    object AdminProfile : Screen("admin_profile")
}

@Composable
fun AppNavigation(navController: NavHostController, startDestination: String) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""

    // Проверка, является ли текущий экран экраном администратора
    val isAdminScreen = currentRoute.startsWith("admin_") ||
            currentRoute == Screen.UniversityManagement.route ||
            currentRoute == Screen.RegistrationRequests.route ||
            currentRoute == Screen.ScheduleManagement.route ||
            currentRoute == Screen.SubjectManagement.route ||
            currentRoute == Screen.UserManagement.route ||
            (currentRoute == Screen.Messages.route && startDestination == Screen.AdminDashboard.route)

    // Проверка, является ли текущий экран экраном студента
    val isStudentScreen = currentRoute == Screen.Schedule.route ||
            currentRoute == Screen.GradeBook.route ||
            currentRoute == Screen.Messages.route ||
            currentRoute == Screen.Profile.route

    // Проверка, является ли текущий экран экраном преподавателя
    val isTeacherScreen = currentRoute == Screen.TeacherSchedule.route ||
            currentRoute == Screen.TeacherGrades.route ||
            currentRoute == Screen.TeacherMessages.route ||
            currentRoute == Screen.TeacherProfile.route

    // Функция для выхода из аккаунта
    val navigateToOnboarding = {
        navController.navigate(Screen.OnboardingWelcome.route) {
            // Очищаем весь стек навигации
            popUpTo(0) { inclusive = true }
        }
    }

    // В зависимости от типа пользователя, показываем соответствующий Scaffold с нижней панелью навигации
    if (isStudentScreen) {
        Scaffold(
            bottomBar = {
                StudentBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        if (route != currentRoute) {
                            navController.navigate(route) {
                                // Предотвращаем построение стека навигации при переключении между экранами нижней панели
                                popUpTo(Screen.Schedule.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        ) { innerPadding ->
            StudentNavHost(
                navController = navController,
                onLogout = navigateToOnboarding
            )
        }
    } else if (isTeacherScreen) {
        Scaffold(
            bottomBar = {
                TeacherBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        if (route != currentRoute) {
                            navController.navigate(route) {
                                // Предотвращаем построение стека навигации при переключении между экранами нижней панели
                                popUpTo(Screen.TeacherSchedule.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        ) { innerPadding ->
            TeacherNavHost(
                navController = navController,
                onLogout = navigateToOnboarding
            )
        }
    } else if (isAdminScreen) {
        Scaffold(
            bottomBar = {
                AdminBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        if (route != currentRoute) {
                            navController.navigate(route) {
                                // Предотвращаем построение стека навигации при переключении между экранами нижней панели
                                popUpTo(Screen.AdminDashboard.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        ) { innerPadding ->
            AdminNavHost(
                navController = navController,
                onLogout = navigateToOnboarding
            )
        }
    } else {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable(Screen.OnboardingWelcome.route) {
                OnboardingWelcomeScreen(
                    onNavigateToUniversitySelection = {
                        navController.navigate(Screen.UniversitySelection.route)
                    }
                )
            }

            composable(Screen.UniversitySelection.route) {
                UniversitySelectionScreen(
                    onUniversitySelected = { universityId, universityName ->
                        navController.navigate(Screen.Registration.createRoute(universityId, universityName))
                    },
                    onNavigateToLogin = { universityId, universityName ->
                        navController.navigate(Screen.Login.createRoute(universityId, universityName))
                    }
                )
            }

            composable(
                route = Screen.Login.route,
                arguments = listOf(
                    navArgument("universityId") { type = NavType.StringType },
                    navArgument("universityName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val universityId = backStackEntry.arguments?.getString("universityId") ?: ""
                val universityName = backStackEntry.arguments?.getString("universityName") ?: ""

                LoginScreen(
                    universityId = universityId,
                    universityName = universityName,
                    onNavigateBack = {
                        navController.navigateUp()
                    },
                    onLoginAsStudent = {
                        navController.navigate(Screen.Schedule.route) {
                            popUpTo(Screen.OnboardingWelcome.route) { inclusive = true }
                        }
                    },
                    onLoginAsTeacher = {
                        navController.navigate(Screen.TeacherSchedule.route) {
                            popUpTo(Screen.OnboardingWelcome.route) { inclusive = true }
                        }
                    },
                    onLoginAsAdmin = {
                        navController.navigate(Screen.AdminDashboard.route) {
                            popUpTo(Screen.OnboardingWelcome.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = Screen.Registration.route,
                arguments = listOf(
                    navArgument("universityId") { type = NavType.StringType },
                    navArgument("universityName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val universityId = backStackEntry.arguments?.getString("universityId") ?: ""
                val universityName = backStackEntry.arguments?.getString("universityName") ?: ""

                RegistrationScreen(
                    universityId = universityId,
                    universityName = universityName,
                    onRegistrationComplete = {
                        navController.navigate(Screen.Login.createRoute(universityId, universityName)) {
                            popUpTo(Screen.Registration.route) { inclusive = true }
                        }
                    },
                    onChangeUniversity = {
                        navController.navigateUp()
                    }
                )
            }

            // Экраны студента для первоначальной навигации
            composable(Screen.Schedule.route) {
                ScheduleScreen()
            }

            // Экраны преподавателя для первоначальной навигации
            composable(Screen.TeacherSchedule.route) {
                ScheduleScreen()
            }

            composable(Screen.TeacherGrades.route) {
                TeacherGradesScreen(
                    onNavigateBack = {
                        navController.navigateUp()
                    }
                )
            }

            composable(Screen.TeacherMessages.route) {
                MessagesScreen(
                    onChatSelected = { chatId ->
                        navController.navigate(Screen.Chat.createRoute(chatId))
                    }
                )
            }

            composable(Screen.TeacherProfile.route) {
                ProfileScreen(
                    onLogout = navigateToOnboarding
                )
            }

            // Переход к экранам администратора
            composable(Screen.AdminDashboard.route) {
                AdminDashboardScreen(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route)
                    },
                    onNavigateToUniversityManagement = {
                        navController.navigate(Screen.UniversityManagement.route)
                    },
                    onNavigateToRegistrationRequests = {
                        navController.navigate(Screen.RegistrationRequests.route)
                    },
                    onNavigateToScheduleManagement = {
                        navController.navigate(Screen.ScheduleManagement.route)
                    },
                    onNavigateToMessages = {
                        navController.navigate(Screen.Messages.route)
                    },
                    onLogout = navigateToOnboarding
                )
            }

            composable(Screen.UniversityManagement.route) {
                UniversityManagementScreen(
                    onNavigateBack = {
                        navController.navigateUp()
                    }
                )
            }

            composable(Screen.RegistrationRequests.route) {
                RegistrationRequestsScreen(
                    onNavigateBack = {
                        navController.navigateUp()
                    }
                )
            }

            composable(Screen.ScheduleManagement.route) {
                ScheduleManagementScreen(
                    onNavigateBack = {
                        navController.navigateUp()
                    }
                )
            }

            composable(Screen.Messages.route) {
                MessagesScreen(
                    onChatSelected = { chatId ->
                        navController.navigate(Screen.Chat.createRoute(chatId))
                    }
                )
            }

            // Добавляем недостающие экраны администратора
            composable(Screen.SubjectManagement.route) {
                // Временная заглушка для экрана управления предметами
                Box(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Управление предметами",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            composable(Screen.UserManagement.route) {
                // Временная заглушка для экрана управления пользователями
                Box(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Управление пользователями",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            composable(Screen.AdminProfile.route) {
                ProfileScreen(
                    onLogout = navigateToOnboarding
                )
            }

            composable(
                route = Screen.Chat.route,
                arguments = listOf(navArgument("chatId") { type = NavType.StringType })
            ) { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
                ChatScreen(
                    chatId = chatId,
                    onNavigateBack = {
                        navController.navigateUp()
                    }
                )
            }
        }
    }
}

@Composable
fun StudentNavHost(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Schedule.route
    ) {
        composable(Screen.Schedule.route) {
            ScheduleScreen()
        }

        composable(Screen.GradeBook.route) {
            GradeBookScreen()
        }

        composable(Screen.Messages.route) {
            MessagesScreen(
                onChatSelected = { chatId ->
                    navController.navigate(Screen.Chat.createRoute(chatId))
                }
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            ChatScreen(
                chatId = chatId,
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onLogout = onLogout
            )
        }
    }
}

@Composable
fun TeacherNavHost(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.TeacherSchedule.route
    ) {
        // Экраны преподавателя
        composable(Screen.TeacherSchedule.route) {
            ScheduleScreen()
        }

        composable(Screen.TeacherGrades.route) {
            TeacherGradesScreen(
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        composable(Screen.TeacherMessages.route) {
            MessagesScreen(
                onChatSelected = { chatId ->
                    navController.navigate(Screen.Chat.createRoute(chatId))
                }
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            ChatScreen(
                chatId = chatId,
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        composable(Screen.TeacherProfile.route) {
            ProfileScreen(
                onLogout = onLogout
            )
        }
    }
}

@Composable
fun AdminNavHost(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.AdminDashboard.route
    ) {
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                currentRoute = Screen.AdminDashboard.route,
                onNavigate = { route ->
                    navController.navigate(route)
                },
                onNavigateToUniversityManagement = {
                    navController.navigate(Screen.UniversityManagement.route)
                },
                onNavigateToRegistrationRequests = {
                    navController.navigate(Screen.RegistrationRequests.route)
                },
                onNavigateToScheduleManagement = {
                    navController.navigate(Screen.ScheduleManagement.route)
                },
                onNavigateToMessages = {
                    navController.navigate(Screen.Messages.route)
                },
                onLogout = onLogout
            )
        }

        composable(Screen.UniversityManagement.route) {
            UniversityManagementScreen(
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        composable(Screen.RegistrationRequests.route) {
            RegistrationRequestsScreen(
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        composable(Screen.ScheduleManagement.route) {
            ScheduleManagementScreen(
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        composable(Screen.Messages.route) {
            MessagesScreen(
                onChatSelected = { chatId ->
                    navController.navigate(Screen.Chat.createRoute(chatId))
                }
            )
        }

        // Добавляем недостающие экраны администратора
        composable(Screen.SubjectManagement.route) {
            // Временная заглушка для экрана управления предметами
            Box(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Управление предметами",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        composable(Screen.UserManagement.route) {
            // Временная заглушка для экрана управления пользователями
            Box(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Управление пользователями",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        composable(Screen.AdminProfile.route) {
            ProfileScreen(
                onLogout = onLogout
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            ChatScreen(
                chatId = chatId,
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
    }
}