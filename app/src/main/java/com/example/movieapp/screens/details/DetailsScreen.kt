package com.example.movieapp.screens.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.movieapp.R
import com.example.movieapp.core.ui.components.badge.GenreChip
import com.example.movieapp.core.ui.components.text.BodyText
import com.example.movieapp.core.ui.components.text.TitleText
import com.example.movieapp.core.ui.theme.AppBackground
import com.example.movieapp.core.ui.theme.StarGold
import com.example.movieapp.model.getMoviesList
import com.example.movieapp.ui.components.FavoriteButton
import com.example.movieapp.ui.components.ImageSkeleton
import com.example.movieapp.ui.components.YouTubePlayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    navController: NavController,
    movieId: String?,
    modifier: Modifier = Modifier,
    viewModel: DetailsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(movieId) {
        movieId?.let { viewModel.loadMovie(it) }
    }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) navController.popBackStack()
    }

    DetailsScreenContent(
        uiState = uiState,
        modifier = modifier,
        onBackClick = { navController.popBackStack() },
        onFavoriteClick = { movieId?.let { viewModel.toggleFavorite(it) } },
        onMenuClick = { movieId?.let { viewModel.deleteMovie(it) } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreenContent(
    uiState: DetailsUiState,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
) {
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(0),
                navigationIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.details_back),
                        modifier = Modifier.clickable { onBackClick() },
                        tint = Color.White,
                    )
                },
                title = { Text(stringResource(R.string.details_title)) },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = AppBackground,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                    ),
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "bla",
                                tint = Color.White,
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.details_delete_movie)) },
                                onClick = {
                                    menuExpanded = false
                                    onMenuClick()
                                }
                            )
                        }
                    }
                }
            )
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                color = AppBackground,
            ) {
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    uiState.errorMessage != null -> {
                        Text(stringResource(R.string.details_error, uiState.errorMessage))
                    }

                    else -> {
                        uiState.movie?.let { movie ->
                            Column(
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.Top,
                            ) {
                                // Title + Favorite button
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TitleText(
                                        text = movie.title,
                                        modifier = Modifier.weight(1f),
                                    )
                                    FavoriteButton(
                                        movie = movie,
                                        onFavoriteClick = onFavoriteClick,
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Rating + Year row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = StarGold,
                                        modifier =
                                            Modifier
                                                .width(16.dp)
                                                .height(16.dp),
                                    )
                                    BodyText(
                                        text = movie.rating,
                                        color = StarGold,
                                    )
                                    BodyText(
                                        text = movie.year,
                                        color = Color.White.copy(alpha = 0.6f),
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Genre chips
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val genres = movie.genre.split(",").map { it.trim() }
                                    items(genres.size) { index ->
                                        GenreChip(label = genres[index])
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Trailer
                                movie.trailerUrl?.let {
                                    DetailSection(label = stringResource(R.string.details_trailer)) {
                                        YouTubePlayer(
                                            videoUrl = it,
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .height(220.dp),
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                // Image gallery
                                LazyRow {
                                    items(movie.images.size) { index ->
                                        Card(
                                            modifier =
                                                Modifier
                                                    .padding(end = 8.dp)
                                                    .width(300.dp)
                                                    .height(200.dp),
                                            elevation = CardDefaults.cardElevation(),
                                            onClick = { selectedImageUrl = movie.images[index] },
                                        ) {
                                            SubcomposeAsyncImage(
                                                model =
                                                    ImageRequest
                                                        .Builder(LocalContext.current)
                                                        .data(movie.images[index])
                                                        .crossfade(true)
                                                        .build(),
                                                contentDescription =
                                                    stringResource(
                                                        R.string.details_movie_image,
                                                        index,
                                                    ),
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize(),
                                                loading = { ImageSkeleton(modifier = Modifier.fillMaxSize()) },
                                                error = { ImageSkeleton(modifier = Modifier.fillMaxSize()) },
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Synopsis
                                DetailSection(label = stringResource(R.string.details_synopsis)) {
                                    BodyText(text = movie.description)
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Director
                                DetailSection(label = stringResource(R.string.details_director)) {
                                    BodyText(text = movie.director)
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Cast
                                DetailSection(label = stringResource(R.string.details_cast)) {
                                    BodyText(text = movie.actors)
                                }
                            }
                        }
                    }
                }
            }

            // Fullscreen image overlay
            selectedImageUrl?.let {
                ZoomableImage(
                    imageUrl = it,
                    modifier = Modifier.fillMaxSize(),
                    onClose = { selectedImageUrl = null },
                )
            }
        }
    }
}

@Composable
fun DetailSection(
    label: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.45f),
            letterSpacing = 1.5.sp,
        )
        content()
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsScreenPreview() {
    DetailsScreenContent(
        uiState =
            DetailsUiState(
                movie = getMoviesList().first(),
            ),
    )
}
