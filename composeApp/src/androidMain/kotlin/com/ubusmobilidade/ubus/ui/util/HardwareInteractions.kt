package com.ubusmobilidade.ubus.ui.util

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.net.Uri

@Composable
actual fun rememberFilePickerLauncher(onResult: (String?) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        onResult(uri?.toString())
    }
    return { launcher.launch("*/*") }
}

@Composable
actual fun rememberCameraLauncher(onResult: (String?) -> Unit): () -> Unit {
    val context = androidx.compose.ui.platform.LocalContext.current
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && photoUri != null) {
            onResult(photoUri.toString())
        } else {
            onResult(null)
        }
    }

    return {
        try {
            val file = java.io.File(context.cacheDir, "images").apply { mkdirs() }
            val tempFile = java.io.File.createTempFile("student_photo_", ".jpg", file)
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "com.ubusmobilidade.ubus.fileprovider",
                tempFile
            )
            photoUri = uri
            launcher.launch(uri)
        } catch (e: Exception) {
            onResult(null)
        }
    }
}
