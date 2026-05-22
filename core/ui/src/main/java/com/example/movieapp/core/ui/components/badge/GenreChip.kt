package com.example.movieapp.core.ui.components.badge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.movieapp.core.ui.theme.CardDark

@Composable
fun GenreChip(label: String) {
    Box(
        modifier =
            Modifier
                .background(
                    color = CardDark,
                    shape = MaterialTheme.shapes.small,
                ).padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.85f),
        )
    }
}
