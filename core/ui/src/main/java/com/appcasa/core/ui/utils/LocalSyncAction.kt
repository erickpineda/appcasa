package com.appcasa.core.ui.utils

import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.delay

/**
 * A CompositionLocal that provides a global sync action.
 * Screens using PullToRefreshWrapper will consume this action to trigger real sync.
 */
val LocalSyncAction = staticCompositionLocalOf<suspend () -> Unit> {
    { delay(1000) } // Default fallback
}
