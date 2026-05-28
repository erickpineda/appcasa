package com.appcasa.core.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Un componente reutilizable que añade la funcionalidad de "Deslizar para actualizar" (Pull to Refresh).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullToRefreshWrapper(
    modifier: Modifier = Modifier,
    onRefresh: suspend () -> Unit = { delay(1000) }, // Por defecto simula un segundo de carga
    content: @Composable () -> Unit
) {
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                onRefresh()
                isRefreshing = false
            }
        },
        modifier = modifier
    ) {
        content()
    }
}
