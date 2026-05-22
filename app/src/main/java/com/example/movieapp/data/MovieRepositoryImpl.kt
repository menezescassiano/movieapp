package com.example.movieapp.data

import com.example.movieapp.data.remote.MovieApiService
import com.example.movieapp.model.Movie
import com.example.movieapp.model.PagedResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieRepositoryImpl
    @Inject
    constructor(
        private val apiService: MovieApiService,
    ) : MovieRepository {
        override suspend fun getMovies(
            page: Int,
            size: Int,
        ): PagedResponse<Movie> = apiService.getMovies(page, size)

        override suspend fun searchMovies(
            query: String,
            page: Int,
            size: Int,
        ): PagedResponse<Movie> = apiService.searchMovies(query, page, size)

        override suspend fun getMovieById(movieId: String): Movie? = apiService.getMovieById(movieId)

        override suspend fun favoriteMovie(movieId: String) {
            apiService.favoriteMovie(movieId)
        }

        override suspend fun unfavoriteMovie(movieId: String) {
            apiService.unfavoriteMovie(movieId)
        }

        override suspend fun getFavoriteMovies(
            page: Int,
            size: Int,
        ): PagedResponse<Movie> = apiService.getFavoriteMovies(page, size)
    }
