package com.example.app_my_university.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import com.example.app_my_university.ui.navigation.BottomNavTabMapping
import com.example.app_my_university.ui.navigation.Screen
import com.example.app_my_university.ui.test.UiTestTags

/** Аналитика остаётся доступна с главного экрана; в панели — основной контур из 5 пунктов. */
private val teacherNavItems = listOf(
    MuBottomNavDestination(Screen.TeacherHome.route, Icons.Default.Home, "Главная", "Главная", UiTestTags.BottomNav.TEACHER_HOME),
    MuBottomNavDestination(Screen.Schedule.route, Icons.Default.CalendarMonth, "Расписание", "Расписание", UiTestTags.BottomNav.TEACHER_SCHEDULE),
    MuBottomNavDestination(Screen.TeacherGrades.route, Icons.Default.Grade, "Оценки", "Оценки", UiTestTags.BottomNav.TEACHER_GRADES),
    MuBottomNavDestination(Screen.Dialogs.route, Icons.AutoMirrored.Filled.Chat, "Сообщения", "Сообщения", UiTestTags.BottomNav.TEACHER_MESSAGES),
    MuBottomNavDestination(Screen.Profile.route, Icons.Default.Person, "Профиль", "Профиль", UiTestTags.BottomNav.TEACHER_PROFILE),
)

@Composable
fun TeacherBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
) {
    MuBottomNavigationBar(
        items = teacherNavItems,
        currentRoute = currentRoute,
        onNavigate = onNavigate,
        isSelected = { dest, route ->
            BottomNavTabMapping.teacherTabRouteForSelection(route) == dest.route
        },
    )
}
