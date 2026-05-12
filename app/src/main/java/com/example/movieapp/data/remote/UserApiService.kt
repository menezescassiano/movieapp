package com.example.movieapp.data.remote

import com.example.movieapp.data.remote.dto.UpdateUserRequest
import com.example.movieapp.data.remote.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface UserApiService {

    @GET("users/me")
    suspend fun getUser(): UserDto

    @PUT("users/me")
    suspend fun updateUser(@Body request: UpdateUserRequest): UserDto
}
