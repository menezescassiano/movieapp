package com.example.movieapp.feature.onboarding.view

import androidx.lifecycle.ViewModel
import com.example.movieapp.feature.onboarding.domain.MarkOnboardingCompletedUseCase
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
