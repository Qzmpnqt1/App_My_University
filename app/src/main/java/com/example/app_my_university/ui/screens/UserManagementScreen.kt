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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.app_my_university.data.api.model.CreateAdminAccountRequest
import com.example.app_my_university.data.api.model.UniversityResponse
import com.example.app_my_university.data.api.model.UserProfileResponse
import com.example.app_my_university.ui.components.RoleShellScaffold
import com.example.app_my_university.ui.components.UniformTopAppBar
import com.example.app_my_university.ui.navigation.AppRole
import com.example.app_my_university.ui.viewmodel.AdminViewModel

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

    RoleShellScaffold(
        role = AppRole.Admin,
        navController = navController,
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null && uiState.users.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.error ?: "Ошибка загрузки",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadUsers() }) {
                            Text("Повторить")
                        }
                    }
                }
            }
            uiState.users.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Нет пользователей",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = { Text("Поиск по имени или email") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true
                    )
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        filterLabels.forEachIndexed { index, label ->
                            SegmentedButton(
                                selected = filterTab == index,
                                onClick = { filterTab = index },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = filterLabels.size
                                ),
                                label = { Text(label) }
                            )
                        }
                    }
                    Text(
                        text = "Найдено: ${filteredUsers.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                    )
                    if (filteredUsers.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Ничего не найдено",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredUsers) { user ->
                                UserCard(
                                    user = user,
                                    onActivate = { viewModel.activateUser(user.id) },
                                    onDeactivate = { viewModel.deactivateUser(user.id) },
                                    isLoading = uiState.isLoading
                                )
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

@Composable
private fun UserCard(
    user: UserProfileResponse,
    onActivate: () -> Unit,
    onDeactivate: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${user.lastName} ${user.firstName}" +
                            (user.middleName?.let { " $it" } ?: ""),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )

                val isActive = user.isActive ?: false
                SuggestionChip(
                    onClick = {},
                    label = {
                        Text(
                            text = if (isActive) "Активен" else "Неактивен",
                            color = if (isActive) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error
                        )
                    }
                )
            }

            Text(
                text = user.email,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = when (user.userType) {
                    "STUDENT" -> "Студент"
                    "TEACHER" -> "Преподаватель"
                    "ADMIN" -> "Администратор"
                    "SUPER_ADMIN" -> "Супер-администратор"
                    else -> user.userType
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                val isActive = user.isActive ?: false
                if (isActive) {
                    OutlinedButton(
                        onClick = onDeactivate,
                        enabled = !isLoading,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Деактивировать")
                    }
                } else {
                    Button(
                        onClick = onActivate,
                        enabled = !isLoading
                    ) {
                        Text("Активировать")
                    }
                }
            }
        }
    }
}
