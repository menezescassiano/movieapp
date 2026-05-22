package com.example.movieapp.domain

import com.example.movieapp.data.OnboardingRepository
import javax.inject.Inject

class MarkOnboardingCompletedUseCase
    @Inject
    constructor(
        private val repository: OnboardingRepository,
    ) {
        operator fun invoke() = repository.markCompleted()
    }
