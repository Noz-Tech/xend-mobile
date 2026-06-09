package com.noztek.xend.feature.auth.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.noztek.xend.core.ui.components.AppButton
import com.noztek.xend.core.ui.components.AppTextField
import com.noztek.xend.feature.auth.presentation.viewmodel.AuthViewModel
import org.koin.compose.koinInject

@Composable
fun LoginScreen(
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
            text = "Login",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )

        AppTextField(
            value = state.loginEmail,
            onValueChange = viewModel::onLoginEmailChanged,
            label = "Email",
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Email,
            ),
        )

        AppTextField(
            value = state.loginPassword,
            onValueChange = viewModel::onLoginPasswordChanged,
            label = "Password",
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Password,
            ),
        )

        AppButton(
            text = "Login",
            onClick = viewModel::submitLogin,
            modifier = Modifier.fillMaxWidth(),
            enabled = state.isLoginSubmissionEnabled,
            isLoading = state.isLoading,
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
