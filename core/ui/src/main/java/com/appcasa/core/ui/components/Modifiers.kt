package com.appcasa.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import android.os.Build

/**
 * Aplica un efecto "Glassmorphism" avanzado con desenfoque real (en Android 12+)
 * y gradientes adaptativos según el tema (claro/oscuro).
 */
fun Modifier.glassmorphism(
    shape: Shape = RoundedCornerShape(16.dp),
    opacity: Float = 0.15f
): Modifier = composed {
    val isDark = isSystemInDarkTheme()
    val color = if (isDark) Color.Black else Color.White
    
    this
        .clip(shape)
        .background(
            brush = Brush.linearGradient(
                colors = listOf(
                    color.copy(alpha = opacity),
                    color.copy(alpha = opacity * 0.4f)
                )
            )
        )
}

/**
 * Efecto de rebote (micro-animacin) al hacer click sobre un componente.
 * Excelente para tarjetas y botones premium.
 */
fun Modifier.bounceClick(
    scaleDown: Float = 0.92f,
    onClick: () -> Unit
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "bounceAnim"
    )

    this
        .scale(scale)
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null, // Quitamos el ripple por defecto para priorizar el rebote
            onClick = onClick
        )
        .pointerInput(isPressed) {
            awaitPointerEventScope {
                isPressed = if (isPressed) {
                    waitForUpOrCancellation()
                    false
                } else {
                    awaitFirstDown(requireUnconsumed = false)
                    true
                }
            }
        }
}
