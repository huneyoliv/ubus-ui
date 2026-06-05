package com.ubusmobilidade.ubus.ui.screens.manager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.UserRepository
import com.ubusmobilidade.ubus.data.model.User
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.theme.UbusDestructive
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary
import com.ubusmobilidade.ubus.ui.theme.UbusText3
import com.ubusmobilidade.ubus.ui.util.toUserMessage
import kotlinx.coroutines.launch

@Composable
fun ManagerMotoristaDetailScreen(component: RootComponent, userId: String) {
    val scope = rememberCoroutineScope()
    val apiClient = remember { ApiClient(component.authStorage, onUnauthorized = { component.logout() }) }
    val userRepo = remember { UserRepository(apiClient) }
    
    var user by remember { mutableStateOf<User?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        try {
            user = userRepo.getUser(userId)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            error = e.toUserMessage("Não foi possível carregar o motorista.")
        }
        loading = false
    }

    fun handleDelete() {
        loading = true
        scope.launch {
            try {
                userRepo.deleteUser(userId)
                component.goBack()
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                error = e.toUserMessage("Erro ao excluir motorista.")
                loading = false
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
            Text("Detalhes do Motorista", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = UbusPrimary)
            }
        } else {
            Spacer(Modifier.height(16.dp))
            
            if (error.isNotEmpty()) {
                Text(error, color = UbusDestructive, modifier = Modifier.padding(bottom = 16.dp))
            }

            user?.let { u ->
                BentoCard {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        DetailRow("Nome Completo", u.name)
                        DetailRow("E-mail", u.email)
                        DetailRow("CPF", u.cpf)
                        DetailRow("Telefone", u.phone ?: "Não informado")
                    }
                }

                Spacer(Modifier.height(32.dp))
                
                Button(
                    onClick = { handleDelete() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = UbusDestructive),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Excluir Motorista")
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
