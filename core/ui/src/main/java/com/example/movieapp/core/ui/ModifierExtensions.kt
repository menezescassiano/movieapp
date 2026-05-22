package com.example.movieapp.core.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha

fun Modifier.invisible(): Modifier = this.alpha(0f)
