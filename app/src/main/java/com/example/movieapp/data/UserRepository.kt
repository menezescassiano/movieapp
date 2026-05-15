package com.example.movieapp.data

import android.net.Uri
import com.example.movieapp.model.User

interface UserRepository {
    suspend fun getUser(): User
    suspend fun updateUser(
        name: String,
        email: String,
        city: String,
        profilePictureUrl: String,
    ): User
    suspend fun uploadProfilePicture(uri: Uri): User
}
