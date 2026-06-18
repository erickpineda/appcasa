package com.appcasa.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun isCompactView(hogarId: Long): Flow<Boolean>
    fun getCurrencySymbol(hogarId: Long): Flow<String>
    suspend fun triggerManualSync(hogarId: Long)
    suspend fun exportData(hogarId: Long): String // Returns a JSON or path to backup
    
    fun isOnboardingCompleted(): Boolean
    fun setOnboardingCompleted(completed: Boolean)
    fun isBiometricLockEnabled(): Boolean
    fun setBiometricLockEnabled(enabled: Boolean)
    fun isBiometricPromptedBefore(): Boolean
    fun setBiometricPromptedBefore(prompted: Boolean)
}
