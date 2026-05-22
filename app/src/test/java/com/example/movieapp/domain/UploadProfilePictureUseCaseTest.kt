package com.example.movieapp.domain

import android.net.Uri
import com.example.movieapp.data.UserRepository
import com.example.movieapp.model.User
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UploadProfilePictureUseCaseTest {
    private lateinit var repository: UserRepository
    private lateinit var useCase: UploadProfilePictureUseCase

    private val fakeUser =
        User(
            id = "1",
            name = "John Doe",
            email = "john@example.com",
            city = "São Paulo",
            profilePictureUrl = "https://example.com/photo.jpg",
        )

    @Before
    fun setUp() {
        repository = mockk()
        useCase = UploadProfilePictureUseCase(repository)
    }

    @Test
    fun `invoke delegates to repository and returns updated user`() =
        runTest {
            val uri = mockk<Uri>()
            coEvery { repository.uploadProfilePicture(uri) } returns fakeUser

            val result = useCase(uri)

            assertEquals(fakeUser, result)
            coVerify(exactly = 1) { repository.uploadProfilePicture(uri) }
        }

    @Test
    fun `invoke propagates exception from repository`() =
        runTest {
            val uri = mockk<Uri>()
            coEvery { repository.uploadProfilePicture(uri) } throws RuntimeException("Upload failed")

            val exception = runCatching { useCase(uri) }.exceptionOrNull()

            assertEquals("Upload failed", exception?.message)
        }
}
