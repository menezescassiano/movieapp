package com.example.movieapp.data

import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory token store. Lives as long as the process.
 * Replace with EncryptedSharedPreferences if persistence across restarts is needed.
 */
@Singleton
class TokenStore @Inject constructor() {

    @Volatile
    private var accessToken: String? = null

    fun save(token: String) {
        accessToken = token
    }

    fun get(): String? = accessToken

    fun clear() {
        accessToken = null
    }
}
