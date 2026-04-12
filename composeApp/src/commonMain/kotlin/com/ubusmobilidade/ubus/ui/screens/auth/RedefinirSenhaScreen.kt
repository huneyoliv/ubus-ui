package com.ubusmobilidade.ubus.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
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
import androidx.compose.ui.unit.dp
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.AuthRepository
import com.ubusmobilidade.ubus.navigation.RootComponent
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

    var email by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var success by remember { mutableStateOf(false) }

    fun handleReset() {
        if (email.isBlank()) {
            error = "Informe seu email."
            return
        }
        error = ""
        loading = true
        scope.launch {
            try {
                authRepo.sendPasswordResetEmail(email.trim())
                success = true
            } catch (_: Exception) {
                error = "Erro ao enviar email. Tente novamente."
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
            onClick = { component.goBack() },
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = MaterialTheme.colorScheme.onBackground)
        }

        Spacer(Modifier.height(16.dp))

        Text("Redefinir senha", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onBackground)

        Spacer(Modifier.height(8.dp))

        Text(
            "Informe seu email para receber o link de redefinição.",
            style = MaterialTheme.typography.bodyLarge,
            color = UbusText3,
        )

        Spacer(Modifier.height(32.dp))

        if (success) {
            Text(
                "Email enviado com sucesso! Verifique sua caixa de entrada.",
                color = UbusSuccess,
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            UbusTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "seu@email.com",
                leadingIcon = { Icon(Icons.Default.Email, null, tint = UbusText3, modifier = Modifier) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
            )

            Spacer(Modifier.height(16.dp))

            if (error.isNotEmpty()) {
                Text(error, color = UbusDestructive, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(16.dp))
            }

            UbusButton(text = "Enviar link", onClick = { handleReset() }, loading = loading)
        }
    }
}
