package com.ubusmobilidade.ubus.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import com.ubusmobilidade.ubus.ui.components.UbusButton
import com.ubusmobilidade.ubus.ui.components.UbusTextField
import com.ubusmobilidade.ubus.ui.theme.UbusBackground
import com.ubusmobilidade.ubus.ui.theme.UbusDestructive
import com.ubusmobilidade.ubus.ui.theme.UbusMutedForeground
import com.ubusmobilidade.ubus.ui.theme.UbusSuccess
import kotlinx.coroutines.launch

@Composable
fun AlterarSenhaScreen(component: RootComponent) {
    val scope = rememberCoroutineScope()
    val apiClient = remember { ApiClient(component.authStorage, onUnauthorized = { component.logout() }) }
    val userRepo = remember { UserRepository(apiClient) }

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().background(UbusBackground).padding(horizontal = 20.dp),
    ) {
        IconButton(onClick = { component.goBack() }, modifier = Modifier.padding(top = 8.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = MaterialTheme.colorScheme.onBackground)
        }
        Text("Alterar senha", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(top = 8.dp, bottom = 24.dp))

        UbusTextField(
            value = currentPassword, onValueChange = { currentPassword = it }, label = "Senha atual",
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = UbusMutedForeground) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        )
        Spacer(Modifier.height(16.dp))
        UbusTextField(
            value = newPassword, onValueChange = { newPassword = it }, label = "Nova senha",
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = UbusMutedForeground) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        )
        Spacer(Modifier.height(16.dp))
        UbusTextField(
            value = confirmPassword, onValueChange = { confirmPassword = it }, label = "Confirmar nova senha",
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = UbusMutedForeground) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        )
        Spacer(Modifier.height(24.dp))

        if (message.isNotEmpty()) {
            Text(message, color = if (isError) UbusDestructive else UbusSuccess, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(16.dp))
        }

        UbusButton(
            text = "Alterar senha",
            loading = loading,
            onClick = {
                if (newPassword != confirmPassword) { message = "Senhas não coincidem."; isError = true; return@UbusButton }
                loading = true
                scope.launch {
                    try {
                        userRepo.changePassword(currentPassword, newPassword)
                        message = "Senha alterada com sucesso!"
                        isError = false
                    } catch (_: Exception) {
                        message = "Erro ao alterar senha."
                        isError = true
                    }
                    loading = false
                }
            },
        )
    }
}
