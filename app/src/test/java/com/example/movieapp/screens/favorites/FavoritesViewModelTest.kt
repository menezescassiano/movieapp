package com.example.movieapp.screens.favorites

import com.example.movieapp.domain.GetFavoriteMoviesUseCase
import com.example.movieapp.model.Movie
import com.example.movieapp.model.PagedResponse
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
class FavoritesViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var getFavoriteMoviesUseCase: GetFavoriteMoviesUseCase
    private lateinit var viewModel: FavoritesViewModel

    private val fakeMovies =
        listOf(
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
                favorite = true,
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
                favorite = true,
            ),
        )

    private fun pagedOf(
        movies: List<Movie>,
        page: Int = 0,
        totalPages: Int = 1,
    ) = PagedResponse(
        content = movies,
        page = page,
        size = movies.size,
        totalElements = movies.size,
        totalPages = totalPages,
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

    // ── estado inicial ────────────────────────────────────────────────────

    @Test
    fun `initial state has isLoading true`() {
        val state = viewModel.uiState.value
        assertTrue(state.isLoading)
        assertEquals(emptyList<Movie>(), state.movies)
        assertNull(state.errorMessage)
    }

    // ── loadFavorites: sucesso ────────────────────────────────────────────

    @Test
    fun `loadFavorites fetches movies successfully`() =
        runTest {
            coEvery { getFavoriteMoviesUseCase(any(), any()) } returns pagedOf(fakeMovies)

            viewModel.loadFavorites()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(fakeMovies, state.movies)
            assertFalse(state.isLoading)
            assertNull(state.errorMessage)
        }

    @Test
    fun `loadFavorites returns empty list when no favorites`() =
        runTest {
            coEvery { getFavoriteMoviesUseCase(any(), any()) } returns pagedOf(emptyList())

            viewModel.loadFavorites()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(emptyList<Movie>(), state.movies)
            assertFalse(state.isLoading)
            assertNull(state.errorMessage)
        }

    @Test
    fun `loadFavorites success replaces previously loaded movies`() =
        runTest {
            coEvery { getFavoriteMoviesUseCase(any(), any()) } returns pagedOf(fakeMovies)

            viewModel.loadFavorites()
            advanceUntilIdle()
            assertEquals(fakeMovies, viewModel.uiState.value.movies)

            val updatedMovies = listOf(fakeMovies[0].copy(title = "Updated Movie"))
            coEvery { getFavoriteMoviesUseCase(any(), any()) } returns pagedOf(updatedMovies)

            viewModel.loadFavorites()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(updatedMovies, state.movies)
            assertFalse(state.isLoading)
            assertNull(state.errorMessage)
        }

    @Test
    fun `loadFavorites updates pagination metadata`() =
        runTest {
            val response =
                PagedResponse(
                    content = fakeMovies,
                    page = 2,
                    size = 10,
                    totalElements = 50,
                    totalPages = 5,
                )
            coEvery { getFavoriteMoviesUseCase(any(), any()) } returns response

            viewModel.loadFavorites()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(2, state.currentPage)
            assertEquals(5, state.totalPages)
            assertEquals(50, state.totalElements)
        }

    // ── loadFavorites: erro ───────────────────────────────────────────────

    @Test
    fun `loadFavorites sets error message on failure`() =
        runTest {
            coEvery { getFavoriteMoviesUseCase(any(), any()) } throws RuntimeException("Network error")

            viewModel.loadFavorites()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("Network error", state.errorMessage)
            assertFalse(state.isLoading)
            assertEquals(emptyList<Movie>(), state.movies)
        }

    @Test
    fun `loadFavorites clears errorMessage on success after previous failure`() =
        runTest {
            coEvery { getFavoriteMoviesUseCase(any(), any()) } throws RuntimeException("Oops")

            viewModel.loadFavorites()
            advanceUntilIdle()
            assertEquals("Oops", viewModel.uiState.value.errorMessage)

            coEvery { getFavoriteMoviesUseCase(any(), any()) } returns pagedOf(fakeMovies)

            viewModel.loadFavorites()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(fakeMovies, state.movies)
            assertFalse(state.isLoading)
            assertNull(state.errorMessage)
        }

    @Test
    fun `loadFavorites error does not clear previously loaded movies`() =
        runTest {
            coEvery { getFavoriteMoviesUseCase(any(), any()) } returns pagedOf(fakeMovies)

            viewModel.loadFavorites()
            advanceUntilIdle()
            assertEquals(fakeMovies, viewModel.uiState.value.movies)

            coEvery { getFavoriteMoviesUseCase(any(), any()) } throws RuntimeException("Server error")

            viewModel.loadFavorites()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("Server error", state.errorMessage)
            assertEquals(fakeMovies, state.movies)
        }

    // ── loadNextPage ──────────────────────────────────────────────────────

    @Test
    fun `loadNextPage appends movies when more pages exist`() =
        runTest {
            val page0 = pagedOf(listOf(fakeMovies[0]), page = 0, totalPages = 2)
            val page1 = pagedOf(listOf(fakeMovies[1]), page = 1, totalPages = 2)
            coEvery { getFavoriteMoviesUseCase(page = 0, size = any()) } returns page0
            coEvery { getFavoriteMoviesUseCase(page = 1, size = any()) } returns page1

            viewModel.loadFavorites()
            advanceUntilIdle()

            viewModel.loadNextPage()
            advanceUntilIdle()

            assertEquals(fakeMovies, viewModel.uiState.value.movies)
        }

    @Test
    fun `loadNextPage does nothing when already on last page`() =
        runTest {
            coEvery { getFavoriteMoviesUseCase(any(), any()) } returns pagedOf(fakeMovies, page = 0, totalPages = 1)

            viewModel.loadFavorites()
            advanceUntilIdle()

            viewModel.loadNextPage()
            advanceUntilIdle()

            coVerify(exactly = 1) { getFavoriteMoviesUseCase(any(), any()) }
        }
}
