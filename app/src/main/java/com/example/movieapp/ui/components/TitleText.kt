package com.example.movieapp.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun TitleText(
    text: String,
    style: TextStyle = MaterialTheme.typography.headlineMedium,
    color: Color = Color.White
) {
    Text(
        text = text,
        style = style,
        color = color
    )
}

@Preview
@Composable
fun TitleTextPreview() {
    TitleText(text = "Breaking Bad")
}