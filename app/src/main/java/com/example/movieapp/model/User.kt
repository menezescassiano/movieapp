package com.example.movieapp.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val city: String,
    val profilePictureUrl: String = "",
)
