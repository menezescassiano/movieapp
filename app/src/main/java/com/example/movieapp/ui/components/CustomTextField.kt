package com.example.movieapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.movieapp.ui.theme.AccentPurple
import com.example.movieapp.ui.theme.AppBackground
import com.example.movieapp.ui.theme.NavUnselected

@Composable
fun CustomTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = NavUnselected,
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(text = placeholder, color = NavUnselected)
            },
            leadingIcon =
                leadingIcon?.let {
                    {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            tint =
                                if (errorMessage != null) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    AccentPurple
                                },
                        )
                    }
                },
            trailingIcon =
                trailingIcon?.let {
                    {
                        IconButton(onClick = { onTrailingIconClick?.invoke() }) {
                            Icon(
                                imageVector = it,
                                contentDescription = null,
                                tint = NavUnselected,
                            )
                        }
                    }
                },
            visualTransformation = visualTransformation,
            isError = errorMessage != null,
            supportingText =
                errorMessage?.let { msg ->
                    { Text(text = msg, color = MaterialTheme.colorScheme.error) }
                },
            singleLine = true,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            shape = RoundedCornerShape(12.dp),
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentPurple,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = AccentPurple,
                    focusedContainerColor = AppBackground.copy(alpha = 0.6f),
                    unfocusedContainerColor = AppBackground.copy(alpha = 0.6f),
                ),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF12121A)
@Composable
private fun CustomTextFieldPreview() {
    CustomTextField(
        label = "Email",
        value = "",
        onValueChange = {},
        placeholder = "Enter your email",
        leadingIcon = Icons.Default.Email,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF12121A, name = "With error")
@Composable
private fun CustomTextFieldErrorPreview() {
    CustomTextField(
        label = "Email",
        value = "invalid",
        onValueChange = {},
        placeholder = "Enter your email",
        leadingIcon = Icons.Default.Email,
        errorMessage = "Enter a valid email",
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF12121A, name = "Password")
@Composable
private fun CustomTextFieldPasswordPreview() {
    CustomTextField(
        label = "Password",
        value = "secret",
        onValueChange = {},
        placeholder = "Enter your password",
        trailingIcon = Icons.Default.Visibility,
        onTrailingIconClick = {},
    )
}
