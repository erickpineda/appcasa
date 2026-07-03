package com.appcasa.core.domain.usecase.household

import com.appcasa.core.domain.model.FamilyMember
import com.appcasa.core.domain.repository.FamilyRepository
import com.appcasa.core.domain.repository.UserRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class UpdateMemberUseCase @Inject constructor(
  private val familyRepository: FamilyRepository,
  private val userRepository: UserRepository,
  private val syncBirthdayEventUseCase: SyncBirthdayEventUseCase,
) {
  suspend operator fun invoke(member: FamilyMember) {
    familyRepository.updateMember(member)
    if (member.fechaNacimiento != null) {
      syncBirthdayEventUseCase(member.id)
    }

    // Si el miembro actualizado es el usuario actual, también actualizamos su perfil de usuario
    val currentUser = userRepository.getCurrentUser().firstOrNull()
    if (currentUser?.miembroId == member.id) {
      userRepository.insertUser(
        currentUser.copy(
          nombre = member.nombre,
          avatarUrl = member.avatarUrl
        )
      )
    }
  }
}
