package com.example.movieapp.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieapp.domain.CheckSavedTokenUseCase
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
class SplashViewModel @Inject constructor(
    private val checkSavedTokenUseCase: CheckSavedTokenUseCase
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.Loading)
    val destination: StateFlow<SplashDestination> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            _destination.value = if (checkSavedTokenUseCase()) {
                SplashDestination.Home
            } else {
                SplashDestination.Login
            }
        }
    }
}
