package com.noztek.xend.core.ui.scan

import androidx.compose.runtime.Composable

@Composable
expect fun rememberInviteQrScannerLauncher(
    onScanned: (String) -> Unit,
    onUnavailable: (String) -> Unit = {},
): () -> Unit
