package com.example.app_my_university.data.api.model

data class AcademicGroupDTO(
    val id: Int,
    val name: String,
    val course: Int,
    val yearOfAdmission: Int,
    val directionId: Int
) 