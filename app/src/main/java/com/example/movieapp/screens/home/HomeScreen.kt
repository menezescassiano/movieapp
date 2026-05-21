package com.example.movieapp.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.movieapp.model.Movie
import com.example.movieapp.R
import com.example.movieapp.navigation.DetailsRoute
import com.example.movieapp.navigation.QrCodeRoute
import com.example.movieapp.ui.components.text.BodyText
import com.example.movieapp.ui.components.MovieCard
import com.example.movieapp.ui.components.MovieSearchBar
import com.example.movieapp.ui.components.text.TitleText
import com.example.movieapp.ui.theme.AccentPurple
import com.example.movieapp.ui.theme.AppBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.loadMovies()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        AnimatedContent(
            targetState = uiState,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            contentKey = { it.isLoading && it.movies.isEmpty() || it.errorMessage != null },
            label = "HomeScreenTransition"
        ) { state ->
            when {
                state.isLoading && state.movies.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = AccentPurple
                        )
                    }
                }

                state.errorMessage != null -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = state.errorMessage ?: "Unknown error",
                                color = Color.White
                            )
                            Button(onClick = { viewModel.loadMovies() }) {
                                Text(stringResource(R.string.home_retry))
                            }
                        }
                    }
                }

                else -> {
                    PullToRefreshBox(
                        isRefreshing = state.isRefreshing,
                        onRefresh = { viewModel.refresh() },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        MainContent(
                            navController = navController,
                            uiState = state,
                            onSearchQueryChange = viewModel::onSearchQueryChange,
                            onLoadNextPage = viewModel::loadNextPage
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            onClick = { navController.navigate(QrCodeRoute) },
            containerColor = AccentPurple,
            elevation = FloatingActionButtonDefaults.elevation(0.dp)
        ) {
            Icon(
                Icons.Default.QrCodeScanner,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

@Composable
fun MainContent(
    navController: NavController,
    uiState: HomeUiState,
    onSearchQueryChange: (String) -> Unit,
    onLoadNextPage: () -> Unit
) {
    val gridState = rememberLazyGridState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = gridState.layoutInfo.totalItemsCount
            totalItems > 0 && lastVisible >= totalItems - 3
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadNextPage()
    }

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item(span = { GridItemSpan(2) }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TitleText(stringResource(R.string.home_title))
                BodyText(
                    text = stringResource(R.string.home_subtitle),
                    color = Color.White.copy(alpha = 0.55f)
                )
                MovieSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = onSearchQueryChange,
                    placeholder = stringResource(R.string.home_search_placeholder),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (uiState.movies.isEmpty()) {
            item(span = { GridItemSpan(2) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.home_search_no_results, uiState.searchQuery),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            items(items = uiState.movies, key = { it.id }) { movie ->
                MovieCard(
                    movie = movie,
                    onItemClick = { navController.navigate(DetailsRoute(movie.id)) },
                    modifier = Modifier.animateItem()
                )
            }

            if (uiState.isLoading) {
                item(span = { GridItemSpan(2) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = AccentPurple,
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        }
    }
}
