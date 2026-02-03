package com.example.movieapp.navigation

import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
data class DetailsRoute(val movieId: String)

@Serializable
object QrCodeRoute

@Serializable
object ProfileRoute

@Serializable
object FavoritesRoute