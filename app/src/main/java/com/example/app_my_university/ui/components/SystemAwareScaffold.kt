package com.example.app_my_university.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * @deprecated Используйте [AppScaffold] или [RoleShellScaffold]. Оставлено для совместимости импортов.
 */
@Deprecated("Используйте AppScaffold", ReplaceWith("AppScaffold(...)"))
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemAwareScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    addBelowTopBarContentGap: Boolean = true,
    content: @Composable (paddingValues: PaddingValues) -> Unit,
) {
    AppScaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        containerColor = containerColor,
        contentColor = contentColor,
        addBelowTopBarContentGap = addBelowTopBarContentGap,
        content = content,
    )
}
