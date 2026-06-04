package com.ubusmobilidade.ubus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.ubusmobilidade.ubus.data.model.AttendanceBadge
import com.ubusmobilidade.ubus.data.model.AttendanceScore
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary
import com.ubusmobilidade.ubus.ui.theme.UbusText3

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AttendanceBadgeCard(
    score: AttendanceScore,
    modifier: Modifier = Modifier
) {
    BentoCard(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "UbusClub",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = "${score.score.toInt()}/100 pts",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = UbusPrimary
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Seu índice de assiduidade e pontualidade no transporte escolar.",
                fontSize = 12.sp,
                color = UbusText3,
                lineHeight = 16.sp
            )

            Spacer(Modifier.height(12.dp))

            val progressValue = (score.score / 100.0).toFloat().coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = progressValue,
                color = UbusPrimary,
                trackColor = Color(0xFFE2E8F0),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            if (score.badges.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Suas Conquistas",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    score.badges.forEach { badge ->
                        val badgeLabel = when (badge) {
                            AttendanceBadge.PUNCTUAL -> "Pontual"
                            AttendanceBadge.FREQUENT_RIDER -> "Frequente"
                            AttendanceBadge.ECO_FRIENDLY -> "Sustentável"
                            AttendanceBadge.PERFECT_WEEK -> "Semana Perfeita"
                            AttendanceBadge.PERFECT_MONTH -> "Mês Perfeito"
                        }

                        val badgeColor = when (badge) {
                            AttendanceBadge.PUNCTUAL -> Color(0xFF10B981)
                            AttendanceBadge.FREQUENT_RIDER -> Color(0xFF3B82F6)
                            AttendanceBadge.ECO_FRIENDLY -> Color(0xFF8B5CF6)
                            AttendanceBadge.PERFECT_WEEK -> Color(0xFFF59E0B)
                            AttendanceBadge.PERFECT_MONTH -> Color(0xFFEF4444)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(badgeColor.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MilitaryTech,
                                contentDescription = null,
                                tint = badgeColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = badgeLabel,
                                color = badgeColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
