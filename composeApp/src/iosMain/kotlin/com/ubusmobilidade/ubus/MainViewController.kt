package com.ubusmobilidade.ubus

import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.ubusmobilidade.ubus.navigation.RootComponent

// Hold references at module level so they survive recomposition
private val lifecycle = LifecycleRegistry().apply { resume() }
private val rootComponent = RootComponent(DefaultComponentContext(lifecycle = lifecycle))

fun MainViewController() = ComposeUIViewController {
    App(rootComponent)
}