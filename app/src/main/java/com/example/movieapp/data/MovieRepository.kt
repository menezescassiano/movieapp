package com.example.movieapp.data

import com.example.movieapp.model.Movie

interface MovieRepository {
    suspend fun getMovies(): List<Movie>

    suspend fun getMovieById(movieId: String): Movie?
}