package com.ubusmobilidade.ubus.ui.screens.manager

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.BackendCapabilities
import com.ubusmobilidade.ubus.data.api.FleetRepository
import com.ubusmobilidade.ubus.data.api.TripRepository
import com.ubusmobilidade.ubus.data.api.UserRepository
import com.ubusmobilidade.ubus.data.model.Bus
import com.ubusmobilidade.ubus.data.model.Route
import com.ubusmobilidade.ubus.data.model.UpdateRoutePayload
import com.ubusmobilidade.ubus.data.model.User
import com.ubusmobilidade.ubus.data.model.CreateTripPayload
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

private fun getDayOfWeek(year: Int, month: Int, day: Int): Int {
    val t = intArrayOf(0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4)
    var y = year
    if (month < 3) {
        y -= 1
    }
    return (y + y / 4 - y / 100 + y / 400 + t[month - 1] + day) % 7
}

private fun getEasterDate(year: Int): Pair<Int, Int> {
    val a = year % 19
    val b = year / 100
    val c = year % 100
    val d = b / 4
    val e = b % 4
    val f = (b + 8) / 25
    val g = (b - f + 1) / 3
    val h = (19 * a + b - d - g + 15) % 30
    val i = c / 4
    val k = c % 4
    val l = (32 + 2 * e + 2 * i - h - k) % 7
    val m = (a + 11 * h + 22 * l) / 451
    val month = (h + l - 7 * m + 114) / 31
    val day = ((h + l - 7 * m + 114) % 31) + 1
    return Pair(month, day)
}

private fun dateToEpochDays(year: Int, month: Int, day: Int): Int {
    var y = year
    var m = month
    if (m <= 2) {
        y -= 1
        m += 12
    }
    val era = (if (y >= 0) y else y - 399) / 400
    val yoe = y - era * 400
    val doy = (153 * (m - 3) + 2) / 5 + day - 1
    val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
    return era * 146097 + doe - 719468
}

private fun epochDaysToDate(epochDays: Int): Triple<Int, Int, Int> {
    val z = epochDays + 719468
    val era = (if (z >= 0) z else z - 146096) / 146097
    val doe = z - era * 146097
    val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
    val y = yoe + era * 400
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
    val mp = (5 * doy + 2) / 153
    val d = doy - (153 * mp + 2) / 5 + 1
    val m = mp + (if (mp < 10) 3 else -9)
    return Triple(y, m + if (m <= 2) 1 else 0, d)
}

private fun getVariableHolidays(year: Int): Map<String, String> {
    val (easterMonth, easterDay) = getEasterDate(year)
    val easterEpoch = dateToEpochDays(year, easterMonth, easterDay)
    
    val carnivalMonday = epochDaysToDate(easterEpoch - 48)
    val carnivalTuesday = epochDaysToDate(easterEpoch - 47)
    val ashWednesday = epochDaysToDate(easterEpoch - 46)
    val goodFriday = epochDaysToDate(easterEpoch - 2)
    val corpusChristi = epochDaysToDate(easterEpoch + 60)
    
    fun formatDate(t: Triple<Int, Int, Int>): String {
        val y = t.first
        val m = if (t.second < 10) "0${t.second}" else "${t.second}"
        val d = if (t.third < 10) "0${t.third}" else "${t.third}"
        return "$y-$m-$d"
    }
    
    return mapOf(
        formatDate(carnivalMonday) to "Carnaval (Segunda-feira)",
        formatDate(carnivalTuesday) to "Carnaval (Terça-feira)",
        formatDate(ashWednesday) to "Quarta-feira de Cinzas",
        formatDate(goodFriday) to "Sexta-feira Santa",
        formatDate(corpusChristi) to "Corpus Christi"
    )
}

