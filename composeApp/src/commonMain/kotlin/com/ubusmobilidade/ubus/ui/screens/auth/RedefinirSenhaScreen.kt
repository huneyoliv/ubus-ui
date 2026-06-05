package com.ubusmobilidade.ubus.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.AuthRepository
import com.ubusmobilidade.ubus.data.model.PasswordRedefinitionPayload
import com.ubusmobilidade.ubus.data.model.VerificationChannel
import com.ubusmobilidade.ubus.data.model.VerificationContext
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.VerificationChannelSelector
import com.ubusmobilidade.ubus.ui.components.UbusButton
import com.ubusmobilidade.ubus.ui.components.UbusTextField
import com.ubusmobilidade.ubus.ui.theme.UbusDestructive
import com.ubusmobilidade.ubus.ui.theme.UbusSuccess
import com.ubusmobilidade.ubus.ui.theme.UbusText3
import kotlinx.coroutines.launch

@Composable
fun RedefinirSenhaScreen(component: RootComponent) {
    val scope = rememberCoroutineScope()
    val authRepo = remember {
        AuthRepository(ApiClient(component.authStorage, onUnauthorized = { component.logout() }))
    }

    var step by remember { mutableStateOf(1) }
    var email by remember { mutableStateOf("") }
    var channel by remember { mutableStateOf(VerificationChannel.EMAIL) }
    var code by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var token by remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var success by remember { mutableStateOf(false) }

    fun sendCode() {
        if (email.isBlank() || !email.contains("@")) {
            error = "Informe um e-mail válido."
            return
        }
        error = ""
        loading = true
        scope.launch {
            try {
                authRepo.sendVerificationCode(email.trim(), channel, VerificationContext.RESET_PASSWORD)
                step = 2
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                error = e.message ?: "Erro ao enviar código de verificação."
            } finally {
                loading = false
            }
        }
    }

    fun verifyCode() {
        if (code.length != 6) {
            error = "O código deve ter 6 dígitos."
            return
        }
        error = ""
        loading = true
        scope.launch {
            try {
                val res = authRepo.verifyCode(email.trim(), code, channel, VerificationContext.RESET_PASSWORD)
                if (res.verified && res.token != null) {
                    token = res.token
                    step = 3
                } else {
                    error = res.message ?: "Código de verificação inválido."
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                error = e.message ?: "Erro ao verificar código."
            } finally {
                loading = false
            }
        }
    }

    fun resetPassword() {
        if (newPassword.isBlank() || confirmPassword.isBlank()) {
            error = "Preencha todos os campos de senha."
            return
        }
        if (newPassword != confirmPassword) {
            error = "As senhas não coincidem."
            return
        }
        error = ""
        loading = true
        scope.launch {
            try {
                authRepo.resetPassword(PasswordRedefinitionPayload(token, newPassword))
                success = true
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                error = e.message ?: "Erro ao redefinir senha."
            } finally {
                loading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
    ) {
        IconButton(
            onClick = {
                if (step > 1 && !success) {
                    step--
                    error = ""
                } else {
                    component.goBack()
                }
            },
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = MaterialTheme.colorScheme.onBackground)
        }

        Spacer(Modifier.height(16.dp))

        Text("Redefinir senha", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onBackground)

        Spacer(Modifier.height(8.dp))

        if (success) {
            Text(
                "Senha redefinida com sucesso! Você já pode entrar com sua nova senha.",
                style = MaterialTheme.typography.bodyLarge,
                color = UbusSuccess,
            )
            Spacer(Modifier.height(32.dp))
            UbusButton(text = "Ir para o login", onClick = { component.goBack() })
        } else {
            when (step) {
                1 -> {
                    Text(
                        "Informe seu e-mail e selecione o canal para receber o código.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = UbusText3,
                    )
                    Spacer(Modifier.height(24.dp))
                    UbusTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "E-mail",
                        placeholder = "seu@email.com",
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = UbusText3) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    )
                    Spacer(Modifier.height(20.dp))
                    VerificationChannelSelector(
                        selected = channel,
                        onSelect = { channel = it },
                        phone = "placeholder"
                    )
                    Spacer(Modifier.height(24.dp))
                    if (error.isNotEmpty()) {
                        Text(error, color = UbusDestructive, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(16.dp))
                    }
                    UbusButton(text = "Enviar código", onClick = { sendCode() }, loading = loading)
                }
                2 -> {
                    Text(
                        "Digite o código de 6 dígitos enviado para $email via ${channel.name}.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = UbusText3,
                    )
                    Spacer(Modifier.height(24.dp))
                    UbusTextField(
                        value = code,
                        onValueChange = { code = it.filter { c -> c.isDigit() }.take(6) },
                        label = "Código de verificação",
                        placeholder = "000000",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    )
                    Spacer(Modifier.height(24.dp))
                    if (error.isNotEmpty()) {
                        Text(error, color = UbusDestructive, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(16.dp))
                    }
                    UbusButton(text = "Verificar código", onClick = { verifyCode() }, loading = loading)
                }
                3 -> {
                    Text(
                        "Crie uma nova senha de acesso.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = UbusText3,
                    )
                    Spacer(Modifier.height(24.dp))
                    UbusTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = "Nova senha",
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = UbusText3) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                    )
                    Spacer(Modifier.height(16.dp))
                    UbusTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = "Confirmar nova senha",
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = UbusText3) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    )
                    Spacer(Modifier.height(24.dp))
                    if (error.isNotEmpty()) {
                        Text(error, color = UbusDestructive, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(16.dp))
                    }
                    UbusButton(text = "Salvar nova senha", onClick = { resetPassword() }, loading = loading)
                }
            }
        }
    }
}
