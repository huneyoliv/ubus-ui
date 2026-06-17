package com.ubusmobilidade.ubus.ui.screens.manager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.FleetRepository
import com.ubusmobilidade.ubus.data.model.CreateRoutePayload
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.components.UbusButton
import com.ubusmobilidade.ubus.ui.components.UbusTextField
import com.ubusmobilidade.ubus.ui.theme.UbusDestructive
import com.ubusmobilidade.ubus.ui.util.toUserMessage
import kotlinx.coroutines.launch

@Composable
fun ManagerRouteFormScreen(component: RootComponent) {
    val scope = rememberCoroutineScope()
    val apiClient = remember { ApiClient(component.authStorage, onUnauthorized = { component.logout() }) }
    val fleetRepo = remember { FleetRepository(apiClient) }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var departureTimeOutbound by remember { mutableStateOf("06:30") }
    var departureTimeInbound by remember { mutableStateOf("18:00") }
    var votingOpenTime by remember { mutableStateOf("06:00") }
    var votingOpenDaysBefore by remember { mutableStateOf(0) }
    var votingCloseTime by remember { mutableStateOf("18:00") }

    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    fun handleSave() {
        if (name.isBlank()) {
            error = "O nome da rota é obrigatório."
            return
        }
        
        val timeRegex = "^([01]\\d|2[0-3]):[0-5]\\d$".toRegex()
        if (!departureTimeOutbound.matches(timeRegex) || !departureTimeInbound.matches(timeRegex)) {
            error = "Os horários de partida devem estar no formato HH:mm."
            return
        }
        if (departureTimeOutbound.isNotBlank() && (!votingOpenTime.matches(timeRegex) || !votingCloseTime.matches(timeRegex))) {
            error = "Os horários de votação devem estar no formato HH:mm."
            return
        }

        saving = true
        scope.launch {
            try {
                val newRoute = fleetRepo.createRoute(
                    CreateRoutePayload(
                        name = name,
                        description = description.ifBlank { null },
                        weekDays = listOf(1, 2, 3, 4, 5),
                        votingOpenTime = if (departureTimeOutbound.isNotBlank()) votingOpenTime else null,
                        votingCloseTime = if (departureTimeOutbound.isNotBlank()) votingCloseTime else null,
                        votingOpenDaysBefore = if (departureTimeOutbound.isNotBlank()) votingOpenDaysBefore else null,
                        departureTimeOutbound = departureTimeOutbound,
                        departureTimeInbound = departureTimeInbound
                    )
                )
                component.navigateTo(RootComponent.Config.ManagerRouteDetail(newRoute.id))
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                error = e.toUserMessage("Erro ao criar rota.")
            } finally {
                saving = false
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
            IconButton(onClick = { component.goBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
            }
            Text("Nova Rota", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))

        if (error.isNotEmpty()) {
            Text(error, color = UbusDestructive, modifier = Modifier.padding(bottom = 16.dp), fontSize = 14.sp)
        }

        BentoCard {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                UbusTextField(value = name, onValueChange = { name = it }, label = "Nome da Rota")
                UbusTextField(value = description, onValueChange = { description = it }, label = "Descrição (Opcional)")
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    UbusTextField(
                        value = departureTimeOutbound,
                        onValueChange = { departureTimeOutbound = it },
                        label = "Horário de Ida",
                        modifier = Modifier.weight(1f),
                        placeholder = "06:30"
                    )
                    UbusTextField(
                        value = departureTimeInbound,
                        onValueChange = { departureTimeInbound = it },
                        label = "Horário de Volta",
                        modifier = Modifier.weight(1f),
                        placeholder = "18:00"
                    )
                }

                if (departureTimeOutbound.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Configurações de Votação (Ida/Volta)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        UbusTextField(
                            value = votingOpenTime,
                            onValueChange = { votingOpenTime = it },
                            label = "Hora de Abertura",
                            modifier = Modifier.weight(1f),
                            placeholder = "06:00"
                        )
                        UbusTextField(
                            value = votingCloseTime,
                            onValueChange = { votingCloseTime = it },
                            label = "Hora de Fechamento",
                            modifier = Modifier.weight(1f),
                            placeholder = "18:00"
                        )
                    }
                    Column {
                        Text(
                            text = "Dia de abertura",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            BentoCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { votingOpenDaysBefore = 0 }
                                    .then(if (votingOpenDaysBefore == 0) Modifier.border(2.dp, UbusPrimary, MaterialTheme.shapes.large) else Modifier)
                            ) {
                                Text(
                                    text = "Mesmo dia",
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                    fontWeight = if (votingOpenDaysBefore == 0) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                            BentoCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { votingOpenDaysBefore = 1 }
                                    .then(if (votingOpenDaysBefore == 1) Modifier.border(2.dp, UbusPrimary, MaterialTheme.shapes.large) else Modifier)
                            ) {
                                Text(
                                    text = "Dia anterior",
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                    fontWeight = if (votingOpenDaysBefore == 1) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        UbusButton(text = "Criar Rota", onClick = { handleSave() }, loading = saving)
        Spacer(Modifier.height(32.dp))
    }
}
