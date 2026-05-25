package com.example.movieapp.domain

import com.example.movieapp.data.TmdbRepository
import javax.inject.Inject

class AddMovieFromTmdbUseCase
    @Inject
    constructor(
        private val repository: TmdbRepository,
    ) {
        suspend operator fun invoke(tmdbId: Long) {
            repository.addMovieFromTmdb(tmdbId)
        }
    }
