package com.example.movieapp.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha

fun Modifier.invisible(): Modifier = this.alpha(0f)
