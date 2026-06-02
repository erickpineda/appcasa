package com.appcasa.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun BoxScope.AppCasaSutilToast(
    message: String?,
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        if (message != null) {
            visible = true
            delay(2000)
            visible = false
            delay(300) // tiempo de animación de salida
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Black.copy(alpha = 0.75f))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = message ?: "",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
