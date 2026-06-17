package com.ubusmobilidade.ubus.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.components.UbusButton
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary
import com.ubusmobilidade.ubus.ui.theme.UbusSuccess
import com.ubusmobilidade.ubus.ui.theme.UbusText3

@Composable
fun ReservaConcluidaScreen(component: RootComponent, isRideShare: Boolean) {
    val backgroundColor = if (isRideShare) {
        Color(0xFFFFEDD5) // Laranja claro
    } else {
        Color(0xFFE0F2FE) // Azul claro céu
    }

    val iconColor = if (isRideShare) {
        Color(0xFFEA580C) // Laranja escuro
    } else {
        Color(0xFF0284C7) // Azul escuro
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.height(48.dp))

        Icon(
            imageVector = if (isRideShare) Icons.Default.Info else Icons.Default.CheckCircle,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(80.dp)
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = if (isRideShare) "Reserva de Carona" else "Reserva Confirmada!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = if (isRideShare) {
                "Sua solicitação de carona foi registrada com sucesso."
            } else {
                "Sua vaga está garantida nesta viagem. Tenha uma excelente viagem!"
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(32.dp))

        if (isRideShare) {
            BentoCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = iconColor.copy(alpha = 0.3f)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Atenção",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = iconColor
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Nessa rota a prioridade são os alunos titulares da rota. Como você não é titular dela, sua reserva está sujeita à disponibilidade de vagas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        } else {
            BentoCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = iconColor.copy(alpha = 0.3f)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Aluno Titular",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = iconColor
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Você é titular nesta rota, sua prioridade está garantida nesta viagem.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(48.dp))

        UbusButton(
            text = "Ir para o início",
            onClick = { component.replaceWith(RootComponent.Config.StudentHome) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(48.dp))
    }
}
