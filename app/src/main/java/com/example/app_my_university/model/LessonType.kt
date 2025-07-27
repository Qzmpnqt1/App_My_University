package com.example.app_my_university.model

import androidx.compose.ui.graphics.Color

enum class LessonType(val displayName: String, val color: Color) {
    LECTURE("Лекция", Color(0xFF2196F3)),
    SEMINAR("Семинар", Color(0xFF4CAF50)),
    LABORATORY("Лабораторная", Color(0xFFF44336)),
    PRACTICE("Практика", Color(0xFFFF9800))
} 