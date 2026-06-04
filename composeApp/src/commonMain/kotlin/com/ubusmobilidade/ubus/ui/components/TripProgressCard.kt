package com.ubusmobilidade.ubus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ubusmobilidade.ubus.data.model.Reservation
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary

@Composable
fun TripProgressCard(
    reservation: Reservation,
    modifier: Modifier = Modifier
) {
    val trip = reservation.trip ?: return

    val gradient = Brush.linearGradient(
        colors = listOf(UbusPrimary, Color(0xFF6366F1))
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(gradient)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Viagem de Hoje",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            CountdownTimer(
                tripDate = trip.tripDate,
                shift = trip.shift,
                direction = trip.direction.name,
                textColor = Color.White
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = trip.route?.name ?: "Rota Escolar",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${if (trip.direction == com.ubusmobilidade.ubus.data.model.TripDirection.OUTBOUND) "Ida" else "Volta"} · ${if (trip.shift == "MORNING") "Manhã" else if (trip.shift == "AFTERNOON") "Tarde" else "Noite"}",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp
            )

            if (reservation.seatNumber != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Assento #${reservation.seatNumber}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
