package com.example.app_my_university.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app_my_university.ui.viewmodel.RegistrationStatusViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationStatusScreen(
    onNavigateBack: () -> Unit,
    viewModel: RegistrationStatusViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(state.error) {
        state.error?.let { viewModel.clearError() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Статус заявки") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Проверка и редактирование заявки (только PENDING). Данные защищены: нужны email и пароль из заявки.",
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль из заявки") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Button(
                onClick = { viewModel.lookup(email, password) },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Проверить статус") }

            state.status?.let { s ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Статус: ${s.status ?: "—"}", style = MaterialTheme.typography.titleMedium)
                        Text("Тип: ${s.userType ?: "—"}")
                        s.rejectionReason?.let { Text("Причина отказа: $it", color = MaterialTheme.colorScheme.error) }
                        s.createdAt?.let { Text("Создана: $it", style = MaterialTheme.typography.bodySmall) }
                    }
                }
                if (s.status.equals("PENDING", ignoreCase = true)) {
                    Spacer(Modifier.height(8.dp))
                    Text("Редактирование заявки", style = MaterialTheme.typography.titleSmall)
                    PendingEditForm(
                        defaultEmail = email,
                        onSubmit = { currentPwd, req -> viewModel.updatePending(currentPwd, req) },
                        isLoading = state.isLoading
                    )
                }
            }

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun PendingEditForm(
    defaultEmail: String,
    onSubmit: (String, com.example.app_my_university.data.api.model.RegisterRequest) -> Unit,
    isLoading: Boolean
) {
    var regEmail by remember { mutableStateOf(defaultEmail) }
    LaunchedEffect(defaultEmail) { regEmail = defaultEmail }
    var currentPwd by remember { mutableStateOf("") }
    var first by remember { mutableStateOf("") }
    var last by remember { mutableStateOf("") }
    var mid by remember { mutableStateOf("") }
    var pwd by remember { mutableStateOf("") }
    var userType by remember { mutableStateOf("STUDENT") }
    var uni by remember { mutableStateOf("") }
    var inst by remember { mutableStateOf("") }
    var grp by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(regEmail, { regEmail = it }, label = { Text("Email в заявке") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(currentPwd, { currentPwd = it }, label = { Text("Текущий пароль заявки") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(first, { first = it }, label = { Text("Имя") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(last, { last = it }, label = { Text("Фамилия") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(mid, { mid = it }, label = { Text("Отчество") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(pwd, { pwd = it }, label = { Text("Новый пароль (мин. 6)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(userType, { userType = it }, label = { Text("Тип: STUDENT/TEACHER/ADMIN") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(uni, { uni = it }, label = { Text("universityId") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(inst, { inst = it }, label = { Text("instituteId (опц.)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(grp, { grp = it }, label = { Text("groupId студента (опц.)") }, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = {
                if (regEmail.isBlank()) return@Button
                val u = uni.toLongOrNull() ?: return@Button
                val i = inst.toLongOrNull()
                val g = grp.toLongOrNull()
                onSubmit(
                    currentPwd,
                    com.example.app_my_university.data.api.model.RegisterRequest(
                        email = regEmail.trim(),
                        password = pwd,
                        firstName = first,
                        lastName = last,
                        middleName = mid.ifBlank { null },
                        userType = userType,
                        universityId = u,
                        groupId = g,
                        instituteId = i
                    )
                )
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Сохранить изменения заявки") }
    }
}
