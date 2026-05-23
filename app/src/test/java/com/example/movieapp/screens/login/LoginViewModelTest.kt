package com.example.movieapp.screens.login

import com.example.movieapp.data.AuthException
import com.example.movieapp.domain.GetSavedCredentialsUseCase
import com.example.movieapp.domain.LoginUseCase
import com.example.movieapp.model.AuthResult
import com.example.movieapp.model.SavedCredentials
import com.example.movieapp.model.User
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var loginUseCase: LoginUseCase
    private lateinit var getSavedCredentialsUseCase: GetSavedCredentialsUseCase
    private lateinit var viewModel: LoginViewModel

    private val fakeAuthResult =
        AuthResult(
            accessToken = "access-token",
            refreshToken = "refresh-token",
            expiresIn = 3600,
            user = User(id = "1", name = "John", email = "user@example.com", city = "São Paulo"),
        )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        loginUseCase = mockk()
        getSavedCredentialsUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): LoginViewModel = LoginViewModel(loginUseCase, getSavedCredentialsUseCase)

    // ── estado inicial ───────────────────────────────────────────────────

    @Test
    fun `initial state has empty fields and no errors`() =
        runTest {
            coEvery { getSavedCredentialsUseCase() } returns null

            viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("", state.email)
            assertEquals("", state.password)
            assertFalse(state.isLoading)
            assertFalse(state.loginSuccess)
            assertNull(state.emailError)
            assertNull(state.passwordError)
            assertNull(state.errorMessage)
            assertFalse(state.passwordVisible)
        }

    // ── init: credenciais salvas ─────────────────────────────────────────

    @Test
    fun `init loads saved credentials when available`() =
        runTest {
            coEvery { getSavedCredentialsUseCase() } returns
                SavedCredentials(
                    email = "user@example.com",
                    password = "secret123",
                )

            viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("user@example.com", state.email)
            assertEquals("secret123", state.password)
        }

    @Test
    fun `init does not change fields when no saved credentials`() =
        runTest {
            coEvery { getSavedCredentialsUseCase() } returns null

            viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("", state.email)
            assertEquals("", state.password)
        }

    // ── onEmailChange ────────────────────────────────────────────────────

    @Test
    fun `onEmailChange updates email and clears emailError`() =
        runTest {
            coEvery { getSavedCredentialsUseCase() } returns null
            viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onContinueClick("Invalid email", "Empty password")
            assertTrue(viewModel.uiState.value.emailError != null)

            viewModel.onEmailChange("new@email.com")

            assertEquals("new@email.com", viewModel.uiState.value.email)
            assertNull(viewModel.uiState.value.emailError)
        }

    // ── onPasswordChange ─────────────────────────────────────────────────

    @Test
    fun `onPasswordChange updates password and clears passwordError`() =
        runTest {
            coEvery { getSavedCredentialsUseCase() } returns null
            viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEmailChange("user@example.com")
            viewModel.onContinueClick("Invalid email", "Empty password")
            assertTrue(viewModel.uiState.value.passwordError != null)

            viewModel.onPasswordChange("newpass")

            assertEquals("newpass", viewModel.uiState.value.password)
            assertNull(viewModel.uiState.value.passwordError)
        }

    // ── onTogglePasswordVisibility ───────────────────────────────────────

    @Test
    fun `onTogglePasswordVisibility toggles passwordVisible`() =
        runTest {
            coEvery { getSavedCredentialsUseCase() } returns null
            viewModel = createViewModel()
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.passwordVisible)

            viewModel.onTogglePasswordVisibility()
            assertTrue(viewModel.uiState.value.passwordVisible)

            viewModel.onTogglePasswordVisibility()
            assertFalse(viewModel.uiState.value.passwordVisible)
        }

    // ── onContinueClick: validação ────────────────────────────────────────

    @Test
    fun `onContinueClick with invalid email sets emailError`() =
        runTest {
            coEvery { getSavedCredentialsUseCase() } returns null
            viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEmailChange("not-an-email")
            viewModel.onPasswordChange("password123")
            viewModel.onContinueClick("Invalid email", "Empty password")

            assertEquals("Invalid email", viewModel.uiState.value.emailError)
            assertNull(viewModel.uiState.value.passwordError)
        }

    @Test
    fun `onContinueClick with blank password sets passwordError`() =
        runTest {
            coEvery { getSavedCredentialsUseCase() } returns null
            viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEmailChange("valid@email.com")
            viewModel.onPasswordChange("   ")
            viewModel.onContinueClick("Invalid email", "Empty password")

            assertNull(viewModel.uiState.value.emailError)
            assertEquals("Empty password", viewModel.uiState.value.passwordError)
        }

    @Test
    fun `onContinueClick with both fields invalid sets both errors and never calls loginUseCase`() =
        runTest {
            coEvery { getSavedCredentialsUseCase() } returns null
            viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onContinueClick("Invalid email", "Empty password")

            assertEquals("Invalid email", viewModel.uiState.value.emailError)
            assertEquals("Empty password", viewModel.uiState.value.passwordError)
            assertFalse(viewModel.uiState.value.isLoading)
            coVerify(exactly = 0) { loginUseCase(any(), any()) }
        }

    // ── onContinueClick: login com sucesso ────────────────────────────────

    @Test
    fun `onContinueClick with valid data calls loginUseCase and sets loginSuccess`() =
        runTest {
            coEvery { getSavedCredentialsUseCase() } returns null
            coEvery { loginUseCase("user@example.com", "password123") } returns fakeAuthResult
            viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEmailChange("user@example.com")
            viewModel.onPasswordChange("password123")
            viewModel.onContinueClick("Invalid email", "Empty password")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.loginSuccess)
            assertFalse(state.isLoading)
            assertNull(state.errorMessage)
            coVerify(exactly = 1) { loginUseCase("user@example.com", "password123") }
        }

    @Test
    fun `onContinueClick success clears previous errorMessage`() =
        runTest {
            coEvery { getSavedCredentialsUseCase() } returns null
            coEvery { loginUseCase(any(), any()) } throws AuthException.InvalidCredentials
            viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEmailChange("user@example.com")
            viewModel.onPasswordChange("wrongpass")
            viewModel.onContinueClick("Invalid email", "Empty password")
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.errorMessage != null)

            coEvery { loginUseCase(any(), any()) } returns fakeAuthResult
            viewModel.onContinueClick("Invalid email", "Empty password")
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.errorMessage)
            assertTrue(viewModel.uiState.value.loginSuccess)
        }

    // ── onContinueClick: login com falha ──────────────────────────────────

    @Test
    fun `onContinueClick with InvalidCredentials sets errorMessage`() =
        runTest {
            coEvery { getSavedCredentialsUseCase() } returns null
            coEvery { loginUseCase(any(), any()) } throws AuthException.InvalidCredentials
            viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEmailChange("user@example.com")
            viewModel.onPasswordChange("wrongpass")
            viewModel.onContinueClick("Invalid email", "Empty password")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("Invalid credentials", state.errorMessage)
            assertFalse(state.isLoading)
            assertFalse(state.loginSuccess)
        }

    @Test
    fun `onContinueClick with NoNetwork sets errorMessage`() =
        runTest {
            coEvery { getSavedCredentialsUseCase() } returns null
            coEvery { loginUseCase(any(), any()) } throws AuthException.NoNetwork
            viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEmailChange("user@example.com")
            viewModel.onPasswordChange("password123")
            viewModel.onContinueClick("Invalid email", "Empty password")
            advanceUntilIdle()

            assertEquals("No network connection", viewModel.uiState.value.errorMessage)
            assertFalse(viewModel.uiState.value.loginSuccess)
        }

    @Test
    fun `onContinueClick with ServerError sets errorMessage`() =
        runTest {
            coEvery { getSavedCredentialsUseCase() } returns null
            coEvery { loginUseCase(any(), any()) } throws AuthException.ServerError
            viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEmailChange("user@example.com")
            viewModel.onPasswordChange("password123")
            viewModel.onContinueClick("Invalid email", "Empty password")
            advanceUntilIdle()

            assertEquals("Server error", viewModel.uiState.value.errorMessage)
        }

    // ── onErrorDismissed ─────────────────────────────────────────────────

    @Test
    fun `onErrorDismissed clears errorMessage`() =
        runTest {
            coEvery { getSavedCredentialsUseCase() } returns null
            coEvery { loginUseCase(any(), any()) } throws AuthException.InvalidCredentials
            viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEmailChange("user@example.com")
            viewModel.onPasswordChange("wrongpass")
            viewModel.onContinueClick("Invalid email", "Empty password")
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.errorMessage != null)

            viewModel.onErrorDismissed()

            assertNull(viewModel.uiState.value.errorMessage)
        }
}
