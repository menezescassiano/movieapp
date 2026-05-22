package com.example.movieapp.domain

import com.example.movieapp.data.MovieRepository
import javax.inject.Inject

class GetFavoriteMovieUseCase
    @Inject
    constructor(
        private val repository: MovieRepository,
    ) {
        suspend operator fun invoke(movieId: String) {
            repository.favoriteMovie(movieId)
        }
    }
