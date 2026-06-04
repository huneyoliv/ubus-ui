package com.ubusmobilidade.ubus.ui.util

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun ForceMaxBrightness() {
    val context = LocalContext.current
    val activity = context as? Activity ?: return

    DisposableEffect(Unit) {
        val window = activity.window
        val layoutParams = window.attributes
        val originalBrightness = layoutParams.screenBrightness

        layoutParams.screenBrightness = 1.0f
        window.attributes = layoutParams

        onDispose {
            layoutParams.screenBrightness = originalBrightness
            window.attributes = layoutParams
        }
    }
}
