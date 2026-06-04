package com.ubusmobilidade.ubus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ubusmobilidade.ubus.data.model.PickupPoint
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary

@Composable
fun PickupPointTimeline(
    points: List<PickupPoint>,
    selectedPointId: String?,
    onPointSelect: (PickupPoint) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        points.forEachIndexed { index, point ->
            val isSelected = point.id == selectedPointId
            val isLast = index == points.size - 1

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onPointSelect(point) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) UbusPrimary else Color(0xFFCBD5E1))
                            .then(
                                if (isSelected) {
                                    Modifier.border(4.dp, Color(0xFFDBEAFE), CircleShape)
                                } else Modifier
                            )
                    )

                    if (!isLast) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .weight(1f)
                                .fillMaxHeight()
                                .background(Color(0xFFE2E8F0))
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color(0xFFEFF6FF) else Color.Transparent)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) UbusPrimary.copy(alpha = 0.5f) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = if (isSelected) UbusPrimary else Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = point.name,
                            color = if (isSelected) UbusPrimary else MaterialTheme.colorScheme.onBackground,
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            if (!isLast) {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
