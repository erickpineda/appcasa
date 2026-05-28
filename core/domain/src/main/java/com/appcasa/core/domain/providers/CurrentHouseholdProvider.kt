package com.appcasa.core.domain.providers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface CurrentHouseholdProvider {
    val householdId: Flow<Long>
    suspend fun setHouseholdId(id: Long)
    fun getCurrentHouseholdId(): Long
}

class DefaultCurrentHouseholdProvider : CurrentHouseholdProvider {
    private val _householdId = MutableStateFlow(1L)
    override val householdId: Flow<Long> = _householdId.asStateFlow()

    override suspend fun setHouseholdId(id: Long) {
        _householdId.value = id
    }

    override fun getCurrentHouseholdId(): Long {
        return _householdId.value
    }
}
