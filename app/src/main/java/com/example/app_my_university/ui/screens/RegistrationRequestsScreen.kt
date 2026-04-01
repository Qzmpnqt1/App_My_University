package com.example.app_my_university.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app_my_university.data.api.model.RegistrationRequestResponse
import com.example.app_my_university.ui.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationRequestsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Все", "Студенты", "Преподаватели")
    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectingRequestId by remember { mutableStateOf<Long?>(null) }
    var rejectionReason by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadRegistrationRequests()
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

    val visibleRequests = remember(uiState.registrationRequests, selectedTab) {
        when (selectedTab) {
            1 -> uiState.registrationRequests.filter { it.userType == "STUDENT" }
            2 -> uiState.registrationRequests.filter { it.userType == "TEACHER" }
            else -> uiState.registrationRequests
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Заявки на регистрацию") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
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
            uiState.isLoading && uiState.registrationRequests.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null && uiState.registrationRequests.isEmpty() -> {
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
                        Button(onClick = { viewModel.loadRegistrationRequests() }) {
                            Text("Повторить")
                        }
                    }
                }
            }
            uiState.registrationRequests.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Нет заявок на регистрацию",
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
                    TabRow(selectedTabIndex = selectedTab) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(title) }
                            )
                        }
                    }
                    if (visibleRequests.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "В этой категории заявок нет",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(visibleRequests) { request ->
                                RegistrationRequestCard(
                                    request = request,
                                    onApprove = { viewModel.approveRequest(request.id) },
                                    onReject = {
                                        rejectingRequestId = request.id
                                        rejectionReason = ""
                                        showRejectDialog = true
                                    },
                                    isLoading = uiState.isLoading
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Отклонить заявку") },
            text = {
                OutlinedTextField(
                    value = rejectionReason,
                    onValueChange = { rejectionReason = it },
                    label = { Text("Причина отклонения") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        rejectingRequestId?.let { id ->
                            viewModel.rejectRequest(id, rejectionReason.ifBlank { null })
                        }
                        showRejectDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Отклонить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
private fun RegistrationRequestCard(
    request: RegistrationRequestResponse,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "${request.lastName} ${request.firstName}" +
                        (request.middleName?.let { " $it" } ?: ""),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = request.email,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SuggestionChip(
                    onClick = {},
                    label = {
                        Text(
                            when (request.userType) {
                                "STUDENT" -> "Студент"
                                "TEACHER" -> "Преподаватель"
                                "ADMIN" -> "Администратор"
                                else -> request.userType
                            }
                        )
                    }
                )

                SuggestionChip(
                    onClick = {},
                    label = {
                        Text(
                            when (request.status) {
                                "PENDING" -> "Ожидает"
                                "APPROVED" -> "Одобрена"
                                "REJECTED" -> "Отклонена"
                                else -> request.status
                            }
                        )
                    }
                )
            }

            request.universityName?.let {
                Text(
                    text = "Университет: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            request.groupName?.let {
                Text(
                    text = "Группа: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (request.status == "PENDING") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        enabled = !isLoading,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Отклонить")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onApprove,
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Одобрить")
                    }
                }
            }
        }
    }
}
