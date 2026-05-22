package com.example.movieapp.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieapp.domain.CheckSavedTokenUseCase
import com.example.movieapp.domain.RestoreTokenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SplashDestination {
    data object Loading : SplashDestination

    data object Home : SplashDestination

    data object Login : SplashDestination
}

@HiltViewModel
class SplashViewModel
    @Inject
    constructor(
        private val checkSavedTokenUseCase: CheckSavedTokenUseCase,
        private val restoreTokenUseCase: RestoreTokenUseCase,
    ) : ViewModel() {
        private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.Loading)
        val destination: StateFlow<SplashDestination> = _destination.asStateFlow()

        init {
            viewModelScope.launch {
                if (checkSavedTokenUseCase()) {
                    // Populate the in-memory TokenStore before any authenticated
                    // request is fired by the screens that follow the splash.
                    // Without this, AuthInterceptor sends requests with no
                    // Authorization header, leaving the 401 → refresh flow to
                    // work only by coincidence.
                    restoreTokenUseCase()
                    _destination.value = SplashDestination.Home
                } else {
                    _destination.value = SplashDestination.Login
                }
            }
        }
    }
