package com.example.movieapp.data

import com.example.movieapp.model.User

interface UserRepository {
    suspend fun getUser(): User
    suspend fun updateUser(
        name: String,
        email: String,
        city: String,
        profilePictureUrl: String,
    ): User
}
