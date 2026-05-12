package com.example.movieapp.screens.profile

import android.net.Uri
import com.example.movieapp.model.User

enum class EditableField { NAME, EMAIL, CITY }

data class ProfileUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null,
    val avatarUri: Uri? = null,
    val showAvatarPicker: Boolean = false,
    val editingField: EditableField? = null,
)
