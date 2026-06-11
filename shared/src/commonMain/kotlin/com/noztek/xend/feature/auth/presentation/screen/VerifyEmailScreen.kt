package com.noztek.xend.feature.auth.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.noztek.xend.core.time.currentEpochSeconds
import com.noztek.xend.core.ui.components.AppButton
import com.noztek.xend.core.ui.components.AppTextField
import com.noztek.xend.feature.auth.presentation.composables.rememberAuthSnackbarHostState
import com.noztek.xend.feature.auth.presentation.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import xend.shared.generated.resources.Res
import xend.shared.generated.resources.logo
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun VerifyEmailScreen(
    onChangeEmailClick: () -> Unit = {},
    viewModel: AuthViewModel = koinInject(),
) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val resendRemainingSeconds = rememberVerificationResendRemainingSeconds(
        state.verificationResendAvailableAtEpochSeconds,
    )
    val snackbarHostState = rememberAuthSnackbarHostState(
        message = state.message,
        onMessageConsumed = viewModel::consumeMessage,
    )
    var showChangeEmailConfirmation by remember { mutableStateOf(false) }
    val submitVerification = {
        keyboardController?.hide()
        focusManager.clearFocus(force = true)
        viewModel.submitEmailVerification()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
        ) {
            Image(
                painter = painterResource(Res.drawable.logo),
                contentDescription = "Xend",
                modifier = Modifier.height(28.dp),
            )
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "Unlock your private space",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "We sent a verification code to your email. Enter it below so we know this space starts with you.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f),
            )
            if (state.verificationEmail.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = state.verificationEmail,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    TextButton(
                        onClick = { showChangeEmailConfirmation = true },
                        enabled = !state.isLoading,
                    ) {
                        Text(
                            text = "Change Email",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                textDecoration = TextDecoration.Underline
                            ),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = if (state.isLoading) 0.45f else 0.82f),
                        )
                    }
                }
            }

            AppTextField(
                value = state.verificationCode,
                onValueChange = viewModel::onVerificationCodeChanged,
                label = "Verification code",
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = { submitVerification() },
                ),
                contentType = ContentType.SmsOtpCode,
            )

            Spacer(modifier = Modifier.height(18.dp))
            AppButton(
                text = "Verify",
                onClick = submitVerification,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.isVerificationSubmissionEnabled,
                isLoading = state.isLoading,
            )

            Spacer(modifier = Modifier.height(8.dp))
            val isResendEnabled = !state.isLoading &&
                state.verificationEmail.isNotBlank() &&
                resendRemainingSeconds == 0L
            TextButton(
                onClick = {
                    keyboardController?.hide()
                    focusManager.clearFocus(force = true)
                    viewModel.resendCode(state.verificationEmail)
                },
                enabled = isResendEnabled,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Text(
                    text = if (resendRemainingSeconds > 0L) {
                        "Resend in ${resendRemainingSeconds}s"
                    } else {
                        "Resend code"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = if (isResendEnabled) 0.82f else 0.45f),
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Check your inbox or spam folder. The code may take a few moments to arrive.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.58f),
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 20.dp),
        )
    }

    if (showChangeEmailConfirmation) {
        AlertDialog(
            onDismissRequest = { showChangeEmailConfirmation = false },
            title = {
                Text(
                    text = "Change email?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                )
            },
            text = {
                Text(
                    text = "This will clear your current verification progress and take you back to registration.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showChangeEmailConfirmation = false
                        keyboardController?.hide()
                        focusManager.clearFocus(force = true)
                        viewModel.changeVerificationEmail()
                        onChangeEmailClick()
                    },
                ) {
                    Text(
                        text = "Change Email",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showChangeEmailConfirmation = false },
                ) {
                    Text(
                        text = "Cancel",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    )
                }
            },
        )

    }
}

@Composable
private fun rememberVerificationResendRemainingSeconds(
    availableAtEpochSeconds: Long,
): Long {
    var remainingSeconds by remember(availableAtEpochSeconds) {
        mutableLongStateOf((availableAtEpochSeconds - currentEpochSeconds()).coerceAtLeast(0L))
    }

    LaunchedEffect(availableAtEpochSeconds) {
        while (true) {
            val nextRemaining = (availableAtEpochSeconds - currentEpochSeconds()).coerceAtLeast(0L)
            remainingSeconds = nextRemaining
            if (nextRemaining <= 0L) break
            delay(1_000L.milliseconds)
        }
    }

    return remainingSeconds
}
