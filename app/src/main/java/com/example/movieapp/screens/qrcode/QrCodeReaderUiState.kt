package com.example.movieapp.screens.qrcode

sealed class QrCodeReaderUiState {
    data object Idle : QrCodeReaderUiState()
    data object Loading : QrCodeReaderUiState()
    data class Success(val content: String) : QrCodeReaderUiState()
    data class NotFound(val content: String) : QrCodeReaderUiState()
    data class Error(val message: String) : QrCodeReaderUiState()
}