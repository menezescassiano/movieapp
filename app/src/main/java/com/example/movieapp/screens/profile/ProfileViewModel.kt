package com.example.movieapp.screens.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieapp.domain.GetUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

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

    fun onAvatarClick() {
        _uiState.value = _uiState.value.copy(showAvatarPicker = true)
    }

    fun onAvatarPickerDismiss() {
        _uiState.value = _uiState.value.copy(showAvatarPicker = false)
    }

    fun onPhotoTaken(uri: Uri) {
        _uiState.value = _uiState.value.copy(avatarUri = uri, showAvatarPicker = false)
    }

    fun onImagePicked(uri: Uri) {
        _uiState.value = _uiState.value.copy(avatarUri = uri, showAvatarPicker = false)
    }
}
