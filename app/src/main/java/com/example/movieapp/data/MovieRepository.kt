package com.example.movieapp.data

import com.example.movieapp.model.Movie

interface MovieRepository {
    suspend fun getMovies(): List<Movie>

    suspend fun searchMovies(query: String): List<Movie>

    suspend fun getMovieById(movieId: String): Movie?

    suspend fun getFavoriteMovie(movieId: String)

    suspend fun unfavoriteMovie(movieId: String)

    suspend fun getFavoriteMovies(): List<Movie>
}
