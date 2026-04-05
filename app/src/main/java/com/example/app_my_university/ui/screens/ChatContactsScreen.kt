package com.example.app_my_university.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.app_my_university.data.api.model.ChatContactResponse
import com.example.app_my_university.ui.components.RoleShellScaffold
import com.example.app_my_university.ui.components.UniformTopAppBar
import com.example.app_my_university.ui.designsystem.AppSpacing
import com.example.app_my_university.ui.navigation.rememberAppRole
import com.example.app_my_university.ui.viewmodel.ChatContactsViewModel

private fun displayName(c: ChatContactResponse): String {
    val parts = listOfNotNull(c.lastName, c.firstName, c.middleName).filter { it.isNotBlank() }
    return if (parts.isNotEmpty()) parts.joinToString(" ") else c.email
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatContactsScreen(
    navController: NavHostController,
    onNavigateBack: () -> Unit,
    onContactSelected: (userId: Long, displayName: String) -> Unit,
    viewModel: ChatContactsViewModel = hiltViewModel()
) {
    val role = rememberAppRole()
    val uiState by viewModel.uiState.collectAsState()

    RoleShellScaffold(
        role = role,
        navController = navController,
        topBar = {
            UniformTopAppBar(
                title = "Новое сообщение",
                onBackPressed = onNavigateBack,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = { viewModel.setQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.screen, vertical = AppSpacing.s),
                placeholder = { Text("Поиск по имени или email") },
                singleLine = true
            )

            when {
                uiState.isLoading && uiState.contacts.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null && uiState.contacts.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = uiState.error ?: "Ошибка",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadContacts() }) {
                            Text("Повторить")
                        }
                    }
                }
                else -> {
                    val list = viewModel.filteredContacts()
                    if (list.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Никого не найдено",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(list, key = { it.id }) { contact ->
                                ContactRow(contact = contact) {
                                    onContactSelected(contact.id, displayName(contact))
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
private fun ContactRow(contact: ChatContactResponse, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName(contact),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = contact.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                contact.userType?.let { t ->
                    Text(
                        text = when (t) {
                            "STUDENT" -> "Студент"
                            "TEACHER" -> "Преподаватель"
                            "ADMIN" -> "Администратор"
                            else -> t
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
    HorizontalDivider(modifier = Modifier.padding(start = 76.dp))
}
