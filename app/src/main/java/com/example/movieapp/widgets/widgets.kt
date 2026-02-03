package com.example.movieapp.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.movieapp.model.Movie
import com.example.movieapp.model.getMoviesList

@Preview
@Composable
fun MovieRow(movie: Movie = getMoviesList()[0], onItemClick: () -> Unit = {}) {

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
            Surface(
                modifier = Modifier.padding(12.dp),
                shape = RectangleShape,
                tonalElevation = 4.dp,
                color = Color.Transparent
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(movie.images.first())
                        .crossfade(true)
                        .size(400) // single side for square; adjust as needed
                        .build(),
                    contentDescription = "Movie Poster",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                )
            }
            Column(modifier = Modifier.padding(4.dp)) {
                Text(movie.title, style = MaterialTheme.typography.titleMedium)
                Text("Director: ${movie.director}", style = MaterialTheme.typography.bodyMedium)
                Text("Released: ${movie.year}", style = MaterialTheme.typography.bodyMedium)

                AnimatedVisibility(visible = expanded) {
                    Column() {
                        Text(movie.description, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = "See More",
                    modifier = Modifier
                        .size(25.dp)
                        .clickable {
                            expanded = !expanded
                        }
                )
            }

        }
    }
}

@Composable
fun StatusBubble(
    backgroundColor: Color,
    icon: ImageVector,
    contentDescription: String,
    message: String
) {
    Box(
        modifier = Modifier
            .size(140.dp)
            .background(
                color = backgroundColor,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color(0xFFF7F8F7),
                modifier = Modifier.size(72.dp)
            )
            Text(
                text = message,
                color = Color(0xFFF7F8F7)
            )
        }
    }
}

@Composable
fun QrScannerOverlay(
    modifier: Modifier = Modifier,
    borderColor: Color = Color.Transparent,
    borderWidth: Float = 6f,
    cornerRadius: Float = 32f,
    overlayAlpha: Float = 0.6f,
    boxAspectRatio: Float = 1f // 1f = quadrado
) {
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Define o tamanho da área de scan (por ex. 70% da largura)
        val boxWidth = canvasWidth * 0.7f
        val boxHeight = boxWidth / boxAspectRatio

        val left = (canvasWidth - boxWidth) / 2f
        val top = (canvasHeight - boxHeight) / 2f
        val right = left + boxWidth
        val bottom = top + boxHeight

        val scanRect = androidx.compose.ui.geometry.Rect(left, top, right, bottom)

        // Fundo escurecido
        drawRect(
            color = Color.Black.copy(alpha = overlayAlpha),
            size = size
        )

        // "Limpa" a área do retângulo central usando modo de composição
        drawRoundRect(
            color = Color.Transparent,
            topLeft = scanRect.topLeft,
            size = scanRect.size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius),
            blendMode = androidx.compose.ui.graphics.BlendMode.Clear
        )

        // Borda do retângulo
        drawRoundRect(
            color = borderColor,
            topLeft = scanRect.topLeft,
            size = scanRect.size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = borderWidth)
        )
    }
}
