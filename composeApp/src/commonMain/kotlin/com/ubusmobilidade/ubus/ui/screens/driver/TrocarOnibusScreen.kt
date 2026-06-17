package com.ubusmobilidade.ubus.ui.screens.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.DriverRepository
import com.ubusmobilidade.ubus.data.model.Bus
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.components.UbusButton
import com.ubusmobilidade.ubus.ui.components.UbusOutlinedButton
import com.ubusmobilidade.ubus.ui.components.UbusTextField
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary
import com.ubusmobilidade.ubus.ui.theme.UbusText3
import com.ubusmobilidade.ubus.ui.theme.UbusDestructive
import com.ubusmobilidade.ubus.ui.theme.UbusWarning
import com.ubusmobilidade.ubus.ui.util.toUserMessage
import kotlinx.coroutines.launch
import com.ubusmobilidade.ubus.data.api.ApiError

sealed interface BusSearchState {
    object Idle : BusSearchState
    object Loading : BusSearchState
    data class Found(val bus: Bus) : BusSearchState
    object NotFound : BusSearchState
    data class Error(val message: String) : BusSearchState
}

@Composable
fun TrocarOnibusScreen(component: RootComponent, currentBusId: String? = null) {
    val scope = rememberCoroutineScope()
    val apiClient = remember { ApiClient(component.authStorage, onUnauthorized = { component.logout() }) }
    val driverRepo = remember { DriverRepository(apiClient) }

    var busNumber by remember { mutableStateOf("") }
    var busSearchState by remember { mutableStateOf<BusSearchState>(BusSearchState.Idle) }
    var swapping by remember { mutableStateOf(false) }
    var swapError by remember { mutableStateOf<String?>(null) }

    fun searchBus() {
        if (busNumber.isBlank()) return
        busSearchState = BusSearchState.Loading
        swapError = null
        scope.launch {
            try {
                val bus = driverRepo.getBusByNumber(busNumber.trim())
                busSearchState = BusSearchState.Found(bus)
            } catch (e: ApiError) {
                busSearchState = if (e.status == 404) {
                    BusSearchState.NotFound
                } else {
                    BusSearchState.Error(e.toUserMessage("Erro ao buscar ônibus."))
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                busSearchState = BusSearchState.Error("Não foi possível realizar a busca.")
            }
        }
    }

    fun confirmSwap(busId: String) {
        swapping = true
        swapError = null
        scope.launch {
            try {
                driverRepo.swapBus(busId)
                component.replaceWith(RootComponent.Config.DriverHome)
            } catch (e: ApiError) {
                swapError = when (e.status) {
                    409 -> "Este ônibus já está em outra viagem ativa."
                    403 -> "Sem viagem ativa para trocar o ônibus."
                    400 -> "Ônibus não pertence ao seu município."
                    else -> e.toUserMessage("Erro ao trocar ônibus.")
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                swapError = "Erro de conexão ao trocar ônibus."
            } finally {
                swapping = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        IconButton(onClick = { component.goBack() }, modifier = Modifier.padding(top = 8.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = MaterialTheme.colorScheme.onBackground)
        }
        Text(
            "Trocar ônibus",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        BentoCard(modifier = Modifier.padding(bottom = 16.dp)) {
            Column {
                Text(
                    "Informe o número de identificação do novo ônibus:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    UbusTextField(
                        value = busNumber,
                        onValueChange = { busNumber = it },
                        label = "Número do ônibus",
                        placeholder = "Ex: 20120",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(12.dp))
                    IconButton(
                        onClick = { searchBus() },
                        modifier = Modifier
                            .size(56.dp)
                            .background(UbusPrimary, MaterialTheme.shapes.small),
                    ) {
                        Icon(Icons.Default.Search, "Buscar", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }

        if (swapError != null) {
            BentoCard(modifier = Modifier.padding(bottom = 16.dp)) {
                Text(swapError!!, color = UbusDestructive, style = MaterialTheme.typography.bodyMedium)
            }
        }

        when (val state = busSearchState) {
            is BusSearchState.Loading -> {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = UbusPrimary)
                }
            }
            is BusSearchState.Error -> {
                BentoCard {
                    Text(state.message, color = UbusDestructive, style = MaterialTheme.typography.bodyMedium)
                }
            }
            is BusSearchState.NotFound -> {
                BentoCard {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Ônibus #$busNumber não encontrado",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Caso este veículo ainda não esteja cadastrado no sistema, você pode registrá-lo agora.",
                            style = MaterialTheme.typography.bodySmall,
                            color = UbusText3,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        UbusButton(
                            text = "Cadastrar este ônibus",
                            onClick = {
                                component.navigateTo(RootComponent.Config.CadastroVeiculoMultiStep(prefillNumber = busNumber))
                            }
                        )
                    }
                }
            }
            is BusSearchState.Found -> {
                val bus = state.bus
                BentoCard {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DirectionsBus, null, tint = UbusPrimary, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(
                                    "Ônibus #${bus.identificationNumber}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    bus.plate ?: "Sem placa",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = UbusText3
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        val amenities = mutableListOf<String>()
                        amenities.add("${bus.standardCapacity} assentos")
                        if (bus.hasBathroom) amenities.add("Banheiro ✓")
                        if (bus.hasElevator) amenities.add("Acessível ✓")
                        Text(
                            amenities.joinToString(" · "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )
                        UbusButton(
                            text = "Selecionar este ônibus",
                            loading = swapping,
                            onClick = { confirmSwap(bus.id) }
                        )
                    }
                }
            }
            else -> {}
        }
    }
}
