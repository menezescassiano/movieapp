package com.example.movieapp.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.movieapp.R
import com.example.movieapp.model.User
import com.example.movieapp.ui.theme.AccentPurple
import com.example.movieapp.ui.theme.AppBackground
import com.example.movieapp.ui.theme.CardDark
import com.example.movieapp.ui.theme.NavUnselected
import java.io.File

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    val cameraImageUri = remember {
        val file = File(context.cacheDir, "profile_photo_${System.currentTimeMillis()}.jpg")
        FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success -> if (success) viewModel.onPhotoTaken(cameraImageUri) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.onImagePicked(it) } }

    ProfileContent(
        uiState = uiState,
        onAvatarClick = viewModel::onAvatarClick,
        onDismissPicker = viewModel::onAvatarPickerDismiss,
        onTakePhoto = { cameraLauncher.launch(cameraImageUri) },
        onPickFromGallery = { galleryLauncher.launch("image/*") },
        onRetry = viewModel::loadProfile
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileContent(
    uiState: ProfileUiState,
    onAvatarClick: () -> Unit,
    onDismissPicker: () -> Unit,
    onTakePhoto: () -> Unit,
    onPickFromGallery: () -> Unit,
    onRetry: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = AccentPurple
                )
            }

            uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.errorMessage,
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRetry) {
                        Text(stringResource(R.string.profile_retry))
                    }
                }
            }

            uiState.user != null -> {
                ProfileLoaded(
                    user = uiState.user,
                    avatarUri = uiState.avatarUri,
                    onAvatarClick = onAvatarClick
                )
            }
        }
    }

    if (uiState.showAvatarPicker) {
        ModalBottomSheet(
            onDismissRequest = onDismissPicker,
            sheetState = sheetState,
            containerColor = CardDark,
            tonalElevation = 0.dp
        ) {
            AvatarPickerSheet(
                onTakePhoto = { onDismissPicker(); onTakePhoto() },
                onPickFromGallery = { onDismissPicker(); onPickFromGallery() }
            )
        }
    }
}

@Composable
private fun ProfileLoaded(
    user: User,
    avatarUri: Uri?,
    onAvatarClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(CardDark)
                .border(2.dp, AccentPurple, CircleShape)
                .clickable { onAvatarClick() },
            contentAlignment = Alignment.Center
        ) {
            if (avatarUri != null) {
                AsyncImage(
                    model = avatarUri,
                    contentDescription = stringResource(R.string.profile_picture_description),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = stringResource(R.string.profile_picture_description),
                    tint = AccentPurple,
                    modifier = Modifier.size(52.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = user.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.profile_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = NavUnselected
        )

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardDark)
                .padding(vertical = 8.dp)
        ) {
            ProfileInfoRow(
                icon = Icons.Default.Person,
                label = stringResource(R.string.profile_label_name),
                value = user.name
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color.White.copy(alpha = 0.06f)
            )
            ProfileInfoRow(
                icon = Icons.Default.Email,
                label = stringResource(R.string.profile_label_email),
                value = user.email
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color.White.copy(alpha = 0.06f)
            )
            ProfileInfoRow(
                icon = Icons.Default.LocationOn,
                label = stringResource(R.string.profile_label_city),
                value = user.city
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun AvatarPickerSheet(
    onTakePhoto: () -> Unit,
    onPickFromGallery: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.profile_avatar_picker_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 20.dp)
        )
        AvatarPickerOption(
            icon = Icons.Default.CameraAlt,
            label = stringResource(R.string.profile_avatar_take_photo),
            onClick = onTakePhoto
        )
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            color = Color.White.copy(alpha = 0.06f)
        )
        AvatarPickerOption(
            icon = Icons.Default.Photo,
            label = stringResource(R.string.profile_avatar_choose_gallery),
            onClick = onPickFromGallery
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun AvatarPickerOption(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AccentPurple.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AccentPurple,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White
        )
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AccentPurple.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AccentPurple,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = NavUnselected
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF12121A)
@Composable
fun ProfileScreenPreview() {
    val previewUser = User(
        id = "1",
        name = "Cassiano Menezes",
        email = "cassianomenezes@gmail.com",
        city = "Porto Alegre"
    )
    ProfileContent(
        uiState = ProfileUiState(isLoading = false, user = previewUser),
        onAvatarClick = {},
        onDismissPicker = {},
        onTakePhoto = {},
        onPickFromGallery = {},
        onRetry = {}
    )
}
