package com.example.movieapp.data.remote

import com.example.movieapp.data.remote.dto.LoginRequest
import com.example.movieapp.data.remote.dto.LoginResponse
import com.example.movieapp.data.remote.dto.RefreshRequest
import com.example.movieapp.data.remote.dto.RefreshResponse
import com.example.movieapp.data.remote.dto.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest,
    ): LoginResponse

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest,
    ): LoginResponse

    @POST("auth/refresh")
    suspend fun refresh(
        @Body request: RefreshRequest,
    ): RefreshResponse
}
