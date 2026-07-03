package com.appcasa.features.family.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.FamilyMember
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.core.domain.usecase.household.GetFamilyMembersUseCase
import com.appcasa.core.domain.usecase.household.SyncBirthdayEventUseCase
import com.appcasa.core.domain.usecase.user.GetCurrentUserUseCase
import com.appcasa.core.domain.model.User
import com.appcasa.features.family.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FamilyViewModel @Inject constructor(
  private val getFamilyMembersUseCase: GetFamilyMembersUseCase,
  private val getPeopleUseCase: GetPeopleUseCase,
  private val getPetsUseCase: GetPetsUseCase,
  private val deleteMemberUseCase: DeleteMemberUseCase,
  private val syncBirthdayEventUseCase: SyncBirthdayEventUseCase,
  private val currentHouseholdProvider: CurrentHouseholdProvider,
  private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

  private val householdId: String get() = currentHouseholdProvider.getCurrentHouseholdId()

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val familyMembers: StateFlow<List<FamilyMember>> = currentHouseholdProvider.householdId
    .flatMapLatest { id -> getFamilyMembersUseCase(id) }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val people: StateFlow<List<FamilyMember>> = currentHouseholdProvider.householdId
    .flatMapLatest { id -> getPeopleUseCase(id) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val pets: StateFlow<List<FamilyMember>> = currentHouseholdProvider.householdId
    .flatMapLatest { id -> getPetsUseCase(id) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val currentUser: StateFlow<User?> = getCurrentUserUseCase()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  fun deleteMember(member: FamilyMember) {
    viewModelScope.launch {
      deleteMemberUseCase(member)
    }
  }

  fun syncBirthdayEvent(member: FamilyMember) {
    viewModelScope.launch {
      syncBirthdayEventUseCase(member.id)
    }
  }
}
