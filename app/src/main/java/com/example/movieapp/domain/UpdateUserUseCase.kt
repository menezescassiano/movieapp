package com.example.movieapp.domain

import com.example.movieapp.data.UserRepository
import com.example.movieapp.model.User
import javax.inject.Inject

class UpdateUserUseCase @Inject constructor(private val repository: UserRepository) {
    suspend operator fun invoke(
        name: String,
        email: String,
        city: String,
        profilePictureUrl: String,
    ): User = repository.updateUser(
        name = name,
        email = email,
        city = city,
        profilePictureUrl = profilePictureUrl,
    )
}
