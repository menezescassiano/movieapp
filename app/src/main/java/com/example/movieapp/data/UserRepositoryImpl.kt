package com.example.movieapp.data

import android.net.Uri
import com.example.movieapp.data.remote.UserApiService
import com.example.movieapp.data.remote.dto.UpdateUserRequest
import com.example.movieapp.data.remote.dto.toDomain
import com.example.movieapp.model.User
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val apiService: UserApiService,
    private val uriReader: UriReader,
) : UserRepository {

    override suspend fun getUser(): User = apiService.getUser().toDomain()

    override suspend fun updateUser(
        name: String,
        email: String,
        city: String,
        profilePictureUrl: String,
    ): User = apiService.updateUser(
        UpdateUserRequest(
            name = name,
            email = email,
            city = city
        )
    ).toDomain()

    override suspend fun uploadProfilePicture(uri: Uri): User {
        val mimeType = uriReader.getMimeType(uri)
        val bytes = uriReader.readBytes(uri)
        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData(
            name = MULTIPART_FIELD_NAME,
            filename = "profile_picture",
            body = requestBody
        )
        return apiService.uploadProfilePicture(part).toDomain()
    }

    companion object {
        private const val MULTIPART_FIELD_NAME = "file"
    }
}
