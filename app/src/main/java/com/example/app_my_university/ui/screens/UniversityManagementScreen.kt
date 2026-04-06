package com.example.app_my_university.ui.screens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.app_my_university.data.api.model.InstituteRequest
import com.example.app_my_university.data.api.model.InstituteResponse
import com.example.app_my_university.data.api.model.UniversityRequest
import com.example.app_my_university.data.api.model.UniversityResponse
import com.example.app_my_university.ui.components.common.MuEmptyState
import com.example.app_my_university.ui.components.common.MuErrorState
import com.example.app_my_university.ui.components.common.MuLoadingState
import com.example.app_my_university.ui.components.RoleShellScaffold
import com.example.app_my_university.ui.components.UniformTopAppBar
import com.example.app_my_university.ui.navigation.AppRole
import com.example.app_my_university.ui.theme.Dimens
import com.example.app_my_university.ui.viewmodel.AdminUiState
import com.example.app_my_university.ui.viewmodel.AdminViewModel

/**
 * Кампусный админ: свой вуз и институты.
 * SUPER_ADMIN без выбранного вуза: глобальный список вузов и создание вуза (без подстановки «первого» как текущего).
 * SUPER_ADMIN с выбранным вузом: управление этим вузом и институтами.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversityManagementScreen(
    navController: NavHostController,
    onNavigateBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showEditUniversityDialog by remember { mutableStateOf(false) }
    var showAddInstituteDialog by remember { mutableStateOf(false) }
    var showDeleteInstituteDialog by remember { mutableStateOf(false) }
    var showCreateUniversityDialog by remember { mutableStateOf(false) }
    var deletingInstitute by remember { mutableStateOf<InstituteResponse?>(null) }
    var editingUniversity by remember { mutableStateOf<UniversityResponse?>(null) }
    var newUniName by remember { mutableStateOf("") }
    var newUniShort by remember { mutableStateOf("") }
    var newUniCity by remember { mutableStateOf("") }
    var instName by remember { mutableStateOf("") }
    var instShort by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    val isGlobalSuper =
        uiState.isSuperAdmin && uiState.adminUniversityId == null

    val scopedUniversity: UniversityResponse? = when {
        uiState.isSuperAdmin && uiState.adminUniversityId != null ->
            uiState.universities.find { it.id == uiState.adminUniversityId }
        !uiState.isSuperAdmin -> uiState.universities.firstOrNull()
        else -> null
    }

    LaunchedEffect(Unit) {
        viewModel.loadAdminContext()
        viewModel.loadMyUniversityAndInstitutes()
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

    val topTitle = when {
        isGlobalSuper -> "Вузы"
        else -> "Мой вуз"
    }

    RoleShellScaffold(
        role = AppRole.Admin,
        navController = navController,
        topBar = {
            UniformTopAppBar(
                title = topTitle,
                onBackPressed = onNavigateBack,
            )
        },
        floatingActionButton = {
            when {
                isGlobalSuper -> {
                    FloatingActionButton(
                        onClick = {
                            newUniName = ""
                            newUniShort = ""
                            newUniCity = ""
                            showCreateUniversityDialog = true
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Создать вуз")
                    }
                }
                scopedUniversity != null -> {
                    FloatingActionButton(
                        onClick = {
                            instName = ""
                            instShort = ""
                            showAddInstituteDialog = true
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Добавить институт")
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when {
            uiState.isLoading && scopedUniversity == null && !isGlobalSuper && uiState.error == null -> {
                MuLoadingState(Modifier.fillMaxSize().padding(padding))
            }
            uiState.error != null && scopedUniversity == null && !isGlobalSuper -> {
                BoxWithPadding(padding) {
                    MuErrorState(
                        message = uiState.error ?: "Ошибка",
                        onRetry = { viewModel.loadMyUniversityAndInstitutes() }
                    )
                }
            }
            isGlobalSuper -> {
                GlobalUniversitiesBody(
                    padding = padding,
                    uiState = uiState,
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    onScopeUniversity = { id, name ->
                        viewModel.setSuperAdminScopeUniversity(id, name)
                    },
                    onReload = { viewModel.loadUniversities(); viewModel.loadMyUniversityAndInstitutes() },
                )
            }
            scopedUniversity == null && !uiState.isSuperAdmin -> {
                BoxWithPadding(padding) {
                    MuEmptyState(
                        title = "Вуз не найден",
                        subtitle = "Обратитесь к системному администратору.",
                        actionLabel = "Обновить",
                        onAction = { viewModel.loadMyUniversityAndInstitutes() }
                    )
                }
            }
            scopedUniversity != null -> {
                val u = scopedUniversity
                val filteredInstitutes = remember(searchQuery, uiState.institutes) {
                    if (searchQuery.isBlank()) uiState.institutes
                    else uiState.institutes.filter { i ->
                        i.name.contains(searchQuery, ignoreCase = true) ||
                            (i.shortName?.contains(searchQuery, ignoreCase = true) == true)
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(Dimens.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spaceM)
                ) {
                    if (uiState.isSuperAdmin && uiState.adminUniversityId != null) {
                        item {
                            OutlinedButton(
                                onClick = {
                                    viewModel.clearSuperAdminScope()
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Выйти в глобальный список вузов")
                            }
                        }
                    }
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            )
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(Dimens.spaceM),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        u.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    u.shortName?.let {
                                        Text(
                                            it,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    u.city?.let {
                                        Text(
                                            it,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        editingUniversity = u
                                        newUniName = u.name
                                        newUniShort = u.shortName ?: ""
                                        newUniCity = u.city ?: ""
                                        showEditUniversityDialog = true
                                    }
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Изменить вуз")
                                }
                            }
                        }
                    }
                    item {
                        Text(
                            "Институты",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = Dimens.spaceS),
                            label = { Text("Поиск") },
                            leadingIcon = { Icon(Icons.Default.Search, null) },
                            singleLine = true
                        )
                    }
                    if (filteredInstitutes.isEmpty()) {
                        item {
                            Text(
                                if (searchQuery.isBlank()) "Добавьте первый институт" else "Ничего не найдено",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 24.dp)
                            )
                        }
                    } else {
                        items(filteredInstitutes, key = { it.id }) { inst ->
                            InstituteRowCard(
                                institute = inst,
                                onDelete = {
                                    deletingInstitute = inst
                                    showDeleteInstituteDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateUniversityDialog) {
        AlertDialog(
            onDismissRequest = { showCreateUniversityDialog = false },
            title = { Text("Новый вуз") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newUniName,
                        onValueChange = { newUniName = it },
                        label = { Text("Название *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newUniShort,
                        onValueChange = { newUniShort = it },
                        label = { Text("Сокращение") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newUniCity,
                        onValueChange = { newUniCity = it },
                        label = { Text("Город") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createUniversity(
                            UniversityRequest(
                                name = newUniName.trim(),
                                shortName = newUniShort.ifBlank { null },
                                city = newUniCity.ifBlank { null }
                            )
                        )
                        showCreateUniversityDialog = false
                    },
                    enabled = newUniName.isNotBlank()
                ) { Text("Создать") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateUniversityDialog = false }) { Text("Отмена") }
            }
        )
    }

    if (showEditUniversityDialog && editingUniversity != null) {
        AlertDialog(
            onDismissRequest = { showEditUniversityDialog = false },
            title = { Text("Данные вуза") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newUniName,
                        onValueChange = { newUniName = it },
                        label = { Text("Название *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newUniShort,
                        onValueChange = { newUniShort = it },
                        label = { Text("Сокращение") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newUniCity,
                        onValueChange = { newUniCity = it },
                        label = { Text("Город") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        editingUniversity?.let { u ->
                            viewModel.updateUniversity(
                                u.id,
                                UniversityRequest(
                                    name = newUniName,
                                    shortName = newUniShort.ifBlank { null },
                                    city = newUniCity.ifBlank { null }
                                )
                            )
                        }
                        showEditUniversityDialog = false
                    },
                    enabled = newUniName.isNotBlank()
                ) { Text("Сохранить") }
            },
            dismissButton = {
                TextButton(onClick = { showEditUniversityDialog = false }) { Text("Отмена") }
            }
        )
    }

    if (showAddInstituteDialog && scopedUniversity != null) {
        val u = scopedUniversity
        AlertDialog(
            onDismissRequest = { showAddInstituteDialog = false },
            title = { Text("Новый институт") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = instName,
                        onValueChange = { instName = it },
                        label = { Text("Название *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = instShort,
                        onValueChange = { instShort = it },
                        label = { Text("Сокращение") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createInstitute(
                            InstituteRequest(
                                name = instName,
                                shortName = instShort.ifBlank { null },
                                universityId = u.id
                            )
                        )
                        showAddInstituteDialog = false
                    },
                    enabled = instName.isNotBlank()
                ) { Text("Создать") }
            },
            dismissButton = {
                TextButton(onClick = { showAddInstituteDialog = false }) { Text("Отмена") }
            }
        )
    }

    if (showDeleteInstituteDialog && deletingInstitute != null) {
        AlertDialog(
            onDismissRequest = { showDeleteInstituteDialog = false },
            title = { Text("Удалить институт?") },
            text = { Text("«${deletingInstitute?.name}» — связанные данные могут стать недоступны.") },
            confirmButton = {
                Button(
                    onClick = {
                        deletingInstitute?.let { viewModel.deleteInstitute(it.id) }
                        showDeleteInstituteDialog = false
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteInstituteDialog = false }) { Text("Отмена") }
            }
        )
    }
}

@Composable
private fun GlobalUniversitiesBody(
    padding: androidx.compose.foundation.layout.PaddingValues,
    uiState: AdminUiState,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onScopeUniversity: (Long, String) -> Unit,
    onReload: () -> Unit,
) {
    val filtered = remember(searchQuery, uiState.universities) {
        if (searchQuery.isBlank()) uiState.universities
        else uiState.universities.filter { u ->
            u.name.contains(searchQuery, ignoreCase = true) ||
                (u.shortName?.contains(searchQuery, ignoreCase = true) == true) ||
                (u.city?.contains(searchQuery, ignoreCase = true) == true)
        }
    }
    when {
        uiState.isLoading && uiState.universities.isEmpty() -> {
            MuLoadingState(Modifier.fillMaxSize().padding(padding))
        }
        uiState.universities.isEmpty() && !uiState.isLoading -> {
            BoxWithPadding(padding) {
                MuEmptyState(
                    title = "Нет вузов",
                    subtitle = "Создайте первый вуз с помощью кнопки «+».",
                    actionLabel = "Обновить",
                    onAction = onReload
                )
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(Dimens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(Dimens.spaceM),
            ) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                    ) {
                        Text(
                            "Глобальный режим: ни один вуз не выбран для администрирования. " +
                                "Нажмите «Управлять», чтобы работать с институтами и структурой конкретного вуза.",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Поиск вуза") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        singleLine = true,
                    )
                }
                items(filtered, key = { it.id }) { u ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(1.dp),
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(Dimens.spaceM),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(u.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                u.shortName?.let {
                                    Text(
                                        it,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                u.city?.let {
                                    Text(
                                        it,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Button(onClick = { onScopeUniversity(u.id, u.name) }) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Icon(Icons.Default.Settings, contentDescription = null)
                                    Text("Управлять")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxWithPadding(
    padding: androidx.compose.foundation.layout.PaddingValues,
    content: @Composable () -> Unit
) {
    Box(
        Modifier.fillMaxSize().padding(padding),
        contentAlignment = Alignment.Center
    ) { content() }
}

@Composable
private fun InstituteRowCard(
    institute: InstituteResponse,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(Dimens.spaceM),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(institute.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                institute.shortName?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
