package com.example.movieapp.domain

import com.example.movieapp.data.AuthRepository
import com.example.movieapp.model.AuthResult
import javax.inject.Inject

class SignUpUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(name: String, email: String, password: String): AuthResult =
        repository.register(name, email, password)
}
