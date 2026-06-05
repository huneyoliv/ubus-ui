package com.ubusmobilidade.ubus.ui.util

import androidx.compose.runtime.Composable

@Composable
expect fun rememberFilePickerLauncher(onResult: (String?) -> Unit): () -> Unit

@Composable
expect fun rememberCameraLauncher(onResult: (String?) -> Unit): () -> Unit

expect suspend fun readFileBytes(uri: String): ByteArray
expect fun getFileNameFromUri(uri: String): String

expect fun getCurrentTimeMillis(): Long
expect fun getTodayDateString(): String


