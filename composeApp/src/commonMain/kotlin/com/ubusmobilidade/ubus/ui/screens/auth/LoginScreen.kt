package com.ubusmobilidade.ubus.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.ApiError
import com.ubusmobilidade.ubus.data.api.AuthRepository
import com.ubusmobilidade.ubus.data.model.LoginPayload
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.UbusButton
import com.ubusmobilidade.ubus.ui.components.UbusTextField
import com.ubusmobilidade.ubus.ui.theme.UbusDestructive
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary
import com.ubusmobilidade.ubus.ui.theme.UbusText3
import com.ubusmobilidade.ubus.ui.util.toUserMessage
import org.jetbrains.compose.resources.painterResource
import ubus.composeapp.generated.resources.Res
import ubus.composeapp.generated.resources.logo
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(component: RootComponent) {
    val stack by component.childStack.subscribeAsState()
    val showBack = stack.backStack.isNotEmpty()

    LaunchedEffect(Unit) {
        if (component.authStorage.isAuthenticated) {
            component.onLoginSuccess()
        }
    }

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
        if (email.isBlank() || password.isBlank()) {
            error = "Informe e-mail e senha para continuar."
            return
        }
        error = ""
        loading = true
        scope.launch {
            try {
                val response = authRepo.login(LoginPayload(email.trim(), password))
                component.authStorage.setAuth(response.accessToken, response.user)
                component.onLoginSuccess()
            } catch (e: ApiError) {
                e.printStackTrace()
                error = if (e.status == 401) "Email ou senha incorretos."
                else e.toUserMessage("Não foi possível entrar agora. Tente novamente.")
            } catch (e: Exception) {
                e.printStackTrace()
                error = e.toUserMessage("Não foi possível entrar agora. Tente novamente.")
            } finally {
                loading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                if (showBack) {
                    IconButton(
                        onClick = { component.goBack() },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 16.dp, start = 16.dp)
                            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f), CircleShape),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(Res.drawable.logo),
                        contentDescription = "Ubus Logo",
                        modifier = Modifier.height(64.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Transporte Estudantil Inteligente",
                        color = UbusText3,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp)
            ) {
                // Title
                Text(
                    "Entrar na conta",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    "Informe suas credenciais para acessar a plataforma.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = UbusText3,
                )

                Spacer(Modifier.height(28.dp))

                // Email
                UbusTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    placeholder = "seu@email.com",
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null, tint = UbusText3, modifier = Modifier.size(18.dp))
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
                        Icon(Icons.Default.Lock, contentDescription = null, tint = UbusText3, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showPassword) "Ocultar" else "Mostrar",
                                tint = if (showPassword) UbusPrimary else UbusText3,
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

                Spacer(Modifier.height(10.dp))

                // Forgot password
                Text(
                    "Esqueci minha senha",
                    color = UbusPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable { component.navigateTo(RootComponent.Config.RedefinirSenha) }
                        .padding(vertical = 4.dp),
                )

                Spacer(Modifier.height(24.dp))

                // Error
                if (error.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(UbusDestructive.copy(alpha = 0.08f))
                            .border(1.dp, UbusDestructive.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(error, color = UbusDestructive, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(20.dp))
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
                    Text("Ainda não tem conta? ", color = UbusText3, fontSize = 14.sp)
                    Text(
                        "Criar conta",
                        color = UbusPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable {
                            component.navigateTo(RootComponent.Config.Cadastro)
                        },
                    )
                }

                Spacer(Modifier.height(32.dp))

                Text(
                    "© 2026 Ubus — Todos os direitos reservados",
                    color = UbusText3.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
