package com.example.movieapp.data.local

import com.example.movieapp.data.local.dao.SavedCredentialsDao
import com.example.movieapp.data.local.entity.SavedCredentialsEntity
import com.example.movieapp.model.SavedCredentials
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CredentialsLocalDataSource
    @Inject
    constructor(
        private val dao: SavedCredentialsDao,
    ) {
        suspend fun save(
            email: String,
            password: String,
        ) = dao.save(SavedCredentialsEntity(email = email, password = password))

        suspend fun get(): SavedCredentials? = dao.get()?.let { SavedCredentials(email = it.email, password = it.password) }

        suspend fun clear() {
            val entity = dao.get() ?: return
            dao.clear(entity)
        }
    }
