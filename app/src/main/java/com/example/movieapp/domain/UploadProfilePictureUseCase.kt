package com.example.movieapp.domain

import android.net.Uri
import com.example.movieapp.data.UserRepository
import com.example.movieapp.model.User
import javax.inject.Inject

class UploadProfilePictureUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(uri: Uri): User = repository.uploadProfilePicture(uri)
}
