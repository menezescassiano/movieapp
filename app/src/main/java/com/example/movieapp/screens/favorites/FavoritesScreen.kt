package com.example.movieapp.screens.favorites

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.movieapp.model.Movie
import com.example.movieapp.model.getMoviesList
import com.example.movieapp.navigation.DetailsRoute
import com.example.movieapp.ui.components.MovieRow
import com.example.movieapp.ui.theme.AppBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navController: NavController,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.loadFavorites()
    }

    FavoritesScreenContent(
        uiState = uiState,
        onMovieClick = { navController.navigate(DetailsRoute(it.id)) },
        onRetry = { viewModel.loadFavorites() },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreenContent(
    uiState: FavoritesUiState,
    onMovieClick: (Movie) -> Unit = {},
    onRetry: () -> Unit = {},
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(AppBackground),
    ) {
        CenterAlignedTopAppBar(
            title = { Text("Favorites", color = Color.White) },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                ),
        )

        AnimatedContent(
            targetState = uiState,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            contentKey = { it.isLoading || it.errorMessage != null || it.movies.isEmpty() },
            label = "FavoritesTransition",
        ) { state ->
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.errorMessage != null -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(state.errorMessage, color = Color.White)
                            Button(onClick = onRetry) { Text("Retry") }
                        }
                    }

                    state.movies.isEmpty() && !state.isLoading -> {
                        Text(
                            text = "No favorites yet",
                            color = Color.White,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }

                    else -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(items = state.movies, key = { it.id }) { movie ->
                                MovieRow(
                                    movie = movie,
                                    onItemClick = { },
                                    modifier = Modifier.animateItem(),
                                )
                            }
                        }
                    }
                }

                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FavoritesScreenPreview() {
    FavoritesScreenContent(
        uiState =
            FavoritesUiState(
                movies = getMoviesList().filter { it.favorite },
            ),
    )
}
