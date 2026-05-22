package com.example.movieapp.domain

import com.example.movieapp.data.MovieRepository
import com.example.movieapp.model.Movie
import javax.inject.Inject

class GetMovieByIdUseCase
    @Inject
    constructor(
        private val repository: MovieRepository,
    ) {
        suspend operator fun invoke(movieId: String): Movie? = repository.getMovieById(movieId)
    }
