package com.appcasa.features.settings.presentation.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
import dagger.hilt.android.qualifiers.ApplicationContext
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
  private val logoutUseCase: LogoutUseCase,
  private val getAllHouseholdsUseCase: GetAllHouseholdsUseCase,
  private val switchHouseholdUseCase: SwitchHouseholdUseCase,
  private val recoverHouseholdsUseCase: RecoverHouseholdsUseCase,
  private val linkAccountUseCase: LinkAccountUseCase,
  private val firebaseAuth: FirebaseAuth,
  private val currentHouseholdProvider: CurrentHouseholdProvider,
  private val sharedPreferences: SharedPreferences,
  @ApplicationContext private val context: Context
) : ViewModel() {

  private val _uiState = MutableStateFlow(SetupUiState())
  val uiState = _uiState.asStateFlow()

  private val _uiEffect = MutableSharedFlow<SetupUiEffect>(replay = 0)
  val uiEffect = _uiEffect.asSharedFlow()

  private var pendingJoinCode: String? = null
  private var pendingCreateHouseName: String? = null
  private var pendingUserName: String? = null
  private var pendingPhotoUri: String? = null

  init {
    val authListenerFlow = callbackFlow {
      val listener = FirebaseAuth.IdTokenListener { auth ->
        trySend(auth.currentUser != null)
      }
      firebaseAuth.addIdTokenListener(listener)
      awaitClose { firebaseAuth.removeIdTokenListener(listener) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, firebaseAuth.currentUser != null)

    val existingHouseholdFlow = currentHouseholdProvider.householdId
      .flatMapLatest { id ->
        if (id > 0L) getHouseholdByIdUseCase(id) else flowOf(null)
      }

    val allHouseholdsFlow = getAllHouseholdsUseCase()

    val householdMembersFlow = currentHouseholdProvider.householdId
      .flatMapLatest { id ->
        if (id > 0) getFamilyMembersUseCase(id) else flowOf(emptyList())
      }

    viewModelScope.launch {
      combine(
        authListenerFlow,
        existingHouseholdFlow,
        allHouseholdsFlow,
        householdMembersFlow
      ) { isLoggedIn, existingHouse, allHouses, members ->
        _uiState.update { state ->
          state.copy(
            isLoggedIn = isLoggedIn,
            existingHousehold = existingHouse,
            allHouseholds = allHouses,
            householdMembers = members,
            isCheckingDb = false,
            userEmail = firebaseAuth.currentUser?.email
          )
        }
      }.collect()
    }

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

  fun handleIntent(intent: SetupIntent) {
    when (intent) {
      is SetupIntent.SearchHousehold -> searchHousehold(intent.code)
      is SetupIntent.SetPendingJoin -> setPendingJoinData(intent.code, intent.userName, intent.photoUri)
      is SetupIntent.SetPendingCreate -> setPendingCreateData(intent.houseName, intent.userName, intent.photoUri)
      is SetupIntent.TryCompletePendingActions -> tryCompletePendingActions()
      is SetupIntent.ClearPendingStep -> clearPendingStep()
      is SetupIntent.CreateHousehold -> createHousehold(intent.houseName, intent.userName, intent.photoUri)
      is SetupIntent.JoinHousehold -> joinHousehold(intent.code, intent.userName, intent.photoUri)
      is SetupIntent.DiscoverAndJoin -> discoverAndJoin(intent.code)
      is SetupIntent.SelectMember -> selectMember(intent.member)
      is SetupIntent.SwitchHousehold -> switchHousehold(intent.id)
      is SetupIntent.Logout -> logout()
      is SetupIntent.SilentRecoverHouseholds -> silentRecoverHouseholds()
      is SetupIntent.RecoverHouseholdsManual -> recoverHouseholdsManual(intent.email)
      is SetupIntent.CheckNetworkStatus -> checkNetworkStatus()
      is SetupIntent.SetupBiometrics -> setupBiometrics(intent.enable)
      is SetupIntent.SaveOnboardingCompleted -> saveOnboardingCompleted()
    }
  }

  private fun checkNetworkStatus(): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork
    val capabilities = connectivityManager.getNetworkCapabilities(network)
    val hasInternet = capabilities != null && (
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    )
    _uiState.update { it.copy(isNetworkAvailable = hasInternet) }
    return hasInternet
  }

  private fun searchHousehold(code: String) {
    if (code.length < 6) {
      _uiState.update { it.copy(discoveredHousehold = null) }
      return
    }
    viewModelScope.launch {
      try {
        val house = joinHouseholdUseCase.findHousehold(code)
        _uiState.update { it.copy(discoveredHousehold = house) }
      } catch (e: Exception) {
        _uiState.update { it.copy(discoveredHousehold = null) }
      }
    }
  }

  private fun setPendingJoinData(code: String, userName: String, photoUri: String?) {
    pendingJoinCode = code
    pendingUserName = userName
    pendingPhotoUri = photoUri
  }

  private fun setPendingCreateData(houseName: String, userName: String, photoUri: String?) {
    pendingCreateHouseName = houseName
    pendingUserName = userName
    pendingPhotoUri = photoUri
  }

  private fun tryCompletePendingActions() {
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
        viewModelScope.launch { _uiEffect.emit(SetupUiEffect.NavigateToStep(SetupStep.CREATE)) }
        pendingCreateHouseName = null
      }
    }
  }

  fun getPendingStep(): String? {
    return if (pendingCreateHouseName != null && pendingCreateHouseName!!.isEmpty()) STEP_CREATE
    else if (pendingJoinCode != null && (pendingUserName ?: "").isEmpty()) STEP_JOIN
    else null
  }

  private fun clearPendingStep() {
    pendingCreateHouseName = null
    pendingJoinCode = null
  }

  private fun createHousehold(houseName: String, userName: String, photoUri: String?) {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true, loadingMessage = UiText.StringResource(R.string.setup_loading_creating)) }
      try {
        createHouseholdUseCase(houseName, userName, photoUri)
        checkBiometricRequirementAndNavigate()
      } catch (e: Exception) {
        e.printStackTrace()
        _uiState.update { it.copy(isLoading = false) }
        _uiEffect.emit(SetupUiEffect.ShowError(UiText.StringResource(R.string.settings_error_generic)))
      }
    }
  }

  private fun joinHousehold(code: String, userName: String, photoUri: String?) {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true, loadingMessage = UiText.StringResource(R.string.setup_loading_joining)) }
      val success = joinHouseholdUseCase(code, userName, photoUri)
      _uiState.update { it.copy(isLoading = false) }
      if (success) {
        checkBiometricRequirementAndNavigate()
      } else {
        _uiEffect.emit(SetupUiEffect.ShowError(UiText.StringResource(R.string.setup_error_invalid_code)))
      }
    }
  }

  private fun discoverAndJoin(code: String) {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true, loadingMessage = UiText.StringResource(R.string.setup_loading_joining)) }
      try {
        val success = joinHouseholdUseCase.discoverHousehold(code)
        _uiState.update { it.copy(isLoading = false) }
        if (success) {
          _uiEffect.emit(SetupUiEffect.NavigateToStep(SetupStep.SELECT_PROFILE))
        } else {
          _uiEffect.emit(SetupUiEffect.ShowError(UiText.StringResource(R.string.setup_error_invalid_code)))
        }
      } catch (e: Exception) {
        _uiState.update { it.copy(isLoading = false) }
        e.printStackTrace()
        _uiEffect.emit(SetupUiEffect.ShowError(UiText.StringResource(R.string.setup_error_recover_failed)))
      }
    }
  }

  private fun selectMember(member: FamilyMember) {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }
      selectMemberUseCase(member)
      checkBiometricRequirementAndNavigate()
    }
  }

  private fun switchHousehold(householdId: Long) {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true, loadingMessage = UiText.StringResource(R.string.setup_loading_checking_db)) }
      switchHouseholdUseCase(householdId)
      kotlinx.coroutines.delay(1500) // Tiempo extra para que la BD sincronice usuarios remotamente la primera vez
      _uiState.update { it.copy(isLoading = false) }
    }
  }

  private fun logout() {
    viewModelScope.launch {
      logoutUseCase()
      _uiState.update { it.copy(allHouseholds = emptyList(), existingHousehold = null, householdMembers = emptyList()) }
    }
  }

  private fun silentRecoverHouseholds() {
    if (!checkNetworkStatus()) return
    val email = firebaseAuth.currentUser?.email ?: return
    viewModelScope.launch {
      try {
        _uiState.update { it.copy(isCheckingDb = true) }
        val houses = recoverHouseholdsUseCase(email)
        _uiState.update { it.copy(isCheckingDb = false) }

        if (houses.isNotEmpty()) {
          if (houses.size == 1) {
            switchHousehold(houses.first().id)
            _uiEffect.emit(SetupUiEffect.NavigateToStep(SetupStep.SELECT_PROFILE))
          } else {
            _uiEffect.emit(SetupUiEffect.NavigateToStep(SetupStep.SWITCH_HOUSEHOLD))
          }
        }
      } catch (e: Exception) {
        _uiState.update { it.copy(isCheckingDb = false) }
      }
    }
  }

  private fun recoverHouseholdsManual(email: String) {
    if (!checkNetworkStatus()) {
      viewModelScope.launch {
        _uiEffect.emit(SetupUiEffect.ShowError(UiText.StringResource(R.string.setup_error_no_network)))
      }
      return
    }
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true, loadingMessage = UiText.StringResource(R.string.setup_loading_recovering)) }
      try {
        val houses = recoverHouseholdsUseCase(email)
        _uiState.update { it.copy(isLoading = false) }
        if (houses.isEmpty()) {
          _uiEffect.emit(SetupUiEffect.ShowError(UiText.StringResource(R.string.setup_error_no_houses_found)))
        } else {
          if (houses.size == 1) {
            switchHousehold(houses.first().id)
            _uiEffect.emit(SetupUiEffect.NavigateToStep(SetupStep.SELECT_PROFILE))
          } else {
            _uiEffect.emit(SetupUiEffect.NavigateToStep(SetupStep.SWITCH_HOUSEHOLD))
          }
        }
      } catch (e: Exception) {
        _uiState.update { it.copy(isLoading = false) }
        e.printStackTrace()
        _uiEffect.emit(SetupUiEffect.ShowError(UiText.StringResource(R.string.setup_error_recover_failed)))
      }
    }
  }

  private fun checkBiometricRequirementAndNavigate() {
    val biometricSetting = sharedPreferences.getBoolean("biometric_lock_app", false)
    val promptedBefore = sharedPreferences.getBoolean("biometric_prompted_before", false)
    if (!biometricSetting && !promptedBefore) {
      _uiState.update { it.copy(isLoading = false) }
      viewModelScope.launch {
        _uiEffect.emit(SetupUiEffect.NavigateToStep(SetupStep.BIOMETRIC_PROMPT))
      }
    } else {
      _uiState.update { it.copy(isLoading = false) }
      viewModelScope.launch {
        _uiEffect.emit(SetupUiEffect.NavigateToDashboard)
      }
    }
  }

  private fun setupBiometrics(enable: Boolean) {
    sharedPreferences.edit()
      .putBoolean("biometric_lock_app", enable)
      .putBoolean("biometric_prompted_before", true)
      .apply()
    viewModelScope.launch {
      _uiEffect.emit(SetupUiEffect.NavigateToDashboard)
    }
  }

  fun isOnboardingCompleted(): Boolean {
    return sharedPreferences.getBoolean("onboarding_completed", false)
  }

  private fun saveOnboardingCompleted() {
    sharedPreferences.edit().putBoolean("onboarding_completed", true).apply()
    viewModelScope.launch {
      _uiEffect.emit(SetupUiEffect.NavigateToStep(SetupStep.WELCOME))
    }
  }

  enum class SetupStep {
    ONBOARDING, WELCOME, CREATE, JOIN, SELECT_PROFILE, ADD_PROFILE, SWITCH_HOUSEHOLD, BIOMETRIC_PROMPT
  }

  companion object {
    const val STEP_CREATE = "CREATE"
    const val STEP_JOIN = "JOIN"
  }
}

