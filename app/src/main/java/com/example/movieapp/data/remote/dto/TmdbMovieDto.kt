package com.example.movieapp.data.remote.dto

import com.example.movieapp.model.TmdbMovieResult
import com.google.gson.annotations.SerializedName

data class TmdbMovieDto(
    @SerializedName("tmdbId") val tmdbId: Long,
    @SerializedName("title") val title: String,
    @SerializedName("year") val year: String,
    @SerializedName("overview") val overview: String,
    @SerializedName("poster") val poster: String,
    @SerializedName("alreadyAdded") val alreadyAdded: Boolean,
)

fun TmdbMovieDto.toDomain() =
    TmdbMovieResult(
        tmdbId = tmdbId,
        title = title,
        year = year,
        overview = overview,
        poster = poster,
        alreadyAdded = alreadyAdded
    )
