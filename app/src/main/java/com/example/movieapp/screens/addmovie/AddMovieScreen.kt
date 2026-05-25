package com.example.movieapp.screens.addmovie

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.movieapp.R
import com.example.movieapp.core.ui.components.text.BodyText
import com.example.movieapp.core.ui.components.text.TitleText
import com.example.movieapp.core.ui.theme.AccentPurple
import com.example.movieapp.core.ui.theme.AppBackground
import com.example.movieapp.core.ui.theme.CardDark
import com.example.movieapp.core.ui.theme.MovieAppTheme
import com.example.movieapp.model.TmdbMovieResult
import com.example.movieapp.ui.components.CustomButton
import com.example.movieapp.ui.components.MovieSearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMovieScreen(
    modifier: Modifier = Modifier,
    viewModel: AddMovieViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.addError) {
        uiState.addError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissAddError()
        }
    }
    AddMovieScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onAddMovie = viewModel::addMovie,
        modifier = modifier,
    )

}

@Composable
fun AddMovieScreenContent(
    uiState: AddMovieUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onSearchQueryChange: (String) -> Unit = {},
    onAddMovie: (Long) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppBackground),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp)
                    .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TitleText(stringResource(R.string.add_movie_headline))
            BodyText(
                text = stringResource(R.string.add_movie_subtitle),
                color = Color.White.copy(alpha = 0.55f),
            )

            MovieSearchBar(
                query = uiState.searchQuery,
                onQueryChange = onSearchQueryChange,
                placeholder = stringResource(R.string.add_movie_search_placeholder),
                modifier = Modifier.fillMaxWidth(),
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isSearching -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = AccentPurple,
                        )
                    }

                    uiState.searchError != null -> {
                        Text(
                            text = uiState.searchError,
                            color = Color.Red.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }

                    uiState.results.isEmpty() && uiState.searchQuery.isNotBlank() -> {
                        Text(
                            text = stringResource(R.string.add_movie_no_results, uiState.searchQuery),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }

                    uiState.results.isEmpty() -> {
                        Text(
                            text = stringResource(R.string.add_movie_empty_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }

                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(items = uiState.results, key = { it.tmdbId }) { movie ->
                                TmdbMovieItem(
                                    movie = movie,
                                    isAdding = uiState.addingTmdbId == movie.tmdbId,
                                    isAdded = movie.tmdbId in uiState.addedTmdbIds || movie.alreadyAdded,
                                    onAddClick = { onAddMovie(movie.tmdbId) },
                                )
                            }
                        }
                    }
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}

@Composable
private fun TmdbMovieItem(
    movie: TmdbMovieResult,
    isAdding: Boolean,
    isAdded: Boolean,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                AsyncImage(
                    model =
                        ImageRequest
                            .Builder(LocalContext.current)
                            .data(movie.poster)
                            .crossfade(true)
                            .build(),
                    contentDescription = movie.title,
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier
                            .width(80.dp)
                            .aspectRatio(2f / 3f)
                            .clip(RoundedCornerShape(10.dp)),
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = movie.year,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = movie.overview,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            CustomButton(
                text =
                    if (isAdded) {
                        stringResource(R.string.add_movie_button_added)
                    } else {
                        stringResource(R.string.add_movie_button_add)
                    },
                onClick = onAddClick,
                enabled = !isAdded && !isAdding,
                isLoading = isAdding,
                containerColor = if (isAdded) AccentPurple.copy(alpha = 0.4f) else AccentPurple,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
            )
        }
    }
}

private val previewMovie = TmdbMovieResult(
    tmdbId = 1L,
    title = "Interstellar",
    year = "2014",
    overview = "A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival.",
    poster = "",
    alreadyAdded = false
)

private val previewMovie2 = TmdbMovieResult(
    tmdbId = 1L,
    title = "Interstellar",
    year = "2014",
    overview = "A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival.",
    poster = "",
    alreadyAdded = true
)

@Preview(showBackground = true, backgroundColor = 0xFF12121A)
@Composable
private fun AddMovieScreenPreview() {
    MovieAppTheme {
        AddMovieScreenContent(uiState = AddMovieUiState())
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF12121A)
@Composable
private fun AddMovieScreenWithAddableMoviePreview() {
    MovieAppTheme {
        AddMovieScreenContent(
            uiState = AddMovieUiState(
                searchQuery = "Interstellar",
                results = listOf(previewMovie),
            )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF12121A)
@Composable
private fun AddMovieScreenWithAddedMoviePreview() {
    MovieAppTheme {
        AddMovieScreenContent(
            uiState = AddMovieUiState(
                searchQuery = "Interstellar",
                results = listOf(previewMovie),
                addedTmdbIds = setOf(previewMovie.tmdbId),
            ),
        )
    }
}


@Preview(showBackground = true, backgroundColor = 0xFF12121A)
@Composable
private fun AddMovieScreenWithAddedMovie2Preview() {
    MovieAppTheme {
        AddMovieScreenContent(
            uiState = AddMovieUiState(
                searchQuery = "Interstellar",
                results = listOf(previewMovie2),
                addedTmdbIds = setOf(previewMovie.tmdbId),
            ),
        )
    }
}
