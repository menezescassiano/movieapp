package com.example.movieapp.model

data class AuthResult(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int,
    val user: User
)
