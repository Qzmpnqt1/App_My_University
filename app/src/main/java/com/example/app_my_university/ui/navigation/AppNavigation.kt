package com.example.app_my_university.ui.navigation

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.app_my_university.ui.screens.*

sealed class Screen(val route: String, val icon: ImageVector, val label: String) {
    object Onboarding : Screen("onboarding", Icons.Default.Home, "Онбординг")
    object UniversitySelection : Screen("university_selection", Icons.Default.Home, "Выбор ВУЗа")
    object News : Screen("news", Icons.Default.Home, "Новости")
    object Schedule : Screen("schedule", Icons.Default.CalendarToday, "Расписание")
    object Messages : Screen("messages", Icons.AutoMirrored.Filled.Message, "Сообщения")
    object Chat : Screen("chat/{chatId}", Icons.AutoMirrored.Filled.Message, "Чат") {
        fun createRoute(chatId: String): String = "chat/$chatId"
    }
    object GradeBook : Screen("gradebook", Icons.Default.Book, "Зачетка")
    object Profile : Screen("profile", Icons.Default.Person, "Профиль")
    object Login : Screen("login", Icons.Default.AccountCircle, "Вход")
    object Register : Screen("register", Icons.Default.AccountCircle, "Регистрация")
}

/** Нижняя навигационная панель для основных экранов */
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val mainScreens = listOf(
        Screen.News,
        Screen.Schedule,
        Screen.Messages,
        Screen.GradeBook,
        Screen.Profile
    )
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination

    NavigationBar(
        modifier = Modifier.height(70.dp) // Устанавливаем желаемую высоту
    ) {
        mainScreens.forEach { screen ->
            val selected = currentDestination?.hierarchy?.any {
                it.route?.startsWith(screen.route.substringBefore("{")) == true
            } == true

            NavigationBarItem(
                icon = {
                    Icon(
                        screen.icon,
                        contentDescription = screen.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                selected = selected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.padding(vertical = 8.dp) // Отступы элементов
            )
        }
    }
}

/** Основная навигация приложения */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route
) {
    // Определяем, когда показывать нижнюю навигационную панель
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val showBottomBar = listOf(
        Screen.News.route,
        Screen.Schedule.route,
        Screen.Messages.route,
        Screen.GradeBook.route,
        Screen.Profile.route
    ).any { currentRoute?.startsWith(it.substringBefore("{")) == true }

    Scaffold(
        bottomBar = { if (showBottomBar) BottomNavigationBar(navController) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = androidx.compose.ui.Modifier.padding(paddingValues)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Schedule.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = {
                        // После регистрации можно перейти, например, на экран входа
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.Schedule.route) {
                ScheduleScreen(
                    onNavigateToMessages = { navController.navigate(Screen.Messages.route) },
                    onNavigateToGradeBook = { navController.navigate(Screen.GradeBook.route) },
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
                )
            }
            composable(Screen.News.route) {
                NewsScreen()
            }
            composable(Screen.GradeBook.route) {
                GradeBookScreen(
                    onBackPressed = { navController.popBackStack() }
                )
            }
            composable(Screen.Messages.route) {
                MessagesScreen(
                    navigateToChat = { chatId ->
                        navController.navigate(Screen.Chat.createRoute(chatId))
                    }
                )
            }
            composable(
                route = Screen.Chat.route,
                arguments = listOf(navArgument("chatId") { type = NavType.StringType })
            ) { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId") ?: "default"
                ChatScreen(
                    chatId = chatId,
                    onBackPressed = { navController.popBackStack() }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onBackPressed = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun NewsScreen() {
    TODO("Not yet implemented")
}
