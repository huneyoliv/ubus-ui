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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material.icons.filled.Accessible
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.FleetRepository
import com.ubusmobilidade.ubus.data.api.ReservationRepository
import com.ubusmobilidade.ubus.data.api.TripRepository
import com.ubusmobilidade.ubus.data.model.BusCell
import com.ubusmobilidade.ubus.data.model.BusLayout
import com.ubusmobilidade.ubus.data.model.CellType
import com.ubusmobilidade.ubus.data.model.RoleUsuario
import com.ubusmobilidade.ubus.data.model.SeatNumberingMode
import com.ubusmobilidade.ubus.data.model.Trip
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.AppScaffold
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.components.BusCellState
import com.ubusmobilidade.ubus.ui.components.BusCellView
import com.ubusmobilidade.ubus.ui.components.SeatCell
import com.ubusmobilidade.ubus.ui.components.SeatState
import com.ubusmobilidade.ubus.ui.components.StudentBottomNavBar
import com.ubusmobilidade.ubus.ui.components.StudentTab
import com.ubusmobilidade.ubus.ui.components.UbusButton
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary
import com.ubusmobilidade.ubus.ui.theme.UbusText3
import com.ubusmobilidade.ubus.ui.util.NotificationScheduler
import com.ubusmobilidade.ubus.ui.util.toUserMessage

@Composable
fun SeatSelectionScreen(component: RootComponent, tripId: String, pendingInboundTripId: String? = null) {
    val scope = rememberCoroutineScope()
    val apiClient = remember { ApiClient(component.authStorage, onUnauthorized = { component.logout() }) }
    val reservationRepo = remember { ReservationRepository(apiClient) }
    val tripRepo = remember { TripRepository(apiClient) }
    val fleetRepo = remember { FleetRepository(apiClient) }
    val notificationScheduler = remember { NotificationScheduler() }

    var trip by remember { mutableStateOf<Trip?>(null) }
    var occupiedSeats by remember { mutableStateOf<List<Int>>(emptyList()) }
    var busLayout by remember { mutableStateOf<BusLayout?>(null) }
    var selectedSeat by remember { mutableStateOf<Int?>(null) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val fetchedTrip = tripRepo.getTrip(tripId)
            trip = fetchedTrip
            val occupied = reservationRepo.getOccupiedSeats(tripId)
            occupiedSeats = occupied.mapNotNull { it.seatNumber }

            val busId = fetchedTrip.busId.takeIf { it.isNotBlank() }
            if (busId != null) {
                busLayout = fleetRepo.getBusLayout(busId)
            }
            if (busLayout == null) {
                component.replaceWith(RootComponent.Config.SelecionarPontoEmbarque(tripId, null, pendingInboundTripId))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            e.printStackTrace()
            errorMessage = e.toUserMessage("Não foi possível carregar os assentos.")
        } finally {
            loading = false
        }
    }

    val seatsCount = 44
    val itemsListLegacy = remember {
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
                IconButton(onClick = { component.goBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                }
                Spacer(Modifier.width(8.dp))
                val activeTrip = trip
                Text(
                    text = if (activeTrip == null) "Escolher Assento" else "Escolher Assento${if (pendingInboundTripId != null || activeTrip.direction == com.ubusmobilidade.ubus.data.model.TripDirection.OUTBOUND) " (Ida)" else " (Volta)"}",
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

                Spacer(Modifier.height(12.dp))

                val hasBathroom = busLayout?.rows?.any { row -> row.cells.any { it.type == CellType.BATHROOM } } ?: false
                val hasBox = busLayout?.rows?.any { row -> row.cells.any { it.type == CellType.BOX } } ?: false

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFFF1F5F9)))
                        Spacer(Modifier.width(4.dp))
                        Text("Livre", fontSize = 10.sp, color = UbusText3)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(UbusPrimary))
                        Spacer(Modifier.width(4.dp))
                        Text("Selecionado", fontSize = 10.sp, color = UbusText3)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFFE2E8F0)))
                        Spacer(Modifier.width(4.dp))
                        Text("Ocupado", fontSize = 10.sp, color = UbusText3)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFFDCFCE7)))
                        Spacer(Modifier.width(4.dp))
                        Text("DPM", fontSize = 10.sp, color = UbusText3)
                    }
                    if (hasBox) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Accessible, null, tint = Color(0xFF6B21A8), modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(2.dp))
                            Text("Box", fontSize = 10.sp, color = UbusText3)
                        }
                    }
                    if (hasBathroom) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Wc, null, tint = Color(0xFF475569), modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(2.dp))
                            Text("WC", fontSize = 10.sp, color = UbusText3)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFF8FAFC))
                        .padding(16.dp)
                ) {
                    val activeLayout = busLayout
                    if (activeLayout != null) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                        ) {
                            activeLayout.rows.forEach { row ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    row.cells.forEach { cell ->
                                        val state = when {
                                            cell.type != CellType.SEAT -> BusCellState.DISABLED
                                            cell.virtualNumber == selectedSeat -> BusCellState.SELECTED
                                            occupiedSeats.contains(cell.virtualNumber) -> {
                                                if (cell.isDpm) BusCellState.DPM_OCCUPIED else BusCellState.OCCUPIED
                                            }
                                            cell.isDpm -> BusCellState.DPM
                                            else -> BusCellState.FREE
                                        }

                                        BusCellView(
                                            cell = cell,
                                            state = state,
                                            displayMode = activeLayout.numberingMode,
                                            onClick = if (state == BusCellState.FREE || state == BusCellState.DPM || state == BusCellState.SELECTED) {
                                                { selectedSeat = if (selectedSeat == cell.virtualNumber) null else cell.virtualNumber }
                                            } else null
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(5),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(itemsListLegacy) { seatNumber ->
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
                }

                Spacer(Modifier.height(16.dp))

                val buttonText = if (selectedSeat != null) {
                    val activeLayout = busLayout
                    val selectedCell = activeLayout?.rows?.flatMap { it.cells }?.find { it.virtualNumber == selectedSeat }
                    if (selectedCell != null) {
                        when (activeLayout.numberingMode) {
                            SeatNumberingMode.PHYSICAL -> "Avançar (Poltrona ${selectedCell.physicalNumber ?: selectedCell.virtualNumber})"
                            SeatNumberingMode.MIXED -> "Avançar (Assento #${selectedCell.virtualNumber}" + (selectedCell.physicalNumber?.let { " [Físico: $it]" } ?: "") + ")"
                            else -> "Avançar (Assento #$selectedSeat)"
                        }
                    } else {
                        "Avançar (Assento #$selectedSeat)"
                    }
                } else {
                    "Selecione um assento"
                }

                UbusButton(
                    text = buttonText,
                    enabled = selectedSeat != null,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    onClick = {
                        val seat = selectedSeat ?: return@UbusButton
                        component.replaceWith(RootComponent.Config.SelecionarPontoEmbarque(tripId, seat, pendingInboundTripId))
                    }
                )
            }
        }
    }
}
