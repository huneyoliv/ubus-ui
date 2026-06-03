package com.ubusmobilidade.ubus.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ubusmobilidade.ubus.data.model.RoleUsuario
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.AppScaffold
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.components.StudentBottomNavBar
import com.ubusmobilidade.ubus.ui.components.StudentTab
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary
import com.ubusmobilidade.ubus.ui.theme.UbusText3

@Composable
fun SelecionarDirecaoScreen(
    component: RootComponent,
    routeId: String,
    tripDate: String,
    shift: String,
    outboundTripId: String?,
    inboundTripId: String?
) {
    AppScaffold(
        bottomBar = {
            StudentBottomNavBar(
                selectedTab = StudentTab.RESERVAR,
                showLeaderTab = component.authStorage.user?.role == RoleUsuario.LEADER,
                onTabSelected = { tab ->
                    when (tab) {
                        StudentTab.HOME -> component.replaceWith(RootComponent.Config.StudentHome)
                        StudentTab.RESERVAR -> component.replaceWith(RootComponent.Config.Reservar)
                        StudentTab.LIDER -> component.replaceWith(RootComponent.Config.Lider)
                        StudentTab.HISTORICO -> component.replaceWith(RootComponent.Config.Historico)
                        StudentTab.PERFIL -> component.replaceWith(RootComponent.Config.Perfil)
                    }
                },
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { component.goBack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Sentido da Viagem",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Text(
                text = "Selecione como deseja viajar no dia $tripDate (${if (shift == "MORNING") "Manhã" else if (shift == "AFTERNOON") "Tarde" else "Noite"}):",
                style = MaterialTheme.typography.bodyMedium,
                color = UbusText3,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (outboundTripId != null) {
                BentoCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clickable {
                            component.navigateTo(
                                RootComponent.Config.SelecionarAssento(
                                    tripId = outboundTripId,
                                    pendingInboundTripId = null
                                )
                            )
                        },
                    borderColor = UbusPrimary.copy(alpha = 0.15f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(UbusPrimary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ArrowUpward, null, tint = UbusPrimary, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Só Ida",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Reservar assento apenas para a ida",
                                style = MaterialTheme.typography.bodySmall,
                                color = UbusText3
                            )
                        }
                    }
                }
            }

            if (inboundTripId != null) {
                BentoCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clickable {
                            component.navigateTo(
                                RootComponent.Config.SelecionarAssento(
                                    tripId = inboundTripId,
                                    pendingInboundTripId = null
                                )
                            )
                        },
                    borderColor = UbusPrimary.copy(alpha = 0.15f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(UbusPrimary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ArrowDownward, null, tint = UbusPrimary, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Só Volta",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Reservar assento apenas para a volta",
                                style = MaterialTheme.typography.bodySmall,
                                color = UbusText3
                            )
                        }
                    }
                }
            }

            if (outboundTripId != null && inboundTripId != null) {
                BentoCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clickable {
                            component.navigateTo(
                                RootComponent.Config.SelecionarAssento(
                                    tripId = outboundTripId,
                                    pendingInboundTripId = inboundTripId
                                )
                            )
                        },
                    borderColor = UbusPrimary.copy(alpha = 0.15f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(UbusPrimary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SwapVert, null, tint = UbusPrimary, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Ida e Volta",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Reservar assento para a ida e para a volta",
                                style = MaterialTheme.typography.bodySmall,
                                color = UbusText3
                            )
                        }
                    }
                }
            }
        }
    }
}
