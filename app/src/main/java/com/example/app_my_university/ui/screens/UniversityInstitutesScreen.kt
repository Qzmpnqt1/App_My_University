package com.example.app_my_university.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.app_my_university.data.api.model.InstituteRequest
import com.example.app_my_university.data.api.model.InstituteResponse
import com.example.app_my_university.ui.components.RoleShellScaffold
import com.example.app_my_university.ui.components.UniformTopAppBar
import com.example.app_my_university.ui.components.common.MuEmptyState
import com.example.app_my_university.ui.components.common.MuErrorState
import com.example.app_my_university.ui.components.common.MuLoadingState
import com.example.app_my_university.ui.designsystem.AppSpacing
import com.example.app_my_university.ui.navigation.AppRole
import com.example.app_my_university.ui.theme.Dimens
import com.example.app_my_university.ui.viewmodel.UniversityInstitutesViewModel

private val CardShape = RoundedCornerShape(16.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversityInstitutesScreen(
    navController: NavHostController,
    universityId: Long,
    onNavigateBack: () -> Unit,
    viewModel: UniversityInstitutesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showAddInstitute by remember { mutableStateOf(false) }
    var showEditInstitute by remember { mutableStateOf(false) }
    var showDeleteInstitute by remember { mutableStateOf(false) }
    var editingInstitute by remember { mutableStateOf<InstituteResponse?>(null) }
    var deletingInstitute by remember { mutableStateOf<InstituteResponse?>(null) }
    var instName by remember { mutableStateOf("") }
    var instShort by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(universityId) {
        viewModel.load(universityId)
    }

    LaunchedEffect(uiState.actionSuccess) {
        if (uiState.actionSuccess) {
            uiState.actionMessage?.let { snackbarHostState.showSnackbar(it) }
            viewModel.clearActionSuccess()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val u = uiState.university
    val filteredInstitutes = remember(searchQuery, uiState.institutes) {
        if (searchQuery.isBlank()) uiState.institutes
        else uiState.institutes.filter { i ->
            i.name.contains(searchQuery, ignoreCase = true) ||
                (i.shortName?.contains(searchQuery, ignoreCase = true) == true)
        }
    }

    RoleShellScaffold(
        role = AppRole.Admin,
        navController = navController,
        topBar = {
            UniformTopAppBar(
                title = "Институты",
                onBackPressed = onNavigateBack,
                actions = {
                    // Симметрия с кнопкой «Назад» (стандартный touch target), заголовок по центру экрана.
                    Spacer(Modifier.size(48.dp))
                },
            )
        },
        floatingActionButton = {
            if (u != null) {
                FloatingActionButton(
                    onClick = {
                        instName = ""
                        instShort = ""
                        showAddInstitute = true
                    },
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить институт")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when {
            uiState.isLoading && u == null && uiState.error == null -> {
                MuLoadingState(Modifier.fillMaxSize().padding(padding))
            }
            uiState.error != null && u == null -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    MuErrorState(
                        message = uiState.error ?: "Ошибка",
                        onRetry = { viewModel.load(universityId) },
                    )
                }
            }
            u != null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(horizontal = AppSpacing.screen, vertical = AppSpacing.m),
                    verticalArrangement = Arrangement.spacedBy(Dimens.listItemSpacing),
                ) {
                    item {
                        UniversitySummaryHeaderCard(university = u)
                    }
                    item {
                        Text(
                            "Список институтов",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = Dimens.spaceS),
                        )
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = Dimens.spaceS),
                            label = { Text("Поиск по институтам") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                        )
                    }
                    if (filteredInstitutes.isEmpty()) {
                        item {
                            if (searchQuery.isBlank()) {
                                MuEmptyState(
                                    title = "Пока нет институтов",
                                    subtitle = "Добавьте первый институт кнопкой «+» внизу справа.",
                                    actionLabel = "Обновить",
                                    onAction = { viewModel.load(universityId) },
                                )
                            } else {
                                MuEmptyState(
                                    title = "Ничего не найдено",
                                    subtitle = "Попробуйте изменить запрос.",
                                    actionLabel = "Сбросить поиск",
                                    onAction = { searchQuery = "" },
                                )
                            }
                        }
                    } else {
                        items(filteredInstitutes, key = { it.id }) { inst ->
                            InstituteManagementCard(
                                institute = inst,
                                onEdit = {
                                    editingInstitute = inst
                                    instName = inst.name
                                    instShort = inst.shortName ?: ""
                                    showEditInstitute = true
                                },
                                onDelete = {
                                    deletingInstitute = inst
                                    showDeleteInstitute = true
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddInstitute && u != null) {
        InstituteFormDialog(
            title = "Новый институт",
            name = instName,
            shortName = instShort,
            onNameChange = { instName = it },
            onShortNameChange = { instShort = it },
            confirmLabel = "Создать",
            onDismiss = { showAddInstitute = false },
            onConfirm = {
                viewModel.createInstitute(
                    InstituteRequest(
                        name = instName.trim(),
                        shortName = instShort.ifBlank { null },
                        universityId = u.id,
                    ),
                )
                showAddInstitute = false
            },
            confirmEnabled = instName.isNotBlank(),
        )
    }

    if (showEditInstitute && editingInstitute != null && u != null) {
        val inst = editingInstitute!!
        InstituteFormDialog(
            title = "Редактировать институт",
            name = instName,
            shortName = instShort,
            onNameChange = { instName = it },
            onShortNameChange = { instShort = it },
            confirmLabel = "Сохранить",
            onDismiss = { showEditInstitute = false },
            onConfirm = {
                viewModel.updateInstitute(
                    inst.id,
                    InstituteRequest(
                        name = instName.trim(),
                        shortName = instShort.ifBlank { null },
                        universityId = u.id,
                    ),
                )
                showEditInstitute = false
            },
            confirmEnabled = instName.isNotBlank(),
        )
    }

    if (showDeleteInstitute && deletingInstitute != null) {
        AlertDialog(
            onDismissRequest = { showDeleteInstitute = false },
            title = { Text("Удалить институт?") },
            text = { Text("«${deletingInstitute?.name}» — связанные данные могут стать недоступны.") },
            confirmButton = {
                Button(
                    onClick = {
                        deletingInstitute?.let { viewModel.deleteInstitute(it.id, universityId) }
                        showDeleteInstitute = false
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteInstitute = false }) { Text("Отмена") }
            },
        )
    }

}

@Composable
private fun UniversitySummaryHeaderCard(
    university: com.example.app_my_university.data.api.model.UniversityResponse,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPadding),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spaceM),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    Icons.Default.School,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(Dimens.iconM),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        text = university.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                    )
                    university.shortName?.takeIf { it.isNotBlank() }?.let {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    university.city?.takeIf { it.isNotBlank() }?.let { city ->
                        Spacer(Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = city,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InstituteManagementCard(
    institute: InstituteResponse,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceS),
        ) {
            Column(
                Modifier
                    .weight(1f)
                    .padding(end = Dimens.spaceXS),
            ) {
                Text(
                    text = institute.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                institute.shortName?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Изменить")
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun InstituteFormDialog(
    title: String,
    name: String,
    shortName: String,
    onNameChange: (String) -> Unit,
    onShortNameChange: (String) -> Unit,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    confirmEnabled: Boolean,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Название *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = shortName,
                    onValueChange = onShortNameChange,
                    label = { Text("Сокращение") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = confirmEnabled) { Text(confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        },
    )
}

