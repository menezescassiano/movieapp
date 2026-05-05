package com.example.movieapp.screens.details

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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.movieapp.R
import com.example.movieapp.model.getMoviesList

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
    val isPreview = LocalInspectionMode.current
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.clickable { onBackClick() },
                        tint = Color.Black
                    )
                },
                title = { Text("Movie's details") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                uiState.errorMessage != null -> {
                    Text("Error: ${uiState.errorMessage}")
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
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                IconButton(onClick = onFavoriteClick) {
                                    Icon(
                                        imageVector = if (movie.favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        tint = if (movie.favorite) Color.Red else Color.Gray
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
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(movie.images[index])
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "Movie Image $index",
                                            placeholder = if (isPreview) painterResource(R.drawable.avatar) else null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.padding(8.dp))
                            Text(
                                text = movie.description,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                            )
                            Spacer(modifier = Modifier.padding(8.dp))
                            Text(
                                text = "Casting: ${movie.actors}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                            )

                        }
                    }

                }
            }
        }

        // Fullscreen image dialog
        selectedImageUrl?.let {
            ZoomableImage(
                imageUrl = it,
                modifier = Modifier.fillMaxSize(),
                onClose = { selectedImageUrl = null }
            )
        }

    }
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