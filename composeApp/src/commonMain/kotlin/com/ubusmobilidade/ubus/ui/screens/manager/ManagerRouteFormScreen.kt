package com.ubusmobilidade.ubus.ui.screens.manager

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.FleetRepository
import com.ubusmobilidade.ubus.data.model.CreateRoutePayload
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.components.UbusButton
import com.ubusmobilidade.ubus.ui.components.UbusTextField
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary
import com.ubusmobilidade.ubus.ui.theme.UbusDestructive
import com.ubusmobilidade.ubus.ui.theme.UbusSuccess
import com.ubusmobilidade.ubus.ui.theme.UbusText3
import com.ubusmobilidade.ubus.ui.util.toUserMessage
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ManagerRouteFormScreen(component: RootComponent) {
    val scope = rememberCoroutineScope()
    val apiClient = remember { ApiClient(component.authStorage, onUnauthorized = { component.logout() }) }
    val fleetRepo = remember { FleetRepository(apiClient) }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var departureTimeOutbound by remember { mutableStateOf("06:30") }
    var departureTimeInbound by remember { mutableStateOf("18:00") }
    var selectedWeekDays by remember { mutableStateOf(setOf(1, 2, 3, 4, 5)) } // Seg a Sex por padrão

    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    val daysOfWeek = listOf(
        1 to "Seg",
        2 to "Ter",
        3 to "Qua",
        4 to "Qui",
        5 to "Sex",
        6 to "Sáb",
        0 to "Dom"
    )

    fun handleSave() {
        if (name.isBlank()) {
            error = "O nome da rota é obrigatório."
            return
        }
        if (selectedWeekDays.isEmpty()) {
            error = "Selecione pelo menos um dia de operação."
            return
        }
        
        val timeRegex = "^([01]\\d|2[0-3]):[0-5]\\d$".toRegex()
        if (!departureTimeOutbound.matches(timeRegex) || !departureTimeInbound.matches(timeRegex)) {
            error = "Os horários de partida devem estar no formato HH:mm."
            return
        }

        saving = true
        scope.launch {
            try {
                val newRoute = fleetRepo.createRoute(
                    CreateRoutePayload(
                        name = name,
                        description = description.ifBlank { null },
                        weekDays = selectedWeekDays.toList().sorted(),
                        votingOpenTime = "06:00",
                        votingCloseTime = "18:00",
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
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Dias de Operação", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        BentoCard {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                daysOfWeek.forEach { (day, label) ->
                    val isSelected = selectedWeekDays.contains(day)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) UbusPrimary else Color(0xFFF1F5F9))
                            .clickable {
                                selectedWeekDays = if (isSelected) {
                                    selectedWeekDays - day
                                } else {
                                    selectedWeekDays + day
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else UbusText3,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        UbusButton(text = "Criar Rota", onClick = { handleSave() }, loading = saving)
        Spacer(Modifier.height(32.dp))
    }
}
