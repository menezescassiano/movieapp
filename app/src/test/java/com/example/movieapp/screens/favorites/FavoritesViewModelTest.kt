package com.example.movieapp.screens.favorites

import com.example.movieapp.domain.GetFavoriteMoviesUseCase
import com.example.movieapp.model.Movie
import io.mockk.coEvery
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
class FavoritesViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var getFavoriteMoviesUseCase: GetFavoriteMoviesUseCase
    private lateinit var viewModel: FavoritesViewModel

    private val fakeMovies = listOf(
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
            favorite = true
        ),
        Movie(
            id = "2",
            title = "Movie 2",
            year = "2023",
            genre = "Drama",
            director = "Director 2",
            actors = "Actor 2",
            description = "Description 2",
            poster = "poster2.jpg",
            images = listOf("img2.jpg"),
            rating = "7.5",
            favorite = true
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getFavoriteMoviesUseCase = mockk()
        viewModel = FavoritesViewModel(getFavoriteMoviesUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // -- initial state ----------------------------------------------------

    @Test
    fun `initial state has isLoading true`() {
        val state = viewModel.uiState.value
        assertTrue(state.isLoading)
        assertEquals(emptyList<Movie>(), state.movies)
        assertNull(state.errorMessage)
    }

    // -- loadFavorites: sucesso -------------------------------------------

    @Test
    fun `loadFavorites fetches movies successfully`() = runTest {
        coEvery { getFavoriteMoviesUseCase() } returns fakeMovies

        viewModel.loadFavorites()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(fakeMovies, state.movies)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun `loadFavorites returns empty list when no favorites`() = runTest {
        coEvery { getFavoriteMoviesUseCase() } returns emptyList()

        viewModel.loadFavorites()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(emptyList<Movie>(), state.movies)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun `loadFavorites success replaces previously loaded movies`() = runTest {
        coEvery { getFavoriteMoviesUseCase() } returns fakeMovies

        viewModel.loadFavorites()
        advanceUntilIdle()
        assertEquals(fakeMovies, viewModel.uiState.value.movies)

        val updatedMovies = listOf(fakeMovies[0].copy(title = "Updated Movie"))
        coEvery { getFavoriteMoviesUseCase() } returns updatedMovies

        viewModel.loadFavorites()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(updatedMovies, state.movies)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    // -- loadFavorites: erro ----------------------------------------------

    @Test
    fun `loadFavorites sets error message on failure`() = runTest {
        coEvery { getFavoriteMoviesUseCase() } throws RuntimeException("Network error")

        viewModel.loadFavorites()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Network error", state.errorMessage)
        assertFalse(state.isLoading)
        assertEquals(emptyList<Movie>(), state.movies)
    }

    @Test
    fun `loadFavorites clears errorMessage on success after previous failure`() = runTest {
        coEvery { getFavoriteMoviesUseCase() } throws RuntimeException("Oops")

        viewModel.loadFavorites()
        advanceUntilIdle()
        assertEquals("Oops", viewModel.uiState.value.errorMessage)

        coEvery { getFavoriteMoviesUseCase() } returns fakeMovies

        viewModel.loadFavorites()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(fakeMovies, state.movies)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    // -- loadFavorites: erro nao limpa lista anterior ---------------------

    @Test
    fun `loadFavorites error does not clear previously loaded movies`() = runTest {
        coEvery { getFavoriteMoviesUseCase() } returns fakeMovies

        viewModel.loadFavorites()
        advanceUntilIdle()
        assertEquals(fakeMovies, viewModel.uiState.value.movies)

        coEvery { getFavoriteMoviesUseCase() } throws RuntimeException("Server error")

        viewModel.loadFavorites()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Server error", state.errorMessage)
        assertEquals(fakeMovies, state.movies)
    }
}
