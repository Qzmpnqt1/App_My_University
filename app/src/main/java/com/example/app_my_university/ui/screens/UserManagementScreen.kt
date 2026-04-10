package com.example.app_my_university.ui.screens

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.app_my_university.data.api.model.CreateAdminAccountRequest
import com.example.app_my_university.data.api.model.UniversityResponse
import com.example.app_my_university.data.api.model.UserProfileResponse
import com.example.app_my_university.ui.components.RoleShellScaffold
import com.example.app_my_university.ui.components.UniformTopAppBar
import com.example.app_my_university.ui.designsystem.AppSpacing
import com.example.app_my_university.ui.navigation.AppRole
import com.example.app_my_university.ui.viewmodel.AdminViewModel
import com.example.app_my_university.ui.test.UiTestTags

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    navController: NavHostController,
    onNavigateBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var filterTab by remember { mutableIntStateOf(0) }
    val filterLabels = remember(uiState.isSuperAdmin) {
        if (uiState.isSuperAdmin) {
            listOf("Все", "Студенты", "Преподаватели", "Админы")
        } else {
            listOf("Все", "Студенты", "Преподаватели")
        }
    }
    val snackbarHostState = remember { SnackbarHostState() }
    var showCreateAdminDialog by remember { mutableStateOf(false) }

    val filteredUsers = remember(searchQuery, filterTab, filterLabels, uiState.users) {
        val byRole = when {
            filterTab == 1 -> uiState.users.filter { it.userType == "STUDENT" }
            filterTab == 2 -> uiState.users.filter { it.userType == "TEACHER" }
            filterTab == 3 && filterLabels.size > 3 ->
                uiState.users.filter { it.userType == "ADMIN" || it.userType == "SUPER_ADMIN" }
            else -> uiState.users
        }
        if (searchQuery.isBlank()) byRole
        else byRole.filter { u ->
            "${u.lastName} ${u.firstName}".contains(searchQuery, ignoreCase = true) ||
                u.email.contains(searchQuery, ignoreCase = true)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadAdminContext()
    }
    LaunchedEffect(uiState.isSuperAdmin, uiState.adminUniversityId) {
        if (uiState.isSuperAdmin || uiState.adminUniversityId != null) {
            viewModel.loadUsers()
        }
    }
    LaunchedEffect(showCreateAdminDialog) {
        if (showCreateAdminDialog && uiState.isSuperAdmin) {
            viewModel.loadUniversities()
        }
    }

    LaunchedEffect(filterLabels.size) {
        if (filterTab >= filterLabels.size) {
            filterTab = 0
        }
    }

    LaunchedEffect(uiState.actionSuccess) {
        if (uiState.actionSuccess) {
            uiState.actionMessage?.let { snackbarHostState.showSnackbar(it) }
            viewModel.clearActionSuccess()
            showCreateAdminDialog = false
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    if (showCreateAdminDialog && uiState.isSuperAdmin) {
        CreateAdminAccountDialog(
            universities = uiState.universities,
            isSubmitting = uiState.isLoading,
            onDismiss = { showCreateAdminDialog = false },
            onSubmit = { req -> viewModel.createAdminAccount(req) },
        )
    }

    val listBottomPadding = if (uiState.isSuperAdmin) 88.dp else AppSpacing.xl

    RoleShellScaffold(
        role = AppRole.Admin,
        navController = navController,
        screenTestTag = UiTestTags.Screen.ADMIN_USERS,
        topBar = {
            UniformTopAppBar(
                title = "Управление пользователями",
                onBackPressed = onNavigateBack,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (uiState.isSuperAdmin) {
                FloatingActionButton(onClick = { showCreateAdminDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Создать администратора")
                }
            }
        },
    ) { padding ->
        when {
            uiState.isLoading && uiState.users.isEmpty() -> {
                UserManagementLoadingState(modifier = Modifier.padding(padding))
            }
            uiState.error != null && uiState.users.isEmpty() -> {
                UserManagementErrorState(
                    modifier = Modifier.padding(padding),
                    message = uiState.error ?: "Ошибка загрузки",
                    onRetry = { viewModel.loadUsers() },
                )
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = AppSpacing.screen),
                ) {
                    Spacer(modifier = Modifier.height(AppSpacing.s))
                    UserManagementControlsSection(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        filterLabels = filterLabels,
                        filterTab = filterTab,
                        onFilterTabChange = { filterTab = it },
                        resultCount = filteredUsers.size,
                        totalLoaded = uiState.users.size,
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.s))
                    when {
                        uiState.users.isEmpty() -> {
                            UserManagementEmptyUsersPlaceholder(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                            )
                        }
                        filteredUsers.isEmpty() -> {
                            UserManagementNoResultsPlaceholder(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                            )
                        }
                        else -> {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentPadding = PaddingValues(bottom = listBottomPadding),
                                verticalArrangement = Arrangement.spacedBy(AppSpacing.listItem),
                            ) {
                                items(
                                    items = filteredUsers,
                                    key = { it.id },
                                ) { user ->
                                    UserManagementUserCard(
                                        user = user,
                                        onActivate = { viewModel.activateUser(user.id) },
                                        onDeactivate = { viewModel.deactivateUser(user.id) },
                                        isLoading = uiState.isLoading,
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

@Composable
private fun UserManagementLoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(AppSpacing.m))
            Text(
                text = "Загрузка пользователей…",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun UserManagementErrorState(
    modifier: Modifier = Modifier,
    message: String,
    onRetry: () -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.screen),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
            ),
        ) {
            Column(
                modifier = Modifier.padding(AppSpacing.l),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    modifier = Modifier.padding(bottom = AppSpacing.s),
                    tint = MaterialTheme.colorScheme.error,
                )
                Text(
                    text = "Не удалось загрузить список",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(AppSpacing.xs))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(AppSpacing.m))
                Button(onClick = onRetry) {
                    Text("Повторить")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserManagementControlsSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    filterLabels: List<String>,
    filterTab: Int,
    onFilterTabChange: (Int) -> Unit,
    resultCount: Int,
    totalLoaded: Int,
) {
    val scrollState = rememberScrollState()
    val summaryText = if (totalLoaded > 0) {
        "Найдено $resultCount из $totalLoaded"
    } else {
        "Найдено: $resultCount"
    }
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = AppSpacing.m,
                vertical = AppSpacing.s,
            ),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Поиск и фильтр",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = AppSpacing.s),
                )
                Text(
                    text = summaryText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Имя или email",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                ),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                filterLabels.forEachIndexed { index, label ->
                    FilterChip(
                        selected = filterTab == index,
                        onClick = { onFilterTabChange(index) },
                        label = {
                            Text(
                                text = label,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun UserManagementEmptyUsersPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        ) {
            Column(
                modifier = Modifier.padding(AppSpacing.l),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Outlined.PeopleOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = AppSpacing.s),
                )
                Text(
                    text = "Пока нет пользователей",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(AppSpacing.xs))
                Text(
                    text = "В выбранной области видимости список пуст. Измените фильтр или попробуйте позже.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun UserManagementNoResultsPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        ) {
            Column(
                modifier = Modifier.padding(AppSpacing.l),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Outlined.SearchOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = AppSpacing.s),
                )
                Text(
                    text = "Ничего не найдено",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(AppSpacing.xs))
                Text(
                    text = "Попробуйте изменить запрос поиска или тип пользователя.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun UserManagementUserCard(
    user: UserProfileResponse,
    onActivate: () -> Unit,
    onDeactivate: () -> Unit,
    isLoading: Boolean,
) {
    val fullName = "${user.lastName} ${user.firstName}" +
        (user.middleName?.let { " $it" } ?: "")
    val roleLabel = when (user.userType) {
        "STUDENT" -> "Студент"
        "TEACHER" -> "Преподаватель"
        "ADMIN" -> "Администратор"
        "SUPER_ADMIN" -> "Супер-администратор"
        else -> user.userType
    }
    val isActive = user.isActive ?: false

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.m),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s),
        ) {
            Text(
                text = fullName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = roleLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = AppSpacing.s),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                UserStatusBadge(isActive = isActive)
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = AppSpacing.s),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                if (isActive) {
                    OutlinedButton(
                        onClick = onDeactivate,
                        enabled = !isLoading,
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Text("Деактивировать")
                    }
                } else {
                    Button(
                        onClick = onActivate,
                        enabled = !isLoading,
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Text("Активировать")
                    }
                }
            }
        }
    }
}

@Composable
private fun UserStatusBadge(isActive: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val (container, content) = if (isActive) {
        scheme.primaryContainer to scheme.onPrimaryContainer
    } else {
        scheme.errorContainer to scheme.onErrorContainer
    }
    Surface(
        modifier = Modifier.wrapContentWidth(),
        shape = MaterialTheme.shapes.small,
        color = container,
        tonalElevation = 0.dp,
    ) {
        Text(
            text = if (isActive) "Активен" else "Неактивен",
            modifier = Modifier.padding(horizontal = AppSpacing.s, vertical = AppSpacing.xs),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = content,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateAdminAccountDialog(
    universities: List<UniversityResponse>,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (CreateAdminAccountRequest) -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var createSuperAdmin by remember { mutableStateOf(true) }
    var uniMenuExpanded by remember { mutableStateOf(false) }
    var selectedUni by remember { mutableStateOf<UniversityResponse?>(null) }

    LaunchedEffect(universities) {
        if (universities.isEmpty()) {
            selectedUni = null
        } else if (selectedUni == null || universities.none { it.id == selectedUni?.id }) {
            selectedUni = universities.first()
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text("Новый администратор") },
        text = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = createSuperAdmin,
                        onClick = { createSuperAdmin = true },
                        label = { Text("Супер-админ") },
                    )
                    FilterChip(
                        selected = !createSuperAdmin,
                        onClick = { createSuperAdmin = false },
                        label = { Text("Админ вуза") },
                    )
                }
                if (!createSuperAdmin) {
                    ExposedDropdownMenuBox(
                        expanded = uniMenuExpanded,
                        onExpandedChange = { uniMenuExpanded = it },
                    ) {
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            readOnly = true,
                            value = selectedUni?.name ?: "Выберите вуз",
                            onValueChange = {},
                            label = { Text("Вуз") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = uniMenuExpanded)
                            },
                        )
                        ExposedDropdownMenu(
                            expanded = uniMenuExpanded,
                            onDismissRequest = { uniMenuExpanded = false },
                        ) {
                            universities.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text(u.name) },
                                    onClick = {
                                        selectedUni = u
                                        uniMenuExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Пароль (мин. 6 символов)") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Фамилия") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("Имя") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = middleName,
                    onValueChange = { middleName = it },
                    label = { Text("Отчество (необязательно)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (email.isBlank() || password.length < 6 ||
                        firstName.isBlank() || lastName.isBlank()
                    ) {
                        return@TextButton
                    }
                    if (!createSuperAdmin && selectedUni == null) {
                        return@TextButton
                    }
                    onSubmit(
                        CreateAdminAccountRequest(
                            email = email.trim(),
                            password = password,
                            firstName = firstName.trim(),
                            lastName = lastName.trim(),
                            middleName = middleName.trim().ifBlank { null },
                            userType = if (createSuperAdmin) "SUPER_ADMIN" else "ADMIN",
                            universityId = if (createSuperAdmin) null else selectedUni!!.id,
                        ),
                    )
                },
                enabled = !isSubmitting,
            ) { Text("Создать") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("Отмена")
            }
        },
    )
}
