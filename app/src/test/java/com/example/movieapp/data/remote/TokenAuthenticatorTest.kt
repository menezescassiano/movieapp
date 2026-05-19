package com.example.movieapp.data.remote

import com.example.movieapp.data.SessionManager
import com.example.movieapp.data.TokenStore
import com.example.movieapp.data.local.TokenLocalDataSource
import com.example.movieapp.data.remote.dto.LoginResponse
import com.example.movieapp.data.remote.dto.TokensDto
import com.example.movieapp.data.remote.dto.UserDto
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

    private val fakeLoginResponse = LoginResponse(
        tokens = TokensDto(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
            expiresIn = 900
        ),
        user = UserDto(
            id = "user-1",
            name = "Test User",
            email = "test@example.com",
            city = null,
            profilePictureUrl = null
        )
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

    // ── Cenário 1: happy path — token expirado → refresh bem-sucedido ────

    @Test
    fun `when access token is expired, refresh succeeds and retries request with new token`() =
        runTest {
            // O TokenStore ainda tem o token antigo (igual ao da request)
            every { tokenStore.get() } returns expiredToken
            coEvery { tokenLocalDataSource.getRefreshToken() } returns refreshToken
            coEvery { authApiService.refresh(any()) } returns fakeLoginResponse

            val newRequest = authenticator.authenticate(route = null, response = fake401())

            // Deve ter retornado uma request com o novo token
            assertNotNull("Deveria retornar uma nova Request para retry", newRequest)
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

        // Verifica que os novos tokens foram salvos no banco e no store em memória
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

    // ── Cenário 2: refresh token ausente → logout ────────────────────────

    @Test
    fun `when there is no refresh token, authenticate returns null and triggers logout`() =
        runTest {
            every { tokenStore.get() } returns expiredToken
            coEvery { tokenLocalDataSource.getRefreshToken() } returns null

            val result = authenticator.authenticate(route = null, response = fake401())

            assertNull("Sem refresh token, deve retornar null (sem retry)", result)
            verify { sessionManager.logout() }
        }

    @Test
    fun `when there is no refresh token, the api refresh endpoint is never called`() = runTest {
        every { tokenStore.get() } returns expiredToken
        coEvery { tokenLocalDataSource.getRefreshToken() } returns null

        authenticator.authenticate(route = null, response = fake401())

        coVerify(exactly = 0) { authApiService.refresh(any()) }
    }

    // ── Cenário 3: refresh token presente mas API retorna erro ───────────

    @Test
    fun `when refresh api call throws, authenticate returns null and triggers logout`() = runTest {
        every { tokenStore.get() } returns expiredToken
        coEvery { tokenLocalDataSource.getRefreshToken() } returns refreshToken
        coEvery { authApiService.refresh(any()) } throws RuntimeException("Network error")

        val result = authenticator.authenticate(route = null, response = fake401())

        assertNull("Erro na API de refresh deve retornar null", result)
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

    // ── Cenário 4: outro thread já fez o refresh antes ───────────────────

    @Test
    fun `when token in store is already different from request token, retries without refreshing`() =
        runTest {
            // Simula que outra thread já atualizou o token enquanto essa request estava
            // em voo. O store já tem um token novo, diferente do que foi enviado.
            every { tokenStore.get() } returns newAccessToken // já foi trocado por outra thread
            coEvery { tokenLocalDataSource.getRefreshToken() } returns refreshToken

            val result = authenticator.authenticate(route = null, response = fake401(expiredToken))

            // Deve usar diretamente o novo token sem chamar a API de refresh
            assertNotNull(result)
            assertEquals("Bearer $newAccessToken", result!!.header("Authorization"))
            coVerify(exactly = 0) { authApiService.refresh(any()) }
        }

    // ── Cenário 5: TokenStore em memória vazio (ex: processo reiniciado) ─

    @Test
    fun `when in-memory token store is empty but refresh token exists, refresh still succeeds`() =
        runTest {
            // Após reinício do processo, TokenStore fica null mas o Room ainda tem os tokens.
            // O 401 chega porque AuthInterceptor enviou a request sem header.
            // O header da request original será null nesse caso.
            every { tokenStore.get() } returns null
            coEvery { tokenLocalDataSource.getRefreshToken() } returns refreshToken
            coEvery { authApiService.refresh(any()) } returns fakeLoginResponse

            // Request sem header de Authorization (processo reiniciado, store vazio)
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

            // BUG IDENTIFICADO: requestToken será null e currentToken também é null,
            // então a condição `currentToken != null && currentToken != requestToken`
            // é FALSA. O fluxo continua para o getRefreshToken() corretamente —
            // esse cenário deve funcionar.
            assertNotNull("Deve tentar refresh mesmo com store vazio", result)
            assertEquals("Bearer $newAccessToken", result!!.header("Authorization"))
        }
}
