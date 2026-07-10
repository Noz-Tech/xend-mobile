package com.noztek.xend.core.ui.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberImagePickerLauncher(
    onPicked: (PickedImageData) -> Unit,
    onUnavailable: (String) -> Unit,
): () -> Unit {
    return remember(onUnavailable) {
        {
            onUnavailable("Image picking is not available on iOS yet.")
        }
    }
}
