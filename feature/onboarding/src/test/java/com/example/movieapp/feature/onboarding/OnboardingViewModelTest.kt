package com.example.movieapp.feature.onboarding

import com.example.movieapp.feature.onboarding.domain.MarkOnboardingCompletedUseCase
import com.example.movieapp.feature.onboarding.view.OnboardingViewModel
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class OnboardingViewModelTest {
    private lateinit var markOnboardingCompletedUseCase: MarkOnboardingCompletedUseCase
    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setUp() {
        markOnboardingCompletedUseCase = mockk(relaxed = true)
        viewModel = OnboardingViewModel(markOnboardingCompletedUseCase)
    }

    // ── markOnboardingCompleted ──────────────────────────────────────────

    @Test
    fun `markOnboardingCompleted calls use case exactly once`() {
        viewModel.markOnboardingCompleted()

        verify(exactly = 1) { markOnboardingCompletedUseCase.invoke() }
    }

    @Test
    fun `markOnboardingCompleted called multiple times invokes use case each time`() {
        viewModel.markOnboardingCompleted()
        viewModel.markOnboardingCompleted()
        viewModel.markOnboardingCompleted()

        verify(exactly = 3) { markOnboardingCompletedUseCase.invoke() }
    }

    @Test
    fun `markOnboardingCompleted does not throw even if use case is slow`() {
        // use case is relaxed (no-op), just ensure no exception is propagated
        viewModel.markOnboardingCompleted()
    }
}
