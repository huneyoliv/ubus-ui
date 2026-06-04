package com.ubusmobilidade.ubus.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun RatingStars(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..5) {
            val isSelected = i <= rating
            val icon = if (isSelected) Icons.Default.Star else Icons.Default.StarBorder
            val tint = if (isSelected) Color(0xFFFBBF24) else Color(0xFF94A3B8)

            Icon(
                imageVector = icon,
                contentDescription = "Avaliação $i estrelas",
                tint = tint,
                modifier = Modifier
                    .size(40.dp)
                    .clickable(enabled = enabled) { onRatingChange(i) }
                    .padding(4.dp)
            )
        }
    }
}
