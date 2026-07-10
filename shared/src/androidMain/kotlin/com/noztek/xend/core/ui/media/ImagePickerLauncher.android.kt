package com.noztek.xend.core.ui.media

import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberImagePickerLauncher(
    onPicked: (PickedImageData) -> Unit,
    onUnavailable: (String) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val latestOnPicked = rememberUpdatedState(onPicked)
    val latestOnUnavailable = rememberUpdatedState(onUnavailable)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult

        runCatching {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri)?.trim().orEmpty().ifBlank { "image/jpeg" }
            val fileName = contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
                }
                ?.takeIf { it.isNotBlank() }
                ?: "ritual-photo"
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: error("Unable to read selected image.")
            PickedImageData(
                bytes = bytes,
                fileName = fileName,
                mimeType = mimeType,
            )
        }.onSuccess { picked ->
            latestOnPicked.value(picked)
        }.onFailure {
            latestOnUnavailable.value("Unable to read the selected image.")
        }
    }

    return remember(launcher) {
        { launcher.launch("image/*") }
    }
}
