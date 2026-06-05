package com.appcasa.features.settings.domain.usecase

import com.appcasa.core.domain.model.FamilyMember
import com.appcasa.core.domain.model.Household
import com.appcasa.core.domain.model.RolHogar
import com.appcasa.core.domain.model.TipoMiembro
import com.appcasa.core.domain.model.User
import com.appcasa.core.domain.providers.CurrentHouseholdProvider
import com.appcasa.core.domain.repository.FamilyRepository
import com.appcasa.core.domain.repository.HouseholdRepository
import com.appcasa.core.domain.repository.UserRepository
import com.appcasa.core.ui.utils.HouseCodeUtils
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentHouseholdUseCase @Inject constructor(
    private val repository: HouseholdRepository,
) {
    operator fun invoke(): Flow<Household?> {
        return repository.getHogarActual()
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
    private val householdProvider: CurrentHouseholdProvider
) {
    suspend operator fun invoke(houseName: String, userName: String, photoUri: String?) {
        val code = HouseCodeUtils.generateHouseCode()
        
        val hogarId = householdRepository.insertHogar(
            Household(
                nombre = houseName,
                codigoHogar = code
            )
        )
        
        val miembroId = familyRepository.insertMember(
            FamilyMember(
                hogarId = hogarId,
                nombre = userName,
                tipo = TipoMiembro.PERSONA,
                rol = RolHogar.ADMIN,
                fotoUri = photoUri
            )
        )

        userRepository.deactivateAllUsers()
        userRepository.insertUser(
            User(
                hogarId = hogarId,
                miembroId = miembroId,
                nombre = userName,
                email = "admin_${System.currentTimeMillis()}@appcasa.local",
                rol = RolHogar.ADMIN,
                avatarUrl = photoUri,
                isActive = true
            )
        )

        householdProvider.setHouseholdId(hogarId)
    }
}

class JoinHouseholdUseCase @Inject constructor(
    private val householdRepository: HouseholdRepository,
    private val userRepository: UserRepository,
    private val familyRepository: FamilyRepository,
    private val householdProvider: CurrentHouseholdProvider
) {
    suspend operator fun invoke(code: String, userName: String, photoUri: String?): Boolean {
        val hogar = householdRepository.getHogarByCodigo(code) ?: return false
        val hogarId = hogar.id
        
        val miembroId = familyRepository.insertMember(
            FamilyMember(
                hogarId = hogarId,
                nombre = userName,
                tipo = TipoMiembro.PERSONA,
                rol = RolHogar.COLABORADOR,
                fotoUri = photoUri
            )
        )

        userRepository.deactivateAllUsers()
        userRepository.insertUser(
            User(
                hogarId = hogarId,
                miembroId = miembroId,
                nombre = userName,
                email = "colab_${System.currentTimeMillis()}@appcasa.local",
                rol = RolHogar.COLABORADOR,
                avatarUrl = photoUri,
                isActive = true
            )
        )

        householdProvider.setHouseholdId(hogarId)
        return true
    }
}

class SelectMemberUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val householdProvider: CurrentHouseholdProvider
) {
    suspend operator fun invoke(member: FamilyMember) {
        userRepository.deactivateAllUsers()
        userRepository.insertUser(
            User(
                hogarId = member.hogarId,
                miembroId = member.id,
                nombre = member.nombre,
                email = "user_${member.id}@appcasa.local",
                rol = member.rol,
                avatarUrl = member.fotoUri,
                isActive = true
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
    private val householdProvider: CurrentHouseholdProvider
) {
    suspend operator fun invoke() {
        userRepository.deactivateAllUsers()
        householdProvider.setHouseholdId(0L)
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
    private val householdProvider: CurrentHouseholdProvider
) {
    suspend operator fun invoke(householdId: Long) {
        userRepository.deactivateAllUsers()
        userRepository.activateUserByHousehold(householdId)
        householdProvider.setHouseholdId(householdId)
    }
}
