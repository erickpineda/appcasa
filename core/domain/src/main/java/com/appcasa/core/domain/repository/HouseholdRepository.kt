package com.appcasa.core.domain.repository

import com.appcasa.core.domain.model.Household
import kotlinx.coroutines.flow.Flow

interface HouseholdRepository {
    fun getHogarActual(): Flow<Household?>
    fun getAllHogares(): Flow<List<Household>>
    suspend fun getHogarByCodigo(code: String): Household?
    suspend fun insertHogar(hogar: Household): Long
    suspend fun updateCodigoHogar(hogarId: Long, newCode: String)
    suspend fun deleteAllHogares()
}
