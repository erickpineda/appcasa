package com.appcasa.features.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.FamilyMember
import com.appcasa.core.domain.model.Household
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.core.domain.usecase.household.GetFamilyMembersUseCase
import com.appcasa.core.ui.utils.UiText
import com.appcasa.feature.settings.R
import com.appcasa.features.settings.domain.usecase.*
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HouseSetupViewModel @Inject constructor(
  private val getCurrentHouseholdUseCase: GetCurrentHouseholdUseCase,
  private val getHouseholdByIdUseCase: GetHouseholdByIdUseCase,
  private val getFamilyMembersUseCase: GetFamilyMembersUseCase,
  private val createHouseholdUseCase: CreateHouseholdUseCase,
  private val joinHouseholdUseCase: JoinHouseholdUseCase,
  private val selectMemberUseCase: SelectMemberUseCase,
  private val resetHouseholdUseCase: ResetHouseholdUseCase,
  private val getAllHouseholdsUseCase: GetAllHouseholdsUseCase,
  private val switchHouseholdUseCase: SwitchHouseholdUseCase,
  private val recoverHouseholdsUseCase: RecoverHouseholdsUseCase,
  private val linkAccountUseCase: LinkAccountUseCase,
  private val firebaseAuth: FirebaseAuth,
  private val currentHouseholdProvider: CurrentHouseholdProvider
) : ViewModel() {

  private val _setupEvent = MutableSharedFlow<SetupResult>(replay = 0)
  val setupEvent = _setupEvent.asSharedFlow()

  private val _navEvent = MutableSharedFlow<SetupStep>(replay = 0)
  val navEvent = _navEvent.asSharedFlow()

  private val _isCheckingDb = MutableStateFlow(true)
  val isCheckingDb = _isCheckingDb.asStateFlow()

  private val _discoveredHousehold = MutableStateFlow<Household?>(null)
  val discoveredHousehold = _discoveredHousehold.asStateFlow()

  private var pendingJoinCode: String? = null
  private var pendingCreateHouseName: String? = null
  private var pendingUserName: String? = null
  private var pendingPhotoUri: String? = null

  init {
    // Al iniciar, si el usuario está logueado pero no tenemos hogares locales,
    // intentamos una recuperación silenciosa de la nube.
    viewModelScope.launch {
      if (isUserLoggedIn()) {
        val households = getAllHouseholdsUseCase().first()
        if (households.isEmpty()) {
          silentRecoverHouseholds()
        }
      }
    }
  }

  fun isUserLoggedIn(): Boolean = firebaseAuth.currentUser != null
  fun getCurrentUserEmail(): String? = firebaseAuth.currentUser?.email

  val isLoggedIn: StateFlow<Boolean> = callbackFlow {
    val listener = FirebaseAuth.IdTokenListener { auth ->
      trySend(auth.currentUser != null)
    }
    firebaseAuth.addIdTokenListener(listener)
    awaitClose { firebaseAuth.removeIdTokenListener(listener) }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), firebaseAuth.currentUser != null)

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val existingHousehold: StateFlow<Household?> = currentHouseholdProvider.householdId
    .flatMapLatest { id ->
      if (id > 0L) getHouseholdByIdUseCase(id) else flowOf(null)
    }
    .onEach { _isCheckingDb.value = false }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  val allHouseholds = getAllHouseholdsUseCase()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val householdMembers = currentHouseholdProvider.householdId.flatMapLatest { id ->
    if (id > 0) getFamilyMembersUseCase(id) else flowOf(emptyList())
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun searchHousehold(code: String) {
    if (code.length < 6) {
      _discoveredHousehold.value = null
      return
    }
    viewModelScope.launch {
      try {
        val house = joinHouseholdUseCase.findHousehold(code)
        _discoveredHousehold.value = house
      } catch (e: Exception) {
        _discoveredHousehold.value = null
      }
    }
  }

  fun setPendingJoinData(code: String, userName: String, photoUri: String?) {
    pendingJoinCode = code
    pendingUserName = userName
    pendingPhotoUri = photoUri
  }

  fun setPendingCreateData(houseName: String, userName: String, photoUri: String?) {
    pendingCreateHouseName = houseName
    pendingUserName = userName
    pendingPhotoUri = photoUri
  }

  fun tryCompletePendingActions() {
    if (pendingJoinCode != null) {
      val code = pendingJoinCode!!
      val name = pendingUserName ?: ""
      if (name.isNotEmpty()) {
        joinHousehold(code, name, pendingPhotoUri)
        pendingJoinCode = null
      } else {
        discoverAndJoin(code)
        pendingJoinCode = null
      }
    } else if (pendingCreateHouseName != null) {
      val houseName = pendingCreateHouseName!!
      val userName = pendingUserName ?: ""
      if (houseName.isNotEmpty() && userName.isNotEmpty()) {
        createHousehold(houseName, userName, pendingPhotoUri)
        pendingCreateHouseName = null
      } else {
        viewModelScope.launch { _navEvent.emit(SetupStep.CREATE) }
        pendingCreateHouseName = null
      }
    }
  }

  fun getPendingStep(): String? {
    return if (pendingCreateHouseName != null && pendingCreateHouseName!!.isEmpty()) STEP_CREATE
    else if (pendingJoinCode != null && (pendingUserName ?: "").isEmpty()) STEP_JOIN
    else null
  }

  fun clearPendingStep() {
    pendingCreateHouseName = null
    pendingJoinCode = null
  }

  fun createHousehold(houseName: String, userName: String, photoUri: String?) {
    viewModelScope.launch {
      try {
        createHouseholdUseCase(houseName, userName, photoUri)
        _setupEvent.emit(SetupResult.Success)
      } catch (e: Exception) {
        e.printStackTrace()
        _setupEvent.emit(SetupResult.Error(UiText.StringResource(R.string.settings_error_generic)))
      }
    }
  }

  fun joinHousehold(code: String, userName: String, photoUri: String?) {
    viewModelScope.launch {
      val success = joinHouseholdUseCase(code, userName, photoUri)
      if (success) {
        _setupEvent.emit(SetupResult.Success)
      } else {
        _setupEvent.emit(SetupResult.Error(UiText.StringResource(R.string.setup_error_invalid_code)))
      }
    }
  }

  fun discoverAndJoin(code: String) {
    viewModelScope.launch {
      try {
        _isCheckingDb.value = true
        val success = joinHouseholdUseCase.discoverHousehold(code)
        _isCheckingDb.value = false
        if (success) {
          _navEvent.emit(SetupStep.SELECT_PROFILE)
        } else {
          _setupEvent.emit(SetupResult.Error(UiText.StringResource(R.string.setup_error_invalid_code)))
        }
      } catch (e: Exception) {
        _isCheckingDb.value = false
        e.printStackTrace()
        _setupEvent.emit(SetupResult.Error(UiText.StringResource(R.string.setup_error_recover_failed)))
      }
    }
  }

  fun selectMember(member: FamilyMember) {
    viewModelScope.launch {
      selectMemberUseCase(member)
      _setupEvent.emit(SetupResult.Success)
    }
  }

  fun switchHousehold(householdId: Long) {
    viewModelScope.launch {
      switchHouseholdUseCase(householdId)
    }
  }

  fun resetHousehold() {
    viewModelScope.launch {
      resetHouseholdUseCase()
    }
  }

  /**
   * Recuperación automática sin mensajes de error para el usuario.
   */
  fun silentRecoverHouseholds() {
    val email = firebaseAuth.currentUser?.email ?: return
    viewModelScope.launch {
      try {
        _isCheckingDb.value = true
        val houses = recoverHouseholdsUseCase(email)
        _isCheckingDb.value = false

        if (houses.isNotEmpty()) {
          if (houses.size == 1) {
            // Si solo hay una, la pre-seleccionamos y vamos a perfiles
            switchHousehold(houses.first().id)
            _navEvent.emit(SetupStep.SELECT_PROFILE)
          } else {
            // Si hay varias, mostramos la lista para elegir
            _navEvent.emit(SetupStep.SWITCH_HOUSEHOLD)
          }
        }
      } catch (e: Exception) {
        _isCheckingDb.value = false
      }
    }
  }

  fun recoverHouseholdsManual(email: String) {
    viewModelScope.launch {
      try {
        _isCheckingDb.value = true
        val houses = recoverHouseholdsUseCase(email)
        _isCheckingDb.value = false
        if (houses.isEmpty()) {
          _setupEvent.emit(SetupResult.Error(UiText.DynamicString("No hay hogares vinculados a esta cuenta. Si es la primera vez en este móvil, usa el 'Código de Invitación' de tu casa.")))
        }
      } catch (e: Exception) {
        _isCheckingDb.value = false
        e.printStackTrace()
        _setupEvent.emit(SetupResult.Error(UiText.StringResource(R.string.setup_error_recover_failed)))
      }
    }
  }

  sealed class SetupResult {
    data object Success : SetupResult()
    data class Error(val message: UiText) : SetupResult()
  }

  enum class SetupStep {
    WELCOME, CREATE, JOIN, SELECT_PROFILE, ADD_PROFILE, SWITCH_HOUSEHOLD
  }

  companion object {
    const val STEP_CREATE = "CREATE"
    const val STEP_JOIN = "JOIN"
  }
}
