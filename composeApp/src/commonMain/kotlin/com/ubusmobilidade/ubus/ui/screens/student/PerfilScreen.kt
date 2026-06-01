package com.ubusmobilidade.ubus.ui.screens.student

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ubusmobilidade.ubus.data.model.RoleUsuario
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.AppScaffold
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.components.StudentBottomNavBar
import com.ubusmobilidade.ubus.ui.components.StudentTab
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary
import com.ubusmobilidade.ubus.ui.theme.UbusDestructive
import com.ubusmobilidade.ubus.ui.theme.UbusText3

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.ubusmobilidade.ubus.data.api.ApiClient
import com.ubusmobilidade.ubus.data.api.BackendCapabilities
import com.ubusmobilidade.ubus.data.api.AttendanceRepository
import com.ubusmobilidade.ubus.data.model.AttendanceScore
import com.ubusmobilidade.ubus.ui.components.AttendanceBadgeCard

@Composable
fun PerfilScreen(component: RootComponent) {
    val user = component.authStorage.user
    val initials = user?.name
        ?.split(" ")
        ?.mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
        ?.take(2)
        ?.joinToString("") ?: "?"

    val apiClient = remember { ApiClient(component.authStorage, onUnauthorized = { component.logout() }) }
    val attendanceRepo = remember { AttendanceRepository(apiClient) }
    var attendanceScore by remember { mutableStateOf<AttendanceScore?>(null) }

    LaunchedEffect(Unit) {
        try {
            if (BackendCapabilities.supportsAttendanceScore) {
                attendanceScore = attendanceRepo.getScore()
            }
        } catch (_: Exception) {}
    }

    AppScaffold(
        bottomBar = {
            StudentBottomNavBar(
                selectedTab = StudentTab.PERFIL,
                showLeaderTab = component.authStorage.user?.role == RoleUsuario.LEADER,
                onTabSelected = { tab ->
                    when (tab) {
                        StudentTab.HOME -> component.replaceWith(RootComponent.Config.StudentHome)
                        StudentTab.RESERVAR -> component.replaceWith(RootComponent.Config.Reservar)
                        StudentTab.LIDER -> component.replaceWith(RootComponent.Config.Lider)
                        StudentTab.HISTORICO -> component.replaceWith(RootComponent.Config.Historico)
                        StudentTab.PERFIL -> {}
                    }
                },
            )
        },
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        ) {
            // Avatar and name
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, bottom = 24.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(UbusPrimary, Color(0xFF6366F1)))
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                }
                Spacer(Modifier.height(12.dp))
                Text(user?.name ?: "", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text(user?.email ?: "", style = MaterialTheme.typography.bodyMedium, color = UbusText3)
                
                user?.status?.let { status ->
                    Spacer(Modifier.height(10.dp))
                    com.ubusmobilidade.ubus.ui.components.StatusChip(status = status)
                }
            }

            if (BackendCapabilities.supportsAttendanceScore && attendanceScore != null) {
                AttendanceBadgeCard(
                    score = attendanceScore!!,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Menu items
            ProfileMenuItem(Icons.Default.Edit, "Meus dados") {
                component.navigateTo(RootComponent.Config.MeusDados)
            }
            ProfileMenuItem(Icons.Default.Lock, "Alterar senha") {
                component.navigateTo(RootComponent.Config.AlterarSenha)
            }
            ProfileMenuItem(Icons.Default.CalendarMonth, "Renovar semestre") {
                component.navigateTo(RootComponent.Config.RenovarSemestre)
            }
            ProfileMenuItem(Icons.Default.Person, "Carteirinha") {
                component.navigateTo(RootComponent.Config.Carteirinha)
            }
            if (component.authStorage.user?.role == RoleUsuario.LEADER) {
                ProfileMenuItem(Icons.Default.Group, "Líder") {
                    component.navigateTo(RootComponent.Config.Lider)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Logout
            BentoCard(
                modifier = Modifier.clickable { component.logout() },
                borderColor = UbusDestructive.copy(alpha = 0.3f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = UbusDestructive, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Sair da conta", color = UbusDestructive, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ProfileMenuItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    BentoCard(modifier = Modifier.padding(bottom = 8.dp).clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = UbusPrimary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = UbusText3, modifier = Modifier.size(16.dp))
        }
    }
}
