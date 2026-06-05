package com.ubusmobilidade.ubus.ui.screens.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.AuthRepository
import com.ubusmobilidade.ubus.data.api.ManagementRepository
import com.ubusmobilidade.ubus.data.model.Municipality
import com.ubusmobilidade.ubus.data.model.VerificationChannel
import com.ubusmobilidade.ubus.data.model.VerificationContext
import com.ubusmobilidade.ubus.data.model.RegisterPayload
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.components.PasswordStrengthBar
import com.ubusmobilidade.ubus.ui.components.UbusButton
import com.ubusmobilidade.ubus.ui.components.UbusOutlinedButton
import com.ubusmobilidade.ubus.ui.components.UbusTextField
import com.ubusmobilidade.ubus.ui.theme.UbusBorder
import com.ubusmobilidade.ubus.ui.theme.UbusDestructive
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary
import com.ubusmobilidade.ubus.ui.theme.UbusSuccess
import com.ubusmobilidade.ubus.ui.theme.UbusText3
import com.ubusmobilidade.ubus.ui.util.CpfVisualTransformation
import com.ubusmobilidade.ubus.ui.util.PasswordStrength
import com.ubusmobilidade.ubus.ui.util.PhoneVisualTransformation
import com.ubusmobilidade.ubus.ui.util.isValidCpf
import com.ubusmobilidade.ubus.ui.util.toUserMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import com.ubusmobilidade.ubus.ui.util.rememberCameraLauncher

private const val TOTAL_STEPS = 5

private val stepTitles = listOf(
    "Dados pessoais",
    "Tirar foto",
    "Verificar e-mail",
    "Selecionar município",
    "Criar senha",
)

private val stepSubtitles = listOf(
    "Informe seu nome, CPF e telefone.",
    "Precisamos de uma foto sua para identificação.",
    "Enviaremos um código para seu e-mail.",
    "Escolha o município onde você utiliza o transporte.",
    "Crie uma senha segura para sua conta.",
)

