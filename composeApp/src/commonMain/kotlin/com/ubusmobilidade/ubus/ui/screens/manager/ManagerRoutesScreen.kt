package com.ubusmobilidade.ubus.ui.screens.manager

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.FleetRepository
import com.ubusmobilidade.ubus.data.model.Route
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.theme.UbusAccent
import com.ubusmobilidade.ubus.ui.theme.UbusBackground
import com.ubusmobilidade.ubus.ui.theme.UbusDestructive
import com.ubusmobilidade.ubus.ui.theme.UbusMutedForeground
import com.ubusmobilidade.ubus.ui.theme.UbusSuccess

@Composable
fun ManagerRoutesScreen(component: RootComponent) {
    val apiClient = remember { ApiClient(component.authStorage, onUnauthorized = { component.logout() }) }
    val fleetRepo = remember { FleetRepository(apiClient) }
    var routes by remember { mutableStateOf<List<Route>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            routes = fleetRepo.listRoutes()
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
            "Rotas",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
        )
        Text(
            "Gerencie as linhas de transporte",
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
        } else if (routes.isEmpty()) {
            BentoCard {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Route, null, tint = UbusMutedForeground, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Nenhuma rota cadastrada", style = MaterialTheme.typography.titleMedium, color = UbusMutedForeground, textAlign = TextAlign.Center)
                }
            }
        } else {
            routes.forEach { route ->
                BentoCard(modifier = Modifier.padding(bottom = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Route, null, tint = UbusAccent, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(route.name, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
                            if (route.description != null) {
                                Text(route.description, style = MaterialTheme.typography.bodySmall, color = UbusMutedForeground)
                            }
                            val weekDaysText = route.weekDays?.joinToString(", ") { dayName(it) } ?: ""
                            if (weekDaysText.isNotEmpty()) {
                                Text(weekDaysText, style = MaterialTheme.typography.bodySmall, color = UbusMutedForeground)
                            }
                        }
                        Text(
                            if (route.active) "Ativa" else "Inativa",
                            color = if (route.active) UbusSuccess else UbusDestructive,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                    if (route.votingOpenTime != null && route.votingCloseTime != null) {
                        Text(
                            "Votação: ${route.votingOpenTime} - ${route.votingCloseTime}",
                            style = MaterialTheme.typography.bodySmall,
                            color = UbusMutedForeground,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

private fun dayName(day: Int): String = when (day) {
    0 -> "Dom"
    1 -> "Seg"
    2 -> "Ter"
    3 -> "Qua"
    4 -> "Qui"
    5 -> "Sex"
    6 -> "Sáb"
    else -> "$day"
}
