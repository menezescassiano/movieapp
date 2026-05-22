package com.example.movieapp.screens.splash

import com.example.movieapp.domain.CheckSavedTokenUseCase
import com.example.movieapp.domain.RestoreTokenUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var checkSavedTokenUseCase: CheckSavedTokenUseCase
    private lateinit var restoreTokenUseCase: RestoreTokenUseCase
    private lateinit var viewModel: SplashViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        checkSavedTokenUseCase = mockk()
        restoreTokenUseCase = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── estado inicial ────────────────────────────────────────────────────

    @Test
    fun `initial destination is Loading`() {
        coEvery { checkSavedTokenUseCase() } returns true

        viewModel = SplashViewModel(checkSavedTokenUseCase, restoreTokenUseCase)

        assertEquals(SplashDestination.Loading, viewModel.destination.value)
    }

    // ── token salvo → Home ────────────────────────────────────────────────

    @Test
    fun `when saved token exists, destination becomes Home`() =
        runTest {
            coEvery { checkSavedTokenUseCase() } returns true

            viewModel = SplashViewModel(checkSavedTokenUseCase, restoreTokenUseCase)
            advanceUntilIdle()

            assertEquals(SplashDestination.Home, viewModel.destination.value)
        }

    @Test
    fun `when saved token exists, restoreToken is called to populate in-memory store`() =
        runTest {
            // BUG FIX: without restoreToken(), the TokenStore is empty and AuthInterceptor
            // sends all requests without an Authorization header, causing the refresh to
            // happen "by accident" (only works if the backend returns 401 for requests
            // without a header — behaviour that is not guaranteed).
            coEvery { checkSavedTokenUseCase() } returns true

            viewModel = SplashViewModel(checkSavedTokenUseCase, restoreTokenUseCase)
            advanceUntilIdle()

            coVerify(exactly = 1) { restoreTokenUseCase() }
        }

    @Test
    fun `when saved token exists, restoreToken is called before navigating to Home`() =
        runTest {
            // Order matters: the token must be in the store BEFORE any request
            // triggered by the screens that load after the splash.
            val callOrder = mutableListOf<String>()
            coEvery { checkSavedTokenUseCase() } returns true
            coEvery { restoreTokenUseCase() } answers { callOrder.add("restore") }

            viewModel = SplashViewModel(checkSavedTokenUseCase, restoreTokenUseCase)
            advanceUntilIdle()

            // restoreToken should have been called and the destination should be Home
            assertEquals(listOf("restore"), callOrder)
            assertEquals(SplashDestination.Home, viewModel.destination.value)
        }

    // ── sem token salvo → Login ───────────────────────────────────────────

    @Test
    fun `when no saved token exists, destination becomes Login`() =
        runTest {
            coEvery { checkSavedTokenUseCase() } returns false

            viewModel = SplashViewModel(checkSavedTokenUseCase, restoreTokenUseCase)
            advanceUntilIdle()

            assertEquals(SplashDestination.Login, viewModel.destination.value)
        }

    @Test
    fun `when no saved token exists, restoreToken is never called`() =
        runTest {
            coEvery { checkSavedTokenUseCase() } returns false

            viewModel = SplashViewModel(checkSavedTokenUseCase, restoreTokenUseCase)
            advanceUntilIdle()

            coVerify(exactly = 0) { restoreTokenUseCase() }
        }
}
