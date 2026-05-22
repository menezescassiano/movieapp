package com.example.movieapp.screens.home

import com.example.movieapp.domain.GetMoviesUseCase
import com.example.movieapp.domain.SearchMoviesUseCase
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
class HomeViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var getMoviesUseCase: GetMoviesUseCase
    private lateinit var searchMoviesUseCase: SearchMoviesUseCase
    private lateinit var viewModel: HomeViewModel

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
            ),
        )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getMoviesUseCase = mockk()
        searchMoviesUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): HomeViewModel = HomeViewModel(getMoviesUseCase, searchMoviesUseCase)

    // ── init / loadMovies ────────────────────────────────────────────────

    @Test
    fun `init loads movies and sets correct state`() =
        runTest {
            coEvery { getMoviesUseCase() } returns fakeMovies

            viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(fakeMovies, state.movies)
            assertFalse(state.isLoading)
            assertFalse(state.isRefreshing)
            assertEquals("", state.searchQuery)
            assertNull(state.errorMessage)
        }

    @Test
    fun `loadMovies sets error message on failure`() =
        runTest {
            coEvery { getMoviesUseCase() } throws RuntimeException("Network error")

            viewModel = createViewModel()
            viewModel.loadMovies()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("Network error", state.errorMessage)
            assertFalse(state.isLoading)
            assertTrue(state.movies.isEmpty())
        }

    @Test
    fun `successful fetch clears previous error message`() =
        runTest {
            coEvery { getMoviesUseCase() } throws RuntimeException("Initial error")

            viewModel = createViewModel()
            advanceUntilIdle()

            assertEquals("Initial error", viewModel.uiState.value.errorMessage)

            coEvery { getMoviesUseCase() } returns fakeMovies

            viewModel.loadMovies()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNull(state.errorMessage)
            assertEquals(fakeMovies, state.movies)
        }

    @Test
    fun `exception without message sets null errorMessage`() =
        runTest {
            coEvery { getMoviesUseCase() } throws RuntimeException()

            viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNull(state.errorMessage)
            assertFalse(state.isLoading)
        }

    @Test
    fun `getMoviesUseCase returning empty list results in empty movies`() =
        runTest {
            coEvery { getMoviesUseCase() } returns emptyList()

            viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.movies.isEmpty())
            assertFalse(state.isLoading)
            assertNull(state.errorMessage)
        }

    // ── refresh ──────────────────────────────────────────────────────────

    @Test
    fun `refresh fetches movies without changing isLoading`() =
        runTest {
            coEvery { getMoviesUseCase() } returns fakeMovies

            viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.refresh()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(fakeMovies, state.movies)
            assertFalse(state.isLoading)
            assertFalse(state.isRefreshing)
            assertNull(state.errorMessage)
        }

    @Test
    fun `refresh failure preserves existing movies and sets error`() =
        runTest {
            coEvery { getMoviesUseCase() } returns fakeMovies

            viewModel = createViewModel()
            advanceUntilIdle()

            assertEquals(fakeMovies, viewModel.uiState.value.movies)

            coEvery { getMoviesUseCase() } throws RuntimeException("Refresh error")

            viewModel.refresh()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(fakeMovies, state.movies)
            assertEquals("Refresh error", state.errorMessage)
            assertFalse(state.isRefreshing)
        }

    // ── search ───────────────────────────────────────────────────────────

    @Test
    fun `onSearchQueryChange updates search query`() =
        runTest {
            coEvery { getMoviesUseCase() } returns fakeMovies
            coEvery { searchMoviesUseCase(any()) } returns emptyList()

            viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onSearchQueryChange("batman")

            assertEquals("batman", viewModel.uiState.value.searchQuery)
        }

    @Test
    fun `search query triggers searchMoviesUseCase`() =
        runTest {
            val searchResults = listOf(fakeMovies[0])
            coEvery { getMoviesUseCase() } returns fakeMovies
            coEvery { searchMoviesUseCase("batman") } returns searchResults

            viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onSearchQueryChange("batman")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(searchResults, state.movies)
            coVerify { searchMoviesUseCase("batman") }
        }

    @Test
    fun `blank search query uses getMoviesUseCase`() =
        runTest {
            coEvery { getMoviesUseCase() } returns fakeMovies
            coEvery { searchMoviesUseCase(any()) } returns emptyList()

            viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onSearchQueryChange("batman")
            advanceUntilIdle()

            viewModel.onSearchQueryChange("")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(fakeMovies, state.movies)
        }

    @Test
    fun `search query failure sets error message`() =
        runTest {
            coEvery { getMoviesUseCase() } returns fakeMovies
            coEvery { searchMoviesUseCase("fail") } throws RuntimeException("Search failed")

            viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onSearchQueryChange("fail")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("Search failed", state.errorMessage)
        }

    @Test
    fun `search returns empty list`() =
        runTest {
            coEvery { getMoviesUseCase() } returns fakeMovies
            coEvery { searchMoviesUseCase("xyz") } returns emptyList()

            viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onSearchQueryChange("xyz")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.movies.isEmpty())
            assertNull(state.errorMessage)
        }
}
