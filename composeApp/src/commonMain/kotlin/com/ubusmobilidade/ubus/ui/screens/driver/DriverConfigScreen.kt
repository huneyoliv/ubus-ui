package com.ubusmobilidade.ubus.ui.screens.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.AppScaffold
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.components.DriverBottomNavBar
import com.ubusmobilidade.ubus.ui.components.DriverTab
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary
import com.ubusmobilidade.ubus.ui.theme.UbusDestructive

@Composable
fun DriverConfigScreen(component: RootComponent) {
    AppScaffold(
        bottomBar = {
            DriverBottomNavBar(
                selectedTab = DriverTab.CONFIG,
                onTabSelected = { tab ->
                    when (tab) {
                        DriverTab.MAPA -> component.replaceWith(RootComponent.Config.Mapa)
                        DriverTab.AVISOS -> component.replaceWith(RootComponent.Config.Avisos)
                        DriverTab.CONFIG -> {}
                    }
                },
            )
        },
    ) {
        Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 20.dp)) {
            Text("Configurações", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(top = 32.dp, bottom = 24.dp))

            BentoCard(modifier = Modifier.padding(bottom = 8.dp).clickable { component.navigateTo(RootComponent.Config.MeusDados) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, tint = UbusPrimary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Meus dados", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
                }
            }

            BentoCard(modifier = Modifier.padding(bottom = 8.dp).clickable { component.navigateTo(RootComponent.Config.AlterarSenha) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, null, tint = UbusPrimary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Alterar senha", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
                }
            }

            Spacer(Modifier.height(8.dp))
            BentoCard(modifier = Modifier.clickable { component.logout() }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = UbusDestructive, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Sair da conta", style = MaterialTheme.typography.titleSmall, color = UbusDestructive)
                }
            }
        }
    }
}
