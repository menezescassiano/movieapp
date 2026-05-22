package com.example.movieapp.domain

import com.example.movieapp.data.OnboardingRepository
import javax.inject.Inject

class IsOnboardingCompletedUseCase
    @Inject
    constructor(
        private val repository: OnboardingRepository,
    ) {
        operator fun invoke(): Boolean = repository.isCompleted()
    }
