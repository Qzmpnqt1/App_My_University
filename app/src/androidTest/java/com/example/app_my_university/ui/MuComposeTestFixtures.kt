package com.example.app_my_university.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import com.example.app_my_university.ui.navigation.AppRole
import com.example.app_my_university.ui.navigation.LocalAppRoleOverride
import com.example.app_my_university.ui.theme.AppMyUniversityTheme

fun muSetContent(
    rule: AndroidComposeTestRule<*, *>,
    appRole: AppRole? = AppRole.Student,
    content: @Composable () -> Unit,
) {
    rule.setContent {
        AppMyUniversityTheme(darkTheme = false, dynamicColor = false) {
            if (appRole != null) {
                CompositionLocalProvider(LocalAppRoleOverride provides appRole) {
                    content()
                }
            } else {
                content()
            }
        }
    }
}
