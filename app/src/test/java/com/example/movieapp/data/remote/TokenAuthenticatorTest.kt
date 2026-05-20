package com.example.movieapp.data.remote

import com.example.movieapp.data.SessionManager
import com.example.movieapp.data.TokenStore
import com.example.movieapp.data.local.TokenLocalDataSource
import com.example.movieapp.data.remote.dto.RefreshResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class TokenAuthenticatorTest {

    // ── Doubles ──────────────────────────────────────────────────────────

    private lateinit var tokenLocalDataSource: TokenLocalDataSource
    private lateinit var tokenStore: TokenStore
    private lateinit var sessionManager: SessionManager
    private lateinit var authApiService: AuthApiService

    private lateinit var authenticator: TokenAuthenticator

    // ── Helpers ──────────────────────────────────────────────────────────

    private val expiredToken = "expired.access.token"
    private val refreshToken = "valid.refresh.token"
    private val newAccessToken = "new.access.token"
    private val newRefreshToken = "new.refresh.token"

    private val fakeLoginResponse = RefreshResponse(
        accessToken = newAccessToken,
        refreshToken = newRefreshToken,
        expiresIn = 900
    )

    /** Builds a fake 401 [Response] whose request carries the given Bearer token. */
    private fun fake401(bearerToken: String = expiredToken): Response {
        val request = Request.Builder()
            .url("https://api.example.com/movies")
            .header("Authorization", "Bearer $bearerToken")
            .build()

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()
    }

    @Before
    fun setUp() {
        tokenLocalDataSource = mockk(relaxed = true)
        tokenStore = mockk(relaxed = true)
        sessionManager = mockk(relaxed = true)
        authApiService = mockk()

        authenticator = TokenAuthenticator(
            tokenLocalDataSource = tokenLocalDataSource,
            tokenStore = tokenStore,
            sessionManager = sessionManager,
            authApiService = authApiService
        )
    }

    // ── Scenario 1: happy path — expired token → successful refresh ──────

    @Test
    fun `when access token is expired, refresh succeeds and retries request with new token`() =
        runTest {
            // TokenStore still holds the old token (matching the one in the request)
            every { tokenStore.get() } returns expiredToken
            coEvery { tokenLocalDataSource.getRefreshToken() } returns refreshToken
            coEvery { authApiService.refresh(any()) } returns fakeLoginResponse

            val newRequest = authenticator.authenticate(route = null, response = fake401())

            // Should have returned a new Request with the refreshed token
            assertNotNull("Should return a new Request for retry", newRequest)
            assertEquals(
                "Bearer $newAccessToken",
                newRequest!!.header("Authorization")
            )
        }

    @Test
    fun `when access token is expired, new tokens are persisted after refresh`() = runTest {
        every { tokenStore.get() } returns expiredToken
        coEvery { tokenLocalDataSource.getRefreshToken() } returns refreshToken
        coEvery { authApiService.refresh(any()) } returns fakeLoginResponse

        authenticator.authenticate(route = null, response = fake401())

        // Verifies that the new tokens were persisted in the database and in the in-memory store
        coVerify { tokenLocalDataSource.save(newAccessToken, newRefreshToken) }
        verify { tokenStore.save(newAccessToken) }
    }

    @Test
    fun `when access token is expired, refresh request is sent with the stored refresh token`() =
        runTest {
            every { tokenStore.get() } returns expiredToken
            coEvery { tokenLocalDataSource.getRefreshToken() } returns refreshToken
            coEvery { authApiService.refresh(any()) } returns fakeLoginResponse

            authenticator.authenticate(route = null, response = fake401())

            coVerify { authApiService.refresh(match { it.refreshToken == refreshToken }) }
        }

    // ── Scenario 2: missing refresh token → logout ───────────────────────

    @Test
    fun `when there is no refresh token, authenticate returns null and triggers logout`() =
        runTest {
            every { tokenStore.get() } returns expiredToken
            coEvery { tokenLocalDataSource.getRefreshToken() } returns null

            val result = authenticator.authenticate(route = null, response = fake401())

            assertNull("No refresh token: should return null (no retry)", result)
            verify { sessionManager.logout() }
        }

    @Test
    fun `when there is no refresh token, the api refresh endpoint is never called`() = runTest {
        every { tokenStore.get() } returns expiredToken
        coEvery { tokenLocalDataSource.getRefreshToken() } returns null

        authenticator.authenticate(route = null, response = fake401())

        coVerify(exactly = 0) { authApiService.refresh(any()) }
    }

    // ── Scenario 3: refresh token present but API returns an error ───────

    @Test
    fun `when refresh api call throws, authenticate returns null and triggers logout`() = runTest {
        every { tokenStore.get() } returns expiredToken
        coEvery { tokenLocalDataSource.getRefreshToken() } returns refreshToken
        coEvery { authApiService.refresh(any()) } throws RuntimeException("Network error")

        val result = authenticator.authenticate(route = null, response = fake401())

        assertNull("Refresh API error should return null", result)
        verify { sessionManager.logout() }
    }

    @Test
    fun `when refresh api call throws, local tokens are cleared`() = runTest {
        every { tokenStore.get() } returns expiredToken
        coEvery { tokenLocalDataSource.getRefreshToken() } returns refreshToken
        coEvery { authApiService.refresh(any()) } throws RuntimeException("Network error")

        authenticator.authenticate(route = null, response = fake401())

        coVerify { tokenLocalDataSource.clear() }
        verify { tokenStore.clear() }
    }

    // ── Scenario 4: another thread already refreshed first ───────────────

    @Test
    fun `when token in store is already different from request token, retries without refreshing`() =
        runTest {
            // Simulates another thread having refreshed the token while this request was
            // in flight. The store already holds a new token, different from the one that was sent.
            every { tokenStore.get() } returns newAccessToken // already updated by another thread
            coEvery { tokenLocalDataSource.getRefreshToken() } returns refreshToken

            val result = authenticator.authenticate(route = null, response = fake401(expiredToken))

            // Should use the new token directly without calling the refresh API
            assertNotNull(result)
            assertEquals("Bearer $newAccessToken", result!!.header("Authorization"))
            coVerify(exactly = 0) { authApiService.refresh(any()) }
        }

    // ── Scenario 5: in-memory TokenStore is empty (e.g. process restart) ─

    @Test
    fun `when in-memory token store is empty but refresh token exists, refresh still succeeds`() =
        runTest {
            // After a process restart, TokenStore is null but Room still has the tokens.
            // The 401 arrives because AuthInterceptor sent the request without a header.
            // The original request's Authorization header will be null in this case.
            every { tokenStore.get() } returns null
            coEvery { tokenLocalDataSource.getRefreshToken() } returns refreshToken
            coEvery { authApiService.refresh(any()) } returns fakeLoginResponse

            // Request with no Authorization header (process restarted, store is empty)
            val requestWithNoToken = Request.Builder()
                .url("https://api.example.com/movies")
                .build()
            val responseWithNoToken = Response.Builder()
                .request(requestWithNoToken)
                .protocol(Protocol.HTTP_1_1)
                .code(401)
                .message("Unauthorized")
                .build()

            val result = authenticator.authenticate(route = null, response = responseWithNoToken)

            // NOTE: requestToken will be null and currentToken is also null, so the
            // condition `currentToken != null && currentToken != requestToken` is FALSE.
            // Execution correctly falls through to getRefreshToken() — this scenario works.
            assertNotNull("Should attempt refresh even with empty store", result)
            assertEquals("Bearer $newAccessToken", result!!.header("Authorization"))
        }
}