@Composable
fun CadastroScreen(component: RootComponent) {
    val scope = rememberCoroutineScope()
    val apiClient = remember {
        ApiClient(component.authStorage, onUnauthorized = { component.logout() })
    }
    val authRepo = remember { AuthRepository(apiClient) }
    val managementRepo = remember { ManagementRepository(apiClient) }

    var currentStep by remember { mutableIntStateOf(0) }

    var name by remember { mutableStateOf("") }
    var cpf by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var cpfError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    var photoUri by remember { mutableStateOf<String?>(null) }
    val cameraLauncher = rememberCameraLauncher { uri ->
        if (uri != null) {
            photoUri = uri
        }
    }

    // ── Step 2: E-mail + código ──
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var codeSent by remember { mutableStateOf(false) }
    var code by remember { mutableStateOf("") }
    var codeError by remember { mutableStateOf<String?>(null) }
    var sendingCode by remember { mutableStateOf(false) }
    var verifyingCode by remember { mutableStateOf(false) }
    var resendCountdown by remember { mutableIntStateOf(0) }

    // ── Step 3: Município ──
    var municipalities by remember { mutableStateOf<List<Municipality>>(emptyList()) }
    var loadingMunicipalities by remember { mutableStateOf(false) }
    var municipalitiesError by remember { mutableStateOf<String?>(null) }
    var selectedMunicipalityId by remember { mutableStateOf<String?>(null) }

    // ── Step 4: Senha ──
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var registering by remember { mutableStateOf(false) }

    var generalError by remember { mutableStateOf<String?>(null) }

    // ── Resend countdown timer ──
    LaunchedEffect(resendCountdown) {
        if (resendCountdown > 0) {
            delay(1_000L)
            resendCountdown--
        }
    }

    // ── Auto open camera in step 1 ──
    LaunchedEffect(currentStep) {
        if (currentStep == 1 && photoUri == null) {
            cameraLauncher()
        }
    }

    // ── Load municipalities when entering step 3 ──
    LaunchedEffect(currentStep) {
        if (currentStep == 2 && municipalities.isEmpty() && !loadingMunicipalities) {
            loadingMunicipalities = true
            municipalitiesError = null
            try {
                municipalities = managementRepo.listPublicMunicipalities()
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                municipalitiesError = "Erro ao carregar municípios. Tente novamente."
            } finally {
                loadingMunicipalities = false
            }
        }
    }

    // ── Validation helpers ──
    fun validateStep1(): Boolean {
        var valid = true
        nameError = if (name.isBlank()) { valid = false; "Nome é obrigatório" } else null
        val digitsOnlyCpf = cpf.filter { it.isDigit() }
        cpfError = when {
            digitsOnlyCpf.isBlank() -> { valid = false; "CPF é obrigatório" }
            digitsOnlyCpf.length != 11 -> { valid = false; "CPF deve ter 11 dígitos" }
            !isValidCpf(digitsOnlyCpf) -> { valid = false; "CPF inválido" }
            else -> null
        }
        val digitsOnlyPhone = phone.filter { it.isDigit() }
        phoneError = when {
            digitsOnlyPhone.isBlank() -> { valid = false; "Telefone é obrigatório" }
            digitsOnlyPhone.length != 11 -> { valid = false; "Telefone deve ter 11 dígitos" }
            else -> null
        }
        return valid
    }

    fun validateEmail(): Boolean {
        emailError = if (email.isBlank() || !email.contains("@")) {
            "Informe um e-mail válido"
        } else null
        return emailError == null
    }

    fun validateStep4(): Boolean {
        var valid = true
        val strength = PasswordStrength.evaluate(password)
        passwordError = when {
            password.length < 6 -> { valid = false; "Mínimo de 6 caracteres" }
            strength.score < 2 -> { valid = false; "Senha muito fraca" }
            else -> null
        }
        confirmPasswordError = if (confirmPassword != password) {
            valid = false; "As senhas não coincidem"
        } else null
        return valid
    }

    fun handleSendCode() {
        if (!validateEmail()) return
        sendingCode = true
        generalError = null
        scope.launch {
            try {
                authRepo.sendVerificationCode(email.trim(), VerificationChannel.EMAIL, VerificationContext.REGISTER)
                codeSent = true
                resendCountdown = 60
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                generalError = e.toUserMessage("Não foi possível enviar o código. Tente novamente.")
            } finally {
                sendingCode = false
            }
        }
    }

    fun handleVerifyCode() {
        if (code.filter { it.isDigit() }.length != 6) {
            codeError = "Informe o código de 6 dígitos"
            return
        }
        verifyingCode = true
        generalError = null
        codeError = null
        scope.launch {
            try {
                val response = authRepo.verifyCode(email.trim(), code.trim(), VerificationChannel.EMAIL, VerificationContext.REGISTER)
                if (response.verified) {
                    currentStep = 3
                } else {
                    codeError = response.message ?: "Código inválido"
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                codeError = e.toUserMessage("Não foi possível verificar o código. Tente novamente.")
            } finally {
                verifyingCode = false
            }
        }
    }

    fun handleRegister() {
        if (!validateStep4()) return
        registering = true
        generalError = null
        scope.launch {
            try {
                authRepo.register(
                    RegisterPayload(
                        municipalityId = selectedMunicipalityId ?: "",
                        cpf = cpf.filter { it.isDigit() },
                        name = name.trim(),
                        email = email.trim(),
                        password = password,
                        phone = phone.filter { it.isDigit() },
                        photoUrl = photoUri,
                    )
                )
                component.replaceWith(RootComponent.Config.Login)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                generalError = e.toUserMessage("Não foi possível concluir o cadastro. Tente novamente.")
            } finally {
                registering = false
            }
        }
    }

    fun handleAdvance() {
        generalError = null
        when (currentStep) {
            0 -> if (validateStep1()) currentStep = 1
            1 -> if (photoUri != null) currentStep = 2 else generalError = "Tire uma foto para continuar"
            2 -> { /* advance handled by verify callback */ }
            3 -> if (selectedMunicipalityId != null) currentStep = 4
            4 -> handleRegister()
        }
    }

    fun handleBack() {
        generalError = null
        if (currentStep == 0) component.goBack() else currentStep--
    }

    // ── UI ──
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        IconButton(
            onClick = { handleBack() },
            modifier = Modifier.padding(start = 8.dp, top = 8.dp),
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar",
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            StepIndicator(currentStep = currentStep, totalSteps = TOTAL_STEPS)

            Spacer(Modifier.height(24.dp))

            Text(
                stepTitles[currentStep],
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                stepSubtitles[currentStep],
                style = MaterialTheme.typography.bodyMedium,
                color = UbusText3,
            )



            Spacer(Modifier.height(28.dp))

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    val direction = if (targetState > initialState) 1 else -1
                    (slideInHorizontally { it * direction } + fadeIn())
                        .togetherWith(slideOutHorizontally { -it * direction } + fadeOut())
                },
            ) { step ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    when (step) {
                        0 -> StepDadosPessoais(
                            name = name, onNameChange = { name = it; nameError = null },
                            cpf = cpf, onCpfChange = { if (it.filter(Char::isDigit).length <= 11) { cpf = it.filter(Char::isDigit) }; cpfError = null },
                            phone = phone, onPhoneChange = { if (it.filter(Char::isDigit).length <= 11) { phone = it.filter(Char::isDigit) }; phoneError = null },
                            nameError = nameError, cpfError = cpfError, phoneError = phoneError,
                        )
                        1 -> StepTirarFoto(
                            photoUri = photoUri,
                            onTakePhoto = { 
                                cameraLauncher() 
                            }
                        )
                        2 -> StepVerificarEmail(
                            email = email, onEmailChange = { email = it; emailError = null },
                            emailError = emailError,
                            codeSent = codeSent,
                            code = code, onCodeChange = { if (it.filter(Char::isDigit).length <= 6) code = it.filter(Char::isDigit) },
                            codeError = codeError,
                            sendingCode = sendingCode,
                            verifyingCode = verifyingCode,
                            resendCountdown = resendCountdown,
                            onSendCode = { handleSendCode() },
                            onVerifyCode = { handleVerifyCode() },
                        )
                        3 -> StepSelecionarMunicipio(
                            municipalities = municipalities,
                            loading = loadingMunicipalities,
                            error = municipalitiesError,
                            selectedId = selectedMunicipalityId,
                            onSelect = { selectedMunicipalityId = it },
                            onRetry = {
                                municipalitiesError = null
                                loadingMunicipalities = true
                                scope.launch {
                                try {
                                    municipalities = managementRepo.listPublicMunicipalities()
                                } catch (e: Exception) {
                                    if (e is kotlinx.coroutines.CancellationException) throw e
                                    municipalitiesError = "Erro ao carregar municípios."
                                } finally {
                                    loadingMunicipalities = false
                                }
                                }
                            },
                        )
                        4 -> StepCriarSenha(
                            password = password, onPasswordChange = { password = it; passwordError = null },
                            confirmPassword = confirmPassword, onConfirmChange = { confirmPassword = it; confirmPasswordError = null },
                            passwordError = passwordError, confirmPasswordError = confirmPasswordError,
                        )
                    }
                }
            }

            if (!generalError.isNullOrBlank()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    generalError!!,
                    color = UbusDestructive,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(Modifier.height(24.dp))
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 24.dp)) {
            when (currentStep) {
                0, 1 -> UbusButton(text = "Avançar", onClick = { handleAdvance() })
                2 -> {} 
                3 -> UbusButton(
                    text = "Avançar",
                    onClick = { handleAdvance() },
                    enabled = selectedMunicipalityId != null,
                )
                4 -> UbusButton(
                    text = "Criar conta",
                    onClick = { handleAdvance() },
                    loading = registering,
                )
            }
        }
    }
}

