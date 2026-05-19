package com.example.movieapp.data

import com.example.movieapp.data.local.CredentialsLocalDataSource
import com.example.movieapp.data.local.TokenLocalDataSource
import com.example.movieapp.data.remote.AuthApiService
import com.example.movieapp.data.remote.dto.LoginRequest
import com.example.movieapp.data.remote.dto.RegisterRequest
import com.example.movieapp.data.remote.dto.toDomain
import com.example.movieapp.model.AuthResult
import com.example.movieapp.model.SavedCredentials
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val tokenStore: TokenStore,
    private val tokenLocalDataSource: TokenLocalDataSource,
    private val credentialsDataSource: CredentialsLocalDataSource
) : AuthRepository {

    override suspend fun register(name: String, email: String, password: String): AuthResult {
        try {
            val result = apiService.register(RegisterRequest(name, email, password)).toDomain()
            tokenStore.save(result.accessToken)
            tokenLocalDataSource.save(result.accessToken, result.refreshToken)
            return result
        } catch (e: HttpException) {
            throw when (e.code()) {
                409 -> AuthException.EmailAlreadyInUse
                400 -> AuthException.BadRequest
                else -> AuthException.ServerError
            }
        } catch (_: IOException) {
            throw AuthException.NoNetwork
        }
    }

    override suspend fun login(email: String, password: String): AuthResult {
        try {
            val result = apiService.login(LoginRequest(email, password)).toDomain()
            tokenStore.save(result.accessToken)
            tokenLocalDataSource.save(result.accessToken, result.refreshToken)
            credentialsDataSource.save(email, password)
            return result
        } catch (e: HttpException) {
            throw when (e.code()) {
                401 -> AuthException.InvalidCredentials
                400 -> AuthException.BadRequest
                else -> AuthException.ServerError
            }
        } catch (_: IOException) {
            throw AuthException.NoNetwork
        }
    }

    override suspend fun logout() {
        tokenLocalDataSource.clear()
        tokenStore.clear()
    }

    override suspend fun restoreToken() {
        val token = tokenLocalDataSource.get() ?: return
        tokenStore.save(token)
    }

    override suspend fun hasSavedToken(): Boolean =
        tokenLocalDataSource.getRefreshToken() != null

    override suspend fun getSavedCredentials(): SavedCredentials? =
        credentialsDataSource.get()

    override suspend fun clearSavedCredentials() =
        credentialsDataSource.clear()
}

sealed class AuthException(message: String) : Exception(message) {
    object InvalidCredentials : AuthException("Invalid credentials")
    object EmailAlreadyInUse : AuthException("Email already in use")
    object BadRequest : AuthException("Bad request")
    object NoNetwork : AuthException("No network connection")
    object ServerError : AuthException("Server error")
}
