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
            
            // 2. Crear Miembro con Rol ADMIN
            val miembroId = miembroDao.insertMiembro(
                MiembroEntity(
                    hogarId = hogarId,
                    nombre = userName,
                    tipo = TipoMiembro.PERSONA.name,
                    rol = RolHogar.ADMIN.name,
                    fotoUri = photoUri
                )
            )

            // 3. Crear Usuario y activarlo
            configuracionDao.deactivateAllUsers()
            configuracionDao.insertUsuario(
                UsuarioEntity(
                    hogarId = hogarId,
                    miembroId = miembroId,
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
            val existing = existingHousehold.value
            val hogarId = existing?.id ?: 1L
            
            // 1. Crear Miembro con Rol COLABORADOR
            val miembroId = miembroDao.insertMiembro(
                MiembroEntity(
                    hogarId = hogarId,
                    nombre = userName,
                    tipo = TipoMiembro.PERSONA.name,
                    rol = RolHogar.COLABORADOR.name,
                    fotoUri = photoUri
                )
            )

            // 2. Crear Usuario
            configuracionDao.deactivateAllUsers()
            configuracionDao.insertUsuario(
                UsuarioEntity(
                    hogarId = hogarId,
                    miembroId = miembroId,
                    nombre = userName,
                    email = "colaborador@appcasa.local",
                    rol = RolHogar.COLABORADOR.name,
                    avatarUrl = photoUri,
                    isActive = true
                )
            )

            householdProvider.setHouseholdId(hogarId)
            _setupEvent.emit(SetupResult.Success)
        }
    }

    fun selectMember(member: MiembroEntity) {
        viewModelScope.launch {
            configuracionDao.deactivateAllUsers()
            configuracionDao.insertUsuario(
                UsuarioEntity(
                    hogarId = member.hogarId,
                    miembroId = member.id,
                    nombre = member.nombre,
                    email = "usuario@appcasa.local",
                    rol = member.rol, // ¡IMPORTANTE! Recuperar el rol real del miembro
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
