package com.noztek.xend.core.ui.haptics

import androidx.compose.runtime.Composable

@Composable
expect fun rememberSwipeCompletionHaptic(): () -> Unit
