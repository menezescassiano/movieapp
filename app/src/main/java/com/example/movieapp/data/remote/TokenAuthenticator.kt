package com.example.movieapp.data.remote

import com.example.movieapp.data.SessionManager
import com.example.movieapp.data.TokenStore
import com.example.movieapp.data.local.TokenLocalDataSource
import com.example.movieapp.data.remote.dto.RefreshRequest
import com.example.movieapp.di.NoAuth
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenLocalDataSource: TokenLocalDataSource,
    private val tokenStore: TokenStore,
    private val sessionManager: SessionManager,
    @NoAuth private val authApiService: AuthApiService
) : Authenticator {

    // Synchronized to prevent multiple simultaneous refresh calls
    // when parallel requests all get 401 at the same time.
    override fun authenticate(route: Route?, response: Response): Request? = synchronized(this) {
        val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")
        val currentToken = tokenStore.get()

        // Another thread already refreshed while this request was in flight.
        // Just retry with the new token.
        if (currentToken != null && currentToken != requestToken) {
            return response.request.newBuilder()
                .header("Authorization", "Bearer $currentToken")
                .build()
        }

        // runBlocking is intentional: OkHttp calls authenticate() on a background
        // thread, so blocking here is safe and avoids callback complexity.
        val refreshToken = runBlocking { tokenLocalDataSource.getRefreshToken() }
        if (refreshToken == null) {
            sessionManager.logout()
            return null
        }

        val newTokens = runBlocking {
            try {
                authApiService.refresh(RefreshRequest(refreshToken)).tokens
            } catch (e: Exception) {
                null
            }
        }

        if (newTokens == null) {
            runBlocking { tokenLocalDataSource.clear() }
            tokenStore.clear()
            sessionManager.logout()
            return null
        }

        runBlocking { tokenLocalDataSource.save(newTokens.accessToken, newTokens.refreshToken) }
        tokenStore.save(newTokens.accessToken)

        response.request.newBuilder()
            .header("Authorization", "Bearer ${newTokens.accessToken}")
            .build()
    }
}
