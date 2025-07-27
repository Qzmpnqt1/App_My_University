package com.example.app_my_university.data.api.model

data class RegistrationRequest(
    val lastName: String,
    val firstName: String,
    val middleName: String,
    val email: String,
    val password: String,
    val userType: String,
    val universityId: Int,
    val instituteId: Int? = null,
    val directionId: Int? = null,
    val groupId: Int? = null,
    val courseYear: Int? = null,
    val subjectIds: List<Int>? = null
) 