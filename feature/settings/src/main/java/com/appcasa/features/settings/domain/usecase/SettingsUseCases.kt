package com.appcasa.features.settings.domain.usecase

import com.appcasa.core.domain.model.FamilyMember
import com.appcasa.core.domain.model.Household
import com.appcasa.core.domain.model.RolHogar
import com.appcasa.core.domain.model.TipoMiembro
import com.appcasa.core.domain.model.User
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.core.domain.repository.FamilyRepository
import com.appcasa.core.domain.repository.HouseholdRepository
import com.appcasa.core.domain.repository.SettingsRepository
import com.appcasa.core.domain.repository.UserRepository
import com.appcasa.core.ui.utils.HouseCodeUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetCurrentHouseholdUseCase @Inject constructor(
    private val repository: HouseholdRepository,
) {
    operator fun invoke(): Flow<Household?> {
        return repository.getHogarActual()
    }
}

class GetHouseholdByIdUseCase @Inject constructor(
    private val repository: HouseholdRepository
) {
    operator fun invoke(id: Long): Flow<Household?> {
        return repository.getHogarById(id)
    }
}

class UpdateUserUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val familyRepository: FamilyRepository
) {
    suspend operator fun invoke(user: User, nombre: String, avatarUrl: String? = null) {
        val updatedUser = user.copy(
            nombre = nombre,
            avatarUrl = avatarUrl ?: user.avatarUrl
        )
        userRepository.insertUser(updatedUser)

        user.miembroId?.let { id ->
            val member = familyRepository.getMemberById(id)
            member?.let {
                familyRepository.updateMember(
                    it.copy(
                        nombre = nombre,
                        fotoUri = avatarUrl ?: it.fotoUri
                    )
                )
            }
        }
    }
}

class RegenerateHouseCodeUseCase @Inject constructor(
    private val repository: HouseholdRepository
) {
    suspend operator fun invoke(hogarId: Long) {
        val newCode = HouseCodeUtils.generateHouseCode()
        repository.updateCodigoHogar(hogarId, newCode)
    }
}

class UpdateHouseholdUseCase @Inject constructor(
    private val repository: HouseholdRepository
) {
    suspend operator fun invoke(hogar: Household, nombre: String) {
        repository.insertHogar(hogar.copy(nombre = nombre))
    }
}

class CreateHouseholdUseCase @Inject constructor(
    private val householdRepository: HouseholdRepository,
    private val userRepository: UserRepository,
    private val familyRepository: FamilyRepository,
    private val householdProvider: CurrentHouseholdProvider,
    private val firebaseMessaging: FirebaseMessaging,
    private val firebaseAuth: FirebaseAuth
) {
    suspend operator fun invoke(houseName: String, userName: String, photoUri: String?) {
        val code = HouseCodeUtils.generateHouseCode()
        val syncId = java.util.UUID.randomUUID().toString()
        val currentUser = firebaseAuth.currentUser
        
        // 1. Insertar Hogar
        val hogarId = householdRepository.insertHogar(
            Household(nombre = houseName, codigoHogar = code, syncId = syncId)
        )

        // 2. Insertar Miembro (ADMIN) vinculado al hogar
        val member = FamilyMember(
            hogarId = hogarId,
            nombre = userName,
            tipo = TipoMiembro.PERSONA,
            rol = RolHogar.ADMIN,
            fotoUri = photoUri,
            firebaseUid = currentUser?.uid,
            email = currentUser?.email
        )
        val miembroId = familyRepository.insertMember(member)
        
        // 3. Sincronización inmediata del primer miembro para desbloquear seguridad
        familyRepository.syncMember(member.copy(id = miembroId))

        // 4. Desactivar usuarios previos e insertar el nuevo Usuario local vinculado
        userRepository.deactivateAllUsers()
        userRepository.insertUser(
            User(
                hogarId = hogarId,
                miembroId = miembroId,
                nombre = userName,
                email = currentUser?.email ?: "admin_${System.currentTimeMillis()}@appcasa.local",
                rol = RolHogar.ADMIN,
                avatarUrl = photoUri,
                isActive = true
            )
        )

        // 4. Suscribir y establecer sesión
        firebaseMessaging.subscribeToTopic("household_$hogarId")
        householdProvider.setHouseholdId(hogarId)
    }
}

