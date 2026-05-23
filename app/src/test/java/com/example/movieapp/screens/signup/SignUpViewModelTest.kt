package com.example.movieapp.screens.signup

import com.example.movieapp.data.AuthException
import com.example.movieapp.domain.SignUpUseCase
import com.example.movieapp.model.AuthResult
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
class SignUpViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var signUpUseCase: SignUpUseCase
    private lateinit var viewModel: SignUpViewModel

    private val fakeAuthResult =
        AuthResult(
            accessToken = "access-token",
            refreshToken = "refresh-token",
            expiresIn = 3600,
            user = User(id = "1", name = "John Doe", email = "john@example.com", city = "São Paulo"),
        )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        signUpUseCase = mockk()
        viewModel = SignUpViewModel(signUpUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── estado inicial ───────────────────────────────────────────────────

    @Test
    fun `initial state has empty fields and no errors`() {
        val state = viewModel.uiState.value
        assertEquals("", state.name)
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertEquals("", state.confirmPassword)
        assertFalse(state.isLoading)
        assertFalse(state.signUpSuccess)
        assertFalse(state.passwordVisible)
        assertFalse(state.confirmPasswordVisible)
        assertNull(state.nameError)
        assertNull(state.emailError)
        assertNull(state.passwordError)
        assertNull(state.confirmPasswordError)
        assertNull(state.errorMessage)
    }

    // ── onNameChange ─────────────────────────────────────────────────────

    @Test
    fun `onNameChange updates name and clears nameError`() {
        viewModel.onSignUpClick("Empty name", "Invalid email", "Short password", "Password mismatch")
        assertTrue(viewModel.uiState.value.nameError != null)

        viewModel.onNameChange("John Doe")

        assertEquals("John Doe", viewModel.uiState.value.name)
        assertNull(viewModel.uiState.value.nameError)
    }

    // ── onEmailChange ────────────────────────────────────────────────────

    @Test
    fun `onEmailChange updates email and clears emailError`() {
        viewModel.onSignUpClick("Empty name", "Invalid email", "Short password", "Password mismatch")
        viewModel.onEmailChange("invalid")
        viewModel.onSignUpClick("Empty name", "Invalid email", "Short password", "Password mismatch")
        assertTrue(viewModel.uiState.value.emailError != null)

        viewModel.onEmailChange("valid@email.com")

        assertEquals("valid@email.com", viewModel.uiState.value.email)
        assertNull(viewModel.uiState.value.emailError)
    }

    // ── onPasswordChange ─────────────────────────────────────────────────

    @Test
    fun `onPasswordChange updates password and clears passwordError`() {
        viewModel.onNameChange("John")
        viewModel.onEmailChange("john@example.com")
        viewModel.onPasswordChange("123")
        viewModel.onSignUpClick("Empty name", "Invalid email", "Short password", "Password mismatch")
        assertTrue(viewModel.uiState.value.passwordError != null)

        viewModel.onPasswordChange("newpassword")

        assertEquals("newpassword", viewModel.uiState.value.password)
        assertNull(viewModel.uiState.value.passwordError)
    }

    // ── onConfirmPasswordChange ───────────────────────────────────────────

    @Test
    fun `onConfirmPasswordChange updates confirmPassword and clears confirmPasswordError`() {
        viewModel.onNameChange("John")
        viewModel.onEmailChange("john@example.com")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("differentpass")
        viewModel.onSignUpClick("Empty name", "Invalid email", "Short password", "Password mismatch")
        assertTrue(viewModel.uiState.value.confirmPasswordError != null)

        viewModel.onConfirmPasswordChange("password123")

        assertEquals("password123", viewModel.uiState.value.confirmPassword)
        assertNull(viewModel.uiState.value.confirmPasswordError)
    }

    // ── onTogglePasswordVisibility ────────────────────────────────────────

    @Test
    fun `onTogglePasswordVisibility toggles passwordVisible`() {
        assertFalse(viewModel.uiState.value.passwordVisible)

        viewModel.onTogglePasswordVisibility()
        assertTrue(viewModel.uiState.value.passwordVisible)

        viewModel.onTogglePasswordVisibility()
        assertFalse(viewModel.uiState.value.passwordVisible)
    }

    // ── onToggleConfirmPasswordVisibility ─────────────────────────────────

    @Test
    fun `onToggleConfirmPasswordVisibility toggles confirmPasswordVisible`() {
        assertFalse(viewModel.uiState.value.confirmPasswordVisible)

        viewModel.onToggleConfirmPasswordVisibility()
        assertTrue(viewModel.uiState.value.confirmPasswordVisible)

        viewModel.onToggleConfirmPasswordVisibility()
        assertFalse(viewModel.uiState.value.confirmPasswordVisible)
    }

    // ── onSignUpClick: validação ──────────────────────────────────────────

    @Test
    fun `onSignUpClick with blank name sets nameError`() {
        viewModel.onEmailChange("john@example.com")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("password123")

        viewModel.onSignUpClick("Empty name", "Invalid email", "Short password", "Password mismatch")

        assertEquals("Empty name", viewModel.uiState.value.nameError)
        assertNull(viewModel.uiState.value.emailError)
        assertNull(viewModel.uiState.value.passwordError)
        assertNull(viewModel.uiState.value.confirmPasswordError)
    }

    @Test
    fun `onSignUpClick with invalid email sets emailError`() {
        viewModel.onNameChange("John")
        viewModel.onEmailChange("not-valid")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("password123")

        viewModel.onSignUpClick("Empty name", "Invalid email", "Short password", "Password mismatch")

        assertNull(viewModel.uiState.value.nameError)
        assertEquals("Invalid email", viewModel.uiState.value.emailError)
    }

    @Test
    fun `onSignUpClick with password shorter than 6 chars sets passwordError`() {
        viewModel.onNameChange("John")
        viewModel.onEmailChange("john@example.com")
        viewModel.onPasswordChange("12345")
        viewModel.onConfirmPasswordChange("12345")

        viewModel.onSignUpClick("Empty name", "Invalid email", "Short password", "Password mismatch")

        assertEquals("Short password", viewModel.uiState.value.passwordError)
    }

    @Test
    fun `onSignUpClick with password mismatch sets confirmPasswordError`() {
        viewModel.onNameChange("John")
        viewModel.onEmailChange("john@example.com")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("different123")

        viewModel.onSignUpClick("Empty name", "Invalid email", "Short password", "Password mismatch")

        assertEquals("Password mismatch", viewModel.uiState.value.confirmPasswordError)
        assertNull(viewModel.uiState.value.passwordError)
    }

    @Test
    fun `onSignUpClick with all fields invalid sets all errors and never calls signUpUseCase`() {
        viewModel.onSignUpClick("Empty name", "Invalid email", "Short password", "Password mismatch")

        assertEquals("Empty name", viewModel.uiState.value.nameError)
        assertEquals("Invalid email", viewModel.uiState.value.emailError)
        assertEquals("Short password", viewModel.uiState.value.passwordError)
        assertEquals("Password mismatch", viewModel.uiState.value.confirmPasswordError)
        assertFalse(viewModel.uiState.value.isLoading)
        coVerify(exactly = 0) { signUpUseCase(any(), any(), any()) }
    }

    @Test
    fun `onSignUpClick password of exactly 6 chars passes validation`() =
        runTest {
            coEvery { signUpUseCase(any(), any(), any()) } returns fakeAuthResult

            viewModel.onNameChange("John")
            viewModel.onEmailChange("john@example.com")
            viewModel.onPasswordChange("123456")
            viewModel.onConfirmPasswordChange("123456")

            viewModel.onSignUpClick("Empty name", "Invalid email", "Short password", "Password mismatch")
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.passwordError)
            assertTrue(viewModel.uiState.value.signUpSuccess)
        }

    // ── onSignUpClick: cadastro com sucesso ───────────────────────────────

    @Test
    fun `onSignUpClick with valid data calls signUpUseCase and sets signUpSuccess`() =
        runTest {
            coEvery { signUpUseCase("John Doe", "john@example.com", "password123") } returns fakeAuthResult

            viewModel.onNameChange("John Doe")
            viewModel.onEmailChange("john@example.com")
            viewModel.onPasswordChange("password123")
            viewModel.onConfirmPasswordChange("password123")

            viewModel.onSignUpClick("Empty name", "Invalid email", "Short password", "Password mismatch")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.signUpSuccess)
            assertFalse(state.isLoading)
            assertNull(state.errorMessage)
            coVerify(exactly = 1) { signUpUseCase("John Doe", "john@example.com", "password123") }
        }

    @Test
    fun `onSignUpClick trims name before calling signUpUseCase`() =
        runTest {
            coEvery { signUpUseCase("John Doe", "john@example.com", "password123") } returns fakeAuthResult

            viewModel.onNameChange("  John Doe  ")
            viewModel.onEmailChange("john@example.com")
            viewModel.onPasswordChange("password123")
            viewModel.onConfirmPasswordChange("password123")

            viewModel.onSignUpClick("Empty name", "Invalid email", "Short password", "Password mismatch")
            advanceUntilIdle()

            coVerify(exactly = 1) { signUpUseCase("John Doe", "john@example.com", "password123") }
        }

    @Test
    fun `onSignUpClick success clears previous errorMessage`() =
        runTest {
            coEvery { signUpUseCase(any(), any(), any()) } throws AuthException.ServerError

            viewModel.onNameChange("John")
            viewModel.onEmailChange("john@example.com")
            viewModel.onPasswordChange("password123")
            viewModel.onConfirmPasswordChange("password123")
            viewModel.onSignUpClick("Empty name", "Invalid email", "Short password", "Password mismatch")
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.errorMessage != null)

            coEvery { signUpUseCase(any(), any(), any()) } returns fakeAuthResult
            viewModel.onSignUpClick("Empty name", "Invalid email", "Short password", "Password mismatch")
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.errorMessage)
            assertTrue(viewModel.uiState.value.signUpSuccess)
        }

    // ── onSignUpClick: cadastro com falha ─────────────────────────────────

    @Test
    fun `onSignUpClick with EmailAlreadyInUse sets errorMessage`() =
        runTest {
            coEvery { signUpUseCase(any(), any(), any()) } throws AuthException.EmailAlreadyInUse

            viewModel.onNameChange("John")
            viewModel.onEmailChange("john@example.com")
            viewModel.onPasswordChange("password123")
            viewModel.onConfirmPasswordChange("password123")
            viewModel.onSignUpClick("Empty name", "Invalid email", "Short password", "Password mismatch")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("Email already in use", state.errorMessage)
            assertFalse(state.isLoading)
            assertFalse(state.signUpSuccess)
        }

    @Test
    fun `onSignUpClick with NoNetwork sets errorMessage`() =
        runTest {
            coEvery { signUpUseCase(any(), any(), any()) } throws AuthException.NoNetwork

            viewModel.onNameChange("John")
            viewModel.onEmailChange("john@example.com")
            viewModel.onPasswordChange("password123")
            viewModel.onConfirmPasswordChange("password123")
            viewModel.onSignUpClick("Empty name", "Invalid email", "Short password", "Password mismatch")
            advanceUntilIdle()

            assertEquals("No network connection", viewModel.uiState.value.errorMessage)
        }

    @Test
    fun `onSignUpClick with ServerError sets errorMessage`() =
        runTest {
            coEvery { signUpUseCase(any(), any(), any()) } throws AuthException.ServerError

            viewModel.onNameChange("John")
            viewModel.onEmailChange("john@example.com")
            viewModel.onPasswordChange("password123")
            viewModel.onConfirmPasswordChange("password123")
            viewModel.onSignUpClick("Empty name", "Invalid email", "Short password", "Password mismatch")
            advanceUntilIdle()

            assertEquals("Server error", viewModel.uiState.value.errorMessage)
        }

    @Test
    fun `onSignUpClick failure does not set signUpSuccess`() =
        runTest {
            coEvery { signUpUseCase(any(), any(), any()) } throws AuthException.ServerError

            viewModel.onNameChange("John")
            viewModel.onEmailChange("john@example.com")
            viewModel.onPasswordChange("password123")
            viewModel.onConfirmPasswordChange("password123")
            viewModel.onSignUpClick("Empty name", "Invalid email", "Short password", "Password mismatch")
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.signUpSuccess)
        }

    // ── onErrorDismissed ─────────────────────────────────────────────────

    @Test
    fun `onErrorDismissed clears errorMessage`() =
        runTest {
            coEvery { signUpUseCase(any(), any(), any()) } throws AuthException.ServerError

            viewModel.onNameChange("John")
            viewModel.onEmailChange("john@example.com")
            viewModel.onPasswordChange("password123")
            viewModel.onConfirmPasswordChange("password123")
            viewModel.onSignUpClick("Empty name", "Invalid email", "Short password", "Password mismatch")
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.errorMessage != null)

            viewModel.onErrorDismissed()

            assertNull(viewModel.uiState.value.errorMessage)
        }
}