@Composable
private fun StepTirarFoto(
    photoUri: String?,
    onTakePhoto: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = if (photoUri != null) listOf(UbusSuccess, Color(0xFF10B981)) else listOf(UbusPrimary, Color(0xFF6366F1))
                    )
                )
                .clickable { onTakePhoto() }
                .padding(4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                if (photoUri != null) {
                    Icon(Icons.Default.CheckCircle, null, tint = UbusSuccess, modifier = Modifier.size(64.dp))
                } else {
                    Icon(Icons.Default.CameraAlt, null, tint = UbusPrimary, modifier = Modifier.size(54.dp))
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        UbusOutlinedButton(
            text = if (photoUri != null) "Tirar outra foto" else "Tirar foto",
            onClick = onTakePhoto,
        )
    }
}

/* ═══════════════════════════════════════════════
   Step Indicator
   ═══════════════════════════════════════════════ */

@Composable
private fun StepIndicator(currentStep: Int, totalSteps: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(totalSteps) { index ->
            val isActive = index <= currentStep
            val bgColor = if (isActive) UbusPrimary else UbusBorder
            val textColor = if (isActive) Color.White else UbusText3

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "${index + 1}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                )
            }

            if (index < totalSteps - 1) {
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(2.dp)
                        .background(if (index < currentStep) UbusPrimary else UbusBorder),
                )
            }
        }
    }
}

/* ═══════════════════════════════════════════════
   Step 1 — Dados Pessoais
   ═══════════════════════════════════════════════ */

@Composable
private fun StepDadosPessoais(
    name: String, onNameChange: (String) -> Unit,
    cpf: String, onCpfChange: (String) -> Unit,
    phone: String, onPhoneChange: (String) -> Unit,
    nameError: String?, cpfError: String?, phoneError: String?,
) {
    UbusTextField(
        value = name, onValueChange = onNameChange,
        label = "Nome completo", placeholder = "Seu nome",
        leadingIcon = { Icon(Icons.Default.Person, null, tint = UbusText3) },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        isError = nameError != null, errorMessage = nameError,
    )
    Spacer(Modifier.height(16.dp))

    UbusTextField(
        value = cpf, onValueChange = onCpfChange,
        label = "CPF", placeholder = "000.000.000-00",
        visualTransformation = CpfVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        isError = cpfError != null, errorMessage = cpfError,
    )
    Spacer(Modifier.height(16.dp))

    UbusTextField(
        value = phone, onValueChange = onPhoneChange,
        label = "Telefone", placeholder = "(00) 0 0000-0000",
        leadingIcon = { Icon(Icons.Default.Phone, null, tint = UbusText3) },
        visualTransformation = PhoneVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
        isError = phoneError != null, errorMessage = phoneError,
    )
}

