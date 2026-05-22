package com.example.movieapp.domain

import com.example.movieapp.data.AuthRepository
import com.example.movieapp.model.SavedCredentials
import javax.inject.Inject

class GetSavedCredentialsUseCase
    @Inject
    constructor(
        private val repository: AuthRepository,
    ) {
        suspend operator fun invoke(): SavedCredentials? = repository.getSavedCredentials()
    }
