package com.example.movieapp.screens.qrcode

import com.example.movieapp.domain.GetMoviesUseCase
import com.example.movieapp.model.Movie
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QrCodeReaderViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getMoviesUseCase: GetMoviesUseCase
    private lateinit var viewModel: QrCodeReaderViewModel

    private val fakeMovies =
        listOf(
            Movie(
                id = "tt1234",
                title = "Inception",
                year = "2010",
                genre = "Sci-Fi",
                director = "Nolan",
                actors = "DiCaprio",
                description = "Dreams",
                poster = "poster.jpg",
                images = emptyList(),
                rating = "8.8",
            ),
            Movie(
                id = "tt5678",
                title = "Interstellar",
                year = "2014",
                genre = "Sci-Fi",
                director = "Nolan",
                actors = "McConaughey",
                description = "Space",
                poster = "poster2.jpg",
                images = emptyList(),
                rating = "8.6",
            ),
        )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getMoviesUseCase = mockk()
        viewModel = QrCodeReaderViewModel(getMoviesUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── estado inicial ───────────────────────────────────────────────────

    @Test
    fun `initial state is Idle with showCheck and showError false`() {
        assertEquals(QrCodeReaderUiState.Idle, viewModel.scanResult.value)
        assertFalse(viewModel.showCheck.value)
        assertFalse(viewModel.showError.value)
    }

    // ── onQrDecoded: sucesso ─────────────────────────────────────────────

    @Test
    fun `onQrDecoded sets Success and showCheck when movie id is found`() =
        runTest {
            coEvery { getMoviesUseCase() } returns fakeMovies

            viewModel.onQrDecoded("tt1234")
            advanceUntilIdle()

            assertEquals(QrCodeReaderUiState.Success("tt1234"), viewModel.scanResult.value)
            assertTrue(viewModel.showCheck.value)
            assertFalse(viewModel.showError.value)
        }

    @Test
    fun `onQrDecoded finds movie by id regardless of position in list`() =
        runTest {
            coEvery { getMoviesUseCase() } returns fakeMovies

            viewModel.onQrDecoded("tt5678")
            advanceUntilIdle()

            assertEquals(QrCodeReaderUiState.Success("tt5678"), viewModel.scanResult.value)
            assertTrue(viewModel.showCheck.value)
            assertFalse(viewModel.showError.value)
        }

    // ── onQrDecoded: not found ─────────────────────────────────────────────

    @Test
    fun `onQrDecoded sets NotFound and showError when movie id is not found`() =
        runTest {
            coEvery { getMoviesUseCase() } returns fakeMovies

            viewModel.onQrDecoded("tt9999")
            advanceUntilIdle()

            assertEquals(QrCodeReaderUiState.NotFound("tt9999"), viewModel.scanResult.value)
            assertFalse(viewModel.showCheck.value)
            assertTrue(viewModel.showError.value)
        }

    @Test
    fun `onQrDecoded with empty movie list always sets NotFound`() =
        runTest {
            coEvery { getMoviesUseCase() } returns emptyList()

            viewModel.onQrDecoded("tt1234")
            advanceUntilIdle()

            assertEquals(QrCodeReaderUiState.NotFound("tt1234"), viewModel.scanResult.value)
            assertTrue(viewModel.showError.value)
            assertFalse(viewModel.showCheck.value)
        }

    // ── onQrDecoded: debounce / evita reprocessamento ────────────────────

    @Test
    fun `onQrDecoded while Loading is ignored and useCase is called only once`() =
        runTest {
            coEvery { getMoviesUseCase() } returns fakeMovies

            // first call sets Loading and starts the coroutine
            viewModel.onQrDecoded("tt1234")
            // second call must be ignored because scanResult is already Loading
            viewModel.onQrDecoded("tt1234")
            advanceUntilIdle()

            coVerify(exactly = 1) { getMoviesUseCase() }
        }

    // ── reset ────────────────────────────────────────────────────────────

    @Test
    fun `reset returns state to Idle and clears showCheck and showError`() =
        runTest {
            coEvery { getMoviesUseCase() } returns fakeMovies

            viewModel.onQrDecoded("tt1234")
            advanceUntilIdle()
            assertEquals(QrCodeReaderUiState.Success("tt1234"), viewModel.scanResult.value)
            assertTrue(viewModel.showCheck.value)

            viewModel.reset()

            assertEquals(QrCodeReaderUiState.Idle, viewModel.scanResult.value)
            assertFalse(viewModel.showCheck.value)
            assertFalse(viewModel.showError.value)
        }

    @Test
    fun `reset after NotFound clears showError`() =
        runTest {
            coEvery { getMoviesUseCase() } returns fakeMovies

            viewModel.onQrDecoded("tt9999")
            advanceUntilIdle()
            assertTrue(viewModel.showError.value)

            viewModel.reset()

            assertEquals(QrCodeReaderUiState.Idle, viewModel.scanResult.value)
            assertFalse(viewModel.showError.value)
            assertFalse(viewModel.showCheck.value)
        }

    @Test
    fun `reset on Idle state keeps everything false`() {
        viewModel.reset()

        assertEquals(QrCodeReaderUiState.Idle, viewModel.scanResult.value)
        assertFalse(viewModel.showCheck.value)
        assertFalse(viewModel.showError.value)
    }

    // ── onCameraPermissionChanged ────────────────────────────────────────

    @Test
    fun `onCameraPermissionChanged false resets state`() =
        runTest {
            coEvery { getMoviesUseCase() } returns fakeMovies

            viewModel.onQrDecoded("tt1234")
            advanceUntilIdle()
            assertEquals(QrCodeReaderUiState.Success("tt1234"), viewModel.scanResult.value)

            viewModel.onCameraPermissionChanged(granted = false)

            assertEquals(QrCodeReaderUiState.Idle, viewModel.scanResult.value)
            assertFalse(viewModel.showCheck.value)
            assertFalse(viewModel.showError.value)
        }

    @Test
    fun `onCameraPermissionChanged true does not reset state`() =
        runTest {
            coEvery { getMoviesUseCase() } returns fakeMovies

            viewModel.onQrDecoded("tt1234")
            advanceUntilIdle()

            viewModel.onCameraPermissionChanged(granted = true)

            assertEquals(QrCodeReaderUiState.Success("tt1234"), viewModel.scanResult.value)
            assertTrue(viewModel.showCheck.value)
        }

    // ── fluxo completo ───────────────────────────────────────────────────

    @Test
    fun `scan success followed by reset allows a new scan`() =
        runTest {
            coEvery { getMoviesUseCase() } returns fakeMovies

            viewModel.onQrDecoded("tt1234")
            advanceUntilIdle()
            assertEquals(QrCodeReaderUiState.Success("tt1234"), viewModel.scanResult.value)

            viewModel.reset()
            assertEquals(QrCodeReaderUiState.Idle, viewModel.scanResult.value)

            viewModel.onQrDecoded("tt5678")
            advanceUntilIdle()
            assertEquals(QrCodeReaderUiState.Success("tt5678"), viewModel.scanResult.value)
            coVerify(exactly = 2) { getMoviesUseCase() }
        }

    @Test
    fun `scan notFound followed by reset allows a new scan`() =
        runTest {
            coEvery { getMoviesUseCase() } returns fakeMovies

            viewModel.onQrDecoded("tt9999")
            advanceUntilIdle()
            assertEquals(QrCodeReaderUiState.NotFound("tt9999"), viewModel.scanResult.value)

            viewModel.reset()

            viewModel.onQrDecoded("tt1234")
            advanceUntilIdle()
            assertEquals(QrCodeReaderUiState.Success("tt1234"), viewModel.scanResult.value)
            assertTrue(viewModel.showCheck.value)
            assertFalse(viewModel.showError.value)
        }
}
