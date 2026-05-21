package com.example.movieapp.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieapp.domain.GetFavoriteMoviesUseCase
import com.example.movieapp.model.Movie
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val isLoading: Boolean = false,
    val movies: List<Movie> = emptyList(),
    val errorMessage: String? = null,
    val currentPage: Int = 0,
    val totalPages: Int = 1,
    val totalElements: Int = 0
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val getFavoriteMoviesUseCase: GetFavoriteMoviesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState(isLoading = true))
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    fun loadFavorites() {
        viewModelScope.launch { fetchFavorites(page = 0) }
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoading || state.currentPage + 1 >= state.totalPages) return
        viewModelScope.launch { fetchFavorites(page = state.currentPage + 1, append = true) }
    }

    private suspend fun fetchFavorites(page: Int, append: Boolean = false) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        try {
            val response = getFavoriteMoviesUseCase(page = page)
            val updatedMovies = if (append) _uiState.value.movies + response.content else response.content
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                movies = updatedMovies,
                currentPage = response.page,
                totalPages = response.totalPages,
                totalElements = response.totalElements
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
        }
    }
}
