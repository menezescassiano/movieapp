package com.example.movieapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

/**
 * Embeds a YouTube video using the android-youtube-player library.
 * Shows a shimmer skeleton while the player initialises.
 *
 * Accepts either a full YouTube URL (e.g. https://www.youtube.com/watch?v=XXXX)
 * or a bare video ID.
 */
@Composable
fun YouTubePlayer(
    videoUrl: String,
    modifier: Modifier = Modifier,
) {
    val videoId = remember(videoUrl) { extractYouTubeId(videoUrl) }
    var isLoading by remember { mutableStateOf(true) }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.matchParentSize(),
            factory = { context ->
                YouTubePlayerView(context).apply {
                    addYouTubePlayerListener(
                        object : AbstractYouTubePlayerListener() {
                            override fun onReady(youTubePlayer: YouTubePlayer) {
                                youTubePlayer.cueVideo(videoId, 0f)
                                isLoading = false
                            }
                        },
                    )
                }
            },
        )

        if (isLoading) {
            ImageSkeleton(modifier = Modifier.matchParentSize())
        }
    }
}

private fun extractYouTubeId(url: String): String {
    val patterns =
        listOf(
            Regex("""(?:v=)([a-zA-Z0-9_-]{11})"""),
            Regex("""youtu\.be/([a-zA-Z0-9_-]{11})"""),
        )
    for (pattern in patterns) {
        val match = pattern.find(url)
        if (match != null) return match.groupValues[1]
    }
    return url // assume it's already a bare video ID
}
