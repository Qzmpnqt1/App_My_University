package com.example.app_my_university.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// ──────────────────────────────────────────────────────────────────────────────
// Simple data model (stubbed – later you can bind to real API / ViewModel)
// ──────────────────────────────────────────────────────────────────────────────
data class NewsArticle(
    val id: String,
    val title: String,
    val summary: String,
    val imageUrl: String,
    val publishedAt: LocalDateTime
)

// ──────────────────────────────────────────────────────────────────────────────
// PUBLIC API – Screen entry point (replaces the old TODO("Not yet implemented"))
// ──────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen() {
    // • Replace with a ViewModel later. This is just to stop crashes & demo UI.
    val articles = remember {
        listOf(
            NewsArticle(
                "1",
                "Университет открыл новый IT‑центр",
                "Центр займётся исследованиями в области ИИ и Big Data.",
                "https://picsum.photos/600/400?random=1",
                LocalDateTime.now().minusDays(1)
            ),
            NewsArticle(
                "2",
                "Старт приёма заявок на грант молодым учёным",
                "Студенты и аспиранты могут получить до 1 млн ₽ на реализацию проекта.",
                "https://picsum.photos/600/400?random=2",
                LocalDateTime.now().minusHours(5)
            )
        )
    }

    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = { Text("Новости") })
    }) { pv ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(pv)
        ) {
            items(articles) { NewsCard(it) }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Reusable composable for article preview
// ──────────────────────────────────────────────────────────────────────────────
@Composable
private fun NewsCard(article: NewsArticle) {
    val formatter = DateTimeFormatter.ofPattern("dd MMMM • HH:mm")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(article.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            Column(Modifier.padding(16.dp)) {
                Text(article.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(article.summary, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    article.publishedAt.format(formatter),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}