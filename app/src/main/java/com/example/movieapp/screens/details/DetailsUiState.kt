package com.example.movieapp.screens.details

import com.example.movieapp.model.Movie

data class DetailsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val movie: Movie? = null,
    val isDeleted: Boolean = false,
)
