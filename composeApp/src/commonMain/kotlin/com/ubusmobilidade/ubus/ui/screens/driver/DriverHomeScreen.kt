package com.ubusmobilidade.ubus.ui.screens.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.DriverRepository
import com.ubusmobilidade.ubus.data.model.DriverCurrentTripSummary
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.AppScaffold
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.components.DriverBottomNavBar
import com.ubusmobilidade.ubus.ui.components.DriverTab
import com.ubusmobilidade.ubus.ui.components.UbusButton
import com.ubusmobilidade.ubus.ui.components.UbusOutlinedButton
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary
import com.ubusmobilidade.ubus.ui.theme.UbusText3
import com.ubusmobilidade.ubus.ui.theme.UbusWarning
import com.ubusmobilidade.ubus.ui.theme.UbusDestructive
import com.ubusmobilidade.ubus.ui.util.toUserMessage
import kotlinx.coroutines.launch

@Composable
fun DriverHomeScreen(component: RootComponent) {
    val scope = rememberCoroutineScope()
    val user = component.authStorage.user
    val apiClient = remember { ApiClient(component.authStorage, onUnauthorized = { component.logout() }) }
    val driverRepo = remember { DriverRepository(apiClient) }

    var summary by remember { mutableStateOf<DriverCurrentTripSummary?>(null) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var actionLoading by remember { mutableStateOf(false) }

    fun loadSummary() {
        loading = true
        errorMessage = null
        scope.launch {
            try {
                summary = driverRepo.getCurrentTripSummary()
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                errorMessage = e.toUserMessage("Não foi possível carregar os dados da escala.")
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadSummary()
    }

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
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Olá, ${user?.name ?: "Motorista"} 👋",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Bem-vindo ao seu painel de trabalho",
                        style = MaterialTheme.typography.bodyMedium,
                        color = UbusText3
                    )
                }
            }

            if (loading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = UbusPrimary)
                }
            } else if (errorMessage != null) {
                BentoCard {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(errorMessage!!, color = UbusDestructive, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(16.dp))
                        UbusButton(text = "Tentar novamente", onClick = { loadSummary() })
                    }
                }
            } else {
                val currentSummary = summary
                val noTrip = currentSummary == null || currentSummary.noTripToday || currentSummary.route == null

                if (noTrip) {
                    BentoCard(modifier = Modifier.padding(bottom = 16.dp)) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                        ) {
                            Icon(Icons.Default.Warning, null, tint = UbusWarning, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Sem escalas para hoje",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Você não tem nenhuma viagem escalada pelo gestor para a data de hoje.",
                                style = MaterialTheme.typography.bodySmall,
                                color = UbusText3,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(20.dp))
                            UbusButton(
                                text = "Selecionar veículo manualmente",
                                onClick = { component.navigateTo(RootComponent.Config.SelecionarVeiculo) }
                            )
                        }
                    }
                } else {
                    val route = currentSummary!!.route!!
                    val bus = currentSummary.bus
                    val phase = currentSummary.phase
                    val tripId = currentSummary.tripId

                    BentoCard(modifier = Modifier.padding(bottom = 16.dp)) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "VIAGEM DE HOJE",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = UbusPrimary
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                route.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            if (!route.description.isNullOrBlank()) {
                                Text(
                                    route.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = UbusText3
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            val isReturn = currentSummary.direction == com.ubusmobilidade.ubus.data.model.TripDirection.INBOUND
                            val directionText = if (isReturn) "Volta" else "Ida"
                            val shiftText = when (currentSummary.shift) {
                                "MORNING" -> "Manhã"
                                "AFTERNOON" -> "Tarde"
                                "NIGHT" -> "Noite"
                                else -> currentSummary.shift ?: ""
                            }
                            val departureTime = if (isReturn) route.departureTimeInbound else route.departureTimeOutbound

                            Text(
                                "Saída: ${departureTime ?: "—"} · $shiftText · $directionText",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    BentoCard(modifier = Modifier.padding(bottom = 16.dp)) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "ÔNIBUS ATRIBUÍDO",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = UbusPrimary
                            )
                            Spacer(Modifier.height(12.dp))
                            if (bus != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.DirectionsBus,
                                        null,
                                        tint = UbusPrimary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "#${bus.identificationNumber} · ${bus.plate ?: "Sem placa"}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        val amenities = mutableListOf<String>()
                                        amenities.add("${bus.standardCapacity} assentos")
                                        if (bus.hasBathroom) amenities.add("Banheiro ✓")
                                        if (bus.hasElevator) amenities.add("Acessível ✓")
                                        Text(
                                            amenities.joinToString(" · "),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = UbusText3
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    "Nenhum ônibus atribuído pelo gestor.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = UbusText3
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            UbusOutlinedButton(
                                text = "Trocar ônibus",
                                onClick = { component.navigateTo(RootComponent.Config.TrocarOnibus(bus?.id)) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    val totalStudents = currentSummary.points.sumOf { pt -> pt.studentsCount }
                    val totalPoints = currentSummary.points.size

                    BentoCard(modifier = Modifier.padding(bottom = 24.dp)) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "ALUNOS",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = UbusPrimary
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "$totalStudents confirmados em $totalPoints pontos",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(Modifier.height(12.dp))
                            UbusOutlinedButton(
                                text = "Ver mapa completo →",
                                onClick = { component.navigateTo(RootComponent.Config.Mapa) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    if (tripId != null) {
                        when (phase) {
                            "UPCOMING" -> {
                                UbusButton(
                                    text = "Confirmar Partida",
                                    loading = actionLoading,
                                    onClick = {
                                        actionLoading = true
                                        scope.launch {
                                            try {
                                                driverRepo.notifyDeparting(tripId)
                                                loadSummary()
                                            } catch (e: Exception) {
                                                if (e is kotlinx.coroutines.CancellationException) throw e
                                                errorMessage = e.toUserMessage("Erro ao confirmar partida.")
                                            } finally {
                                                actionLoading = false
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                                )
                            }
                            "ONGOING" -> {
                                UbusButton(
                                    text = "Abrir Mapa (Em Andamento)",
                                    onClick = { component.navigateTo(RootComponent.Config.Mapa) },
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
