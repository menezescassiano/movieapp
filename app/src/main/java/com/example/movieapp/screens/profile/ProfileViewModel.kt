package com.example.movieapp.screens.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieapp.domain.GetUserUseCase
import com.example.movieapp.domain.LogoutUseCase
import com.example.movieapp.domain.UpdateUserUseCase
import com.example.movieapp.domain.UploadProfilePictureUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val uploadProfilePictureUseCase: UploadProfilePictureUseCase,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val user = getUserUseCase()
                _uiState.value = _uiState.value.copy(isLoading = false, user = user)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load profile"
                )
            }
        }
    }

    // Avatar
    fun onAvatarClick() {
        _uiState.value = _uiState.value.copy(showAvatarPicker = true)
    }

    fun onAvatarPickerDismiss() {
        _uiState.value = _uiState.value.copy(showAvatarPicker = false)
    }

    fun onPhotoTaken(uri: Uri) {
        _uiState.value = _uiState.value.copy(showAvatarPicker = false)
        uploadAvatar(uri)
    }

    fun onImagePicked(uri: Uri) {
        _uiState.value = _uiState.value.copy(showAvatarPicker = false)
        uploadAvatar(uri)
    }

    private fun uploadAvatar(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            try {
                val updatedUser = uploadProfilePictureUseCase(uri)
                // Keep the local URI for immediate display; the remote URL is now in updatedUser
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    avatarUri = uri,
                    user = updatedUser
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Failed to upload picture"
                )
            }
        }
    }

    // Edit field
    fun onEditField(field: EditableField) {
        _uiState.value = _uiState.value.copy(editingField = field)
    }

    fun onEditFieldDismiss() {
        _uiState.value = _uiState.value.copy(editingField = null)
    }

    fun onEditFieldConfirm(field: EditableField, newValue: String) {
        val current = _uiState.value.user ?: return
        val updated = when (field) {
            EditableField.NAME  -> current.copy(name = newValue)
            EditableField.EMAIL -> current.copy(email = newValue)
            EditableField.CITY  -> current.copy(city = newValue)
        }
        _uiState.value = _uiState.value.copy(user = updated, editingField = null)
        saveProfile(updated.name, updated.email, updated.city, updated.profilePictureUrl)
    }

    fun logout() {
        viewModelScope.launch { logoutUseCase() }
    }

    private fun saveProfile(
        name: String,
        email: String,
        city: String,
        profilePictureUrl: String,
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            try {
                val updated = updateUserUseCase(
                    name = name,
                    email = email,
                    city = city,
                    profilePictureUrl = profilePictureUrl,
                )
                _uiState.value = _uiState.value.copy(isSaving = false, user = updated)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Failed to save profile"
                )
            }
        }
    }
}
