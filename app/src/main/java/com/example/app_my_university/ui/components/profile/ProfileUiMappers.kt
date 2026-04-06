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

/** Краткая строка вуза и институтов для списков и дашборда преподавателя. */
fun UserProfileResponse.teacherWorkplaceSummary(): String? {
    val tp = teacherProfile ?: return null
    val parts = buildList {
        tp.universityName?.trim()?.takeIf { it.isNotEmpty() }?.let { add(it) }
        if (!tp.institutesFromAssignments.isNullOrEmpty()) {
            add(tp.institutesFromAssignments.joinToString(", "))
        } else {
            tp.instituteName?.trim()?.takeIf { it.isNotEmpty() }?.let { add(it) }
        }
    }
    return parts.takeIf { it.isNotEmpty() }?.joinToString(" · ")
}

fun userRoleLabelRu(userType: String): String = when (userType) {
    "STUDENT" -> "Студент"
    "TEACHER" -> "Преподаватель"
    "ADMIN" -> "Администратор"
    "SUPER_ADMIN" -> "Суперадминистратор"
    else -> userType
}

fun UserProfileResponse.roleSectionTitle(): String = when (userType) {
    "STUDENT" -> "Учёба"
    "TEACHER" -> "Работа в вузе"
    "ADMIN" -> "Служебная информация"
    "SUPER_ADMIN" -> "Глобальное администрирование"
    else -> "Дополнительно"
}

fun UserProfileResponse.roleSectionSubtitle(): String? = when (userType) {
    "STUDENT" -> "Группа и подразделение"
    "TEACHER" -> "Вуз, институты по нагрузке и должность"
    "ADMIN" -> "Ваш вуз и роль в системе"
    "SUPER_ADMIN" -> "Доступ ко всем вузам системы"
    else -> null
}

/** Краткие строки под ФИО в шапке (без дублирования email). */
fun UserProfileResponse.heroContextLines(): List<String> = when (userType) {
    "STUDENT" -> buildList {
        studentProfile?.course?.let { add("$it курс") }
        studentProfile?.groupName?.let { add("Группа $it") }
        studentProfile?.instituteName?.let { add(it) }
    }
    "TEACHER" -> buildList {
        teacherProfile?.universityName?.takeIf { it.isNotBlank() }?.let { add(it) }
        val fromAssign = teacherProfile?.institutesFromAssignments
        if (!fromAssign.isNullOrEmpty()) {
            add(fromAssign.joinToString(", "))
        } else {
            teacherProfile?.instituteName?.takeIf { it.isNotBlank() }?.let { add(it) }
        }
        teacherProfile?.position?.let { add(it) }
    }
    "ADMIN" -> buildList {
        val uni = adminProfile?.universityName?.trim().orEmpty()
        if (uni.isNotEmpty()) add(uni)
        else add("Вуз будет отображён после привязки учётной записи")
    }
    "SUPER_ADMIN" -> buildList {
        adminProfile?.universityName?.trim()?.takeIf { it.isNotEmpty() }?.let { add(it) }
            ?: add("Глобальный администратор")
    }
    else -> emptyList()
}
