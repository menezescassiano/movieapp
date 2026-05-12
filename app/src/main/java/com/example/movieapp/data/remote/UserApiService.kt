package com.example.movieapp.data.remote

import com.example.movieapp.data.remote.dto.UserDto
import retrofit2.http.GET

interface UserApiService {

    @GET("users/me")
    suspend fun getUser(): UserDto
}
