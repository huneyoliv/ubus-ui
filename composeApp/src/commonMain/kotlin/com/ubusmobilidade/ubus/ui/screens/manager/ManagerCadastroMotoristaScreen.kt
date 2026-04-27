package com.ubusmobilidade.ubus.ui.screens.manager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.AuthRepository
import com.ubusmobilidade.ubus.data.model.RegisterPayload
import com.ubusmobilidade.ubus.data.model.RoleUsuario
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.components.PasswordStrengthBar
import com.ubusmobilidade.ubus.ui.components.UbusButton
import com.ubusmobilidade.ubus.ui.components.UbusTextField
import com.ubusmobilidade.ubus.ui.theme.UbusDestructive
import com.ubusmobilidade.ubus.ui.theme.UbusSuccess
import com.ubusmobilidade.ubus.ui.theme.UbusText3
import com.ubusmobilidade.ubus.ui.util.CpfVisualTransformation
import com.ubusmobilidade.ubus.ui.util.PhoneVisualTransformation
import com.ubusmobilidade.ubus.ui.util.isValidCpf
import com.ubusmobilidade.ubus.ui.util.toUserMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ManagerCadastroMotoristaScreen(component: RootComponent) {
    val scope = rememberCoroutineScope()
    val apiClient = remember { ApiClient(component.authStorage, onUnauthorized = { component.logout() }) }
    val authRepo = remember { AuthRepository(apiClient) }

    var name by remember { mutableStateOf("") }
    var cpf by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var success by remember { mutableStateOf(false) }

    val cpfDigits = cpf.filter { it.isDigit() }
    val cpfValid = cpfDigits.length == 11 && isValidCpf(cpfDigits)
    val formValid = name.isNotBlank() && cpfValid && email.isNotBlank() && password.length >= 6

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
    ) {
        IconButton(onClick = { component.goBack() }, modifier = Modifier.padding(top = 8.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = MaterialTheme.colorScheme.onBackground)
        }
        Text(
            "Cadastrar motorista",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
        )
        Text(
            "Preencha os dados do novo motorista",
            style = MaterialTheme.typography.bodyMedium,
            color = UbusText3,
            modifier = Modifier.padding(bottom = 20.dp),
        )

        if (success) {
            BentoCard {
                Text("Motorista cadastrado com sucesso!", color = UbusSuccess, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(16.dp))
            return@Column
        }

        UbusTextField(
            value = name,
            onValueChange = { name = it },
            label = "Nome",
            placeholder = "Nome completo",
            leadingIcon = { Icon(Icons.Default.Person, null) },
        )
        Spacer(Modifier.height(12.dp))

        UbusTextField(
            value = cpf,
            onValueChange = { if (it.filter { c -> c.isDigit() }.length <= 11) cpf = it },
            label = "CPF",
            placeholder = "000.000.000-00",
            visualTransformation = CpfVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = cpf.isNotEmpty() && cpfDigits.length == 11 && !cpfValid,
            errorMessage = if (cpf.isNotEmpty() && cpfDigits.length == 11 && !cpfValid) "CPF inválido" else null,
        )
        Spacer(Modifier.height(12.dp))

        UbusTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            placeholder = "email@exemplo.com",
            leadingIcon = { Icon(Icons.Default.Email, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        Spacer(Modifier.height(12.dp))

        UbusTextField(
            value = phone,
            onValueChange = { if (it.filter { c -> c.isDigit() }.length <= 11) phone = it },
            label = "Telefone (opcional)",
            placeholder = "(00) 00000-0000",
            leadingIcon = { Icon(Icons.Default.Phone, null) },
            visualTransformation = PhoneVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        )
        Spacer(Modifier.height(12.dp))

        UbusTextField(
            value = password,
            onValueChange = { password = it },
            label = "Senha",
            placeholder = "Mínimo 6 caracteres",
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = password.isNotEmpty() && password.length < 6,
            errorMessage = if (password.isNotEmpty() && password.length < 6) "Mínimo 6 caracteres" else null,
        )
        if (password.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            PasswordStrengthBar(password)
        }
        Spacer(Modifier.height(20.dp))

        if (error.isNotEmpty()) {
            BentoCard(modifier = Modifier.padding(bottom = 12.dp)) {
                Text(error, color = UbusDestructive, style = MaterialTheme.typography.bodySmall)
            }
        }

        UbusButton(
            text = "Cadastrar",
            enabled = formValid && !loading,
            loading = loading,
            onClick = {
                loading = true
                error = ""
                scope.launch {
                    try {
                        authRepo.register(
                            RegisterPayload(
                                municipalityId = component.authStorage.user?.municipalityId ?: "",
                                cpf = cpfDigits,
                                name = name.trim(),
                                email = email.trim(),
                                password = password,
                                phone = phone.filter { it.isDigit() }.ifBlank { null },
                                role = RoleUsuario.DRIVER,
                            )
                        )
                        success = true
                        delay(2000)
                        component.goBack()
                    } catch (e: Exception) {
                        if (e is kotlinx.coroutines.CancellationException) throw e
                        error = e.toUserMessage("Não foi possível cadastrar o motorista. Tente novamente.")
                    }
                    loading = false
                }
            },
        )
        Spacer(Modifier.height(24.dp))
    }
}
