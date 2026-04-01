package com.example.app_my_university.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CacheEntryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cacheDao(): CacheDao
}
