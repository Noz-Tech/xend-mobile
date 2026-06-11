package com.noztek.xend.feature.auth.presentation.composables

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

@Composable
internal fun rememberAuthSnackbarHostState(
    message: String?,
    onMessageConsumed: () -> Unit,
): SnackbarHostState {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        if (message.isNullOrBlank()) return@LaunchedEffect
        snackbarHostState.currentSnackbarData?.dismiss()
        snackbarHostState.showSnackbar(message)
        onMessageConsumed()
    }

    return snackbarHostState
}
