package com.ubusmobilidade.ubus.ui.screens.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.FleetRepository
import com.ubusmobilidade.ubus.data.model.CreateBusPayload
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.UbusButton
import com.ubusmobilidade.ubus.ui.components.UbusTextField
import kotlinx.coroutines.launch

@Composable
fun CadastroVeiculoScreen(component: RootComponent) {
    var placa by remember { mutableStateOf("") }
    var modelo by remember { mutableStateOf("") }
    var capacidade by remember { mutableStateOf("") }
    var ano by remember { mutableStateOf("") }
    
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    val apiClient = remember { ApiClient(component.authStorage, onUnauthorized = { component.logout() }) }
    val fleetRepo = remember { FleetRepository(apiClient) }

    fun handleCadastro() {
        if (placa.isBlank() || modelo.isBlank() || capacidade.isBlank()) {
            error = "Preencha todos os campos obrigatórios."
            return
        }
        
        val capInt = capacidade.toIntOrNull()
        if (capInt == null || capInt <= 0) {
            error = "Capacidade deve ser um número maior que zero."
            return
        }

        scope.launch {
            println("DEBUG: CadastroVeiculoScreen - Registering vehicle: $placa")
            loading = true
            error = null
            try {
                fleetRepo.createBus(
                    CreateBusPayload(
                        municipalityId = component.authStorage.user?.municipalityId,
                        identificationNumber = modelo,
                        plate = placa,
                        standardCapacity = capInt
                    )
                )
                println("DEBUG: CadastroVeiculoScreen - Vehicle registered successfully")
                component.goBack()
            } catch (e: Exception) {
                println("DEBUG: CadastroVeiculoScreen - Error registering vehicle: ${e.message}")
                error = "Erro ao cadastrar veículo. Tente novamente."
            } finally {
                loading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        IconButton(onClick = { component.goBack() }, modifier = Modifier.padding(top = 8.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = MaterialTheme.colorScheme.onBackground)
        }
        Text(
            "Cadastrar veículo", 
            style = MaterialTheme.typography.displaySmall, 
            color = MaterialTheme.colorScheme.onBackground, 
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        if (error != null) {
            Text(
                error!!, 
                color = MaterialTheme.colorScheme.error, 
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        UbusTextField(value = placa, onValueChange = { placa = it }, label = "Placa")
        Spacer(Modifier.height(16.dp))
        UbusTextField(value = modelo, onValueChange = { modelo = it }, label = "Modelo/Identificação")
        Spacer(Modifier.height(16.dp))
        UbusTextField(value = capacidade, onValueChange = { capacidade = it }, label = "Capacidade")
        Spacer(Modifier.height(16.dp))
        UbusTextField(value = ano, onValueChange = { ano = it }, label = "Ano (Opcional)")
        Spacer(Modifier.height(24.dp))
        
        UbusButton(
            text = "Cadastrar", 
            loading = loading,
            onClick = { handleCadastro() }
        )
        Spacer(Modifier.height(32.dp))
    }
}
