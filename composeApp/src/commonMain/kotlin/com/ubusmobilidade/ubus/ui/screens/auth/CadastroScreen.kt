package com.ubusmobilidade.ubus.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.AuthRepository
import com.ubusmobilidade.ubus.data.model.RegisterPayload
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.UbusButton
import com.ubusmobilidade.ubus.ui.components.UbusTextField
import com.ubusmobilidade.ubus.ui.theme.UbusBackground
import com.ubusmobilidade.ubus.ui.theme.UbusDestructive
import com.ubusmobilidade.ubus.ui.theme.UbusMutedForeground
import kotlinx.coroutines.launch

@Composable
fun CadastroScreen(component: RootComponent) {
    val scope = rememberCoroutineScope()
    val authRepo = remember {
        AuthRepository(ApiClient(component.authStorage, onUnauthorized = { component.logout() }))
    }

    var name by remember { mutableStateOf("") }
    var cpf by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var municipalityId by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    fun handleRegister() {
        if (name.isBlank() || cpf.isBlank() || email.isBlank() || password.isBlank()) {
            error = "Preencha todos os campos obrigatórios."
            return
        }
        if (password != confirmPassword) {
            error = "As senhas não coincidem."
            return
        }
        error = ""
        loading = true
        scope.launch {
            try {
                authRepo.register(
                    RegisterPayload(
                        municipalityId = municipalityId,
                        cpf = cpf.replace(Regex("\\D"), ""),
                        name = name.trim(),
                        email = email.trim(),
                        password = password,
                        phone = phone.ifBlank { null },
                    )
                )
                component.replaceWith(RootComponent.Config.Login)
            } catch (e: Exception) {
                error = "Erro ao cadastrar. Tente novamente."
            } finally {
                loading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UbusBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        IconButton(
            onClick = { component.goBack() },
            modifier = Modifier.padding(top = 48.dp),
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = MaterialTheme.colorScheme.onBackground)
        }

        Spacer(Modifier.height(16.dp))

        Text("Criar conta", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onBackground)

        Spacer(Modifier.height(8.dp))

        Text("Preencha seus dados para começar.", style = MaterialTheme.typography.bodyLarge, color = UbusMutedForeground)

        Spacer(Modifier.height(32.dp))

        UbusTextField(
            value = name, onValueChange = { name = it }, label = "Nome completo", placeholder = "Seu nome",
            leadingIcon = { Icon(Icons.Default.Person, null, tint = UbusMutedForeground, modifier = Modifier) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )
        Spacer(Modifier.height(16.dp))

        UbusTextField(
            value = cpf, onValueChange = { cpf = it }, label = "CPF", placeholder = "000.000.000-00",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        )
        Spacer(Modifier.height(16.dp))

        UbusTextField(
            value = email, onValueChange = { email = it }, label = "Email", placeholder = "seu@email.com",
            leadingIcon = { Icon(Icons.Default.Email, null, tint = UbusMutedForeground, modifier = Modifier) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        )
        Spacer(Modifier.height(16.dp))

        UbusTextField(
            value = phone, onValueChange = { phone = it }, label = "Telefone (opcional)", placeholder = "(00) 00000-0000",
            leadingIcon = { Icon(Icons.Default.Phone, null, tint = UbusMutedForeground, modifier = Modifier) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
        )
        Spacer(Modifier.height(16.dp))

        UbusTextField(
            value = password, onValueChange = { password = it }, label = "Senha", placeholder = "Mínimo 6 caracteres",
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = UbusMutedForeground, modifier = Modifier) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
        )
        Spacer(Modifier.height(16.dp))

        UbusTextField(
            value = confirmPassword, onValueChange = { confirmPassword = it }, label = "Confirmar senha", placeholder = "Repita a senha",
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = UbusMutedForeground, modifier = Modifier) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        )
        Spacer(Modifier.height(16.dp))

        if (error.isNotEmpty()) {
            Text(error, color = UbusDestructive, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(16.dp))
        }

        UbusButton(text = "Cadastrar", onClick = { handleRegister() }, loading = loading)

        Spacer(Modifier.height(32.dp))
    }
}
