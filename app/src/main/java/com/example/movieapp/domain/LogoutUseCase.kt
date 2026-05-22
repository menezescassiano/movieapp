package com.example.movieapp.domain

import com.example.movieapp.data.AuthRepository
import com.example.movieapp.data.SessionManager
import javax.inject.Inject

class LogoutUseCase
    @Inject
    constructor(
        private val repository: AuthRepository,
        private val sessionManager: SessionManager,
    ) {
        suspend operator fun invoke() {
            repository.logout()
            sessionManager.logout()
        }
    }
