package com.appcasa.core.domain.usecase.household

import com.appcasa.core.domain.model.FamilyMember
import com.appcasa.core.domain.repository.FamilyRepository
import javax.inject.Inject

class UpdateMemberUseCase @Inject constructor(
    private val familyRepository: FamilyRepository,
    private val syncBirthdayEventUseCase: SyncBirthdayEventUseCase
) {
    suspend operator fun invoke(member: FamilyMember) {
        familyRepository.updateMember(member)
        if (member.fechaNacimiento != null) {
            syncBirthdayEventUseCase(member.id)
        }
    }
}
