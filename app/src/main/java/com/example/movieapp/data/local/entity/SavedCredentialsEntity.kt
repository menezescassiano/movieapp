package com.example.movieapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_credentials")
data class SavedCredentialsEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int = 0, // single-row table
    @ColumnInfo(name = "email")
    val email: String,
    @ColumnInfo(name = "password")
    val password: String,
)
