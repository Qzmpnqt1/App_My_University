package com.example.app_my_university.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app_my_university.data.api.model.UserProfileResponse
import com.example.app_my_university.ui.components.common.MuErrorState
import com.example.app_my_university.ui.components.common.MuLoadingState
import com.example.app_my_university.ui.components.profile.ProfileDestructiveActionCard
import com.example.app_my_university.ui.components.profile.ProfileExpandableAction
import com.example.app_my_university.ui.components.profile.ProfileHeroHeader
import com.example.app_my_university.ui.components.profile.ProfileReadOnlyField
import com.example.app_my_university.ui.components.profile.ProfileSectionCard
import com.example.app_my_university.ui.components.profile.fullDisplayName
import com.example.app_my_university.ui.components.profile.heroContextLines
import com.example.app_my_university.ui.components.profile.roleSectionSubtitle
import com.example.app_my_university.ui.components.profile.roleSectionTitle
import com.example.app_my_university.ui.components.profile.userInitials
import com.example.app_my_university.ui.components.profile.userRoleLabelRu
import com.example.app_my_university.ui.components.UniformTopAppBar
import com.example.app_my_university.ui.theme.Dimens
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
    var showPersonalEdit by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var editLastName by remember { mutableStateOf("") }
    var editFirstName by remember { mutableStateOf("") }
    var editMiddleName by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val busy = uiState.isLoading
    val profile = uiState.profile

    LaunchedEffect(profile?.id, profile?.lastName, profile?.firstName, profile?.middleName) {
        val p = profile ?: return@LaunchedEffect
        editLastName = p.lastName
        editFirstName = p.firstName
        editMiddleName = p.middleName.orEmpty()
    }

    fun requestLogout() {
        showLogoutConfirm = true
    }

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

    LaunchedEffect(uiState.profileUpdateSuccess) {
        if (uiState.profileUpdateSuccess) {
            snackbarHostState.showSnackbar("Данные профиля сохранены")
            showPersonalEdit = false
            viewModel.clearSuccessFlags()
        }
    }

    Scaffold(
        topBar = {
            UniformTopAppBar(
                title = "Профиль",
                onBackPressed = onNavigateBack,
                actions = {
                    IconButton(onClick = { requestLogout() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Выйти из аккаунта"
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            uiState.isLoading && uiState.profile == null -> {
                MuLoadingState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    message = "Загружаем профиль…"
                )
            }
            uiState.error != null && uiState.profile == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MuErrorState(
                        message = "Не удалось загрузить профиль",
                        onRetry = { viewModel.loadProfile() }
                    )
                    Spacer(Modifier.height(Dimens.spaceL))
                    TextButton(onClick = { requestLogout() }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(Dimens.spaceS))
                            Text("Выйти из аккаунта")
                        }
                    }
                }
            }
            profile == null -> {
                MuErrorState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    message = "Нет данных профиля",
                    onRetry = { viewModel.loadProfile() }
                )
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Dimens.screenPadding, vertical = Dimens.spaceM),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spaceM)
                ) {
                    ProfileHeroHeader(
                        fullName = profile.fullDisplayName(),
                        initials = profile.userInitials(),
                        roleLabel = userRoleLabelRu(profile.userType),
                        email = profile.email,
                        contextLines = profile.heroContextLines(),
                        isActive = profile.isActive
                    )

                    RoleSpecificSection(profile)

                    ProfileSectionCard(
                        title = "Личные данные",
                        subtitle = "Текущие ФИО. Изменение — в разделе «Редактировать ФИО».",
                        icon = Icons.Default.Person
                    ) {
                        ProfileReadOnlyField(label = "Фамилия", value = profile.lastName)
                        ProfileReadOnlyField(label = "Имя", value = profile.firstName)
                        profile.middleName?.let {
                            ProfileReadOnlyField(label = "Отчество", value = it)
                        }
                    }

                    ProfileExpandableAction(
                        title = "Редактировать ФИО",
                        collapsedHint = "Доступно для изменения",
                        icon = Icons.Default.Person,
                        expanded = showPersonalEdit,
                        onToggle = { showPersonalEdit = !showPersonalEdit },
                        enabled = !busy
                    ) {
                        OutlinedTextField(
                            value = editLastName,
                            onValueChange = { editLastName = it },
                            label = { Text("Фамилия") },
                            singleLine = true,
                            enabled = !busy,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editFirstName,
                            onValueChange = { editFirstName = it },
                            label = { Text("Имя") },
                            singleLine = true,
                            enabled = !busy,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editMiddleName,
                            onValueChange = { editMiddleName = it },
                            label = { Text("Отчество (необязательно)") },
                            singleLine = true,
                            enabled = !busy,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Остальные поля профиля задаются администратором вуза.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = {
                                viewModel.updatePersonalProfile(
                                    editFirstName,
                                    editLastName,
                                    editMiddleName.trim().takeIf { it.isNotEmpty() }
                                )
                            },
                            enabled = !busy &&
                                editLastName.isNotBlank() &&
                                editFirstName.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Сохранить ФИО")
                        }
                    }

                    Text(
                        text = "Безопасность",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = Dimens.spaceXS, top = Dimens.spaceS)
                    )

                    ProfileExpandableAction(
                        title = "Изменить email",
                        collapsedHint = profile.email,
                        icon = Icons.Default.Email,
                        expanded = showEmailForm,
                        onToggle = { showEmailForm = !showEmailForm },
                        enabled = !busy
                    ) {
                        OutlinedTextField(
                            value = newEmail,
                            onValueChange = { newEmail = it },
                            label = { Text("Новый email") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            enabled = !busy,
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
                            enabled = !busy,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = { viewModel.changeEmail(newEmail, emailConfirmPassword) },
                            enabled = !busy &&
                                newEmail.isNotBlank() &&
                                emailConfirmPassword.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Сохранить email")
                        }
                    }

                    ProfileExpandableAction(
                        title = "Изменить пароль",
                        collapsedHint = "Обновить пароль входа",
                        icon = Icons.Default.Lock,
                        expanded = showPasswordForm,
                        onToggle = { showPasswordForm = !showPasswordForm },
                        enabled = !busy
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
                            enabled = !busy,
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
                            enabled = !busy,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newPasswordConfirm,
                            onValueChange = { newPasswordConfirm = it },
                            label = { Text("Подтверждение пароля") },
                            singleLine = true,
                            visualTransformation = if (showNewPassword) VisualTransformation.None
                            else PasswordVisualTransformation(),
                            enabled = !busy,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                viewModel.changePassword(oldPassword, newPassword, newPasswordConfirm)
                            },
                            enabled = !busy &&
                                oldPassword.isNotBlank() &&
                                newPassword.isNotBlank() &&
                                newPasswordConfirm.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Сохранить пароль")
                        }
                    }

                    if (!profile.createdAt.isNullOrBlank()) {
                        ProfileSectionCard(
                            title = "Системная информация",
                            subtitle = "Только просмотр",
                            icon = Icons.Default.Info
                        ) {
                            ProfileReadOnlyField(
                                label = "Дата создания учётной записи",
                                value = profile.createdAt.orEmpty(),
                                hint = "Формат данных приходит с сервера"
                            )
                        }
                    }

                    ProfileDestructiveActionCard(
                        title = "Выйти из аккаунта",
                        subtitle = "Потребуется снова войти в приложение",
                        icon = Icons.AutoMirrored.Filled.Logout,
                        onClick = { requestLogout() },
                        enabled = !busy
                    )

                    Spacer(Modifier.height(Dimens.spaceM))
                }
            }
        }
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            icon = {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Выйти из аккаунта?") },
            text = {
                Text(
                    "Вы действительно хотите завершить сеанс? Потребуется снова войти в систему.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutConfirm = false
                        onLogout()
                    }
                ) {
                    Text("Выйти", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
private fun RoleSpecificSection(profile: UserProfileResponse) {
    val icon = when (profile.userType) {
        "STUDENT" -> Icons.Default.School
        "TEACHER" -> Icons.Default.Business
        "ADMIN" -> Icons.Default.Settings
        else -> Icons.Default.Info
    }
    ProfileSectionCard(
        title = profile.roleSectionTitle(),
        subtitle = profile.roleSectionSubtitle(),
        icon = icon
    ) {
        when (profile.userType) {
            "STUDENT" -> {
                profile.studentProfile?.groupName?.let {
                    ProfileReadOnlyField(
                        label = "Группа",
                        value = it,
                        hint = "Назначается при распределении"
                    )
                }
                profile.studentProfile?.instituteName?.let {
                    ProfileReadOnlyField(label = "Институт", value = it)
                }
                if (profile.studentProfile?.groupName == null &&
                    profile.studentProfile?.instituteName == null
                ) {
                    Text(
                        text = "Учебные данные появятся после назначения группы и института.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            "TEACHER" -> {
                profile.teacherProfile?.instituteName?.let {
                    ProfileReadOnlyField(label = "Институт", value = it)
                }
                profile.teacherProfile?.position?.let {
                    ProfileReadOnlyField(label = "Должность", value = it)
                }
                if (profile.teacherProfile?.instituteName == null &&
                    profile.teacherProfile?.position == null
                ) {
                    Text(
                        text = "Служебные данные будут доступны после заполнения карточки преподавателя.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Список дисциплин доступен в расписании и связанных разделах.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            "ADMIN" -> {
                Text(
                    text = "Вы администратор конкретного вуза в системе «Мой ВУЗ».",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(Dimens.spaceS))
                profile.adminProfile?.universityName?.let {
                    ProfileReadOnlyField(
                        label = "Университет",
                        value = it,
                        hint = "Область ваших полномочий"
                    )
                }
                profile.adminProfile?.role?.takeIf { it.isNotBlank() }?.let {
                    ProfileReadOnlyField(
                        label = "Роль в системе",
                        value = it,
                        hint = "Как задано на сервере"
                    )
                }
                if (profile.adminProfile?.universityName.isNullOrBlank()) {
                    ProfileReadOnlyField(
                        label = "Университет",
                        value = "",
                        hint = "Обратитесь к владельцу системы, если вуз не отображается"
                    )
                }
            }
            else -> {
                Text(
                    text = "Дополнительные сведения недоступны для этого типа учётной записи.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
