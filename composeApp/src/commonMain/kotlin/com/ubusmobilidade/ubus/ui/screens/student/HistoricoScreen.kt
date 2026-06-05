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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.DirectionsBus
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
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.AppScaffold
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.components.StudentBottomNavBar
import com.ubusmobilidade.ubus.ui.components.StudentTab
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary
import com.ubusmobilidade.ubus.ui.theme.UbusText3
import com.ubusmobilidade.ubus.ui.util.toUserMessage
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.LocalDate
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun HistoricoScreen(component: RootComponent) {
    val scope = rememberCoroutineScope()
    val apiClient = remember { ApiClient(component.authStorage, onUnauthorized = { component.logout() }) }
    val reservationRepo = remember { ReservationRepository(apiClient) }
    var reservations by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var selectedFilter by remember { mutableStateOf("TODAS") }
    var resToCancel by remember { mutableStateOf<Reservation?>(null) }
    var cancelingId by remember { mutableStateOf<String?>(null) }

    val today = remember { kotlinx.datetime.LocalDate.parse(com.ubusmobilidade.ubus.ui.util.getTodayDateString()) }

    val filteredReservations = remember(reservations, selectedFilter) {
        when (selectedFilter) {
            "CONFIRMADO" -> reservations.filter {
                it.status == com.ubusmobilidade.ubus.data.model.ReservationStatus.CONFIRMED ||
                it.status == com.ubusmobilidade.ubus.data.model.ReservationStatus.PRESENT
            }
            "AUSENTE" -> reservations.filter {
                it.status == com.ubusmobilidade.ubus.data.model.ReservationStatus.ABSENT
            }
            else -> reservations
        }
    }

    LaunchedEffect(Unit) {
        try {
            reservations = reservationRepo.getMyReservations()
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            errorMessage = e.toUserMessage("Não foi possível carregar seu histórico.")
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
                        StudentTab.HISTORICO -> {}
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
                "Histórico de viagens",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 32.dp, bottom = 20.dp),
            )

            if (!successMessage.isNullOrBlank()) {
                BentoCard(
                    modifier = Modifier.padding(bottom = 16.dp),
                    borderColor = Color(0xFF22C55E).copy(alpha = 0.5f)
                ) {
                    Text(
                        successMessage!!,
                        color = Color(0xFF22C55E),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (loading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = UbusPrimary)
                }
            } else if (!errorMessage.isNullOrBlank()) {
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
                // Filtros de status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf("TODAS" to "Todas", "CONFIRMADO" to "Confirmadas", "AUSENTE" to "Ausências")
                    filters.forEach { (key, label) ->
                        val isSelected = selectedFilter == key
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) UbusPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedFilter = key }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (filteredReservations.isEmpty()) {
                    BentoCard {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.History, null, tint = UbusText3, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("Nenhum histórico encontrado", style = MaterialTheme.typography.titleMedium, color = UbusText3, textAlign = TextAlign.Center)
                        }
                    }
                } else {
                    filteredReservations.forEach { res ->
                        BentoCard(
                            modifier = Modifier.padding(bottom = 12.dp),
                            borderColor = UbusPrimary.copy(alpha = 0.1f)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(UbusPrimary.copy(alpha = 0.05f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DirectionsBus,
                                                contentDescription = null,
                                                tint = UbusPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = if (res.trip?.direction == com.ubusmobilidade.ubus.data.model.TripDirection.OUTBOUND) "Ida" else "Volta",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                            Text(
                                                text = "${res.trip?.tripDate ?: ""} · ${if (res.trip?.shift == "MORNING") "Manhã" else if (res.trip?.shift == "AFTERNOON") "Tarde" else "Noite"}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = UbusText3
                                            )
                                        }
                                    }
                                    com.ubusmobilidade.ubus.ui.components.StatusChip(status = res.status)
                                }

                                val tripDate = res.trip?.tripDate?.let { try { LocalDate.parse(it) } catch (e: Exception) { null } }
                                val isFutureOrToday = tripDate != null && tripDate >= today
                                val isConfirmed = res.status == com.ubusmobilidade.ubus.data.model.ReservationStatus.CONFIRMED

                                if (isConfirmed && isFutureOrToday) {
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        androidx.compose.material3.TextButton(
                                            onClick = { resToCancel = res },
                                            enabled = cancelingId != res.id,
                                            colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                                                contentColor = MaterialTheme.colorScheme.error
                                            )
                                        ) {
                                            Text(
                                                if (cancelingId == res.id) "Cancelando..." else "Cancelar",
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (resToCancel != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { resToCancel = null },
            title = { Text("Cancelar Reserva") },
            text = { Text("Deseja realmente cancelar esta reserva?") },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        val res = resToCancel ?: return@TextButton
                        resToCancel = null
                        scope.launch {
                            try {
                                cancelingId = res.id
                                reservationRepo.cancel(res.id)
                                reservations = reservationRepo.getMyReservations()
                                successMessage = "Reserva cancelada com sucesso!"
                                errorMessage = null
                            } catch (e: Exception) {
                                if (e is kotlinx.coroutines.CancellationException) throw e
                                errorMessage = e.toUserMessage("Não foi possível cancelar a reserva.")
                                successMessage = null
                            } finally {
                                cancelingId = null
                            }
                        }
                    }
                ) {
                    Text("Confirmar", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { resToCancel = null }) {
                    Text("Voltar")
                }
            }
        )
    }
}
