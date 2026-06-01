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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.ReservationRepository
import com.ubusmobilidade.ubus.data.api.TripRepository
import com.ubusmobilidade.ubus.data.model.Reservation
import com.ubusmobilidade.ubus.data.model.Trip
import com.ubusmobilidade.ubus.data.model.RoleUsuario
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.AppScaffold
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.components.StudentBottomNavBar
import com.ubusmobilidade.ubus.ui.components.StudentTab
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary
import com.ubusmobilidade.ubus.ui.theme.UbusText3
import com.ubusmobilidade.ubus.ui.theme.UbusSuccess
import com.ubusmobilidade.ubus.ui.util.toUserMessage

import com.ubusmobilidade.ubus.data.api.BackendCapabilities
import com.ubusmobilidade.ubus.data.api.TripRatingRepository
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Star

@Composable
fun HomeScreen(component: RootComponent) {
    val user = component.authStorage.user
    val firstName = user?.name?.split(" ")?.firstOrNull() ?: ""
    val initials = user?.name
        ?.split(" ")
        ?.mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
        ?.take(2)
        ?.joinToString("") ?: "?"

    var openTrips by remember { mutableStateOf<List<Trip>>(emptyList()) }
    var myReservations by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    var pendingRatings by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }

    val apiClient = remember { ApiClient(component.authStorage, onUnauthorized = { component.logout() }) }
    val tripRepo = remember { TripRepository(apiClient) }
    val reservationRepo = remember { ReservationRepository(apiClient) }
    val tripRatingRepo = remember { TripRatingRepository(apiClient) }

    LaunchedEffect(Unit) {
        try {
            try {
                openTrips = tripRepo.getOpenTrips()
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                openTrips = emptyList()
                loadError = e.toUserMessage("Não foi possível carregar os dados da tela inicial.")
            }
            try {
                myReservations = reservationRepo.getMyReservations()
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                myReservations = emptyList()
                if (loadError == null) {
                    loadError = e.toUserMessage("Não foi possível carregar os dados da tela inicial.")
                }
            }
            try {
                if (BackendCapabilities.supportsTripRating) {
                    pendingRatings = tripRatingRepo.listPendingRatings()
                }
            } catch (_: Exception) {}
        } finally {
            loading = false
        }
    }

    AppScaffold(
        bottomBar = {
            StudentBottomNavBar(
                selectedTab = StudentTab.HOME,
                showLeaderTab = component.authStorage.user?.role == RoleUsuario.LEADER,
                onTabSelected = { tab ->
                    when (tab) {
                        StudentTab.HOME -> {}
                        StudentTab.RESERVAR -> component.navigateTo(RootComponent.Config.Reservar)
                        StudentTab.LIDER -> component.navigateTo(RootComponent.Config.Lider)
                        StudentTab.HISTORICO -> component.navigateTo(RootComponent.Config.Historico)
                        StudentTab.PERFIL -> component.navigateTo(RootComponent.Config.Perfil)
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
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(UbusPrimary, MaterialTheme.colorScheme.tertiary)
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(initials, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Bem-vindo de volta", style = MaterialTheme.typography.bodySmall, color = UbusText3)
                    Text(firstName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                }
            }

            if (loading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = UbusPrimary)
                }
            } else {
                if (!loadError.isNullOrBlank()) {
                    BentoCard(modifier = Modifier.padding(bottom = 12.dp)) {
                        Text(
                            loadError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }

                if (BackendCapabilities.supportsTripRating && pendingRatings.isNotEmpty()) {
                    val pending = pendingRatings.first()
                    BentoCard(
                        modifier = Modifier
                            .clickable {
                                component.navigateTo(
                                    RootComponent.Config.AvaliarViagem(
                                        reservationId = pending.id,
                                        tripId = pending.tripId
                                    )
                                )
                            }
                            .padding(bottom = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Como foi sua viagem de ontem?",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    "Ajude-nos a melhorar avaliando sua viagem de ${pending.trip?.tripDate ?: ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = UbusText3
                                )
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = UbusText3, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                // Quick action: Reserve
                BentoCard(modifier = Modifier.clickable { component.navigateTo(RootComponent.Config.Reservar) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DirectionsBus, null, tint = UbusPrimary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Reservar viagem", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                            Text(
                                "${openTrips.size} viagens disponíveis",
                                style = MaterialTheme.typography.bodySmall,
                                color = UbusText3,
                            )
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = UbusText3, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Active reservations
                if (myReservations.isNotEmpty()) {
                    Text(
                        "Suas reservas",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
                    )
                    myReservations.take(3).forEach { reservation ->
                        BentoCard(modifier = Modifier.padding(bottom = 8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, null, tint = UbusSuccess, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        reservation.trip?.shift ?: "Viagem",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onBackground,
                                    )
                                    Text(
                                        reservation.trip?.tripDate ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = UbusText3,
                                    )
                                }
                            }
                        }
                    }
                } else {
                    BentoCard {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.CalendarMonth, null, tint = UbusText3, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Nenhuma reserva ativa", style = MaterialTheme.typography.bodyMedium, color = UbusText3)
                            Text("Reserve uma viagem para começar", style = MaterialTheme.typography.bodySmall, color = UbusText3)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
