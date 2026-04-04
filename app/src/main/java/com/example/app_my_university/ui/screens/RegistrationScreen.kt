package com.example.app_my_university.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app_my_university.ui.components.UniformTopAppBar
import com.example.app_my_university.ui.viewmodel.RegistrationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    onNavigateBack: () -> Unit,
    onRegistrationSuccess: () -> Unit,
    viewModel: RegistrationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    var universityExpanded by remember { mutableStateOf(false) }
    var instituteExpanded by remember { mutableStateOf(false) }
    var directionExpanded by remember { mutableStateOf(false) }
    var groupExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.isRegistered) {
        if (uiState.isRegistered) {
            snackbarHostState.showSnackbar("Регистрация успешна! Ожидайте подтверждения администратора.")
        }
    }

    Scaffold(
        topBar = {
            UniformTopAppBar(
                title = "Регистрация",
                onBackPressed = onNavigateBack,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.isRegistered) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Заявка отправлена!",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Ваша заявка на регистрацию отправлена. " +
                                    "После одобрения администратором вы сможете войти в систему.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = onNavigateBack) {
                            Text("Вернуться к входу")
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Фамилия *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("Имя *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = middleName,
                    onValueChange = { middleName = it },
                    label = { Text("Отчество") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email *") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Пароль *") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.VisibilityOff
                                else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Тип пользователя",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        selected = uiState.selectedUserType == "STUDENT",
                        onClick = { viewModel.selectUserType("STUDENT") },
                        label = { Text("Студент") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = uiState.selectedUserType == "TEACHER",
                        onClick = { viewModel.selectUserType("TEACHER") },
                        label = { Text("Преподаватель") },
                        modifier = Modifier.weight(1f)
                    )
                }

                ExposedDropdownMenuBox(
                    expanded = universityExpanded,
                    onExpandedChange = { universityExpanded = it }
                ) {
                    OutlinedTextField(
                        value = uiState.universities.find { it.id == uiState.selectedUniversityId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Университет *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = universityExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = universityExpanded,
                        onDismissRequest = { universityExpanded = false }
                    ) {
                        uiState.universities.forEach { uni ->
                            DropdownMenuItem(
                                text = { Text(uni.name) },
                                onClick = {
                                    viewModel.selectUniversity(uni.id)
                                    universityExpanded = false
                                }
                            )
                        }
                    }
                }

                if (uiState.selectedUserType == "STUDENT" || uiState.selectedUserType == "TEACHER") {
                    if (uiState.institutes.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = instituteExpanded,
                            onExpandedChange = { instituteExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = uiState.institutes.find { it.id == uiState.selectedInstituteId }?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Институт *") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = instituteExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = instituteExpanded,
                                onDismissRequest = { instituteExpanded = false }
                            ) {
                                uiState.institutes.forEach { inst ->
                                    DropdownMenuItem(
                                        text = { Text(inst.name) },
                                        onClick = {
                                            viewModel.selectInstitute(inst.id)
                                            instituteExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (uiState.selectedUserType == "STUDENT" && uiState.directions.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = directionExpanded,
                            onExpandedChange = { directionExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = uiState.directions.find { it.id == uiState.selectedDirectionId }?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Направление *") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = directionExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = directionExpanded,
                                onDismissRequest = { directionExpanded = false }
                            ) {
                                uiState.directions.forEach { dir ->
                                    DropdownMenuItem(
                                        text = { Text(dir.name) },
                                        onClick = {
                                            viewModel.selectDirection(dir.id)
                                            directionExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (uiState.selectedUserType == "STUDENT" && uiState.groups.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = groupExpanded,
                            onExpandedChange = { groupExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = uiState.groups.find { it.id == uiState.selectedGroupId }?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Группа *") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = groupExpanded,
                                onDismissRequest = { groupExpanded = false }
                            ) {
                                uiState.groups.forEach { group ->
                                    DropdownMenuItem(
                                        text = { Text(group.name) },
                                        onClick = {
                                            viewModel.selectGroup(group.id)
                                            groupExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        viewModel.register(
                            email = email,
                            password = password,
                            firstName = firstName,
                            lastName = lastName,
                            middleName = middleName.ifBlank { null }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Зарегистрироваться")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
