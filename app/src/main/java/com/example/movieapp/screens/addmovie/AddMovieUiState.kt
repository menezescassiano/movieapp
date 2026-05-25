package com.example.movieapp.screens.addmovie

import com.example.movieapp.model.TmdbMovieResult

data class AddMovieUiState(
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val results: List<TmdbMovieResult> = emptyList(),
    val searchError: String? = null,
    val addingTmdbId: Long? = null,
    val addedTmdbIds: Set<Long> = emptySet(),
    val addError: String? = null,
)
