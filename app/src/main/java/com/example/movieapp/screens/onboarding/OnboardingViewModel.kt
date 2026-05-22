package com.example.movieapp.screens.onboarding

import androidx.lifecycle.ViewModel
import com.example.movieapp.domain.MarkOnboardingCompletedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel
    @Inject
    constructor(
        private val markOnboardingCompleted: MarkOnboardingCompletedUseCase,
    ) : ViewModel() {
        fun markOnboardingCompleted() {
            markOnboardingCompleted.invoke()
        }
    }
