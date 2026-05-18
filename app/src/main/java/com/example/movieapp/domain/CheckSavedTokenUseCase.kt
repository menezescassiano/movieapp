package com.example.movieapp.domain

import com.example.movieapp.data.AuthRepository
import javax.inject.Inject

class CheckSavedTokenUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): Boolean = repository.hasSavedToken()
}
