package com.example.app_my_university.ui.navigation

import androidx.navigation.NavController

/**
 * Навигация внутри роли: два режима.
 *
 * **Переключение top-level вкладок** ([switchStudentTab] / [switchTeacherTab] / [switchAdminTab]):
 * используется для bottom bar и для переходов «на другую вкладку» с главного экрана.
 * [popUpTo] на домашний экран роли, [saveState], [launchSingleTop], [restoreState] —
 * как у прежнего `navigateWithin*Flow`.
 *
 * **Вложенные экраны** ([openStudentNested] / [openTeacherNested] / [openAdminNested]):
 * открытие поверх текущего destination без среза стека до home — сохраняется логический родитель
 * (хаб «Структура», «Ещё» и т.д.). Только [launchSingleTop], чтобы не дублировать тот же экран наверху.
 */

fun NavController.switchStudentTab(route: String) {
    navigate(route) {
        popUpTo(Screen.StudentHome.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

fun NavController.switchTeacherTab(route: String) {
    navigate(route) {
        popUpTo(Screen.TeacherHome.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

fun NavController.switchAdminTab(route: String) {
    navigate(route) {
        popUpTo(Screen.AdminHome.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

fun NavController.openStudentNested(route: String) {
    navigate(route) {
        launchSingleTop = true
    }
}

fun NavController.openTeacherNested(route: String) {
    navigate(route) {
        launchSingleTop = true
    }
}

fun NavController.openAdminNested(route: String) {
    navigate(route) {
        launchSingleTop = true
    }
}
