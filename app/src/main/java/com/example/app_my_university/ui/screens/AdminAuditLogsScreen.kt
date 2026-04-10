package com.example.app_my_university.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.app_my_university.ui.audit.AuditFilterCatalog
import com.example.app_my_university.ui.audit.AuditFilterOption
import com.example.app_my_university.ui.components.RoleShellScaffold
import com.example.app_my_university.ui.components.UniformTopAppBar
import com.example.app_my_university.ui.components.common.MuEmptyState
import com.example.app_my_university.ui.designsystem.AppSpacing
import com.example.app_my_university.ui.navigation.AppRole
import com.example.app_my_university.ui.viewmodel.AdminAuditViewModel
import com.example.app_my_university.util.formatApiDateTimeForDisplay
import com.example.app_my_university.ui.test.UiTestTags

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAuditLogsScreen(
    navController: NavHostController,
    onNavigateBack: () -> Unit,
    viewModel: AdminAuditViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var selectedAction by remember { mutableStateOf<String?>(null) }
    var selectedEntityType by remember { mutableStateOf<String?>(null) }
    var actionMenuExpanded by remember { mutableStateOf(false) }
    var entityMenuExpanded by remember { mutableStateOf(false) }

    val actionFieldLabel = remember(selectedAction) {
        AuditFilterCatalog.actionChoices.find { it.apiValue == selectedAction }?.labelRu
            ?: AuditFilterCatalog.actionChoices.first().labelRu
    }
    val entityFieldLabel = remember(selectedEntityType) {
        AuditFilterCatalog.entityTypeChoices.find { it.apiValue == selectedEntityType }?.labelRu
            ?: AuditFilterCatalog.entityTypeChoices.first().labelRu
    }

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    RoleShellScaffold(
        role = AppRole.Admin,
        navController = navController,
        screenTestTag = UiTestTags.Screen.ADMIN_AUDIT,
        topBar = {
            UniformTopAppBar(
                title = "Журнал аудита",
                onBackPressed = onNavigateBack,
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(AppSpacing.screen)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s),
        ) {
            AuditFilterDropdown(
                label = "Действие (action)",
                expanded = actionMenuExpanded,
                onExpandedChange = { actionMenuExpanded = it },
                displayText = actionFieldLabel,
                options = AuditFilterCatalog.actionChoices,
                onSelect = { opt ->
                    selectedAction = opt.apiValue
                    actionMenuExpanded = false
                },
            )
            AuditFilterDropdown(
                label = "Тип сущности",
                expanded = entityMenuExpanded,
                onExpandedChange = { entityMenuExpanded = it },
                displayText = entityFieldLabel,
                options = AuditFilterCatalog.entityTypeChoices,
                onSelect = { opt ->
                    selectedEntityType = opt.apiValue
                    entityMenuExpanded = false
                },
            )
            Button(
                onClick = {
                    viewModel.load(
                        action = selectedAction,
                        entityType = selectedEntityType,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Применить фильтр") }

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
                state.error != null -> {
                    Text(
                        state.error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f),
                    )
                }
                state.logs.isEmpty() -> {
                    MuEmptyState(
                        title = "Нет данных для аудита",
                        subtitle = "Записей по текущим фильтрам нет. Измените фильтры и нажмите «Применить фильтр».",
                        modifier = Modifier.weight(1f),
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.logs, key = { it.id }) { log ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        "${log.action} · ${log.entityType} #${log.entityId}",
                                        style = MaterialTheme.typography.titleSmall,
                                    )
                                    log.details?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                                    Text(
                                        "userId=${log.userId} · ${
                                            log.createdAt?.let { formatApiDateTimeForDisplay(it) } ?: "—"
                                        }",
                                        style = MaterialTheme.typography.labelSmall,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuditFilterDropdown(
    label: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    displayText: String,
    options: List<AuditFilterOption>,
    onSelect: (AuditFilterOption) -> Unit,
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            readOnly = true,
            value = displayText,
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
        ) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = {
                        Text(
                            opt.labelRu,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    onClick = { onSelect(opt) },
                )
            }
        }
    }
}
