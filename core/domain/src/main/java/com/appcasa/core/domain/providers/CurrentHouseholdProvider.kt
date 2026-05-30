package com.appcasa.core.domain.providers

import kotlinx.coroutines.flow.Flow

/**
 * Provee el ID del hogar actual de forma reactiva.
 * La implementación debe asegurar la persistencia de esta selección.
 */
interface CurrentHouseholdProvider {
    val householdId: Flow<Long>
    suspend fun setHouseholdId(id: Long)
    fun getCurrentHouseholdId(): Long
}
