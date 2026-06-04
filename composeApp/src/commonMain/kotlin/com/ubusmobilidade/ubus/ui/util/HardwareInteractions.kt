package com.ubusmobilidade.ubus.ui.util

import androidx.compose.runtime.Composable

@Composable
expect fun rememberFilePickerLauncher(onResult: (String?) -> Unit): () -> Unit

@Composable
expect fun rememberCameraLauncher(onResult: (String?) -> Unit): () -> Unit
