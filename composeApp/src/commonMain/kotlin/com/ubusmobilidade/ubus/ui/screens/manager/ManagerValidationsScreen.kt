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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.UserRepository
import com.ubusmobilidade.ubus.data.model.RegistrationStatus
import com.ubusmobilidade.ubus.data.model.User
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.components.UbusButton
import com.ubusmobilidade.ubus.ui.theme.UbusAccent
import com.ubusmobilidade.ubus.ui.theme.UbusBackground
import com.ubusmobilidade.ubus.ui.theme.UbusDestructive
import com.ubusmobilidade.ubus.ui.theme.UbusMutedForeground
import com.ubusmobilidade.ubus.ui.theme.UbusSuccess
import kotlinx.coroutines.launch

@Composable
fun ManagerValidationsScreen(component: RootComponent) {
    val scope = rememberCoroutineScope()
    val apiClient = remember { ApiClient(component.authStorage, onUnauthorized = { component.logout() }) }
    val userRepo = remember { UserRepository(apiClient) }
    var pendingUsers by remember { mutableStateOf<List<User>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            pendingUsers = userRepo.listPending()
        } catch (e: Exception) {
            error = e.message ?: "Erro ao carregar"
        }
        loading = false
    }

    Column(
        modifier = Modifier.fillMaxSize().background(UbusBackground)
            .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
    ) {
        IconButton(onClick = { component.goBack() }, modifier = Modifier.padding(top = 8.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = MaterialTheme.colorScheme.onBackground)
        }
        Text(
            "Validações",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
        )
        Text(
            "Aprove ou recuse cadastros pendentes",
            style = MaterialTheme.typography.bodyMedium,
            color = UbusMutedForeground,
            modifier = Modifier.padding(bottom = 20.dp),
        )

        if (loading) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = UbusAccent)
            }
        } else if (error.isNotEmpty()) {
            BentoCard {
                Text(error, color = UbusDestructive, style = MaterialTheme.typography.bodyMedium)
            }
        } else if (pendingUsers.isEmpty()) {
            BentoCard {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.CheckCircle, null, tint = UbusSuccess, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Nenhum cadastro pendente", style = MaterialTheme.typography.titleMedium, color = UbusMutedForeground, textAlign = TextAlign.Center)
                }
            }
        } else {
            pendingUsers.forEach { user ->
                var processing by remember { mutableStateOf(false) }
                BentoCard(modifier = Modifier.padding(bottom = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, tint = UbusAccent, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(user.name, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
                            Text("${user.email} · ${user.role}", style = MaterialTheme.typography.bodySmall, color = UbusMutedForeground)
                            Text("CPF: ${user.cpf}", style = MaterialTheme.typography.bodySmall, color = UbusMutedForeground)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    if (processing) {
                        CircularProgressIndicator(color = UbusAccent, modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally))
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            UbusButton(
                                text = "Aprovar",
                                onClick = {
                                    processing = true
                                    scope.launch {
                                        try {
                                            userRepo.updateStatus(user.id, RegistrationStatus.APPROVED)
                                            pendingUsers = pendingUsers.filter { it.id != user.id }
                                        } catch (_: Exception) {}
                                        processing = false
                                    }
                                },
                                modifier = Modifier.weight(1f),
                            )
                            UbusButton(
                                text = "Recusar",
                                onClick = {
                                    processing = true
                                    scope.launch {
                                        try {
                                            userRepo.updateStatus(user.id, RegistrationStatus.REJECTED)
                                            pendingUsers = pendingUsers.filter { it.id != user.id }
                                        } catch (_: Exception) {}
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
        Spacer(Modifier.height(16.dp))
    }
}
