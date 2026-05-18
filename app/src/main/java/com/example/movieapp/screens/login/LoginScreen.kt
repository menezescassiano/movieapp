package com.example.movieapp.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.movieapp.R
import com.example.movieapp.data.AuthException
import com.example.movieapp.ui.theme.AccentPurple
import com.example.movieapp.ui.theme.AppBackground
import com.example.movieapp.ui.theme.CardDark
import com.example.movieapp.ui.theme.NavUnselected

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.loginSuccess) {
        onLoginSuccess()
        return
    }

    val invalidEmailMsg   = stringResource(R.string.login_error_invalid_email)
    val emptyPasswordMsg  = stringResource(R.string.login_error_empty_password)
    val invalidCredsMsg   = stringResource(R.string.login_error_invalid_credentials)
    val noNetworkMsg      = stringResource(R.string.login_error_no_network)
    val genericErrorMsg   = stringResource(R.string.login_error_generic)

    fun mapError(raw: String?): String? = when (raw) {
        AuthException.InvalidCredentials.message -> invalidCredsMsg
        AuthException.NoNetwork.message          -> noNetworkMsg
        null                                     -> null
        else                                     -> genericErrorMsg
    }

    LoginContent(
        uiState = uiState,
        errorMessage = mapError(uiState.errorMessage),
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
        onContinueClick = {
            viewModel.onContinueClick(invalidEmailMsg, emptyPasswordMsg)
        },
        onForgotPasswordClick = viewModel::onForgotPasswordClick,
        onErrorDismissed = viewModel::onErrorDismissed
    )
}

@Composable
private fun LoginContent(
    uiState: LoginUiState,
    errorMessage: String?,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onContinueClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onErrorDismissed: () -> Unit
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
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.statusBars)
                .imePadding()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // ── Header ───────────────────────────────────────────────────
            Text(
                text = stringResource(R.string.login_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.login_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = NavUnselected
            )

            Spacer(modifier = Modifier.height(40.dp))

            // ── Fields card ──────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardDark, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Email
                Column {
                    Text(
                        text = stringResource(R.string.login_label_email),
                        style = MaterialTheme.typography.labelSmall,
                        color = NavUnselected
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = onEmailChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                stringResource(R.string.login_email_placeholder),
                                color = NavUnselected
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                tint = if (uiState.emailError != null) MaterialTheme.colorScheme.error
                                else AccentPurple
                            )
                        },
                        isError = uiState.emailError != null,
                        supportingText = uiState.emailError?.let { msg ->
                            { Text(msg, color = MaterialTheme.colorScheme.error) }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors()
                    )
                }

                // Password
                Column {
                    Text(
                        text = stringResource(R.string.login_label_password),
                        style = MaterialTheme.typography.labelSmall,
                        color = NavUnselected
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = onPasswordChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                stringResource(R.string.login_password_placeholder),
                                color = NavUnselected
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (uiState.passwordError != null) MaterialTheme.colorScheme.error
                                else AccentPurple
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = onTogglePasswordVisibility) {
                                Icon(
                                    imageVector = if (uiState.passwordVisible) Icons.Default.VisibilityOff
                                    else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = NavUnselected
                                )
                            }
                        },
                        visualTransformation = if (uiState.passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        isError = uiState.passwordError != null,
                        supportingText = uiState.passwordError?.let { msg ->
                            { Text(msg, color = MaterialTheme.colorScheme.error) }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                onContinueClick()
                            }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Continue button ──────────────────────────────────────────
            Button(
                onClick = onContinueClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.height(20.dp)
                    )
                } else {
                    Text(
                        text = stringResource(R.string.login_button_continue),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Forgot password ──────────────────────────────────────────
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TextButton(onClick = onForgotPasswordClick) {
                    Text(
                        text = stringResource(R.string.login_button_forgot_password),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AccentPurple
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // ── Snackbar ─────────────────────────────────────────────────────
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = CardDark,
                contentColor = Color.White,
                actionColor = AccentPurple
            )
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AccentPurple,
    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = AccentPurple,
    focusedContainerColor = AppBackground.copy(alpha = 0.6f),
    unfocusedContainerColor = AppBackground.copy(alpha = 0.6f)
)

@Preview(showBackground = true, backgroundColor = 0xFF12121A)
@Composable
private fun LoginScreenPreview() {
    LoginContent(
        uiState = LoginUiState(),
        errorMessage = null,
        onEmailChange = {},
        onPasswordChange = {},
        onTogglePasswordVisibility = {},
        onContinueClick = {},
        onForgotPasswordClick = {},
        onErrorDismissed = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF12121A, name = "With errors")
@Composable
private fun LoginScreenErrorPreview() {
    LoginContent(
        uiState = LoginUiState(
            email = "invalid",
            emailError = "Enter a valid email",
            password = "",
            passwordError = "Password cannot be empty"
        ),
        errorMessage = null,
        onEmailChange = {},
        onPasswordChange = {},
        onTogglePasswordVisibility = {},
        onContinueClick = {},
        onForgotPasswordClick = {},
        onErrorDismissed = {}
    )
}
