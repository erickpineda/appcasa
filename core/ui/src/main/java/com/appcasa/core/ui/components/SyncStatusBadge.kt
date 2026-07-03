package com.appcasa.core.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.appcasa.core.ui.R

@Composable
fun SyncStatusBadge(
    isSynced: Boolean,
    modifier: Modifier = Modifier
) {
    if (isSynced) {
        Icon(
            imageVector = Icons.Default.CloudDone,
            contentDescription = stringResource(R.string.cd_sync_synced),
            modifier = modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
    } else {
        Icon(
            imageVector = Icons.Default.CloudSync,
            contentDescription = stringResource(R.string.cd_sync_pending),
            modifier = modifier.size(14.dp),
            tint = Color(0xFFF44336) // Rojo suave
        )
    }
}
