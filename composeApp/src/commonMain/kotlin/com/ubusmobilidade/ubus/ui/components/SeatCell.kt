package com.ubusmobilidade.ubus.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary

enum class SeatState {
    FREE, OCCUPIED, SELECTED, ACCESSIBLE
}

@Composable
fun SeatCell(
    number: Int,
    state: SeatState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isClickable = state == SeatState.FREE || state == SeatState.ACCESSIBLE || state == SeatState.SELECTED

    val backgroundColor by animateColorAsState(
        targetValue = when (state) {
            SeatState.FREE -> Color(0xFFF1F5F9)
            SeatState.OCCUPIED -> Color(0xFFE2E8F0)
            SeatState.SELECTED -> UbusPrimary
            SeatState.ACCESSIBLE -> Color(0xFFDCFCE7)
        }
    )

    val contentColor by animateColorAsState(
        targetValue = when (state) {
            SeatState.FREE -> Color(0xFF475569)
            SeatState.OCCUPIED -> Color(0xFF94A3B8)
            SeatState.SELECTED -> Color.White
            SeatState.ACCESSIBLE -> Color(0xFF15803D)
        }
    )

    val borderColor = when (state) {
        SeatState.SELECTED -> UbusPrimary
        SeatState.ACCESSIBLE -> Color(0xFF22C55E)
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .size(46.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .then(
                if (borderColor != Color.Transparent) {
                    Modifier.border(2.dp, borderColor, RoundedCornerShape(8.dp))
                } else Modifier
            )
            .clickable(enabled = isClickable) { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            color = contentColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
