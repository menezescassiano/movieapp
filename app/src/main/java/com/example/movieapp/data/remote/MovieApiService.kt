package com.example.movieapp.data.remote

import com.example.movieapp.model.Movie
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MovieApiService {

    @GET("movies")
    suspend fun getMovies(): List<Movie>

    @GET("movies/{id}")
    suspend fun getMovieById(@Path("id") movieId: String): Movie

    @POST("movies/{id}/favorite")
    suspend fun favoriteMovie(@Path("id") movieId: String)

    @DELETE("movies/{id}/favorite")
    suspend fun unfavoriteMovie(@Path("id") movieId: String)

    @GET("movies/favorite")
    suspend fun getFavoriteMovies(): List<Movie>
}
