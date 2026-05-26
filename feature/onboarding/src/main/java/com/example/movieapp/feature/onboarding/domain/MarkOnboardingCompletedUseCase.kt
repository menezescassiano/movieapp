package com.example.movieapp.feature.onboarding.domain

import com.example.movieapp.feature.onboarding.data.OnboardingRepository
import javax.inject.Inject

class MarkOnboardingCompletedUseCase
    @Inject
    constructor(
        private val repository: OnboardingRepository,
    ) {
        operator fun invoke() = repository.markCompleted()
    }
