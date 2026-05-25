package com.example.movieapp.domain

import com.example.movieapp.data.TmdbRepository
import com.example.movieapp.model.TmdbMovieResult
import javax.inject.Inject

class SearchTmdbMoviesUseCase
    @Inject
    constructor(
        private val repository: TmdbRepository,
    ) {
        suspend operator fun invoke(query: String): List<TmdbMovieResult> = repository.searchTmdbMovies(query)
    }
