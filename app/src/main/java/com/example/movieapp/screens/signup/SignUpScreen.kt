package com.example.movieapp.screens.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.movieapp.R
import com.example.movieapp.data.AuthException
import com.example.movieapp.ui.components.CustomButton
import com.example.movieapp.ui.components.CustomTextField
import com.example.movieapp.ui.components.LinkButton
import com.example.movieapp.ui.components.text.BodyText
import com.example.movieapp.ui.components.text.TitleText
import com.example.movieapp.ui.theme.AccentPurple
import com.example.movieapp.ui.theme.AppBackground
import com.example.movieapp.ui.theme.CardDark
import com.example.movieapp.ui.theme.NavUnselected

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    viewModel: SignUpViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.signUpSuccess) {
        onSignUpSuccess()
        return
    }

    val emptyNameMsg = stringResource(R.string.signup_error_empty_name)
    val invalidEmailMsg = stringResource(R.string.signup_error_invalid_email)
    val shortPasswordMsg = stringResource(R.string.signup_error_short_password)
    val passwordMismatchMsg = stringResource(R.string.signup_error_password_mismatch)
    val emailInUseMsg = stringResource(R.string.signup_error_email_in_use)
    val noNetworkMsg = stringResource(R.string.login_error_no_network)
    val genericErrorMsg = stringResource(R.string.signup_error_generic)

    fun mapError(raw: String?): String? =
        when (raw) {
            AuthException.EmailAlreadyInUse.message -> emailInUseMsg
            AuthException.NoNetwork.message -> noNetworkMsg
            null -> null
            else -> genericErrorMsg
        }

    SignUpContent(
        uiState = uiState,
        errorMessage = mapError(uiState.errorMessage),
        onNameChange = viewModel::onNameChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
        onToggleConfirmPasswordVisibility = viewModel::onToggleConfirmPasswordVisibility,
        onCreateAccountClick = {
            viewModel.onSignUpClick(
                emptyNameMsg,
                invalidEmailMsg,
                shortPasswordMsg,
                passwordMismatchMsg,
            )
        },
        onLoginClick = onLoginClick,
        onErrorDismissed = viewModel::onErrorDismissed,
    )
}

@Composable
private fun SignUpContent(
    uiState: SignUpUiState,
    errorMessage: String?,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onToggleConfirmPasswordVisibility: () -> Unit,
    onCreateAccountClick: () -> Unit,
    onLoginClick: () -> Unit,
    onErrorDismissed: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
            onErrorDismissed()
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(AppBackground),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .imePadding()
                    .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // ── Header ───────────────────────────────────────────────────
            TitleText(
                text = stringResource(R.string.signup_title),
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            BodyText(
                text = stringResource(R.string.signup_subtitle),
                color = NavUnselected,
            )

            Spacer(modifier = Modifier.height(40.dp))

            // ── Fields card ──────────────────────────────────────────────
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(CardDark, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Name
                CustomTextField(
                    label = stringResource(R.string.signup_label_name),
                    value = uiState.name,
                    onValueChange = onNameChange,
                    placeholder = stringResource(R.string.signup_placeholder_name),
                    leadingIcon = Icons.Default.Person,
                    errorMessage = uiState.nameError,
                    keyboardOptions =
                        KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next,
                        ),
                    keyboardActions =
                        KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) },
                        ),
                )

                // Email
                CustomTextField(
                    label = stringResource(R.string.signup_label_email),
                    value = uiState.email,
                    onValueChange = onEmailChange,
                    placeholder = stringResource(R.string.signup_placeholder_email),
                    leadingIcon = Icons.Default.Email,
                    errorMessage = uiState.emailError,
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                        ),
                    keyboardActions =
                        KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) },
                        ),
                )

                // Password
                CustomTextField(
                    label = stringResource(R.string.signup_label_password),
                    value = uiState.password,
                    onValueChange = onPasswordChange,
                    placeholder = stringResource(R.string.signup_placeholder_password),
                    leadingIcon = Icons.Default.Lock,
                    trailingIcon =
                        if (uiState.passwordVisible) {
                            Icons.Default.VisibilityOff
                        } else {
                            Icons.Default.Visibility
                        },
                    onTrailingIconClick = onTogglePasswordVisibility,
                    visualTransformation =
                        if (uiState.passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                    errorMessage = uiState.passwordError,
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next,
                        ),
                    keyboardActions =
                        KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) },
                        ),
                )

                // Confirm password
                CustomTextField(
                    label = stringResource(R.string.signup_label_confirm_password),
                    value = uiState.confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    placeholder = stringResource(R.string.signup_placeholder_confirm_password),
                    leadingIcon = Icons.Default.Lock,
                    trailingIcon =
                        if (uiState.confirmPasswordVisible) {
                            Icons.Default.VisibilityOff
                        } else {
                            Icons.Default.Visibility
                        },
                    onTrailingIconClick = onToggleConfirmPasswordVisibility,
                    visualTransformation =
                        if (uiState.confirmPasswordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                    errorMessage = uiState.confirmPasswordError,
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                    keyboardActions =
                        KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                onCreateAccountClick()
                            },
                        ),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Create account button ────────────────────────────────────
            CustomButton(
                text = stringResource(R.string.signup_button_create),
                onClick = onCreateAccountClick,
                modifier = Modifier.fillMaxWidth(),
                isLoading = uiState.isLoading,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Already have an account ──────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BodyText(text = stringResource(R.string.signup_login_prompt))
                LinkButton(
                    text = stringResource(R.string.signup_login_action),
                    onClick = onLoginClick,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // ── Snackbar ─────────────────────────────────────────────────────
        SnackbarHost(
            hostState = snackbarHostState,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = CardDark,
                contentColor = Color.White,
                actionColor = AccentPurple,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF12121A)
@Composable
private fun SignUpScreenPreview() {
    SignUpContent(
        uiState = SignUpUiState(),
        errorMessage = null,
        onNameChange = {},
        onEmailChange = {},
        onPasswordChange = {},
        onConfirmPasswordChange = {},
        onTogglePasswordVisibility = {},
        onToggleConfirmPasswordVisibility = {},
        onCreateAccountClick = {},
        onLoginClick = {},
        onErrorDismissed = {},
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF12121A, name = "With errors")
@Composable
private fun SignUpScreenErrorPreview() {
    SignUpContent(
        uiState =
            SignUpUiState(
                name = "",
                nameError = "Name cannot be empty",
                email = "invalid",
                emailError = "Enter a valid email",
                password = "123",
                passwordError = "Password must be at least 8 characters",
                confirmPassword = "456",
                confirmPasswordError = "Passwords do not match",
            ),
        errorMessage = null,
        onNameChange = {},
        onEmailChange = {},
        onPasswordChange = {},
        onConfirmPasswordChange = {},
        onTogglePasswordVisibility = {},
        onToggleConfirmPasswordVisibility = {},
        onCreateAccountClick = {},
        onLoginClick = {},
        onErrorDismissed = {},
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF12121A, name = "Loading")
@Composable
private fun SignUpScreenLoadingPreview() {
    SignUpContent(
        uiState =
            SignUpUiState(
                name = "Cassiano Menezes",
                email = "cassiano@email.com",
                password = "secret",
                confirmPassword = "secret",
                isLoading = true,
            ),
        errorMessage = null,
        onNameChange = {},
        onEmailChange = {},
        onPasswordChange = {},
        onConfirmPasswordChange = {},
        onTogglePasswordVisibility = {},
        onToggleConfirmPasswordVisibility = {},
        onCreateAccountClick = {},
        onLoginClick = {},
        onErrorDismissed = {},
    )
}
