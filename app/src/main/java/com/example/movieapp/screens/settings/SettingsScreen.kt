package com.example.movieapp.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Recommend
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.movieapp.R
import com.example.movieapp.core.ui.components.text.BodyText
import com.example.movieapp.core.ui.components.text.TitleText
import com.example.movieapp.core.ui.theme.AccentPurple
import com.example.movieapp.core.ui.theme.AppBackground
import com.example.movieapp.core.ui.theme.CardDark
import com.example.movieapp.core.ui.theme.NavUnselected

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
) {
    var uiState by remember { mutableStateOf(SettingsUiState()) }

    SettingsContent(
        modifier = modifier,
        uiState = uiState,
        onBack = onBack,
        onToggleNotifications = {
            uiState = uiState.copy(notificationsEnabled = !uiState.notificationsEnabled)
        },
        onToggleNewReleases = {
            uiState = uiState.copy(newReleasesEnabled = !uiState.newReleasesEnabled)
        },
        onToggleRecommendations = {
            uiState = uiState.copy(recommendationsEnabled = !uiState.recommendationsEnabled)
        },
        onToggleBiometric = {
            uiState = uiState.copy(biometricEnabled = !uiState.biometricEnabled)
        },
        onToggleHideWatched = {
            uiState = uiState.copy(hideWatchedMovies = !uiState.hideWatchedMovies)
        },
        onLanguageSelected = { language ->
            uiState = uiState.copy(contentLanguage = language)
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    modifier: Modifier = Modifier,
    uiState: SettingsUiState,
    onBack: () -> Unit,
    onToggleNotifications: () -> Unit,
    onToggleNewReleases: () -> Unit,
    onToggleRecommendations: () -> Unit,
    onToggleBiometric: () -> Unit,
    onToggleHideWatched: () -> Unit,
    onLanguageSelected: (ContentLanguage) -> Unit,
) {
    var showLanguagePicker by remember { mutableStateOf(false) }
    val languageSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier =
            modifier
                .background(AppBackground)
                .windowInsetsPadding(WindowInsets.statusBars)
                .verticalScroll(rememberScrollState()),
    ) {
        // ── Top bar ──────────────────────────────────────────────────────────
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            TitleText(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.titleLarge,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Notifications ────────────────────────────────────────────────────
        SettingsSectionHeader(title = stringResource(R.string.settings_section_notifications))

        SettingsGroup {
            SettingsToggleRow(
                icon = Icons.Filled.Notifications,
                title = stringResource(R.string.settings_notifications_enable),
                subtitle = stringResource(R.string.settings_notifications_enable_subtitle),
                checked = uiState.notificationsEnabled,
                onToggle = onToggleNotifications,
            )
            SettingsDivider()
            SettingsToggleRow(
                icon = Icons.Outlined.Notifications,
                title = stringResource(R.string.settings_notifications_new_releases),
                subtitle = stringResource(R.string.settings_notifications_new_releases_subtitle),
                checked = uiState.newReleasesEnabled,
                enabled = uiState.notificationsEnabled,
                onToggle = onToggleNewReleases,
            )
            SettingsDivider()
            SettingsToggleRow(
                icon = Icons.Filled.Recommend,
                title = stringResource(R.string.settings_notifications_recommendations),
                subtitle = stringResource(R.string.settings_notifications_recommendations_subtitle),
                checked = uiState.recommendationsEnabled,
                enabled = uiState.notificationsEnabled,
                onToggle = onToggleRecommendations,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Biometrics ───────────────────────────────────────────────────────
        SettingsSectionHeader(title = stringResource(R.string.settings_section_biometrics))

        SettingsGroup {
            SettingsToggleRow(
                icon = Icons.Filled.Fingerprint,
                title = stringResource(R.string.settings_biometric_enable),
                subtitle = stringResource(R.string.settings_biometric_enable_subtitle),
                checked = uiState.biometricEnabled,
                onToggle = onToggleBiometric,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Content ──────────────────────────────────────────────────────────
        SettingsSectionHeader(title = stringResource(R.string.settings_section_content))

        SettingsGroup {
            SettingsNavigationRow(
                icon = Icons.Filled.Language,
                title = stringResource(R.string.settings_content_language),
                value = uiState.contentLanguage.label,
                onClick = { showLanguagePicker = true },
            )
            SettingsDivider()
            SettingsToggleRow(
                icon = Icons.Filled.VisibilityOff,
                title = stringResource(R.string.settings_content_hide_watched),
                subtitle = stringResource(R.string.settings_content_hide_watched_subtitle),
                checked = uiState.hideWatchedMovies,
                onToggle = onToggleHideWatched,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── About ────────────────────────────────────────────────────────────
        SettingsSectionHeader(title = stringResource(R.string.settings_section_about))

        SettingsGroup {
            SettingsInfoRow(
                icon = Icons.Filled.Info,
                title = stringResource(R.string.settings_about_version),
                value = "1.0.0",
            )
            SettingsDivider()
            SettingsLinkRow(
                icon = Icons.Filled.Policy,
                title = stringResource(R.string.settings_about_privacy),
            )
            SettingsDivider()
            SettingsLinkRow(
                icon = Icons.Filled.Star,
                title = stringResource(R.string.settings_about_rate),
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // ── Language picker sheet ────────────────────────────────────────────────
    if (showLanguagePicker) {
        ModalBottomSheet(
            onDismissRequest = { showLanguagePicker = false },
            sheetState = languageSheetState,
            containerColor = CardDark,
            tonalElevation = 0.dp,
        ) {
            LanguagePickerSheet(
                currentLanguage = uiState.contentLanguage,
                onLanguageSelected = { language ->
                    onLanguageSelected(language)
                    showLanguagePicker = false
                },
            )
        }
    }
}

// ── Section header ────────────────────────────────────────────────────────────

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = NavUnselected,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
    )
}

// ── Card group wrapper ────────────────────────────────────────────────────────

@Composable
private fun SettingsGroup(content: @Composable () -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(CardDark)
                .padding(vertical = 4.dp),
    ) {
        content()
    }
}

// ── Divider ───────────────────────────────────────────────────────────────────

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = Color.White.copy(alpha = 0.06f),
    )
}

// ── Row types ─────────────────────────────────────────────────────────────────

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    enabled: Boolean = true,
    onToggle: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { onToggle() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsIcon(icon = icon, enabled = enabled)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            BodyText(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) Color.White else NavUnselected,
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                BodyText(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = NavUnselected.copy(alpha = if (enabled) 1f else 0.5f),
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = { if (enabled) onToggle() },
            enabled = enabled,
            colors =
                SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = AccentPurple,
                    uncheckedThumbColor = NavUnselected,
                    uncheckedTrackColor = NavUnselected.copy(alpha = 0.3f),
                    disabledCheckedTrackColor = AccentPurple.copy(alpha = 0.3f),
                    disabledUncheckedTrackColor = NavUnselected.copy(alpha = 0.15f),
                ),
        )
    }
}

@Composable
private fun SettingsNavigationRow(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsIcon(icon = icon)
        Spacer(modifier = Modifier.width(12.dp))
        BodyText(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            modifier = Modifier.weight(1f),
        )
        BodyText(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = AccentPurple,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = null,
            tint = NavUnselected,
            modifier = Modifier.size(14.dp),
        )
    }
}

@Composable
private fun SettingsInfoRow(
    icon: ImageVector,
    title: String,
    value: String,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsIcon(icon = icon)
        Spacer(modifier = Modifier.width(12.dp))
        BodyText(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            modifier = Modifier.weight(1f),
        )
        BodyText(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = NavUnselected,
        )
    }
}

@Composable
private fun SettingsLinkRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit = {},
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsIcon(icon = icon)
        Spacer(modifier = Modifier.width(12.dp))
        BodyText(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = null,
            tint = NavUnselected,
            modifier = Modifier.size(16.dp),
        )
    }
}

