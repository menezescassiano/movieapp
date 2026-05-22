package com.example.movieapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.movieapp.data.local.dao.SavedCredentialsDao
import com.example.movieapp.data.local.entity.SavedCredentialsEntity

// Separate and isolated database — never uses fallbackToDestructiveMigration.
// The credentials schema is fixed: id (0), email, password.
@Database(
    entities = [SavedCredentialsEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class CredentialsDatabase : RoomDatabase() {
    abstract fun savedCredentialsDao(): SavedCredentialsDao
}
