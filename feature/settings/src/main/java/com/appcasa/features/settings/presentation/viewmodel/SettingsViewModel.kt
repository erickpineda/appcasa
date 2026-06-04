package com.appcasa.features.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.core.domain.model.RolHogar
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.core.ui.utils.HouseCodeUtils
import com.appcasa.features.family.data.local.MiembroDao
import com.appcasa.features.lists.data.local.ListaDao
import com.appcasa.features.lists.data.local.ListaEntity
import com.appcasa.features.settings.data.local.ConfiguracionDao
import com.appcasa.features.settings.data.local.ConfiguracionEntity
import com.appcasa.features.settings.data.local.HogarEntity
import com.appcasa.features.settings.data.local.UsuarioEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val configuracionDao: ConfiguracionDao,
    private val listaDao: ListaDao,
    private val miembroDao: MiembroDao,
    private val householdProvider: CurrentHouseholdProvider
) : ViewModel() {

    val hogarActual: StateFlow<HogarEntity?> = configuracionDao.getHogarActual()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val usuarioActual: StateFlow<UsuarioEntity?> = configuracionDao.getUsuarioActual()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isAdmin: StateFlow<Boolean> = usuarioActual.map { it?.rol == RolHogar.ADMIN.name }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val todasLasListas: StateFlow<List<ListaEntity>> = hogarActual.flatMapLatest { hogar ->
        hogar?.let { listaDao.getListasByHogar(it.id) } ?: flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _configuraciones = MutableStateFlow<Map<String, String>>(emptyMap())
    val configuraciones: StateFlow<Map<String, String>> = _configuraciones.asStateFlow()

    init {
        viewModelScope.launch {
            hogarActual.collect { hogar ->
                hogar?.let {
                    configuracionDao.getConfiguracion(it.id).collect { configs ->
                        _configuraciones.value = configs.associate { it.clave to it.valor }
                    }
                }
            }
        }
    }

    fun updateConfig(clave: String, valor: String) {
        viewModelScope.launch {
            val hogar = hogarActual.value ?: return@launch
            configuracionDao.insertConfiguracion(
                ConfiguracionEntity(
                    hogarId = hogar.id,
                    clave = clave,
                    valor = valor
                )
            )
        }
    }

    fun updateUsuario(nombre: String, avatarUrl: String? = null) {
        viewModelScope.launch {
            val usuario = usuarioActual.value ?: return@launch
            
            // 1. Actualizar Usuario (Perfil)
            configuracionDao.insertUsuario(
                usuario.copy(
                    nombre = nombre,
                    avatarUrl = avatarUrl ?: usuario.avatarUrl
                )
            )

            // 2. Sincronizar con Miembro si existe el vínculo
            usuario.miembroId?.let { id ->
                val miembro = miembroDao.getMiembroById(id)
                miembro?.let {
                    miembroDao.updateMiembro(it.copy(
                        nombre = nombre,
                        fotoUri = avatarUrl ?: it.fotoUri
                    ))
                }
            }
        }
    }

    fun regenerateHouseCode() {
        viewModelScope.launch {
            val hogar = hogarActual.value ?: return@launch
            if (isAdmin.value) {
                val newCode = HouseCodeUtils.generateHouseCode()
                configuracionDao.updateCodigoHogar(hogar.id, newCode)
                // TODO: Enviar notificación a otros miembros vía Firebase/WorkManager
            }
        }
    }

    fun updateHogar(nombre: String) {
        viewModelScope.launch {
            val hogar = hogarActual.value ?: return@launch
            configuracionDao.insertHogar(hogar.copy(nombre = nombre))
        }
    }

    fun logout() {
        viewModelScope.launch {
            // 1. Desactivar usuario actual
            configuracionDao.deactivateAllUsers()
            
            // 2. Limpiar ID del hogar en el provider reactivo para forzar re-evaluación
            householdProvider.setHouseholdId(0L)
            
            // Nota: No borramos el hogar ni el usuario de la DB, solo los desactivamos
            // para que aparezca la pantalla de "Seleccionar Perfil".
        }
    }
}
