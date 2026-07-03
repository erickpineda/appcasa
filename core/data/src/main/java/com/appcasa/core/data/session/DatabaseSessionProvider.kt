package com.appcasa.core.data.session

import android.content.SharedPreferences
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.features.settings.data.local.ConfiguracionDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSessionProvider @Inject constructor(
    private val configuracionDao: ConfiguracionDao,
    private val sharedPrefs: SharedPreferences
) : CurrentHouseholdProvider {

    private val _manualId = MutableStateFlow("")

    init {
        // Cargar el ID guardado al iniciar (Recuerda sesión)
        _manualId.value = sharedPrefs.getString("current_household_id", "") ?: ""
    }

    override val householdId: Flow<String> = combine(
        configuracionDao.getHogarActual().map { it?.id ?: "" },
        _manualId
    ) { dbId, manualId ->
        val id = if (manualId.isNotEmpty()) manualId else dbId
        _cachedId = id
        id
    }.distinctUntilChanged()

    private var _cachedId: String = ""

    override suspend fun setHouseholdId(id: String) {
        sharedPrefs.edit().putString("current_household_id", id).apply()
        _manualId.value = id
        _cachedId = id
    }

    override fun getCurrentHouseholdId(): String {
        return _cachedId
    }
}
