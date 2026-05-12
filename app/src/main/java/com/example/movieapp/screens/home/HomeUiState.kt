package com.example.movieapp.screens.home

import com.example.movieapp.model.Movie

data class HomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val movies: List<Movie> = emptyList(),
    val searchQuery: String = "",
    val errorMessage: String? = null
)
