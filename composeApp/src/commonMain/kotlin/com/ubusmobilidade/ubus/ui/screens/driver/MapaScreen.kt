package com.ubusmobilidade.ubus.ui.screens.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.DriverRepository
import com.ubusmobilidade.ubus.data.api.FleetRepository
import com.ubusmobilidade.ubus.data.api.TripRepository
import com.ubusmobilidade.ubus.data.model.DriverCurrentTripSummary
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.AppScaffold
import com.ubusmobilidade.ubus.ui.components.DriverBottomNavBar
import com.ubusmobilidade.ubus.ui.components.DriverTab
import com.ubusmobilidade.ubus.ui.components.MapPoint
import com.ubusmobilidade.ubus.ui.components.MapWebView
import com.ubusmobilidade.ubus.ui.components.UbusButton
import com.ubusmobilidade.ubus.ui.theme.UbusText3
import kotlinx.coroutines.launch

@Composable
fun MapaScreen(component: RootComponent) {
    val scope = rememberCoroutineScope()
    val apiClient = remember { ApiClient(component.authStorage, onUnauthorized = { component.logout() }) }
    val driverRepo = remember { DriverRepository(apiClient) }
    val tripRepo = remember { TripRepository(apiClient) }
    val fleetRepo = remember { FleetRepository(apiClient) }

    var summary by remember { mutableStateOf<DriverCurrentTripSummary?>(null) }
    var tripPoints by remember { mutableStateOf<List<MapPoint>>(emptyList()) }
    var driverLat by remember { mutableStateOf(-23.55052) }
    var driverLng by remember { mutableStateOf(-46.633308) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }
    var actionLoading by remember { mutableStateOf(false) }

    fun loadTripData() {
        loading = true
        error = ""
        scope.launch {
            try {
                val currentSummary = driverRepo.getCurrentTripSummary()
                summary = currentSummary
                val tripId = currentSummary.tripId
                if (tripId != null) {
                    try {
                        val loc = tripRepo.getLocation(tripId)
                        driverLat = loc.lat
                        driverLng = loc.lng
                    } catch (e: Exception) {
                    }

                    val trip = tripRepo.getTrip(tripId)
                    val isReturn = trip.direction == com.ubusmobilidade.ubus.data.model.TripDirection.INBOUND

                    val pickupList = try {
                        fleetRepo.listPickupPoints(trip.routeId)
                    } catch (_: Exception) {
                        emptyList()
                    }

                    val dropoffList = try {
                        fleetRepo.listDropoffPoints(trip.routeId)
                    } catch (_: Exception) {
                        emptyList()
                    }

                    val mappedPoints = mutableListOf<MapPoint>()

                    if (isReturn) {
                        val boarding = currentSummary.points.mapNotNull { summaryPoint ->
                            val detail = dropoffList.find { it.id == summaryPoint.pointId }
                            if (detail?.lat != null && detail.lng != null) {
                                MapPoint(
                                    lat = detail.lat,
                                    lng = detail.lng,
                                    label = "[Embarque] ${detail.name} (${summaryPoint.studentsCount} alunos)"
                                )
                            } else null
                        }
                        val alighting = pickupList.mapNotNull { detail ->
                            if (detail.lat != null && detail.lng != null) {
                                MapPoint(
                                    lat = detail.lat,
                                    lng = detail.lng,
                                    label = "[Desembarque] ${detail.name}"
                                )
                            } else null
                        }
                        mappedPoints.addAll(boarding)
                        mappedPoints.addAll(alighting)
                    } else {
                        val boarding = currentSummary.points.mapNotNull { summaryPoint ->
                            val detail = pickupList.find { it.id == summaryPoint.pointId }
                            if (detail?.lat != null && detail.lng != null) {
                                MapPoint(
                                    lat = detail.lat,
                                    lng = detail.lng,
                                    label = "[Embarque] ${detail.name} (${summaryPoint.studentsCount} alunos)"
                                )
                            } else null
                        }
                        val alighting = dropoffList.mapNotNull { detail ->
                            if (detail.lat != null && detail.lng != null) {
                                MapPoint(
                                    lat = detail.lat,
                                    lng = detail.lng,
                                    label = "[Desembarque] ${detail.name}"
                                )
                            } else null
                        }
                        mappedPoints.addAll(boarding)
                        mappedPoints.addAll(alighting)
                    }
                    tripPoints = mappedPoints
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                error = "Erro ao carregar dados da viagem."
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadTripData()
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
                },
            )
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (error.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(16.dp))
                    UbusButton(text = "Tentar Novamente", onClick = { loadTripData() })
                }
            } else {
                val currentSummary = summary
                val tripId = currentSummary?.tripId

                if (tripId == null) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            MapWebView(
                                lat = driverLat,
                                lng = driverLng,
                                points = emptyList(),
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = MaterialTheme.shapes.large,
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Map, null, tint = UbusText3, modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = "Nenhuma viagem ativa",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Selecione um veículo para iniciar o dia de trabalho.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = UbusText3
                                )
                                Spacer(Modifier.height(20.dp))
                                UbusButton(
                                    text = "Selecionar veículo",
                                    onClick = { component.replaceWith(RootComponent.Config.SelecionarVeiculo) }
                                )
                            }
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Viagem Ativa",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Fase: ${currentSummary.phase ?: "Em andamento"}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Navigation,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            MapWebView(
                                lat = driverLat,
                                lng = driverLng,
                                points = tripPoints,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    UbusButton(
                                        text = "Confirmar Partida",
                                        loading = actionLoading,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            actionLoading = true
                                            scope.launch {
                                                try {
                                                    driverRepo.notifyDeparting(tripId)
                                                    loadTripData()
                                                } catch (e: Exception) {
                                                    if (e is kotlinx.coroutines.CancellationException) throw e
                                                    error = e.message ?: "Erro ao confirmar partida."
                                                } finally {
                                                    actionLoading = false
                                                }
                                            }
                                        }
                                    )

                                    Button(
                                        onClick = {
                                            actionLoading = true
                                            scope.launch {
                                                try {
                                                    driverLat += 0.001
                                                    driverLng += 0.001
                                                    tripRepo.updateLocation(tripId, driverLat, driverLng)
                                                    loadTripData()
                                                } catch (e: Exception) {
                                                    if (e is kotlinx.coroutines.CancellationException) throw e
                                                    error = e.message ?: "Erro ao atualizar localização."
                                                } finally {
                                                    actionLoading = false
                                                }
                                            }
                                        },
                                        enabled = !actionLoading,
                                        modifier = Modifier.height(52.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondary
                                        )
                                    ) {
                                        Icon(Icons.Default.GpsFixed, contentDescription = "Atualizar GPS")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
