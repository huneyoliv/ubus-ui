package com.ubusmobilidade.ubus.ui.screens.manager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.UserRepository
import com.ubusmobilidade.ubus.data.model.RegisterPayload
import com.ubusmobilidade.ubus.data.model.RegistrationStatus
import com.ubusmobilidade.ubus.data.model.User
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.components.UbusButton
import com.ubusmobilidade.ubus.ui.components.UbusTextField
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
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    
    // Form states
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var cpf by remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        try {
            val drivers = userRepo.listUsers(role = com.ubusmobilidade.ubus.data.model.RoleUsuario.DRIVER)
            val u = drivers.find { it.id == userId }
            if (u != null) {
                user = u
                name = u.name
                email = u.email
                phone = u.phone ?: ""
                cpf = u.cpf
            } else {
                error = "Motorista não encontrado."
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            error = e.toUserMessage("Não foi possível carregar o motorista.")
        }
        loading = false
    }

    fun handleSave() {
        saving = true
        scope.launch {
            try {
                userRepo.updateUser(userId, RegisterPayload(
                    municipalityId = user?.municipalityId ?: "",
                    cpf = cpf,
                    name = name,
                    email = email,
                    password = "", // API should ignore password if blank in update
                    phone = phone,
                    role = user?.role
                ))
                component.goBack()
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                error = e.toUserMessage("Erro ao salvar alterações.")
            }
            saving = false
        }
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

            BentoCard {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    UbusTextField(value = name, onValueChange = { name = it }, label = "Nome Completo")
                    UbusTextField(value = email, onValueChange = { email = it }, label = "E-mail", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
                    UbusTextField(value = cpf, onValueChange = { cpf = it }, label = "CPF", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    UbusTextField(value = phone, onValueChange = { phone = it }, label = "Telefone", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                }
            }

            Spacer(Modifier.height(24.dp))

            UbusButton(text = "Salvar Alterações", onClick = { handleSave() }, loading = saving)
            
            Spacer(Modifier.height(12.dp))
            
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
        Spacer(Modifier.height(32.dp))
    }
}
