package com.example.movieapp.data.remote

import com.example.movieapp.data.remote.dto.UpdateUserRequest
import com.example.movieapp.data.remote.dto.UserDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part

interface UserApiService {

    @GET("users/me")
    suspend fun getUser(): UserDto

    @PUT("users/me")
    suspend fun updateUser(@Body request: UpdateUserRequest): UserDto

    @Multipart
    @POST("users/me/picture")
    suspend fun uploadProfilePicture(@Part file: MultipartBody.Part): UserDto
}
