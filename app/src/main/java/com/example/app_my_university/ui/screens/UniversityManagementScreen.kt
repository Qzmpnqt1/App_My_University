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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.app_my_university.model.University

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversityManagementScreen(
    onNavigateBack: () -> Unit
) {
    // Состояние для управления университетами
    val universities = remember { 
        mutableStateListOf(
            University("1", "Московский государственный университет имени М.В. Ломоносова", "МГУ", "Москва"),
            University("2", "Национальный исследовательский университет «Высшая школа экономики»", "ВШЭ", "Москва"),
            University("3", "Московский физико-технический институт", "МФТИ", "Москва"),
            University("4", "Российский университет дружбы народов", "РУДН", "Москва"),
            University("5", "Московский государственный технический университет им. Н.Э. Баумана", "МГТУ", "Москва")
        )
    }
    
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var currentUniversity by remember { mutableStateOf<University?>(null) }
    
    // Фильтрация университетов по поисковому запросу
    val filteredUniversities = remember(searchQuery, universities) {
        if (searchQuery.isEmpty()) {
            universities
        } else {
            universities.filter { 
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.shortName.contains(searchQuery, ignoreCase = true) ||
                it.city.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Топ-бар с учетом системных инсетов
            TopAppBar(
                title = { Text("Управление университетами") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.height(48.dp)
            )
            
            // Основной контент
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Поле поиска
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Поиск университета") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Поиск"
                        )
                    },
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Список университетов
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredUniversities) { university ->
                        UniversityManagementItem(
                            university = university,
                            onEdit = {
                                currentUniversity = university
                                showEditDialog = true
                            },
                            onDelete = {
                                currentUniversity = university
                                showDeleteDialog = true
                            }
                        )
                        
                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
        
        // Кнопка добавления нового университета
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Добавить университет",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        
        // Диалог добавления университета
        if (showAddDialog) {
            UniversityDialog(
                university = null,
                onDismiss = { showAddDialog = false },
                onSave = { name, shortName, city ->
                    val newId = (universities.maxByOrNull { it.id.toInt() }?.id?.toInt() ?: 0) + 1
                    universities.add(University(newId.toString(), name, shortName, city))
                    showAddDialog = false
                }
            )
        }
        
        // Диалог редактирования университета
        if (showEditDialog && currentUniversity != null) {
            UniversityDialog(
                university = currentUniversity,
                onDismiss = { showEditDialog = false },
                onSave = { name, shortName, city ->
                    val index = universities.indexOfFirst { it.id == currentUniversity!!.id }
                    if (index != -1) {
                        universities[index] = University(currentUniversity!!.id, name, shortName, city)
                    }
                    showEditDialog = false
                }
            )
        }
        
        // Диалог подтверждения удаления
        if (showDeleteDialog && currentUniversity != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Удаление университета") },
                text = { Text("Вы уверены, что хотите удалить ${currentUniversity!!.name}?") },
                confirmButton = {
                    Button(
                        onClick = {
                            universities.removeIf { it.id == currentUniversity!!.id }
                            showDeleteDialog = false
                        }
                    ) {
                        Text("Удалить")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteDialog = false }
                    ) {
                        Text("Отмена")
                    }
                }
            )
        }
    }
}

@Composable
fun UniversityManagementItem(
    university: University,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Цветовой индикатор
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 40.dp)
                .background(MaterialTheme.colorScheme.primary)
                .clip(RoundedCornerShape(2.dp))
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Информация об университете
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = university.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Row {
                Text(
                    text = university.shortName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = university.city,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Кнопки действий
        IconButton(onClick = onEdit) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Редактировать",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Удалить",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversityDialog(
    university: University?,
    onDismiss: () -> Unit,
    onSave: (name: String, shortName: String, city: String) -> Unit
) {
    val isEditing = university != null
    
    var name by remember { mutableStateOf(university?.name ?: "") }
    var shortName by remember { mutableStateOf(university?.shortName ?: "") }
    var city by remember { mutableStateOf(university?.city ?: "") }
    
    var nameError by remember { mutableStateOf<String?>(null) }
    var shortNameError by remember { mutableStateOf<String?>(null) }
    var cityError by remember { mutableStateOf<String?>(null) }
    
    fun validate(): Boolean {
        var isValid = true
        
        if (name.isBlank()) {
            nameError = "Название не может быть пустым"
            isValid = false
        } else {
            nameError = null
        }
        
        if (shortName.isBlank()) {
            shortNameError = "Аббревиатура не может быть пустой"
            isValid = false
        } else {
            shortNameError = null
        }
        
        if (city.isBlank()) {
            cityError = "Город не может быть пустым"
            isValid = false
        } else {
            cityError = null
        }
        
        return isValid
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Редактирование университета" else "Добавление университета") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { 
                        name = it
                        nameError = null
                    },
                    label = { Text("Полное название") },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                
                OutlinedTextField(
                    value = shortName,
                    onValueChange = { 
                        shortName = it
                        shortNameError = null
                    },
                    label = { Text("Аббревиатура") },
                    isError = shortNameError != null,
                    supportingText = shortNameError?.let { { Text(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                
                OutlinedTextField(
                    value = city,
                    onValueChange = { 
                        city = it
                        cityError = null
                    },
                    label = { Text("Город") },
                    isError = cityError != null,
                    supportingText = cityError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (validate()) {
                        onSave(name, shortName, city)
                    }
                }
            ) {
                Text(if (isEditing) "Сохранить" else "Добавить")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Отмена")
            }
        }
    )
} 