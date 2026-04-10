package com.ubusmobilidade.ubus

import androidx.compose.runtime.Composable
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.navigation.RootContent
import com.ubusmobilidade.ubus.ui.theme.UbusTheme

@Composable
fun App(component: RootComponent) {
    UbusTheme {
        RootContent(component)
    }
}