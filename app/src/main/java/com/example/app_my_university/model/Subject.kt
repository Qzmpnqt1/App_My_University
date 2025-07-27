package com.example.app_my_university.model

data class Subject(
    val id: String,
    val name: String,
    val university: String? = null,
    val institute: String? = null,
    val direction: String? = null,
    val course: Int? = null,
    val semester: Int? = null,
    val lessonTypes: List<LessonType>? = null
) 