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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.FleetRepository
import com.ubusmobilidade.ubus.data.model.AccessibilityFeature
import com.ubusmobilidade.ubus.data.model.BusCell
import com.ubusmobilidade.ubus.data.model.BusLayout
import com.ubusmobilidade.ubus.data.model.BusWizardAnswers
import com.ubusmobilidade.ubus.data.model.CellType
import com.ubusmobilidade.ubus.data.model.CreateBusPayload
import com.ubusmobilidade.ubus.data.model.FrontRowLayout
import com.ubusmobilidade.ubus.data.model.NumerationSide
import com.ubusmobilidade.ubus.data.model.NumberingPattern
import com.ubusmobilidade.ubus.data.model.RearLayout
import com.ubusmobilidade.ubus.data.model.SaveBusLayoutPayload
import com.ubusmobilidade.ubus.data.model.SeatNumberingMode
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.components.BusCellState
import com.ubusmobilidade.ubus.ui.components.BusCellView
import com.ubusmobilidade.ubus.ui.components.UbusButton
import com.ubusmobilidade.ubus.ui.components.UbusTextField
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary
import com.ubusmobilidade.ubus.ui.theme.UbusText3
import com.ubusmobilidade.ubus.ui.util.BusLayoutEngine
import com.ubusmobilidade.ubus.ui.util.toUserMessage
import kotlinx.coroutines.launch

