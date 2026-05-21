package com.example.movieapp.data.remote

import com.example.movieapp.model.Movie
import com.example.movieapp.model.PagedResponse
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MovieApiService {

    @GET("movies")
    suspend fun getMovies(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): PagedResponse<Movie>

    @GET("movies/search")
    suspend fun searchMovies(
        @Query("q") query: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): PagedResponse<Movie>

    @GET("movies/{id}")
    suspend fun getMovieById(@Path("id") movieId: String): Movie

    @POST("movies/{id}/favorite")
    suspend fun favoriteMovie(@Path("id") movieId: String)

    @DELETE("movies/{id}/favorite")
    suspend fun unfavoriteMovie(@Path("id") movieId: String)

    @GET("movies/favorite")
    suspend fun getFavoriteMovies(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): PagedResponse<Movie>
}
