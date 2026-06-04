package com.ubusmobilidade.ubus.ui.screens.student

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
import androidx.compose.material.icons.filled.Lock
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
import com.ubusmobilidade.ubus.data.api.UserRepository
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.PasswordStrengthBar
import com.ubusmobilidade.ubus.ui.components.UbusButton
import com.ubusmobilidade.ubus.ui.components.UbusTextField
import com.ubusmobilidade.ubus.ui.theme.UbusDestructive
import com.ubusmobilidade.ubus.ui.theme.UbusSuccess
import com.ubusmobilidade.ubus.ui.theme.UbusText3
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AlterarSenhaScreen(component: RootComponent) {
    val scope = rememberCoroutineScope()
    val apiClient = remember { ApiClient(component.authStorage, onUnauthorized = { component.logout() }) }
    val userRepo = remember { UserRepository(apiClient) }

    var step by remember { mutableStateOf(1) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        IconButton(
            onClick = {
                if (step == 2) {
                    step = 1; newPassword = ""; confirmPassword = ""; message = ""
                } else {
                    component.goBack()
                }
            },
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = MaterialTheme.colorScheme.onBackground)
        }

        if (step == 1) {
            Text(
                "Verificar identidade",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
            )
            Text(
                "Informe sua senha atual para continuar.",
                style = MaterialTheme.typography.bodyMedium,
                color = UbusText3,
                modifier = Modifier.padding(bottom = 24.dp),
            )

            UbusTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = "Senha atual",
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = UbusText3) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )
            Spacer(Modifier.height(24.dp))

            if (message.isNotEmpty()) {
                Text(message, color = if (isError) UbusDestructive else UbusSuccess, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(16.dp))
            }

            UbusButton(
                text = "Verificar",
                loading = loading,
                enabled = currentPassword.isNotEmpty(),
                onClick = {
                    message = ""
                    step = 2
                },
            )
        } else {
            Text(
                "Nova senha",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
            )
            Text(
                "Escolha uma nova senha segura.",
                style = MaterialTheme.typography.bodyMedium,
                color = UbusText3,
                modifier = Modifier.padding(bottom = 24.dp),
            )

            UbusTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = "Nova senha",
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = UbusText3) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )
            PasswordStrengthBar(password = newPassword)
            Spacer(Modifier.height(16.dp))

            UbusTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirmar nova senha",
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = UbusText3) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = confirmPassword.isNotEmpty() && confirmPassword != newPassword,
                errorMessage = if (confirmPassword.isNotEmpty() && confirmPassword != newPassword) "Senhas não coincidem" else null,
            )
            Spacer(Modifier.height(24.dp))

            if (message.isNotEmpty()) {
                Text(message, color = if (isError) UbusDestructive else UbusSuccess, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(16.dp))
            }

            UbusButton(
                text = "Alterar senha",
                loading = loading,
                enabled = newPassword.length >= 6 && newPassword == confirmPassword,
                onClick = {
                    loading = true; message = ""
                    scope.launch {
                        try {
                            userRepo.changePassword(currentPassword, newPassword)
                            message = "Senha alterada com sucesso!"
                            isError = false
                            delay(2000)
                            component.goBack()
                        } catch (e: Exception) {
                            if (e is kotlinx.coroutines.CancellationException) throw e
                            message = "Senha incorreta ou erro ao alterar."
                            isError = true
                        }
                        loading = false
                    }
                },
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}
