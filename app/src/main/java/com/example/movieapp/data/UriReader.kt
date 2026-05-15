package com.example.movieapp.data

import android.net.Uri

interface UriReader {
    fun readBytes(uri: Uri): ByteArray
    fun getMimeType(uri: Uri): String
}
