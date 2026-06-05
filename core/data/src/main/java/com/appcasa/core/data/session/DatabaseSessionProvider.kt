package com.appcasa.core.data.session

import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.features.settings.data.local.ConfiguracionDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSessionProvider @Inject constructor(
    private val configuracionDao: ConfiguracionDao
) : CurrentHouseholdProvider {

    private var _cachedId: Long = 0L

    override val householdId: Flow<Long> = configuracionDao.getHogarActual()
        .map { it?.id ?: 0L }
        .distinctUntilChanged()
        .onEach { _cachedId = it }

    override suspend fun setHouseholdId(id: Long) {
        _cachedId = id
    }

    override fun getCurrentHouseholdId(): Long {
        return _cachedId
    }
}
