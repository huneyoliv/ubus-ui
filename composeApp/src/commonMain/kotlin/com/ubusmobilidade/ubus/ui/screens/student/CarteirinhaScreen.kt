package com.ubusmobilidade.ubus.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary
import com.ubusmobilidade.ubus.ui.theme.UbusPrimaryContainer
import com.ubusmobilidade.ubus.ui.theme.UbusText3

@Composable
fun CarteirinhaScreen(component: RootComponent) {
    val user = component.authStorage.user

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 20.dp),
    ) {
        IconButton(onClick = { component.goBack() }, modifier = Modifier.padding(top = 8.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = MaterialTheme.colorScheme.onBackground)
        }

        Text(
            "Carteirinha",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
        )

        BentoCard(cornerRadius = 24.dp) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(UbusPrimary, MaterialTheme.colorScheme.tertiary))),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Badge, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(36.dp))
                }

                Spacer(Modifier.height(16.dp))

                Text(user?.name ?: "", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground, textAlign = TextAlign.Center)
                Text(user?.email ?: "", style = MaterialTheme.typography.bodyMedium, color = UbusText3)
                Text("CPF: ${user?.cpf ?: ""}", style = MaterialTheme.typography.bodySmall, color = UbusText3)

                Spacer(Modifier.height(20.dp))

                // QR placeholder
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(UbusPrimaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.QrCode2, null, tint = UbusPrimary, modifier = Modifier.size(100.dp))
                }

                Spacer(Modifier.height(12.dp))
                Text("ESTUDANTE", color = UbusPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 2.sp)
            }
        }
    }
}
