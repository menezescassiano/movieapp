package com.example.movieapp.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieapp.domain.GetMovieByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val getMovieByIdUseCase: GetMovieByIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailsUiState(isLoading = false))
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    fun loadMovie(movieId: String) {
        viewModelScope.launch {
            _uiState.value = DetailsUiState(isLoading = true)
            try {
                val movie = getMovieByIdUseCase(movieId)
                _uiState.value = DetailsUiState(movie = movie)
            } catch (e: Exception) {
                _uiState.value = DetailsUiState(errorMessage = e.message)
            }
        }
    }

}