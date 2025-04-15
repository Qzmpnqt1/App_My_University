package com.example.app_my_university.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

enum class UserRole {
    STUDENT, TEACHER
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    selectedUniversity: University? = null
) {
    // Состояние полей формы
    var lastName by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var userRole by remember { mutableStateOf(UserRole.STUDENT) }
    
    // Состояние для студента
    var selectedGroup by remember { mutableStateOf("") }
    var groupSearchText by remember { mutableStateOf("") }
    var isGroupExpanded by remember { mutableStateOf(false) }
    
    // Состояние для преподавателя
    var selectedSubjects = remember { mutableStateListOf<String>() }
    var subjectSearchText by remember { mutableStateOf("") }
    var isSubjectExpanded by remember { mutableStateOf(false) }
    
    // Временные данные для демонстрации, учитывающие выбранный университет
    val universityName = selectedUniversity?.shortName ?: "ВУЗ"
    
    // Здесь можно было бы загрузить эти данные из API на основе ID университета
    val availableGroups = remember(selectedUniversity) {
        when(selectedUniversity?.id) {
            "1" -> listOf("МГУ-БПИ21-01", "МГУ-БПИ21-02", "МГУ-ВМК22-01", "МГУ-МАТ23-01")
            "2" -> listOf("СПбГУ-ПИ21-01", "СПбГУ-ФИТ22-01", "СПбГУ-МАТ23-01")
            "3" -> listOf("МФТИ-ИВТ21-01", "МФТИ-ФЭФМ22-01", "МФТИ-ФБМФ23-01")
            "4" -> listOf("ВШЭ-ПИ21-01", "ВШЭ-ИНФ22-01", "ВШЭ-МАТ23-01")
            else -> listOf("БПИ21-01", "БПИ21-02", "БПИ21-03", "БПИ22-01", "БПИ22-02", "ПИН23-01")
        }
    }
    
    val availableSubjects = remember(selectedUniversity) {
        when(selectedUniversity?.id) {
            "1" -> listOf("Математический анализ", "Линейная алгебра", "Дискретная математика", "Программирование на C++", "Алгоритмы и структуры данных", "Базы данных")
            "2" -> listOf("Высшая математика", "Физика", "Программирование на Java", "Web-разработка", "Английский язык", "Операционные системы")
            "3" -> listOf("Математический анализ", "Теоретическая физика", "Дифференциальные уравнения", "Программирование на Python", "Квантовая механика", "Параллельные вычисления")
            "4" -> listOf("Экономическая теория", "Право", "Бизнес-информатика", "Программирование на R", "Машинное обучение", "Анализ данных")
            else -> listOf("Программирование", "Базы данных", "Математика", "Английский язык", "Алгоритмы", "Веб-разработка")
        }
    }
    
    val filteredGroups = availableGroups.filter { 
        it.contains(groupSearchText, ignoreCase = true) 
    }
    
    val filteredSubjects = availableSubjects.filter {
        it.contains(subjectSearchText, ignoreCase = true)
    }
    
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
            
            // Информация о выбранном университете
            if (selectedUniversity != null) {
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
                            style = MaterialTheme.typography.labelLarge
                        )
                        
                        Text(
                            text = selectedUniversity.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = "${selectedUniversity.shortName}, ${selectedUniversity.city}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            // Поля для персональных данных
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Фамилия*") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("Имя*") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = middleName,
                onValueChange = { middleName = it },
                label = { Text("Отчество (если есть)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Поле ввода email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email*") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Поле ввода пароля
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль*") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Поле подтверждения пароля
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Подтвердите пароль*") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Выбор роли пользователя
            Text(
                text = "Выберите роль*:",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = userRole == UserRole.STUDENT,
                    onClick = { userRole = UserRole.STUDENT }
                )
                Text(
                    text = "Студент",
                    modifier = Modifier.clickable { userRole = UserRole.STUDENT }
                )
                
                Spacer(modifier = Modifier.width(24.dp))
                
                RadioButton(
                    selected = userRole == UserRole.TEACHER,
                    onClick = { userRole = UserRole.TEACHER }
                )
                Text(
                    text = "Преподаватель",
                    modifier = Modifier.clickable { userRole = UserRole.TEACHER }
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Динамический блок в зависимости от выбранной роли
            if (userRole == UserRole.STUDENT) {
                Text(
                    text = "Информация о группе в $universityName",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Поле выбора группы с поиском
                ExposedDropdownMenuBox(
                    expanded = isGroupExpanded,
                    onExpandedChange = { isGroupExpanded = it }
                ) {
                    OutlinedTextField(
                        value = groupSearchText,
                        onValueChange = { groupSearchText = it },
                        label = { Text("Введите или выберите группу*") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isGroupExpanded) }
                    )
                    
                    ExposedDropdownMenu(
                        expanded = isGroupExpanded,
                        onDismissRequest = { isGroupExpanded = false }
                    ) {
                        filteredGroups.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group) },
                                onClick = {
                                    selectedGroup = group
                                    groupSearchText = group
                                    isGroupExpanded = false
                                }
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "Предметы преподавания в $universityName",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Поле поиска предметов
                OutlinedTextField(
                    value = subjectSearchText,
                    onValueChange = { subjectSearchText = it },
                    label = { Text("Поиск предметов") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Список всех предметов с чекбоксами
                Text(
                    text = "Выберите предметы (можно несколько):",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                // Список предметов после фильтрации
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    if (filteredSubjects.isEmpty()) {
                        Text(
                            text = "Нет предметов, соответствующих поиску",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        filteredSubjects.forEach { subject ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        if (subject in selectedSubjects) {
                                            selectedSubjects.remove(subject)
                                        } else {
                                            selectedSubjects.add(subject)
                                        }
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = subject in selectedSubjects,
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            selectedSubjects.add(subject)
                                        } else {
                                            selectedSubjects.remove(subject)
                                        }
                                    }
                                )
                                Text(
                                    text = subject,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Отображение количества выбранных предметов
                Text(
                    text = "Выбрано предметов: ${selectedSubjects.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Кнопка регистрации
            Button(
                onClick = {
                    // Проверка валидности данных
                    if (lastName.isNotEmpty() && firstName.isNotEmpty() && 
                        email.isNotEmpty() && password.isNotEmpty() && 
                        password == confirmPassword) {
                        
                        // Дополнительные проверки в зависимости от роли
                        if ((userRole == UserRole.STUDENT && groupSearchText.isNotEmpty()) ||
                            (userRole == UserRole.TEACHER && selectedSubjects.isNotEmpty())) {
                            onRegisterSuccess()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Зарегистрироваться")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Ссылка на экран входа
            Text(
                text = "Уже есть аккаунт? Войти",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToLogin() }
                    .padding(vertical = 8.dp)
            )
        }
    }
}
