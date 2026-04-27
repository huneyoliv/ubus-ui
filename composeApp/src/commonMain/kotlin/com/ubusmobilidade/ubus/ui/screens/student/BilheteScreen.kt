package com.ubusmobilidade.ubus.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.ReservationRepository
import com.ubusmobilidade.ubus.data.model.Reservation
import com.ubusmobilidade.ubus.data.model.RoleUsuario
import com.ubusmobilidade.ubus.data.model.ReservationStatus
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.AppScaffold
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.components.StudentBottomNavBar
import com.ubusmobilidade.ubus.ui.components.StudentTab
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary
import com.ubusmobilidade.ubus.ui.theme.UbusPrimaryContainer
import com.ubusmobilidade.ubus.ui.theme.UbusText3
import com.ubusmobilidade.ubus.ui.theme.UbusSuccess
import com.ubusmobilidade.ubus.ui.util.toUserMessage

@Composable
fun BilheteScreen(component: RootComponent) {
    val apiClient = remember { ApiClient(component.authStorage, onUnauthorized = { component.logout() }) }
    val reservationRepo = remember { ReservationRepository(apiClient) }
    var activeReservation by remember { mutableStateOf<Reservation?>(null) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val reservations = reservationRepo.getMyReservations()
            activeReservation = reservations.firstOrNull { it.status == ReservationStatus.CONFIRMED }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            errorMessage = e.toUserMessage("Não foi possível carregar seu bilhete.")
        }
        loading = false
    }

    AppScaffold(
        bottomBar = {
            StudentBottomNavBar(
                selectedTab = StudentTab.HISTORICO,
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
            Text(
                "Bilhete",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 32.dp, bottom = 20.dp),
            )

            if (loading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
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
            } else if (activeReservation != null) {
                val res = activeReservation!!
                BentoCard {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        // QR Code placeholder
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center,
                        ) {
                            DenseQrPlaceholder(
                                value = "${res.id}-${res.trip?.tripId.orEmpty()}-${res.seatNumber ?: 0}",
                                modifier = Modifier.size(160.dp),
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        Text("BILHETE VÁLIDO", color = UbusSuccess, fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 2.sp)

                        Spacer(Modifier.height(8.dp))

                        Text(
                            res.trip?.shift ?: "Viagem",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )

                        Text(
                            res.trip?.tripDate ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = UbusText3,
                        )

                        if (res.seatNumber != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Assento ${res.seatNumber}",
                                style = MaterialTheme.typography.titleMedium,
                                color = UbusPrimary,
                            )
                        }
                    }
                }
            } else {
                BentoCard {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.ConfirmationNumber, null, tint = UbusText3, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Nenhum bilhete ativo",
                            style = MaterialTheme.typography.titleMedium,
                            color = UbusText3,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            "Reserve uma viagem para obter seu bilhete.",
                            style = MaterialTheme.typography.bodySmall,
                            color = UbusText3,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DenseQrPlaceholder(
    value: String,
    modifier: Modifier = Modifier,
    modules: Int = 33,
) {
    val safeModules = if (modules < 21) 21 else modules
    val hashes = value.fold(7) { acc, c -> (acc * 31) + c.code }
    Canvas(modifier = modifier.background(Color.White)) {
        val moduleSize = size.minDimension / safeModules
        for (x in 0 until safeModules) {
            for (y in 0 until safeModules) {
                val isFinder = (x < 7 && y < 7) ||
                    (x > safeModules - 8 && y < 7) ||
                    (x < 7 && y > safeModules - 8)
                val isOn = if (isFinder) {
                    val edge = x == 0 || y == 0 || x == 6 || y == 6
                    val center = x in 2..4 && y in 2..4
                    edge || center
                } else {
                    ((x * 17 + y * 31 + hashes) % 7) < 3
                }
                if (isOn) {
                    drawRect(
                        color = Color.Black,
                        topLeft = androidx.compose.ui.geometry.Offset(x * moduleSize, y * moduleSize),
                        size = androidx.compose.ui.geometry.Size(moduleSize, moduleSize),
                    )
                }
            }
        }
    }
}
