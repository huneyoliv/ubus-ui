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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ubusmobilidade.ubus.navigation.RootComponent
import com.ubusmobilidade.ubus.ui.components.BentoCard
import com.ubusmobilidade.ubus.ui.theme.UbusPrimary
import com.ubusmobilidade.ubus.ui.theme.UbusPrimaryContainer
import com.ubusmobilidade.ubus.ui.theme.UbusText3
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
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
        )

        BentoCard(cornerRadius = 24.dp) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
            ) {
                // Avatar — first letter of name
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(UbusPrimary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        initial,
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    userName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(4.dp))
                Text(user?.email ?: "", style = MaterialTheme.typography.bodyMedium, color = UbusText3)
                Text("CPF: ${user?.cpf ?: ""}", style = MaterialTheme.typography.bodySmall, color = UbusText3)

                Spacer(Modifier.height(24.dp))

                // QR-like pattern generated from user ID
                val userId = user?.id ?: ""
                val hashCode = remember(userId) { abs(userId.hashCode()) }
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(2.dp, UbusPrimary, RoundedCornerShape(12.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    Canvas(modifier = Modifier.size(140.dp)) {
                        val gridSize = 11
                        val cellSize = size.width / gridSize
                        val seed = hashCode
                        for (row in 0 until gridSize) {
                            for (col in 0 until gridSize) {
                                // Border cells always filled (finder pattern)
                                val isBorder = row == 0 || row == gridSize - 1 || col == 0 || col == gridSize - 1
                                // Corner squares (3×3 finder patterns)
                                val isTopLeftFinder = row < 3 && col < 3
                                val isTopRightFinder = row < 3 && col >= gridSize - 3
                                val isBottomLeftFinder = row >= gridSize - 3 && col < 3
                                val isFinder = isTopLeftFinder || isTopRightFinder || isBottomLeftFinder

                                val filled = if (isFinder) {
                                    // Solid corners with inner empty
                                    val lr = if (isTopLeftFinder) row else if (isBottomLeftFinder) row - (gridSize - 3) else row
                                    val lc = if (isTopLeftFinder || isBottomLeftFinder) col else col - (gridSize - 3)
                                    !(lr == 1 && lc == 1)
                                } else if (isBorder) {
                                    (row + col) % 2 == 0
                                } else {
                                    // Pseudo-random from hash
                                    val bit = (seed xor (row * 31 + col * 17 + row * col * 7))
                                    bit % 3 != 0
                                }

                                if (filled) {
                                    drawRect(
                                        color = UbusPrimary,
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
                        color = UbusText3,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                    )
                }

                Spacer(Modifier.height(16.dp))

                // ESTUDANTE badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(UbusPrimaryContainer)
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "ESTUDANTE",
                        color = UbusPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 2.sp,
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}
