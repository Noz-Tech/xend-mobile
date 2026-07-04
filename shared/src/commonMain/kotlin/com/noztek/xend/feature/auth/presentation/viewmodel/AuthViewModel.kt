package com.noztek.xend.feature.auth.presentation.viewmodel

import com.noztek.xend.core.presentation.defaultViewModelScope
import com.noztek.xend.core.utils.capitalizeWords
import com.noztek.xend.core.time.currentEpochSeconds
import com.noztek.xend.currentDeviceName
import com.noztek.xend.feature.auth.data.remote.AuthApiException
import com.noztek.xend.feature.auth.domain.model.LoginParams
import com.noztek.xend.feature.auth.domain.usecase.CompleteLoginSessionUseCase
import com.noztek.xend.feature.auth.domain.usecase.CompleteLogoutSessionUseCase
import com.noztek.xend.feature.auth.domain.usecase.ClearPendingAuthFlowUseCase
import com.noztek.xend.feature.auth.domain.usecase.GetCurrentSessionUseCase
import com.noztek.xend.feature.auth.domain.usecase.GetCurrentUserProfileUseCase
import com.noztek.xend.feature.auth.domain.usecase.LoginUseCase
import com.noztek.xend.feature.auth.domain.usecase.LogoutUseCase
import com.noztek.xend.feature.auth.domain.usecase.RefreshSessionUseCase
import com.noztek.xend.feature.auth.domain.usecase.RegisterWithEmailUseCase
import com.noztek.xend.feature.auth.domain.usecase.ResendVerificationCodeUseCase
import com.noztek.xend.feature.auth.domain.usecase.SavePendingEmailVerificationUseCase
import com.noztek.xend.feature.auth.domain.usecase.VerifyEmailCodeUseCase
import com.noztek.xend.feature.auth.presentation.state.AuthUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val VERIFICATION_RESEND_COOLDOWN_SECONDS = 25L

