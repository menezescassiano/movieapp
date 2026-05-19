package com.example.movieapp.navigation

import kotlinx.serialization.Serializable

@Serializable
object SplashRoute

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

@Serializable
object LoginRoute

@Serializable
object SettingsRoute