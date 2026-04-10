package com.example.app_my_university.ui.navigation

/**
 * Приведение текущего маршрута к «вкладке» bottom bar для подсветки.
 */
object BottomNavTabMapping {

    fun studentTabRouteForSelection(currentRoute: String?): String? {
        if (currentRoute == null) return null
        return when {
            currentRoute == Screen.Notifications.route -> Screen.Profile.route
            currentRoute == Screen.StudentPerformance.route -> Screen.GradeBook.route
            currentRoute == Screen.ChatContacts.route -> Screen.Dialogs.route
            currentRoute.startsWith("chat/") -> Screen.Dialogs.route
            else -> currentRoute
        }
    }

    fun teacherTabRouteForSelection(currentRoute: String?): String? {
        if (currentRoute == null) return null
        return when {
            currentRoute == Screen.Notifications.route -> Screen.Profile.route
            currentRoute == Screen.TeacherStatistics.route -> Screen.TeacherHome.route
            currentRoute == Screen.ChatContacts.route -> Screen.Dialogs.route
            currentRoute.startsWith("chat/") -> Screen.Dialogs.route
            else -> currentRoute
        }
    }

    val adminStructureRoutes = setOf(
        Screen.AdminStructure.route,
        Screen.AdminUniversities.route,
        Screen.AdminGroups.route,
        Screen.AdminSubjects.route,
        Screen.AdminSubjectPlan.route,
        Screen.AdminDirections.route,
        Screen.AdminClassrooms.route,
        Screen.AdminTeacherSubjects.route,
    )

    val adminMoreRoutes = setOf(
        Screen.AdminMore.route,
        Screen.Dialogs.route,
        Screen.Profile.route,
        Screen.Notifications.route,
        Screen.AdminUsers.route,
        Screen.AdminAudit.route,
        Screen.AdminStatistics.route,
        Screen.ChatContacts.route,
        Screen.StudentPerformance.route,
    )

    fun adminTabSelected(itemRoute: String, currentRoute: String?): Boolean {
        if (currentRoute == null) return false
        if (currentRoute.startsWith("chat/")) return itemRoute == Screen.AdminMore.route
        return when (itemRoute) {
            Screen.AdminStructure.route ->
                currentRoute in adminStructureRoutes ||
                    currentRoute.startsWith("admin_university_institutes/")
            Screen.AdminMore.route -> currentRoute in adminMoreRoutes
            else -> currentRoute == itemRoute
        }
    }
}
