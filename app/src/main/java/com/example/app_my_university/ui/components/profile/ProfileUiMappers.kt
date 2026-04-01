package com.example.app_my_university.ui.components.profile

import com.example.app_my_university.data.api.model.UserProfileResponse

fun UserProfileResponse.fullDisplayName(): String =
    buildString {
        append(lastName.trim())
        append(" ")
        append(firstName.trim())
        middleName?.trim()?.takeIf { it.isNotEmpty() }?.let {
            append(" ")
            append(it)
        }
    }.trim()

fun UserProfileResponse.userInitials(): String {
    val a = lastName.firstOrNull()?.uppercaseChar()
    val b = firstName.firstOrNull()?.uppercaseChar()
    return when {
        a != null && b != null -> "$a$b"
        a != null -> "$a"
        b != null -> "$b"
        else -> "?"
    }
}

fun userRoleLabelRu(userType: String): String = when (userType) {
    "STUDENT" -> "Студент"
    "TEACHER" -> "Преподаватель"
    "ADMIN" -> "Администратор"
    else -> userType
}

fun UserProfileResponse.roleSectionTitle(): String = when (userType) {
    "STUDENT" -> "Учёба"
    "TEACHER" -> "Работа в вузе"
    "ADMIN" -> "Служебная информация"
    else -> "Дополнительно"
}

fun UserProfileResponse.roleSectionSubtitle(): String? = when (userType) {
    "STUDENT" -> "Группа и подразделение"
    "TEACHER" -> "Подразделение и должность"
    "ADMIN" -> "Ваш вуз и роль в системе"
    else -> null
}

/** Краткие строки под ФИО в шапке (без дублирования email). */
fun UserProfileResponse.heroContextLines(): List<String> = when (userType) {
    "STUDENT" -> buildList {
        studentProfile?.groupName?.let { add("Группа $it") }
        studentProfile?.instituteName?.let { add(it) }
    }
    "TEACHER" -> buildList {
        teacherProfile?.position?.let { add(it) }
        teacherProfile?.instituteName?.let { add(it) }
    }
    "ADMIN" -> buildList {
        val uni = adminProfile?.universityName?.trim().orEmpty()
        if (uni.isNotEmpty()) add(uni)
        else add("Вуз будет отображён после привязки учётной записи")
    }
    else -> emptyList()
}
