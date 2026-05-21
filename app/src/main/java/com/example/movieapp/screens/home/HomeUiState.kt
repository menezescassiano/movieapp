package com.example.movieapp.screens.home

import com.example.movieapp.model.Movie

data class HomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val movies: List<Movie> = emptyList(),
    val searchQuery: String = "",
    val errorMessage: String? = null,
    val currentPage: Int = 0,
    val totalPages: Int = 1,
    val totalElements: Int = 0,
    val pageSize: Int = 10
)
