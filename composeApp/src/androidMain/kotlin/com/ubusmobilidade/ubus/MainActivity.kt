package com.ubusmobilidade.ubus

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.arkivanov.decompose.defaultComponentContext
import com.ubusmobilidade.ubus.navigation.RootComponent

class MainActivity : ComponentActivity() {

    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* user responded — app continues regardless */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        requestAppPermissions()

        val rootComponent = RootComponent(defaultComponentContext())

        setContent {
            App(rootComponent)
        }
    }

    private fun requestAppPermissions() {
        val needed = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            needed += Manifest.permission.POST_NOTIFICATIONS
            needed += Manifest.permission.READ_MEDIA_IMAGES
        } else {
            needed += Manifest.permission.READ_EXTERNAL_STORAGE
        }

        val missing = needed.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            permissionsLauncher.launch(missing.toTypedArray())
        }
    }
}