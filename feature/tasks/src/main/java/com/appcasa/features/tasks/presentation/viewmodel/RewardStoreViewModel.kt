package com.appcasa.features.tasks.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.FamilyMember
import com.appcasa.core.domain.model.Reward
import com.appcasa.core.domain.model.TipoMiembro
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.core.domain.usecase.household.GetFamilyMembersUseCase
import com.appcasa.features.tasks.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RewardStoreViewModel @Inject constructor(
    private val getRewardsUseCase: GetRewardsUseCase,
    private val addRewardUseCase: AddRewardUseCase,
    private val redeemRewardUseCase: RedeemRewardUseCase,
    private val deleteRewardUseCase: DeleteRewardUseCase,
    private val getFamilyMembersUseCase: GetFamilyMembersUseCase,
    private val currentHouseholdProvider: CurrentHouseholdProvider
) : ViewModel() {

    private val householdId: String get() = currentHouseholdProvider.getCurrentHouseholdId()

    private val _events = MutableSharedFlow<RewardEvent>()
    val events = _events.asSharedFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val recompensas: StateFlow<List<Reward>> = currentHouseholdProvider.householdId
        .flatMapLatest { getRewardsUseCase(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val members: StateFlow<List<FamilyMember>> = currentHouseholdProvider.householdId
        .flatMapLatest { getFamilyMembersUseCase(it) }
        .map { list -> list.filter { it.tipo == TipoMiembro.PERSONA } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addRecompensa(titulo: String, puntos: Int, desc: String?) {
        viewModelScope.launch {
            addRewardUseCase(householdId, titulo, puntos, desc)
        }
    }

    fun redeemReward(memberId: String, recompensa: Reward) {
        viewModelScope.launch {
            val success = redeemRewardUseCase(memberId, recompensa)
            if (success) {
                _events.emit(RewardEvent.Success)
            } else {
                _events.emit(RewardEvent.Error("Puntos insuficientes"))
            }
        }
    }
    
    fun deleteReward(recompensa: Reward) {
        viewModelScope.launch {
            deleteRewardUseCase(recompensa)
        }
    }

    sealed interface RewardEvent {
        data object Success : RewardEvent
        data class Error(val message: String) : RewardEvent
    }
}
