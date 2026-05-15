package com.example.movieapp.data

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentResolverUriReader @Inject constructor(
    @ApplicationContext private val context: Context,
) : UriReader {

    override fun getMimeType(uri: Uri): String =
        context.contentResolver.getType(uri) ?: "image/*"

    override fun readBytes(uri: Uri): ByteArray =
        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("Could not open image stream")
}
