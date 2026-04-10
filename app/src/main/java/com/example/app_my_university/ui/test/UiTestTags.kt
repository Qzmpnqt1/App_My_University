package com.example.app_my_university.ui.test

/**
 * Стабильные семантические теги для Compose UI-тестов.
 * Используйте [androidx.compose.ui.platform.testTag].
 */
object UiTestTags {
    object State {
        const val LOADING = "mu_state_loading"
        const val ERROR = "mu_state_error"
        const val EMPTY = "mu_state_empty"
        const val RETRY = "mu_state_retry"
    }

    object Sheet {
        const val TEACHER_GRADING_ASSESSMENT = "mu_sheet_teacher_grading_assessment"
        const val ADMIN_TEACHER_ASSIGNMENT = "mu_sheet_admin_teacher_assignment"
    }

    object Screen {
        const val WELCOME = "mu_screen_welcome"
        const val LOGIN = "mu_screen_login"
        const val REGISTRATION = "mu_screen_registration"
        const val REGISTRATION_STATUS = "mu_screen_registration_status"
        const val STUDENT_HOME = "mu_screen_student_home"
        const val TEACHER_HOME = "mu_screen_teacher_home"
        const val ADMIN_HOME = "mu_screen_admin_home"
        const val PROFILE = "mu_screen_profile"
        const val NOTIFICATIONS = "mu_screen_notifications"
        const val SCHEDULE = "mu_screen_schedule"
        const val GRADEBOOK = "mu_screen_gradebook"
        /** Поле поиска на экране зачётной книжки (есть только в состоянии с данными). */
        const val GRADEBOOK_SEARCH = "mu_gradebook_search"
        const val TEACHER_GRADES = "mu_screen_teacher_grades"
        const val TEACHER_STATISTICS = "mu_screen_teacher_statistics"
        const val DIALOGS = "mu_screen_messages"
        const val CHAT_CONTACTS = "mu_screen_chat_contacts"
        const val CHAT = "mu_screen_chat"
        const val ADMIN_REQUESTS = "mu_screen_admin_requests"
        const val ADMIN_USERS = "mu_screen_admin_users"
        const val ADMIN_UNIVERSITIES = "mu_screen_admin_universities"
        const val ADMIN_UNIVERSITY_INSTITUTES = "mu_screen_admin_university_institutes"
        const val ADMIN_SCHEDULE = "mu_screen_admin_schedule"
        const val ADMIN_SUBJECTS = "mu_screen_admin_subjects"
        const val ADMIN_SUBJECT_PLAN = "mu_screen_admin_subject_plan"
        const val ADMIN_DIRECTIONS = "mu_screen_admin_directions"
        const val ADMIN_GROUPS = "mu_screen_admin_groups"
        const val ADMIN_AUDIT = "mu_screen_admin_audit"
        const val ADMIN_STATISTICS = "mu_screen_admin_statistics"
        const val STUDENT_PERFORMANCE = "mu_screen_student_performance"
        const val ADMIN_STRUCTURE = "mu_screen_admin_structure"
        const val ADMIN_CLASSROOMS = "mu_screen_admin_classrooms"
        const val ADMIN_TEACHER_SUBJECTS = "mu_screen_admin_teacher_subjects"
        const val ADMIN_MORE = "mu_screen_admin_more"
    }

    /** Поля формы регистрации (instrumented UI). */
    object RegistrationForm {
        const val SCROLL_CONTAINER = "mu_reg_scroll"
        const val LAST_NAME = "mu_reg_last_name"
        const val FIRST_NAME = "mu_reg_first_name"
        const val EMAIL = "mu_reg_email"
        const val PASSWORD = "mu_reg_password"
        const val SUBMIT = "mu_reg_submit"
    }

    /** Вкладки экрана аналитики администратора ([AdminStatisticsScreen]): `mu_admin_stat_tab_<enum name>`. */
    object AdminStat {
        const val TAB_PREFIX = "mu_admin_stat_tab_"
    }

    /** Нижняя навигация — стабильные селекторы для вкладок. */
    object BottomNav {
        const val STUDENT_HOME = "mu_bottom_nav_student_home"
        const val STUDENT_SCHEDULE = "mu_bottom_nav_student_schedule"
        const val STUDENT_GRADEBOOK = "mu_bottom_nav_student_gradebook"
        const val STUDENT_MESSAGES = "mu_bottom_nav_student_messages"
        const val STUDENT_PROFILE = "mu_bottom_nav_student_profile"

        const val TEACHER_HOME = "mu_bottom_nav_teacher_home"
        const val TEACHER_SCHEDULE = "mu_bottom_nav_teacher_schedule"
        const val TEACHER_GRADES = "mu_bottom_nav_teacher_grades"
        const val TEACHER_MESSAGES = "mu_bottom_nav_teacher_messages"
        const val TEACHER_PROFILE = "mu_bottom_nav_teacher_profile"

        const val ADMIN_HOME = "mu_bottom_nav_admin_home"
        const val ADMIN_REQUESTS = "mu_bottom_nav_admin_requests"
        const val ADMIN_STRUCTURE = "mu_bottom_nav_admin_structure"
        const val ADMIN_SCHEDULE = "mu_bottom_nav_admin_schedule"
        const val ADMIN_MORE = "mu_bottom_nav_admin_more"
    }
}
