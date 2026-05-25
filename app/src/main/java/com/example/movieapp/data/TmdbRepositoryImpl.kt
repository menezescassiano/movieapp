package com.example.movieapp.data

import com.example.movieapp.data.remote.TmdbApiService
import com.example.movieapp.data.remote.dto.toDomain
import com.example.movieapp.model.TmdbMovieResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TmdbRepositoryImpl
    @Inject
    constructor(
        private val apiService: TmdbApiService,
    ) : TmdbRepository {
        override suspend fun searchTmdbMovies(query: String): List<TmdbMovieResult> = apiService.searchTmdbMovies(query).map { it.toDomain() }

        override suspend fun addMovieFromTmdb(tmdbId: Long) {
            apiService.addMovieFromTmdb(tmdbId)
        }
    }
