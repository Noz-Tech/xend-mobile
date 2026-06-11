package com.noztek.xend.feature.auth.presentation.state

import com.noztek.xend.feature.auth.domain.model.AuthSessionModel
import com.noztek.xend.feature.auth.domain.model.UserProfileModel

data class AuthUiState(
    val isLoading: Boolean = false,
    val session: AuthSessionModel? = null,
    val profile: UserProfileModel? = null,
    val registerDisplayName: String = "",
    val registerEmail: String = "",
    val registerPassword: String = "",
    val loginEmail: String = "",
    val loginPassword: String = "",
    val verificationEmail: String = "",
    val verificationCode: String = "",
    val verificationResendAvailableAtEpochSeconds: Long = 0,
    val message: String? = null,
    val registeredEmail: String? = null,
    val emailVerified: Boolean = false,
) {
    val isRegisterSubmissionEnabled: Boolean
        get() = !isLoading &&
            registerDisplayName.isNotBlank() &&
            registerEmail.isNotBlank() &&
            registerPassword.length >= 8

    val isLoginSubmissionEnabled: Boolean
        get() = !isLoading &&
            loginEmail.isNotBlank() &&
            loginPassword.isNotBlank()

    val isVerificationSubmissionEnabled: Boolean
        get() = !isLoading &&
            verificationEmail.isNotBlank() &&
            verificationCode.isNotBlank()
}
