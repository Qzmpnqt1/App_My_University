package com.example.app_my_university.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cache_entries")
data class CacheEntryEntity(
    @PrimaryKey val key: String,
    val payload: String,
    val updatedAtMillis: Long
)
