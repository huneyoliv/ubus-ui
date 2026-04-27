package com.ubusmobilidade.ubus.ui.screens.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ubusmobilidade.ubus.data.api.BackendCapabilities
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.UbusButton
import com.ubusmobilidade.ubus.ui.components.UbusOutlinedButton
import com.ubusmobilidade.ubus.ui.theme.UbusBackground
import com.ubusmobilidade.ubus.ui.theme.UbusText3

@Composable
fun MotoristaSplashScreen(component: RootComponent) {
    Column(
        modifier = Modifier.fillMaxSize().background(UbusBackground).padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Bem-vindo, motorista!", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(8.dp))
        Text("Selecione seu veículo para iniciar a viagem.", style = MaterialTheme.typography.bodyMedium, color = UbusText3)
        if (!BackendCapabilities.supportsDriverOperationalAssignment) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Modo operacional completo (ida/volta, saida e notificacoes) sera habilitado apos atualizacao da API.",
                style = MaterialTheme.typography.bodySmall,
                color = UbusText3,
            )
        }
        Spacer(Modifier.height(32.dp))
        UbusButton(text = "Selecionar veículo", onClick = { component.navigateTo(RootComponent.Config.SelecionarVeiculo) })
        Spacer(Modifier.height(12.dp))
        UbusOutlinedButton(text = "Cadastrar novo veículo", onClick = { component.navigateTo(RootComponent.Config.CadastroVeiculoMultiStep) })
        Spacer(Modifier.height(24.dp))
        UbusOutlinedButton(text = "Sair", onClick = { component.logout() })
    }
}
