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
class HomeViewModel
    @Inject
    constructor(
        private val getMoviesUseCase: GetMoviesUseCase,
        private val searchMoviesUseCase: SearchMoviesUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(HomeUiState())
        val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

        init {
            observeSearchQuery()
        }

        fun loadMovies() {
            viewModelScope.launch { fetchMovies(page = 0) }
        }

        fun loadNextPage() {
            val state = _uiState.value
            if (state.isLoading || state.currentPage + 1 >= state.totalPages) return
            viewModelScope.launch { fetchMovies(page = state.currentPage + 1, append = true) }
        }

        fun onSearchQueryChange(query: String) {
            _uiState.value = _uiState.value.copy(searchQuery = query)
        }

        fun refresh() {
            viewModelScope.launch { fetchMovies(page = 0, isRefresh = true) }
        }

        private fun observeSearchQuery() {
            _uiState
                .debounce(400)
                .distinctUntilChanged { old, new -> old.searchQuery == new.searchQuery }
                .onEach { state -> fetchMovies(query = state.searchQuery, page = 0) }
                .launchIn(viewModelScope)
        }

        private suspend fun fetchMovies(
            query: String = _uiState.value.searchQuery,
            page: Int = 0,
            isRefresh: Boolean = false,
            append: Boolean = false,
        ) {
            _uiState.value =
                when {
                    isRefresh -> _uiState.value.copy(isRefreshing = true, errorMessage = null)
                    append -> _uiState.value.copy(isLoading = true, errorMessage = null)
                    else -> _uiState.value.copy(isLoading = true, errorMessage = null, movies = emptyList())
                }

            try {
                val response =
                    if (query.isBlank()) {
                        getMoviesUseCase(page = page, size = _uiState.value.pageSize)
                    } else {
                        searchMoviesUseCase(query = query, page = page, size = _uiState.value.pageSize)
                    }

                val updatedMovies =
                    if (append) {
                        _uiState.value.movies + response.content
                    } else {
                        response.content
                    }

                _uiState.value =
                    _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        movies = updatedMovies,
                        currentPage = response.page,
                        totalPages = response.totalPages,
                        totalElements = response.totalElements,
                        errorMessage = null,
                    )
            } catch (e: Exception) {
                _uiState.value =
                    _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = e.message,
                    )
            }
        }
    }
