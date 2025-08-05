package com.example.app_my_university.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app_my_university.data.api.model.UserProfile
import com.example.app_my_university.ui.viewmodel.ProfileViewModel
import com.example.app_my_university.ui.viewmodel.ProfileUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val userProfile = uiState.userProfile
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Убрали LaunchedEffect для logoutSuccess
    // Теперь выход выполняется синхронно

    // Диалог подтверждения выхода
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Выход из аккаунта") },
            text = { Text("Вы уверены, что хотите выйти из аккаунта?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout() // Очистка данных
                        onLogout() // Немедленный переход
                    }
                ) {
                    Text("Выйти")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Отмена")
                }
            }
        )
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Загрузка профиля...")
                }
            }
        } else if (uiState.error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Ошибка",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = uiState.error ?: "Ошибка загрузки профиля",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadUserProfile() }) {
                        Text("Повторить")
                    }
                }
            }
        } else if (userProfile != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 36.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    // Добавляем отступ снизу, чтобы контент не перекрывался BottomBar
                    .padding(bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Аватар пользователя
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Аватар пользователя",
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Имя пользователя
                Text(
                    text = "${userProfile.lastName} ${userProfile.firstName} ${userProfile.middleName ?: ""}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Тип пользователя
                Text(
                    text = when (userProfile.userType) {
                        "STUDENT" -> "Студент"
                        "TEACHER" -> "Преподаватель"
                        "ADMIN" -> "Администратор"
                        else -> userProfile.userType
                    },
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Общая информация для всех типов пользователей
                ProfileInfoSection(userProfile, viewModel, uiState)

                Spacer(modifier = Modifier.height(32.dp))

                // Кнопка выхода из аккаунта
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Выйти"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Выйти из аккаунта")
                }
            }
        }
    }
}

@Composable
fun ProfileInfoSection(
    userProfile: UserProfile,
    viewModel: ProfileViewModel,
    uiState: ProfileUiState
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Личная информация",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            ProfileInfoRow("Имя", userProfile.firstName)
            ProfileInfoRow("Фамилия", userProfile.lastName)
            ProfileInfoRow("Отчество", userProfile.middleName)

            // Дополнительные данные в зависимости от типа пользователя
            if (userProfile.studentProfile != null) {
                ProfileInfoRow("Университет", userProfile.studentProfile.universityName)
                ProfileInfoRow("Институт", userProfile.studentProfile.instituteName)
                ProfileInfoRow("Направление", userProfile.studentProfile.directionName)
                ProfileInfoRow("Группа", userProfile.studentProfile.groupName)
            }
            if (userProfile.teacherProfile != null) {
                ProfileInfoRow("Университет", userProfile.teacherProfile.universityName)
                // Предметы, которые преподает учитель
                val subjects = userProfile.teacherProfile.subjects?.joinToString(", ") { it.name } ?: "-"
                ProfileInfoRow("Предметы", subjects)
            }
            if (userProfile.adminProfile != null) {
                ProfileInfoRow("Университет", userProfile.adminProfile.universityName)
                ProfileInfoRow("Роль", userProfile.adminProfile.role ?: "Администратор системы")
            }

            // Добавляем редактируемое поле email
            var isEditingEmail by remember { mutableStateOf(false) }
            var emailValue by remember { mutableStateOf(userProfile.email) }

            if (isEditingEmail) {
                OutlinedTextField(
                    value = emailValue,
                    onValueChange = { emailValue = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Row {
                            IconButton(onClick = {
                                viewModel.updateProfile(
                                    firstName = userProfile.firstName,
                                    lastName = userProfile.lastName,
                                    middleName = userProfile.middleName,
                                    email = emailValue
                                )
                                isEditingEmail = false
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Done,
                                    contentDescription = "Сохранить"
                                )
                            }
                            IconButton(onClick = {
                                emailValue = userProfile.email
                                isEditingEmail = false
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Отменить"
                                )
                            }
                        }
                    }
                )
            } else {
                ProfileInfoRow(
                    label = "Email",
                    value = userProfile.email,
                    onEdit = {
                        isEditingEmail = true
                    }
                )
            }

            // Секция для изменения пароля
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            var isChangingPassword by remember { mutableStateOf(false) }
            var currentPassword by remember { mutableStateOf("") }
            var newPassword by remember { mutableStateOf("") }
            var confirmPassword by remember { mutableStateOf("") }

            Text(
                text = "Безопасность",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (isChangingPassword) {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Текущий пароль") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Новый пароль") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Подтвердите новый пароль") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        currentPassword = ""
                        newPassword = ""
                        confirmPassword = ""
                        isChangingPassword = false
                    }) {
                        Text("Отмена")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            viewModel.changePassword(
                                currentPassword = currentPassword,
                                newPassword = newPassword,
                                confirmPassword = confirmPassword
                            )
                            if (uiState.passwordChangeSuccess != null) {
                                currentPassword = ""
                                newPassword = ""
                                confirmPassword = ""
                                isChangingPassword = false
                            }
                        },
                        enabled = currentPassword.isNotEmpty() && newPassword.isNotEmpty() && confirmPassword.isNotEmpty()
                    ) {
                        Text("Сохранить")
                    }
                }
            } else {
                Button(
                    onClick = { isChangingPassword = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Изменить пароль")
                }
            }

            // Сообщения об успешных операциях или ошибках
            if (uiState.updateSuccess != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.updateSuccess,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
                LaunchedEffect(uiState.updateSuccess) {
                    // Очищаем сообщение через некоторое время
                    kotlinx.coroutines.delay(3000)
                    viewModel.clearMessages()
                }
            }

            if (uiState.passwordChangeSuccess != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.passwordChangeSuccess,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
                LaunchedEffect(uiState.passwordChangeSuccess) {
                    // Очищаем сообщение через некоторое время
                    kotlinx.coroutines.delay(3000)
                    viewModel.clearMessages()
                }
            }
        }
    }
}

@Composable
fun ProfileInfoRow(
    label: String,
    value: String?,
    onEdit: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(120.dp)
        )

        Text(
            text = value ?: "-",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        if (onEdit != null) {
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Редактировать",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}