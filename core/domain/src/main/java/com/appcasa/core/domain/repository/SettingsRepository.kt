package com.appcasa.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun isCompactView(hogarId: String): Flow<Boolean>
    fun getCurrencySymbol(hogarId: String): Flow<String>
    suspend fun triggerManualSync(hogarId: String)
    suspend fun exportData(hogarId: String): String // Returns a JSON or path to backup
    
    fun isOnboardingCompleted(): Boolean
    fun setOnboardingCompleted(completed: Boolean)
    fun isBiometricLockEnabled(): Boolean
    fun setBiometricLockEnabled(enabled: Boolean)
    fun isBiometricPromptedBefore(): Boolean
    fun setBiometricPromptedBefore(prompted: Boolean)
}
