package com.example.app_my_university.ui.navigation

import androidx.compose.runtime.compositionLocalOf

/**
 * Для UI-тестов и превью: если задано, [rememberAppRole] возвращает это значение без чтения [TokenManager].
 */
val LocalAppRoleOverride = compositionLocalOf<AppRole?> { null }
