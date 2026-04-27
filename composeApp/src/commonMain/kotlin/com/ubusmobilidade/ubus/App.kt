package com.ubusmobilidade.ubus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.navigation.RootContent
import com.ubusmobilidade.ubus.ui.theme.UbusBackground
import com.ubusmobilidade.ubus.ui.theme.UbusTheme

@Composable
fun App(component: RootComponent) {
    LaunchedEffect(Unit) {
        println("DEBUG: ### UBUS VERSION 2.0 - NEW LOGIC DEPLOYED ###")
    }
    UbusTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(UbusBackground)
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            RootContent(component)
        }
    }
}