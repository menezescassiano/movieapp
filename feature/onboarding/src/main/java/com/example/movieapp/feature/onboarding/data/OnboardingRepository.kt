package com.example.movieapp.feature.onboarding.data

interface OnboardingRepository {
    fun isCompleted(): Boolean

    fun markCompleted()
}
