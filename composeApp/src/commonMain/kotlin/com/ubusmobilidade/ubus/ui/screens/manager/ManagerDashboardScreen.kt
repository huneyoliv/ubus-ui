package com.ubusmobilidade.ubus.ui.screens.manager

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.theme.UbusAccent
import com.ubusmobilidade.ubus.ui.theme.UbusBackground
import com.ubusmobilidade.ubus.ui.theme.UbusDestructive
import com.ubusmobilidade.ubus.ui.theme.UbusMutedForeground

private data class DashItem(val icon: ImageVector, val label: String, val config: RootComponent.Config)

private val dashItems = listOf(
    DashItem(Icons.Default.Route, "Rotas", RootComponent.Config.ManagerRoutes),
    DashItem(Icons.Default.CheckCircle, "Validações", RootComponent.Config.ManagerValidations),
    DashItem(Icons.Default.DirectionsBus, "Frota", RootComponent.Config.ManagerFrota),
    DashItem(Icons.Default.Person, "Motoristas", RootComponent.Config.ManagerMotoristas),
    DashItem(Icons.Default.Assessment, "Relatórios", RootComponent.Config.ManagerRelatorios),
    DashItem(Icons.Default.Settings, "Configurações", RootComponent.Config.ManagerConfiguracoes),
)

@Composable
fun ManagerDashboardScreen(component: RootComponent) {
    val user = component.authStorage.user

    Column(
        modifier = Modifier.fillMaxSize().background(UbusBackground).padding(horizontal = 20.dp),
    ) {
        Text("Painel do gestor", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(top = 48.dp, bottom = 8.dp))
        Text("Olá, ${user?.name ?: "Gestor"}!", style = MaterialTheme.typography.bodyMedium, color = UbusMutedForeground, modifier = Modifier.padding(bottom = 24.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f),
        ) {
            items(dashItems) { item ->
                BentoCard(modifier = Modifier.clickable { component.navigateTo(item.config) }) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    ) {
                        Icon(item.icon, null, tint = UbusAccent, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(item.label, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        BentoCard(modifier = Modifier.clickable { component.logout() }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = UbusDestructive, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text("Sair", style = MaterialTheme.typography.titleSmall, color = UbusDestructive)
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}
