package com.appcasa.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GoogleIcon(size: Dp = 20.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val red = Color(0xFFEA4335)
        val yellow = Color(0xFFFBBC05)
        val green = Color(0xFF34A853)
        val blue = Color(0xFF4285F4)
        
        drawArc(
            color = blue,
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = true
        )
        drawArc(
            color = green,
            startAngle = 45f,
            sweepAngle = 90f,
            useCenter = true
        )
        drawArc(
            color = yellow,
            startAngle = 135f,
            sweepAngle = 90f,
            useCenter = true
        )
        drawArc(
            color = red,
            startAngle = -135f,
            sweepAngle = 90f,
            useCenter = true
        )
    }
}
