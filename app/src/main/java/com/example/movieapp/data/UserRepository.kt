package com.example.movieapp.data

import com.example.movieapp.model.User

interface UserRepository {
    suspend fun getUser(): User
}
