package com.noztek.xend.app

data class StartupUiState(
    val isChecking: Boolean = false,
    val isApiOnline: Boolean? = null,
    val hasSession: Boolean = false,
)
