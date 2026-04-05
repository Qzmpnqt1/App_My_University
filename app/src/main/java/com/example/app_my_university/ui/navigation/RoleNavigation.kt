package com.example.app_my_university.ui.navigation

import androidx.navigation.NavController

/**
 * Единые правила переходов внутри основного контура роли (bottom nav + связанные маршруты).
 * Всегда: [popUpTo] на корень роли, [saveState], [launchSingleTop], [restoreState].
 */
fun NavController.navigateWithinStudentFlow(route: String) {
    navigate(route) {
        popUpTo(Screen.StudentHome.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

fun NavController.navigateWithinTeacherFlow(route: String) {
    navigate(route) {
        popUpTo(Screen.TeacherHome.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

fun NavController.navigateWithinAdminFlow(route: String) {
    navigate(route) {
        popUpTo(Screen.AdminHome.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
