package com.example.app_my_university.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app_my_university.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var newEmail by remember { mutableStateOf("") }
    var emailConfirmPassword by remember { mutableStateOf("") }
    var showEmailConfirmPassword by remember { mutableStateOf(false) }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var newPasswordConfirm by remember { mutableStateOf("") }
    var showOldPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showEmailForm by remember { mutableStateOf(false) }
    var showPasswordForm by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.emailChangeSuccess) {
        if (uiState.emailChangeSuccess) {
            snackbarHostState.showSnackbar("Email успешно изменён")
            newEmail = ""
            emailConfirmPassword = ""
            showEmailForm = false
            viewModel.clearSuccessFlags()
        }
    }

    LaunchedEffect(uiState.passwordChangeSuccess) {
        if (uiState.passwordChangeSuccess) {
            snackbarHostState.showSnackbar("Пароль успешно изменён")
            oldPassword = ""
            newPassword = ""
            newPasswordConfirm = ""
            showPasswordForm = false
            viewModel.clearSuccessFlags()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Выйти из аккаунта"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            uiState.isLoading && uiState.profile == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null && uiState.profile == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Ошибка загрузки профиля",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadProfile() }) {
                            Text("Повторить")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(onClick = onLogout) {
                            Icon(
                                Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Выйти из аккаунта")
                        }
                    }
                }
            }
            else -> {
                val profile = uiState.profile
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Личные данные",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            HorizontalDivider()

                            ProfileRow("Фамилия", profile?.lastName ?: "—")
                            ProfileRow("Имя", profile?.firstName ?: "—")
                            profile?.middleName?.let { ProfileRow("Отчество", it) }
                            ProfileRow("Email", profile?.email ?: "—")
                            ProfileRow("Тип", when (profile?.userType) {
                                "STUDENT" -> "Студент"
                                "TEACHER" -> "Преподаватель"
                                "ADMIN" -> "Администратор"
                                else -> profile?.userType ?: "—"
                            })

                            profile?.studentProfile?.let { sp ->
                                sp.groupName?.let { ProfileRow("Группа", it) }
                                sp.instituteName?.let { ProfileRow("Институт", it) }
                            }

                            profile?.teacherProfile?.let { tp ->
                                tp.instituteName?.let { ProfileRow("Институт", it) }
                                tp.position?.let { ProfileRow("Должность", it) }
                            }

                            profile?.adminProfile?.let { ap ->
                                ap.universityName?.let { ProfileRow("Университет", it) }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { showEmailForm = !showEmailForm },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (showEmailForm) "Скрыть" else "Изменить Email")
                    }

                    if (showEmailForm) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = newEmail,
                                    onValueChange = { newEmail = it },
                                    label = { Text("Новый Email") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = emailConfirmPassword,
                                    onValueChange = { emailConfirmPassword = it },
                                    label = { Text("Текущий пароль") },
                                    singleLine = true,
                                    visualTransformation = if (showEmailConfirmPassword) VisualTransformation.None
                                        else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { showEmailConfirmPassword = !showEmailConfirmPassword }) {
                                            Icon(
                                                if (showEmailConfirmPassword) Icons.Default.VisibilityOff
                                                else Icons.Default.Visibility,
                                                contentDescription = null
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Button(
                                    onClick = { viewModel.changeEmail(newEmail, emailConfirmPassword) },
                                    enabled = !uiState.isLoading && newEmail.isNotBlank() && emailConfirmPassword.isNotBlank(),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Сохранить Email")
                                }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { showPasswordForm = !showPasswordForm },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (showPasswordForm) "Скрыть" else "Изменить пароль")
                    }

                    if (showPasswordForm) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = oldPassword,
                                    onValueChange = { oldPassword = it },
                                    label = { Text("Текущий пароль") },
                                    singleLine = true,
                                    visualTransformation = if (showOldPassword) VisualTransformation.None
                                        else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { showOldPassword = !showOldPassword }) {
                                            Icon(
                                                if (showOldPassword) Icons.Default.VisibilityOff
                                                else Icons.Default.Visibility,
                                                contentDescription = null
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = newPassword,
                                    onValueChange = { newPassword = it },
                                    label = { Text("Новый пароль") },
                                    singleLine = true,
                                    visualTransformation = if (showNewPassword) VisualTransformation.None
                                        else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { showNewPassword = !showNewPassword }) {
                                            Icon(
                                                if (showNewPassword) Icons.Default.VisibilityOff
                                                else Icons.Default.Visibility,
                                                contentDescription = null
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = newPasswordConfirm,
                                    onValueChange = { newPasswordConfirm = it },
                                    label = { Text("Подтверждение пароля") },
                                    singleLine = true,
                                    visualTransformation = if (showNewPassword) VisualTransformation.None
                                        else PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Button(
                                    onClick = { viewModel.changePassword(oldPassword, newPassword, newPasswordConfirm) },
                                    enabled = !uiState.isLoading &&
                                            oldPassword.isNotBlank() &&
                                            newPassword.isNotBlank() &&
                                            newPasswordConfirm.isNotBlank(),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Сохранить пароль")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Выйти из аккаунта")
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
