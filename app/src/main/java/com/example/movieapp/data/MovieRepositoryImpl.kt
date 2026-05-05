package com.example.movieapp.data

import com.example.movieapp.data.remote.MovieApiService
import com.example.movieapp.model.Movie
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieRepositoryImpl @Inject constructor(
    private val apiService: MovieApiService
) : MovieRepository {

    override suspend fun getMovies(): List<Movie> =
        apiService.getMovies()

    override suspend fun getMovieById(movieId: String): Movie? =
        apiService.getMovieById(movieId)

    override suspend fun getFavoriteMovie(movieId: String) {
        apiService.favoriteMovie(movieId)
    }

    override suspend fun unfavoriteMovie(movieId: String) {
        apiService.unfavoriteMovie(movieId)
    }

    override suspend fun getFavoriteMovies(): List<Movie> =
        apiService.getFavoriteMovies()
    
}
