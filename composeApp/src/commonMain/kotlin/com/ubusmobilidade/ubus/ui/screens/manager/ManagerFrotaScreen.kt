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
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Wc
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.FleetRepository
import com.ubusmobilidade.ubus.data.model.Bus
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.theme.UbusAccent
import com.ubusmobilidade.ubus.ui.theme.UbusBackground
import com.ubusmobilidade.ubus.ui.theme.UbusDestructive
import com.ubusmobilidade.ubus.ui.theme.UbusMutedForeground
import com.ubusmobilidade.ubus.ui.theme.UbusSuccess

@Composable
fun ManagerFrotaScreen(component: RootComponent) {
    val apiClient = remember { ApiClient(component.authStorage, onUnauthorized = { component.logout() }) }
    val fleetRepo = remember { FleetRepository(apiClient) }
    var buses by remember { mutableStateOf<List<Bus>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            buses = fleetRepo.listBuses()
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
            "Frota",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
        )
        Text(
            "Veículos cadastrados no sistema",
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
        } else if (buses.isEmpty()) {
            BentoCard {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.DirectionsBus, null, tint = UbusMutedForeground, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Nenhum veículo cadastrado", style = MaterialTheme.typography.titleMedium, color = UbusMutedForeground, textAlign = TextAlign.Center)
                }
            }
        } else {
            buses.forEach { bus ->
                BentoCard(modifier = Modifier.padding(bottom = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DirectionsBus, null, tint = UbusAccent, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Nº ${bus.identificationNumber}", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
                            Text("Placa: ${bus.plate ?: "—"} · ${bus.standardCapacity} lugares", style = MaterialTheme.typography.bodySmall, color = UbusMutedForeground)
                        }
                        Text(
                            if (bus.active) "Ativo" else "Inativo",
                            color = if (bus.active) UbusSuccess else UbusDestructive,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                    Row(modifier = Modifier.padding(top = 4.dp)) {
                        if (bus.hasAirConditioning) {
                            Icon(Icons.Default.AcUnit, "Ar", tint = UbusMutedForeground, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("A/C", style = MaterialTheme.typography.bodySmall, color = UbusMutedForeground)
                            Spacer(Modifier.width(12.dp))
                        }
                        if (bus.hasBathroom) {
                            Icon(Icons.Default.Wc, "WC", tint = UbusMutedForeground, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("WC", style = MaterialTheme.typography.bodySmall, color = UbusMutedForeground)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
