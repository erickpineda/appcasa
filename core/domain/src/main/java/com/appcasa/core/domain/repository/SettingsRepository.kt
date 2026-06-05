package com.appcasa.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun isCompactView(hogarId: Long): Flow<Boolean>
    fun getCurrencySymbol(hogarId: Long): Flow<String>
}
