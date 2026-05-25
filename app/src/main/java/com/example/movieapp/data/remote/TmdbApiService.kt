package com.example.movieapp.data.remote

import com.example.movieapp.data.remote.dto.TmdbMovieDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface TmdbApiService {
    @GET("movies/tmdb/search")
    suspend fun searchTmdbMovies(
        @Query("q") query: String,
    ): List<TmdbMovieDto>

    @POST("movies/import")
    suspend fun addMovieFromTmdb(
        @Query("tmdbId") tmdbId: Long,
    )
}
