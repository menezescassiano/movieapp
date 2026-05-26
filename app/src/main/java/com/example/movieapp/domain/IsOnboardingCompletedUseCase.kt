package com.example.movieapp.domain

import com.example.movieapp.feature.onboarding.data.OnboardingRepository
import javax.inject.Inject

class IsOnboardingCompletedUseCase
    @Inject
    constructor(
        private val repository: OnboardingRepository
    ) {
        operator fun invoke(): Boolean = repository.isCompleted()
    }