private fun getHolidayName(dateStr: String): String? {
    val parts = dateStr.split("-")
    if (parts.size != 3) return null
    val year = parts[0].toIntOrNull() ?: return null
    val month = parts[1]
    val day = parts[2]
    
    val fixedHoliday = when ("$month-$day") {
        "01-01" -> "Confraternização Universal"
        "04-21" -> "Tiradentes"
        "05-01" -> "Dia do Trabalho"
        "09-07" -> "Independência do Brasil"
        "10-12" -> "Nossa Senhora Aparecida"
        "11-02" -> "Finados"
        "11-15" -> "Proclamação da República"
        "11-20" -> "Consciência Negra"
        "12-25" -> "Natal"
        else -> null
    }
    if (fixedHoliday != null) return fixedHoliday
    
    return getVariableHolidays(year)[dateStr]
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ManagerRouteDetailScreen(component: RootComponent, routeId: String) {
    val scope = rememberCoroutineScope()
    val apiClient = remember { ApiClient(component.authStorage, onUnauthorized = { component.logout() }) }
    val fleetRepo = remember { FleetRepository(apiClient) }
    val userRepo = remember { UserRepository(apiClient) }
    val tripRepo = remember { TripRepository(apiClient) }
    
    var route by remember { mutableStateOf<Route?>(null) }
    var assignedBuses by remember { mutableStateOf<List<Bus>>(emptyList()) }
    var allBuses by remember { mutableStateOf<List<Bus>>(emptyList()) }
    var allDrivers by remember { mutableStateOf<List<User>>(emptyList()) }
    
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    
    // Form states
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var departureTimeOutbound by remember { mutableStateOf("") }
    var departureTimeInbound by remember { mutableStateOf("") }
    var selectedDriverId by remember { mutableStateOf<String?>(null) }
    var driverDropdownExpanded by remember { mutableStateOf(false) }
    
    // Calendar scheduling states
    var selectedMonth by remember { mutableStateOf("2026-06") }
    var selectedDates by remember { mutableStateOf<Set<String>>(emptySet()) }
    var scheduledTripsDates by remember { mutableStateOf<Set<String>>(emptySet()) }
    var generatingTrips by remember { mutableStateOf(false) }
    var generatingProgress by remember { mutableStateOf("") }

    LaunchedEffect(routeId, selectedMonth) {
        try {
            val routes = fleetRepo.listRoutes()
            val r = routes.find { it.id == routeId }
            if (r != null) {
                route = r
                name = r.name
                description = r.description ?: ""
                departureTimeOutbound = r.departureTimeOutbound ?: "06:30"
                departureTimeInbound = r.departureTimeInbound ?: "18:00"
                
                assignedBuses = fleetRepo.listBusesByRoute(routeId)
                allBuses = fleetRepo.listBuses()
                
                val drivers = userRepo.listUsers(role = com.ubusmobilidade.ubus.data.model.RoleUsuario.DRIVER)
                allDrivers = drivers
                
                // Pega o motorista do primeiro ônibus atribuído como padrão da rota
                val busDriver = assignedBuses.firstOrNull()?.driverId
                if (busDriver != null) {
                    selectedDriverId = busDriver
                }
                
                try {
                    val calendar = fleetRepo.getRouteCalendar(routeId, selectedMonth)
                    scheduledTripsDates = calendar.scheduledDates.toSet()
                } catch (e: Exception) {
                    scheduledTripsDates = emptySet()
                }
            } else {
                error = "Rota não encontrada."
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            error = e.toUserMessage("Não foi possível carregar os detalhes da rota.")
        }
        loading = false
    }

    fun handleSave() {
        val timeRegex = "^([01]\\d|2[0-3]):[0-5]\\d$".toRegex()
        if (!departureTimeOutbound.matches(timeRegex) || !departureTimeInbound.matches(timeRegex)) {
            error = "Os horários de partida devem estar no formato HH:mm."
            return
        }

        saving = true
        scope.launch {
            try {
                fleetRepo.updateRoute(routeId, UpdateRoutePayload(
                    name = name,
                    description = description,
                    departureTimeOutbound = departureTimeOutbound,
                    departureTimeInbound = departureTimeInbound
                ))
                error = "Alterações salvas com sucesso!"
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                error = e.toUserMessage("Erro ao salvar rota.")
            }
            saving = false
        }
    }

    fun assignBus(busId: String) {
        scope.launch {
            try {
                fleetRepo.assignBusToRoute(routeId, busId)
                assignedBuses = fleetRepo.listBusesByRoute(routeId)
                // Ao atribuir o ônibus, sugere o motorista padrão dele na rota
                val bus = assignedBuses.find { it.id == busId }
                if (bus?.driverId != null) {
                    selectedDriverId = bus.driverId
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                error = e.toUserMessage("Erro ao atribuir ônibus.")
            }
        }
    }

    fun removeBus(busId: String) {
        scope.launch {
            try {
                fleetRepo.removeBusFromRoute(routeId, busId)
                assignedBuses = assignedBuses.filter { it.id != busId }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                error = e.toUserMessage("Erro ao remover ônibus.")
            }
        }
    }

    fun handleAutoSelectWeekdays() {
        val parts = selectedMonth.split("-")
        if (parts.size != 2) return
        val year = parts[0].toIntOrNull() ?: 2026
        val month = parts[1].toIntOrNull() ?: 6
        
        val daysInMonth = when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) 29 else 28
            else -> 30
        }
        
        val newSelected = mutableSetOf<String>()
        for (day in 1..daysInMonth) {
            val dayStr = if (day < 10) "0$day" else "$day"
            val monthStr = if (month < 10) "0$month" else "$month"
            val dateStr = "$year-$monthStr-$dayStr"
            
            val dayOfWeek = getDayOfWeek(year, month, day)
            val isWeekend = dayOfWeek == 0 || dayOfWeek == 6
            val isScheduled = scheduledTripsDates.contains(dateStr)
            val isHoliday = getHolidayName(dateStr) != null
            
            if (!isWeekend && !isScheduled && !isHoliday) {
                newSelected.add(dateStr)
            }
        }
        selectedDates = newSelected
    }

    fun handleGenerateTrips() {
        if (assignedBuses.isEmpty()) {
            error = "Atribua pelo menos um ônibus à rota antes de agendar viagens."
            return
        }
        generatingTrips = true
        generatingProgress = "Iniciando agendamento..."
        scope.launch {
            try {
                val datesToGenerate = selectedDates.filter { !scheduledTripsDates.contains(it) }
                if (datesToGenerate.isEmpty()) {
                    error = "Nenhuma nova data selecionada para geração."
                    generatingTrips = false
                    return@launch
                }
                val busId = assignedBuses.first().id
                
                var count = 0
                datesToGenerate.forEach { date ->
                    generatingProgress = "Gerando dia $date (${count + 1}/${datesToGenerate.size})..."
                    
                    // 1. Viagem de Ida (OUTBOUND)
                    val outboundId = "trip_${routeId.take(8)}_${date.replace("-", "")}_O"
                    tripRepo.createTrip(
                        CreateTripPayload(
                            tripId = outboundId,
                            tripDate = date,
                            shift = "AFTERNOON",
                            direction = com.ubusmobilidade.ubus.data.model.TripDirection.OUTBOUND,
                            routeId = routeId,
                            busId = busId,
                            driverId = selectedDriverId,
                            realCapacity = assignedBuses.first().standardCapacity,
                            votingOpen = "${date}T00:00:00Z",
                            votingClose = "${date}T23:59:59Z",
                        )
                    )
                    
                    // 2. Viagem de Volta (INBOUND)
                    val inboundId = "trip_${routeId.take(8)}_${date.replace("-", "")}_I"
                    tripRepo.createTrip(
                        CreateTripPayload(
                            tripId = inboundId,
                            tripDate = date,
                            shift = "AFTERNOON",
                            direction = com.ubusmobilidade.ubus.data.model.TripDirection.INBOUND,
                            routeId = routeId,
                            busId = busId,
                            driverId = selectedDriverId,
                            realCapacity = assignedBuses.first().standardCapacity,
                            votingOpen = "${date}T00:00:00Z",
                            votingClose = "${date}T23:59:59Z",
                        )
                    )
                    count++
                }
                
                try {
                    val calendar = fleetRepo.getRouteCalendar(routeId, selectedMonth)
                    scheduledTripsDates = calendar.scheduledDates.toSet()
                } catch (e: Exception) {
                    scheduledTripsDates = scheduledTripsDates + datesToGenerate
                }
                
                selectedDates = emptySet()
                error = "Viagens geradas com sucesso para $count dias!"
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                error = e.toUserMessage("Erro ao gerar viagens.")
            } finally {
                generatingTrips = false
                generatingProgress = ""
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
            IconButton(onClick = { component.goBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
            }
            Text("Detalhes da Rota", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = UbusPrimary)
            }
        } else {
            Spacer(Modifier.height(16.dp))
            
            if (error.isNotEmpty()) {
                Text(error, color = if (error.contains("sucesso")) UbusSuccess else UbusDestructive, modifier = Modifier.padding(bottom = 16.dp))
            }

            BentoCard {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    UbusTextField(value = name, onValueChange = { name = it }, label = "Nome da Rota")
                    UbusTextField(value = description, onValueChange = { description = it }, label = "Descrição")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        UbusTextField(value = departureTimeOutbound, onValueChange = { departureTimeOutbound = it }, label = "Horário de Ida", modifier = Modifier.weight(1f), placeholder = "06:30")
                        UbusTextField(value = departureTimeInbound, onValueChange = { departureTimeInbound = it }, label = "Horário de Volta", modifier = Modifier.weight(1f), placeholder = "18:00")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            UbusButton(text = "Salvar Dados da Rota", onClick = { handleSave() }, loading = saving)

            // Seção de Atribuição de Motorista
            Spacer(Modifier.height(28.dp))
            Text("Motorista da Rota", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            BentoCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Person, null, tint = UbusPrimary)
                        Spacer(Modifier.width(12.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            val activeDriver = allDrivers.find { it.id == selectedDriverId }
                            Text(
                                text = activeDriver?.name ?: "Selecionar Motorista",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { driverDropdownExpanded = true }
                                    .padding(vertical = 8.dp)
                            )
                            DropdownMenu(
                                expanded = driverDropdownExpanded,
                                onDismissRequest = { driverDropdownExpanded = false }
                            ) {
                                allDrivers.forEach { d ->
                                    DropdownMenuItem(
                                        text = { Text(d.name) },
                                        onClick = {
                                            selectedDriverId = d.id
                                            driverDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Seção de Pontos de Embarque
            Spacer(Modifier.height(28.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Pontos de Embarque", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (route?.requiresElevator == true) {
                        Spacer(Modifier.width(8.dp))
                        Text("♿", style = MaterialTheme.typography.titleMedium)
                    }
                }
                TextButton(onClick = {
                    component.navigateTo(RootComponent.Config.ManagerPickupPointForm(routeId))
                }) {
                    Icon(Icons.Default.Add, null, tint = UbusPrimary)
                    Spacer(Modifier.width(4.dp))
                    Text("Novo", color = UbusPrimary, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(8.dp))

            var pickupPoints by remember { mutableStateOf<List<com.ubusmobilidade.ubus.data.model.PickupPoint>>(emptyList()) }
            var loadingPoints by remember { mutableStateOf(true) }
            var pointToDelete by remember { mutableStateOf<com.ubusmobilidade.ubus.data.model.PickupPoint?>(null) }
            var deletingPoint by remember { mutableStateOf(false) }

            LaunchedEffect(routeId) {
                loadingPoints = true
                try {
                    pickupPoints = fleetRepo.listPickupPoints(routeId)
                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                }
                loadingPoints = false
            }

            if (pointToDelete != null) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { if (!deletingPoint) pointToDelete = null },
                    title = { Text("Excluir ponto", fontWeight = FontWeight.Bold) },
                    text = { Text("Excluir \"${pointToDelete!!.name}\"? Esta ação não pode ser desfeita.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                deletingPoint = true
                                val target = pointToDelete!!
                                scope.launch {
                                    try {
                                        fleetRepo.deletePickupPoint(routeId, target.id)
                                        pickupPoints = pickupPoints.filter { it.id != target.id }
                                    } catch (e: Exception) {
                                        if (e is kotlinx.coroutines.CancellationException) throw e
                                        error = e.toUserMessage("Erro ao excluir ponto.")
                                    }
                                    deletingPoint = false
                                    pointToDelete = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = UbusDestructive),
                            enabled = !deletingPoint,
                        ) {
                            if (deletingPoint) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.onError,
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Text("Excluir")
                            }
                        }
                    },
                    dismissButton = {
                        androidx.compose.material3.TextButton(
                            onClick = { pointToDelete = null },
                            enabled = !deletingPoint,
                        ) {
                            Text("Cancelar")
                        }
                    },
                )
            }

            if (loadingPoints) {
                Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = UbusPrimary, strokeWidth = 2.dp)
                }
            } else if (pickupPoints.isEmpty()) {
                BentoCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Nenhum ponto cadastrado. Toque em \"Novo\" para adicionar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = UbusText3,
                    )
                }
            } else {
                pickupPoints.forEach { point ->
                    BentoCard(modifier = Modifier.padding(bottom = 8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = UbusPrimary)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(point.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                if (point.lat != null && point.lng != null) {
                                    Text(
                                        "${point.lat}, ${point.lng}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = UbusText3,
                                    )
                                }
                            }
                            IconButton(onClick = {
                                component.navigateTo(RootComponent.Config.ManagerPickupPointForm(routeId, point.id))
                            }) {
                                Icon(Icons.Default.Edit, "Editar", tint = UbusPrimary)
                            }
                            IconButton(onClick = { pointToDelete = point }) {
                                Icon(Icons.Default.Delete, "Excluir", tint = UbusDestructive)
                            }
                        }
                    }
                }
            }

            // Calendário de Agendamento
            Spacer(Modifier.height(28.dp))
            Text("Calendário de Viagens", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(8.dp))
            BentoCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Seleção de Mês
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                        listOf("2026-06" to "Junho 2026", "2026-07" to "Julho 2026").forEach { (valMonth, labelMonth) ->
                            val isSel = selectedMonth == valMonth
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) UbusPrimary else Color(0xFFF1F5F9))
                                    .clickable { selectedMonth = valMonth; selectedDates = emptySet() }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(labelMonth, color = if (isSel) Color.White else UbusText3, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Dias da semana header
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        listOf("D", "S", "T", "Q", "Q", "S", "S").forEach {
                            Text(
                                text = it,
                                modifier = Modifier.width(36.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = UbusText3
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))

                    // Processamento e renderização do grid de dias do calendário real
                    val parts = selectedMonth.split("-")
                    val year = parts[0].toIntOrNull() ?: 2026
                    val month = parts[1].toIntOrNull() ?: 6
                    val daysInMonth = when (month) {
                        1, 3, 5, 7, 8, 10, 12 -> 31
                        4, 6, 9, 11 -> 30
                        2 -> if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) 29 else 28
                        else -> 30
                    }
                    
                    val firstDayOfWeek = getDayOfWeek(year, month, 1) // 0 = Domingo, 1 = Segunda, etc.
                    
                    val calendarItems = mutableListOf<String?>()
                    for (i in 0 until firstDayOfWeek) {
                        calendarItems.add(null)
                    }
                    for (day in 1..daysInMonth) {
                        val dayStr = if (day < 10) "0$day" else "$day"
                        val monthStr = if (month < 10) "0$month" else "$month"
                        calendarItems.add("$year-$monthStr-$dayStr")
                    }
                    
                    // Renderiza as semanas
                    calendarItems.chunked(7).forEach { week ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            week.forEach { date ->
                                if (date == null) {
                                    Spacer(Modifier.size(36.dp))
                                } else {
                                    val dayNum = date.split("-").last().toInt()
                                    val dayOfWeek = getDayOfWeek(year, month, dayNum)
                                    val isWeekend = dayOfWeek == 0 || dayOfWeek == 6
                                    val isSelected = selectedDates.contains(date)
                                    val isScheduled = scheduledTripsDates.contains(date)
                                    val holidayName = getHolidayName(date)
                                    val isHoliday = holidayName != null
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isScheduled -> UbusSuccess.copy(alpha = 0.2f)
                                                    isSelected -> UbusPrimary
                                                    isHoliday -> Color(0xFFFEE2E2)
                                                    isWeekend -> Color(0xFFF8FAFC)
                                                    else -> Color(0xFFF1F5F9)
                                                }
                                            )
                                            .clickable(enabled = !isScheduled) {
                                                if (isSelected) {
                                                    selectedDates = selectedDates - date
                                                } else {
                                                    selectedDates = selectedDates + date
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isScheduled) {
                                            Icon(Icons.Default.Check, null, tint = UbusSuccess, modifier = Modifier.size(16.dp))
                                        } else {
                                            Text(
                                                text = "$dayNum",
                                                color = when {
                                                    isSelected -> Color.White
                                                    isHoliday -> Color(0xFFDC2626)
                                                    isWeekend -> UbusText3.copy(alpha = 0.4f)
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                },
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                            // Preenche o espaço se a última semana tiver menos de 7 dias
                            if (week.size < 7) {
                                for (i in 0 until (7 - week.size)) {
                                    Spacer(Modifier.size(36.dp))
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(UbusSuccess.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Check, null, tint = UbusSuccess, modifier = Modifier.size(10.dp))
                            }
                            Text("Agendado", fontSize = 10.sp, color = UbusText3, fontWeight = FontWeight.Medium)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFEE2E2)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("F", color = Color(0xFFDC2626), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("Feriado", fontSize = 10.sp, color = UbusText3, fontWeight = FontWeight.Medium)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF8FAFC))
                            )
                            Text("Fim de semana", fontSize = 10.sp, color = UbusText3, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { handleAutoSelectWeekdays() }) {
                            Text("Auto-Seleção (Seg a Sex)", color = UbusPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        
                        Text(
                            text = "${selectedDates.size} selecionados",
                            style = MaterialTheme.typography.bodySmall,
                            color = UbusText3
                        )
                    }

                    if (generatingTrips) {
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = UbusPrimary, strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text(generatingProgress, style = MaterialTheme.typography.bodySmall, color = UbusPrimary)
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    UbusButton(
                        text = "Gerar Viagens no Calendário",
                        enabled = selectedDates.isNotEmpty() && !generatingTrips,
                        onClick = { handleGenerateTrips() }
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
            Text("Ônibus Atribuídos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            
            assignedBuses.forEach { bus ->
                BentoCard(modifier = Modifier.padding(bottom = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DirectionsBus, null, tint = UbusPrimary)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Nº ${bus.identificationNumber} · ${bus.plate}", style = MaterialTheme.typography.bodyMedium)
                            Text("${bus.standardCapacity} lugares", style = MaterialTheme.typography.bodySmall, color = UbusText3)
                        }
                        IconButton(onClick = { removeBus(bus.id) }) {
                            Icon(Icons.Default.Delete, null, tint = UbusDestructive)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Atribuir mais ônibus", style = MaterialTheme.typography.labelLarge, color = UbusText3)
            Spacer(Modifier.height(8.dp))
            
            val availableBuses = allBuses.filter { ab -> assignedBuses.none { it.id == ab.id } }
            availableBuses.forEach { bus ->
                BentoCard(
                    modifier = Modifier.padding(bottom = 8.dp),
                    onClick = { assignBus(bus.id) }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, null, tint = UbusPrimary)
                        Spacer(Modifier.width(12.dp))
                        Text("Nº ${bus.identificationNumber} (${bus.plate})", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}
