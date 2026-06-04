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
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary
import com.ubusmobilidade.ubus.ui.theme.UbusDestructive
import com.ubusmobilidade.ubus.ui.theme.UbusText3
import com.ubusmobilidade.ubus.ui.theme.UbusSuccess
import com.ubusmobilidade.ubus.ui.util.toUserMessage
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
            pendingUsers = userRepo.listUsers(
                role = RoleUsuario.STUDENT,
                status = RegistrationStatus.PENDING,
            )
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            error = e.toUserMessage("Não foi possível carregar os cadastros pendentes.")
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
            "Validações",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
        )
        Text(
            "Aprove ou recuse cadastros pendentes",
            style = MaterialTheme.typography.bodyMedium,
            color = UbusText3,
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
        } else if (pendingUsers.isEmpty()) {
            BentoCard {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.CheckCircle, null, tint = UbusSuccess, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Nenhum cadastro pendente", style = MaterialTheme.typography.titleMedium, color = UbusText3, textAlign = TextAlign.Center)
                }
            }
        } else {
            pendingUsers.forEach { user ->
                var processing by remember { mutableStateOf(false) }
                BentoCard(modifier = Modifier.padding(bottom = 12.dp).clickable {
                    component.navigateTo(RootComponent.Config.ManagerStudentDetail(user.id))
                }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, tint = UbusPrimary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(user.name, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
                            Text("${user.email} · ${user.role}", style = MaterialTheme.typography.bodySmall, color = UbusText3)
                            Text("CPF: ${user.cpf}", style = MaterialTheme.typography.bodySmall, color = UbusText3)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    if (user.gradeFileUrl != null || user.residenciaFileUrl != null) {
                        Text("Documentos enviados:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        user.gradeFileUrl?.let { Text("Matrícula: $it", style = MaterialTheme.typography.bodySmall, color = UbusPrimary) }
                        user.residenciaFileUrl?.let { Text("Residência: $it", style = MaterialTheme.typography.bodySmall, color = UbusPrimary) }
                        Spacer(Modifier.height(12.dp))
                    }
                    if (processing) {
                        CircularProgressIndicator(color = UbusPrimary, modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally))
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
                                        } catch (e: Exception) {
                                            if (e is kotlinx.coroutines.CancellationException) throw e
                                            error = e.toUserMessage("Não foi possível aprovar o cadastro.")
                                        }
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
                                        } catch (e: Exception) {
                                            if (e is kotlinx.coroutines.CancellationException) throw e
                                            error = e.toUserMessage("Não foi possível recusar o cadastro.")
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
        Spacer(Modifier.height(16.dp))
    }
}
