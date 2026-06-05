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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.MetricsRepository
import com.ubusmobilidade.ubus.data.model.DashboardMetrics
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary
import com.ubusmobilidade.ubus.ui.theme.UbusText3
import com.ubusmobilidade.ubus.ui.theme.UbusSuccess
import com.ubusmobilidade.ubus.ui.theme.UbusWarning
import com.ubusmobilidade.ubus.ui.util.toUserMessage

private data class DashItem(val icon: ImageVector, val label: String, val config: RootComponent.Config)

private val dashItems = listOf(
    DashItem(Icons.Default.Route, "Rotas", RootComponent.Config.ManagerRoutes),
    DashItem(Icons.Default.CheckCircle, "Validações", RootComponent.Config.ManagerValidations),
    DashItem(Icons.Default.DirectionsBus, "Frota", RootComponent.Config.ManagerFrota),
    DashItem(Icons.Default.Person, "Motoristas", RootComponent.Config.ManagerMotoristas),
    DashItem(Icons.Default.Assessment, "Relatórios", RootComponent.Config.ManagerRelatorios),
    DashItem(Icons.Default.Notifications, "Notificações", RootComponent.Config.ManagerNotificacoes),
    DashItem(Icons.Default.Settings, "Configurações", RootComponent.Config.ManagerConfiguracoes),
)

@Composable
fun ManagerDashboardScreen(component: RootComponent) {
    val user = component.authStorage.user
    val apiClient = remember { ApiClient(component.authStorage, onUnauthorized = { component.logout() }) }
    val metricsRepo = remember { MetricsRepository(apiClient) }
    var metrics by remember { mutableStateOf<DashboardMetrics?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            metrics = metricsRepo.getDashboard()
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            loadError = e.toUserMessage("Não foi possível carregar os indicadores.")
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 20.dp),
    ) {
        Text("Painel do gestor", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
        Text("Olá, ${user?.name ?: "Gestor"}!", style = MaterialTheme.typography.bodyMedium, color = UbusText3, modifier = Modifier.padding(bottom = 16.dp))

        if (!loadError.isNullOrBlank()) {
            BentoCard(modifier = Modifier.padding(bottom = 16.dp)) {
                Text(
                    loadError!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        // Quick metrics row
        if (metrics != null) {
            val m = metrics!!
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                SmallMetric("${m.activeTrips ?: 0}", "Viagens", Icons.Default.Schedule, UbusSuccess, Modifier.weight(1f))
                SmallMetric("${m.pendingUsers ?: 0}", "Pendentes", Icons.Default.Group, UbusWarning, Modifier.weight(1f))
                SmallMetric("${m.totalReservationsToday ?: 0}", "Reservas", Icons.Default.Assessment, UbusPrimary, Modifier.weight(1f))
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f),
        ) {
            items(dashItems) { item ->
                BentoCard(onClick = { component.navigateTo(item.config) }) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    ) {
                        Icon(item.icon, null, tint = UbusPrimary, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(item.label, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SmallMetric(
    value: String,
    label: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    BentoCard(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text(label, style = MaterialTheme.typography.labelSmall, color = UbusText3)
        }
    }
}
