package com.example.movieapp.data.remote

import com.example.movieapp.model.Movie
import retrofit2.http.GET
import retrofit2.http.Path

interface MovieApiService {

    @GET("movies")
    suspend fun getMovies(): List<Movie>

    @GET("movies/{id}")
    suspend fun getMovieById(@Path("id") movieId: String): Movie
}
