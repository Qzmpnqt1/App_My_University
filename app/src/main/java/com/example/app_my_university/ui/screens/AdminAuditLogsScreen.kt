package com.example.app_my_university.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.app_my_university.ui.components.UniformTopAppBar
import com.example.app_my_university.ui.viewmodel.AdminAuditViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAuditLogsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminAuditViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var action by remember { mutableStateOf("") }
    var entity by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    Scaffold(
        topBar = {
            UniformTopAppBar(
                title = "Журнал аудита",
                onBackPressed = onNavigateBack,
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = action,
                onValueChange = { action = it },
                label = { Text("Действие (action)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = entity,
                onValueChange = { entity = it },
                label = { Text("Тип сущности") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Button(
                onClick = { viewModel.load(action = action, entityType = entity) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Применить фильтр") }

            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.logs, key = { it.id }) { log ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("${log.action} · ${log.entityType} #${log.entityId}", style = MaterialTheme.typography.titleSmall)
                            log.details?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                            Text("userId=${log.userId} · ${log.createdAt ?: ""}", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
