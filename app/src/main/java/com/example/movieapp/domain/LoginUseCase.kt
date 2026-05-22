package com.example.movieapp.domain

import com.example.movieapp.data.AuthRepository
import com.example.movieapp.model.AuthResult
import javax.inject.Inject

class LoginUseCase
    @Inject
    constructor(
        private val repository: AuthRepository,
    ) {
        suspend operator fun invoke(
            email: String,
            password: String,
        ): AuthResult = repository.login(email, password)
    }
