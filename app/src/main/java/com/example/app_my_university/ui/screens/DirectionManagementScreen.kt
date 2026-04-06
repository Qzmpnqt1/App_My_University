package com.example.app_my_university.ui.screens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.app_my_university.data.api.model.StudyDirectionRequest
import com.example.app_my_university.data.api.model.StudyDirectionResponse
import com.example.app_my_university.ui.components.common.MuEmptyState
import com.example.app_my_university.ui.components.common.MuLoadingState
import com.example.app_my_university.ui.components.RoleShellScaffold
import com.example.app_my_university.ui.components.UniformTopAppBar
import com.example.app_my_university.ui.navigation.AppRole
import com.example.app_my_university.ui.theme.Dimens
import com.example.app_my_university.ui.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectionManagementScreen(
    navController: NavHostController,
    onNavigateBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var search by remember { mutableStateOf("") }
    var showEditor by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<StudyDirectionResponse?>(null) }
    var deleting by remember { mutableStateOf<StudyDirectionResponse?>(null) }
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var instituteId by remember { mutableStateOf<Long?>(null) }
    var instituteMenuExpanded by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadAdminContext()
    }
    LaunchedEffect(uiState.adminUniversityId, uiState.isSuperAdmin) {
        viewModel.loadInstitutes(uiState.adminUniversityId)
        viewModel.loadDirections(null)
    }

    LaunchedEffect(uiState.actionSuccess) {
        if (uiState.actionSuccess) {
            uiState.actionMessage?.let { snackbar.showSnackbar(it) }
            viewModel.clearActionSuccess()
        }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbar.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val filtered = remember(search, uiState.directions) {
        val q = search.trim().lowercase()
        if (q.isEmpty()) uiState.directions
        else uiState.directions.filter { d ->
            d.name.lowercase().contains(q) ||
                (d.code?.lowercase()?.contains(q) == true) ||
                (d.instituteName?.lowercase()?.contains(q) == true)
        }
    }

    RoleShellScaffold(
        role = AppRole.Admin,
        navController = navController,
        topBar = {
            UniformTopAppBar(
                title = "Направления подготовки",
                onBackPressed = onNavigateBack,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editing = null
                    name = ""
                    code = ""
                    instituteId = uiState.institutes.firstOrNull()?.id
                    showEditor = true
                },
            ) { Icon(Icons.Default.Add, contentDescription = "Добавить направление") }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Dimens.screenPadding),
        ) {
            if (uiState.isSuperAdmin && uiState.adminUniversityId == null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Dimens.spaceM),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f),
                ) {
                    Text(
                        "Глобальный режим: доступны направления всех вузов. При создании выберите институт из списка.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Поиск") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
            )
            Text(
                "Найдено: ${filtered.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Dimens.spaceS, bottom = Dimens.spaceM),
            )
            when {
                uiState.isLoading && uiState.directions.isEmpty() -> {
                    MuLoadingState(Modifier.fillMaxSize())
                }
                filtered.isEmpty() -> {
                    MuEmptyState(
                        title = "Нет направлений",
                        subtitle = "Добавьте направление и укажите институт.",
                    )
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(Dimens.spaceM),
                        contentPadding = PaddingValues(bottom = 88.dp),
                    ) {
                        items(filtered, key = { it.id }) { d ->
                            DirectionCard(
                                direction = d,
                                onEdit = {
                                    editing = d
                                    name = d.name
                                    code = d.code ?: ""
                                    instituteId = d.instituteId
                                    showEditor = true
                                },
                                onDelete = { deleting = d },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        val institutes = uiState.institutes
        AlertDialog(
            onDismissRequest = { showEditor = false },
            title = { Text(if (editing == null) "Новое направление" else "Редактирование") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (institutes.isEmpty()) {
                        Text(
                            "Сначала создайте институт во «Вузе и институтах».",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    } else {
                        ExposedDropdownMenuBox(
                            expanded = instituteMenuExpanded,
                            onExpandedChange = { instituteMenuExpanded = it },
                        ) {
                            OutlinedTextField(
                                value = institutes.find { it.id == instituteId }?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Институт *") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = instituteMenuExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                            )
                            ExposedDropdownMenu(
                                expanded = instituteMenuExpanded,
                                onDismissRequest = { instituteMenuExpanded = false },
                            ) {
                                institutes.forEach { inst ->
                                    DropdownMenuItem(
                                        text = { Text("${inst.name} (${inst.universityName ?: "вуз"})") },
                                        onClick = {
                                            instituteId = inst.id
                                            instituteMenuExpanded = false
                                        },
                                    )
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Название *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Код") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                val iid = instituteId
                Button(
                    onClick = {
                        if (iid == null) return@Button
                        val req = StudyDirectionRequest(
                            name = name.trim(),
                            code = code.ifBlank { null },
                            instituteId = iid,
                        )
                        if (editing == null) viewModel.createDirection(req)
                        else viewModel.updateDirection(editing!!.id, req)
                        showEditor = false
                    },
                    enabled = name.isNotBlank() && instituteId != null && institutes.isNotEmpty(),
                ) { Text("Сохранить") }
            },
            dismissButton = { TextButton({ showEditor = false }) { Text("Отмена") } },
        )
    }

    deleting?.let { d ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Удалить направление?") },
            text = { Text("«${d.name}» будет удалено, если нет блокирующих связей на сервере.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDirection(d.id)
                        deleting = null
                    },
                ) { Text("Удалить") }
            },
            dismissButton = { TextButton({ deleting = null }) { Text("Отмена") } },
        )
    }
}

@Composable
private fun DirectionCard(
    direction: StudyDirectionResponse,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(Dimens.spaceM)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    direction.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Изменить") }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Удалить", tint = MaterialTheme.colorScheme.error)
                }
            }
            direction.code?.let {
                Text(
                    "Код: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                "Институт: ${direction.instituteName ?: "—"}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