data class SetupUiState(
  val isCheckingDb: Boolean = true,
  val existingHousehold: Household? = null,
  val allHouseholds: List<Household> = emptyList(),
  val householdMembers: List<FamilyMember> = emptyList(),
  val discoveredHousehold: Household? = null,
  val isLoggedIn: Boolean = false,
  val isLoading: Boolean = false,
  val loadingMessage: UiText? = null,
  val userEmail: String? = null,
  val isNetworkAvailable: Boolean = true
)

sealed interface SetupUiEffect {
  object NavigateToDashboard : SetupUiEffect
  data class ShowError(val message: UiText) : SetupUiEffect
  data class NavigateToStep(val step: HouseSetupViewModel.SetupStep) : SetupUiEffect
}

sealed interface SetupIntent {
  data class SearchHousehold(val code: String) : SetupIntent
  data class SetPendingJoin(val code: String, val userName: String, val photoUri: String?) : SetupIntent
  data class SetPendingCreate(val houseName: String, val userName: String, val photoUri: String?) : SetupIntent
  object TryCompletePendingActions : SetupIntent
  object ClearPendingStep : SetupIntent
  data class CreateHousehold(val houseName: String, val userName: String, val photoUri: String?) : SetupIntent
  data class JoinHousehold(val code: String, val userName: String, val photoUri: String?) : SetupIntent
  data class DiscoverAndJoin(val code: String) : SetupIntent
  data class SelectMember(val member: FamilyMember) : SetupIntent
  data class SwitchHousehold(val id: Long) : SetupIntent
  object Logout : SetupIntent
  object SilentRecoverHouseholds : SetupIntent
  data class RecoverHouseholdsManual(val email: String) : SetupIntent
  object CheckNetworkStatus : SetupIntent
  data class SetupBiometrics(val enable: Boolean) : SetupIntent
  object SaveOnboardingCompleted : SetupIntent
}
