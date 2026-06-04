package com.ubusmobilidade.ubus.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary
import com.ubusmobilidade.ubus.ui.util.getTripDepartureMillis
import kotlinx.coroutines.delay

@Composable
fun CountdownTimer(
    tripDate: String,
    shift: String,
    direction: String = "OUTBOUND",
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onBackground
) {
    val departureMillis = remember(tripDate, shift, direction) { getTripDepartureMillis(tripDate, shift, direction) }
    var timeRemaining by remember { mutableStateOf(departureMillis - System.currentTimeMillis()) }

    LaunchedEffect(departureMillis) {
        while (timeRemaining > 0) {
            timeRemaining = departureMillis - System.currentTimeMillis()
            delay(1000L)
        }
    }

    val displayString = when {
        timeRemaining <= 0 -> "Viagem iniciada"
        else -> {
            val totalSeconds = timeRemaining / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60

            when {
                hours > 0 -> "Embarque em ${hours}h ${minutes}min"
                else -> "Embarque em ${minutes}min ${seconds}seg"
            }
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = null,
            tint = if (timeRemaining <= 0) Color(0xFFEF4444) else UbusPrimary,
            modifier = Modifier.width(14.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = displayString,
            color = if (timeRemaining <= 0) Color(0xFFEF4444) else textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
