package com.example.movieapp.data

import com.example.movieapp.model.Movie
import com.example.movieapp.model.PagedResponse

interface MovieRepository {
    suspend fun getMovies(
        page: Int = 0,
        size: Int = 10,
    ): PagedResponse<Movie>

    suspend fun searchMovies(
        query: String,
        page: Int = 0,
        size: Int = 10,
    ): PagedResponse<Movie>

    suspend fun getMovieById(movieId: String): Movie?

    suspend fun favoriteMovie(movieId: String)

    suspend fun unfavoriteMovie(movieId: String)

    suspend fun getFavoriteMovies(
        page: Int = 0,
        size: Int = 10,
    ): PagedResponse<Movie>
}
