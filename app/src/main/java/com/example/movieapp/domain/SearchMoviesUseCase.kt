package com.example.movieapp.domain

import com.example.movieapp.data.MovieRepository
import com.example.movieapp.model.Movie
import com.example.movieapp.model.PagedResponse
import javax.inject.Inject

class SearchMoviesUseCase
    @Inject
    constructor(
        private val repository: MovieRepository,
    ) {
        suspend operator fun invoke(
            query: String,
            page: Int = 0,
            size: Int = 10,
        ): PagedResponse<Movie> = repository.searchMovies(query, page, size)
    }
