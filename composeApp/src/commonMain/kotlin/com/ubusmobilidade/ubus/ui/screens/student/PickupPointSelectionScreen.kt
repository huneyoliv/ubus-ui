package com.ubusmobilidade.ubus.ui.screens.student

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.FleetRepository
import com.ubusmobilidade.ubus.data.api.ReservationRepository
import com.ubusmobilidade.ubus.data.api.TripRepository
import com.ubusmobilidade.ubus.data.model.CreateReservationPayload
import com.ubusmobilidade.ubus.data.model.PickupPoint
import com.ubusmobilidade.ubus.data.model.RoleUsuario
import com.ubusmobilidade.ubus.data.model.Trip
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.AppScaffold
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.components.PickupPointTimeline
import com.ubusmobilidade.ubus.ui.components.StudentBottomNavBar
import com.ubusmobilidade.ubus.ui.components.StudentTab
import com.ubusmobilidade.ubus.ui.components.UbusButton
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary
import com.ubusmobilidade.ubus.ui.theme.UbusText3
import com.ubusmobilidade.ubus.ui.util.NotificationScheduler
import com.ubusmobilidade.ubus.ui.util.toUserMessage
import kotlinx.coroutines.launch

@Composable
fun PickupPointSelectionScreen(
    component: RootComponent,
    tripId: String,
    seatNumber: Int
) {
    val scope = rememberCoroutineScope()
    val apiClient = remember { ApiClient(component.authStorage, onUnauthorized = { component.logout() }) }
    val reservationRepo = remember { ReservationRepository(apiClient) }
    val tripRepo = remember { TripRepository(apiClient) }
    val fleetRepo = remember { FleetRepository(apiClient) }
    val notificationScheduler = remember { NotificationScheduler() }

    var trip by remember { mutableStateOf<Trip?>(null) }
    var points by remember { mutableStateOf<List<PickupPoint>>(emptyList()) }
    var selectedPoint by remember { mutableStateOf<PickupPoint?>(null) }
    var loading by remember { mutableStateOf(true) }
    var reserving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val fetchedTrip = tripRepo.getTrip(tripId)
            trip = fetchedTrip
            val fetchedPoints = fleetRepo.listPickupPoints(fetchedTrip.routeId)
            points = fetchedPoints

            val defaultPointId = component.authStorage.user?.defaultPointId
            if (!defaultPointId.isNullOrBlank()) {
                selectedPoint = fetchedPoints.find { it.id == defaultPointId }
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            errorMessage = e.toUserMessage("Não foi possível carregar os pontos de embarque.")
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
                        StudentTab.RESERVAR -> component.replaceWith(RootComponent.Config.Reservar)
                        StudentTab.LIDER -> component.replaceWith(RootComponent.Config.Lider)
                        StudentTab.HISTORICO -> component.replaceWith(RootComponent.Config.Historico)
                        StudentTab.PERFIL -> component.replaceWith(RootComponent.Config.Perfil)
                    }
                },
            )
        },
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { component.replaceWith(RootComponent.Config.SelecionarAssento(tripId)) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Ponto de Embarque",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = UbusPrimary)
                }
            } else if (errorMessage != null) {
                BentoCard {
                    Text(
                        errorMessage!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                val activeTrip = trip!!

                BentoCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(
                            text = activeTrip.route?.name ?: "Viagem",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Assento Escolhido: #$seatNumber",
                            style = MaterialTheme.typography.bodySmall,
                            color = UbusPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Selecione o ponto onde irá embarcar:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = UbusText3,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (points.isEmpty()) {
                        Text(
                            text = "Nenhum ponto de embarque cadastrado para esta rota.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = UbusText3,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    } else {
                        PickupPointTimeline(
                            points = points,
                            selectedPointId = selectedPoint?.id,
                            onPointSelect = { selectedPoint = it }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                UbusButton(
                    text = if (selectedPoint != null) "Confirmar Reserva" else "Selecione um ponto",
                    enabled = selectedPoint != null && !reserving,
                    loading = reserving,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    onClick = {
                        val point = selectedPoint ?: return@UbusButton
                        reserving = true
                        scope.launch {
                            try {
                                val payload = CreateReservationPayload(
                                    tripId = tripId,
                                    pickupPointId = point.id,
                                    seatNumber = seatNumber
                                )
                                val reservation = reservationRepo.create(payload)
                                val reservationWithTrip = if (reservation.trip == null) {
                                    reservation.copy(trip = activeTrip)
                                } else {
                                    reservation
                                }
                                notificationScheduler.scheduleEmbarkAlert(reservationWithTrip, 60)
                                notificationScheduler.scheduleEmbarkAlert(reservationWithTrip, 30)

                                component.replaceWith(RootComponent.Config.StudentHome)
                            } catch (e: Exception) {
                                if (e is kotlinx.coroutines.CancellationException) throw e
                                errorMessage = e.toUserMessage("Erro ao concluir a reserva.")
                                reserving = false
                            }
                        }
                    }
                )
            }
        }
    }
}
