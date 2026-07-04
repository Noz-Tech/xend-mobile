package com.noztek.xend.core.ui.scan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberInviteQrScannerLauncher(
    onScanned: (String) -> Unit,
    onUnavailable: (String) -> Unit,
): () -> Unit {
    return remember(onUnavailable) {
        {
            onUnavailable("QR scanning is not available on iOS yet.")
        }
    }
}
