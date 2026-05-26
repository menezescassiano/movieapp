package com.example.movieapp.screens.details

import com.example.movieapp.domain.DeleteMovieUseCase
import com.example.movieapp.domain.GetFavoriteMovieUseCase
import com.example.movieapp.domain.GetMovieByIdUseCase
import com.example.movieapp.domain.UnfavoriteMovieUseCase
import com.example.movieapp.model.Movie
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
class DetailsViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var getMovieByIdUseCase: GetMovieByIdUseCase
    private lateinit var favoriteMovieUseCase: GetFavoriteMovieUseCase
    private lateinit var unfavoriteMovieUseCase: UnfavoriteMovieUseCase

    private lateinit var deleteMovieUseCase: DeleteMovieUseCase
    private lateinit var viewModel: DetailsViewModel

    private val fakeMovie =
        Movie(
            id = "1",
            title = "Movie 1",
            year = "2024",
            genre = "Action",
            director = "Director 1",
            actors = "Actor 1",
            description = "Description 1",
            poster = "poster1.jpg",
            images = listOf("img1.jpg"),
            rating = "8.0",
            favorite = false,
        )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getMovieByIdUseCase = mockk()
        favoriteMovieUseCase = mockk()
        unfavoriteMovieUseCase = mockk()
        deleteMovieUseCase = mockk()
        viewModel = DetailsViewModel(getMovieByIdUseCase, favoriteMovieUseCase, unfavoriteMovieUseCase, deleteMovieUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── initial state ────────────────────────────────────────────────────

    @Test
    fun `initial state is idle with no movie`() {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.movie)
        assertNull(state.errorMessage)
    }

    // ── loadMovie ────────────────────────────────────────────────────────

    @Test
    fun `loadMovie fetches movie successfully`() =
        runTest {
            coEvery { getMovieByIdUseCase("1") } returns fakeMovie

            viewModel.loadMovie("1")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(fakeMovie, state.movie)
            assertFalse(state.isLoading)
            assertNull(state.errorMessage)
        }

    @Test
    fun `loadMovie sets null movie when use case returns null`() =
        runTest {
            coEvery { getMovieByIdUseCase("99") } returns null

            viewModel.loadMovie("99")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNull(state.movie)
            assertFalse(state.isLoading)
            assertNull(state.errorMessage)
        }

    @Test
    fun `loadMovie sets error message on failure`() =
        runTest {
            coEvery { getMovieByIdUseCase("1") } throws RuntimeException("Not found")

            viewModel.loadMovie("1")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("Not found", state.errorMessage)
            assertFalse(state.isLoading)
            assertNull(state.movie)
        }

    @Test
    fun `loadMovie error does not clear previously loaded movie`() =
        runTest {
            coEvery { getMovieByIdUseCase("1") } returns fakeMovie
            coEvery { getMovieByIdUseCase("2") } throws RuntimeException("Network error")

            viewModel.loadMovie("1")
            advanceUntilIdle()
            assertEquals(fakeMovie, viewModel.uiState.value.movie)

            viewModel.loadMovie("2")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("Network error", state.errorMessage)
            assertEquals(fakeMovie, state.movie)
        }

    @Test
    fun `loadMovie success does not clear previous errorMessage`() =
        runTest {
            coEvery { getMovieByIdUseCase("1") } throws RuntimeException("Oops")

            viewModel.loadMovie("1")
            advanceUntilIdle()
            assertEquals("Oops", viewModel.uiState.value.errorMessage)

            coEvery { getMovieByIdUseCase("1") } returns fakeMovie

            viewModel.loadMovie("1")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(fakeMovie, state.movie)
            assertNull("errorMessage should be cleared after a successful load", state.errorMessage)
        }

    @Test
    fun `loadMovie with different ids updates movie each time`() =
        runTest {
            val movie2 = fakeMovie.copy(id = "2", title = "Movie 2")
            coEvery { getMovieByIdUseCase("1") } returns fakeMovie
            coEvery { getMovieByIdUseCase("2") } returns movie2

            viewModel.loadMovie("1")
            advanceUntilIdle()
            assertEquals(fakeMovie, viewModel.uiState.value.movie)

            viewModel.loadMovie("2")
            advanceUntilIdle()
            assertEquals(movie2, viewModel.uiState.value.movie)
        }

    // ── toggleFavorite: favoritar ────────────────────────────────────────

    @Test
    fun `toggleFavorite on unfavorited movie calls favoriteMovieUseCase`() =
        runTest {
            coEvery { getMovieByIdUseCase("1") } returns fakeMovie.copy(favorite = false)
            coEvery { favoriteMovieUseCase("1") } returns Unit

            viewModel.loadMovie("1")
            advanceUntilIdle()

            viewModel.toggleFavorite("1")
            advanceUntilIdle()

            assertTrue(
                viewModel.uiState.value.movie
                    ?.favorite == true,
            )
            coVerify(exactly = 1) { favoriteMovieUseCase("1") }
            coVerify(exactly = 0) { unfavoriteMovieUseCase(any()) }
        }

    @Test
    fun `toggleFavorite on unfavorited movie reverts on failure`() =
        runTest {
            coEvery { getMovieByIdUseCase("1") } returns fakeMovie.copy(favorite = false)
            coEvery { favoriteMovieUseCase("1") } throws RuntimeException("Server error")

            viewModel.loadMovie("1")
            advanceUntilIdle()

            viewModel.toggleFavorite("1")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.movie?.favorite == true)
            assertEquals("Server error", state.errorMessage)
        }

    // ── toggleFavorite: desfavoritar ─────────────────────────────────────

    @Test
    fun `toggleFavorite on favorited movie calls unfavoriteMovieUseCase`() =
        runTest {
            coEvery { getMovieByIdUseCase("1") } returns fakeMovie.copy(favorite = true)
            coEvery { unfavoriteMovieUseCase("1") } returns Unit

            viewModel.loadMovie("1")
            advanceUntilIdle()

            viewModel.toggleFavorite("1")
            advanceUntilIdle()

            assertFalse(
                viewModel.uiState.value.movie
                    ?.favorite == true,
            )
            coVerify(exactly = 1) { unfavoriteMovieUseCase("1") }
            coVerify(exactly = 0) { favoriteMovieUseCase(any()) }
        }

    @Test
    fun `toggleFavorite on favorited movie reverts on failure`() =
        runTest {
            coEvery { getMovieByIdUseCase("1") } returns fakeMovie.copy(favorite = true)
            coEvery { unfavoriteMovieUseCase("1") } throws RuntimeException("Server error")

            viewModel.loadMovie("1")
            advanceUntilIdle()

            viewModel.toggleFavorite("1")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.movie?.favorite == true)
            assertEquals("Server error", state.errorMessage)
        }

    @Test
    fun `toggleFavorite success does not clear previous errorMessage`() =
        runTest {
            coEvery { getMovieByIdUseCase("1") } returns fakeMovie.copy(favorite = false)
            coEvery { favoriteMovieUseCase("1") } throws RuntimeException("Server error")

            viewModel.loadMovie("1")
            advanceUntilIdle()

            viewModel.toggleFavorite("1")
            advanceUntilIdle()
            assertEquals("Server error", viewModel.uiState.value.errorMessage)

            coEvery { favoriteMovieUseCase("1") } returns Unit

            viewModel.toggleFavorite("1")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.movie?.favorite == true)
            assertNull("errorMessage should be cleared after a successful toggle", state.errorMessage)
        }

    // ── toggleFavorite: duplo toggle ──────────────────────────────────────

    @Test
    fun `toggleFavorite twice returns movie to original favorite state`() =
        runTest {
            coEvery { getMovieByIdUseCase("1") } returns fakeMovie.copy(favorite = false)
            coEvery { favoriteMovieUseCase("1") } returns Unit
            coEvery { unfavoriteMovieUseCase("1") } returns Unit

            viewModel.loadMovie("1")
            advanceUntilIdle()
            assertFalse(
                viewModel.uiState.value.movie
                    ?.favorite == true,
            )

            viewModel.toggleFavorite("1")
            advanceUntilIdle()
            assertTrue(
                viewModel.uiState.value.movie
                    ?.favorite == true,
            )

            viewModel.toggleFavorite("1")
            advanceUntilIdle()
            assertFalse(
                viewModel.uiState.value.movie
                    ?.favorite == true,
            )

            coVerify(exactly = 1) { favoriteMovieUseCase("1") }
            coVerify(exactly = 1) { unfavoriteMovieUseCase("1") }
        }

    // ── toggleFavorite: sem filme carregado ──────────────────────────────

    @Test
    fun `toggleFavorite without loaded movie keeps null movie and calls favoriteUseCase`() =
        runTest {
            coEvery { favoriteMovieUseCase("1") } returns Unit

            viewModel.toggleFavorite("1")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNull(state.movie)
            coVerify(exactly = 1) { favoriteMovieUseCase("1") }
        }
}
