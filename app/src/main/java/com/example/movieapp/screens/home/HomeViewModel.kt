package com.example.movieapp.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieapp.domain.GetMoviesUseCase
import com.example.movieapp.domain.SearchMoviesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getMoviesUseCase: GetMoviesUseCase,
    private val searchMoviesUseCase: SearchMoviesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeSearchQuery()
    }

    fun loadMovies() {
        viewModelScope.launch { fetchMovies() }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun refresh() {
        viewModelScope.launch { fetchMovies(isRefresh = true) }
    }

    private fun observeSearchQuery() {
        _uiState
            .debounce(400)
            .distinctUntilChanged { old, new -> old.searchQuery == new.searchQuery }
            .onEach { state -> fetchMovies(query = state.searchQuery) }
            .launchIn(viewModelScope)
    }

    private suspend fun fetchMovies(query: String = "", isRefresh: Boolean = false) {
        _uiState.value = if (isRefresh) {
            _uiState.value.copy(isRefreshing = true, errorMessage = null)
        } else {
            _uiState.value.copy(isLoading = true, errorMessage = null)
        }

        try {
            val movies = if (query.isBlank()) {
                getMoviesUseCase()
            } else {
                searchMoviesUseCase(query)
            }
            _uiState.value = if (isRefresh) {
                _uiState.value.copy(isRefreshing = false, movies = movies)
            } else {
                _uiState.value.copy(isLoading = false, movies = movies)
            }
        } catch (e: Exception) {
            _uiState.value = if (isRefresh) {
                _uiState.value.copy(isRefreshing = false, errorMessage = e.message)
            } else {
                _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }
}
