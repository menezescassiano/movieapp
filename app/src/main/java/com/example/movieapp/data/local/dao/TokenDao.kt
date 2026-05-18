package com.example.movieapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.movieapp.data.local.entity.TokenEntity

@Dao
interface TokenDao {

    @Upsert
    suspend fun save(entity: TokenEntity)

    @Query("SELECT * FROM token WHERE id = 0")
    suspend fun get(): TokenEntity?

    @Delete
    suspend fun clear(entity: TokenEntity)
}
