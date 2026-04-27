package com.ubusmobilidade.ubus.ui.screens.manager

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.BackendCapabilities
import com.ubusmobilidade.ubus.data.api.FleetRepository
import com.ubusmobilidade.ubus.data.model.Bus
import com.ubusmobilidade.ubus.data.model.Route
import com.ubusmobilidade.ubus.data.model.UpdateRoutePayload
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.components.UbusButton
import com.ubusmobilidade.ubus.ui.components.UbusTextField
import com.ubusmobilidade.ubus.ui.theme.UbusDestructive
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary
import com.ubusmobilidade.ubus.ui.theme.UbusSuccess
import com.ubusmobilidade.ubus.ui.theme.UbusText3
import com.ubusmobilidade.ubus.ui.util.toUserMessage
import kotlinx.coroutines.launch

@Composable
fun ManagerRouteDetailScreen(component: RootComponent, routeId: String) {
    val scope = rememberCoroutineScope()
    val apiClient = remember { ApiClient(component.authStorage, onUnauthorized = { component.logout() }) }
    val fleetRepo = remember { FleetRepository(apiClient) }
    
    var route by remember { mutableStateOf<Route?>(null) }
    var assignedBuses by remember { mutableStateOf<List<Bus>>(emptyList()) }
    var allBuses by remember { mutableStateOf<List<Bus>>(emptyList()) }
    
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var syncingCalendar by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    
    // Form states
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var votingOpenTime by remember { mutableStateOf("") }
    var votingCloseTime by remember { mutableStateOf("") }

    LaunchedEffect(routeId) {
        try {
            val routes = fleetRepo.listRoutes()
            val r = routes.find { it.id == routeId }
            if (r != null) {
                route = r
                name = r.name
                description = r.description ?: ""
                votingOpenTime = r.votingOpenTime ?: ""
                votingCloseTime = r.votingCloseTime ?: ""
                
                assignedBuses = fleetRepo.listBusesByRoute(routeId)
                allBuses = fleetRepo.listBuses()
            } else {
                error = "Rota não encontrada."
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            error = e.toUserMessage("Não foi possível carregar os detalhes da rota.")
        }
        loading = false
    }

    fun handleSave() {
        saving = true
        scope.launch {
            try {
                fleetRepo.updateRoute(routeId, UpdateRoutePayload(
                    name = name,
                    description = description,
                    votingOpenTime = votingOpenTime,
                    votingCloseTime = votingCloseTime
                ))
                error = "Alterações salvas com sucesso!"
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                error = e.toUserMessage("Erro ao salvar rota.")
            }
            saving = false
        }
    }

    fun assignBus(busId: String) {
        scope.launch {
            try {
                fleetRepo.assignBusToRoute(routeId, busId)
                assignedBuses = fleetRepo.listBusesByRoute(routeId)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                error = e.toUserMessage("Erro ao atribuir ônibus.")
            }
        }
    }

    fun removeBus(busId: String) {
        scope.launch {
            try {
                fleetRepo.removeBusFromRoute(routeId, busId)
                assignedBuses = assignedBuses.filter { it.id != busId }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                error = e.toUserMessage("Erro ao remover ônibus.")
            }
        }
    }

    fun syncRouteCalendar() {
        if (!BackendCapabilities.supportsManagerRouteCalendarScheduling) return
        syncingCalendar = true
        scope.launch {
            try {
                // TODO: replace hardcoded month with UI-controlled month selector.
                val calendar = fleetRepo.getRouteCalendar(routeId = routeId, month = "2026-01")
                error = "Calendario sincronizado (${calendar.scheduledDates.size} datas agendadas)."
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                error = e.toUserMessage("Erro ao sincronizar calendario da rota.")
            } finally {
                syncingCalendar = false
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
            IconButton(onClick = { component.goBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
            }
            Text("Detalhes da Rota", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = UbusPrimary)
            }
        } else {
            Spacer(Modifier.height(16.dp))
            
            if (error.isNotEmpty()) {
                Text(error, color = if (error.contains("sucesso")) UbusSuccess else UbusDestructive, modifier = Modifier.padding(bottom = 16.dp))
            }

            if (!BackendCapabilities.supportsManagerRouteCalendarScheduling || !BackendCapabilities.supportsManagerDriverAssignmentByRoute) {
                BentoCard(modifier = Modifier.padding(bottom = 16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "Calendario de viagens e atribuicao de motorista por rota em preparacao.",
                            style = MaterialTheme.typography.bodySmall,
                            color = UbusText3,
                        )
                        Text(
                            "Esses recursos serao ativados apos o backend publicar os novos endpoints.",
                            style = MaterialTheme.typography.bodySmall,
                            color = UbusText3,
                        )
                    }
                }
            }

            BentoCard {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    UbusTextField(value = name, onValueChange = { name = it }, label = "Nome da Rota")
                    UbusTextField(value = description, onValueChange = { description = it }, label = "Descrição")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        UbusTextField(value = votingOpenTime, onValueChange = { votingOpenTime = it }, label = "Abertura Votação", modifier = Modifier.weight(1f), placeholder = "06:00")
                        UbusTextField(value = votingCloseTime, onValueChange = { votingCloseTime = it }, label = "Fechamento Votação", modifier = Modifier.weight(1f), placeholder = "18:00")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            UbusButton(text = "Salvar Dados da Rota", onClick = { handleSave() }, loading = saving)
            if (BackendCapabilities.supportsManagerRouteCalendarScheduling) {
                Spacer(Modifier.height(12.dp))
                UbusButton(
                    text = "Sincronizar calendario",
                    onClick = { syncRouteCalendar() },
                    loading = syncingCalendar,
                )
            }

            Spacer(Modifier.height(32.dp))
            Text("Ônibus Atribuídos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            
            assignedBuses.forEach { bus ->
                BentoCard(modifier = Modifier.padding(bottom = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DirectionsBus, null, tint = UbusPrimary)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Nº ${bus.identificationNumber} · ${bus.plate}", style = MaterialTheme.typography.bodyMedium)
                            Text("${bus.standardCapacity} lugares", style = MaterialTheme.typography.bodySmall, color = UbusText3)
                        }
                        IconButton(onClick = { removeBus(bus.id) }) {
                            Icon(Icons.Default.Delete, null, tint = UbusDestructive)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Atribuir mais ônibus", style = MaterialTheme.typography.labelLarge, color = UbusText3)
            Spacer(Modifier.height(8.dp))
            
            val availableBuses = allBuses.filter { ab -> assignedBuses.none { it.id == ab.id } }
            availableBuses.forEach { bus ->
                BentoCard(modifier = Modifier.padding(bottom = 8.dp).clickable { assignBus(bus.id) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, null, tint = UbusPrimary)
                        Spacer(Modifier.width(12.dp))
                        Text("Nº ${bus.identificationNumber} (${bus.plate})", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}
