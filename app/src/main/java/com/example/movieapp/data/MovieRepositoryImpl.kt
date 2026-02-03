package com.example.movieapp.data

import com.example.movieapp.model.Movie
import com.example.movieapp.model.getMoviesList
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieRepositoryImpl @Inject constructor() : MovieRepository {

    override suspend fun getMovies(): List<Movie> {
        delay(2000) // simulate network delay
        return getMoviesList()
    }

    override suspend fun getMovieById(movieId: String): Movie {
        delay(2000) // simulate network delay
        return getMoviesList().first { it.id == movieId }
    }

}