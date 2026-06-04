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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.ReservationRepository
import com.ubusmobilidade.ubus.data.model.Reservation
import com.ubusmobilidade.ubus.data.model.RoleUsuario
import com.ubusmobilidade.ubus.data.model.ReservationStatus
import com.ubusmobilidade.ubus.data.storage.LocalTicketStorage
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.AppScaffold
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.components.DenseQr
import com.ubusmobilidade.ubus.ui.components.OfflineBadge
import com.ubusmobilidade.ubus.ui.components.QrCodeZoomDialog
import com.ubusmobilidade.ubus.ui.components.StudentBottomNavBar
import com.ubusmobilidade.ubus.ui.components.StudentTab
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary
import com.ubusmobilidade.ubus.ui.theme.UbusText3
import com.ubusmobilidade.ubus.ui.theme.UbusSuccess
import com.ubusmobilidade.ubus.ui.util.toUserMessage

@Composable
fun SmartTicketScreen(component: RootComponent) {
    val apiClient = remember { ApiClient(component.authStorage, onUnauthorized = { component.logout() }) }
    val reservationRepo = remember { ReservationRepository(apiClient) }
    val localTicketStorage = remember { LocalTicketStorage() }

    var activeReservation by remember { mutableStateOf<Reservation?>(null) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isOfflineData by remember { mutableStateOf(false) }
    var showZoomDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val reservations = reservationRepo.getMyReservations()
            val active = reservations.firstOrNull { it.status == ReservationStatus.CONFIRMED }
            if (active != null) {
                activeReservation = active
                localTicketStorage.latestTicket = active
                isOfflineData = false
            } else {
                activeReservation = null
                localTicketStorage.clear()
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            val cached = localTicketStorage.latestTicket
            if (cached != null) {
                activeReservation = cached
                isOfflineData = true
            } else {
                errorMessage = e.toUserMessage("Não foi possível carregar seu bilhete.")
            }
        }
        loading = false
    }

    val qrValue = activeReservation?.let {
        "${it.id}-${it.tripId}-${it.seatNumber ?: 0}"
    } ?: ""

    if (showZoomDialog && qrValue.isNotEmpty()) {
        QrCodeZoomDialog(
            value = qrValue,
            onDismiss = { showZoomDialog = false }
        )
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Bilhete Inteligente",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (isOfflineData && !loading) {
                    OfflineBadge()
                }
            }

            if (loading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = UbusPrimary)
                }
            } else if (errorMessage != null && activeReservation == null) {
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
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .clickable { showZoomDialog = true },
                            contentAlignment = Alignment.Center,
                        ) {
                            DenseQr(
                                value = qrValue,
                                modifier = Modifier.size(160.dp),
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            "Toque para ampliar",
                            color = UbusText3,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

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
