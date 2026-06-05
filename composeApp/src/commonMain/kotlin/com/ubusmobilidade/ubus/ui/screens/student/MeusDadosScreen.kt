package com.ubusmobilidade.ubus.ui.screens.student

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.AuthRepository
import com.ubusmobilidade.ubus.data.api.UserRepository
import com.ubusmobilidade.ubus.data.model.UpdateProfilePayload
import com.ubusmobilidade.ubus.data.model.VerificationChannel
import com.ubusmobilidade.ubus.data.model.VerificationContext
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.UbusButton
import com.ubusmobilidade.ubus.ui.components.UbusOutlinedButton
import com.ubusmobilidade.ubus.ui.components.UbusTextField
import com.ubusmobilidade.ubus.ui.theme.UbusDestructive
import com.ubusmobilidade.ubus.ui.theme.UbusSuccess
import com.ubusmobilidade.ubus.ui.theme.UbusText3
import com.ubusmobilidade.ubus.ui.util.CpfVisualTransformation
import com.ubusmobilidade.ubus.ui.util.PhoneVisualTransformation
import kotlinx.coroutines.launch

@Composable
fun MeusDadosScreen(component: RootComponent) {
    val scope = rememberCoroutineScope()
    val user = component.authStorage.user
    val apiClient = remember { ApiClient(component.authStorage, onUnauthorized = { component.logout() }) }
    val userRepo = remember { UserRepository(apiClient) }
    val authRepo = remember { AuthRepository(apiClient) }

    var phone by remember { mutableStateOf(user?.phone ?: "") }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    // Email change flow
    var showEmailChange by remember { mutableStateOf(false) }
    var emailStep by remember { mutableStateOf(1) } // 1 = enter email, 2 = enter code
    var newEmail by remember { mutableStateOf("") }
    var emailCode by remember { mutableStateOf("") }
    var emailLoading by remember { mutableStateOf(false) }
    var emailMessage by remember { mutableStateOf("") }
    var emailIsError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        IconButton(onClick = { component.goBack() }, modifier = Modifier.padding(top = 8.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = MaterialTheme.colorScheme.onBackground)
        }
        Text(
            "Meus dados",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
        )

        UbusTextField(
            value = user?.name ?: "",
            onValueChange = {},
            label = "Nome",
            enabled = false,
        )
        Spacer(Modifier.height(16.dp))

        UbusTextField(
            value = user?.cpf ?: "",
            onValueChange = {},
            label = "CPF",
            enabled = false,
            visualTransformation = CpfVisualTransformation(),
        )
        Spacer(Modifier.height(16.dp))

        UbusTextField(
            value = phone,
            onValueChange = { phone = it.filter { c -> c.isDigit() }.take(11) },
            label = "Telefone",
            placeholder = "(00) 0 0000-0000",
            leadingIcon = { Icon(Icons.Default.Phone, null, tint = UbusText3) },
            visualTransformation = PhoneVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        )
        Spacer(Modifier.height(16.dp))

        // Current email (read-only)
        UbusTextField(
            value = user?.email ?: "",
            onValueChange = {},
            label = "E-mail",
            leadingIcon = { Icon(Icons.Default.Email, null, tint = UbusText3) },
            enabled = false,
        )
        Spacer(Modifier.height(8.dp))

        UbusOutlinedButton(
            text = if (showEmailChange) "Cancelar alteração" else "Alterar e-mail",
            onClick = {
                showEmailChange = !showEmailChange
                if (!showEmailChange) {
                    emailStep = 1; newEmail = ""; emailCode = ""; emailMessage = ""
                }
            },
        )

        AnimatedVisibility(visible = showEmailChange) {
            Column {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Spacer(Modifier.height(12.dp))

                if (emailStep == 1) {
                    Text("Novo e-mail", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.height(8.dp))
                    UbusTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = "Novo e-mail",
                        placeholder = "seu@email.com",
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = UbusText3) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    )
                    Spacer(Modifier.height(12.dp))
                    if (emailMessage.isNotEmpty()) {
                        Text(emailMessage, color = if (emailIsError) UbusDestructive else UbusSuccess, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(8.dp))
                    }
                    UbusButton(
                        text = "Enviar código",
                        loading = emailLoading,
                        enabled = newEmail.contains("@"),
                        onClick = {
                            emailLoading = true; emailMessage = ""
                            scope.launch {
                                try {
                                    authRepo.sendVerificationCode(newEmail, VerificationChannel.EMAIL, VerificationContext.CHANGE_EMAIL)
                                    emailStep = 2
                                    emailMessage = "Código enviado para $newEmail"
                                    emailIsError = false
                                } catch (e: Exception) {
                                    if (e is kotlinx.coroutines.CancellationException) throw e
                                    emailMessage = "Erro ao enviar código."
                                    emailIsError = true
                                }
                                emailLoading = false
                            }
                        },
                    )
                } else {
                    Text("Verificar código", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.height(4.dp))
                    Text("Insira o código de 6 dígitos enviado para $newEmail", style = MaterialTheme.typography.bodySmall, color = UbusText3)
                    Spacer(Modifier.height(8.dp))
                    UbusTextField(
                        value = emailCode,
                        onValueChange = { emailCode = it.filter { c -> c.isDigit() }.take(6) },
                        label = "Código de verificação",
                        placeholder = "000000",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    Spacer(Modifier.height(12.dp))
                    if (emailMessage.isNotEmpty()) {
                        Text(emailMessage, color = if (emailIsError) UbusDestructive else UbusSuccess, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(8.dp))
                    }
                    UbusButton(
                        text = "Verificar",
                        loading = emailLoading,
                        enabled = emailCode.length == 6,
                        onClick = {
                            emailLoading = true; emailMessage = ""
                            scope.launch {
                                try {
                                    val result = authRepo.verifyCode(newEmail, emailCode, VerificationChannel.EMAIL, VerificationContext.CHANGE_EMAIL)
                                    if (result.verified) {
                                        val updated = userRepo.updateMe(UpdateProfilePayload(email = newEmail))
                                        component.authStorage.user = updated
                                        emailMessage = "E-mail alterado com sucesso!"
                                        emailIsError = false
                                        showEmailChange = false
                                        emailStep = 1; newEmail = ""; emailCode = ""
                                    } else {
                                        emailMessage = result.message ?: "Código inválido."
                                        emailIsError = true
                                    }
                                } catch (e: Exception) {
                                    if (e is kotlinx.coroutines.CancellationException) throw e
                                    emailMessage = "Erro ao verificar código."
                                    emailIsError = true
                                }
                                emailLoading = false
                            }
                        },
                    )
                }
                Spacer(Modifier.height(12.dp))
            }
        }

        Spacer(Modifier.height(24.dp))

        if (message.isNotEmpty()) {
            Text(message, color = if (isError) UbusDestructive else UbusSuccess, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(16.dp))
        }

        UbusButton(
            text = "Salvar alterações",
            loading = loading,
            onClick = {
                loading = true; message = ""
                scope.launch {
                    try {
                        val digits = phone.filter { it.isDigit() }
                        val updated = userRepo.updateMe(UpdateProfilePayload(phone = digits))
                        component.authStorage.user = updated
                        message = "Dados atualizados!"
                        isError = false
                    } catch (e: Exception) {
                        if (e is kotlinx.coroutines.CancellationException) throw e
                        message = "Erro ao atualizar."
                        isError = true
                    }
                    loading = false
                }
            },
        )
        Spacer(Modifier.height(32.dp))
    }
}
