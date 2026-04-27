package com.ubusmobilidade.ubus.ui.screens.manager

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.FleetRepository
import com.ubusmobilidade.ubus.data.api.NotificationRepository
import com.ubusmobilidade.ubus.data.model.Route
import com.ubusmobilidade.ubus.data.model.SendNotificationPayload
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.components.UbusButton
import com.ubusmobilidade.ubus.ui.components.UbusTextField
import com.ubusmobilidade.ubus.ui.theme.UbusBorder
import com.ubusmobilidade.ubus.ui.theme.UbusDestructive
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary
import com.ubusmobilidade.ubus.ui.theme.UbusSuccess
import com.ubusmobilidade.ubus.ui.theme.UbusText3
import com.ubusmobilidade.ubus.ui.util.toUserMessage
import kotlinx.coroutines.launch

private const val TARGET_MUNICIPALITY = "MUNICIPALITY"
private const val TARGET_ROUTE = "ROUTE"

@Composable
fun ManagerNotificacoesScreen(component: RootComponent) {
    val scope = rememberCoroutineScope()
    val apiClient = remember { ApiClient(component.authStorage, onUnauthorized = { component.logout() }) }
    val notificationRepo = remember { NotificationRepository(apiClient) }
    val fleetRepo = remember { FleetRepository(apiClient) }

    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var selectedTarget by remember { mutableStateOf(TARGET_MUNICIPALITY) }
    var selectedRouteId by remember { mutableStateOf<String?>(null) }

    var routes by remember { mutableStateOf<List<Route>>(emptyList()) }
    var routesLoading by remember { mutableStateOf(false) }
    var routesError by remember { mutableStateOf("") }

    var sending by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    val user = component.authStorage.user

    // Load routes when target changes to ROUTE
    LaunchedEffect(selectedTarget) {
        if (selectedTarget == TARGET_ROUTE && routes.isEmpty()) {
            routesLoading = true
            routesError = ""
            try {
                routes = fleetRepo.listRoutes()
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                routesError = e.toUserMessage("Não foi possível carregar as rotas.")
            }
            routesLoading = false
        }
    }

    val targetId = when (selectedTarget) {
        TARGET_MUNICIPALITY -> user?.municipalityId ?: ""
        TARGET_ROUTE -> selectedRouteId ?: ""
        else -> ""
    }
    val formValid = title.isNotBlank() && message.isNotBlank() && targetId.isNotBlank()

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
    ) {
        IconButton(onClick = { component.goBack() }, modifier = Modifier.padding(top = 8.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = MaterialTheme.colorScheme.onBackground)
        }
        Text(
            "Notificações",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
        )
        Text(
            "Envie mensagens para os estudantes",
            style = MaterialTheme.typography.bodyMedium,
            color = UbusText3,
            modifier = Modifier.padding(bottom = 20.dp),
        )

        if (successMessage.isNotEmpty()) {
            BentoCard(modifier = Modifier.padding(bottom = 16.dp)) {
                Text(successMessage, color = UbusSuccess, style = MaterialTheme.typography.bodyMedium)
            }
        }

        UbusTextField(
            value = title,
            onValueChange = { title = it },
            label = "Título",
            placeholder = "Título da notificação",
        )
        Spacer(Modifier.height(12.dp))

        UbusTextField(
            value = message,
            onValueChange = { message = it },
            label = "Mensagem",
            placeholder = "Escreva a mensagem...",
            singleLine = false,
        )
        Spacer(Modifier.height(16.dp))

        Text(
            "Destinatários",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            val municipalitySelected = selectedTarget == TARGET_MUNICIPALITY
            BentoCard(
                modifier = Modifier.weight(1f)
                    .then(
                        if (municipalitySelected) Modifier.border(2.dp, UbusPrimary, RoundedCornerShape(18.dp))
                        else Modifier.border(1.dp, UbusBorder, RoundedCornerShape(18.dp))
                    )
                    .clickable {
                        selectedTarget = TARGET_MUNICIPALITY
                        selectedRouteId = null
                        routesError = ""
                    },
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Icon(Icons.Default.Groups, null, tint = if (municipalitySelected) UbusPrimary else UbusText3, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Todo o município",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (municipalitySelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (municipalitySelected) UbusPrimary else MaterialTheme.colorScheme.onBackground,
                    )
                }
            }

            val routeSelected = selectedTarget == TARGET_ROUTE
            BentoCard(
                modifier = Modifier.weight(1f)
                    .then(
                        if (routeSelected) Modifier.border(2.dp, UbusPrimary, RoundedCornerShape(18.dp))
                        else Modifier.border(1.dp, UbusBorder, RoundedCornerShape(18.dp))
                    )
                    .clickable {
                        selectedTarget = TARGET_ROUTE
                        routesError = ""
                    },
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Icon(Icons.Default.Route, null, tint = if (routeSelected) UbusPrimary else UbusText3, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Rota específica",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (routeSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (routeSelected) UbusPrimary else MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        // Route selection list
        if (selectedTarget == TARGET_ROUTE) {
            if (routesLoading) {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = UbusPrimary, modifier = Modifier.size(24.dp))
                }
            } else if (routesError.isNotBlank()) {
                Text(routesError, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            } else if (routes.isEmpty()) {
                Text("Nenhuma rota encontrada", style = MaterialTheme.typography.bodySmall, color = UbusText3)
            } else {
                Text(
                    "Selecione a rota:",
                    style = MaterialTheme.typography.bodySmall,
                    color = UbusText3,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                routes.forEach { route ->
                    val isSelected = selectedRouteId == route.id
                    BentoCard(
                        modifier = Modifier.padding(bottom = 8.dp)
                            .then(
                                if (isSelected) Modifier.border(2.dp, UbusPrimary, RoundedCornerShape(18.dp))
                                else Modifier
                            )
                            .clickable { selectedRouteId = route.id },
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Route, null,
                                tint = if (isSelected) UbusPrimary else UbusText3,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.padding(start = 12.dp))
                            Column {
                                Text(
                                    route.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) UbusPrimary else MaterialTheme.colorScheme.onBackground,
                                )
                                if (!route.description.isNullOrBlank()) {
                                    Text(route.description, style = MaterialTheme.typography.bodySmall, color = UbusText3)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        if (error.isNotEmpty()) {
            BentoCard(modifier = Modifier.padding(bottom = 12.dp)) {
                Text(error, color = UbusDestructive, style = MaterialTheme.typography.bodySmall)
            }
        }

        UbusButton(
            text = "Enviar notificação",
            enabled = formValid && !sending,
            loading = sending,
            onClick = {
                sending = true
                error = ""
                successMessage = ""
                scope.launch {
                    try {
                        val response = notificationRepo.send(
                            SendNotificationPayload(
                                title = title.trim(),
                                message = message.trim(),
                                target = selectedTarget,
                                targetId = targetId,
                            )
                        )
                        val count = response.recipientCount
                        successMessage = if (count != null) {
                            "Notificação enviada para $count destinatário(s)!"
                        } else {
                            "Notificação enviada com sucesso!"
                        }
                        title = ""
                        message = ""
                        selectedTarget = TARGET_MUNICIPALITY
                        selectedRouteId = null
                    } catch (e: Exception) {
                        if (e is kotlinx.coroutines.CancellationException) throw e
                        error = e.toUserMessage("Não foi possível enviar a notificação.")
                    }
                    sending = false
                }
            },
        )
        Spacer(Modifier.height(24.dp))
    }
}