// ── Shared icon box ───────────────────────────────────────────────────────────

@Composable
private fun SettingsIcon(
    icon: ImageVector,
    enabled: Boolean = true,
) {
    Box(
        modifier =
            Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AccentPurple.copy(alpha = if (enabled) 0.15f else 0.07f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentPurple.copy(alpha = if (enabled) 1f else 0.4f),
            modifier = Modifier.size(18.dp),
        )
    }
}

// ── Language picker ───────────────────────────────────────────────────────────

@Composable
private fun LanguagePickerSheet(
    currentLanguage: ContentLanguage,
    onLanguageSelected: (ContentLanguage) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
    ) {
        TitleText(
            text = stringResource(R.string.settings_language_picker_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        ContentLanguage.entries.forEach { language ->
            val isSelected = language == currentLanguage
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) AccentPurple.copy(alpha = 0.12f) else Color.Transparent)
                        .clickable { onLanguageSelected(language) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                BodyText(
                    text = language.label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSelected) AccentPurple else Color.White,
                )
                if (isSelected) {
                    Box(
                        modifier =
                            Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(AccentPurple),
                    )
                }
            }
            if (language != ContentLanguage.entries.last()) {
                HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF12121A)
@Composable
private fun SettingsScreenPreview() {
    SettingsContent(
        uiState = SettingsUiState(),
        onBack = {},
        onToggleNotifications = {},
        onToggleNewReleases = {},
        onToggleRecommendations = {},
        onToggleBiometric = {},
        onToggleHideWatched = {},
        onLanguageSelected = {},
    )
}
