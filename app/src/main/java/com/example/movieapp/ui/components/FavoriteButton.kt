package com.example.movieapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.movieapp.R
import com.example.movieapp.core.ui.theme.StarGold
import com.example.movieapp.model.Movie

@Composable
fun FavoriteButton(
    movie: Movie,
    onFavoriteClick: () -> Unit,
) {
    val starColor by animateColorAsState(
        targetValue = if (movie.favorite) StarGold else Color.White,
        animationSpec = tween(durationMillis = 300),
    )
    val starScale by animateFloatAsState(
        targetValue = if (movie.favorite) 1f else 0.85f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
    )
    IconButton(onClick = onFavoriteClick) {
        Icon(
            imageVector = if (movie.favorite) Icons.Filled.Check else Icons.Outlined.AddCircleOutline,
            contentDescription = stringResource(R.string.details_favorite),
            tint = starColor,
            modifier = Modifier.scale(starScale),
        )
    }
}

private val previewMovie =
    Movie(
        id = "1",
        title = "Inception",
        year = "2010",
        genre = "Sci-Fi",
        director = "Christopher Nolan",
        actors = "Leonardo DiCaprio",
        description = "A thief who steals corporate secrets.",
        poster = "",
        images = emptyList(),
        rating = "8.8",
    )

@Preview(name = "Not Favorite", showBackground = true, backgroundColor = 0xFF1C1C1C)
@Composable
private fun FavoriteButtonNotFavoritePreview() {
    FavoriteButton(
        movie = previewMovie.copy(favorite = false),
        onFavoriteClick = {},
    )
}

@Preview(name = "Favorite", showBackground = true, backgroundColor = 0xFF1C1C1C)
@Composable
private fun FavoriteButtonFavoritePreview() {
    FavoriteButton(
        movie = previewMovie.copy(favorite = true),
        onFavoriteClick = {},
    )
}
