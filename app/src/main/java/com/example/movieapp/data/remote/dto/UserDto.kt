package com.example.movieapp.data.remote.dto

import com.example.movieapp.model.User
import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("city") val city: String?,
    @SerializedName("profilePictureUrl") val profilePictureUrl: String?,
)

fun UserDto.toDomain() = User(
    id = id,
    name = name.orEmpty(),
    email = email.orEmpty(),
    city = city.orEmpty(),
    profilePictureUrl = profilePictureUrl.orEmpty(),
)

data class UpdateUserRequest(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("city") val city: String,
    @SerializedName("profilePictureUrl") val profilePictureUrl: String,
)
