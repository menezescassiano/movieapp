package com.example.movieapp.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.movieapp.screens.details.DetailsScreen
import com.example.movieapp.screens.favorites.FavoritesScreen
import com.example.movieapp.screens.home.HomeScreen
import com.example.movieapp.screens.profile.ProfileScreen
import com.example.movieapp.screens.qrcode.QrCodeReaderScreen

@Composable
fun MovieNavigation(navController: NavHostController, modifier: Modifier) {

    NavHost(navController = navController, startDestination = HomeRoute) {

        composable<HomeRoute> {
            HomeScreen(navController = navController, modifier = modifier.fillMaxSize())
        }

        composable<DetailsRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<DetailsRoute>()
            DetailsScreen(navController = navController, movieId = args.movieId)
        }

        composable<QrCodeRoute> {
            QrCodeReaderScreen(
                navController = navController,
                onQrDetected = { qrData ->
                    navController.navigate(DetailsRoute(qrData)) {
                        popUpTo(QrCodeRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<FavoritesRoute> {
            FavoritesScreen(navController = navController)
        }

        composable<ProfileRoute> {
            ProfileScreen()
        }
    }
}