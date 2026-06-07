package com.ubusmobilidade.ubus.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessible
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ubusmobilidade.ubus.data.model.BusCell
import com.ubusmobilidade.ubus.data.model.CellType
import com.ubusmobilidade.ubus.data.model.SeatNumberingMode
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary

enum class BusCellState {
    FREE,
    OCCUPIED,
    SELECTED,
    DPM,
    DPM_OCCUPIED,
    SHELL,
    DISABLED
}

@Composable
fun BusCellView(
    cell: BusCell,
    state: BusCellState,
    displayMode: SeatNumberingMode,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (cell.type == CellType.AISLE) {
        Box(modifier = modifier.width(12.dp).height(46.dp))
        return
    }

    if (cell.type == CellType.EMPTY) {
        Box(modifier = modifier.size(46.dp))
        return
    }

    val isClickable = onClick != null && state != BusCellState.DISABLED

    val backgroundColor by animateColorAsState(
        targetValue = when (cell.type) {
            CellType.BATHROOM -> Color(0xFFE2E8F0)
            CellType.BOX -> Color(0xFFF3E8FF)
            CellType.SEAT -> when (state) {
                BusCellState.FREE -> Color(0xFFF1F5F9)
                BusCellState.OCCUPIED -> Color(0xFFE2E8F0)
                BusCellState.SELECTED -> UbusPrimary
                BusCellState.DPM -> Color(0xFFDCFCE7)
                BusCellState.DPM_OCCUPIED -> Color(0xFFE6F4EA)
                BusCellState.SHELL -> UbusPrimary.copy(alpha = 0.15f)
                BusCellState.DISABLED -> Color(0xFFE2E8F0)
            }
            else -> Color.Transparent
        }
    )

    val contentColor by animateColorAsState(
        targetValue = when (cell.type) {
            CellType.BATHROOM -> Color(0xFF475569)
            CellType.BOX -> Color(0xFF6B21A8)
            CellType.SEAT -> when (state) {
                BusCellState.FREE -> Color(0xFF475569)
                BusCellState.OCCUPIED -> Color(0xFF94A3B8)
                BusCellState.SELECTED -> Color.White
                BusCellState.DPM -> Color(0xFF15803D)
                BusCellState.DPM_OCCUPIED -> Color(0xFF94A3B8)
                BusCellState.SHELL -> UbusPrimary
                BusCellState.DISABLED -> Color(0xFF94A3B8)
            }
            else -> Color.Transparent
        }
    )

    val borderColor = when (cell.type) {
        CellType.SEAT -> when (state) {
            BusCellState.SELECTED -> UbusPrimary
            BusCellState.DPM -> Color(0xFF22C55E)
            BusCellState.SHELL -> UbusPrimary
            else -> Color.Transparent
        }
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
            .then(
                if (isClickable && onClick != null) {
                    Modifier.clickable { onClick() }
                } else Modifier
            )
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        when (cell.type) {
            CellType.BATHROOM -> {
                Icon(
                    imageVector = Icons.Default.Wc,
                    contentDescription = "Banheiro",
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            CellType.BOX -> {
                Icon(
                    imageVector = Icons.Default.Accessible,
                    contentDescription = "Box Acessível",
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            CellType.SEAT -> {
                val label = buildAnnotatedString {
                    val mainNumber = when (displayMode) {
                        SeatNumberingMode.VIRTUAL -> cell.virtualNumber
                        SeatNumberingMode.PHYSICAL -> cell.physicalNumber ?: cell.virtualNumber
                        SeatNumberingMode.MIXED -> cell.virtualNumber
                    }
                    append(mainNumber?.toString() ?: "")

                    if (displayMode == SeatNumberingMode.MIXED && cell.physicalNumber != null) {
                        withStyle(
                            SpanStyle(
                                baselineShift = BaselineShift.Subscript,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Normal
                            )
                        ) {
                            append("(${cell.physicalNumber})")
                        }
                    }
                }

                if (cell.isDpm) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(1.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Accessible,
                            contentDescription = "Acessível DPM",
                            tint = contentColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = label,
                            color = contentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = label,
                        color = contentColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            else -> {}
        }
    }
}
