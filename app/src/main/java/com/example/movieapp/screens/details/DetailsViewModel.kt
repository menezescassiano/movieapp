package com.example.movieapp.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieapp.domain.GetFavoriteMovieUseCase
import com.example.movieapp.domain.GetMovieByIdUseCase
import com.example.movieapp.domain.UnfavoriteMovieUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel
    @Inject
    constructor(
        private val getMovieByIdUseCase: GetMovieByIdUseCase,
        private val favoriteMovieUseCase: GetFavoriteMovieUseCase,
        private val unfavoriteMovieUseCase: UnfavoriteMovieUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(DetailsUiState(isLoading = false))
        val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

        fun loadMovie(movieId: String) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true)
                try {
                    val movie = getMovieByIdUseCase(movieId)
                    _uiState.value = _uiState.value.copy(isLoading = false, movie = movie, errorMessage = null)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
                }
            }
        }

        fun toggleFavorite(movieId: String) {
            val isFavorite = _uiState.value.movie?.favorite == true
            _uiState.value =
                _uiState.value.copy(
                    movie = _uiState.value.movie?.copy(favorite = !isFavorite),
                )
            viewModelScope.launch {
                try {
                    if (isFavorite) {
                        unfavoriteMovieUseCase(movieId)
                    } else {
                        favoriteMovieUseCase(movieId)
                    }
                    _uiState.value = _uiState.value.copy(errorMessage = null)
                } catch (e: Exception) {
                    _uiState.value =
                        _uiState.value.copy(
                            movie = _uiState.value.movie?.copy(favorite = isFavorite),
                            errorMessage = e.message,
                        )
                }
            }
        }
    }
