package com.example.movieapp.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.movieapp.feature.onboarding.OnboardingScreen
import com.example.movieapp.screens.details.DetailsScreen
import com.example.movieapp.screens.favorites.FavoritesScreen
import com.example.movieapp.screens.home.HomeScreen
import com.example.movieapp.screens.login.LoginScreen
import com.example.movieapp.screens.onboarding.OnboardingViewModel
import com.example.movieapp.screens.profile.ProfileScreen
import com.example.movieapp.screens.qrcode.QrCodeReaderScreen
import com.example.movieapp.screens.settings.SettingsScreen
import com.example.movieapp.screens.signup.SignUpScreen
import com.example.movieapp.screens.splash.SplashScreen

@Composable
fun MovieNavigation(
    navController: NavHostController,
    modifier: Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = SplashRoute,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable<SplashRoute> {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(HomeRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(LoginRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                },
                onNavigateToOnboarding = {
                    navController.navigate(OnboardingRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                },
            )
        }

        composable<OnboardingRoute> {
            val viewModel: OnboardingViewModel = hiltViewModel()
            OnboardingScreen(
                onFinish = {
                    viewModel.markOnboardingCompleted()
                    navController.navigate(LoginRoute) {
                        popUpTo(OnboardingRoute) { inclusive = true }
                    }
                },
            )
        }

        composable<LoginRoute> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(HomeRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                },
                onSignUpClick = {
                    navController.navigate(SignUpRoute)
                },
            )
        }

        composable<SignUpRoute> {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(HomeRoute) {
                        popUpTo(SignUpRoute) { inclusive = true }
                    }
                },
                onLoginClick = {
                    navController.navigateUp()
                },
            )
        }

        composable<HomeRoute> {
            HomeScreen(navController = navController, modifier = modifier.fillMaxSize())
        }

        composable<DetailsRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<DetailsRoute>()
            DetailsScreen(navController = navController, movieId = args.movieId, modifier = modifier)
        }

        composable<QrCodeRoute> {
            QrCodeReaderScreen(
                navController = navController,
                onQrDetected = { qrData ->
                    navController.navigate(DetailsRoute(qrData)) {
                        popUpTo(QrCodeRoute) { inclusive = true }
                    }
                },
            )
        }

        composable<FavoritesRoute> {
            FavoritesScreen(navController = navController)
        }

        composable<ProfileRoute> {
            ProfileScreen()
        }

        composable<SettingsRoute> {
            SettingsScreen(
                modifier = modifier.fillMaxSize(),
                onBack = { navController.navigateUp() },
            )
        }
    }
}
