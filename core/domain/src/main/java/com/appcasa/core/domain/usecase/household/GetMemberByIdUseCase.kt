package com.appcasa.core.domain.usecase.household

import com.appcasa.core.domain.model.FamilyMember
import com.appcasa.core.domain.repository.FamilyRepository
import javax.inject.Inject

class GetMemberByIdUseCase @Inject constructor(
    private val repository: FamilyRepository
) {
    suspend operator fun invoke(id: String): FamilyMember? {
        return repository.getMemberById(id)
    }
}
