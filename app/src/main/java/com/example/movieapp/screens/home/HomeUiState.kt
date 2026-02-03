package com.example.movieapp.screens.home

import com.example.movieapp.model.Movie

data class HomeUiState(
    val isLoading: Boolean = true,
    val movies: List<Movie> = emptyList(),
    val errorMessage: String? = null
)
