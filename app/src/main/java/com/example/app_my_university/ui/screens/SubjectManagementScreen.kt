package com.example.app_my_university.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.app_my_university.model.LessonType
import com.example.app_my_university.model.Subject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectManagementScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var showEditSubjectDialog by remember { mutableStateOf<Subject?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf<Subject?>(null) }
    var selectedUniversity by remember { mutableStateOf<String?>(null) }
    var selectedInstitute by remember { mutableStateOf<String?>(null) }
    var selectedDirection by remember { mutableStateOf<String?>(null) }
    var showFilterMenu by remember { mutableStateOf(false) }
    
    // В реальном приложении эти данные будут загружаться из БД
    val universities = listOf("Все", "МГУ", "МГТУ", "МФТИ")
    val institutes = listOf("Все", "Мехмат", "Физфак", "Информатика", "Робототехника")
    val directions = listOf("Все", "Прикладная математика", "Физика", "Программная инженерия")
    
    // Пример данных о предметах
    val subjects = remember {
        listOf(
            Subject(
                id = "1",
                name = "Математический анализ",
                university = "МГУ",
                institute = "Мехмат",
                direction = "Прикладная математика",
                course = 1,
                semester = 1,
                lessonTypes = listOf(LessonType.LECTURE, LessonType.SEMINAR)
            ),
            Subject(
                id = "2",
                name = "Линейная алгебра",
                university = "МГУ",
                institute = "Мехмат",
                direction = "Прикладная математика",
                course = 1,
                semester = 1,
                lessonTypes = listOf(LessonType.LECTURE, LessonType.SEMINAR, LessonType.PRACTICE)
            ),
            Subject(
                id = "3",
                name = "Программирование",
                university = "МГТУ",
                institute = "Информатика",
                direction = "Программная инженерия",
                course = 1,
                semester = 1,
                lessonTypes = listOf(LessonType.LECTURE, LessonType.PRACTICE, LessonType.LABORATORY)
            ),
            Subject(
                id = "4",
                name = "Базы данных",
                university = "МГТУ",
                institute = "Информатика",
                direction = "Программная инженерия",
                course = 2,
                semester = 1,
                lessonTypes = listOf(LessonType.LECTURE, LessonType.PRACTICE)
            ),
            Subject(
                id = "5",
                name = "Механика",
                university = "МФТИ",
                institute = "Физфак",
                direction = "Физика",
                course = 1,
                semester = 2,
                lessonTypes = listOf(LessonType.LECTURE, LessonType.SEMINAR, LessonType.LABORATORY)
            )
        )
    }
    
    // Фильтрация предметов
    val filteredSubjects = subjects.filter {
        (selectedUniversity == null || selectedUniversity == "Все" || it.university == selectedUniversity) &&
        (selectedInstitute == null || selectedInstitute == "Все" || it.institute == selectedInstitute) &&
        (selectedDirection == null || selectedDirection == "Все" || it.direction == selectedDirection) &&
        (searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true))
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Верхняя панель
            TopAppBar(
                title = { Text("Управление предметами") },
                actions = {
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Фильтры",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        Text(
                            text = "Фильтры",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        
                        Divider()
                        
                        // ВУЗ
                        Text(
                            text = "ВУЗ",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        
                        universities.forEach { university ->
                            DropdownMenuItem(
                                text = { Text(university) },
                                onClick = {
                                    selectedUniversity = university.takeUnless { it == "Все" }
                                    showFilterMenu = false
                                }
                            )
                        }
                        
                        Divider()
                        
                        // Институт
                        Text(
                            text = "Институт",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        
                        institutes.forEach { institute ->
                            DropdownMenuItem(
                                text = { Text(institute) },
                                onClick = {
                                    selectedInstitute = institute.takeUnless { it == "Все" }
                                    showFilterMenu = false
                                }
                            )
                        }
                        
                        Divider()
                        
                        // Направление
                        Text(
                            text = "Направление",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        
                        directions.forEach { direction ->
                            DropdownMenuItem(
                                text = { Text(direction) },
                                onClick = {
                                    selectedDirection = direction.takeUnless { it == "Все" }
                                    showFilterMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.height(48.dp)
            )
            
            // Содержимое экрана
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Поисковая строка
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Поиск по названию предмета") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Поиск"
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Отображение активных фильтров
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectedUniversity != null || selectedInstitute != null || selectedDirection != null) {
                        Text(
                            text = "Активные фильтры:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        selectedUniversity?.let {
                            Surface(
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "ВУЗ: $it",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        
                        selectedInstitute?.let {
                            Surface(
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "Институт: $it",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        
                        selectedDirection?.let {
                            Surface(
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "Направление: $it",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
                
                // Счетчик предметов
                Text(
                    text = "Найдено: ${filteredSubjects.size} предметов",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Список предметов
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredSubjects) { subject ->
                        SubjectCard(
                            subject = subject,
                            onEditClick = { showEditSubjectDialog = subject },
                            onDeleteClick = { showDeleteConfirmation = subject }
                        )
                    }
                }
            }
        }
        
        // Кнопка добавления предмета
        FloatingActionButton(
            onClick = { showAddSubjectDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Добавить предмет")
        }
    }
    
    // Диалог добавления предмета
    if (showAddSubjectDialog) {
        SubjectDialog(
            subject = null,
            onDismiss = { showAddSubjectDialog = false },
            onSave = { 
                // В реальном приложении здесь будет логика сохранения нового предмета
                showAddSubjectDialog = false
            },
            universities = universities.filterNot { it == "Все" },
            institutes = institutes.filterNot { it == "Все" },
            directions = directions.filterNot { it == "Все" }
        )
    }
    
    // Диалог редактирования предмета
    showEditSubjectDialog?.let { subject ->
        SubjectDialog(
            subject = subject,
            onDismiss = { showEditSubjectDialog = null },
            onSave = { 
                // В реальном приложении здесь будет логика обновления предмета
                showEditSubjectDialog = null
            },
            universities = universities.filterNot { it == "Все" },
            institutes = institutes.filterNot { it == "Все" },
            directions = directions.filterNot { it == "Все" }
        )
    }
    
    // Диалог подтверждения удаления
    showDeleteConfirmation?.let { subject ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = null },
            title = { Text("Удаление предмета") },
            text = { 
                Text("Вы действительно хотите удалить предмет \"${subject.name}\"?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        // В реальном приложении здесь будет логика удаления предмета
                        showDeleteConfirmation = null
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = null }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun SubjectCard(
    subject: Subject,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Заголовок и кнопки управления
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Иконка и название предмета
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = subject.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Кнопки управления
                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Редактировать",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Удалить",
                            tint = Color.Red
                        )
                    }
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Информация о предмете
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    InfoRow(label = "ВУЗ", value = subject.university ?: "-")
                    InfoRow(label = "Институт", value = subject.institute ?: "-")
                    InfoRow(label = "Направление", value = subject.direction ?: "-")
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    InfoRow(label = "Курс", value = subject.course?.toString() ?: "-")
                    InfoRow(label = "Семестр", value = subject.semester?.toString() ?: "-")
                    InfoRow(
                        label = "Типы занятий",
                        value = subject.lessonTypes?.joinToString(", ") { lessonType ->
                            when (lessonType) {
                                LessonType.LECTURE -> "Лекция"
                                LessonType.SEMINAR -> "Семинар"
                                LessonType.PRACTICE -> "Практика"
                                LessonType.LABORATORY -> "Лабораторная"
                            }
                        } ?: "-"
                    )
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDialog(
    subject: Subject?,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    universities: List<String>,
    institutes: List<String>,
    directions: List<String>
) {
    // Состояния для полей формы
    var name by remember { mutableStateOf(subject?.name ?: "") }
    var university by remember { mutableStateOf(subject?.university ?: universities.firstOrNull() ?: "") }
    var institute by remember { mutableStateOf(subject?.institute ?: institutes.firstOrNull() ?: "") }
    var direction by remember { mutableStateOf(subject?.direction ?: directions.firstOrNull() ?: "") }
    var course by remember { mutableStateOf(subject?.course?.toString() ?: "1") }
    var semester by remember { mutableStateOf(subject?.semester?.toString() ?: "1") }
    
    // Состояния для типов занятий
    var hasLecture by remember { mutableStateOf(subject?.lessonTypes?.contains(LessonType.LECTURE) ?: false) }
    var hasSeminar by remember { mutableStateOf(subject?.lessonTypes?.contains(LessonType.SEMINAR) ?: false) }
    var hasPractice by remember { mutableStateOf(subject?.lessonTypes?.contains(LessonType.PRACTICE) ?: false) }
    var hasLaboratory by remember { mutableStateOf(subject?.lessonTypes?.contains(LessonType.LABORATORY) ?: false) }
    
    // Выпадающие меню
    var showUniversityMenu by remember { mutableStateOf(false) }
    var showInstituteMenu by remember { mutableStateOf(false) }
    var showDirectionMenu by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (subject == null) "Добавление предмета" else "Редактирование предмета") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Название предмета
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название предмета") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // ВУЗ
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = university,
                        onValueChange = {},
                        label = { Text("ВУЗ") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showUniversityMenu = true }) {
                                Icon(Icons.Default.Menu, contentDescription = "Выбрать ВУЗ")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    DropdownMenu(
                        expanded = showUniversityMenu,
                        onDismissRequest = { showUniversityMenu = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        universities.forEach { univ ->
                            DropdownMenuItem(
                                text = { Text(univ) },
                                onClick = {
                                    university = univ
                                    showUniversityMenu = false
                                }
                            )
                        }
                    }
                }
                
                // Институт
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = institute,
                        onValueChange = {},
                        label = { Text("Институт") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showInstituteMenu = true }) {
                                Icon(Icons.Default.Menu, contentDescription = "Выбрать институт")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    DropdownMenu(
                        expanded = showInstituteMenu,
                        onDismissRequest = { showInstituteMenu = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        institutes.forEach { inst ->
                            DropdownMenuItem(
                                text = { Text(inst) },
                                onClick = {
                                    institute = inst
                                    showInstituteMenu = false
                                }
                            )
                        }
                    }
                }
                
                // Направление
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = direction,
                        onValueChange = {},
                        label = { Text("Направление") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showDirectionMenu = true }) {
                                Icon(Icons.Default.Menu, contentDescription = "Выбрать направление")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    DropdownMenu(
                        expanded = showDirectionMenu,
                        onDismissRequest = { showDirectionMenu = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        directions.forEach { dir ->
                            DropdownMenuItem(
                                text = { Text(dir) },
                                onClick = {
                                    direction = dir
                                    showDirectionMenu = false
                                }
                            )
                        }
                    }
                }
                
                // Курс и семестр
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = course,
                        onValueChange = { if (it.toIntOrNull() != null || it.isEmpty()) course = it },
                        label = { Text("Курс") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    
                    OutlinedTextField(
                        value = semester,
                        onValueChange = { if (it.toIntOrNull() != null || it.isEmpty()) semester = it },
                        label = { Text("Семестр") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Типы занятий
                Text(
                    text = "Типы занятий",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LessonTypeCheckbox("Лекции", hasLecture) { hasLecture = it }
                    LessonTypeCheckbox("Семинары", hasSeminar) { hasSeminar = it }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LessonTypeCheckbox("Практики", hasPractice) { hasPractice = it }
                    LessonTypeCheckbox("Лаборат.", hasLaboratory) { hasLaboratory = it }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = name.isNotBlank() && university.isNotBlank() && 
                         institute.isNotBlank() && direction.isNotBlank() &&
                         course.isNotBlank() && semester.isNotBlank() &&
                         (hasLecture || hasSeminar || hasPractice || hasLaboratory)
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun LessonTypeCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .border(
                width = 1.dp,
                color = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(4.dp)
            )
            .background(
                if (checked) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else MaterialTheme.colorScheme.surface
            )
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.Checkbox(
            checked = checked,
            onCheckedChange = { onCheckedChange(it) }
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
    }
} 