package com.ubusmobilidade.ubus.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.ubusmobilidade.ubus.data.model.VerificationChannel

@Composable
fun VerificationChannelSelector(
    selected: VerificationChannel,
    onSelect: (VerificationChannel) -> Unit,
    phone: String?,
    modifier: Modifier = Modifier,
    showOnlyEmail: Boolean = false,
) {
    val isWhatsappEnabled = !phone.isNullOrBlank()

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Receber código por:",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (showOnlyEmail) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "E-mail",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Enviar código para o e-mail cadastrado",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelect(VerificationChannel.EMAIL) },
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(
                        width = if (selected == VerificationChannel.EMAIL) 2.dp else 1.dp,
                        color = if (selected == VerificationChannel.EMAIL) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        }
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected == VerificationChannel.EMAIL) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = if (selected == VerificationChannel.EMAIL) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "E-mail",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .alpha(if (isWhatsappEnabled) 1f else 0.4f)
                        .clickable(enabled = isWhatsappEnabled) {
                            onSelect(VerificationChannel.WHATSAPP)
                        },
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(
                        width = if (selected == VerificationChannel.WHATSAPP && isWhatsappEnabled) 2.dp else 1.dp,
                        color = if (selected == VerificationChannel.WHATSAPP && isWhatsappEnabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        }
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected == VerificationChannel.WHATSAPP && isWhatsappEnabled) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = if (selected == VerificationChannel.WHATSAPP && isWhatsappEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "WhatsApp",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            if (!isWhatsappEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "*WhatsApp indisponível (cadastre um telefone no perfil)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
