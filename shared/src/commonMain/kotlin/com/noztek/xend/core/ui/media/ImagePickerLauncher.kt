package com.noztek.xend.core.ui.media

import androidx.compose.runtime.Composable

data class PickedImageData(
    val bytes: ByteArray,
    val fileName: String,
    val mimeType: String,
)

@Composable
expect fun rememberImagePickerLauncher(
    onPicked: (PickedImageData) -> Unit,
    onUnavailable: (String) -> Unit = {},
): () -> Unit
