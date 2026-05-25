package com.example.movieapp.screens.addmovie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieapp.domain.AddMovieFromTmdbUseCase
import com.example.movieapp.domain.SearchTmdbMoviesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class AddMovieViewModel
    @Inject
    constructor(
        private val searchTmdbMoviesUseCase: SearchTmdbMoviesUseCase,
        private val addMovieFromTmdbUseCase: AddMovieFromTmdbUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(AddMovieUiState())
        val uiState: StateFlow<AddMovieUiState> = _uiState.asStateFlow()

        init {
            observeSearchQuery()
        }

        fun onSearchQueryChange(query: String) {
            _uiState.value =
                _uiState.value.copy(
                    searchQuery = query,
                    searchError = null,
                    results = if (query.isBlank()) emptyList() else _uiState.value.results,
                )
        }

        fun addMovie(tmdbId: Long) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(addingTmdbId = tmdbId, addError = null)
                try {
                    addMovieFromTmdbUseCase(tmdbId)
                    _uiState.value =
                        _uiState.value.copy(
                            addingTmdbId = null,
                            addedTmdbIds = _uiState.value.addedTmdbIds + tmdbId,
                        )
                } catch (e: Exception) {
                    _uiState.value =
                        _uiState.value.copy(
                            addingTmdbId = null,
                            addError = e.message,
                        )
                }
            }
        }

        fun dismissAddError() {
            _uiState.value = _uiState.value.copy(addError = null)
        }

        private fun observeSearchQuery() {
            _uiState
                .map { it.searchQuery }
                .debounce(400)
                .distinctUntilChanged()
                .onEach { query -> if (query.isNotBlank()) search(query) else clearResults() }
                .launchIn(viewModelScope)
        }

        private suspend fun search(query: String) {
            _uiState.value = _uiState.value.copy(isSearching = true, searchError = null)
            try {
                val results = searchTmdbMoviesUseCase(query)
                _uiState.value = _uiState.value.copy(isSearching = false, results = results)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSearching = false, searchError = e.message)
            }
        }

        private fun clearResults() {
            _uiState.value = _uiState.value.copy(results = emptyList(), searchError = null)
        }
    }
