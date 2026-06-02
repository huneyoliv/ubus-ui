package com.ubusmobilidade.ubus.ui.screens.driver

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.FleetRepository
import com.ubusmobilidade.ubus.data.model.CreateBusPayload
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.components.UbusButton
import com.ubusmobilidade.ubus.ui.components.UbusTextField
import com.ubusmobilidade.ubus.ui.theme.UbusBorder
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary
import com.ubusmobilidade.ubus.ui.theme.UbusSuccess
import com.ubusmobilidade.ubus.ui.theme.UbusText3
import com.ubusmobilidade.ubus.ui.util.toUserMessage
import kotlinx.coroutines.launch

@Composable
fun CadastroVeiculoMultiStepScreen(component: RootComponent) {
    val scope = rememberCoroutineScope()
    val apiClient = remember { ApiClient(component.authStorage, onUnauthorized = { component.logout() }) }
    val fleetRepo = remember { FleetRepository(apiClient) }

    var currentStep by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Dados do Veículo
    var plate by remember { mutableStateOf("") }
    var identificationNumber by remember { mutableStateOf("") }
    var hasElevator by remember { mutableStateOf<Boolean?>(null) }
    var hasNumberedSeats by remember { mutableStateOf<Boolean?>(null) }
    var lastSeatNumber by remember { mutableStateOf("") }
    var firstRowLayout = remember { mutableStateListOf("", "", "", "") }
    var hasBathroom by remember { mutableStateOf<Boolean?>(null) }
    var capacityFromDoc by remember { mutableStateOf("") }

    fun calculateCapacity(): Int {
        if (hasNumberedSeats == false) {
            return capacityFromDoc.toIntOrNull() ?: 0
        }
        
        var base = lastSeatNumber.toIntOrNull() ?: 0
        // Se tem banheiro, perde 1 poltrona na última fileira
        if (hasBathroom == true) base -= 1
        
        // Primeira fileira (se alguma poltrona foi removida/está vazia)
        val missingInFirstRow = firstRowLayout.count { it.isBlank() }
        base -= missingInFirstRow
        
        // Elevador costuma ocupar espaço mas o usuário disse "tem 1 espaço reservado além das poltronas"
        // Isso sugere que a capacidade total (lugares) não diminui, ou talvez aumente?
        // Vou assumir que a capacidade em poltronas é o que calculamos acima.
        return base
    }

    fun isValidPlate(plate: String): Boolean {
        val clean = plate.replace("-", "").uppercase()
        val oldPattern = Regex("^[A-Z]{3}\\d{4}$")
        val mercosulPattern = Regex("^[A-Z]{3}\\d[A-Z]\\d{2}$")
        return oldPattern.matches(clean) || mercosulPattern.matches(clean)
    }

    fun handleSave() {
        val finalCapacity = calculateCapacity()
        loading = true
        error = null
        scope.launch {
            try {
                fleetRepo.createBus(
                    CreateBusPayload(
                        municipalityId = component.authStorage.user?.municipalityId,
                        identificationNumber = identificationNumber,
                        plate = plate.uppercase(),
                        standardCapacity = finalCapacity,
                        hasBathroom = hasBathroom,
                        hasAirConditioning = false,
                        hasElevator = hasElevator
                    )
                )
                component.goBack()
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                error = e.toUserMessage("Erro ao cadastrar veículo.")
            } finally {
                loading = false
            }
        }
    }

    fun handleAdvance() {
        when (currentStep) {
            0 -> {
                if (plate.isNotBlank() && identificationNumber.isNotBlank()) {
                    if (isValidPlate(plate)) currentStep = 1
                    else error = "Placa inválida. Use o padrão AAA-1234 ou ABC1D23."
                } else {
                    error = "Preencha a placa e o número do veículo."
                }
            }
            1 -> if (hasElevator != null) currentStep = 2
            2 -> {
                if (hasNumberedSeats == true) currentStep = 3
                else if (hasNumberedSeats == false) currentStep = 4 // Pula P3 se não houver numeração
            }
            3 -> if (lastSeatNumber.isNotBlank()) currentStep = 4
            4 -> if (hasBathroom != null) currentStep = 5
            5 -> handleSave()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
    ) {
        IconButton(onClick = { component.goBack() }, modifier = Modifier.padding(top = 8.dp, start = 8.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = MaterialTheme.colorScheme.onBackground)
        }

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
        ) {
            Text("Novo Veículo", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            Text("Siga as etapas para configurar o ônibus.", style = MaterialTheme.typography.bodyMedium, color = UbusText3)
            Spacer(Modifier.height(32.dp))

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                }
            ) { step ->
                Column {
                    when (step) {
                        0 -> StepPlate(
                            plate = plate, onPlateChange = { if (it.length <= 7) plate = it },
                            id = identificationNumber, onIdChange = { identificationNumber = it }
                        )
                        1 -> QuestionStep("O ônibus tem elevador para cadeirante?", selected = hasElevator, onSelect = { hasElevator = it; error = null })
                        2 -> QuestionStep("O ônibus tem numeração nas poltronas?", selected = hasNumberedSeats, onSelect = { hasNumberedSeats = it; error = null })
                        3 -> StepLastSeat(lastSeatNumber, onValueChange = { lastSeatNumber = it; error = null })
                        4 -> QuestionStep("O ônibus tem banheiro?", selected = hasBathroom, onSelect = { hasBathroom = it; error = null })
                        5 -> QuestionStep("O ônibus é executivo (leito)?", subtitle = "Determina o layout de 2 ou 4 colunas", selected = null, onSelect = { /* Mock por enquanto */ handleSave() })
                    }
                }
            }

            error?.let {
                Spacer(Modifier.height(16.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }

        UbusButton(
            text = if (currentStep == 5) "Concluir e Salvar" else "Avançar",
            onClick = { handleAdvance() },
            loading = loading,
            modifier = Modifier.padding(24.dp)
        )
    }
}

@Composable
private fun StepPlate(plate: String, onPlateChange: (String) -> Unit, id: String, onIdChange: (String) -> Unit) {
    UbusTextField(
        value = plate,
        onValueChange = {
            val clean = it.replace("-", "").uppercase().take(7)
            val formatted = if (clean.length > 3) {
                clean.substring(0, 3) + "-" + clean.substring(3)
            } else {
                clean
            }
            onPlateChange(formatted)
        },
        label = "Placa do Veículo",
        placeholder = "ABC-1234"
    )
    Spacer(Modifier.height(16.dp))
    UbusTextField(value = id, onValueChange = onIdChange, label = "Número de Identificação", placeholder = "Ex: 1050")
}

@Composable
private fun QuestionStep(question: String, subtitle: String? = null, selected: Boolean?, onSelect: (Boolean) -> Unit) {
    Text(question, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = UbusText3) }
    Spacer(Modifier.height(24.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SelectCard("Sim", isSelected = selected == true, onClick = { onSelect(true) }, Modifier.weight(1f))
        SelectCard("Não", isSelected = selected == false, onClick = { onSelect(false) }, Modifier.weight(1f))
    }
}

@Composable
private fun SelectCard(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    BentoCard(
        modifier = modifier.clickable { onClick() }.border(2.dp, if (isSelected) UbusPrimary else Color.Transparent, MaterialTheme.shapes.large)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
            Text(text, style = MaterialTheme.typography.titleMedium, color = if (isSelected) UbusPrimary else MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun StepLastSeat(value: String, onValueChange: (String) -> Unit) {
    Text("Qual o número da última poltrona?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(16.dp))
    UbusTextField(value = value, onValueChange = onValueChange, label = "Número da última poltrona", placeholder = "Ex: 48")
}

@Composable
private fun StepFirstRow(layout: SnapshotStateList<String>) {
    Text("Configuração da primeira fileira", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Text("Digite o número da poltrona ou deixe vazio se não existir.", style = MaterialTheme.typography.bodySmall, color = UbusText3)
    Spacer(Modifier.height(32.dp))
    
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Column {
            Text("Esquerda", style = MaterialTheme.typography.labelSmall)
            Row {
                SeatItemInput(layout[0]) { layout[0] = it }
                Spacer(Modifier.width(8.dp))
                SeatItemInput(layout[1]) { layout[1] = it }
            }
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(24.dp))
            Text("CORREDOR", style = MaterialTheme.typography.labelSmall, color = UbusText3)
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text("Direita", style = MaterialTheme.typography.labelSmall)
            Row {
                SeatItemInput(layout[2]) { layout[2] = it }
                Spacer(Modifier.width(8.dp))
                SeatItemInput(layout[3]) { layout[3] = it }
            }
        }
    }
}

@Composable
private fun SeatItemInput(value: String, onValueChange: (String) -> Unit) {
    Box(
        modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp))
            .background(if (value.isNotEmpty()) UbusPrimary else UbusBorder.copy(alpha = 0.5f))
            .border(1.dp, if (value.isNotEmpty()) UbusPrimary else UbusBorder, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = value,
            onValueChange = { if (it.length <= 3) onValueChange(it) },
            textStyle = MaterialTheme.typography.titleMedium.copy(color = if (value.isNotEmpty()) Color.White else MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            cursorBrush = SolidColor(if (value.isNotEmpty()) Color.White else UbusPrimary)
        )
    }
}

@Composable
private fun SeatItem(number: Int, exists: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(if (exists) UbusPrimary else UbusBorder).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (exists) Text("$number", color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StepManualCapacity(value: String, onValueChange: (String) -> Unit) {
    Text("Capacidade do Veículo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Text("Consulte o CRLV ou a placa de lotação interna.", style = MaterialTheme.typography.bodySmall, color = UbusText3)
    Spacer(Modifier.height(16.dp))
    UbusTextField(value = value, onValueChange = onValueChange, label = "Total de lugares", placeholder = "Ex: 44")
}
