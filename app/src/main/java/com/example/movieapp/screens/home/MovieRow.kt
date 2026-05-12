package com.example.movieapp.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.movieapp.R
import com.example.movieapp.model.Movie
import com.example.movieapp.model.getMoviesList
import com.example.movieapp.ui.theme.CardDark
import com.example.movieapp.ui.theme.StarGold
import com.example.movieapp.ui.theme.YearBadgeBg

// ---------------------------------------------------------------------------
// Original row card — kept for FavoritesScreen and other consumers
// ---------------------------------------------------------------------------

@Composable
fun MovieRow(movie: Movie, onItemClick: () -> Unit) {

    var expanded by remember {
        mutableStateOf(false)
    }
    Card(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .clickable {
                onItemClick()
            },
        shape = RoundedCornerShape(corner = CornerSize(16.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(movie.poster.replace("http://", "https://"))
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.movie_row_poster_description),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(12.dp)
                    .size(width = 60.dp, height = 85.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Column(modifier = Modifier.padding(4.dp)) {
                Text(movie.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.movie_row_director, movie.director),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    stringResource(R.string.movie_row_released, movie.year),
                    style = MaterialTheme.typography.bodyMedium
                )

                AnimatedVisibility(visible = expanded) {
                    Column {
                        Text(movie.description, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.movie_row_see_more),
                    modifier = Modifier
                        .size(25.dp)
                        .clickable { expanded = !expanded }
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// New poster card — used in the HomeScreen grid
// ---------------------------------------------------------------------------

@Composable
fun MovieCard(movie: Movie, onItemClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // Poster with year badge overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(movie.poster.replace("http://", "https://"))
                        .crossfade(true)
                        .build(),
                    contentDescription = movie.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Subtle gradient at bottom of the poster
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)),
                                startY = 200f
                            )
                        )
                )

                // Favorite star badge
                if (movie.favorite) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(
                                color = StarGold.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = stringResource(R.string.movie_row_favorited_description),
                            tint = StarGold,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Year badge
                Text(
                    text = movie.year,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .background(
                            color = YearBadgeBg.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            // Title
            Text(
                text = movie.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
            )
        }
    }
}

@Preview
@Composable
fun MovieRowPreview() {
    MovieRow(
        movie = getMoviesList()[0],
        onItemClick = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF12121A)
@Composable
fun MovieCardPreview() {
    MovieCard(
        movie = getMoviesList()[0],
        onItemClick = {}
    )
}
