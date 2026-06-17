package com.ubusmobilidade.ubus.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.ReservationRepository
import com.ubusmobilidade.ubus.data.api.TripRepository
import com.ubusmobilidade.ubus.data.model.CreateReservationPayload
import com.ubusmobilidade.ubus.data.model.RoleUsuario
import com.ubusmobilidade.ubus.data.model.Trip
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.AppScaffold
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.components.StudentBottomNavBar
import com.ubusmobilidade.ubus.ui.components.StudentTab
import com.ubusmobilidade.ubus.ui.components.UbusButton
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary
import com.ubusmobilidade.ubus.ui.theme.UbusText3
import com.ubusmobilidade.ubus.ui.theme.UbusSuccess
import com.ubusmobilidade.ubus.ui.util.NotificationScheduler
import com.ubusmobilidade.ubus.ui.util.toUserMessage
import kotlinx.coroutines.launch

@Composable
fun ReservarScreen(component: RootComponent) {
    val scope = rememberCoroutineScope()
    val apiClient = remember { ApiClient(component.authStorage, onUnauthorized = { component.logout() }) }
    val notificationScheduler = remember { NotificationScheduler() }
    val tripRepo = remember { TripRepository(apiClient) }
    val reservationRepo = remember { ReservationRepository(apiClient) }

    var trips by remember { mutableStateOf<List<Trip>>(emptyList()) }
    var occupiedSeatsMap by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var loading by remember { mutableStateOf(true) }
    var reservingId by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            val fetchedTrips = tripRepo.getOpenTrips()
            trips = fetchedTrips
            val map = fetchedTrips.associate { trip ->
                trip.tripId to try {
                    reservationRepo.getOccupiedSeats(trip.tripId).size
                } catch (e: Exception) {
                    0
                }
            }
            occupiedSeatsMap = map
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            errorMessage = e.toUserMessage("Não foi possível carregar as viagens disponíveis.")
        }
        loading = false
    }

    AppScaffold(
        bottomBar = {
            StudentBottomNavBar(
                selectedTab = StudentTab.RESERVAR,
                showLeaderTab = component.authStorage.user?.role == RoleUsuario.LEADER,
                onTabSelected = { tab ->
                    when (tab) {
                        StudentTab.HOME -> component.replaceWith(RootComponent.Config.StudentHome)
                        StudentTab.RESERVAR -> {}
                        StudentTab.LIDER -> component.replaceWith(RootComponent.Config.Lider)
                        StudentTab.HISTORICO -> component.replaceWith(RootComponent.Config.Historico)
                        StudentTab.PERFIL -> component.replaceWith(RootComponent.Config.Perfil)
                    }
                },
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Text(
                "Reservar viagem",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 32.dp, bottom = 20.dp),
            )

            if (successMessage.isNotEmpty()) {
                BentoCard(
                    modifier = Modifier.padding(bottom = 16.dp),
                    borderColor = UbusSuccess.copy(alpha = 0.5f)
                ) {
                    Text(successMessage, color = UbusSuccess, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }

            if (errorMessage.isNotEmpty()) {
                BentoCard(
                    modifier = Modifier.padding(bottom = 16.dp),
                    borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                ) {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (loading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = UbusPrimary)
                }
            } else if (trips.isEmpty()) {
                BentoCard {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Schedule, null, tint = UbusText3, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Nenhuma viagem disponível", style = MaterialTheme.typography.bodyMedium, color = UbusText3)
                    }
                }
            } else {
                val groupedTrips = remember(trips) {
                    trips.groupBy { Triple(it.routeId ?: "", it.tripDate, it.shift) }
                }

                groupedTrips.forEach { (key, tripsInGroup) ->
                    val (routeId, tripDate, shift) = key
                    val outboundTrip = tripsInGroup.find { it.direction == com.ubusmobilidade.ubus.data.model.TripDirection.OUTBOUND }
                    val inboundTrip = tripsInGroup.find { it.direction == com.ubusmobilidade.ubus.data.model.TripDirection.INBOUND }
                    val firstTrip = tripsInGroup.first()

                    BentoCard(
                        modifier = Modifier.padding(bottom = 16.dp),
                        borderColor = UbusPrimary.copy(alpha = 0.15f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(UbusPrimary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.DirectionsBus, null, tint = UbusPrimary, modifier = Modifier.size(22.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    firstTrip.route?.name ?: "Rota Escolar",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                                Text(
                                    "$tripDate · ${if (shift == "MORNING") "Manhã" else if (shift == "AFTERNOON") "Tarde" else "Noite"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = UbusText3,
                                )
                            }
                        }

                        val capacity = tripsInGroup.sumOf { if (it.realCapacity > 0) it.realCapacity else 40 }
                        val occupiedSeatsCount = tripsInGroup.sumOf { trip ->
                            occupiedSeatsMap[trip.tripId] ?: 0
                        }
                        val availableSeats = capacity - occupiedSeatsCount
                        val occupancyRatio = occupiedSeatsCount.toFloat() / capacity.toFloat()

                        Column(modifier = Modifier.padding(vertical = 12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Ocupação: $occupiedSeatsCount / $capacity assentos",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = UbusText3
                                )
                                Text(
                                    text = "$availableSeats livres",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (availableSeats > 5) UbusSuccess else Color(0xFFEF4444)
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            androidx.compose.material3.LinearProgressIndicator(
                                progress = { occupancyRatio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (occupancyRatio > 0.85f) Color(0xFFEF4444) else UbusPrimary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        UbusButton(
                            text = "Selecionar Viagem",
                            onClick = {
                                component.navigateTo(
                                    RootComponent.Config.SelecionarDirecao(
                                        routeId = routeId,
                                        tripDate = tripDate,
                                        shift = shift,
                                        outboundTripId = outboundTrip?.tripId,
                                        inboundTripId = inboundTrip?.tripId
                                    )
                                )
                            },
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
