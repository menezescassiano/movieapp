package com.example.movieapp.data

import com.example.movieapp.model.TmdbMovieResult

interface TmdbRepository {
    suspend fun searchTmdbMovies(query: String): List<TmdbMovieResult>

    suspend fun addMovieFromTmdb(tmdbId: Long)
}
