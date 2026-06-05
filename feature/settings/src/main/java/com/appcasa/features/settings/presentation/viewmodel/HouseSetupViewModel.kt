package com.appcasa.features.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.FamilyMember
import com.appcasa.core.domain.usecase.GetFamilyMembersUseCase
import com.appcasa.features.settings.domain.usecase.CreateHouseholdUseCase
import com.appcasa.features.settings.domain.usecase.GetCurrentHouseholdUseCase
import com.appcasa.features.settings.domain.usecase.JoinHouseholdUseCase
import com.appcasa.features.settings.domain.usecase.ResetHouseholdUseCase
import com.appcasa.features.settings.domain.usecase.SelectMemberUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HouseSetupViewModel @Inject constructor(
    private val getCurrentHouseholdUseCase: GetCurrentHouseholdUseCase,
    private val getFamilyMembersUseCase: GetFamilyMembersUseCase,
    private val createHouseholdUseCase: CreateHouseholdUseCase,
    private val joinHouseholdUseCase: JoinHouseholdUseCase,
    private val selectMemberUseCase: SelectMemberUseCase,
    private val resetHouseholdUseCase: ResetHouseholdUseCase
) : ViewModel() {

    private val _setupEvent = MutableSharedFlow<SetupResult>(replay = 0)
    val setupEvent = _setupEvent.asSharedFlow()

    private val _isCheckingDb = MutableStateFlow(true)
    val isCheckingDb = _isCheckingDb.asStateFlow()

    val existingHousehold = getCurrentHouseholdUseCase()
        .onEach { _isCheckingDb.value = false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val householdMembers = existingHousehold.flatMapLatest { hogar ->
        hogar?.let { getFamilyMembersUseCase(it.id) } ?: flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createHousehold(houseName: String, userName: String, photoUri: String?) {
        viewModelScope.launch {
            createHouseholdUseCase(houseName, userName, photoUri)
            _setupEvent.emit(SetupResult.Success)
        }
    }

    fun joinHousehold(code: String, userName: String, photoUri: String?) {
        viewModelScope.launch {
            val success = joinHouseholdUseCase(code, userName, photoUri)
            if (success) {
                _setupEvent.emit(SetupResult.Success)
            } else {
                _setupEvent.emit(SetupResult.Error("Código de hogar no encontrado"))
            }
        }
    }

    fun selectMember(member: FamilyMember) {
        viewModelScope.launch {
            selectMemberUseCase(member)
            _setupEvent.emit(SetupResult.Success)
        }
    }

    fun resetHousehold() {
        viewModelScope.launch {
            resetHouseholdUseCase()
        }
    }

    sealed class SetupResult {
        data object Success : SetupResult()
        data class Error(val message: String) : SetupResult()
    }
}
