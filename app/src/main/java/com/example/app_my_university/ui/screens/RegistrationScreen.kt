package com.example.app_my_university.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app_my_university.model.UserType
import com.example.app_my_university.model.Subject
import com.example.app_my_university.ui.viewmodel.RegistrationViewModel

data class Institute(
    val id: String,
    val name: String
)

data class Direction(
    val id: String,
    val name: String,
    val instituteId: String
)

data class Group(
    val id: String,
    val name: String,
    val directionId: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    universityId: String,
    universityName: String,
    onRegistrationComplete: () -> Unit,
    onChangeUniversity: () -> Unit = {},
    viewModel: RegistrationViewModel = hiltViewModel()
) {
    var userType by remember { mutableStateOf(UserType.STUDENT) }
    val uiState by viewModel.uiState.collectAsState()
    
    // Состояние для отображения диалога смены университета
    var showUniversitySelectionDialog by remember { mutableStateOf(false) }
    
    // Общие поля
    var lastName by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    // Поля для студента
    var courseYear by remember { mutableStateOf("1") }
    var selectedInstitute by remember { mutableStateOf<Institute?>(null) }
    var selectedDirection by remember { mutableStateOf<Direction?>(null) }
    var selectedGroup by remember { mutableStateOf<Group?>(null) }
    
    // Поля для преподавателя
    var subjectSearchQuery by remember { mutableStateOf("") }
    val selectedSubjects = remember { mutableStateListOf<Subject>() }
    
    // Загрузка данных на основе выбранных элементов
    LaunchedEffect(selectedInstitute) {
        selectedInstitute?.let {
            viewModel.loadDirections(it.id)
        }
    }
    
    LaunchedEffect(selectedDirection) {
        selectedDirection?.let {
            viewModel.loadGroups(it.id, courseYear)
        }
    }
    
    LaunchedEffect(courseYear) {
        selectedDirection?.let {
            viewModel.loadGroups(it.id, courseYear)
        }
    }
    
    LaunchedEffect(subjectSearchQuery) {
        viewModel.searchSubjects(subjectSearchQuery)
    }
    
    // Валидация полей
    val isEmailValid = email.endsWith("@gmail.com") && email.length > 10
    val isPasswordValid = password.length >= 6
    val doPasswordsMatch = password == confirmPassword
    
    val isStudentFormValid = lastName.isNotEmpty() && 
                            firstName.isNotEmpty() && 
                            isEmailValid && 
                            isPasswordValid && 
                            doPasswordsMatch &&
                            selectedInstitute != null &&
                            selectedDirection != null &&
                            selectedGroup != null
    
    val isTeacherFormValid = lastName.isNotEmpty() && 
                            firstName.isNotEmpty() && 
                            isEmailValid && 
                            isPasswordValid && 
                            doPasswordsMatch &&
                            selectedSubjects.isNotEmpty()
    
    val isFormValid = if (userType == UserType.STUDENT) isStudentFormValid else isTeacherFormValid
    
    val scrollState = rememberScrollState()
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Заголовок
            Text(
                text = "Регистрация",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            // Карточка с информацией о выбранном университете
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Выбранный университет:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    
                    Text(
                        text = universityName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    
                    Button(
                        onClick = { showUniversitySelectionDialog = true },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Изменить")
                    }
                }
            }
            
            // Выбор типа пользователя
            TabRow(
                selectedTabIndex = if (userType == UserType.STUDENT) 0 else 1,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Tab(
                    selected = userType == UserType.STUDENT,
                    onClick = { userType = UserType.STUDENT },
                    text = { Text("Студент") }
                )
                Tab(
                    selected = userType == UserType.TEACHER,
                    onClick = { userType = UserType.TEACHER },
                    text = { Text("Преподаватель") }
                )
            }
            
            // Общие поля для всех пользователей
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Фамилия*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("Имя*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = middleName,
                onValueChange = { middleName = it },
                label = { Text("Отчество (если есть)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            
            // Поля для студента
            if (userType == UserType.STUDENT) {
                Text(
                    text = "Курс обучения",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectableGroup()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    for (course in 1..5) {
                        Row(
                            modifier = Modifier
                                .selectable(
                                    selected = courseYear == course.toString(),
                                    onClick = { courseYear = course.toString() }
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = courseYear == course.toString(),
                                onClick = null
                            )
                            Text(
                                text = course.toString(),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
                
                // Выбор института
                var isInstituteDropdownExpanded by remember { mutableStateOf(false) }
                
                OutlinedTextField(
                    value = selectedInstitute?.name ?: "",
                    onValueChange = { },
                    label = { Text("Институт*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .onFocusChanged { if (it.isFocused) isInstituteDropdownExpanded = true },
                    readOnly = true,
                    trailingIcon = {
                        Row {
                            if (uiState.isLoadingInstitutes) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .width(24.dp)
                                        .height(24.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        IconButton(onClick = { isInstituteDropdownExpanded = !isInstituteDropdownExpanded }) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Выбрать",
                                modifier = Modifier.padding(8.dp)
                            )
                            }
                        }
                    },
                    supportingText = {
                        if (uiState.instituteError != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "Ошибка",
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = uiState.instituteError ?: "",
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                TextButton(onClick = { viewModel.loadInstitutes() }) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Повторить"
                                    )
                                    Text("Повторить")
                                }
                            }
                        }
                    }
                )
                
                DropdownMenu(
                    expanded = isInstituteDropdownExpanded && !uiState.isLoadingInstitutes && uiState.institutes.isNotEmpty(),
                    onDismissRequest = { isInstituteDropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    uiState.institutes.forEach { institute ->
                        DropdownMenuItem(
                            text = { Text(institute.name) },
                            onClick = {
                                selectedInstitute = institute
                                selectedDirection = null
                                selectedGroup = null
                                isInstituteDropdownExpanded = false
                            }
                        )
                    }
                }
                
                // Выбор направления
                var isDirectionDropdownExpanded by remember { mutableStateOf(false) }
                
                OutlinedTextField(
                    value = selectedDirection?.name ?: "",
                    onValueChange = { },
                    label = { Text("Направление*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .onFocusChanged { if (it.isFocused) isDirectionDropdownExpanded = true },
                    enabled = selectedInstitute != null,
                    readOnly = true,
                    trailingIcon = {
                        Row {
                            if (uiState.isLoadingDirections) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .width(24.dp)
                                        .height(24.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        IconButton(onClick = { 
                            if (selectedInstitute != null) {
                                isDirectionDropdownExpanded = !isDirectionDropdownExpanded
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Выбрать",
                                modifier = Modifier.padding(8.dp)
                            )
                            }
                        }
                    },
                    supportingText = {
                        if (uiState.directionError != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "Ошибка",
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = uiState.directionError ?: "",
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                TextButton(onClick = { 
                                    selectedInstitute?.let { viewModel.loadDirections(it.id) }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Повторить"
                                    )
                                    Text("Повторить")
                                }
                            }
                        }
                    }
                )
                
                DropdownMenu(
                    expanded = isDirectionDropdownExpanded && !uiState.isLoadingDirections && uiState.directions.isNotEmpty(),
                    onDismissRequest = { isDirectionDropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    uiState.directions.forEach { direction ->
                        DropdownMenuItem(
                            text = { Text(direction.name) },
                            onClick = {
                                selectedDirection = direction
                                selectedGroup = null
                                isDirectionDropdownExpanded = false
                            }
                        )
                    }
                }
                
                // Выбор группы
                var isGroupDropdownExpanded by remember { mutableStateOf(false) }
                
                OutlinedTextField(
                    value = selectedGroup?.name ?: "",
                    onValueChange = { },
                    label = { Text("Группа*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .onFocusChanged { if (it.isFocused) isGroupDropdownExpanded = true },
                    enabled = selectedDirection != null,
                    readOnly = true,
                    trailingIcon = {
                        Row {
                            if (uiState.isLoadingGroups) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .width(24.dp)
                                        .height(24.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        IconButton(onClick = { 
                            if (selectedDirection != null) {
                                isGroupDropdownExpanded = !isGroupDropdownExpanded
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Выбрать",
                                modifier = Modifier.padding(8.dp)
                            )
                            }
                        }
                    },
                    supportingText = {
                        if (uiState.groupError != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "Ошибка",
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = uiState.groupError ?: "",
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                TextButton(onClick = { 
                                    selectedDirection?.let { viewModel.loadGroups(it.id, courseYear) }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Повторить"
                                    )
                                    Text("Повторить")
                                }
                            }
                        }
                    }
                )
                
                DropdownMenu(
                    expanded = isGroupDropdownExpanded && !uiState.isLoadingGroups && uiState.groups.isNotEmpty(),
                    onDismissRequest = { isGroupDropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    uiState.groups.forEach { group ->
                        DropdownMenuItem(
                            text = { Text(group.name) },
                            onClick = {
                                selectedGroup = group
                                isGroupDropdownExpanded = false
                            }
                        )
                    }
                }
            }
            
            // Поля для преподавателя
            if (userType == UserType.TEACHER) {
                Text(
                    text = "Выберите предметы, которые вы преподаете",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Отображение состояния загрузки предметов
                if (uiState.isLoadingSubjects) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .width(20.dp)
                                .height(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Загрузка предметов...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                // Поиск предметов
                var isSubjectsDropdownExpanded by remember { mutableStateOf(false) }
                
                OutlinedTextField(
                    value = subjectSearchQuery,
                    onValueChange = { 
                        subjectSearchQuery = it
                        if (it.isNotEmpty()) {
                            isSubjectsDropdownExpanded = true
                        } else {
                            isSubjectsDropdownExpanded = false
                        }
                    },
                    label = { Text("Поиск предметов") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Поиск"
                        )
                    },
                    trailingIcon = {
                        if (subjectSearchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { 
                                    subjectSearchQuery = ""
                                    isSubjectsDropdownExpanded = false
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Очистить поиск"
                                )
                            }
                        }
                    },
                    supportingText = {
                        if (uiState.subjectError != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "Ошибка",
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = uiState.subjectError ?: "",
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                TextButton(onClick = { viewModel.loadSubjects() }) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Повторить"
                                    )
                                    Text("Повторить")
                                }
                            }
                        }
                    }
                )
                
                // Выпадающий список найденных предметов с ограничением высоты
                if (subjectSearchQuery.isNotEmpty() && uiState.searchSubjectsResults.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp) // Фиксированная высота списка
                        ) {
                            items(uiState.searchSubjectsResults) { subject ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (!selectedSubjects.contains(subject)) {
                                                selectedSubjects.add(subject)
                                            }
                                            subjectSearchQuery = ""
                                            isSubjectsDropdownExpanded = false
                                        }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = subject.name,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                
                                if (subject != uiState.searchSubjectsResults.last()) {
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
                
                // Сообщение, если поиск не дал результатов
                if (subjectSearchQuery.isNotEmpty() && uiState.searchSubjectsResults.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Предметы не найдены",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Отображение всех доступных предметов, если поиск не активен
                if (uiState.subjects.isNotEmpty() && subjectSearchQuery.isEmpty()) {
                    Text(
                        text = "Доступные предметы:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp) // Фиксированная высота списка (такая же как у поиска)
                        ) {
                            items(uiState.subjects) { subject ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (!selectedSubjects.contains(subject)) {
                                                selectedSubjects.add(subject)
                                            }
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = subject.name,
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    if (selectedSubjects.contains(subject)) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Выбрано",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                
                                if (subject != uiState.subjects.last()) {
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
                
                // Список выбранных предметов
                if (selectedSubjects.isNotEmpty()) {
                    Text(
                        text = "Выбранные предметы:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            selectedSubjects.sortedBy { it.name }.forEach { subject ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(
                                        text = subject.name,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { selectedSubjects.remove(subject) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Удалить"
                                        )
                                    }
                                }
                                
                                if (subject != selectedSubjects.sortedBy { it.name }.last()) {
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            }
            
            // Общие поля для авторизации
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email* (@gmail.com)") },
                isError = email.isNotEmpty() && !isEmailValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            
            if (email.isNotEmpty() && !isEmailValid) {
                Text(
                    text = "Email должен заканчиваться на @gmail.com",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
            }
            
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль*") },
                isError = password.isNotEmpty() && !isPasswordValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            
            if (password.isNotEmpty() && !isPasswordValid) {
                Text(
                    text = "Пароль должен содержать не менее 6 символов",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
            }
            
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Подтверждение пароля*") },
                isError = confirmPassword.isNotEmpty() && !doPasswordsMatch,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            
            if (confirmPassword.isNotEmpty() && !doPasswordsMatch) {
                Text(
                    text = "Пароли не совпадают",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
            }
            
            // Кнопка регистрации
            Button(
                onClick = { onRegistrationComplete() },
                modifier = Modifier.fillMaxWidth(),
                enabled = isFormValid
            ) {
                Text("Зарегистрироваться")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Ссылка на вход
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Есть аккаунт?")
                TextButton(onClick = onRegistrationComplete) {
                    Text("Войти")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    
    // Диалоговое окно для подтверждения смены университета
    if (showUniversitySelectionDialog) {
        AlertDialog(
            onDismissRequest = { showUniversitySelectionDialog = false },
            title = { Text("Смена университета") },
            text = { 
                Text(
                    "При смене университета введенные данные будут потеряны. Вы уверены, что хотите продолжить?"
                ) 
            },
            confirmButton = {
                Button(
                    onClick = { 
                        showUniversitySelectionDialog = false
                        onChangeUniversity()
                    }
                ) {
                    Text("Да, сменить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showUniversitySelectionDialog = false }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
} 