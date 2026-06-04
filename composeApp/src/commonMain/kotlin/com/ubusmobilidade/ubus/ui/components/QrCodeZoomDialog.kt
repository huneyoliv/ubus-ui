package com.ubusmobilidade.ubus.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ubusmobilidade.ubus.ui.util.ForceMaxBrightness

@Composable
fun QrCodeZoomDialog(
    value: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        ForceMaxBrightness()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Toque para fechar",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(32.dp))

                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    DenseQr(
                        value = value,
                        modifier = Modifier.size(260.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DenseQr(
    value: String,
    modifier: Modifier = Modifier,
    modules: Int = 33,
) {
    val safeModules = if (modules < 21) 21 else modules
    val hashes = value.fold(7) { acc, c -> (acc * 31) + c.code }
    Canvas(modifier = modifier.background(Color.White)) {
        val moduleSize = size.minDimension / safeModules
        for (x in 0 until safeModules) {
            for (y in 0 until safeModules) {
                val isFinder = (x < 7 && y < 7) ||
                    (x > safeModules - 8 && y < 7) ||
                    (x < 7 && y > safeModules - 8)
                val isOn = if (isFinder) {
                    val edge = x == 0 || y == 0 || x == 6 || y == 6
                    val center = x in 2..4 && y in 2..4
                    edge || center
                } else {
                    ((x * 17 + y * 31 + hashes) % 7) < 3
                }
                if (isOn) {
                    drawRect(
                        color = Color.Black,
                        topLeft = androidx.compose.ui.geometry.Offset(x * moduleSize, y * moduleSize),
                        size = androidx.compose.ui.geometry.Size(moduleSize, moduleSize),
                    )
                }
            }
        }
    }
}
