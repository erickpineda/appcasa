package com.appcasa.core.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun AppCasaConfirmDialog(
    show: Boolean,
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = "Eliminar",
    dismissText: String = "Cancelar",
    icon: ImageVector? = null,
    isDestructive: Boolean = true
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = icon?.let { { Icon(it, contentDescription = null) } },
            title = { Text(title) },
            text = { Text(text) },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    colors = if (isDestructive) {
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    } else {
                        ButtonDefaults.buttonColors()
                    }
                ) {
                    Text(confirmText)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(dismissText)
                }
            }
        )
    }
}
