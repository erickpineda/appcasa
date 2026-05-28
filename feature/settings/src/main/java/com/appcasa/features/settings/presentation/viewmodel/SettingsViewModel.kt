package com.appcasa.features.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcasa.features.settings.data.local.ConfiguracionDao
import com.appcasa.features.settings.data.local.ConfiguracionEntity
import com.appcasa.features.settings.data.local.HogarEntity
import com.appcasa.features.settings.data.local.UsuarioEntity
import com.appcasa.features.lists.data.local.ListaDao
import com.appcasa.features.lists.data.local.ListaEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val configuracionDao: ConfiguracionDao,
    private val listaDao: ListaDao
) : ViewModel() {

    val hogarActual: StateFlow<HogarEntity?> = configuracionDao.getHogarActual()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val usuarioActual: StateFlow<UsuarioEntity?> = configuracionDao.getUsuarioActual()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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

    fun updateUsuario(nombre: String) {
        viewModelScope.launch {
            val usuario = usuarioActual.value ?: return@launch
            configuracionDao.insertUsuario(usuario.copy(nombre = nombre))
        }
    }

    fun updateHogar(nombre: String) {
        viewModelScope.launch {
            val hogar = hogarActual.value ?: return@launch
            configuracionDao.insertHogar(hogar.copy(nombre = nombre))
        }
    }
}
