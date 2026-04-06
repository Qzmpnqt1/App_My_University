package com.example.app_my_university.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.app_my_university.di.NavigationEntryPoint
import dagger.hilt.android.EntryPointAccessors

@Composable
fun rememberAppRole(): AppRole {
    val context = LocalContext.current
    val entryPoint = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            NavigationEntryPoint::class.java,
        )
    }
    val userType by entryPoint.tokenManager().userType.collectAsState(initial = null)
    return when (userType) {
        "TEACHER" -> AppRole.Teacher
        "ADMIN", "SUPER_ADMIN" -> AppRole.Admin
        else -> AppRole.Student
    }
}
