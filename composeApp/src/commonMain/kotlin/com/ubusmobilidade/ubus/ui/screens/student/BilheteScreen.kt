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
import androidx.compose.material.icons.filled.QrCode2
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.ReservationRepository
import com.ubusmobilidade.ubus.data.model.Reservation
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.AppScaffold
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.components.StudentBottomNavBar
import com.ubusmobilidade.ubus.ui.components.StudentTab
import com.ubusmobilidade.ubus.ui.theme.UbusAccent
import com.ubusmobilidade.ubus.ui.theme.UbusAccentContainer
import com.ubusmobilidade.ubus.ui.theme.UbusMutedForeground
import com.ubusmobilidade.ubus.ui.theme.UbusSuccess

@Composable
fun BilheteScreen(component: RootComponent) {
    val apiClient = remember { ApiClient(component.authStorage, onUnauthorized = { component.logout() }) }
    val reservationRepo = remember { ReservationRepository(apiClient) }
    var activeReservation by remember { mutableStateOf<Reservation?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val reservations = reservationRepo.getMyReservations()
            activeReservation = reservations.firstOrNull { it.status.name == "CONFIRMED" }
        } catch (_: Exception) {}
        loading = false
    }

    AppScaffold(
        bottomBar = {
            StudentBottomNavBar(
                selectedTab = StudentTab.BILHETE,
                onTabSelected = { tab ->
                    when (tab) {
                        StudentTab.HOME -> component.replaceWith(RootComponent.Config.StudentHome)
                        StudentTab.RESERVAR -> component.replaceWith(RootComponent.Config.Reservar)
                        StudentTab.BILHETE -> {}
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
                    CircularProgressIndicator(color = UbusAccent)
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
                                .background(UbusAccentContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.QrCode2, null, tint = UbusAccent, modifier = Modifier.size(120.dp))
                        }

                        Spacer(Modifier.height(20.dp))

                        Text("BILHETE VÁLIDO", color = UbusSuccess, fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 2.sp)

                        Spacer(Modifier.height(8.dp))

                        Text(
                            res.viagem?.turno ?: "Viagem",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )

                        Text(
                            res.viagem?.dataViagem ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = UbusMutedForeground,
                        )

                        if (res.numeroAssento != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Assento ${res.numeroAssento}",
                                style = MaterialTheme.typography.titleMedium,
                                color = UbusAccent,
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
                        Icon(Icons.Default.ConfirmationNumber, null, tint = UbusMutedForeground, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Nenhum bilhete ativo",
                            style = MaterialTheme.typography.titleMedium,
                            color = UbusMutedForeground,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            "Reserve uma viagem para obter seu bilhete.",
                            style = MaterialTheme.typography.bodySmall,
                            color = UbusMutedForeground,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}
