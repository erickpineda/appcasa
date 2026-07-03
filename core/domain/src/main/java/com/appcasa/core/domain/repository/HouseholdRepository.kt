package com.appcasa.core.domain.repository

import com.appcasa.core.domain.model.Household
import kotlinx.coroutines.flow.Flow

interface HouseholdRepository {
    fun getHogarActual(): Flow<Household?>
    fun getHogarById(id: String): Flow<Household?>
    fun getAllHogares(): Flow<List<Household>>
    suspend fun getHogarByCodigo(code: String): Household?
    suspend fun insertHogar(hogar: Household): String
    suspend fun findHouseholdRemotely(code: String): Household?
    suspend fun findHouseholdsByUserEmail(email: String): List<Household>
    suspend fun findHouseholdsByUserUid(uid: String): List<Household>
    suspend fun updateCodigoHogar(hogarId: String, newCode: String)
    suspend fun updateHogarSyncTimestamp(hogarId: String, timestamp: Long)
    suspend fun deleteHogar(id: String)
    suspend fun deleteAllHogares()
    suspend fun clearAllLocalData()
}
