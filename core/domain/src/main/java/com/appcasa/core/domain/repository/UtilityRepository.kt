package com.appcasa.core.domain.repository

import com.appcasa.core.domain.model.Utility
import kotlinx.coroutines.flow.Flow

interface UtilityRepository {
    fun getUtilidades(): Flow<List<Utility>>
    suspend fun insertUtilidad(utility: Utility)
}
