package com.example.movieapp.di

import android.content.Context
import androidx.room.Room
import com.example.movieapp.data.local.AppDatabase
import com.example.movieapp.data.local.CredentialsDatabase
import com.example.movieapp.data.local.dao.SavedCredentialsDao
import com.example.movieapp.data.local.dao.TokenDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    // Token DB — destructive migration is acceptable since the token can be restored via login.
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase =
        Room
            .databaseBuilder(context, AppDatabase::class.java, "movieapp.db")
            .fallbackToDestructiveMigration(true)
            .build()

    // Credentials DB — isolated database, no destructive migration.
    // The schema never changes: losing saved credentials would be a bad user experience.
    @Provides
    @Singleton
    fun provideCredentialsDatabase(
        @ApplicationContext context: Context,
    ): CredentialsDatabase =
        Room
            .databaseBuilder(context, CredentialsDatabase::class.java, "credentials.db")
            .build()

    @Provides
    fun provideTokenDao(database: AppDatabase): TokenDao = database.tokenDao()

    @Provides
    fun provideSavedCredentialsDao(database: CredentialsDatabase): SavedCredentialsDao = database.savedCredentialsDao()
}
