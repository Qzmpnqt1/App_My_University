package com.example.app_my_university.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app_my_university.ui.viewmodel.AdminStatisticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStatisticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminStatisticsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var groupId by remember { mutableStateOf("") }
    var uniId by remember { mutableStateOf("") }
    var instId by remember { mutableStateOf("") }
    var roomId by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Аналитика (админ)") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Введите идентификаторы сущностей и загрузите агрегаты с сервера.", style = MaterialTheme.typography.bodyMedium)

            OutlinedTextField(groupId, { groupId = it }, label = { Text("ID группы") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = { groupId.toLongOrNull()?.let { viewModel.loadGroup(it) } }, modifier = Modifier.fillMaxWidth()) {
                Text("Статистика группы")
            }

            OutlinedTextField(uniId, { uniId = it }, label = { Text("ID вуза") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = { uniId.toLongOrNull()?.let { viewModel.loadUniversity(it) } }, modifier = Modifier.fillMaxWidth()) {
                Text("Статистика вуза")
            }

            OutlinedTextField(instId, { instId = it }, label = { Text("ID института") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = { instId.toLongOrNull()?.let { viewModel.loadInstitute(it) } }, modifier = Modifier.fillMaxWidth()) {
                Text("Статистика института")
            }

            OutlinedTextField(roomId, { roomId = it }, label = { Text("ID аудитории") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = { roomId.toLongOrNull()?.let { viewModel.loadClassroom(it) } }, modifier = Modifier.fillMaxWidth()) {
                Text("Нагрузка аудитории")
            }

            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            state.lastText?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }
        }
    }
}
