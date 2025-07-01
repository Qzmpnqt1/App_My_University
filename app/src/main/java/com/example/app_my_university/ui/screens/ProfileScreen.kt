package com.example.app_my_university.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

// Модель данных студента
data class StudentProfile(
    val fullName: String,
    val email: String,
    val password: String,
    val institute: String,
    val department: String,
    val group: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackPressed: () -> Unit = {}
) {
    // Пример данных студента (в реальном приложении будут загружаться из базы данных)
    val studentProfile = remember {
        StudentProfile(
            fullName = "Иванов Иван Иванович",
            email = "ivanov@student.university.ru",
            password = "password123",
            institute = "Институт информационных технологий",
            department = "Программная инженерия",
            group = "БПИ21-01"
        )
    }
    
    var isEditing by remember { mutableStateOf(false) }
    
    var editedFullName by remember { mutableStateOf(studentProfile.fullName) }
    var editedEmail by remember { mutableStateOf(studentProfile.email) }
    var editedPassword by remember { mutableStateOf(studentProfile.password) }
    var editedInstitute by remember { mutableStateOf(studentProfile.institute) }
    var editedDepartment by remember { mutableStateOf(studentProfile.department) }
    var editedGroup by remember { mutableStateOf(studentProfile.group) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Личные данные") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isEditing = !isEditing },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = if (isEditing) "Сохранить" else "Редактировать",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Аватар пользователя
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Аватар пользователя",
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                if (isEditing) {
                    // Режим редактирования
                    ProfileFieldEdit(
                        icon = Icons.Default.AccountCircle,
                        label = "ФИО",
                        value = editedFullName,
                        onValueChange = { editedFullName = it }
                    )
                    
                    ProfileFieldEdit(
                        icon = Icons.Default.Email,
                        label = "Email",
                        value = editedEmail,
                        onValueChange = { editedEmail = it }
                    )
                    
                    ProfileFieldEdit(
                        icon = Icons.Default.Key,
                        label = "Пароль",
                        value = editedPassword,
                        onValueChange = { editedPassword = it },
                        isPassword = true
                    )
                    
                    ProfileFieldEdit(
                        icon = Icons.Default.School,
                        label = "Институт",
                        value = editedInstitute,
                        onValueChange = { editedInstitute = it }
                    )
                    
                    ProfileFieldEdit(
                        icon = Icons.Default.Subject,
                        label = "Направление",
                        value = editedDepartment,
                        onValueChange = { editedDepartment = it }
                    )
                    
                    ProfileFieldEdit(
                        icon = Icons.Default.Groups,
                        label = "Группа",
                        value = editedGroup,
                        onValueChange = { editedGroup = it }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { isEditing = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Сохранить изменения")
                    }
                } else {
                    // Режим просмотра
                    Text(
                        text = studentProfile.fullName,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Студент",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    ProfileInfoCard(
                        title = "Учебная информация",
                        items = listOf(
                            Pair(Icons.Default.School, "Институт: ${studentProfile.institute}"),
                            Pair(Icons.Default.Subject, "Направление: ${studentProfile.department}"),
                            Pair(Icons.Default.Groups, "Группа: ${studentProfile.group}")
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ProfileInfoCard(
                        title = "Контактная информация",
                        items = listOf(
                            Pair(Icons.Default.Email, "Email: ${studentProfile.email}"),
                            Pair(Icons.Default.Key, "Пароль: ${"•".repeat(studentProfile.password.length)}")
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileInfoCard(
    title: String,
    items: List<Pair<ImageVector, String>>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            items.forEachIndexed { index, (icon, text) ->
                if (index > 0) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
                
                ProfileInfoRow(icon = icon, text = text)
            }
        }
    }
}

@Composable
fun ProfileInfoRow(
    icon: ImageVector,
    text: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.align(Alignment.CenterStart),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 32.dp)
        )
    }
}

@Composable
fun ProfileFieldEdit(
    icon: ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { 
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        singleLine = true
    )
}
