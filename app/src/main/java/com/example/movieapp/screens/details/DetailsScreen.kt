package com.example.movieapp.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.example.movieapp.R
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.movieapp.model.getMoviesList
import com.example.movieapp.ui.components.FavoriteButton
import com.example.movieapp.ui.components.badge.GenreChip
import com.example.movieapp.ui.components.text.BodyText
import com.example.movieapp.ui.components.text.TitleText
import com.example.movieapp.ui.theme.AppBackground
import com.example.movieapp.ui.theme.CardDark
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
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.Top
                            ) {
                                // Title + Favorite button
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TitleText(
                                        text = movie.title,
                                        modifier = Modifier.weight(1f)
                                    )
                                    FavoriteButton(
                                        movie = movie,
                                        onFavoriteClick = onFavoriteClick
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Rating + Year row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = StarGold,
                                        modifier = Modifier
                                            .width(16.dp)
                                            .height(16.dp)
                                    )
                                    BodyText(
                                        text = movie.rating,
                                        color = StarGold
                                    )
                                    BodyText(
                                        text = movie.year,
                                        color = Color.White.copy(alpha = 0.6f)
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

                                // Image gallery
                                LazyRow {
                                    items(movie.images.size) { index ->
                                        Card(
                                            modifier = Modifier
                                                .padding(end = 8.dp)
                                                .width(300.dp)
                                                .height(200.dp),
                                            elevation = CardDefaults.cardElevation(),
                                            onClick = { selectedImageUrl = movie.images[index] }
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
                    onClose = { selectedImageUrl = null }
                )
            }
        }
    }
}

@Composable
fun DetailSection(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.45f),
            letterSpacing = 1.5.sp
        )
        content()
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
