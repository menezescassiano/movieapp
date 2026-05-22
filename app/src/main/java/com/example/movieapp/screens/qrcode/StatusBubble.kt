package com.example.movieapp.screens.qrcode

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun StatusBubble(
    backgroundColor: Color,
    icon: ImageVector,
    contentDescription: String,
    message: String,
) {
    Box(
        modifier =
            Modifier
                .size(140.dp)
                .background(
                    color = backgroundColor,
                    shape = CircleShape,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color(0xFFF7F8F7),
                modifier = Modifier.size(72.dp),
            )
            Text(
                text = message,
                color = Color(0xFFF7F8F7),
            )
        }
    }
}
