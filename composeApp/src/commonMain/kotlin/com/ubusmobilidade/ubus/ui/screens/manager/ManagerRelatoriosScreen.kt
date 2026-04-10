package com.ubusmobilidade.ubus.ui.screens.manager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.ubusmobilidade.ubus.ui.theme.UbusAccent
import com.ubusmobilidade.ubus.ui.theme.UbusBackground
import com.ubusmobilidade.ubus.ui.theme.UbusDestructive
import com.ubusmobilidade.ubus.ui.theme.UbusMutedForeground
import com.ubusmobilidade.ubus.ui.theme.UbusSuccess
import com.ubusmobilidade.ubus.ui.theme.UbusWarning

@Composable
fun ManagerRelatoriosScreen(component: RootComponent) {
    val apiClient = remember { ApiClient(component.authStorage, onUnauthorized = { component.logout() }) }
    val metricsRepo = remember { MetricsRepository(apiClient) }
    var metrics by remember { mutableStateOf<DashboardMetrics?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            metrics = metricsRepo.getDashboard()
        } catch (e: Exception) {
            error = e.message ?: "Erro ao carregar"
        }
        loading = false
    }

    Column(
        modifier = Modifier.fillMaxSize().background(UbusBackground)
            .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
    ) {
        IconButton(onClick = { component.goBack() }, modifier = Modifier.padding(top = 8.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = MaterialTheme.colorScheme.onBackground)
        }
        Text(
            "Relatórios",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
        )
        Text(
            "Métricas e dados do sistema",
            style = MaterialTheme.typography.bodyMedium,
            color = UbusMutedForeground,
            modifier = Modifier.padding(bottom = 20.dp),
        )

        if (loading) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = UbusAccent)
            }
        } else if (error.isNotEmpty()) {
            BentoCard {
                Text(error, color = UbusDestructive, style = MaterialTheme.typography.bodyMedium)
            }
        } else if (metrics != null) {
            val m = metrics!!

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("Alunos", "${m.totalStudents ?: 0}", Icons.Default.Group, UbusAccent, Modifier.weight(1f))
                MetricCard("Motoristas", "${m.totalDrivers ?: 0}", Icons.Default.Person, UbusSuccess, Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("Veículos", "${m.totalBuses ?: 0}", Icons.Default.DirectionsBus, UbusWarning, Modifier.weight(1f))
                MetricCard("Rotas", "${m.totalRoutes ?: 0}", Icons.Default.Route, UbusAccent, Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("Viagens ativas", "${m.activeTrips ?: 0}", Icons.Default.Schedule, UbusSuccess, Modifier.weight(1f))
                MetricCard("Pendentes", "${m.pendingUsers ?: 0}", Icons.Default.Assessment, UbusWarning, Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            MetricCard("Reservas hoje", "${m.totalReservationsToday ?: 0}", Icons.Default.Today, UbusAccent, Modifier.fillMaxWidth())
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    BentoCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text(label, style = MaterialTheme.typography.bodySmall, color = UbusMutedForeground)
            }
        }
    }
}
