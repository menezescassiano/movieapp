package com.example.movieapp.domain

import com.example.movieapp.data.MovieRepository
import javax.inject.Inject

class GetFavoriteMoviesUseCase @Inject constructor(private val repository: MovieRepository){

    suspend operator fun invoke() = repository.getFavoriteMovies()
}
