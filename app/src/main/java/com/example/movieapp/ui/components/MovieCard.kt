package com.example.movieapp.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.movieapp.R
import com.example.movieapp.model.Movie
import com.example.movieapp.ui.components.badge.YearBadge
import com.example.movieapp.ui.components.text.MovieCardTitle
import com.example.movieapp.ui.theme.CardDark

@Composable
fun MovieCard(
    movie: Movie,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes posterOverride: Int? = null,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { onItemClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column {
            // Poster with year badge overlay
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f / 3f),
            ) {
                if (posterOverride != null) {
                    Image(
                        painter = painterResource(id = posterOverride),
                        contentDescription = movie.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    AsyncImage(
                        model =
                            ImageRequest
                                .Builder(LocalContext.current)
                                .data(movie.poster.replace("http://", "https://"))
                                .crossfade(true)
                                .build(),
                        contentDescription = movie.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                // Subtle gradient at bottom of the poster
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)),
                                    startY = 200f,
                                ),
                            ),
                )

                // Favorite star badge
                if (movie.favorite) {
                    FavoriteBadge(modifier = Modifier.align(Alignment.TopEnd))
                }

                // Year badge
                YearBadge(
                    year = movie.year,
                    modifier = Modifier.align(Alignment.BottomStart),
                )
            }

            // Title
            MovieCardTitle(title = movie.title)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF12121A)
@Composable
fun MovieCardPreview() {
    MovieCard(
        movie =
            Movie(
                id = "1",
                title = "Breaking Bad",
                year = "2008",
                genre = "Drama",
                director = "Vince Gilligan",
                actors = "Bryan Cranston",
                description = "Preview",
                poster = "",
                images = emptyList(),
                rating = "9.5",
                favorite = true,
            ),
        onItemClick = {},
        posterOverride = R.drawable.breaking_bad_poster,
    )
}