/* ═══════════════════════════════════════════════
   Step 2 — Verificar E-mail
   ═══════════════════════════════════════════════ */

@Composable
private fun StepVerificarEmail(
    email: String, onEmailChange: (String) -> Unit,
    emailError: String?,
    codeSent: Boolean,
    code: String, onCodeChange: (String) -> Unit,
    codeError: String?,
    sendingCode: Boolean, verifyingCode: Boolean,
    resendCountdown: Int,
    onSendCode: () -> Unit, onVerifyCode: () -> Unit,
) {
    UbusTextField(
        value = email, onValueChange = onEmailChange,
        label = "E-mail", placeholder = "seu@email.com",
        leadingIcon = { Icon(Icons.Default.Email, null, tint = UbusText3) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
        isError = emailError != null, errorMessage = emailError,
        enabled = !codeSent,
    )
    Spacer(Modifier.height(16.dp))

    if (!codeSent) {
        UbusButton(
            text = "Enviar código",
            onClick = onSendCode,
            loading = sendingCode,
            enabled = email.isNotBlank(),
        )
    } else {
        UbusTextField(
            value = code, onCodeChange,
            label = "Código de verificação", placeholder = "000000",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            isError = codeError != null, errorMessage = codeError,
        )
        Spacer(Modifier.height(16.dp))

        UbusButton(
            text = "Verificar",
            onClick = onVerifyCode,
            loading = verifyingCode,
            enabled = code.filter { it.isDigit() }.length == 6,
        )
        Spacer(Modifier.height(12.dp))

        if (resendCountdown > 0) {
            Text(
                "Reenviar código em ${resendCountdown}s",
                style = MaterialTheme.typography.bodySmall,
                color = UbusText3,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        } else {
            UbusOutlinedButton(
                text = "Reenviar código",
                onClick = onSendCode,
                enabled = !sendingCode,
            )
        }
    }
}

/* ═══════════════════════════════════════════════
   Step 3 — Selecionar Município
   ═══════════════════════════════════════════════ */

@Composable
private fun StepSelecionarMunicipio(
    municipalities: List<Municipality>,
    loading: Boolean,
    error: String?,
    selectedId: String?,
    onSelect: (String) -> Unit,
    onRetry: () -> Unit,
) {
    when {
        loading -> {
            Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = UbusPrimary)
            }
        }
        error != null -> {
            Text(error, color = UbusDestructive, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(12.dp))
            UbusOutlinedButton(text = "Tentar novamente", onClick = onRetry)
        }
        else -> {
            municipalities.forEach { municipality ->
                val isSelected = municipality.id == selectedId
                val borderColor = if (isSelected) UbusPrimary else Color.Transparent

                BentoCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .border(
                            width = 2.dp,
                            color = borderColor,
                            shape = MaterialTheme.shapes.large,
                        )
                        .clip(MaterialTheme.shapes.large)
                        .clickable { onSelect(municipality.id) },
                ) {
                    Text(
                        municipality.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSelected) UbusPrimary else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}

/* ═══════════════════════════════════════════════
   Step 4 — Criar Senha
   ═══════════════════════════════════════════════ */

@Composable
private fun StepCriarSenha(
    password: String, onPasswordChange: (String) -> Unit,
    confirmPassword: String, onConfirmChange: (String) -> Unit,
    passwordError: String?, confirmPasswordError: String?,
) {
    UbusTextField(
        value = password, onValueChange = onPasswordChange,
        label = "Senha", placeholder = "Mínimo 6 caracteres",
        leadingIcon = { Icon(Icons.Default.Lock, null, tint = UbusText3) },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
        isError = passwordError != null, errorMessage = passwordError,
    )
    Spacer(Modifier.height(8.dp))

    PasswordStrengthBar(password = password)

    Spacer(Modifier.height(16.dp))

    UbusTextField(
        value = confirmPassword, onValueChange = onConfirmChange,
        label = "Confirmar senha", placeholder = "Repita a senha",
        leadingIcon = { Icon(Icons.Default.Lock, null, tint = UbusText3) },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        isError = confirmPasswordError != null, errorMessage = confirmPasswordError,
    )
}
