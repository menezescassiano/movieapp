package com.example.movieapp.screens.profile

import android.net.Uri
import com.example.movieapp.model.User

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val errorMessage: String? = null,
    val avatarUri: Uri? = null,
    val showAvatarPicker: Boolean = false
)
