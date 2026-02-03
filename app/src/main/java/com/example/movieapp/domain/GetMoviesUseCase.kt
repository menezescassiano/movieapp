package com.example.movieapp.domain

import com.example.movieapp.data.MovieRepository
import com.example.movieapp.model.Movie
import javax.inject.Inject

class GetMoviesUseCase @Inject constructor(private val repository: MovieRepository) {

    suspend operator fun invoke(): List<Movie> = repository.getMovies()

}