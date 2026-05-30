package com.appcasa.core.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.airbnb.lottie.compose.*
import kotlinx.coroutines.delay

@Composable
fun CelebrationOverlay(
    xp: Int,
    onDismiss: () -> Unit
) {
    // 1. Carga de Lottie con fallback
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Url("https://assets5.lottiefiles.com/packages/lf20_st8unp.json")
    )
    val progress by animateLottieCompositionAsState(composition)

    // 2. Temporizador de seguridad (Auto-dismiss en 4 segundos si algo falla)
    LaunchedEffect(Unit) {
        delay(4000)
        onDismiss()
    }

    // 3. Dismiss al terminar la animación (si carga)
    LaunchedEffect(progress) {
        if (progress == 1f) {
            delay(300)
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            // Animación Lottie (si está cargada)
            if (composition != null) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.size(400.dp)
                )
            } else {
                // Fallback: Animación simple en Compose si no hay internet
                val infiniteTransition = rememberInfiniteTransition(label = "starAnim")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 0.8f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp).scale(scale),
                    tint = Color(0xFFFFD700)
                )
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 180.dp)
            ) {
                Text(
                    text = "¡Tarea completada!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Black
                )
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = "+$xp XP para el equipo",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
            }
        }
    }
}