class JoinHouseholdUseCase @Inject constructor(
    private val householdRepository: HouseholdRepository,
    private val userRepository: UserRepository,
    private val familyRepository: FamilyRepository,
    private val householdProvider: CurrentHouseholdProvider,
    private val firebaseMessaging: FirebaseMessaging,
    private val firebaseAuth: FirebaseAuth
) {
    suspend operator fun invoke(code: String, userName: String, photoUri: String?): Boolean {
        // ... (el invoke original sigue siendo util para cuando creamos perfil nuevo al unirnos)
        var hogar = householdRepository.getHogarByCodigo(code) ?: householdRepository.findHouseholdRemotely(code)
        if (hogar == null) return false
        
        val localId = householdRepository.insertHogar(hogar)
        val currentUser = firebaseAuth.currentUser
        
        val member = FamilyMember(
            hogarId = localId,
            nombre = userName,
            tipo = TipoMiembro.PERSONA,
            rol = RolHogar.COLABORADOR,
            fotoUri = photoUri,
            firebaseUid = currentUser?.uid,
            email = currentUser?.email
        )
        val miembroId = familyRepository.insertMember(member)

        // Sincronización inmediata del nuevo miembro
        familyRepository.syncMember(member.copy(id = miembroId))

        userRepository.deactivateAllUsers()
        userRepository.insertUser(
            User(
                hogarId = localId,
                miembroId = miembroId,
                nombre = userName,
                email = currentUser?.email ?: "user_${miembroId}@appcasa.local",
                rol = RolHogar.COLABORADOR,
                avatarUrl = photoUri,
                isActive = true
            )
        )

        householdProvider.setHouseholdId(localId)
        return true
    }

    /**
     * Descubre una casa y la descarga localmente con sus miembros, 
     * pero no activa ningun usuario todavía.
     */
    suspend fun discoverHousehold(code: String): Boolean {
        var hogar = householdRepository.getHogarByCodigo(code)
        if (hogar == null) {
            hogar = householdRepository.findHouseholdRemotely(code)
            if (hogar != null) {
                val localId = householdRepository.insertHogar(hogar)
                // Forzamos descarga de miembros
                familyRepository.startRemoteSync(localId)
                kotlinx.coroutines.delay(1000)
                householdProvider.setHouseholdId(localId) // Establecemos ID para que los flows de miembros funcionen
                return true
            }
        } else {
            householdProvider.setHouseholdId(hogar.id)
            return true
        }
        return false
    }

    suspend fun findHousehold(code: String): Household? {
        return householdRepository.getHogarByCodigo(code) ?: householdRepository.findHouseholdRemotely(code)
    }
}

class SelectMemberUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val householdProvider: CurrentHouseholdProvider,
    private val firebaseAuth: FirebaseAuth
) {
    suspend operator fun invoke(member: FamilyMember) {
        // Obtenemos el usuario de Firebase más fresco posible
        val currentUser = firebaseAuth.currentUser
        val emailToUse = currentUser?.email ?: member.email ?: "user_${member.id}@appcasa.local"
        
        userRepository.deactivateAllUsers()
        userRepository.insertUser(
            User(
                hogarId = member.hogarId,
                miembroId = member.id,
                nombre = member.nombre,
                email = emailToUse,
                authId = currentUser?.uid ?: member.firebaseUid, // GUARDAMOS EL ID DE SEGURIDAD
                rol = member.rol,
                avatarUrl = member.fotoUri,
                isActive = true,
                lastSyncedAt = if (currentUser != null || member.firebaseUid != null) System.currentTimeMillis() else null
            )
        )
        householdProvider.setHouseholdId(member.hogarId)
    }
}

class ResetHouseholdUseCase @Inject constructor(
    private val householdRepository: HouseholdRepository,
    private val userRepository: UserRepository,
    private val householdProvider: CurrentHouseholdProvider
) {
    suspend operator fun invoke() {
        householdRepository.deleteAllHogares()
        userRepository.deleteUsers()
        householdProvider.setHouseholdId(0L)
    }
}

class LogoutUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val householdProvider: CurrentHouseholdProvider,
    private val firebaseAuth: FirebaseAuth
) {
    suspend operator fun invoke() {
        // 1. Cerrar sesión en Firebase inmediatamente
        firebaseAuth.signOut()
        
        // 2. Borramos los usuarios locales para que no haya nada que re-activar automáticamente
        userRepository.deleteUsers()
        
        // 3. Resetear el estado de la sesión activa
        householdProvider.setHouseholdId(0L)

        // Delay técnico para asegurar que los listeners de Auth reaccionen antes que la UI
        kotlinx.coroutines.delay(300)
    }
}

class GetAllHouseholdsUseCase @Inject constructor(
    private val repository: HouseholdRepository
) {
    operator fun invoke(): Flow<List<Household>> {
        return repository.getAllHogares()
    }
}

class SwitchHouseholdUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val householdProvider: CurrentHouseholdProvider,
    private val firebaseMessaging: FirebaseMessaging
) {
    suspend operator fun invoke(householdId: Long) {
        // Al cambiar de hogar, desactivamos cualquier sesión anterior
        userRepository.deactivateAllUsers()
        
        // Establecemos el ID del hogar seleccionado
        householdProvider.setHouseholdId(householdId)
        
        // Nos suscribimos a las notificaciones de ese hogar
        firebaseMessaging.subscribeToTopic("household_$householdId")
        
        // IMPORTANTE: NO activamos ningún usuario aquí. 
        // La activación real ocurrirá en SelectMemberUseCase cuando el usuario elija su perfil.
    }
}

class ForceSyncUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(hogarId: Long) {
        repository.triggerManualSync(hogarId)
    }
}

class ExportHouseholdDataUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(hogarId: Long): String {
        return repository.exportData(hogarId)
    }
}

class RecoverHouseholdsUseCase @Inject constructor(
    private val repository: HouseholdRepository,
    private val familyRepository: FamilyRepository,
    private val firebaseAuth: FirebaseAuth
) {
    suspend operator fun invoke(email: String): List<Household> {
        val currentUser = firebaseAuth.currentUser
        val cloudHouses = if (currentUser != null) {
            repository.findHouseholdsByUserUid(currentUser.uid)
        } else {
            repository.findHouseholdsByUserEmail(email)
        }

        // Guardamos localmente para que aparezcan en la lista de Switch
        for (house in cloudHouses) {
            val localId = repository.insertHogar(house)
            // IMPORTANTE: Descargamos también los miembros para que la pantalla "¿Quién eres?" funcione
            familyRepository.startRemoteSync(localId)
        }

        // Delay técnico para que Room consolide las transacciones antes de que la UI refresque
        kotlinx.coroutines.delay(600)

        return cloudHouses
    }
}

class LinkAccountUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val familyRepository: FamilyRepository,
    private val firebaseAuth: FirebaseAuth,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke() {
        val currentUser = firebaseAuth.currentUser ?: return
        val user = userRepository.getCurrentUser().first() ?: return
        
        // Evitamos vincular si el usuario Room es "synthetic" (ID -1) o inválido (ID 0)
        if (user.id <= 0L || user.hogarId <= 0L) return
        
        // 1. Vincular el miembro local con el UID de Firebase
        user.miembroId?.let { memberId ->
            if (memberId > 0L) {
                val member = familyRepository.getMemberById(memberId)
                member?.let {
                    familyRepository.updateMember(it.copy(
                        firebaseUid = currentUser.uid,
                        email = currentUser.email
                    ))
                }
            }
        }
        
        // 2. Actualizar el usuario local Room
        userRepository.insertUser(user.copy(
            email = currentUser.email ?: user.email,
            authId = currentUser.uid, // ASEGURAMOS EL ID DE SEGURIDAD
            lastSyncedAt = System.currentTimeMillis()
        ))
        
        // 3. Forzar sincronización inmediata para reclamar la casa en la nube
        settingsRepository.triggerManualSync(user.hogarId)
    }
}
