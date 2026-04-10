package com.example.app_my_university.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.app_my_university.ui.components.RoleShellScaffold
import com.example.app_my_university.ui.components.UniformTopAppBar
import com.example.app_my_university.ui.components.common.MuErrorState
import com.example.app_my_university.ui.components.common.MuLoadingState
import com.example.app_my_university.ui.navigation.rememberAppRole
import com.example.app_my_university.ui.theme.Dimens
import com.example.app_my_university.ui.viewmodel.NotificationsViewModel
import com.example.app_my_university.util.formatApiDateTimeForDisplay
import com.example.app_my_university.ui.test.UiTestTags

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavHostController,
    onNavigateBack: () -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val role = rememberAppRole()
    val ui by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    LaunchedEffect(ui.error) {
        ui.error?.let {
            snackbar.showSnackbar(it)
            viewModel.clearError()
        }
    }

    RoleShellScaffold(
        role = role,
        navController = navController,
        screenTestTag = UiTestTags.Screen.NOTIFICATIONS,
        topBar = {
            UniformTopAppBar(
                title = "Уведомления",
                onBackPressed = onNavigateBack,
                actions = {
                    IconButton(
                        onClick = { viewModel.markAllRead() },
                        enabled = !ui.busy && ui.items.any { it.readAt == null },
                    ) {
                        Icon(Icons.Default.DoneAll, contentDescription = "Прочитать все")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        when {
            ui.loading && ui.items.isEmpty() -> {
                MuLoadingState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    message = "Загрузка…",
                )
            }
            ui.error != null && ui.items.isEmpty() && !ui.loading -> {
                MuErrorState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    message = ui.error ?: "Ошибка",
                    onRetry = { viewModel.load() },
                )
            }
            ui.items.isEmpty() && !ui.loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(Dimens.screenPadding),
                ) {
                    Text(
                        text = "Пока нет уведомлений. Здесь появятся события: оценки, изменения расписания, новые сообщения, подтверждение регистрации.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(Dimens.screenPadding, Dimens.spaceM),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spaceS),
                ) {
                    items(ui.items, key = { it.id }) { n ->
                        val unread = n.readAt == null
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = unread) {
                                    viewModel.markRead(n.id)
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (unread) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerLow
                                },
                            ),
                        ) {
                            Column(Modifier.padding(Dimens.spaceM)) {
                                Text(
                                    text = n.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (unread) FontWeight.SemiBold else FontWeight.Normal,
                                )
                                n.body?.takeIf { it.isNotBlank() }?.let { body ->
                                    Text(
                                        text = body,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(top = 4.dp),
                                    )
                                }
                                n.createdAt?.let { ts ->
                                    Text(
                                        text = formatApiDateTimeForDisplay(ts),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 6.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
