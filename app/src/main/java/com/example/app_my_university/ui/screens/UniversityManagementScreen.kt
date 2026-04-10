package com.example.app_my_university.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.app_my_university.data.api.model.UniversityRequest
import com.example.app_my_university.data.api.model.UniversityResponse
import com.example.app_my_university.ui.components.RoleShellScaffold
import com.example.app_my_university.ui.components.UniformTopAppBar
import com.example.app_my_university.ui.components.common.MuEmptyState
import com.example.app_my_university.ui.components.common.MuErrorState
import com.example.app_my_university.ui.components.common.MuLoadingState
import com.example.app_my_university.ui.designsystem.AppSpacing
import com.example.app_my_university.ui.navigation.AppRole
import com.example.app_my_university.ui.navigation.Screen
import com.example.app_my_university.ui.navigation.openAdminNested
import com.example.app_my_university.ui.theme.Dimens
import com.example.app_my_university.ui.viewmodel.AdminUiState
import com.example.app_my_university.ui.viewmodel.AdminViewModel
import com.example.app_my_university.ui.test.UiTestTags

private val UniCardShape = RoundedCornerShape(16.dp)

/**
 * SUPER_ADMIN: список всех вузов, создание и редактирование; переход к институтам — отдельный маршрут.
 * ADMIN: сразу перенаправление на экран институтов своего вуза (без глобального списка).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversityManagementScreen(
    navController: NavHostController,
    onNavigateBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showCreateUniversityDialog by remember { mutableStateOf(false) }
    var showEditUniversityDialog by remember { mutableStateOf(false) }
    var editingUniversity by remember { mutableStateOf<UniversityResponse?>(null) }
    var newUniName by remember { mutableStateOf("") }
    var newUniShort by remember { mutableStateOf("") }
    var newUniCity by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    var adminRedirectDone by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadAdminContext()
    }

    LaunchedEffect(uiState.adminNavContextReady, uiState.isSuperAdmin, uiState.adminUniversityId) {
        if (!uiState.adminNavContextReady) return@LaunchedEffect
        if (uiState.isSuperAdmin) return@LaunchedEffect
        val uid = uiState.adminUniversityId ?: return@LaunchedEffect
        if (adminRedirectDone) return@LaunchedEffect
        adminRedirectDone = true
        navController.navigate(Screen.AdminUniversityInstitutes.createRoute(uid)) {
            popUpTo(Screen.AdminUniversities.route) { inclusive = true }
            launchSingleTop = true
        }
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

    RoleShellScaffold(
        role = AppRole.Admin,
        navController = navController,
        screenTestTag = UiTestTags.Screen.ADMIN_UNIVERSITIES,
        topBar = {
            UniformTopAppBar(
                title = if (uiState.isSuperAdmin) "ВУЗы" else "Вуз и институты",
                onBackPressed = onNavigateBack,
            )
        },
        floatingActionButton = {
            if (uiState.isSuperAdmin) {
                FloatingActionButton(
                    onClick = {
                        newUniName = ""
                        newUniShort = ""
                        newUniCity = ""
                        showCreateUniversityDialog = true
                    },
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Создать вуз")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when {
            !uiState.adminNavContextReady -> {
                MuLoadingState(Modifier.fillMaxSize().padding(padding))
            }
            uiState.isSuperAdmin -> {
                SuperAdminUniversitiesContent(
                    padding = padding,
                    uiState = uiState,
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    onManageUniversities = { id ->
                        navController.openAdminNested(Screen.AdminUniversityInstitutes.createRoute(id))
                    },
                    onEditUniversity = { u ->
                        editingUniversity = u
                        newUniName = u.name
                        newUniShort = u.shortName ?: ""
                        newUniCity = u.city ?: ""
                        showEditUniversityDialog = true
                    },
                    onReload = { viewModel.loadUniversities() },
                )
            }
            uiState.adminUniversityId == null -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    MuEmptyState(
                        title = "Вуз не найден",
                        subtitle = "В профиле не указан вуз. Обратитесь к системному администратору.",
                        actionLabel = "Обновить",
                        onAction = { viewModel.loadAdminContext() },
                    )
                }
            }
            else -> {
                MuLoadingState(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    message = "Открываем институты…",
                )
            }
        }
    }

    if (showCreateUniversityDialog) {
        UniversityFormDialog(
            title = "Новый вуз",
            name = newUniName,
            shortName = newUniShort,
            city = newUniCity,
            onNameChange = { newUniName = it },
            onShortChange = { newUniShort = it },
            onCityChange = { newUniCity = it },
            confirmLabel = "Создать",
            onDismiss = { showCreateUniversityDialog = false },
            onConfirm = {
                viewModel.createUniversity(
                    UniversityRequest(
                        name = newUniName.trim(),
                        shortName = newUniShort.ifBlank { null },
                        city = newUniCity.ifBlank { null },
                    ),
                )
                showCreateUniversityDialog = false
            },
            confirmEnabled = newUniName.isNotBlank(),
        )
    }

    if (showEditUniversityDialog && editingUniversity != null) {
        val u = editingUniversity!!
        UniversityFormDialog(
            title = "Редактировать вуз",
            name = newUniName,
            shortName = newUniShort,
            city = newUniCity,
            onNameChange = { newUniName = it },
            onShortChange = { newUniShort = it },
            onCityChange = { newUniCity = it },
            confirmLabel = "Сохранить",
            onDismiss = { showEditUniversityDialog = false },
            onConfirm = {
                viewModel.updateUniversity(
                    u.id,
                    UniversityRequest(
                        name = newUniName.trim(),
                        shortName = newUniShort.ifBlank { null },
                        city = newUniCity.ifBlank { null },
                    ),
                )
                showEditUniversityDialog = false
            },
            confirmEnabled = newUniName.isNotBlank(),
        )
    }
}

@Composable
private fun SuperAdminUniversitiesContent(
    padding: androidx.compose.foundation.layout.PaddingValues,
    uiState: AdminUiState,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onManageUniversities: (Long) -> Unit,
    onEditUniversity: (UniversityResponse) -> Unit,
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
        uiState.isLoading && uiState.universities.isEmpty() && uiState.error == null -> {
            MuLoadingState(Modifier.fillMaxSize().padding(padding))
        }
        uiState.error != null && uiState.universities.isEmpty() -> {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                MuErrorState(message = uiState.error ?: "Ошибка", onRetry = onReload)
            }
        }
        uiState.universities.isEmpty() && !uiState.isLoading -> {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                MuEmptyState(
                    title = "Пока нет вузов",
                    subtitle = "Создайте первый вуз кнопкой «+» внизу справа.",
                    actionLabel = "Обновить",
                    onAction = onReload,
                )
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(
                    horizontal = AppSpacing.screen,
                    vertical = AppSpacing.m,
                ),
                verticalArrangement = Arrangement.spacedBy(Dimens.listItemSpacing),
            ) {
                item {
                    Text(
                        "Каталог вузов",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Dimens.spaceS),
                        label = { Text("Поиск по названию, сокращению, городу") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                    )
                }
                items(filtered, key = { it.id }) { u ->
                    SuperAdminUniversityCard(
                        university = u,
                        onManage = { onManageUniversities(u.id) },
                        onEdit = { onEditUniversity(u) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SuperAdminUniversityCard(
    university: UniversityResponse,
    onManage: () -> Unit,
    onEdit: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = UniCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceM),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spaceM),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    Icons.Default.School,
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.iconM),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        university.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    university.shortName?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    university.city?.takeIf { it.isNotBlank() }?.let { city ->
                        Row(
                            modifier = Modifier.padding(top = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                city,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Редактировать вуз")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Button(onClick = onManage) {
                    Text("Управлять")
                }
            }
        }
    }
}

@Composable
private fun UniversityFormDialog(
    title: String,
    name: String,
    shortName: String,
    city: String,
    onNameChange: (String) -> Unit,
    onShortChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
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
                    onValueChange = onShortChange,
                    label = { Text("Сокращение") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = city,
                    onValueChange = onCityChange,
                    label = { Text("Город") },
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
