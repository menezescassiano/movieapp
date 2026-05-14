package com.example.movieapp.screens.profile

import android.net.Uri
import com.example.movieapp.domain.GetUserUseCase
import com.example.movieapp.domain.UpdateUserUseCase
import com.example.movieapp.model.User
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var getUserUseCase: GetUserUseCase
    private lateinit var updateUserUseCase: UpdateUserUseCase
    private lateinit var viewModel: ProfileViewModel

    private val fakeUser = User(
        id = "1",
        name = "John Doe",
        email = "john@example.com",
        city = "São Paulo",
        profilePictureUrl = "https://example.com/photo.jpg"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getUserUseCase = mockk()
        updateUserUseCase = mockk()
        viewModel = ProfileViewModel(getUserUseCase, updateUserUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── estado inicial ───────────────────────────────────────────────────

    @Test
    fun `initial state has isLoading true and no user`() {
        val state = viewModel.uiState.value
        assertTrue(state.isLoading)
        assertNull(state.user)
        assertNull(state.errorMessage)
        assertFalse(state.isSaving)
        assertFalse(state.showAvatarPicker)
        assertNull(state.editingField)
        assertNull(state.avatarUri)
    }

    // ── loadProfile ──────────────────────────────────────────────────────

    @Test
    fun `loadProfile sets user and clears loading on success`() = runTest {
        coEvery { getUserUseCase() } returns fakeUser

        viewModel.loadProfile()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(fakeUser, state.user)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun `loadProfile sets errorMessage and clears loading on failure`() = runTest {
        coEvery { getUserUseCase() } throws RuntimeException("Network error")

        viewModel.loadProfile()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.user)
        assertFalse(state.isLoading)
        assertEquals("Network error", state.errorMessage)
    }

    @Test
    fun `loadProfile sets generic message when exception has no message`() = runTest {
        coEvery { getUserUseCase() } throws RuntimeException()

        viewModel.loadProfile()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Failed to load profile", state.errorMessage)
        assertFalse(state.isLoading)
    }

    @Test
    fun `loadProfile clears previous error on success`() = runTest {
        coEvery { getUserUseCase() } throws RuntimeException("First error")
        viewModel.loadProfile()
        advanceUntilIdle()

        assertEquals("First error", viewModel.uiState.value.errorMessage)

        coEvery { getUserUseCase() } returns fakeUser
        viewModel.loadProfile()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals(fakeUser, viewModel.uiState.value.user)
    }

    // ── avatar ───────────────────────────────────────────────────────────

    @Test
    fun `onAvatarClick sets showAvatarPicker to true`() {
        viewModel.onAvatarClick()

        assertTrue(viewModel.uiState.value.showAvatarPicker)
    }

    @Test
    fun `onAvatarPickerDismiss sets showAvatarPicker to false`() {
        viewModel.onAvatarClick()
        viewModel.onAvatarPickerDismiss()

        assertFalse(viewModel.uiState.value.showAvatarPicker)
    }

    @Test
    fun `onPhotoTaken updates avatarUri and hides picker`() {
        val uri = mockk<Uri>()
        viewModel.onAvatarClick()
        viewModel.onPhotoTaken(uri)

        val state = viewModel.uiState.value
        assertEquals(uri, state.avatarUri)
        assertFalse(state.showAvatarPicker)
    }

    @Test
    fun `onImagePicked updates avatarUri and hides picker`() {
        val uri = mockk<Uri>()
        viewModel.onAvatarClick()
        viewModel.onImagePicked(uri)

        val state = viewModel.uiState.value
        assertEquals(uri, state.avatarUri)
        assertFalse(state.showAvatarPicker)
    }

    // ── edit field ───────────────────────────────────────────────────────

    @Test
    fun `onEditField sets editingField correctly`() {
        viewModel.onEditField(EditableField.NAME)
        assertEquals(EditableField.NAME, viewModel.uiState.value.editingField)

        viewModel.onEditField(EditableField.EMAIL)
        assertEquals(EditableField.EMAIL, viewModel.uiState.value.editingField)

        viewModel.onEditField(EditableField.CITY)
        assertEquals(EditableField.CITY, viewModel.uiState.value.editingField)
    }

    @Test
    fun `onEditFieldDismiss clears editingField`() {
        viewModel.onEditField(EditableField.NAME)
        viewModel.onEditFieldDismiss()

        assertNull(viewModel.uiState.value.editingField)
    }

    // ── onEditFieldConfirm ───────────────────────────────────────────────

    @Test
    fun `onEditFieldConfirm NAME updates name and saves profile`() = runTest {
        val updatedUser = fakeUser.copy(name = "Jane Doe")
        coEvery { getUserUseCase() } returns fakeUser
        coEvery {
            updateUserUseCase(
                name = "Jane Doe",
                email = fakeUser.email,
                city = fakeUser.city,
                profilePictureUrl = fakeUser.profilePictureUrl
            )
        } returns updatedUser

        viewModel.loadProfile()
        advanceUntilIdle()

        viewModel.onEditFieldConfirm(EditableField.NAME, "Jane Doe")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Jane Doe", state.user?.name)
        assertNull(state.editingField)
        assertFalse(state.isSaving)
        assertNull(state.errorMessage)
    }

    @Test
    fun `onEditFieldConfirm EMAIL updates email and saves profile`() = runTest {
        val updatedUser = fakeUser.copy(email = "jane@example.com")
        coEvery { getUserUseCase() } returns fakeUser
        coEvery {
            updateUserUseCase(
                name = fakeUser.name,
                email = "jane@example.com",
                city = fakeUser.city,
                profilePictureUrl = fakeUser.profilePictureUrl
            )
        } returns updatedUser

        viewModel.loadProfile()
        advanceUntilIdle()

        viewModel.onEditFieldConfirm(EditableField.EMAIL, "jane@example.com")
        advanceUntilIdle()

        assertEquals("jane@example.com", viewModel.uiState.value.user?.email)
    }

    @Test
    fun `onEditFieldConfirm CITY updates city and saves profile`() = runTest {
        val updatedUser = fakeUser.copy(city = "Rio de Janeiro")
        coEvery { getUserUseCase() } returns fakeUser
        coEvery {
            updateUserUseCase(
                name = fakeUser.name,
                email = fakeUser.email,
                city = "Rio de Janeiro",
                profilePictureUrl = fakeUser.profilePictureUrl
            )
        } returns updatedUser

        viewModel.loadProfile()
        advanceUntilIdle()

        viewModel.onEditFieldConfirm(EditableField.CITY, "Rio de Janeiro")
        advanceUntilIdle()

        assertEquals("Rio de Janeiro", viewModel.uiState.value.user?.city)
    }

    @Test
    fun `onEditFieldConfirm does nothing when user is null`() = runTest {
        viewModel.onEditFieldConfirm(EditableField.NAME, "Jane Doe")
        advanceUntilIdle()

        coVerify(exactly = 0) { updateUserUseCase(any(), any(), any(), any()) }
        assertNull(viewModel.uiState.value.user)
    }

    @Test
    fun `onEditFieldConfirm clears editingField even before save completes`() = runTest {
        coEvery { getUserUseCase() } returns fakeUser
        coEvery { updateUserUseCase(any(), any(), any(), any()) } returns fakeUser

        viewModel.loadProfile()
        advanceUntilIdle()

        viewModel.onEditField(EditableField.NAME)
        viewModel.onEditFieldConfirm(EditableField.NAME, "Jane Doe")

        assertNull(viewModel.uiState.value.editingField)
    }

    // ── update otimista ──────────────────────────────────────────────────

    @Test
    fun `onEditFieldConfirm updates user optimistically before save completes`() = runTest {
        coEvery { getUserUseCase() } returns fakeUser
        coEvery { updateUserUseCase(any(), any(), any(), any()) } returns fakeUser.copy(name = "Jane Doe")

        viewModel.loadProfile()
        advanceUntilIdle()

        viewModel.onEditFieldConfirm(EditableField.NAME, "Jane Doe")

        // sem advanceUntilIdle: a atualização síncrona já aconteceu antes do save async
        assertEquals("Jane Doe", viewModel.uiState.value.user?.name)
        assertNull(viewModel.uiState.value.editingField)
    }

    @Test
    fun `save failure does not revert optimistic user update`() = runTest {
        coEvery { getUserUseCase() } returns fakeUser
        coEvery { updateUserUseCase(any(), any(), any(), any()) } throws RuntimeException("Save error")

        viewModel.loadProfile()
        advanceUntilIdle()

        viewModel.onEditFieldConfirm(EditableField.NAME, "Jane Doe")
        advanceUntilIdle()

        // user mantém o valor otimista mesmo após falha no save
        assertEquals("Jane Doe", viewModel.uiState.value.user?.name)
        assertEquals("Save error", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `save clears previous errorMessage when starting new save`() = runTest {
        // primeiro save falha e deixa errorMessage
        coEvery { getUserUseCase() } returns fakeUser
        coEvery { updateUserUseCase(any(), any(), any(), any()) } throws RuntimeException("Save error")

        viewModel.loadProfile()
        advanceUntilIdle()
        viewModel.onEditFieldConfirm(EditableField.NAME, "Jane Doe")
        advanceUntilIdle()
        assertEquals("Save error", viewModel.uiState.value.errorMessage)

        // segundo save bem-sucedido deve limpar o erro
        coEvery { updateUserUseCase(any(), any(), any(), any()) } returns fakeUser.copy(name = "Jane Doe")
        viewModel.onEditFieldConfirm(EditableField.EMAIL, "jane@example.com")
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.errorMessage)
    }

    // ── saveProfile (via onEditFieldConfirm) ─────────────────────────────

    @Test
    fun `save failure sets errorMessage and clears isSaving`() = runTest {
        coEvery { getUserUseCase() } returns fakeUser
        coEvery { updateUserUseCase(any(), any(), any(), any()) } throws RuntimeException("Save error")

        viewModel.loadProfile()
        advanceUntilIdle()

        viewModel.onEditFieldConfirm(EditableField.NAME, "Jane Doe")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Save error", state.errorMessage)
        assertFalse(state.isSaving)
    }

    @Test
    fun `save failure with no message sets generic error`() = runTest {
        coEvery { getUserUseCase() } returns fakeUser
        coEvery { updateUserUseCase(any(), any(), any(), any()) } throws RuntimeException()

        viewModel.loadProfile()
        advanceUntilIdle()

        viewModel.onEditFieldConfirm(EditableField.NAME, "Jane Doe")
        advanceUntilIdle()

        assertEquals("Failed to save profile", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `successful save calls updateUserUseCase with correct parameters`() = runTest {
        coEvery { getUserUseCase() } returns fakeUser
        coEvery {
            updateUserUseCase(
                name = "New Name",
                email = fakeUser.email,
                city = fakeUser.city,
                profilePictureUrl = fakeUser.profilePictureUrl
            )
        } returns fakeUser.copy(name = "New Name")

        viewModel.loadProfile()
        advanceUntilIdle()

        viewModel.onEditFieldConfirm(EditableField.NAME, "New Name")
        advanceUntilIdle()

        coVerify(exactly = 1) {
            updateUserUseCase(
                name = "New Name",
                email = fakeUser.email,
                city = fakeUser.city,
                profilePictureUrl = fakeUser.profilePictureUrl
            )
        }
    }
}
