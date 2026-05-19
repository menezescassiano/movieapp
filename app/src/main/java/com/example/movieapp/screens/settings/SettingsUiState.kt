package com.example.movieapp.screens.settings

data class SettingsUiState(
    // Notifications
    val notificationsEnabled: Boolean = true,
    val newReleasesEnabled: Boolean = true,
    val recommendationsEnabled: Boolean = false,

    // Biometrics
    val biometricEnabled: Boolean = false,

    // Content
    val contentLanguage: ContentLanguage = ContentLanguage.ENGLISH,
    val hideWatchedMovies: Boolean = false,

    // About — static, no state needed
)

enum class ContentLanguage(val label: String) {
    ENGLISH("English"),
    PORTUGUESE("Português")
}
