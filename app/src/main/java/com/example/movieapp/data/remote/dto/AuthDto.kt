package com.example.movieapp.data.remote.dto

import com.example.movieapp.model.AuthResult
import com.google.gson.annotations.SerializedName

data class RefreshRequest(
    @SerializedName("refreshToken") val refreshToken: String
)

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("tokens") val tokens: TokensDto,
    @SerializedName("user") val user: UserDto
)

data class TokensDto(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("expiresIn") val expiresIn: Int
)

fun LoginResponse.toDomain() = AuthResult(
    accessToken = tokens.accessToken,
    refreshToken = tokens.refreshToken,
    expiresIn = tokens.expiresIn,
    user = user.toDomain()
)
