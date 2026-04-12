package com.ubusmobilidade.ubus.ui.screens.manager

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary
import com.ubusmobilidade.ubus.ui.theme.UbusDestructive
import com.ubusmobilidade.ubus.ui.theme.UbusText3

@Composable
fun ManagerConfiguracoesScreen(component: RootComponent) {
    val user = component.authStorage.user

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
    ) {
        IconButton(onClick = { component.goBack() }, modifier = Modifier.padding(top = 8.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = MaterialTheme.colorScheme.onBackground)
        }
        Text(
            "Configurações",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
        )
        Text(
            "Ajustes gerais do sistema",
            style = MaterialTheme.typography.bodyMedium,
            color = UbusText3,
            modifier = Modifier.padding(bottom = 20.dp),
        )

        // User info card
        BentoCard(modifier = Modifier.padding(bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, tint = UbusPrimary, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(user?.name ?: "Gestor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Text(user?.email ?: "", style = MaterialTheme.typography.bodySmall, color = UbusText3)
                    Text("Cargo: ${user?.role?.name ?: "—"}", style = MaterialTheme.typography.bodySmall, color = UbusText3)
                }
            }
        }

        // Municipality info
        BentoCard(modifier = Modifier.padding(bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Business, null, tint = UbusPrimary, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Município", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
                    Text("ID: ${user?.municipalityId ?: "—"}", style = MaterialTheme.typography.bodySmall, color = UbusText3)
                }
            }
        }

        // Meus dados
        BentoCard(modifier = Modifier.padding(bottom = 12.dp).clickable { component.navigateTo(RootComponent.Config.MeusDados) }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, tint = UbusPrimary, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Text("Meus dados", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
            }
        }

        // Alterar senha
        BentoCard(modifier = Modifier.padding(bottom = 12.dp).clickable { component.navigateTo(RootComponent.Config.AlterarSenha) }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, null, tint = UbusPrimary, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Text("Alterar senha", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
            }
        }

        // App info
        BentoCard(modifier = Modifier.padding(bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, null, tint = UbusText3, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Sobre o app", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
                    Text("Ubus v1.0.0 · Mobilidade universitária", style = MaterialTheme.typography.bodySmall, color = UbusText3)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Logout
        BentoCard(modifier = Modifier.clickable { component.logout() }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = UbusDestructive, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text("Sair da conta", style = MaterialTheme.typography.titleSmall, color = UbusDestructive)
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}
