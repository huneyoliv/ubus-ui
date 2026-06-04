package com.ubusmobilidade.ubus.ui.screens.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
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
import com.ubusmobilidade.ubus.ui.theme.UbusText3

@Composable
fun MapaScreen(component: RootComponent) {
    AppScaffold(
        bottomBar = {
            DriverBottomNavBar(
                selectedTab = DriverTab.MAPA,
                onTabSelected = { tab ->
                    when (tab) {
                        DriverTab.MAPA -> {}
                        DriverTab.AVISOS -> component.replaceWith(RootComponent.Config.Avisos)
                        DriverTab.CONFIG -> component.replaceWith(RootComponent.Config.DriverConfig)
                    }
                },
            )
        },
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(Icons.Default.Map, null, tint = UbusText3, modifier = Modifier.size(64.dp))
                Text("Mapa em breve", style = MaterialTheme.typography.titleMedium, color = UbusText3, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}
