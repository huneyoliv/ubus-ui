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
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.BackendCapabilities
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
    var loading by remember { mutableStateOf(true) }
    var reservingId by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            trips = tripRepo.getOpenTrips()
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
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 32.dp, bottom = 20.dp),
            )

            if (successMessage.isNotEmpty()) {
                BentoCard(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text(successMessage, color = UbusSuccess, style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (errorMessage.isNotEmpty()) {
                BentoCard(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (!BackendCapabilities.supportsPickupPointConfirmationInReservation) {
                BentoCard(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text(
                        "Confirmacao de ponto de embarque na reserva sera ativada apos atualizacao da API. No momento, a reserva segue com o fluxo padrao.",
                        color = UbusText3,
                        style = MaterialTheme.typography.bodySmall,
                    )
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
                trips.forEach { trip ->
                    BentoCard(modifier = Modifier.padding(bottom = 12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DirectionsBus, null, tint = UbusPrimary, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    trip.route?.name ?: "Rota",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                                Text(
                                    "${trip.tripDate} · ${trip.shift} · ${trip.direction}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = UbusText3,
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        UbusButton(
                            text = "Reservar",
                            loading = reservingId == trip.tripId,
                            onClick = {
                                reservingId = trip.tripId
                                scope.launch {
                                    try {
                                        val defaultPointId = component.authStorage.user?.defaultPointId
                                        val reservation = if (
                                            BackendCapabilities.supportsPickupPointConfirmationInReservation &&
                                            !defaultPointId.isNullOrBlank()
                                        ) {
                                            reservationRepo.createWithPickupPoint(
                                                tripId = trip.tripId,
                                                pickupPointId = defaultPointId,
                                            )
                                        } else {
                                            reservationRepo.create(CreateReservationPayload(tripId = trip.tripId))
                                        }
                                        val reservationWithTrip = if (reservation.trip == null) {
                                            reservation.copy(trip = trip)
                                        } else {
                                            reservation
                                        }
                                        notificationScheduler.scheduleEmbarkAlert(reservationWithTrip, 60)
                                        notificationScheduler.scheduleEmbarkAlert(reservationWithTrip, 30)
                                        successMessage = "Reserva confirmada!"
                                        errorMessage = ""
                                    } catch (e: Exception) {
                                        if (e is kotlinx.coroutines.CancellationException) throw e
                                        errorMessage = e.toUserMessage("Não foi possível concluir a reserva.")
                                    }
                                    reservingId = null
                                }
                            },
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
