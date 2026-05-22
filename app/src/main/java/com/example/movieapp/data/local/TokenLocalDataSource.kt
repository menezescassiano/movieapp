package com.example.movieapp.data.local

import com.example.movieapp.data.local.dao.TokenDao
import com.example.movieapp.data.local.entity.TokenEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenLocalDataSource
    @Inject
    constructor(
        private val dao: TokenDao,
    ) {
        suspend fun save(
            accessToken: String,
            refreshToken: String,
        ) = dao.save(TokenEntity(accessToken = accessToken, refreshToken = refreshToken))

        suspend fun get(): String? = dao.get()?.accessToken

        suspend fun getRefreshToken(): String? = dao.get()?.refreshToken

        suspend fun clear() {
            val entity = dao.get() ?: return
            dao.clear(entity)
        }
    }
