package com.example.app_my_university.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.app_my_university.ui.navigation.AppRole
import com.example.app_my_university.ui.navigation.switchAdminTab
import com.example.app_my_university.ui.navigation.switchStudentTab
import com.example.app_my_university.ui.navigation.switchTeacherTab

/**
 * Оболочка основных экранов роли: единый [AppScaffold], нижняя навигация и те же правила переходов.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleShellScaffold(
    role: AppRole,
    navController: NavHostController,
    topBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    showBottomBar: Boolean = true,
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    addBelowTopBarContentGap: Boolean = true,
    content: @Composable (PaddingValues) -> Unit,
) {
    val currentRoute = navController.currentDestination?.route
    AppScaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = {
            if (showBottomBar) {
                when (role) {
                    AppRole.Student -> StudentBottomBar(
                        currentRoute = currentRoute,
                        onNavigate = { navController.switchStudentTab(it) },
                    )
                    AppRole.Teacher -> TeacherBottomBar(
                        currentRoute = currentRoute,
                        onNavigate = { navController.switchTeacherTab(it) },
                    )
                    AppRole.Admin -> AdminBottomBar(
                        currentRoute = currentRoute,
                        onNavigate = { navController.switchAdminTab(it) },
                    )
                }
            }
        },
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        addBelowTopBarContentGap = addBelowTopBarContentGap,
        content = content,
    )
}
