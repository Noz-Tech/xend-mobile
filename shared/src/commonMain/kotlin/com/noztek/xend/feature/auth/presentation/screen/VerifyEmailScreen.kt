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
import androidx.compose.ui.unit.dp
import com.noztek.xend.core.ui.components.AppButton
import com.noztek.xend.core.ui.components.AppOutlinedButton
import com.noztek.xend.core.ui.components.AppTextField
import com.noztek.xend.feature.auth.presentation.viewmodel.AuthViewModel
import org.koin.compose.koinInject

@Composable
fun VerifyEmailScreen(
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
            text = "Verify your email",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Text(
            text = state.verificationEmail,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        AppTextField(
            value = state.verificationCode,
            onValueChange = viewModel::onVerificationCodeChanged,
            label = "Verification code",
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
        )

        AppButton(
            text = "Verify",
            onClick = viewModel::submitEmailVerification,
            modifier = Modifier.fillMaxWidth(),
            enabled = state.isVerificationSubmissionEnabled,
            isLoading = state.isLoading,
        )

        AppOutlinedButton(
            text = "Resend code",
            onClick = { viewModel.resendCode(state.verificationEmail) },
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
