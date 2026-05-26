package com.example.movieapp.domain

import com.example.movieapp.data.TokenStore
import com.example.movieapp.data.local.TokenLocalDataSource
import com.example.movieapp.feature.onboarding.data.OnboardingRepository
import javax.inject.Inject

/**
 * Saves a pre-authenticated token coming from the automation test runner.
 *
 * The token is passed via the launch Intent as:
 *   --es "auth_token"    "<access_token>"
 *   --es "refresh_token" "<refresh_token>"   (optional, falls back to access token)
 *
 * After this use case runs:
 * - [com.example.movieapp.data.AuthRepository.hasSavedToken] returns true
 * - Onboarding is marked as completed
 * → SplashViewModel routes directly to Home, skipping onboarding and login.
 */
class InjectAutomationTokenUseCase
    @Inject
    constructor(
        private val tokenStore: TokenStore,
        private val tokenLocalDataSource: TokenLocalDataSource,
        private val onboardingRepository: OnboardingRepository,
    ) {
        suspend operator fun invoke(
            accessToken: String,
            refreshToken: String,
        ) {
            // Mark onboarding done so SplashViewModel doesn't route to OnboardingScreen.
            onboardingRepository.markCompleted()
            // Persist token to Room so hasSavedToken() == true.
            tokenLocalDataSource.save(accessToken, refreshToken)
            // Populate the in-memory store so AuthInterceptor can send the token immediately.
            tokenStore.save(accessToken)
        }
    }
