package com.ubusmobilidade.ubus.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.ubusmobilidade.ubus.ui.theme.UbusAccent
import com.ubusmobilidade.ubus.ui.theme.UbusBackground
import com.ubusmobilidade.ubus.ui.theme.UbusMutedForeground
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary

enum class StudentTab(val label: String, val icon: ImageVector) {
    HOME("Início", Icons.Default.Home),
    RESERVAR("Reservar", Icons.Default.DirectionsBus),
    BILHETE("Bilhete", Icons.Default.ConfirmationNumber),
    HISTORICO("Histórico", Icons.Default.History),
    PERFIL("Perfil", Icons.Default.Person),
}

@Composable
fun StudentBottomNavBar(
    selectedTab: StudentTab,
    onTabSelected: (StudentTab) -> Unit,
) {
    NavigationBar(
        containerColor = UbusPrimary,
    ) {
        StudentTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = UbusAccent,
                    selectedTextColor = UbusAccent,
                    unselectedIconColor = UbusMutedForeground,
                    unselectedTextColor = UbusMutedForeground,
                    indicatorColor = UbusBackground,
                ),
            )
        }
    }
}

enum class DriverTab(val label: String, val icon: ImageVector) {
    MAPA("Mapa", Icons.Default.DirectionsBus),
    AVISOS("Avisos", Icons.Default.ConfirmationNumber),
    CONFIG("Config", Icons.Default.Person),
}

@Composable
fun DriverBottomNavBar(
    selectedTab: DriverTab,
    onTabSelected: (DriverTab) -> Unit,
) {
    NavigationBar(containerColor = UbusPrimary) {
        DriverTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = UbusAccent,
                    selectedTextColor = UbusAccent,
                    unselectedIconColor = UbusMutedForeground,
                    unselectedTextColor = UbusMutedForeground,
                    indicatorColor = UbusBackground,
                ),
            )
        }
    }
}
