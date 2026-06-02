package com.appcasa.features.tasks.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.TipoMiembro
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.features.family.data.local.MiembroDao
import com.appcasa.features.family.data.local.MiembroEntity
import com.appcasa.features.tasks.data.local.RecompensaDao
import com.appcasa.features.tasks.data.local.RecompensaEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RewardStoreViewModel @Inject constructor(
    private val recompensaDao: RecompensaDao,
    private val miembroDao: MiembroDao,
    private val currentHouseholdProvider: CurrentHouseholdProvider
) : ViewModel() {

    private val householdId: Long get() = currentHouseholdProvider.getCurrentHouseholdId()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val recompensas: StateFlow<List<RecompensaEntity>> = currentHouseholdProvider.householdId
        .flatMapLatest { recompensaDao.getRecompensasByHogar(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val members: StateFlow<List<MiembroEntity>> = currentHouseholdProvider.householdId
        .flatMapLatest { miembroDao.getMiembrosByHogar(it) }
        .map { list -> list.filter { it.tipo == TipoMiembro.PERSONA.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addRecompensa(titulo: String, puntos: Int, desc: String?) {
        viewModelScope.launch {
            recompensaDao.insertRecompensa(
                RecompensaEntity(
                    hogarId = householdId,
                    titulo = titulo,
                    costePuntos = puntos,
                    descripcion = desc
                )
            )
        }
    }

    fun redeemReward(memberId: Long, recompensa: RecompensaEntity) {
        viewModelScope.launch {
            val miembro = miembroDao.getMiembroById(memberId)
            if (miembro != null && miembro.puntos >= recompensa.costePuntos) {
                miembroDao.updateMiembro(miembro.copy(
                    puntos = miembro.puntos - recompensa.costePuntos,
                    updatedAt = System.currentTimeMillis()
                ))
                // Aquí se podría guardar un historial de canjes si fuera necesario
            }
        }
    }
    
    fun deleteReward(recompensa: RecompensaEntity) {
        viewModelScope.launch {
            recompensaDao.deleteRecompensa(recompensa)
        }
    }
}
