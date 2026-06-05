package com.ubusmobilidade.ubus.ui.screens.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DirectionsBus
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
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.DriverRepository
import com.ubusmobilidade.ubus.data.api.FleetRepository
import com.ubusmobilidade.ubus.data.model.Bus
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary
import com.ubusmobilidade.ubus.ui.theme.UbusText3
import com.ubusmobilidade.ubus.ui.components.UbusOutlinedButton

@Composable
fun SelecionarVeiculoScreen(component: RootComponent) {
    val scope = rememberCoroutineScope()
    var vehicles by remember { mutableStateOf<List<Bus>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val apiClient = remember { ApiClient(component.authStorage, onUnauthorized = { component.logout() }) }
    val fleetRepo = remember { FleetRepository(apiClient) }
    val driverRepo = remember { DriverRepository(apiClient) }

    LaunchedEffect(Unit) {
        try {
            vehicles = fleetRepo.listMyBuses()
        } catch (e: Exception) {
            error = "Erro ao carregar veículos. Tente novamente."
        } finally {
            loading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        IconButton(onClick = { component.goBack() }, modifier = Modifier.padding(top = 8.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = MaterialTheme.colorScheme.onBackground)
        }
        Text(
            "Selecionar veículo",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )


        if (loading) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = UbusPrimary)
            }
        } else if (error != null) {
            BentoCard {
                Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
        } else if (vehicles.isEmpty()) {
            BentoCard {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.DirectionsBus, null, tint = UbusText3, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Nenhum veículo encontrado", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                    Text("Cadastre um novo veículo para começar", style = MaterialTheme.typography.bodySmall, color = UbusText3)
                }
            }
        } else {
            vehicles.forEach { bus ->
                BentoCard(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .clickable {
                            scope.launch {
                                try {
                                    val today = com.ubusmobilidade.ubus.ui.util.getTodayDateString()
                                    driverRepo.assignForToday(busId = bus.id, serviceDate = today)
                                    component.navigateTo(RootComponent.Config.Mapa)
                                } catch (e: Exception) {
                                    if (e is kotlinx.coroutines.CancellationException) throw e
                                    error = e.message ?: "Erro ao vincular veículo."
                                }
                            }
                        }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DirectionsBus, null, tint = UbusPrimary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(bus.identificationNumber, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
                            Text(bus.plate ?: "Sem placa", style = MaterialTheme.typography.bodySmall, color = UbusText3)
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = UbusText3, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        
        UbusOutlinedButton(
            text = "Cadastrar novo veículo",
            onClick = { component.navigateTo(RootComponent.Config.CadastroVeiculoMultiStep) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        )
    }
}
