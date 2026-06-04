package com.ubusmobilidade.ubus.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ubusmobilidade.ubus.ui.theme.UbusBorder
import com.ubusmobilidade.ubus.ui.theme.UbusDestructive
import com.ubusmobilidade.ubus.ui.theme.UbusSuccess
import com.ubusmobilidade.ubus.ui.theme.UbusWarning
import com.ubusmobilidade.ubus.ui.util.PasswordStrength

@Composable
fun PasswordStrengthBar(password: String, modifier: Modifier = Modifier) {
    val strength = PasswordStrength.evaluate(password)
    if (password.isEmpty()) return

    val fraction by animateFloatAsState(targetValue = strength.score / 5f)
    val color by animateColorAsState(
        targetValue = when {
            strength.score <= 1 -> UbusDestructive
            strength.score <= 2 -> UbusWarning
            strength.score <= 3 -> Color(0xFFF59E0B)
            else -> UbusSuccess
        },
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(UbusBorder),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color),
            )
        }
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.weight(1f))
            Text(
                strength.label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
            )
        }
    }
}
