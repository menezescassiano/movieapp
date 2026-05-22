package com.example.movieapp.ui.components.badge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.movieapp.core.ui.theme.YearBadgeBg

@Composable
fun YearBadge(
    year: String,
    color: Color = Color.White,
    modifier: Modifier,
) {
    Text(
        text = year,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color = color,
        modifier =
            modifier
                .padding(8.dp)
                .background(
                    color = YearBadgeBg.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(6.dp),
                ).padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

@Preview
@Composable
fun YearBadgePreview() {
    YearBadge(year = "2023", modifier = Modifier)
}
