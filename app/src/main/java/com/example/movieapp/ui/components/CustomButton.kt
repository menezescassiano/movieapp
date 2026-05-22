package com.example.movieapp.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.movieapp.ui.theme.AccentPurple

@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    containerColor: Color = AccentPurple,
    shape: Shape = RoundedCornerShape(14.dp),
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = shape,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = containerColor,
                disabledContainerColor = containerColor.copy(alpha = 0.3f),
            ),
        enabled = enabled && !isLoading,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.height(22.dp),
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF12121A)
@Composable
private fun CustomButtonPreview() {
    CustomButton(text = "Continue", onClick = {})
}

@Preview(showBackground = true, backgroundColor = 0xFF12121A, name = "Loading")
@Composable
private fun CustomButtonLoadingPreview() {
    CustomButton(text = "Continue", onClick = {}, isLoading = true)
}

@Preview(showBackground = true, backgroundColor = 0xFF12121A, name = "Disabled")
@Composable
private fun CustomButtonDisabledPreview() {
    CustomButton(text = "Continue", onClick = {}, enabled = false)
}
