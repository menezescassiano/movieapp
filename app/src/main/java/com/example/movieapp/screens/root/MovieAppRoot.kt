package com.example.movieapp.screens.root

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.movieapp.navigation.FavoritesRoute
import com.example.movieapp.navigation.HomeRoute
import com.example.movieapp.navigation.LoginRoute
import com.example.movieapp.navigation.MovieNavigation
import com.example.movieapp.navigation.OnboardingRoute
import com.example.movieapp.navigation.ProfileRoute
import com.example.movieapp.navigation.SettingsRoute
import com.example.movieapp.navigation.SignUpRoute
import com.example.movieapp.navigation.SplashRoute
import com.example.movieapp.ui.theme.AccentPurple
import com.example.movieapp.ui.theme.AppBackground
import com.example.movieapp.ui.theme.NavUnselected

@Composable
fun MovieAppRoot(viewModel: RootViewModel = hiltViewModel()) {
    val navController = rememberNavController()

    // Logout triggered by TokenAuthenticator when refresh fails
    LaunchedEffect(Unit) {
        viewModel.logoutEvent.collect {
            navController.navigate(LoginRoute) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    data class BottomNavItem(
        val route: Any,
        val label: String,
        val selectedIcon: ImageVector,
        val unselectedIcon: ImageVector,
    )

    val bottomItems =
        listOf(
            BottomNavItem(HomeRoute, "Home", Icons.Filled.Home, Icons.Outlined.Home),
            BottomNavItem(FavoritesRoute, "Favorites", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
            BottomNavItem(ProfileRoute, "Profile", Icons.Filled.Person, Icons.Outlined.Person),
            BottomNavItem(SettingsRoute, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
        )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar =
        currentDestination?.hierarchy?.none {
            it.hasRoute(LoginRoute::class) ||
                it.hasRoute(SplashRoute::class) ||
                it.hasRoute(SignUpRoute::class) ||
                it.hasRoute(OnboardingRoute::class)
        } == true

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        modifier = Modifier.height(100.dp),
                        containerColor = AppBackground,
                        tonalElevation = 0.dp,
                    ) {
                        bottomItems.forEach { item ->
                            val selected = currentDestination?.hierarchy?.any { it.hasRoute(item.route::class) } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (!selected) {
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label,
                                        modifier = Modifier.height(22.dp),
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.label,
                                        fontSize = 11.sp,
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    )
                                },
                                colors =
                                    NavigationBarItemDefaults.colors(
                                        selectedIconColor = AccentPurple,
                                        selectedTextColor = AccentPurple,
                                        unselectedIconColor = NavUnselected,
                                        unselectedTextColor = NavUnselected,
                                        indicatorColor = AccentPurple.copy(alpha = 0.15f),
                                    ),
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            MovieNavigation(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
            )
        }

        // Status bar overlay
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .windowInsetsTopHeight(WindowInsets.statusBars)
                    .background(AppBackground)
                    .align(Alignment.TopCenter),
        )
    }
}
