package com.appcasa.features.family.domain.usecase

import com.appcasa.core.domain.model.*
import com.appcasa.core.domain.repository.*
import com.appcasa.core.domain.usecase.household.SyncBirthdayEventUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetPeopleUseCase @Inject constructor(
    private val repository: FamilyRepository
) {
    operator fun invoke(hogarId: Long): Flow<List<FamilyMember>> {
        return repository.getMembersByHogar(hogarId).map { list ->
            list.filter { it.tipo == TipoMiembro.PERSONA }
        }
    }
}

class GetPetsUseCase @Inject constructor(
    private val repository: FamilyRepository
) {
    operator fun invoke(hogarId: Long): Flow<List<FamilyMember>> {
        return repository.getMembersByHogar(hogarId).map { list ->
            list.filter { it.tipo != TipoMiembro.PERSONA }
        }
    }
}

class AddMemberUseCase @Inject constructor(
    private val familyRepository: FamilyRepository,
    private val syncBirthdayEventUseCase: SyncBirthdayEventUseCase
) {
    suspend operator fun invoke(member: FamilyMember) {
        val id = familyRepository.insertMember(member)
        if (member.fechaNacimiento != null) {
            syncBirthdayEventUseCase(id)
        }
    }
}

class DeleteMemberUseCase @Inject constructor(
    private val familyRepository: FamilyRepository
) {
    suspend operator fun invoke(member: FamilyMember) {
        familyRepository.deleteMember(member)
    }
}

class GetPetDataSummaryUseCase @Inject constructor() {
    operator fun invoke(miembros: List<FamilyMember>): PetSummary {
        val mascotas = miembros.filter { it.tipo != TipoMiembro.PERSONA }
        val typeCounts = mascotas.groupBy { it.tipo }.mapValues { it.value.size }
        return PetSummary(mascotas.size, typeCounts)
    }
}
