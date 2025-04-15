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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class University(
    val id: String,
    val name: String,
    val shortName: String,
    val city: String
)

@Composable
fun UniversitySelectionScreen(
    onNext: (University) -> Unit
) {
    // Пример списка университетов
    val universities = remember {
        listOf(
            University("1", "Московский государственный университет имени М.В. Ломоносова", "МГУ", "Москва"),
            University("2", "Санкт-Петербургский государственный университет", "СПбГУ", "Санкт-Петербург"),
            University("3", "Московский физико-технический институт", "МФТИ", "Москва"),
            University("4", "Национальный исследовательский университет Высшая школа экономики", "НИУ ВШЭ", "Москва"),
            University("5", "Национальный исследовательский технологический университет МИСиС", "МИСиС", "Москва"),
            University("6", "Российский университет дружбы народов", "РУДН", "Москва"),
            University("7", "Казанский федеральный университет", "КФУ", "Казань"),
            University("8", "Уральский федеральный университет", "УрФУ", "Екатеринбург"),
            University("9", "Новосибирский государственный университет", "НГУ", "Новосибирск"),
            University("10", "Дальневосточный федеральный университет", "ДВФУ", "Владивосток")
        )
    }
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedUniversity by remember { mutableStateOf<University?>(null) }
    
    // Фильтрация университетов по поисковому запросу
    val filteredUniversities = remember(searchQuery) {
        universities.filter { 
            it.name.contains(searchQuery, ignoreCase = true) || 
            it.shortName.contains(searchQuery, ignoreCase = true) ||
            it.city.contains(searchQuery, ignoreCase = true)
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Заголовок
            Text(
                text = "Выберите учебное заведение",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            
            // Поле поиска
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Поиск университета") },
                leadingIcon = { 
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Поиск"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Список университетов
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(filteredUniversities) { university ->
                    UniversityItem(
                        university = university,
                        isSelected = university == selectedUniversity,
                        onSelect = { selectedUniversity = university }
                    )
                    
                    Divider()
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Кнопка "Далее"
            Text(
                text = "Далее →",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (selectedUniversity != null) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = selectedUniversity != null) { 
                        selectedUniversity?.let { onNext(it) }
                    }
                    .padding(vertical = 16.dp, horizontal = 8.dp)
            )
        }
    }
}

@Composable
fun UniversityItem(
    university: University,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onSelect() },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 0.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                                MaterialTheme.colorScheme.primaryContainer 
                             else 
                                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = { onSelect() }
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = university.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Text(
                    text = "${university.shortName}, ${university.city}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
} 