package com.appcasa.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun AppCasaCard(
  modifier: Modifier = Modifier,
  shape: Shape = MaterialTheme.shapes.large,
  containerColor: Color? = null,
  useGlassmorphism: Boolean = false,
  onClick: (() -> Unit)? = null,
  content: @Composable ColumnScope.() -> Unit
) {
  val baseModifier = if (onClick != null) {
    modifier.bounceClick(onClick = onClick)
  } else {
    modifier
  }

  val finalModifier = if (useGlassmorphism) {
    baseModifier.glassmorphism(shape = shape)
  } else {
    baseModifier.shadow(
      elevation = 8.dp,
      shape = shape,
      ambientColor = MaterialTheme.colorScheme.primary,
      spotColor = MaterialTheme.colorScheme.primary
    )
  }

  val defaultContainerColor = if (useGlassmorphism) {
    MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
  } else {
    MaterialTheme.colorScheme.surface
  }

  Card(
    modifier = finalModifier,
    shape = shape,
    colors = CardDefaults.cardColors(
      containerColor = containerColor ?: defaultContainerColor,
      contentColor = MaterialTheme.colorScheme.onSurface
    ),
    border = if (useGlassmorphism) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)) else null,
    content = content
  )
}
