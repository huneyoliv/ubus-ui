package com.ubusmobilidade.ubus.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import platform.UIKit.UIScreen

@Composable
actual fun ForceMaxBrightness() {
    DisposableEffect(Unit) {
        val screen = UIScreen.mainScreen
        val originalBrightness = screen.brightness

        screen.setBrightness(1.0)

        onDispose {
            screen.setBrightness(originalBrightness)
        }
    }
}
