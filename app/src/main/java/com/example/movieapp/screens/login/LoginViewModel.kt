package com.example.movieapp.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieapp.data.AuthException
import com.example.movieapp.domain.GetSavedCredentialsUseCase
import com.example.movieapp.domain.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel
    @Inject
    constructor(
        private val loginUseCase: LoginUseCase,
        private val getSavedCredentialsUseCase: GetSavedCredentialsUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(LoginUiState())
        val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

        init {
            loadSavedCredentials()
        }

        private fun loadSavedCredentials() {
            viewModelScope.launch {
                val saved = getSavedCredentialsUseCase()
                if (saved != null) {
                    _uiState.update {
                        it.copy(email = saved.email, password = saved.password)
                    }
                }
            }
        }

        fun onEmailChange(value: String) {
            _uiState.update { it.copy(email = value, emailError = null) }
        }

        fun onPasswordChange(value: String) {
            _uiState.update { it.copy(password = value, passwordError = null) }
        }

        fun onTogglePasswordVisibility() {
            _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
        }

        fun onContinueClick(
            invalidEmailMessage: String,
            emptyPasswordMessage: String,
        ) {
            val state = _uiState.value

            val emailError =
                if (!android.util.Patterns.EMAIL_ADDRESS
                        .matcher(state.email)
                        .matches()
                ) {
                    invalidEmailMessage
                } else {
                    null
                }
            val passwordError = if (state.password.isBlank()) emptyPasswordMessage else null

            if (emailError != null || passwordError != null) {
                _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
                return
            }

            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                try {
                    loginUseCase(state.email, state.password)
                    _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
                } catch (e: AuthException) {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.message)
                    }
                }
            }
        }

        fun onForgotPasswordClick() {
            // TODO: navigate to forgot-password flow
        }

        fun onErrorDismissed() {
            _uiState.update { it.copy(errorMessage = null) }
        }
    }
