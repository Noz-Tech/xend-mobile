package com.noztek.xend.core.ui.scan

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@Composable
actual fun rememberInviteQrScannerLauncher(
    onScanned: (String) -> Unit,
    onUnavailable: (String) -> Unit,
): () -> Unit {
    val latestOnScanned = rememberUpdatedState(onScanned)
    val launcher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val content = result.contents?.trim().orEmpty()
        if (content.isNotBlank()) {
            latestOnScanned.value(content)
        }
    }

    return remember(launcher) {
        {
            val options = ScanOptions()
                .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                .setPrompt("Scan your partner's invite code")
                .setBeepEnabled(false)
                .setOrientationLocked(true)
                .setBarcodeImageEnabled(false)

            launcher.launch(options)
        }
    }
}
