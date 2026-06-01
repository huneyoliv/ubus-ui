package com.ubusmobilidade.ubus.ui.screens.student

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary
import kotlin.math.abs

@Composable
fun CarteirinhaScreen(component: RootComponent) {
    val user = component.authStorage.user
    val userName = user?.name ?: ""
    val initial = userName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        IconButton(onClick = { component.goBack() }, modifier = Modifier.padding(top = 8.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = MaterialTheme.colorScheme.onBackground)
        }

        Text(
            "Carteirinha",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
        )

        // Cartão Físico com Gradiente Premium
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(UbusPrimary, Color(0xFF6366F1), Color(0xFF4F46E5))
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Header do Cartão
                Text(
                    "TRANSPORTE ESCOLAR MUNICIPAL",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(20.dp))

                // Avatar
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF818CF8), Color(0xFF6366F1))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            initial,
                            color = Color.White,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Nome do estudante e detalhes
                Text(
                    userName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    user?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    "CPF: ${user?.cpf ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )

                user?.status?.let { status ->
                    Spacer(Modifier.height(12.dp))
                    com.ubusmobilidade.ubus.ui.components.StatusChip(status = status)
                }

                Spacer(Modifier.height(24.dp))

                // QR Code
                val userId = user?.id ?: ""
                val hashCode = remember(userId) { abs(userId.hashCode()) }
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val gridSize = 11
                        val cellSize = size.width / gridSize
                        val seed = hashCode
                        for (row in 0 until gridSize) {
                            for (col in 0 until gridSize) {
                                val isBorder = row == 0 || row == gridSize - 1 || col == 0 || col == gridSize - 1
                                val isTopLeftFinder = row < 3 && col < 3
                                val isTopRightFinder = row < 3 && col >= gridSize - 3
                                val isBottomLeftFinder = row >= gridSize - 3 && col < 3
                                val isFinder = isTopLeftFinder || isTopRightFinder || isBottomLeftFinder

                                val filled = if (isFinder) {
                                    val lr = if (isTopLeftFinder) row else if (isBottomLeftFinder) row - (gridSize - 3) else row
                                    val lc = if (isTopLeftFinder || isBottomLeftFinder) col else col - (gridSize - 3)
                                    !(lr == 1 && lc == 1)
                                } else if (isBorder) {
                                    (row + col) % 2 == 0
                                } else {
                                    val bit = (seed xor (row * 31 + col * 17 + row * col * 7))
                                    bit % 3 != 0
                                }

                                if (filled) {
                                    drawRect(
                                        color = Color.Black,
                                        topLeft = Offset(col * cellSize, row * cellSize),
                                        size = Size(cellSize, cellSize),
                                    )
                                }
                            }
                        }
                    }
                }

                if (userId.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        userId,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Badge ESTUDANTE
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 28.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "ESTUDANTE",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 2.5.sp,
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}
