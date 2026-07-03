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
import java.util.UUID
import javax.inject.Inject

class GetCurrentHouseholdUseCase @Inject constructor(
  private val repository: HouseholdRepository
) {
  operator fun invoke(): Flow<Household?> {
    return repository.getHogarActual()
  }
}

class GetHouseholdByIdUseCase @Inject constructor(
  private val repository: HouseholdRepository
) {
  operator fun invoke(id: String): Flow<Household?> {
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
            avatarUrl = avatarUrl ?: it.avatarUrl
          )
        )
      }
    }
  }
}

class RegenerateHouseCodeUseCase @Inject constructor(
  private val repository: HouseholdRepository
) {
  suspend operator fun invoke(hogarId: String) {
    val newCode = HouseCodeUtils.generateHouseCode()
    repository.updateCodigoHogar(hogarId, newCode)
  }
}

class UpdateHouseholdUseCase @Inject constructor(
  private val repository: HouseholdRepository
) {
  suspend operator fun invoke(hogar: Household, nombre: String) {
    repository.insertHogar(hogar.copy(nombre = nombre, updatedAt = System.currentTimeMillis()))
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
  suspend operator fun invoke(houseName: String, userName: String, photoUri: String?): Boolean {
    val existingUser = userRepository.getUserByName(userName)
    if (existingUser != null) {
        return false // Nombre de usuario ya existe
    }

    val code = HouseCodeUtils.generateHouseCode()
    val currentUser = firebaseAuth.currentUser
        
    // 1. Insertar Hogar
    val hogarId = householdRepository.insertHogar(
      Household(nombre = houseName, codigoHogar = code)
    )

    // 2. Insertar Miembro (ADMIN) - Comprobamos si ya existe por firebaseUid por si acaso
    val existingMembers = familyRepository.getMembersByHogar(hogarId).first()
    val alreadyExists = existingMembers.find { it.firebaseUid == currentUser?.uid }
    
    val miembroId = alreadyExists?.id ?: currentUser?.uid ?: UUID.randomUUID().toString()
    
    val memberToInsert = FamilyMember(
      id = miembroId,
      hogarId = hogarId,
      nombre = userName,
      tipo = TipoMiembro.PERSONA,
      rol = RolHogar.ADMIN,
      avatarUrl = photoUri,
      firebaseUid = currentUser?.uid,
      email = currentUser?.email
    )
    familyRepository.upsertMember(memberToInsert)
    familyRepository.syncMember(memberToInsert)

    // 3. Desactivar usuarios previos e insertar el nuevo Usuario local vinculado
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

    firebaseMessaging.subscribeToTopic("household_$hogarId")
    householdProvider.setHouseholdId(hogarId)
    return true
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
    val hogar = householdRepository.getHogarByCodigo(code) ?: householdRepository.findHouseholdRemotely(code)
    if (hogar == null) return false
        
    val localId = householdRepository.insertHogar(hogar)
    val currentUser = firebaseAuth.currentUser

    // 1. Comprobar si este usuario YA es miembro (por firebaseUid)
    val existingMembers = familyRepository.getMembersByHogar(localId).first()
    val memberByUid = existingMembers.find { it.firebaseUid == currentUser?.uid }
    
    // 2. Comprobar si el NOMBRE ya está en uso por OTRA persona
    if (memberByUid == null && existingMembers.any { it.nombre.equals(userName, ignoreCase = true) }) {
      return false // Nombre duplicado
    }
        
    val miembroId = memberByUid?.id ?: currentUser?.uid ?: UUID.randomUUID().toString()
    val memberToInsert = FamilyMember(
      id = miembroId,
      hogarId = localId,
      nombre = userName,
      tipo = TipoMiembro.PERSONA,
      rol = RolHogar.COLABORADOR,
      avatarUrl = photoUri,
      firebaseUid = currentUser?.uid,
      email = currentUser?.email
    )
    familyRepository.upsertMember(memberToInsert)
    familyRepository.syncMember(memberToInsert)

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
    firebaseMessaging.subscribeToTopic("household_$localId")
    return true
  }

  suspend fun discoverHousehold(code: String): Boolean {
    val hogar = householdRepository.getHogarByCodigo(code)
    if (hogar == null) {
      val remoteHogar = householdRepository.findHouseholdRemotely(code)
      if (remoteHogar != null) {
        val localId = householdRepository.insertHogar(remoteHogar)
        familyRepository.startRemoteSync(localId)
        kotlinx.coroutines.delay(1000)
        householdProvider.setHouseholdId(localId) 
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
    val currentUser = firebaseAuth.currentUser
    val emailToUse = currentUser?.email ?: member.email ?: "user_${member.id}@appcasa.local"
        
    userRepository.deactivateAllUsers()
    userRepository.insertUser(
      User(
        hogarId = member.hogarId,
        miembroId = member.id,
        nombre = member.nombre,
        email = emailToUse,
        authId = currentUser?.uid ?: member.firebaseUid, 
        rol = member.rol,
        avatarUrl = member.avatarUrl,
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
    householdProvider.setHouseholdId("")
  }
}

class SwitchProfileUseCase @Inject constructor(
  private val userRepository: UserRepository,
  private val householdProvider: CurrentHouseholdProvider
) {
  suspend operator fun invoke() {
    userRepository.deleteUsers()
    householdProvider.setHouseholdId("")
  }
}

class LogoutUseCase @Inject constructor(
  private val userRepository: UserRepository,
  private val householdProvider: CurrentHouseholdProvider,
  private val firebaseAuth: FirebaseAuth
) {
  suspend operator fun invoke() {
    firebaseAuth.signOut()
    userRepository.deleteUsers()
    householdProvider.setHouseholdId("")
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
  suspend operator fun invoke(householdId: String) {
    userRepository.deactivateAllUsers()
    householdProvider.setHouseholdId(householdId)
    firebaseMessaging.subscribeToTopic("household_$householdId")
  }
}

class ForceSyncUseCase @Inject constructor(
  private val repository: SettingsRepository
) {
  suspend operator fun invoke(hogarId: String) {
    repository.triggerManualSync(hogarId)
  }
}

class ExportHouseholdDataUseCase @Inject constructor(
  private val repository: SettingsRepository
) {
  suspend operator fun invoke(hogarId: String): String {
    return repository.exportData(hogarId)
  }
}

class GetBiometricStatusUseCase @Inject constructor(
  private val repository: SettingsRepository
) {
  operator fun invoke(): Boolean {
    return repository.isBiometricLockEnabled()
  }
}

class SetBiometricStatusUseCase @Inject constructor(
  private val repository: SettingsRepository
) {
  operator fun invoke(enabled: Boolean) {
    repository.setBiometricLockEnabled(enabled)
  }
}

class GetBiometricPromptedUseCase @Inject constructor(
  private val repository: SettingsRepository
) {
  operator fun invoke(): Boolean {
    return repository.isBiometricPromptedBefore()
  }
}

class SetBiometricPromptedUseCase @Inject constructor(
  private val repository: SettingsRepository
) {
  operator fun invoke(prompted: Boolean) {
    repository.setBiometricPromptedBefore(prompted)
  }
}

class GetOnboardingStatusUseCase @Inject constructor(
  private val repository: SettingsRepository
) {
  operator fun invoke(): Boolean {
    return repository.isOnboardingCompleted()
  }
}

class SetOnboardingStatusUseCase @Inject constructor(
  private val repository: SettingsRepository
) {
  operator fun invoke(completed: Boolean) {
    repository.setOnboardingCompleted(completed)
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

    val cloudHouseIds = cloudHouses.map { it.id }.toSet()
    val localHouseholds = repository.getAllHogares().first()
    for (localHouse in localHouseholds) {
      if (localHouse.lastSyncedAt != null && !cloudHouseIds.contains(localHouse.id)) {
        repository.deleteHogar(localHouse.id)
      }
    }

    val recovered = mutableListOf<Household>()
    for (house in cloudHouses) {
      val localId = repository.insertHogar(house)
      familyRepository.startRemoteSync(localId)
      recovered.add(house.copy(id = localId))
    }

    return recovered
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
        
    if (user.id == "volatile_id" || user.hogarId.isEmpty()) return
        
    user.miembroId?.let { memberId ->
      if (memberId.isNotEmpty()) {
        val member = familyRepository.getMemberById(memberId)
        member?.let {
          familyRepository.updateMember(it.copy(
            firebaseUid = currentUser.uid,
            email = currentUser.email
          ))
        }
      }
    }
        
    userRepository.insertUser(user.copy(
      email = currentUser.email ?: user.email,
      authId = currentUser.uid,
      lastSyncedAt = System.currentTimeMillis()
    ))
        
    settingsRepository.triggerManualSync(user.hogarId)
  }
}
