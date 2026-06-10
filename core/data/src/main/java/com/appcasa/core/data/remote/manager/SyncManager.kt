package com.appcasa.core.data.remote.manager

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor() {
    
    private val _isAppInForeground = MutableStateFlow(true)
    val isAppInForeground = _isAppInForeground.asStateFlow()

    fun setAppInForeground(isInForeground: Boolean) {
        _isAppInForeground.value = isInForeground
    }
}
