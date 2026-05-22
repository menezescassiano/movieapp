package com.example.movieapp.domain

import com.example.movieapp.data.UserRepository
import com.example.movieapp.model.User
import javax.inject.Inject

class GetUserUseCase
    @Inject
    constructor(
        private val repository: UserRepository,
    ) {
        suspend operator fun invoke(): User = repository.getUser()
    }
