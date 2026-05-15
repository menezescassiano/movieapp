package com.example.movieapp.data

import android.net.Uri
import com.example.movieapp.data.remote.UserApiService
import com.example.movieapp.data.remote.dto.UpdateUserRequest
import com.example.movieapp.data.remote.dto.UserDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UserRepositoryImplTest {

    private lateinit var apiService: UserApiService
    private lateinit var uriReader: UriReader
    private lateinit var repository: UserRepositoryImpl

    private val fakeDto = UserDto(
        id = "1",
        name = "John Doe",
        email = "john@example.com",
        city = "São Paulo",
        profilePictureUrl = "https://example.com/photo.jpg"
    )

    @Before
    fun setUp() {
        apiService = mockk()
        uriReader = mockk()
        repository = UserRepositoryImpl(apiService, uriReader)
    }

    // ── getUser ──────────────────────────────────────────────────────────

    @Test
    fun `getUser maps dto to domain user`() = runTest {
        coEvery { apiService.getUser() } returns fakeDto

        val result = repository.getUser()

        assertEquals(fakeDto.id, result.id)
        assertEquals(fakeDto.name, result.name)
        assertEquals(fakeDto.email, result.email)
        assertEquals(fakeDto.city, result.city)
        assertEquals(fakeDto.profilePictureUrl, result.profilePictureUrl)
    }

    @Test
    fun `getUser maps null optional fields to empty strings`() = runTest {
        coEvery { apiService.getUser() } returns fakeDto.copy(
            name = null,
            email = null,
            city = null,
            profilePictureUrl = null
        )

        val result = repository.getUser()

        assertEquals("", result.name)
        assertEquals("", result.email)
        assertEquals("", result.city)
        assertEquals("", result.profilePictureUrl)
    }

    // ── updateUser ───────────────────────────────────────────────────────

    @Test
    fun `updateUser sends correct request body and returns mapped user`() = runTest {
        val requestSlot = slot<UpdateUserRequest>()
        val updatedDto = fakeDto.copy(name = "Jane Doe")
        coEvery { apiService.updateUser(capture(requestSlot)) } returns updatedDto

        val result = repository.updateUser(
            name = "Jane Doe",
            email = "john@example.com",
            city = "São Paulo",
            profilePictureUrl = "https://example.com/photo.jpg"
        )

        assertEquals("Jane Doe", requestSlot.captured.name)
        assertEquals("john@example.com", requestSlot.captured.email)
        assertEquals("São Paulo", requestSlot.captured.city)
        assertEquals("Jane Doe", result.name)
    }

    @Test
    fun `updateUser propagates exception from api`() = runTest {
        coEvery { apiService.updateUser(any()) } throws RuntimeException("Server error")

        val exception = runCatching {
            repository.updateUser("name", "email", "city", "url")
        }.exceptionOrNull()

        assertEquals("Server error", exception?.message)
    }

    // ── uploadProfilePicture ─────────────────────────────────────────────

    @Test
    fun `uploadProfilePicture sends multipart with field name 'file' and returns mapped user`() = runTest {
        val uri = mockk<Uri>()
        val imageBytes = ByteArray(16) { it.toByte() }
        val partSlot = slot<MultipartBody.Part>()

        every { uriReader.getMimeType(uri) } returns "image/jpeg"
        every { uriReader.readBytes(uri) } returns imageBytes
        coEvery { apiService.uploadProfilePicture(capture(partSlot)) } returns fakeDto

        val result = repository.uploadProfilePicture(uri)

        val contentDisposition = partSlot.captured.headers?.get("Content-Disposition").orEmpty()
        assertEquals(true, contentDisposition.contains("name=\"file\""))
        assertEquals(fakeDto.id, result.id)
        assertEquals(fakeDto.profilePictureUrl, result.profilePictureUrl)
        coVerify(exactly = 1) { apiService.uploadProfilePicture(any()) }
    }

    @Test
    fun `uploadProfilePicture uses mime type from uriReader`() = runTest {
        val uri = mockk<Uri>()

        every { uriReader.getMimeType(uri) } returns "image/png"
        every { uriReader.readBytes(uri) } returns ByteArray(4)
        coEvery { apiService.uploadProfilePicture(any()) } returns fakeDto

        repository.uploadProfilePicture(uri)

        // se o mime type fosse ignorado, o mock de "image/png" seria irrelevante —
        // o teste confirma que getMimeType é chamado
        coVerify(exactly = 1) { uriReader.getMimeType(uri) }
    }

    @Test
    fun `uploadProfilePicture propagates exception from uriReader`() = runTest {
        val uri = mockk<Uri>()

        every { uriReader.getMimeType(uri) } returns "image/jpeg"
        every { uriReader.readBytes(uri) } throws IllegalStateException("Could not open image stream")

        val exception = runCatching { repository.uploadProfilePicture(uri) }.exceptionOrNull()

        assertEquals("Could not open image stream", exception?.message)
    }

    @Test
    fun `uploadProfilePicture propagates api exception`() = runTest {
        val uri = mockk<Uri>()

        every { uriReader.getMimeType(uri) } returns "image/jpeg"
        every { uriReader.readBytes(uri) } returns ByteArray(4)
        coEvery { apiService.uploadProfilePicture(any()) } throws RuntimeException("Upload failed")

        val exception = runCatching { repository.uploadProfilePicture(uri) }.exceptionOrNull()

        assertEquals("Upload failed", exception?.message)
    }
}
