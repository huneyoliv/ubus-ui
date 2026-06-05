package com.ubusmobilidade.ubus.ui.screens.manager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.FleetRepository
import com.ubusmobilidade.ubus.data.model.Bus
import com.ubusmobilidade.ubus.data.model.OccupiedSeat
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary
import com.ubusmobilidade.ubus.ui.theme.UbusDestructive
import com.ubusmobilidade.ubus.ui.theme.UbusText3
import com.ubusmobilidade.ubus.ui.theme.UbusSuccess
import com.ubusmobilidade.ubus.ui.util.toUserMessage
import kotlinx.coroutines.launch

@Composable
fun ManagerBusDetailScreen(component: RootComponent, busId: String) {
    val apiClient = remember { ApiClient(component.authStorage, onUnauthorized = { component.logout() }) }
    val fleetRepo = remember { FleetRepository(apiClient) }
    
    var bus by remember { mutableStateOf<Bus?>(null) }
    var occupiedSeats by remember { mutableStateOf<List<OccupiedSeat>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }

    LaunchedEffect(busId) {
        try {
            val b = fleetRepo.getBus(busId)
            bus = b
            try {
                occupiedSeats = fleetRepo.getBusLayout(busId)
            } catch (e: Exception) {
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            error = e.toUserMessage("Não foi possível carregar os detalhes do veículo.")
        }
        loading = false
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
            IconButton(onClick = { component.goBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
            }
            Text("Detalhes do Veículo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = UbusPrimary)
            }
        } else if (bus != null) {
            val b = bus!!
            Spacer(Modifier.height(16.dp))
            
            BentoCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Nº Identificação: ${b.identificationNumber}", fontWeight = FontWeight.Bold)
                    Text("Placa: ${b.plate ?: "—"}")
                    Text("Capacidade: ${b.standardCapacity} poltronas")
                    Row {
                        if (b.hasAirConditioning) Badge("A/C", UbusSuccess)
                        Spacer(Modifier.width(8.dp))
                        if (b.hasBathroom) Badge("WC", UbusPrimary)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
            Text("Layout de Ocupação", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Vermelho: Ocupado | Verde: Livre", style = MaterialTheme.typography.bodySmall, color = UbusText3)
            Spacer(Modifier.height(16.dp))

            BusLayoutGrid(b.standardCapacity, occupiedSeats)
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun Badge(text: String, color: Color) {
    Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
        Text(text, color = color, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
    }
}

@Composable
private fun BusLayoutGrid(capacity: Int, occupied: List<OccupiedSeat>) {
    val rows = (capacity + 3) / 4 // 4 seats per row
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        for (i in 0 until rows) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                for (j in 0 until 4) {
                    val seatNum = i * 4 + j + 1
                    if (seatNum <= capacity) {
                        val isOccupied = occupied.any { it.seatNumber == seatNum }
                        SeatVisual(seatNum, isOccupied, Modifier.weight(1f))
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                    if (j == 1) Spacer(Modifier.width(24.dp)) // Corredor
                }
            }
        }
    }
}

@Composable
private fun SeatVisual(number: Int, isOccupied: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.height(48.dp).background(if (isOccupied) UbusDestructive else UbusSuccess, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text("$number", color = Color.White, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}
