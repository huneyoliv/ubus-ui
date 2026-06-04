package com.ubusmobilidade.ubus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ubusmobilidade.ubus.data.model.RegistrationStatus
import com.ubusmobilidade.ubus.data.model.ReservationStatus

@Composable
fun StatusChip(
    status: ReservationStatus,
    modifier: Modifier = Modifier
) {
    val (label, bgColor, textColor) = when (status) {
        ReservationStatus.CONFIRMED -> Triple("Confirmado", Color(0xFFDBEAFE), Color(0xFF1E40AF))
        ReservationStatus.PRESENT -> Triple("Presente", Color(0xFFD1FAE5), Color(0xFF065F46))
        ReservationStatus.ABSENT -> Triple("Ausente", Color(0xFFFEE2E2), Color(0xFF991B1B))
        ReservationStatus.CANCELLED_BY_SYSTEM -> Triple("Cancelado", Color(0xFFF1F5F9), Color(0xFF475569))
        ReservationStatus.EXCESS -> Triple("Excedente", Color(0xFFFEF3C7), Color(0xFF92400E))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StatusChip(
    status: RegistrationStatus,
    modifier: Modifier = Modifier
) {
    val (label, bgColor, textColor) = when (status) {
        RegistrationStatus.PENDING -> Triple("Pendente", Color(0xFFFEF3C7), Color(0xFF92400E))
        RegistrationStatus.APPROVED -> Triple("Aprovado", Color(0xFFD1FAE5), Color(0xFF065F46))
        RegistrationStatus.REJECTED -> Triple("Rejeitado", Color(0xFFFEE2E2), Color(0xFF991B1B))
        RegistrationStatus.RENEWAL_PENDING -> Triple("Renovação", Color(0xFFE0F2FE), Color(0xFF0369A1))
        RegistrationStatus.SUSPENDED -> Triple("Suspenso", Color(0xFFFEE2E2), Color(0xFF7F1D1D))
        RegistrationStatus.INACTIVE -> Triple("Inativo", Color(0xFFF1F5F9), Color(0xFF475569))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
