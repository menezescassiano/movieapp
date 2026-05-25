package com.example.movieapp.model

data class TmdbMovieResult(
    val tmdbId: Long,
    val title: String,
    val year: String,
    val overview: String,
    val poster: String,
    val alreadyAdded: Boolean
)
