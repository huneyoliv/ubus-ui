package com.ubusmobilidade.ubus.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.theme.UbusText3

@Composable
fun BaixaMobilidadeScreen(component: RootComponent) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 20.dp)) {
        IconButton(onClick = { component.goBack() }, modifier = Modifier.padding(top = 8.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = MaterialTheme.colorScheme.onBackground)
        }
        Text("Acessibilidade", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(top = 8.dp, bottom = 24.dp))
        BentoCard {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Accessibility, null, tint = UbusText3, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(12.dp))
                Text("Baixa mobilidade", style = MaterialTheme.typography.titleMedium, color = UbusText3, textAlign = TextAlign.Center)
                Text("Solicite prioridade de embarque se você precisa de acessibilidade.", style = MaterialTheme.typography.bodySmall, color = UbusText3, textAlign = TextAlign.Center)
            }
        }
    }
}
