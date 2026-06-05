package com.ubusmobilidade.ubus.ui.screens.manager

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalUriHandler
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.UserRepository
import com.ubusmobilidade.ubus.data.model.RegistrationStatus
import com.ubusmobilidade.ubus.data.model.RoleUsuario
import com.ubusmobilidade.ubus.data.model.User
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.components.UbusButton
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary
import com.ubusmobilidade.ubus.ui.theme.UbusBorder
import com.ubusmobilidade.ubus.ui.theme.UbusDestructive
import com.ubusmobilidade.ubus.ui.theme.UbusText3
import com.ubusmobilidade.ubus.ui.util.toUserMessage
import kotlinx.coroutines.launch

@Composable
fun ManagerStudentDetailScreen(component: RootComponent, userId: String) {
    val scope = rememberCoroutineScope()
    val apiClient = remember { ApiClient(component.authStorage, onUnauthorized = { component.logout() }) }
    val userRepo = remember { UserRepository(apiClient) }
    
    var user by remember { mutableStateOf<User?>(null) }
    var loading by remember { mutableStateOf(true) }
    var processing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        try {
            user = userRepo.getUser(userId)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            error = e.toUserMessage("Não foi possível carregar os dados do aluno.")
        }
        loading = false
    }

    fun updateStatus(status: RegistrationStatus) {
        processing = true
        scope.launch {
            try {
                userRepo.updateStatus(userId, status)
                component.goBack()
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                error = e.toUserMessage("Erro ao atualizar status.")
            }
            processing = false
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
            Text("Validação de Aluno", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = UbusPrimary)
            }
        } else if (user != null) {
            val u = user!!
            Spacer(Modifier.height(16.dp))

            // Photo
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                if (u.photoUrl != null) {
                    // Placeholder for real image loading
                    Box(Modifier.size(120.dp).clip(CircleShape).background(UbusPrimary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                        Text("FOTO", color = UbusPrimary)
                    }
                } else {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(120.dp).clip(CircleShape).background(UbusBorder), tint = UbusText3)
                }
            }

            Spacer(Modifier.height(24.dp))

            BentoCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow("Nome", u.name)
                    DetailRow("E-mail", u.email)
                    DetailRow("CPF", u.cpf)
                    DetailRow("Telefone", u.phone ?: "Não informado")
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Documentos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            if (u.gradeFileUrl != null) {
                DocumentCard("Comprovante de Matrícula", u.gradeFileUrl)
            }
            if (u.residenciaFileUrl != null) {
                DocumentCard("Comprovante de Residência", u.residenciaFileUrl)
            }
            if (u.gradeFileUrl == null && u.residenciaFileUrl == null) {
                Text("Nenhum documento enviado.", color = UbusText3)
            }

            Spacer(Modifier.height(32.dp))

            if (processing) {
                CircularProgressIndicator(color = UbusPrimary, modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    UbusButton(text = "Aprovar", onClick = { updateStatus(RegistrationStatus.APPROVED) }, modifier = Modifier.weight(1f))
                    Button(
                        onClick = { updateStatus(RegistrationStatus.REJECTED) },
                        colors = ButtonDefaults.buttonColors(containerColor = UbusDestructive),
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Recusar")
                    }
                }
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = UbusText3)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun DocumentCard(label: String, url: String) {
    val uriHandler = LocalUriHandler.current
    BentoCard(
        modifier = Modifier.padding(bottom = 8.dp),
        onClick = {
            try {
                uriHandler.openUri(url)
            } catch (_: Exception) {}
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Description, null, tint = UbusPrimary)
            Spacer(Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Icon(Icons.Default.OpenInNew, null, tint = UbusText3, modifier = Modifier.size(16.dp))
        }
    }
}
