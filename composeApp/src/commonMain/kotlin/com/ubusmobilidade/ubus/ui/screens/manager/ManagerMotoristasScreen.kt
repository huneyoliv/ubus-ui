package com.ubusmobilidade.ubus.ui.screens.manager

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.UserRepository
import com.ubusmobilidade.ubus.data.model.RegistrationStatus
import com.ubusmobilidade.ubus.data.model.RoleUsuario
import com.ubusmobilidade.ubus.data.model.User
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.components.UbusButton
import com.ubusmobilidade.ubus.ui.components.UbusOutlinedButton
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary
import com.ubusmobilidade.ubus.ui.theme.UbusDestructive
import com.ubusmobilidade.ubus.ui.theme.UbusText3
import com.ubusmobilidade.ubus.ui.theme.UbusSuccess
import com.ubusmobilidade.ubus.ui.theme.UbusWarning
import com.ubusmobilidade.ubus.ui.util.toUserMessage
import kotlinx.coroutines.launch

@Composable
fun ManagerMotoristasScreen(component: RootComponent) {
    val scope = rememberCoroutineScope()
    val apiClient = remember { ApiClient(component.authStorage, onUnauthorized = { component.logout() }) }
    val userRepo = remember { UserRepository(apiClient) }
    var drivers by remember { mutableStateOf<List<User>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            drivers = userRepo.listUsers(
                municipalityId = component.authStorage.user?.municipalityId,
                role = RoleUsuario.DRIVER
            )
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            error = e.toUserMessage("Não foi possível carregar motoristas.")
        }
        loading = false
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
    ) {
        IconButton(onClick = { component.goBack() }, modifier = Modifier.padding(top = 8.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = MaterialTheme.colorScheme.onBackground)
        }
        Text(
            "Motoristas",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
        )
        Text(
            "Gerencie motoristas do sistema",
            style = MaterialTheme.typography.bodyMedium,
            color = UbusText3,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        UbusButton(
            text = "Cadastrar motorista",
            onClick = { component.navigateTo(RootComponent.Config.ManagerCadastroMotorista) },
            modifier = Modifier.padding(bottom = 20.dp),
        )

        if (loading) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = UbusPrimary)
            }
        } else if (error.isNotEmpty()) {
            BentoCard {
                Text(error, color = UbusDestructive, style = MaterialTheme.typography.bodyMedium)
            }
        } else if (drivers.isEmpty()) {
            BentoCard {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Person, null, tint = UbusText3, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Nenhum motorista encontrado", style = MaterialTheme.typography.titleMedium, color = UbusText3, textAlign = TextAlign.Center)
                }
            }
        } else {
            drivers.forEach { driver ->
                var processing by remember { mutableStateOf(false) }
                BentoCard(
                    modifier = Modifier.padding(bottom = 12.dp),
                    onClick = {
                        component.navigateTo(RootComponent.Config.ManagerMotoristaDetail(driver.id))
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, tint = UbusPrimary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(driver.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                            Text(driver.email, style = MaterialTheme.typography.bodySmall, color = UbusText3)
                            Text("CPF: ${driver.cpf}", style = MaterialTheme.typography.bodySmall, color = UbusText3)
                        }
                        val statusColor = when (driver.status) {
                            RegistrationStatus.PENDING -> UbusWarning
                            RegistrationStatus.APPROVED -> UbusSuccess
                            RegistrationStatus.REJECTED -> UbusDestructive
                            else -> UbusWarning
                        }
                        val statusLabel = when (driver.status) {
                            RegistrationStatus.PENDING -> "PENDENTE"
                            RegistrationStatus.APPROVED -> "APROVADO"
                            RegistrationStatus.REJECTED -> "REJEITADO"
                            else -> "PENDENTE"
                        }
                        Surface(
                            color = statusColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(
                                statusLabel,
                                color = statusColor,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                    }
                    if (driver.status == RegistrationStatus.PENDING || driver.status == null) {
                        Spacer(Modifier.height(12.dp))
                        if (processing) {
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = UbusPrimary, modifier = Modifier.size(24.dp))
                            }
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                UbusButton(
                                    text = "Aprovar",
                                    onClick = {
                                        processing = true
                                        scope.launch {
                                            try {
                                                userRepo.updateStatus(driver.id, RegistrationStatus.APPROVED)
                                                drivers = drivers.filter { it.id != driver.id }
                                            } catch (e: Exception) {
                                                if (e is kotlinx.coroutines.CancellationException) throw e
                                                error = e.toUserMessage("Não foi possível aprovar o motorista.")
                                            }
                                            processing = false
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                                UbusOutlinedButton(
                                    text = "Rejeitar",
                                    onClick = {
                                        processing = true
                                        scope.launch {
                                            try {
                                                userRepo.updateStatus(driver.id, RegistrationStatus.REJECTED)
                                                drivers = drivers.filter { it.id != driver.id }
                                            } catch (e: Exception) {
                                                if (e is kotlinx.coroutines.CancellationException) throw e
                                                error = e.toUserMessage("Não foi possível rejeitar o motorista.")
                                            }
                                            processing = false
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
