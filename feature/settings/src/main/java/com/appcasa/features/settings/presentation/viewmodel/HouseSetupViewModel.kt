package com.appcasa.features.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.EstadoGeneral
import com.appcasa.core.domain.model.RolHogar
import com.appcasa.core.domain.model.TipoMiembro
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.core.ui.utils.HouseCodeUtils
import com.appcasa.features.family.data.local.MiembroDao
import com.appcasa.features.family.data.local.MiembroEntity
import com.appcasa.features.settings.data.local.ConfiguracionDao
import com.appcasa.features.settings.data.local.HogarEntity
import com.appcasa.features.settings.data.local.UsuarioEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HouseSetupViewModel @Inject constructor(
    private val configuracionDao: ConfiguracionDao,
    private val miembroDao: MiembroDao,
    private val householdProvider: CurrentHouseholdProvider
) : ViewModel() {

    private val _setupEvent = MutableSharedFlow<SetupResult>(replay = 0)
    val setupEvent = _setupEvent.asSharedFlow()

    val existingHousehold = configuracionDao.getHogarActual()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val householdMembers = existingHousehold.flatMapLatest { hogar ->
        hogar?.let { miembroDao.getMiembrosByHogar(it.id) } ?: flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createHousehold(houseName: String, userName: String, photoUri: String?) {
        viewModelScope.launch {
            val code = HouseCodeUtils.generateHouseCode()
            
            // 1. Crear Hogar
            val hogar = HogarEntity(
                nombre = houseName,
                codigoHogar = code,
                estado = EstadoGeneral.ACTIVO.name
            )
            val hogarId = configuracionDao.insertHogar(hogar)
            
            // 2. Crear Miembro (para XP y Ranking)
            miembroDao.insertMiembro(
                MiembroEntity(
                    hogarId = hogarId,
                    nombre = userName,
                    tipo = TipoMiembro.PERSONA.name,
                    fotoUri = photoUri
                )
            )

            // 3. Crear Usuario (Perfil de la app) y activarlo
            configuracionDao.deactivateAllUsers()
            configuracionDao.insertUsuario(
                UsuarioEntity(
                    hogarId = hogarId,
                    nombre = userName,
                    email = "usuario@appcasa.local",
                    rol = RolHogar.ADMIN.name,
                    avatarUrl = photoUri,
                    isActive = true
                )
            )

            householdProvider.setHouseholdId(hogarId)
            _setupEvent.emit(SetupResult.Success)
        }
    }

    fun joinHousehold(code: String, userName: String, photoUri: String?) {
        viewModelScope.launch {
            // TODO: Integración real con la nube para descargar datos del hogar usando 'code'
            val dummyHogarId = existingHousehold.value?.id ?: 1L
            
            configuracionDao.deactivateAllUsers()
            configuracionDao.insertUsuario(
                UsuarioEntity(
                    hogarId = dummyHogarId,
                    nombre = userName,
                    email = "colaborador@appcasa.local",
                    rol = RolHogar.COLABORADOR.name,
                    avatarUrl = photoUri,
                    isActive = true
                )
            )
            
            miembroDao.insertMiembro(
                MiembroEntity(
                    hogarId = dummyHogarId,
                    nombre = userName,
                    tipo = TipoMiembro.PERSONA.name,
                    fotoUri = photoUri
                )
            )

            householdProvider.setHouseholdId(dummyHogarId)
            _setupEvent.emit(SetupResult.Success)
        }
    }

    fun selectMember(member: MiembroEntity) {
        viewModelScope.launch {
            configuracionDao.deactivateAllUsers()
            configuracionDao.insertUsuario(
                UsuarioEntity(
                    hogarId = member.hogarId,
                    nombre = member.nombre,
                    email = "usuario@appcasa.local",
                    rol = RolHogar.COLABORADOR.name,
                    avatarUrl = member.fotoUri,
                    isActive = true
                )
            )
            householdProvider.setHouseholdId(member.hogarId)
            _setupEvent.emit(SetupResult.Success)
        }
    }

    fun resetHousehold() {
        viewModelScope.launch {
            configuracionDao.deleteAllHogares()
            configuracionDao.deleteAllUsuarios()
            householdProvider.setHouseholdId(0L)
        }
    }

    sealed class SetupResult {
        data object Success : SetupResult()
        data class Error(val message: String) : SetupResult()
    }
}
