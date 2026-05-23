package com.example.movieapp.screens.root

import com.example.movieapp.data.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RootViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: RootViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        sessionManager = SessionManager()
        viewModel = RootViewModel(sessionManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── logoutEvent ──────────────────────────────────────────────────────

    @Test
    fun `logoutEvent is exposed from sessionManager`() {
        assertNotNull(viewModel.logoutEvent)
    }

    @Test
    fun `logoutEvent emits when sessionManager logout is called`() =
        runTest {
            var received = false

            val job =
                launch {
                    viewModel.logoutEvent.first()
                    received = true
                }

            sessionManager.logout()
            advanceUntilIdle()

            assert(received) { "logoutEvent should have emitted after sessionManager.logout()" }
            job.cancel()
        }

    @Test
    fun `logoutEvent emits once per logout call`() =
        runTest {
            var count = 0

            val job =
                launch {
                    viewModel.logoutEvent.collect { count++ }
                }

            sessionManager.logout()
            advanceUntilIdle()

            assert(count == 1) { "Expected 1 emission after one logout call, got $count" }
            job.cancel()
        }

    @Test
    fun `logoutEvent emits multiple times on consecutive logout calls`() =
        runTest {
            var count = 0

            val job =
                launch {
                    viewModel.logoutEvent.collect { count++ }
                }

            sessionManager.logout()
            advanceUntilIdle()
            sessionManager.logout()
            advanceUntilIdle()

            assert(count == 2) { "Expected 2 emissions after two logout calls, got $count" }
            job.cancel()
        }

    @Test
    fun `logoutEvent does not emit before logout is called`() =
        runTest {
            var received = false

            val job =
                launch {
                    viewModel.logoutEvent.collect { received = true }
                }

            advanceUntilIdle()

            assert(!received) { "logoutEvent should not emit before logout is called" }
            job.cancel()
        }
}
