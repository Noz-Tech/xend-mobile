package com.noztek.xend.core.ui.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.OpenableColumns
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import java.io.ByteArrayOutputStream

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
            val displayName = contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
                }
                ?.takeIf { it.isNotBlank() }
            val bytes = decodeImageToJpegBytes(context, uri)
            require(bytes.isNotEmpty()) { "Selected image is empty." }
            PickedImageData(
                bytes = bytes,
                fileName = ensureJpegFileName(displayName),
                mimeType = "image/jpeg",
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

private fun decodeImageToJpegBytes(
    context: android.content.Context,
    uri: android.net.Uri,
): ByteArray {
    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
            decoder.isMutableRequired = false
        }
    } else {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            ?: context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)
    } ?: error("Unable to decode selected image.")

    return ByteArrayOutputStream().use { output ->
        check(bitmap.compress(Bitmap.CompressFormat.JPEG, 92, output)) { "Unable to encode selected image." }
        output.toByteArray()
    }
}

private fun ensureJpegFileName(fileName: String?): String {
    val baseName = fileName
        ?.substringBeforeLast('.')
        ?.ifBlank { "selected-image" }
        ?: "selected-image"
    val sanitized = buildString(baseName.length) {
        baseName.forEach { char ->
            append(
                when {
                    char.isLetterOrDigit() -> char
                    char == '-' || char == '_' -> char
                    else -> '_'
                },
            )
        }
    }.trim('_').ifBlank { "selected-image" }
    return "$sanitized.jpg"
}
