package com.ubusmobilidade.ubus.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ubusmobilidade.ubus.navigation.RootComponent
import kotlinx.coroutines.delay

private data class SplashSlide(
    val tag: String,
    val title: String,
    val description: String,
)

private val slides = listOf(
    SplashSlide(
        tag = "Mobilidade",
        title = "Seu transporte\nuniversitário",
        description = "Reserve seu assento, acompanhe o ônibus e viaje com tranquilidade.",
    ),
    SplashSlide(
        tag = "Educação",
        title = "Exclusivo para\nestudantes",
        description = "Cadastre-se com sua matrícula e tenha acesso ao transporte gratuito.",
    ),
    SplashSlide(
        tag = "Segurança",
        title = "Bilhete digital\nantifraude",
        description = "Seu bilhete é gerado em tempo real. Seguro e impossível de falsificar.",
    ),
)

@Composable
fun SplashScreen(component: RootComponent) {
    var currentSlide by remember { mutableStateOf(0) }

    // Check if already authenticated
    LaunchedEffect(Unit) {
        if (component.authStorage.isAuthenticated) {
            component.onLoginSuccess()
            return@LaunchedEffect
        }
    }

    // Auto-advance slides
    LaunchedEffect(currentSlide) {
        delay(3500)
        if (currentSlide < slides.lastIndex) {
            currentSlide++
        }
    }

    val slide = slides[currentSlide]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E3A8A),
                        Color(0xFF0F172A),
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
        ) {
            // Header with logo
            Row(
                modifier = Modifier.padding(top = 56.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.DirectionsBus,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    "ubus.me",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
            }

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                // Tag pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(
                        slide.tag,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Spacer(Modifier.height(32.dp))

                Text(
                    slide.title,
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 40.sp,
                    letterSpacing = (-1).sp,
                )

                Spacer(Modifier.height(20.dp))

                Text(
                    slide.description,
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                )
            }

            // Bottom area
            Column(modifier = Modifier.padding(bottom = 48.dp)) {
                // Slide indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 32.dp),
                ) {
                    slides.forEachIndexed { i, _ ->
                        Box(
                            modifier = Modifier
                                .height(4.dp)
                                .width(if (i == currentSlide) 24.dp else 6.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (i == currentSlide) Color.White
                                    else Color.White.copy(alpha = 0.25f)
                                ),
                        )
                    }
                }

                // Buttons
                Button(
                    onClick = { component.navigateTo(RootComponent.Config.Cadastro) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF0F172A),
                    ),
                ) {
                    Text("Começar agora", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                }

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { component.navigateTo(RootComponent.Config.Login) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White,
                    ),
                ) {
                    Text("Já tenho conta", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    "© 2026 Ubus — Todos os direitos reservados",
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
