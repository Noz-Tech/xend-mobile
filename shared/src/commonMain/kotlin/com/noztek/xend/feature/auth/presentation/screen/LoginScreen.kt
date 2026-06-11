package com.noztek.xend.feature.auth.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.noztek.xend.core.ui.components.AppButton
import com.noztek.xend.core.ui.components.AppTextField
import com.noztek.xend.feature.auth.presentation.composables.AuthLegalFooter
import com.noztek.xend.feature.auth.presentation.composables.GoogleAuthButton
import com.noztek.xend.feature.auth.presentation.composables.OrDivider
import com.noztek.xend.feature.auth.presentation.composables.rememberAuthSnackbarHostState
import com.noztek.xend.feature.auth.presentation.viewmodel.AuthViewModel
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import xend.shared.generated.resources.Res
import xend.shared.generated.resources.logo

@Composable
fun LoginScreen(
    onForgotPasswordClick: () -> Unit = {},
    onGoogleClick: () -> Unit = {},
    onCreateSpaceClick: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    viewModel: AuthViewModel = koinInject(),
) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val submitLogin = {
        keyboardController?.hide()
        focusManager.clearFocus(force = true)
        viewModel.submitLogin()
    }
    val snackbarHostState = rememberAuthSnackbarHostState(
        message = state.message,
        onMessageConsumed = viewModel::consumeMessage,
    )

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
                text = "Enter your private space",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "Log in to continue your journey whether you're creating your space for the first time or returning to one you already share.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f),
            )
            Spacer(modifier = Modifier.height(28.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AppTextField(
                    value = state.loginEmail,
                    onValueChange = viewModel::onLoginEmailChanged,
                    label = "Email",
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) },
                    ),
                    contentType = ContentType.Username + ContentType.EmailAddress,
                )

                AppTextField(
                    value = state.loginPassword,
                    onValueChange = viewModel::onLoginPasswordChanged,
                    label = "Password",
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { submitLogin() },
                    ),
                    contentType = ContentType.Password,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Forgot Password?",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable(onClick = onForgotPasswordClick),
            )

            Spacer(modifier = Modifier.height(18.dp))
            AppButton(
                text = "Continue",
                onClick = submitLogin,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.isLoginSubmissionEnabled,
                isLoading = state.isLoading,
            )

            Spacer(modifier = Modifier.height(24.dp))
            OrDivider()
            Spacer(modifier = Modifier.height(24.dp))

            GoogleAuthButton(
                onClick = onGoogleClick,
                enabled = !state.isLoading,
            )

            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "New to Xend? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f),
                )
                Text(
                    text = "Create your account",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onCreateSpaceClick),
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(18.dp))
            AuthLegalFooter(
                onTermsClick = onTermsClick,
                onPrivacyClick = onPrivacyClick,
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 20.dp),
        )
    }
}
