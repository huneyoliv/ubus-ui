package com.ubusmobilidade.ubus.ui.screens.manager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.FleetRepository
import com.ubusmobilidade.ubus.data.model.CreatePickupPointPayload
import com.ubusmobilidade.ubus.data.model.UpdatePickupPointPayload
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.components.UbusButton
import com.ubusmobilidade.ubus.ui.components.UbusTextField
import com.ubusmobilidade.ubus.ui.theme.UbusDestructive
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary
import com.ubusmobilidade.ubus.ui.theme.UbusSuccess
import com.ubusmobilidade.ubus.ui.theme.UbusText3
import com.ubusmobilidade.ubus.ui.util.toUserMessage
import kotlinx.coroutines.launch

private fun isValidLatitude(value: String): Boolean {
    val d = value.toDoubleOrNull() ?: return false
    return d in -90.0..90.0
}

private fun isValidLongitude(value: String): Boolean {
    val d = value.toDoubleOrNull() ?: return false
    return d in -180.0..180.0
}

@Composable
fun ManagerPickupPointFormScreen(
    component: RootComponent,
    routeId: String,
    pointId: String? = null,
) {
    val isEditing = pointId != null
    val scope = rememberCoroutineScope()
    val apiClient = remember { ApiClient(component.authStorage, onUnauthorized = { component.logout() }) }
    val fleetRepo = remember { FleetRepository(apiClient) }

    var name by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf("") }
    var lng by remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(isEditing) }
    var saving by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    var latTouched by remember { mutableStateOf(false) }
    var lngTouched by remember { mutableStateOf(false) }

    val latError = latTouched && lat.isNotEmpty() && !isValidLatitude(lat)
    val lngError = lngTouched && lng.isNotEmpty() && !isValidLongitude(lng)

    val isFormValid = name.isNotBlank()
        && isValidLatitude(lat)
        && isValidLongitude(lng)

    LaunchedEffect(pointId) {
        if (pointId == null) return@LaunchedEffect
        try {
            val points = fleetRepo.listPickupPoints(routeId)
            val point = points.find { it.id == pointId }
            if (point != null) {
                name = point.name
                lat = point.lat?.toString() ?: ""
                lng = point.lng?.toString() ?: ""
            } else {
                feedbackMessage = "Ponto não encontrado."
                isError = true
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            feedbackMessage = e.toUserMessage("Não foi possível carregar o ponto de embarque.")
            isError = true
        }
        loading = false
    }

    fun handleSave() {
        saving = true
        feedbackMessage = ""
        scope.launch {
            try {
                if (isEditing) {
                    fleetRepo.updatePickupPoint(
                        routeId = routeId,
                        pointId = pointId!!,
                        payload = UpdatePickupPointPayload(
                            name = name.trim(),
                            lat = lat.toDoubleOrNull(),
                            lng = lng.toDoubleOrNull(),
                        ),
                    )
                } else {
                    fleetRepo.createPickupPoint(
                        routeId = routeId,
                        payload = CreatePickupPointPayload(
                            name = name.trim(),
                            lat = lat.toDouble(),
                            lng = lng.toDouble(),
                        ),
                    )
                }
                component.goBack()
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                feedbackMessage = e.toUserMessage("Erro ao salvar ponto de embarque.")
                isError = true
            }
            saving = false
        }
    }

    fun handleDelete() {
        deleting = true
        scope.launch {
            try {
                fleetRepo.deletePickupPoint(routeId = routeId, pointId = pointId!!)
                component.goBack()
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                feedbackMessage = e.toUserMessage("Erro ao excluir ponto de embarque.")
                isError = true
            }
            deleting = false
            showDeleteDialog = false
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Excluir ponto", fontWeight = FontWeight.Bold) },
            text = { Text("Tem certeza que deseja excluir \"$name\"? Esta ação não pode ser desfeita.") },
            confirmButton = {
                Button(
                    onClick = { handleDelete() },
                    colors = ButtonDefaults.buttonColors(containerColor = UbusDestructive),
                    enabled = !deleting,
                ) {
                    if (deleting) {
                        CircularProgressIndicator(modifier = Modifier.padding(horizontal = 8.dp), color = MaterialTheme.colorScheme.onError, strokeWidth = 2.dp)
                    } else {
                        Text("Excluir")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }, enabled = !deleting) {
                    Text("Cancelar")
                }
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp),
        ) {
            IconButton(onClick = { component.goBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
            }
            Text(
                text = if (isEditing) "Editar Ponto de Embarque" else "Novo Ponto de Embarque",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = UbusPrimary)
            }
            return@Column
        }

        Spacer(Modifier.height(8.dp))

        if (feedbackMessage.isNotEmpty()) {
            Text(
                text = feedbackMessage,
                color = if (isError) UbusDestructive else UbusSuccess,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }

        BentoCard {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                UbusTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nome do ponto",
                    placeholder = "Ex: Praça Central",
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = UbusPrimary,
                        modifier = Modifier.padding(top = 28.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.weight(1f)) {
                        UbusTextField(
                            value = lat,
                            onValueChange = {
                                lat = it
                                latTouched = true
                            },
                            label = "Latitude",
                            placeholder = "Ex: -10.9167",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            isError = latError,
                            errorMessage = "Deve estar entre -90 e 90",
                        )
                        UbusTextField(
                            value = lng,
                            onValueChange = {
                                lng = it
                                lngTouched = true
                            },
                            label = "Longitude",
                            placeholder = "Ex: -37.0667",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            isError = lngError,
                            errorMessage = "Deve estar entre -180 e 180",
                        )
                    }
                }

                Text(
                    text = "Digite as coordenadas GPS do ponto de embarque em graus decimais.",
                    style = MaterialTheme.typography.bodySmall,
                    color = UbusText3,
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        UbusButton(
            text = if (isEditing) "Salvar Alterações" else "Cadastrar Ponto",
            enabled = isFormValid && !saving,
            loading = saving,
            onClick = { handleSave() },
            modifier = Modifier.fillMaxWidth(),
        )

        if (isEditing) {
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { showDeleteDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = UbusDestructive),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = !saving,
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Excluir Ponto")
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}
