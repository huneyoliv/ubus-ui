package com.ubusmobilidade.ubus.ui.screens.auth

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.ApiError
import com.ubusmobilidade.ubus.data.api.AuthRepository
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.UbusButton
import com.ubusmobilidade.ubus.ui.components.UbusTextField
import com.ubusmobilidade.ubus.ui.theme.UbusAccent
import com.ubusmobilidade.ubus.ui.theme.UbusBackground
import com.ubusmobilidade.ubus.ui.theme.UbusDestructive
import com.ubusmobilidade.ubus.ui.theme.UbusMutedForeground
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(component: RootComponent) {
    val scope = rememberCoroutineScope()
    val authRepo = remember {
        AuthRepository(ApiClient(component.authStorage, onUnauthorized = { component.logout() }))
    }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    fun handleLogin() {
        if (email.isBlank() || password.isBlank()) return
        error = ""
        loading = true
        scope.launch {
            try {
                val response = authRepo.login(email.trim(), password)
                component.authStorage.setAuth(response.accessToken, response.user)
                component.onLoginSuccess()
            } catch (e: ApiError) {
                error = if (e.status == 401) "Email ou senha incorretos."
                else "Erro ${e.status}: ${e.body ?: e.statusText}"
            } catch (e: Exception) {
                error = "Erro: ${e.message ?: "Falha na conexão"}"
            } finally {
                loading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(UbusBackground),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            // Back button
            IconButton(
                onClick = { component.goBack() },
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }

            Spacer(Modifier.height(16.dp))

            // Logo
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(UbusAccent.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.DirectionsBus,
                        contentDescription = null,
                        tint = UbusAccent,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text("Ubus", color = UbusAccent, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(32.dp))

            // Title
            Text(
                "Entrar",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Informe suas credenciais para acessar.",
                style = MaterialTheme.typography.bodyLarge,
                color = UbusMutedForeground,
            )

            Spacer(Modifier.height(32.dp))

            // Email
            UbusTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "seu@email.com",
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null, tint = UbusMutedForeground, modifier = Modifier.size(18.dp))
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
            )

            Spacer(Modifier.height(16.dp))

            // Password
            UbusTextField(
                value = password,
                onValueChange = { password = it },
                label = "Senha",
                placeholder = "Sua senha",
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = UbusMutedForeground, modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showPassword) "Ocultar" else "Mostrar",
                            tint = if (showPassword) UbusAccent else UbusMutedForeground,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { handleLogin() }),
            )

            Spacer(Modifier.height(8.dp))

            // Forgot password
            Text(
                "Esqueci minha senha",
                color = UbusAccent,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { component.navigateTo(RootComponent.Config.RedefinirSenha) }
                    .padding(vertical = 4.dp),
            )

            Spacer(Modifier.height(16.dp))

            // Error
            if (error.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(UbusDestructive.copy(alpha = 0.08f))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(error, color = UbusDestructive, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(16.dp))
            }

            // Login button
            UbusButton(
                text = "Acessar plataforma",
                onClick = { handleLogin() },
                loading = loading,
            )

            Spacer(Modifier.height(32.dp))

            // Register link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text("Ainda não tem conta? ", color = UbusMutedForeground, fontSize = 14.sp)
                Text(
                    "Cadastre-se",
                    color = UbusAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable {
                        component.navigateTo(RootComponent.Config.Cadastro)
                    },
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
