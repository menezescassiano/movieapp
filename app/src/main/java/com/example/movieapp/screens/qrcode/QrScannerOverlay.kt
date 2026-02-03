package com.example.movieapp.screens.qrcode

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun QrScannerOverlay(
    modifier: Modifier = Modifier,
    borderColor: Color = Color.Transparent,
    borderWidth: Float = 6f,
    cornerRadius: Float = 32f,
    overlayAlpha: Float = 0.6f,
    boxAspectRatio: Float = 1f // 1f = squared
) {
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Size of the scan area: 70%
        val boxWidth = canvasWidth * 0.7f
        val boxHeight = boxWidth / boxAspectRatio

        val left = (canvasWidth - boxWidth) / 2f
        val top = (canvasHeight - boxHeight) / 2f
        val right = left + boxWidth
        val bottom = top + boxHeight

        val scanRect = androidx.compose.ui.geometry.Rect(left, top, right, bottom)

        // Dark background
        drawRect(
            color = Color.Black.copy(alpha = overlayAlpha),
            size = size
        )

        // Cleans the central rectangular area using composition mode
        drawRoundRect(
            color = Color.Transparent,
            topLeft = scanRect.topLeft,
            size = scanRect.size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius),
            blendMode = androidx.compose.ui.graphics.BlendMode.Clear
        )

        // Rectangle border
        drawRoundRect(
            color = borderColor,
            topLeft = scanRect.topLeft,
            size = scanRect.size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = borderWidth)
        )
    }
}