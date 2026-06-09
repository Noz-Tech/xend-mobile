package com.noztek.xend.feature.auth.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.noztek.xend.core.ui.components.AppButton
import com.noztek.xend.core.ui.components.AppOutlinedButton
import com.noztek.xend.core.ui.components.AppTextField
import com.noztek.xend.feature.auth.presentation.viewmodel.AuthViewModel
import org.koin.compose.koinInject

@Composable
fun RegisterScreen(
    onLoginClick: (email: String) -> Unit = {},
    viewModel: AuthViewModel = koinInject(),
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "Create account",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )

        AppTextField(
            value = state.registerDisplayName,
            onValueChange = viewModel::onRegisterDisplayNameChanged,
            label = "Display name",
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
        )

        AppTextField(
            value = state.registerEmail,
            onValueChange = viewModel::onRegisterEmailChanged,
            label = "Email",
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                keyboardType = KeyboardType.Email,
            ),
        )

        AppTextField(
            value = state.registerPassword,
            onValueChange = viewModel::onRegisterPasswordChanged,
            label = "Password",
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                keyboardType = KeyboardType.Password,
            ),
        )

        AppButton(
            text = "Register",
            onClick = viewModel::submitRegistration,
            modifier = Modifier.fillMaxWidth(),
            enabled = state.isRegisterSubmissionEnabled,
            isLoading = state.isLoading,
        )

        AppOutlinedButton(
            text = "Login",
            onClick = { onLoginClick(state.registerEmail.trim()) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
        )

        state.message?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}
