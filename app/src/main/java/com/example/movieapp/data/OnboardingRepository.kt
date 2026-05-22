package com.example.movieapp.data

interface OnboardingRepository {
    fun isCompleted(): Boolean

    fun markCompleted()
}