class AuthViewModel(
    private val registerWithEmailAction: RegisterWithEmailUseCase,
    private val verifyEmailCodeAction: VerifyEmailCodeUseCase,
    private val resendVerificationCodeAction: ResendVerificationCodeUseCase,
    private val savePendingEmailVerification: SavePendingEmailVerificationUseCase,
    private val clearPendingAuthFlow: ClearPendingAuthFlowUseCase,
    private val login: LoginUseCase,
    private val refreshSession: RefreshSessionUseCase,
    private val completeLoginSession: CompleteLoginSessionUseCase,
    private val logout: LogoutUseCase,
    private val completeLogoutSession: CompleteLogoutSessionUseCase,
    private val getCurrentSession: GetCurrentSessionUseCase,
    private val getCurrentProfile: GetCurrentUserProfileUseCase,
) {
    private val scope = defaultViewModelScope()
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    init {
        scope.launch {
            val session = getCurrentSession()
            val profile = getCurrentProfile()
            _state.update { it.copy(session = session, profile = profile) }
        }
    }

    fun onRegisterDisplayNameChanged(value: String) {
        _state.update { it.copy(registerDisplayName = value.capitalizeWords(), message = null, existingAccountEmail = null) }
    }

    fun onRegisterEmailChanged(value: String) {
        _state.update { it.copy(registerEmail = value, message = null, existingAccountEmail = null) }
    }

    fun onRegisterPasswordChanged(value: String) {
        _state.update { it.copy(registerPassword = value, message = null, existingAccountEmail = null) }
    }

    fun prepareVerification(
        email: String,
        resendAvailableAtEpochSeconds: Long = 0,
        startResendCooldown: Boolean = true,
    ) {
        val normalizedEmail = email.trim()
        val resolvedResendAvailableAtEpochSeconds = when {
            startResendCooldown -> currentEpochSeconds() + VERIFICATION_RESEND_COOLDOWN_SECONDS
            resendAvailableAtEpochSeconds > 0L -> resendAvailableAtEpochSeconds
            else -> _state.value.verificationResendAvailableAtEpochSeconds
        }
        _state.update {
            it.copy(
                verificationEmail = normalizedEmail,
                verificationCode = "",
                verificationResendAvailableAtEpochSeconds = resolvedResendAvailableAtEpochSeconds,
                loginEmail = normalizedEmail.ifBlank { it.loginEmail },
                message = null,
            )
        }
    }

    fun onVerificationCodeChanged(value: String) {
        _state.update { it.copy(verificationCode = value.uppercase(), message = null) }
    }

    fun submitEmailVerification() {
        val current = _state.value
        val email = current.verificationEmail.trim()
        val code = current.verificationCode.trim()

        if (email.isBlank()) {
            _state.update { it.copy(message = "Email is required") }
            return
        }

        if (code.isBlank()) {
            _state.update { it.copy(message = "Verification code is required") }
            return
        }

        verifyEmailCode(email, code)
    }

    fun prepareLogin(email: String) {
        _state.update {
            it.copy(
                loginEmail = email.trim(),
                loginPassword = "",
                message = null,
            )
        }
    }

    fun onLoginEmailChanged(value: String) {
        _state.update { it.copy(loginEmail = value, message = null) }
    }

    fun onLoginPasswordChanged(value: String) {
        _state.update { it.copy(loginPassword = value, message = null) }
    }

    fun submitLogin() {
        val current = _state.value
        val email = current.loginEmail.trim()
        val password = current.loginPassword

        if (email.isBlank()) {
            _state.update { it.copy(message = "Email is required") }
            return
        }

        if (password.isBlank()) {
            _state.update { it.copy(message = "Password is required") }
            return
        }

        loginWithEmail(email = email, password = password)
    }

    fun submitRegistration() {
        val current = _state.value
        val displayName = current.registerDisplayName.trim()
        val email = current.registerEmail.trim()
        val password = current.registerPassword

        when {
            displayName.isBlank() -> {
                _state.update { it.copy(message = "Display name is required") }
                return
            }

            email.isBlank() -> {
                _state.update { it.copy(message = "Email is required") }
                return
            }

            password.length < 8 -> {
                _state.update { it.copy(message = "Password must be at least 8 characters") }
                return
            }
        }

        scope.launch {
            _state.update { it.copy(isLoading = true, message = null, existingAccountEmail = null) }
            runCatching {
                val resendAvailableAtEpochSeconds = currentEpochSeconds() + VERIFICATION_RESEND_COOLDOWN_SECONDS
                val registeredEmail = registerWithEmailAction(
                    displayName = displayName,
                    email = email,
                    password = password,
                    deviceName = currentDeviceName(),
                )
                savePendingEmailVerification(
                    email = registeredEmail,
                    resendAvailableAtEpochSeconds = resendAvailableAtEpochSeconds,
                )
                registeredEmail to resendAvailableAtEpochSeconds
            }.onSuccess { (registeredEmail, resendAvailableAtEpochSeconds) ->
                val session = getCurrentSession()
                val profile = getCurrentProfile()
                _state.update {
                    it.copy(
                        isLoading = false,
                        session = session ?: it.session,
                        profile = profile ?: it.profile,
                        message = "Registration completed. Verify your email.",
                        registerDisplayName = displayName,
                        registerEmail = registeredEmail,
                        registerPassword = "",
                        loginEmail = registeredEmail,
                        verificationEmail = registeredEmail,
                        verificationResendAvailableAtEpochSeconds = resendAvailableAtEpochSeconds,
                        registeredEmail = registeredEmail,
                    )
                }
            }.onFailure { error ->
                val apiError = error as? AuthApiException
                if (apiError?.code == "email_exists") {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            message = "Email already registered. Please log in.",
                            loginEmail = email.lowercase(),
                            existingAccountEmail = email.lowercase(),
                        )
                    }
                } else {
                    _state.update { it.copy(isLoading = false, message = error.message ?: "Request failed") }
                }
            }
        }
    }

    fun verifyEmailCode(email: String, token: String) {
        scope.launch {
            _state.update { it.copy(isLoading = true, message = null, emailVerified = false) }
            runCatching { verifyEmailCodeAction(email, token) }
                .onSuccess {
                    clearPendingAuthFlow()
                    _state.update { it.copy(isLoading = false, message = "Email verified.", emailVerified = true) }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, message = e.message ?: "Request failed", emailVerified = false) }
                }
        }
    }

    fun resendCode(email: String) {
        val normalizedEmail = email.trim()
        if (normalizedEmail.isBlank()) {
            _state.update { it.copy(message = "Email is required") }
            return
        }

        val remainingSeconds =
            (_state.value.verificationResendAvailableAtEpochSeconds - currentEpochSeconds()).coerceAtLeast(0L)
        if (remainingSeconds > 0L) {
            _state.update {
                it.copy(message = "Please wait ${remainingSeconds}s before requesting another code.")
            }
            return
        }

        scope.launch {
            _state.update { it.copy(isLoading = true, message = null) }
            runCatching { resendVerificationCodeAction(normalizedEmail) }
                .onSuccess {
                    val resendAvailableAtEpochSeconds =
                        currentEpochSeconds() + VERIFICATION_RESEND_COOLDOWN_SECONDS
                    savePendingEmailVerification(
                        email = normalizedEmail,
                        resendAvailableAtEpochSeconds = resendAvailableAtEpochSeconds,
                    )
                    val session = getCurrentSession()
                    val profile = getCurrentProfile()
                    _state.update {
                        it.copy(
                            isLoading = false,
                            session = session ?: it.session,
                            profile = profile ?: it.profile,
                            message = "Verification code sent.",
                            verificationResendAvailableAtEpochSeconds = resendAvailableAtEpochSeconds,
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, message = e.message ?: "Request failed") }
                }
        }
    }

    fun changeVerificationEmail() {
        val email = _state.value.verificationEmail.trim()
        scope.launch {
            clearPendingAuthFlow()
            _state.update {
                it.copy(
                    registerEmail = email,
                    verificationEmail = "",
                    verificationCode = "",
                    verificationResendAvailableAtEpochSeconds = 0,
                    registeredEmail = null,
                    emailVerified = false,
                    isLoading = false,
                    message = null,
                )
            }
        }
    }

    fun loginWithEmail(email: String, password: String) {
        scope.launch {
            _state.update { it.copy(isLoading = true, message = null) }
            runCatching {
                login(
                    LoginParams(
                        email = email.trim(),
                        password = password,
                        deviceName = currentDeviceName(),
                    ),
                )
            }.onSuccess { session ->
                clearPendingAuthFlow()
                completeLoginSession(session)
                val profile = getCurrentProfile()
                _state.update {
                    it.copy(
                        isLoading = false,
                        session = session,
                        profile = profile,
                        loginPassword = "",
                        verificationCode = "",
                        message = null,
                    )
                }
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, message = e.message ?: "Login failed") }
            }
        }
    }

    fun refresh() = runAction("Session refreshed.") { refreshSession() }

    fun logout() {
        scope.launch {
            _state.update { it.copy(isLoading = true, message = null) }
            runCatching { logout.invoke() }
                .onSuccess {
                    clearPendingAuthFlow()
                    completeLogoutSession()
                    _state.value = AuthUiState(message = "Logged out")
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, message = e.message ?: "Logout failed") }
                }
        }
    }

    fun consumeRegisterSuccess() {
        _state.update { it.copy(registeredEmail = null) }
    }

    fun consumeExistingAccountEmail() {
        _state.update { it.copy(existingAccountEmail = null) }
    }

    fun consumeEmailVerified() {
        _state.update { it.copy(emailVerified = false, verificationCode = "") }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }

    private fun runAction(successMessage: String, action: suspend () -> Unit) {
        scope.launch {
            _state.update { it.copy(isLoading = true, message = null) }
            runCatching { action() }
                .onSuccess {
                    val session = getCurrentSession()
                    val profile = getCurrentProfile()
                    _state.update {
                        it.copy(
                            isLoading = false,
                            session = session ?: it.session,
                            profile = profile ?: it.profile,
                            message = successMessage,
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, message = e.message ?: "Request failed") }
                }
        }
    }
}
