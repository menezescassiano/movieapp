package com.example.movieapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.movieapp.data.local.entity.SavedCredentialsEntity

@Dao
interface SavedCredentialsDao {

    @Upsert
    suspend fun save(entity: SavedCredentialsEntity)

    @Query("SELECT * FROM saved_credentials WHERE id = 0")
    suspend fun get(): SavedCredentialsEntity?

    @Delete
    suspend fun clear(entity: SavedCredentialsEntity)
}
