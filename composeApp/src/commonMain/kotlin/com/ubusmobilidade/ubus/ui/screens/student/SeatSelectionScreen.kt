package com.ubusmobilidade.ubus.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.ubusmobilidade.ubus.ui.components.SeatCell
import com.ubusmobilidade.ubus.ui.components.SeatState
import com.ubusmobilidade.ubus.ui.components.StudentBottomNavBar
import com.ubusmobilidade.ubus.ui.components.StudentTab
import com.ubusmobilidade.ubus.ui.components.UbusButton
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary
import com.ubusmobilidade.ubus.ui.theme.UbusSuccess
import com.ubusmobilidade.ubus.ui.theme.UbusText3
import com.ubusmobilidade.ubus.ui.util.NotificationScheduler
import com.ubusmobilidade.ubus.ui.util.toUserMessage
import kotlinx.coroutines.launch

@Composable
fun SeatSelectionScreen(component: RootComponent, tripId: String) {
    val scope = rememberCoroutineScope()
    val apiClient = remember { ApiClient(component.authStorage, onUnauthorized = { component.logout() }) }
    val reservationRepo = remember { ReservationRepository(apiClient) }
    val tripRepo = remember { TripRepository(apiClient) }
    val notificationScheduler = remember { NotificationScheduler() }

    var trip by remember { mutableStateOf<Trip?>(null) }
    var occupiedSeats by remember { mutableStateOf<List<Int>>(emptyList()) }
    var selectedSeat by remember { mutableStateOf<Int?>(null) }
    var loading by remember { mutableStateOf(true) }
    var reserving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val fetchedTrip = tripRepo.getTrip(tripId)
            trip = fetchedTrip
            val occupied = reservationRepo.getOccupiedSeats(tripId)
            occupiedSeats = occupied.map { it.seatNumber }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            e.printStackTrace()
            errorMessage = e.toUserMessage("Não foi possível carregar os assentos.")
        }
        loading = false
    }

    val seatsCount = 44
    val itemsList = remember {
        val list = mutableListOf<Int?>()
        var seatNum = 1
        for (i in 0 until (seatsCount + seatsCount / 4)) {
            if (i % 5 == 2) {
                list.add(null)
            } else {
                list.add(seatNum++)
            }
        }
        list
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
                IconButton(onClick = { component.replaceWith(RootComponent.Config.Reservar) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Escolher Assento",
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
                            text = "${activeTrip.tripDate} · ${activeTrip.shift} · ${if (activeTrip.direction == com.ubusmobilidade.ubus.data.model.TripDirection.OUTBOUND) "Ida" else "Volta"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = UbusText3
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(16.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFF1F5F9)))
                        Spacer(Modifier.width(4.dp))
                        Text("Livre", fontSize = 11.sp, color = UbusText3)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(16.dp).clip(RoundedCornerShape(4.dp)).background(UbusPrimary))
                        Spacer(Modifier.width(4.dp))
                        Text("Selecionado", fontSize = 11.sp, color = UbusText3)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(16.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFE2E8F0)))
                        Spacer(Modifier.width(4.dp))
                        Text("Ocupado", fontSize = 11.sp, color = UbusText3)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(16.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFDCFCE7)))
                        Spacer(Modifier.width(4.dp))
                        Text("Acessível", fontSize = 11.sp, color = UbusText3)
                    }
                }

                Spacer(Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFF8FAFC))
                        .padding(16.dp)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(itemsList) { seatNumber ->
                            if (seatNumber == null) {
                                Box(Modifier.size(46.dp))
                            } else {
                                val isOccupied = occupiedSeats.contains(seatNumber)
                                val isSelected = selectedSeat == seatNumber
                                val isAccessible = seatNumber <= 4

                                val state = when {
                                    isSelected -> SeatState.SELECTED
                                    isOccupied -> SeatState.OCCUPIED
                                    isAccessible -> SeatState.ACCESSIBLE
                                    else -> SeatState.FREE
                                }

                                SeatCell(
                                    number = seatNumber,
                                    state = state,
                                    onClick = {
                                        selectedSeat = if (isSelected) null else seatNumber
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                UbusButton(
                    text = if (selectedSeat != null) "Avançar (Assento #$selectedSeat)" else "Selecione um assento",
                    enabled = selectedSeat != null,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    onClick = {
                        val seat = selectedSeat ?: return@UbusButton
                        component.replaceWith(RootComponent.Config.SelecionarPontoEmbarque(tripId, seat))
                    }
                )
            }
        }
    }
}
