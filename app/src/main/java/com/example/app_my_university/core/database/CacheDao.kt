package com.example.app_my_university.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CacheDao {

    @Query("SELECT * FROM cache_entries WHERE `key` = :key LIMIT 1")
    suspend fun get(key: String): CacheEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CacheEntryEntity)

    @Query("DELETE FROM cache_entries")
    suspend fun clearAll()
}
