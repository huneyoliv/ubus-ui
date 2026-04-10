package com.ubusmobilidade.ubus.ui.screens.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.AppScaffold
import com.ubusmobilidade.ubus.ui.components.DriverBottomNavBar
import com.ubusmobilidade.ubus.ui.components.DriverTab
import com.ubusmobilidade.ubus.ui.theme.UbusBackground
import com.ubusmobilidade.ubus.ui.theme.UbusMutedForeground

@Composable
fun AvisosScreen(component: RootComponent) {
    AppScaffold(
        bottomBar = {
            DriverBottomNavBar(
                selectedTab = DriverTab.AVISOS,
                onTabSelected = { tab ->
                    when (tab) {
                        DriverTab.MAPA -> component.replaceWith(RootComponent.Config.Mapa)
                        DriverTab.AVISOS -> {}
                        DriverTab.CONFIG -> component.replaceWith(RootComponent.Config.DriverConfig)
                    }
                },
            )
        },
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(UbusBackground),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(Icons.Default.Notifications, null, tint = UbusMutedForeground, modifier = Modifier.size(64.dp))
                Text("Nenhum aviso", style = MaterialTheme.typography.titleMedium, color = UbusMutedForeground, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}
