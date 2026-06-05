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

        saving = true
        scope.launch {
            try {
                val newRoute = fleetRepo.createRoute(
                    CreateRoutePayload(
                        name = name,
                        description = description.ifBlank { null },
                        weekDays = listOf(1, 2, 3, 4, 5),
                        votingOpenTime = "06:00",
                        votingCloseTime = "18:00"
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

        Spacer(Modifier.height(32.dp))
        UbusButton(text = "Criar Rota", onClick = { handleSave() }, loading = saving)
        Spacer(Modifier.height(32.dp))
    }
}
