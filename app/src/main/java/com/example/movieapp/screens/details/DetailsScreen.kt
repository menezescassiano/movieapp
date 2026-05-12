package com.example.movieapp.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.movieapp.R
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.movieapp.model.getMoviesList
import com.example.movieapp.ui.theme.AppBackground
import com.example.movieapp.ui.theme.ShimmerBase
import com.example.movieapp.ui.theme.ShimmerHighlight
import com.example.movieapp.ui.theme.StarGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    navController: NavController,
    movieId: String?,
    viewModel: DetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(movieId) {
        movieId?.let { viewModel.loadMovie(it) }
    }

    DetailsScreenContent(
        uiState = uiState,
        onBackClick = { navController.popBackStack() },
        onFavoriteClick = { movieId?.let { viewModel.toggleFavorite(it) } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreenContent(
    uiState: DetailsUiState,
    onBackClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {}
) {
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.details_back),
                        modifier = Modifier.clickable { onBackClick() },
                        tint = Color.White
                    )
                },
                title = { Text(stringResource(R.string.details_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppBackground,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                color = AppBackground
            ) {

                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
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
                                Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Top
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = movie.title,
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = Color.White
                                    )
                                    IconButton(onClick = onFavoriteClick) {
                                        Icon(
                                            imageVector = if (movie.favorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                            contentDescription = stringResource(R.string.details_favorite),
                                            tint = if (movie.favorite) StarGold else Color.White
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.padding(8.dp))
                                LazyRow {
                                    items(movie.images.size) { index ->
                                        Card(
                                            modifier = Modifier
                                                .padding(4.dp)
                                                .width(300.dp)
                                                .height(240.dp),
                                            elevation = CardDefaults.cardElevation(),
                                            onClick = {
                                                selectedImageUrl = movie.images[index]
                                            }
                                        ) {
                                            SubcomposeAsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(movie.images[index])
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = stringResource(
                                                    R.string.details_movie_image,
                                                    index
                                                ),
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize(),
                                                loading = { ImageSkeleton(modifier = Modifier.fillMaxSize()) },
                                                error = { ImageSkeleton(modifier = Modifier.fillMaxSize()) }
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.padding(8.dp))
                                Text(
                                    text = movie.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.padding(8.dp))
                                Text(
                                    text = stringResource(R.string.details_casting, movie.actors),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier,
                                    color = Color.White.copy(alpha = 0.8f)
                                )

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
                    onClose = { selectedImageUrl = null }
                )
            }
        }
    }
}

@Composable
fun ImageSkeleton(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )
    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                colors = listOf(
                    ShimmerBase,
                    ShimmerHighlight,
                    ShimmerBase
                ),
                start = Offset(translateAnim - 600f, 0f),
                end = Offset(translateAnim, 0f)
            )
        )
    )
}

@Preview(showBackground = true)
@Composable
fun DetailsScreenPreview() {
    DetailsScreenContent(
        uiState = DetailsUiState(
            movie = getMoviesList().first()
        )
    )
}
