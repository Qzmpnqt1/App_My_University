package com.example.app_my_university.data.theme

/**
 * Режим темы приложения. [SYSTEM] — до явного выбора или когда пользователь выбрал «как в системе».
 */
enum class AppThemePreference(val storageValue: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromStorage(raw: String?): AppThemePreference =
            entries.find { it.storageValue == raw } ?: SYSTEM
    }
}
