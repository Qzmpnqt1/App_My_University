package com.example.app_my_university.ui.audit

/**
 * Значения фильтров журнала аудита. Должны совпадать с аргументами action / entityType
 * в вызовах AuditService.log(...) на сервере (Server_My_University).
 */
data class AuditFilterOption(
    /** null — без фильтра по полю */
    val apiValue: String?,
    /** Подпись в UI (русская + код для однозначности) */
    val labelRu: String,
)

object AuditFilterCatalog {

    private val knownActions = listOf(
        AuditFilterOption("ACTIVATE_USER", "Активация пользователя · ACTIVATE_USER"),
        AuditFilterOption("APPROVE_REGISTRATION", "Одобрение заявки на регистрацию · APPROVE_REGISTRATION"),
        AuditFilterOption("CREATE_ADMIN_ACCOUNT", "Создание аккаунта администратора · CREATE_ADMIN_ACCOUNT"),
        AuditFilterOption("CREATE_GRADE", "Создание оценки · CREATE_GRADE"),
        AuditFilterOption("CREATE_PRACTICE_GRADE", "Создание оценки за практику · CREATE_PRACTICE_GRADE"),
        AuditFilterOption("CREATE_SCHEDULE", "Создание записи расписания · CREATE_SCHEDULE"),
        AuditFilterOption("DEACTIVATE_USER", "Деактивация пользователя · DEACTIVATE_USER"),
        AuditFilterOption("DELETE_SCHEDULE", "Удаление записи расписания · DELETE_SCHEDULE"),
        AuditFilterOption("REJECT_REGISTRATION", "Отклонение заявки на регистрацию · REJECT_REGISTRATION"),
        AuditFilterOption("UPDATE_GRADE", "Изменение оценки · UPDATE_GRADE"),
        AuditFilterOption("UPDATE_PRACTICE_GRADE", "Изменение оценки за практику · UPDATE_PRACTICE_GRADE"),
        AuditFilterOption("UPDATE_SCHEDULE", "Изменение записи расписания · UPDATE_SCHEDULE"),
    )

    private val knownEntityTypes = listOf(
        AuditFilterOption("Grade", "Оценка · Grade"),
        AuditFilterOption("PracticeGrade", "Оценка за практику · PracticeGrade"),
        AuditFilterOption("RegistrationRequest", "Заявка на регистрацию · RegistrationRequest"),
        AuditFilterOption("Schedule", "Расписание · Schedule"),
        AuditFilterOption("Users", "Пользователи · Users"),
    )

    val actionChoices: List<AuditFilterOption> =
        listOf(AuditFilterOption(null, "Любое действие")) +
            knownActions.sortedBy { it.labelRu }

    val entityTypeChoices: List<AuditFilterOption> =
        listOf(AuditFilterOption(null, "Любая сущность")) +
            knownEntityTypes.sortedBy { it.labelRu }
}
