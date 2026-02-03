package com.example.movieapp.screens.root

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.movieapp.navigation.FavoritesRoute
import com.example.movieapp.navigation.HomeRoute
import com.example.movieapp.navigation.MovieNavigation
import com.example.movieapp.navigation.ProfileRoute

@Composable
fun MovieAppRoot() {
    val navController = rememberNavController()

    data class BottomNavItem(
        val route: Any,
        val label: String,
        val icon: ImageVector
    )

    val bottomItems = listOf(
        BottomNavItem(HomeRoute, "Home", Icons.Default.Home),
        BottomNavItem(FavoritesRoute, "Favorites", Icons.Default.Favorite),
        BottomNavItem(ProfileRoute, "Profile", Icons.Default.Person)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar(modifier = Modifier.height(100.dp)) {
                bottomItems.forEach { item ->
                    val selected = currentDestination?.route == item.route
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
                                item.icon,
                                contentDescription = item.label,
                                modifier = Modifier.height(16.dp)
                            )
                        },
                        label = { Text(item.label, fontSize = 12.sp) }
                    )
                }
            }
        }
    ) { innerPadding ->
        MovieNavigation(
            navController = navController,
            modifier = Modifier.padding(innerPadding))
    }
}
