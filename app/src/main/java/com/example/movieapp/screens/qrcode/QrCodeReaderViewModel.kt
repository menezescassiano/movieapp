package com.example.movieapp.screens.qrcode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieapp.domain.GetMoviesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QrCodeReaderViewModel
    @Inject
    constructor(
        val useCase: GetMoviesUseCase,
    ) : ViewModel() {
        private val _showCheck = MutableStateFlow(false)
        val showCheck: StateFlow<Boolean> = _showCheck.asStateFlow()

        private val _showError = MutableStateFlow(false)
        val showError: StateFlow<Boolean> = _showError

        private val _scanResult = MutableStateFlow<QrCodeReaderUiState>(QrCodeReaderUiState.Idle)
        val scanResult: StateFlow<QrCodeReaderUiState> = _scanResult

        fun onCameraPermissionChanged(granted: Boolean) {
            if (!granted) {
                reset()
            }
        }

        fun onQrDecoded(qrContent: String) {
            // Prevents the same QR from being processed multiple times in a row
            if (_scanResult.value is QrCodeReaderUiState.Loading) return

            // Set synchronously so that consecutive calls are rejected by the guard
            // above before the coroutine even starts
            _scanResult.value = QrCodeReaderUiState.Loading

            viewModelScope.launch {
                val found = useCase().content.any { it.id == qrContent }

                if (found) {
                    _scanResult.value = QrCodeReaderUiState.Success(qrContent)
                    _showCheck.value = true
                    _showError.value = false
                } else {
                    _scanResult.value = QrCodeReaderUiState.NotFound(qrContent)
                    _showCheck.value = false
                    _showError.value = true
                }
            }
        }

        fun reset() {
            _scanResult.value = QrCodeReaderUiState.Idle
            _showCheck.value = false
            _showError.value = false
        }
    }
