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
import com.ubusmobilidade.ubus.data.model.RegistrationStatus
import com.ubusmobilidade.ubus.data.model.SemesterRenewalPayload
import com.ubusmobilidade.ubus.data.model.UploadType
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.style.TextAlign
import com.ubusmobilidade.ubus.data.api.ApiError
import com.ubusmobilidade.ubus.data.model.UniversityCityRequiredError
import com.ubusmobilidade.ubus.ui.components.UbusTextField
import kotlinx.serialization.json.Json

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

    var showCitySelector by remember { mutableStateOf(false) }
    var availableCities by remember { mutableStateOf<List<String>>(emptyList()) }
    var citySearchQuery by remember { mutableStateOf("") }

    val currentUser = component.authStorage.user
    
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

    fun performRenewal(city: String? = null) {
        loading = true
        message = ""
        scope.launch {
            try {
                val uploadRepo = UploadRepository(apiClient)
                var finalResidenciaUrl: String? = residenciaUri

                gradeUri?.let { uri ->
                    val fileBytes = readFileBytes(uri)
                    val fileName = getFileNameFromUri(uri)
                    userRepo.submitStudentSchedule(fileBytes, fileName, city)
                }

                residenciaUri?.let { uri ->
                    val fileBytes = readFileBytes(uri)
                    val fileName = getFileNameFromUri(uri)
                    finalResidenciaUrl = uploadRepo.upload(fileBytes, fileName, UploadType.RESIDENCIA_DOCUMENT).fileUrl
                }

                val resp = userRepo.requestSemesterRenewal(SemesterRenewalPayload(
                    gradeFileUrl = null,
                    residenciaFileUrl = finalResidenciaUrl,
                    preferredRouteId = null,
                ))
                message = resp.message ?: "Solicitação enviada com sucesso!"
                isError = false
                showCitySelector = false
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                if (e is ApiError && e.status == 422 && e.body != null) {
                    try {
                        val errorDetail = Json { ignoreUnknownKeys = true }.decodeFromString<UniversityCityRequiredError>(e.body)
                        if (errorDetail.error == "UNIVERSITY_CITY_REQUIRED") {
                            availableCities = errorDetail.availableCities
                            showCitySelector = true
                            loading = false
                            return@launch
                        }
                    } catch (_: Exception) {}
                }
                message = e.toUserMessage("Erro ao solicitar renovação.")
                isError = true
            }
            loading = false
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

        if (message.isNotEmpty()) {
            Text(message, color = if (isError) UbusDestructive else UbusSuccess, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(16.dp))
        }

        UbusButton(
            text = "Solicitar renovação",
            loading = loading,
            enabled = (gradeSelected || residenciaSelected) && !statusApproved && !statusPending,
            onClick = { performRenewal() },
        )
        Spacer(Modifier.height(32.dp))
    }

    if (showCitySelector) {
        val filteredCities = availableCities.filter {
            it.contains(citySearchQuery, ignoreCase = true)
        }
        AlertDialog(
            onDismissRequest = { showCitySelector = false },
            title = { Text("Cidade da Faculdade", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Não identificamos a cidade da sua faculdade na grade. Selecione uma das cidades de desembarque abaixo:",
                        style = MaterialTheme.typography.bodySmall,
                        color = UbusText3,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    UbusTextField(
                        value = citySearchQuery,
                        onValueChange = { citySearchQuery = it },
                        label = "Pesquisar cidade",
                        placeholder = "Digite o nome da cidade...",
                        leadingIcon = { Icon(Icons.Default.Search, null) }
                    )
                    Spacer(Modifier.height(8.dp))
                    Box(modifier = Modifier.height(180.dp).fillMaxWidth().border(1.dp, UbusBorder, RoundedCornerShape(8.dp)).clip(RoundedCornerShape(8.dp))) {
                        LazyColumn {
                            items(filteredCities) { city ->
                                Text(
                                    text = city,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            showCitySelector = false
                                            performRenewal(city)
                                        }
                                        .padding(14.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            if (filteredCities.isEmpty()) {
                                item {
                                    Text(
                                        text = "Nenhuma cidade encontrada",
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = UbusText3,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCitySelector = false }) {
                    Text("Cancelar", color = UbusDestructive)
                }
            }
        )
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


