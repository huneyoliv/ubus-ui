package com.ubusmobilidade.ubus.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.material3.CircularProgressIndicator
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.UserRepository
import com.ubusmobilidade.ubus.data.api.UploadRepository
import com.ubusmobilidade.ubus.data.api.FleetRepository
import com.ubusmobilidade.ubus.data.model.RegistrationStatus
import com.ubusmobilidade.ubus.data.model.SemesterRenewalPayload
import com.ubusmobilidade.ubus.data.model.UploadType
import com.ubusmobilidade.ubus.data.model.Route
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.components.UbusButton
import com.ubusmobilidade.ubus.ui.components.UbusOutlinedButton
import com.ubusmobilidade.ubus.ui.theme.UbusBorder
import com.ubusmobilidade.ubus.ui.theme.UbusDestructive
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary
import com.ubusmobilidade.ubus.ui.theme.UbusPrimaryContainer
import com.ubusmobilidade.ubus.ui.theme.UbusSuccess
import com.ubusmobilidade.ubus.ui.theme.UbusText3
import com.ubusmobilidade.ubus.ui.theme.UbusWarning
import com.ubusmobilidade.ubus.ui.util.rememberFilePickerLauncher
import com.ubusmobilidade.ubus.ui.util.toUserMessage
import com.ubusmobilidade.ubus.ui.util.readFileBytes
import com.ubusmobilidade.ubus.ui.util.getFileNameFromUri
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.UploadFile

