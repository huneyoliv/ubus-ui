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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.TripRatingRepository
import com.ubusmobilidade.ubus.data.api.TripRepository
import com.ubusmobilidade.ubus.data.model.CreateTripRatingPayload
import com.ubusmobilidade.ubus.data.model.RoleUsuario
import com.ubusmobilidade.ubus.data.model.Trip
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.AppScaffold
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.components.RatingStars
import com.ubusmobilidade.ubus.ui.components.StudentBottomNavBar
import com.ubusmobilidade.ubus.ui.components.StudentTab
import com.ubusmobilidade.ubus.ui.components.UbusButton
import com.ubusmobilidade.ubus.ui.components.UbusTextField
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary
import com.ubusmobilidade.ubus.ui.theme.UbusText3
import com.ubusmobilidade.ubus.ui.util.toUserMessage
import kotlinx.coroutines.launch

@Composable
fun TripRatingScreen(
    component: RootComponent,
    reservationId: String,
    tripId: String
) {
    val scope = rememberCoroutineScope()
    val apiClient = remember { ApiClient(component.authStorage, onUnauthorized = { component.logout() }) }
    val tripRepo = remember { TripRepository(apiClient) }
    val tripRatingRepo = remember { TripRatingRepository(apiClient) }

    var trip by remember { mutableStateOf<Trip?>(null) }
    var cleanlinessRating by remember { mutableStateOf(5) }
    var punctualityRating by remember { mutableStateOf(5) }
    var driverRating by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var sending by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            trip = tripRepo.getTrip(tripId)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            errorMessage = e.toUserMessage("Não foi possível carregar os detalhes da viagem.")
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { component.replaceWith(RootComponent.Config.Historico) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Avaliar Viagem",
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
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                val activeTrip = trip!!

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
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

                    Spacer(Modifier.height(24.dp))

                    Text(
                        "Como foi sua viagem?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    BentoCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(
                                "Limpeza do Veículo",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(Modifier.height(8.dp))
                            RatingStars(
                                rating = cleanlinessRating,
                                onRatingChange = { cleanlinessRating = it }
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    BentoCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(
                                "Pontualidade",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(Modifier.height(8.dp))
                            RatingStars(
                                rating = punctualityRating,
                                onRatingChange = { punctualityRating = it }
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    BentoCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(
                                "Motorista",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(Modifier.height(8.dp))
                            RatingStars(
                                rating = driverRating,
                                onRatingChange = { driverRating = it }
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    UbusTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = "Comentários ou sugestões (opcional)",
                        placeholder = "Diga-nos como melhorar...",
                        singleLine = false,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(24.dp))
                }

                UbusButton(
                    text = "Enviar Avaliação",
                    enabled = !sending,
                    loading = sending,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    onClick = {
                        sending = true
                        scope.launch {
                            try {
                                val payload = CreateTripRatingPayload(
                                    reservationId = reservationId,
                                    tripId = tripId,
                                    cleanlinessRating = cleanlinessRating,
                                    punctualityRating = punctualityRating,
                                    driverRating = driverRating,
                                    comment = comment.takeIf { it.isNotBlank() }
                                )
                                tripRatingRepo.createRating(payload)
                                component.replaceWith(RootComponent.Config.StudentHome)
                            } catch (e: Exception) {
                                if (e is kotlinx.coroutines.CancellationException) throw e
                                errorMessage = e.toUserMessage("Não foi possível enviar sua avaliação.")
                                sending = false
                            }
                        }
                    }
                )
            }
        }
    }
}
