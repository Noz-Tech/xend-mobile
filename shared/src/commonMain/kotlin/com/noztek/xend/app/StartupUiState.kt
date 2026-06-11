package com.noztek.xend.app

enum class StartupDestination {
    OFFLINE,
    WELCOME,
    VERIFY_EMAIL,
    MAIN,
}

data class StartupUiState(
    val isChecking: Boolean = false,
    val isApiOnline: Boolean? = null,
    val hasSession: Boolean = false,
    val destination: StartupDestination? = null,
    val pendingVerificationEmail: String? = null,
)