@Composable
fun RenovarSemestreScreen(component: RootComponent) {
    val scope = rememberCoroutineScope()
    val user = component.authStorage.user
    val apiClient = remember { ApiClient(component.authStorage, onUnauthorized = { component.logout() }) }
    val userRepo = remember { UserRepository(apiClient) }

    var gradeUri by remember { mutableStateOf<String?>(null) }
    var residenciaUri by remember { mutableStateOf<String?>(null) }
    var gradeSelected by remember { mutableStateOf(false) }
    var residenciaSelected by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    val currentUser = component.authStorage.user
    var routes by remember { mutableStateOf<List<Route>>(emptyList()) }
    var loadingRoutes by remember { mutableStateOf(true) }
    var selectedRouteId by remember { mutableStateOf<String?>(currentUser?.preferredRouteId) }

    LaunchedEffect(Unit) {
        try {
            routes = FleetRepository(apiClient).listRoutes()
        } catch (_: Exception) {
        } finally {
            loadingRoutes = false
        }
    }
    
    val statusApproved = user?.status == RegistrationStatus.APPROVED
    val statusPending = user?.status == RegistrationStatus.PENDING

    val gradePicker = rememberFilePickerLauncher { uri ->
        if (uri != null) {
            gradeUri = uri
            gradeSelected = true
        }
    }
    val residenciaPicker = rememberFilePickerLauncher { uri ->
        if (uri != null) {
            residenciaUri = uri
            residenciaSelected = true
        }
    }

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
            "Renovar semestre",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
        )

        BentoCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (statusApproved) {
                    Icon(Icons.Default.CheckCircle, null, tint = UbusSuccess, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Cadastro ativo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = UbusSuccess)
                        Text("Seu cadastro está aprovado para o semestre atual.", style = MaterialTheme.typography.bodySmall, color = UbusText3)
                    }
                } else if (statusPending) {
                    Icon(Icons.Default.HourglassTop, null, tint = UbusWarning, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Renovação pendente", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = UbusWarning)
                        Text("Sua renovação está em análise.", style = MaterialTheme.typography.bodySmall, color = UbusText3)
                    }
                } else {
                    Icon(Icons.Default.Info, null, tint = UbusText3, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Verifique seu status de cadastro.", style = MaterialTheme.typography.bodySmall, color = UbusText3)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        BentoCard {
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Info, null, tint = UbusPrimary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text(
                    "Envie o comprovante de matrícula e residência para renovar seu cadastro no sistema.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text("Documentos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(12.dp))

        DocumentUploadItem(
            label = "Comprovante de matrícula",
            selected = gradeSelected,
            onSelect = { 
                gradePicker()
            },
        )
        Spacer(Modifier.height(12.dp))

        DocumentUploadItem(
            label = "Comprovante de residência",
            selected = residenciaSelected,
            onSelect = { 
                residenciaPicker()
            },
        )

        Spacer(Modifier.height(24.dp))

        Text("Rota preferencial", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(8.dp))

        if (currentUser?.pendingPreferredRouteId != null) {
            BentoCard {
                Text(
                    "Mudança de rota já solicitada — aguardando aprovação do gestor.",
                    style = MaterialTheme.typography.bodySmall,
                    color = UbusWarning,
                )
            }
        } else {
            Text("Deseja alterar sua rota preferencial nesta renovação?", style = MaterialTheme.typography.bodySmall, color = UbusText3)
            Spacer(Modifier.height(12.dp))
            RouteSelectionList(routes, loadingRoutes, selectedRouteId, onSelect = { selectedRouteId = it })
        }

        Spacer(Modifier.height(24.dp))

        if (message.isNotEmpty()) {
            Text(message, color = if (isError) UbusDestructive else UbusSuccess, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(16.dp))
        }

        UbusButton(
            text = "Solicitar renovação",
            loading = loading,
            enabled = gradeSelected || residenciaSelected,
            onClick = {
                loading = true; message = ""
                scope.launch {
                    try {
                        val uploadRepo = UploadRepository(apiClient)
                        var finalGradeUrl: String? = gradeUri
                        var finalResidenciaUrl: String? = residenciaUri

                        gradeUri?.let { uri ->
                            val fileBytes = readFileBytes(uri)
                            val fileName = getFileNameFromUri(uri)
                            finalGradeUrl = uploadRepo.upload(fileBytes, fileName, UploadType.GRADE_DOCUMENT).fileUrl
                        }
                        residenciaUri?.let { uri ->
                            val fileBytes = readFileBytes(uri)
                            val fileName = getFileNameFromUri(uri)
                            finalResidenciaUrl = uploadRepo.upload(fileBytes, fileName, UploadType.RESIDENCIA_DOCUMENT).fileUrl
                        }

                        val resp = userRepo.requestSemesterRenewal(SemesterRenewalPayload(
                            gradeFileUrl = finalGradeUrl,
                            residenciaFileUrl = finalResidenciaUrl,
                            preferredRouteId = selectedRouteId,
                        ))
                        message = resp.message ?: "Solicitação enviada com sucesso!"
                        isError = false
                    } catch (e: Exception) {
                        if (e is kotlinx.coroutines.CancellationException) throw e
                        message = e.toUserMessage("Erro ao solicitar renovação.")
                        isError = true
                    }
                    loading = false
                }
            },
        )
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun DocumentUploadItem(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, if (selected) UbusPrimary else UbusBorder, RoundedCornerShape(12.dp))
            .background(if (selected) UbusPrimaryContainer else MaterialTheme.colorScheme.surface)
            .padding(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    if (selected) Icons.Default.Description else Icons.Default.UploadFile,
                    null,
                    tint = if (selected) UbusPrimary else UbusText3,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground)
                    if (selected) {
                        Text("Documento selecionado", style = MaterialTheme.typography.labelSmall, color = UbusSuccess)
                    }
                }
            }
            UbusOutlinedButton(
                text = if (selected) "Alterar" else "Selecionar",
                onClick = onSelect,
                modifier = Modifier.width(120.dp),
            )
        }
    }
}

@Composable
private fun RouteSelectionList(
    routes: List<Route>,
    loading: Boolean,
    selectedId: String?,
    onSelect: (String?) -> Unit,
) {
    if (loading) {
        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = UbusPrimary, modifier = Modifier.size(24.dp))
        }
    } else if (routes.isEmpty()) {
        BentoCard {
            Text("Nenhuma rota disponível.", color = UbusText3, style = MaterialTheme.typography.bodySmall)
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val isNoneSelected = selectedId == null
            BentoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(null) }
                    .then(if (isNoneSelected) Modifier.border(2.dp, UbusText3, MaterialTheme.shapes.large) else Modifier)
            ) {
                Text(
                    "Não tenho rota preferencial (serei caronista)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (isNoneSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isNoneSelected) MaterialTheme.colorScheme.onSurface else UbusText3,
                )
            }

            routes.forEach { route ->
                val isSelected = route.id == selectedId
                BentoCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(route.id) }
                        .then(if (isSelected) Modifier.border(2.dp, UbusPrimary, MaterialTheme.shapes.large) else Modifier)
                ) {
                    Column {
                        Text(
                            route.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) UbusPrimary else MaterialTheme.colorScheme.onBackground
                        )
                        if (!route.description.isNullOrBlank()) {
                            Text(route.description, style = MaterialTheme.typography.bodySmall, color = UbusText3)
                        }
                    }
                }
            }
        }
    }
}
