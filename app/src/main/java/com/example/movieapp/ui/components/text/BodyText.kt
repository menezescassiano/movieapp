package com.example.movieapp.ui.components.text

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun BodyText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = Color.White.copy(alpha = 0.8f)
) {
    Text(
        modifier = modifier,
        text = text,
        style = style,
        color = color
    )
}

@Preview
@Composable
fun BodyTextPreview() {
    BodyText(text = "This is a body text")
}