@Composable
fun CadastroVeiculoMultiStepScreen(component: RootComponent, municipalityId: String? = null) {
    val scope = rememberCoroutineScope()
    val apiClient = remember { ApiClient(component.authStorage, onUnauthorized = { component.logout() }) }
    val fleetRepo = remember { FleetRepository(apiClient) }

    // Steps: 0=Placa, 1=P1, 2=P2, 3=P3, 4=P4, 5=P5, 6=P6-A, 10=P6-B, 11=VirtualCapacity
    // 7=Validation, 8=ShellPreview(MIXED), 9=FinalMap
    var currentStep by remember { mutableIntStateOf(0) }
    var plate by remember { mutableStateOf("") }
    var identificationNumber by remember { mutableStateOf("") }
    var p1 by remember { mutableStateOf<SeatNumberingMode?>(null) }
    var p2 by remember { mutableStateOf<FrontRowLayout?>(null) }
    var p3 by remember { mutableStateOf<RearLayout?>(null) }
    var p4capacity by remember { mutableStateOf<Int?>(null) }
    var p5 by remember { mutableStateOf<AccessibilityFeature?>(null) }
    var p6 by remember { mutableStateOf<NumerationSide?>(null) }
    var p6b by remember { mutableStateOf<NumberingPattern?>(null) }
    var virtualCapacity by remember { mutableStateOf("") }
    val p7numbers = remember { mutableStateMapOf<Int, Int>() }

    var validationError by remember { mutableStateOf<String?>(null) }
    var layout by remember { mutableStateOf<BusLayout?>(null) }
    var dpmWarning by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var activeDialogSeat by remember { mutableStateOf<BusCell?>(null) }

    fun buildAnswers(): BusWizardAnswers {
        return BusWizardAnswers(
            plate = plate.uppercase(),
            identificationNumber = identificationNumber.trim(),
            p1 = p1 ?: SeatNumberingMode.PHYSICAL,
            p2 = p2 ?: FrontRowLayout.FOUR,
            p3 = p3 ?: RearLayout.NORMAL,
            p4capacity = if (p1 == SeatNumberingMode.VIRTUAL) (virtualCapacity.toIntOrNull() ?: 44) else (p4capacity ?: 44),
            p5 = p5 ?: AccessibilityFeature.NONE,
            p6 = p6 ?: NumerationSide.LEFT,
            p6b = p6b ?: NumberingPattern.SEQUENTIAL,
            p7physicalNumbers = p7numbers.toMap()
        )
    }

    fun computeDpmWarningMessage(answers: BusWizardAnswers, builtLayout: BusLayout): String? {
        if (answers.p5 != AccessibilityFeature.DPM) return null
        val firstRow = builtLayout.rows.firstOrNull() ?: return null
        return if (answers.p6 == NumerationSide.LEFT) {
            if (firstRow.cells[3].type != CellType.SEAT) {
                "Atenção: O assento acessível (DPM) foi posicionado na janela da primeira fileira (poltrona ${firstRow.cells[4].virtualNumber}) porque a posição de corredor está vazia."
            } else null
        } else {
            if (firstRow.cells[1].type != CellType.SEAT) {
                "Atenção: O assento acessível (DPM) foi posicionado na janela da primeira fileira (poltrona ${firstRow.cells[0].virtualNumber}) porque a posição de corredor está vazia."
            } else null
        }
    }

    fun isValidPlate(plate: String): Boolean {
        val clean = plate.replace("-", "").uppercase()
        val oldPattern = Regex("^[A-Z]{3}\\d{4}$")
        val mercosulPattern = Regex("^[A-Z]{3}\\d[A-Z]\\d{2}$")
        return oldPattern.matches(clean) || mercosulPattern.matches(clean)
    }

    suspend fun handleSaveVirtual() {
        loading = true
        error = null
        try {
            val capacity = virtualCapacity.toIntOrNull() ?: 44
            fleetRepo.createBus(
                CreateBusPayload(
                    municipalityId = municipalityId ?: component.authStorage.user?.municipalityId,
                    identificationNumber = identificationNumber.trim(),
                    plate = plate.uppercase(),
                    standardCapacity = capacity,
                    hasBathroom = false,
                    hasAirConditioning = false,
                    hasElevator = p5 == AccessibilityFeature.BOX,
                    preferentialSeats = emptyList()
                )
            )
            component.goBack()
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            error = e.toUserMessage("Erro ao cadastrar ônibus.")
        } finally {
            loading = false
        }
    }

    suspend fun handleSave() {
        loading = true
        error = null
        try {
            val answers = buildAnswers()
            val finalLayout = BusLayoutEngine.applyPhysicalNumbers(layout!!, p7numbers.toMap())
            val dpmNum = BusLayoutEngine.computeDpmVirtualNumber(answers, finalLayout)

            val bus = fleetRepo.createBus(
                CreateBusPayload(
                    municipalityId = municipalityId ?: component.authStorage.user?.municipalityId,
                    identificationNumber = identificationNumber.trim(),
                    plate = plate.uppercase(),
                    standardCapacity = answers.p4capacity,
                    hasBathroom = answers.p3 == RearLayout.BATHROOM,
                    hasAirConditioning = false,
                    hasElevator = answers.p5 == AccessibilityFeature.BOX || answers.p3 == RearLayout.BOX,
                    preferentialSeats = if (dpmNum != null) listOf(dpmNum) else emptyList()
                )
            )

            fleetRepo.saveBusLayout(
                bus.id,
                SaveBusLayoutPayload(
                    numberingMode = answers.p1,
                    numerationSide = answers.p6,
                    rows = finalLayout.rows,
                    dpmSeatVirtualNumber = dpmNum,
                    preferentialSeats = if (dpmNum != null) listOf(dpmNum) else emptyList()
                )
            )

            component.goBack()
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            error = e.toUserMessage("Erro ao cadastrar ônibus.")
        } finally {
            loading = false
        }
    }

    fun handleAdvance() {
        error = null
        val isVirtual = p1 == SeatNumberingMode.VIRTUAL
        when (currentStep) {
            0 -> {
                if (plate.isNotBlank() && identificationNumber.isNotBlank()) {
                    if (isValidPlate(plate)) currentStep = 1
                    else error = "Placa inválida. Use o padrão AAA-1234 ou ABC1D23."
                } else {
                    error = "Preencha a placa e o número do veículo."
                }
            }
            1 -> {
                if (p1 == null) return
                currentStep = if (isVirtual) 11 else 2
            }
            11 -> {
                val cap = virtualCapacity.toIntOrNull()
                if (cap == null || cap < 1) {
                    error = "Informe uma capacidade válida."
                } else {
                    currentStep = 5
                }
            }
            2 -> if (p2 != null) currentStep = 3
            3 -> if (p3 != null) currentStep = 4
            4 -> if (p4capacity != null) currentStep = 5
            5 -> {
                if (p5 == null) return
                currentStep = if (isVirtual) 9 else 6
            }
            6 -> if (p6 != null) currentStep = 10
            10 -> {
                if (p6b == null) return
                val answers = buildAnswers()
                val err = BusLayoutEngine.validate(answers)
                if (err != null) {
                    validationError = err
                    currentStep = 7
                } else {
                    val built = BusLayoutEngine.buildLayout(answers)
                    layout = built
                    dpmWarning = computeDpmWarningMessage(answers, built)
                    currentStep = if (p1 == SeatNumberingMode.MIXED) 8 else 9
                }
            }
            8 -> currentStep = 9
            9 -> {
                if (isVirtual) scope.launch { handleSaveVirtual() }
                else scope.launch { handleSave() }
            }
        }
    }

    fun handleBack() {
        error = null
        val isVirtual = p1 == SeatNumberingMode.VIRTUAL
        when (currentStep) {
            0 -> component.goBack()
            1 -> currentStep = 0
            11 -> currentStep = 1
            2 -> currentStep = 1
            5 -> currentStep = if (isVirtual) 11 else 4
            6 -> currentStep = 5
            10 -> currentStep = 6
            7 -> currentStep = 10
            8 -> currentStep = 10
            9 -> currentStep = if (isVirtual) 5 else if (p1 == SeatNumberingMode.MIXED) 8 else 10
            else -> currentStep--
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, start = 8.dp, end = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { handleBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = MaterialTheme.colorScheme.onBackground)
            }
            Spacer(Modifier.weight(1f))
            val stepLabel = when {
                p1 == SeatNumberingMode.VIRTUAL && currentStep == 11 -> "Etapa 2 de 3"
                p1 == SeatNumberingMode.VIRTUAL && currentStep == 5 -> "Etapa 3 de 3"
                currentStep in 1..6 -> "Etapa $currentStep de 7"
                currentStep == 10 -> "Etapa 7 de 7"
                else -> null
            }
            stepLabel?.let {
                Text(it, style = MaterialTheme.typography.labelMedium, color = UbusText3)
            }
        }

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
        ) {
            Text("Novo Veículo", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            Text("Siga as etapas para configurar o ônibus.", style = MaterialTheme.typography.bodyMedium, color = UbusText3)
            Spacer(Modifier.height(24.dp))

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                }
            ) { step ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    when (step) {
                        0 -> StepPlate(
                            plate = plate, onPlateChange = { if (it.length <= 8) plate = it },
                            id = identificationNumber, onIdChange = { identificationNumber = it }
                        )
                        1 -> StepP1(p1, onSelect = { p1 = it })
                        11 -> StepCapacityVirtual(virtualCapacity, onCapacityChange = { virtualCapacity = it })
                        2 -> StepP2(p2, onSelect = { p2 = it })
                        3 -> StepP3(p3, onSelect = { p3 = it })
                        4 -> StepP4(p4capacity, p2, p3, onSelect = { p4capacity = it })
                        5 -> StepP5(p5, onSelect = { p5 = it })
                        6 -> StepP6A(p6, onSelect = { p6 = it })
                        10 -> StepP6B(p6b, onSelect = { p6b = it })
                        7 -> StepValidation(validationError ?: "Erro desconhecido", onRedirect = { stepToRedirect -> currentStep = stepToRedirect })
                        8 -> StepShellPreview(
                            layout = layout!!,
                            p7numbers = p7numbers,
                            isMixed = p1 == SeatNumberingMode.MIXED,
                            onSeatClick = { cell -> activeDialogSeat = cell }
                        )
                        9 -> if (p1 == SeatNumberingMode.VIRTUAL) {
                            StepVirtualConfirm(
                                capacity = virtualCapacity.toIntOrNull() ?: 0,
                                accessibility = p5 ?: AccessibilityFeature.NONE
                            )
                        } else {
                            StepFinalMap(
                                layout = layout!!,
                                p7numbers = p7numbers,
                                dpmWarning = dpmWarning
                            )
                        }
                    }
                }
            }

            error?.let {
                Spacer(Modifier.height(16.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }

        if (currentStep != 7) {
            val isNextEnabled = when (currentStep) {
                0 -> plate.isNotBlank() && identificationNumber.isNotBlank()
                1 -> p1 != null
                11 -> virtualCapacity.toIntOrNull() != null && virtualCapacity.toInt() > 0
                2 -> p2 != null
                3 -> p3 != null
                4 -> p4capacity != null
                5 -> p5 != null
                6 -> p6 != null
                10 -> p6b != null
                else -> true
            }

            UbusButton(
                text = if (currentStep == 9) "Concluir e Salvar" else "Avançar",
                onClick = { handleAdvance() },
                enabled = isNextEnabled,
                loading = loading,
                modifier = Modifier.padding(24.dp)
            )
        }
    }

    activeDialogSeat?.let { cell ->
        PhysicalNumberDialog(
            cell = cell,
            initialValue = p7numbers[cell.virtualNumber]?.toString() ?: "",
            onDismiss = { activeDialogSeat = null },
            onConfirm = { number ->
                if (number != null) {
                    p7numbers[cell.virtualNumber!!] = number
                } else {
                    p7numbers.remove(cell.virtualNumber!!)
                }
                activeDialogSeat = null
            }
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
private fun StepP1(selected: SeatNumberingMode?, onSelect: (SeatNumberingMode) -> Unit) {
    Text("As poltronas têm número físico?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(16.dp))
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SelectOptionCard(
            title = "Sim, todas têm (numeração contínua)",
            subtitle = "Ex: As poltronas físicas seguem a sequência normal de 1 até o final.",
            isSelected = selected == SeatNumberingMode.PHYSICAL,
            onClick = { onSelect(SeatNumberingMode.PHYSICAL) }
        )
        SelectOptionCard(
            title = "Não, o ônibus só informa a lotação total",
            subtitle = "Ex: O ônibus não possui números gravados nos assentos, os alunos escolhem livremente por número virtual.",
            isSelected = selected == SeatNumberingMode.VIRTUAL,
            onClick = { onSelect(SeatNumberingMode.VIRTUAL) }
        )
        SelectOptionCard(
            title = "Mista / Personalizada",
            subtitle = "Ex: Algumas poltronas têm números pulados, fora de ordem, ou o banheiro muda a numeração física.",
            isSelected = selected == SeatNumberingMode.MIXED,
            onClick = { onSelect(SeatNumberingMode.MIXED) }
        )
    }
}

@Composable
private fun StepP2(selected: FrontRowLayout?, onSelect: (FrontRowLayout) -> Unit) {
    Text("Quantos assentos existem na primeira fileira?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(16.dp))
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SelectOptionCard(
            title = "4 assentos",
            subtitle = "Layout completo com 2 assentos na esquerda e 2 na direita.",
            isSelected = selected == FrontRowLayout.FOUR,
            onClick = { onSelect(FrontRowLayout.FOUR) }
        )
        SelectOptionCard(
            title = "3 assentos",
            subtitle = "Geralmente falta o assento do corredor direito por conta da escada da porta.",
            isSelected = selected == FrontRowLayout.THREE,
            onClick = { onSelect(FrontRowLayout.THREE) }
        )
        SelectOptionCard(
            title = "2 assentos",
            subtitle = "Apenas os assentos da janela, deixando o corredor e espaço de escada mais amplos.",
            isSelected = selected == FrontRowLayout.TWO,
            onClick = { onSelect(FrontRowLayout.TWO) }
        )
    }
}

@Composable
private fun StepP3(selected: RearLayout?, onSelect: (RearLayout) -> Unit) {
    Text("Como é a parte de trás do ônibus?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(16.dp))
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SelectOptionCard(
            title = "Tem banheiro no fundo",
            subtitle = "O banheiro ocupa o lado direito do fundo. Sobram 2 assentos no lado esquerdo.",
            isSelected = selected == RearLayout.BATHROOM,
            onClick = { onSelect(RearLayout.BATHROOM) }
        )
        SelectOptionCard(
            title = "Fileira normal com 4 assentos",
            subtitle = "Layout padrão de 2 assentos na esquerda e 2 na direita, com corredor central.",
            isSelected = selected == RearLayout.NORMAL,
            onClick = { onSelect(RearLayout.NORMAL) }
        )
        SelectOptionCard(
            title = "Fileira inteira com 5 assentos",
            subtitle = "Última fileira inteiriça sem corredor, contendo 5 lugares.",
            isSelected = selected == RearLayout.FIVE,
            onClick = { onSelect(RearLayout.FIVE) }
        )
        SelectOptionCard(
            title = "Espaço de cadeirante / Box no fundo",
            subtitle = "O fundo do ônibus é reservado para fixação de cadeira de rodas.",
            isSelected = selected == RearLayout.BOX,
            onClick = { onSelect(RearLayout.BOX) }
        )
    }
}

@Composable
private fun StepP4(selected: Int?, p2: FrontRowLayout?, p3: RearLayout?, onSelect: (Int) -> Unit) {
    Text("Qual a capacidade total de lugares?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(16.dp))

    val capacities = listOf(40, 42, 44, 46, 47, 48, 50)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        capacities.forEach { capacity ->
            val isPossible = checkCapacityPossible(capacity, p2, p3)
            SelectOptionCard(
                title = "$capacity lugares",
                subtitle = if (isPossible) "Disposição física compatível" else "Incompatível com o layout de primeira fileira e fundo selecionados",
                isSelected = selected == capacity,
                enabled = isPossible,
                onClick = { onSelect(capacity) }
            )
        }
    }
}

private fun checkCapacityPossible(capacity: Int, p2: FrontRowLayout?, p3: RearLayout?): Boolean {
    val frontSeats = when (p2) {
        FrontRowLayout.FOUR -> 4
        FrontRowLayout.THREE -> 3
        FrontRowLayout.TWO -> 2
        null -> return true
    }
    val rearSeats = when (p3) {
        RearLayout.BATHROOM -> 2
        RearLayout.NORMAL -> 4
        RearLayout.FIVE -> 5
        RearLayout.BOX -> 0
        null -> return true
    }

    if (p2 == FrontRowLayout.THREE && p3 == RearLayout.BATHROOM) return false
    if (p2 == FrontRowLayout.FOUR && p3 == RearLayout.FIVE) return false

    val remaining = capacity - frontSeats - rearSeats
    return remaining >= 0 && remaining % 4 == 0
}

@Composable
private fun StepP5(selected: AccessibilityFeature?, onSelect: (AccessibilityFeature) -> Unit) {
    Text("Qual o recurso de acessibilidade?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(16.dp))
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SelectOptionCard(
            title = "DPM (Dispositivo de Poltrona Móvel)",
            subtitle = "Poltrona da primeira fileira que se projeta para fora do ônibus.",
            isSelected = selected == AccessibilityFeature.DPM,
            onClick = { onSelect(AccessibilityFeature.DPM) }
        )
        SelectOptionCard(
            title = "Box para Cadeira de Rodas no fundo",
            subtitle = "Espaço reservado no fundo para fixação de cadeira de rodas.",
            isSelected = selected == AccessibilityFeature.BOX,
            onClick = { onSelect(AccessibilityFeature.BOX) }
        )
        SelectOptionCard(
            title = "Nenhum",
            subtitle = "O ônibus não possui recursos especiais de acessibilidade instalados.",
            isSelected = selected == AccessibilityFeature.NONE,
            onClick = { onSelect(AccessibilityFeature.NONE) }
        )
    }
}

@Composable
private fun StepP6A(selected: NumerationSide?, onSelect: (NumerationSide) -> Unit) {
    Text("De qual lado começa a numeração?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(16.dp))
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SelectOptionCard(
            title = "Lado Esquerdo",
            subtitle = "A contagem das poltronas inicia no lado do motorista.",
            isSelected = selected == NumerationSide.LEFT,
            onClick = { onSelect(NumerationSide.LEFT) }
        )
        SelectOptionCard(
            title = "Lado Direito",
            subtitle = "A contagem das poltronas inicia no lado da porta de embarque.",
            isSelected = selected == NumerationSide.RIGHT,
            onClick = { onSelect(NumerationSide.RIGHT) }
        )
    }
}

@Composable
private fun StepP6B(selected: NumberingPattern?, onSelect: (NumberingPattern) -> Unit) {
    Text("Como as poltronas são numeradas?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(16.dp))
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SelectOptionCard(
            title = "Sequencial",
            subtitle = "1, 2, 3, 4... — cada fileira é numerada da frente para o fundo, coluna por coluna.",
            isSelected = selected == NumberingPattern.SEQUENTIAL,
            onClick = { onSelect(NumberingPattern.SEQUENTIAL) }
        )
        SelectOptionCard(
            title = "Ímpares na janela",
            subtitle = "Janelas: 1, 3, 5... / Corredor: 2, 4, 6... — poltronas de janela recebem números ímpares.",
            isSelected = selected == NumberingPattern.ODD_WINDOW,
            onClick = { onSelect(NumberingPattern.ODD_WINDOW) }
        )
        SelectOptionCard(
            title = "Pares na janela",
            subtitle = "Janelas: 2, 4, 6... / Corredor: 1, 3, 5... — poltronas de janela recebem números pares.",
            isSelected = selected == NumberingPattern.EVEN_WINDOW,
            onClick = { onSelect(NumberingPattern.EVEN_WINDOW) }
        )
    }
}

@Composable
private fun StepCapacityVirtual(capacity: String, onCapacityChange: (String) -> Unit) {
    Text("Quantas poltronas tem o ônibus?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Text(
        "Como as poltronas não têm numeração física, informe apenas o total de assentos disponíveis.",
        style = MaterialTheme.typography.bodySmall,
        color = UbusText3
    )
    Spacer(Modifier.height(24.dp))
    UbusTextField(
        value = capacity,
        onValueChange = { onCapacityChange(it.filter { c -> c.isDigit() }.take(3)) },
        label = "Total de assentos",
        placeholder = "Ex: 44",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

@Composable
private fun StepVirtualConfirm(capacity: Int, accessibility: AccessibilityFeature) {
    Text("Resumo do Ônibus", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Text(
        "Este ônibus não possui numeração física nas poltronas. Os alunos escolherão livremente ao reservar.",
        style = MaterialTheme.typography.bodySmall,
        color = UbusText3
    )
    Spacer(Modifier.height(24.dp))
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        BentoCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(4.dp)) {
                Text("Capacidade", style = MaterialTheme.typography.labelSmall, color = UbusText3)
                Text("$capacity assentos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
        BentoCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(4.dp)) {
                Text("Acessibilidade", style = MaterialTheme.typography.labelSmall, color = UbusText3)
                Text(
                    when (accessibility) {
                        AccessibilityFeature.DPM -> "DPM — Dispositivo de Poltrona Móvel"
                        AccessibilityFeature.BOX -> "Box para Cadeira de Rodas"
                        AccessibilityFeature.NONE -> "Nenhum recurso especial"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StepValidation(error: String, onRedirect: (Int) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Erro de validação",
            tint = Color(0xFFEF4444),
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Inconsistência Detectada",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            error,
            style = MaterialTheme.typography.bodyMedium,
            color = UbusText3,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(32.dp))

        if (error.contains("box") || error.contains("acessibilidade")) {
            UbusButton(text = "Ajustar Fundo (P3)", onClick = { onRedirect(3) }, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))
            UbusButton(text = "Ajustar Acessibilidade (P5)", onClick = { onRedirect(5) }, modifier = Modifier.fillMaxWidth())
        } else {
            UbusButton(text = "Ajustar Primeira Fileira (P2)", onClick = { onRedirect(2) }, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))
            UbusButton(text = "Ajustar Fundo (P3)", onClick = { onRedirect(3) }, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))
            UbusButton(text = "Ajustar Capacidade (P4)", onClick = { onRedirect(4) }, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun StepShellPreview(
    layout: BusLayout,
    p7numbers: Map<Int, Int>,
    isMixed: Boolean,
    onSeatClick: (BusCell) -> Unit
) {
    Text(
        if (isMixed) "Configurar Numeração Física" else "Pré-visualização do Layout",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
    Text(
        if (isMixed) "Toque nas poltronas para digitar o número físico gravado nelas." else "Verifique a disposição do corredor e assentos gerada.",
        style = MaterialTheme.typography.bodySmall,
        color = UbusText3
    )
    Spacer(Modifier.height(24.dp))

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp)).padding(16.dp)
        ) {
            layout.rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.cells.forEach { cell ->
                        val displayCell = if (cell.type == CellType.SEAT) {
                            cell.copy(physicalNumber = p7numbers[cell.virtualNumber])
                        } else cell

                        BusCellView(
                            cell = displayCell,
                            state = if (cell.type == CellType.SEAT) BusCellState.SHELL else BusCellState.DISABLED,
                            displayMode = if (isMixed) SeatNumberingMode.MIXED else SeatNumberingMode.PHYSICAL,
                            onClick = if (isMixed && cell.type == CellType.SEAT) {
                                { onSeatClick(cell) }
                            } else null
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepFinalMap(
    layout: BusLayout,
    p7numbers: Map<Int, Int>,
    dpmWarning: String?
) {
    Text("Mapa Final do Veículo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Text("Este é o mapa que será exibido aos alunos para a reserva de passagens.", style = MaterialTheme.typography.bodySmall, color = UbusText3)
    Spacer(Modifier.height(16.dp))

    dpmWarning?.let {
        BentoCard(modifier = Modifier.padding(bottom = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, null, tint = UbusPrimary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp)).padding(16.dp)
        ) {
            layout.rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.cells.forEach { cell ->
                        val displayCell = if (cell.type == CellType.SEAT) {
                            cell.copy(
                                physicalNumber = p7numbers[cell.virtualNumber]
                            )
                        } else cell

                        BusCellView(
                            cell = displayCell,
                            state = if (cell.type == CellType.SEAT) {
                                if (cell.isDpm) BusCellState.DPM else BusCellState.FREE
                            } else BusCellState.DISABLED,
                            displayMode = layout.numberingMode,
                            onClick = null
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectOptionCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    BentoCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() }
            .border(
                width = 2.dp,
                color = if (isSelected) UbusPrimary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .alpha(if (enabled) 1.0f else 0.4f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) UbusPrimary else MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = UbusText3)
        }
    }
}

@Composable
private fun PhysicalNumberDialog(
    cell: BusCell,
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (Int?) -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Número Físico - Assento Virtual #${cell.virtualNumber}") },
        text = {
            Column {
                Text(
                    "Insira o número físico visível impresso neste assento no ônibus real.",
                    style = MaterialTheme.typography.bodySmall,
                    color = UbusText3
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.filter { char -> char.isDigit() }.take(3) },
                    label = { Text("Número da Poltrona") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(text.toIntOrNull()) }
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            Button(
                onClick = { onConfirm(null) }
            ) {
                Text("Limpar / Usar Padrão")
            }
        }
    )
}
