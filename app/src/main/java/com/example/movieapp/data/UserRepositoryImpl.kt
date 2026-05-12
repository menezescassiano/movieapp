package com.example.movieapp.data

import com.example.movieapp.data.remote.UserApiService
import com.example.movieapp.data.remote.dto.UpdateUserRequest
import com.example.movieapp.data.remote.dto.toDomain
import com.example.movieapp.model.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val apiService: UserApiService
) : UserRepository {

    override suspend fun getUser(): User = apiService.getUser().toDomain()

    override suspend fun updateUser(
        name: String,
        email: String,
        city: String,
        profilePictureUrl: String,
    ): User = apiService.updateUser(
        UpdateUserRequest(
            name = name,
            email = email,
            city = city,
            profilePictureUrl = profilePictureUrl,
        )
    ).toDomain()
}
