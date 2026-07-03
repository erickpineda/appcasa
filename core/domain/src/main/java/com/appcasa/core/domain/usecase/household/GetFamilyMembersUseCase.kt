package com.appcasa.core.domain.usecase.household

import com.appcasa.core.domain.model.FamilyMember
import com.appcasa.core.domain.repository.FamilyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFamilyMembersUseCase @Inject constructor(
    private val repository: FamilyRepository
) {
    operator fun invoke(hogarId: String): Flow<List<FamilyMember>> {
        return repository.getMembersByHogar(hogarId)
    }
}
