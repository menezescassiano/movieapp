package com.example.movieapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.movieapp.R
import com.example.movieapp.core.ui.theme.StarGold

@Composable
fun FavoriteBadge(
    color: Color = StarGold.copy(alpha = 0.15f),
    image: ImageVector = Icons.Filled.Star,
    contentDescription: String = stringResource(R.string.movie_row_favorited_description),
    tint: Color = StarGold,
    modifier: Modifier,
) {
    Box(
        modifier =
            modifier
                .padding(8.dp)
                .background(
                    color = color,
                    shape = RoundedCornerShape(6.dp),
                ).padding(4.dp),
    ) {
        Icon(
            imageVector = image,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(14.dp),
        )
    }
}

@Preview
@Composable
fun FavoriteBadgePreview() {
    FavoriteBadge(modifier = Modifier)
}
