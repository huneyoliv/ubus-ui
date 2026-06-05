package com.ubusmobilidade.ubus.ui.screens.student

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.UserRepository
import com.ubusmobilidade.ubus.data.api.UploadRepository
import com.ubusmobilidade.ubus.data.model.*
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.theme.*
import com.ubusmobilidade.ubus.ui.util.rememberFilePickerLauncher
import com.ubusmobilidade.ubus.ui.util.toUserMessage
import kotlinx.coroutines.launch

private sealed interface WizardStep {
    object Status : WizardStep
    object SelectReason : WizardStep
    object WheelchairQuestion : WizardStep
    object Document : WizardStep
}

@Composable
fun BaixaMobilidadeScreen(component: RootComponent) {
    val user = component.authStorage.user
    val scope = rememberCoroutineScope()
    val apiClient = remember { ApiClient(component.authStorage, onUnauthorized = { component.logout() }) }
    val userRepo = remember { UserRepository(apiClient) }
    val uploadRepo = remember { UploadRepository(apiClient) }

    var step by remember {
        mutableStateOf<WizardStep>(
            if (user?.accessibilityStatus != null) WizardStep.Status
            else WizardStep.Status
        )
    }

    var selectedReason by remember { mutableStateOf<AccessibilityReason?>(user?.accessibilityReason) }
    var needsWheelchair by remember { mutableStateOf(user?.needsWheelchair ?: false) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var uploadedFileUrl by remember { mutableStateOf<String?>(user?.accessibilityDocUrl) }
    var uploadPending by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val filePicker = rememberFilePickerLauncher { uri ->
        if (uri != null) {
            selectedFileName = uri.substringAfterLast("/")
            scope.launch {
                try {
                    uploadPending = false
                    error = null
                    val fileBytes = ByteArray(0) // Simulação local
                    val result = uploadRepo.upload(fileBytes, selectedFileName!!, UploadType.ACCESSIBILITY_PROOF)
                    uploadedFileUrl = result.fileUrl
                } catch (e: UnsupportedOperationException) {
                    uploadPending = true
                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    error = "Erro ao carregar arquivo."
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        AccessibilityTopBar(
            step = step,
            onBack = {
                when (step) {
                    WizardStep.Status -> component.goBack()
                    WizardStep.SelectReason -> {
                        if (user?.accessibilityStatus != null) step = WizardStep.Status
                        else component.goBack()
                    }
                    WizardStep.WheelchairQuestion -> step = WizardStep.SelectReason
                    WizardStep.Document -> {
                        if (selectedReason?.needsWheelchairQuestion() == true) {
                            step = WizardStep.WheelchairQuestion
                        } else {
                            step = WizardStep.SelectReason
                        }
                    }
                }
            }
        )

        AnimatedContent(
            targetState = step,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            modifier = Modifier.weight(1f)
        ) { currentStep ->
            when (currentStep) {
                WizardStep.Status -> StatusStep(
                    user = user,
                    onNewRequest = {
                        selectedReason = null
                        needsWheelchair = false
                        selectedFileName = null
                        uploadedFileUrl = null
                        uploadPending = false
                        error = null
                        step = WizardStep.SelectReason
                    },
                    onResend = {
                        selectedReason = user?.accessibilityReason
                        needsWheelchair = user?.needsWheelchair ?: false
                        selectedFileName = null
                        uploadedFileUrl = null
                        uploadPending = false
                        error = null
                        step = WizardStep.Document
                    }
                )
                WizardStep.SelectReason -> SelectReasonStep(
                    selected = selectedReason,
                    onSelect = { selectedReason = it },
                    onNext = {
                        if (selectedReason?.needsWheelchairQuestion() == true) {
                            step = WizardStep.WheelchairQuestion
                        } else {
                            needsWheelchair = false
                            step = WizardStep.Document
                        }
                    }
                )
                WizardStep.WheelchairQuestion -> WheelchairStep(
                    needsWheelchair = needsWheelchair,
                    onToggle = { needsWheelchair = it },
                    onNext = { step = WizardStep.Document }
                )
                WizardStep.Document -> DocumentStep(
                    reason = selectedReason,
                    fileName = selectedFileName,
                    uploadPending = uploadPending,
                    fileUrl = uploadedFileUrl,
                    loading = loading,
                    error = error,
                    onPickFile = { filePicker() },
                    onSubmit = {
                        scope.launch {
                            loading = true
                            error = null
                            try {
                                val updated = userRepo.submitAccessibilityRequest(
                                    AccessibilityRequestPayload(
                                        reason = selectedReason!!,
                                        needsWheelchair = needsWheelchair,
                                        accessibilityDocUrl = uploadedFileUrl ?: ""
                                    )
                                )
                                component.authStorage.user = updated
                                step = WizardStep.Status
                            } catch (e: Exception) {
                                if (e is kotlinx.coroutines.CancellationException) throw e
                                error = e.toUserMessage("Erro ao enviar solicitação.")
                            } finally {
                                loading = false
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun AccessibilityTopBar(
    step: WizardStep,
    onBack: () -> Unit
) {
    val progress = when (step) {
        WizardStep.Status -> 0f
        WizardStep.SelectReason -> 0.33f
        WizardStep.WheelchairQuestion -> 0.66f
        WizardStep.Document -> 1f
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text(
                text = "Preferência de Assento e Acessibilidade",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        if (progress > 0f) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = UbusPrimary,
                trackColor = UbusPrimary.copy(alpha = 0.15f)
            )
        }
    }
}

@Composable
private fun StatusStep(
    user: User?,
    onNewRequest: () -> Unit,
    onResend: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (user?.accessibilityStatus == null) {
            Icon(
                imageVector = Icons.Default.AccessibilityNew,
                contentDescription = null,
                tint = UbusPrimary,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Preferência de Assento por Lei",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            BentoCard {
                Column {
                    Text(
                        text = "De acordo com a Lei nº 10.048/2000, estudantes com deficiência, transtorno do espectro autista, idosos, gestantes, lactantes ou pessoas com mobilidade reduzida têm direito à preferência de assento nos ônibus.",
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Solicite o direito de preferência pelo aplicativo anexando os documentos comprobatórios necessários.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 22.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onNewRequest,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UbusPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Solicitar Acessibilidade", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            val status = user.accessibilityStatus
            val reason = user.accessibilityReason

            val (badgeText, badgeColor, icon) = when (status) {
                AccessibilityStatus.APPROVED -> Triple("Aprovado", UbusSuccess, Icons.Default.CheckCircle)
                AccessibilityStatus.PENDING_REVIEW -> Triple("Pendente de Análise", UbusWarning, Icons.Default.HourglassEmpty)
                AccessibilityStatus.REJECTED -> Triple("Rejeitado", UbusDestructive, Icons.Default.Cancel)
                AccessibilityStatus.EXPIRED -> Triple("Expirado", UbusText3, Icons.Default.History)
                AccessibilityStatus.REVOKED -> Triple("Revogado", UbusText3, Icons.Default.RemoveCircle)
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = badgeColor,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = badgeColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Text(
                    text = badgeText.uppercase(),
                    color = badgeColor,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            BentoCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Categoria Solicitada:",
                            style = MaterialTheme.typography.titleSmall,
                            color = UbusText3
                        )
                        Text(
                            text = reason?.displayName() ?: "Nenhuma",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tipo de Benefício:",
                            style = MaterialTheme.typography.titleSmall,
                            color = UbusText3
                        )
                        val isPermanent = reason?.isPermanent() == true
                        Text(
                            text = if (isPermanent) "Permanente" else "Temporário",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isPermanent) UbusSuccess else UbusWarning
                        )
                    }

                    if (user.needsWheelchair == true) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Necessita de Cadeira de Rodas:",
                                style = MaterialTheme.typography.titleSmall,
                                color = UbusText3
                            )
                            Text(
                                text = "Sim (Exige elevador)",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = UbusPrimary
                            )
                        }
                    }

                    if (status == AccessibilityStatus.REJECTED && !user.accessibilityReviewNote.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Motivo da Rejeição:",
                            style = MaterialTheme.typography.titleSmall,
                            color = UbusDestructive,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = user.accessibilityReviewNote,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            when (status) {
                AccessibilityStatus.APPROVED -> {
                    BentoCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, tint = UbusPrimary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (reason?.isPermanent() == true) {
                                    "Seu status de acessibilidade é permanente. Não será necessária renovação semestral para este direito."
                                } else {
                                    if (user.accessibilityConsecutivePeriods >= 3) {
                                        "Atenção: Este é o seu 3º período consecutivo. Para o próximo período, a renovação presencial com a gestão será obrigatória."
                                    } else {
                                        "Seu status expira ao final do semestre letivo. Você precisará reenviar os documentos comprobatórios junto com a renovação de matrícula."
                                    }
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
                AccessibilityStatus.PENDING_REVIEW -> {
                    BentoCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, tint = UbusWarning, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Sua solicitação está em análise pela equipe de trânsito da prefeitura. Você será notificado assim que for revisada.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                AccessibilityStatus.REJECTED -> {
                    Button(
                        onClick = onResend,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = UbusPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Reenviar Documentos", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                AccessibilityStatus.EXPIRED, AccessibilityStatus.REVOKED -> {
                    if (user.accessibilityConsecutivePeriods >= 3) {
                        BentoCard {
                            Text(
                                text = "Você atingiu o limite máximo de 3 períodos consecutivos com status temporário. Procure a prefeitura ou gestor para realizar a validação presencial.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = UbusDestructive,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Button(
                            onClick = onNewRequest,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = UbusPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Nova Solicitação", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectReasonStep(
    selected: AccessibilityReason?,
    onSelect: (AccessibilityReason) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "Selecione o motivo da solicitação:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        AccessibilityReason.values().forEach { reason ->
            val isSelected = selected == reason
            val isTemp = !reason.isPermanent()

            val icon = when (reason) {
                AccessibilityReason.PCD -> Icons.Default.Accessible
                AccessibilityReason.TEA -> Icons.Default.Psychology
                AccessibilityReason.IDOSO -> Icons.Default.Elderly
                AccessibilityReason.GESTANTE -> Icons.Default.PregnantWoman
                AccessibilityReason.LACTANTE -> Icons.Default.ChildCare
                AccessibilityReason.MOBILIDADE_REDUZIDA -> Icons.Default.WheelchairPickup
            }

            BentoCard(
                onClick = { onSelect(reason) },
                cornerRadius = 16.dp,
                borderWidth = if (isSelected) 2.dp else 1.dp,
                borderColor = if (isSelected) UbusPrimary else MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) UbusPrimary else UbusText3,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = reason.displayName(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Lei Federal nº 10.048/2000",
                            style = MaterialTheme.typography.bodySmall,
                            color = UbusText3
                        )
                    }
                    if (isTemp) {
                        Surface(
                            color = UbusWarning.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = "Temporário",
                                color = UbusWarning,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNext,
            enabled = selected != null,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = UbusPrimary,
                disabledContainerColor = UbusPrimary.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Avançar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun WheelchairStep(
    needsWheelchair: Boolean,
    onToggle: (Boolean) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "Necessidade de Cadeira de Rodas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        BentoCard {
            Column {
                Text(
                    text = "Você utiliza cadeira de rodas para locomoção?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Preciso de ônibus com elevador",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = needsWheelchair,
                        onCheckedChange = onToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = UbusPrimary
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (needsWheelchair) {
            BentoCard {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Warning, null, tint = UbusWarning, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Ao ativar, a rota que você selecionar ficará marcada no sistema como rota que necessita obrigatoriamente de ônibus com elevador. Uma notificação será enviada ao gestor de frota.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        } else {
            BentoCard {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Info, null, tint = UbusPrimary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Estudantes preferenciais que não utilizam cadeira de rodas têm prioridade na escolha de assentos reservados, mas não ativam a exigência de elevador na rota.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = UbusPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Avançar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun DocumentStep(
    reason: AccessibilityReason?,
    fileName: String?,
    uploadPending: Boolean,
    fileUrl: String?,
    loading: Boolean,
    error: String?,
    onPickFile: () -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "Envio do Documento Comprobatório",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        BentoCard {
            Column {
                Text(
                    text = "Documentação Necessária:",
                    style = MaterialTheme.typography.titleSmall,
                    color = UbusText3
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = reason?.requiredDocuments() ?: "Documento comprobatório oficial.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        BentoCard {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = if (fileName != null || fileUrl != null) UbusSuccess else UbusPrimary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (fileName != null) {
                    Text(
                        text = fileName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                } else if (!fileUrl.isNullOrEmpty()) {
                    Text(
                        text = "Documento anexado anteriormente",
                        style = MaterialTheme.typography.bodyMedium,
                        color = UbusSuccess,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "Nenhum arquivo selecionado",
                        style = MaterialTheme.typography.bodyMedium,
                        color = UbusText3
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onPickFile,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (fileName != null || fileUrl != null) "Alterar Arquivo" else "Selecionar Arquivo")
                }
            }
        }

        if (uploadPending) {
            Spacer(modifier = Modifier.height(16.dp))
            BentoCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudQueue,
                        contentDescription = null,
                        tint = UbusWarning,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Upload indisponível temporariamente na nuvem. A solicitação será criada e os documentos serão enviados automaticamente quando a infraestrutura estiver pronta.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        if (!error.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                color = UbusDestructive,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onSubmit,
            enabled = (fileName != null || !fileUrl.isNullOrEmpty() || uploadPending) && !loading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = UbusPrimary,
                disabledContainerColor = UbusPrimary.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            } else {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enviar Solicitação", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Send, null, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
