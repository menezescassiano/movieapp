package com.example.movieapp.data

import com.example.movieapp.model.AuthResult
import com.example.movieapp.model.SavedCredentials

interface AuthRepository {
    suspend fun register(name: String, email: String, password: String): AuthResult
    suspend fun login(email: String, password: String): AuthResult
    suspend fun logout()
    suspend fun restoreToken()
    suspend fun hasSavedToken(): Boolean
    suspend fun getSavedCredentials(): SavedCredentials?
    suspend fun clearSavedCredentials()
}
