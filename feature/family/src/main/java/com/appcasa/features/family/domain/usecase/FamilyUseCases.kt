package com.appcasa.features.family.domain.usecase

import com.appcasa.core.domain.model.*
import com.appcasa.core.domain.repository.*
import com.appcasa.core.domain.usecase.household.SyncBirthdayEventUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetPeopleUseCase @Inject constructor(
    private val repository: FamilyRepository
) {
    operator fun invoke(hogarId: String): Flow<List<FamilyMember>> {
        return repository.getMembersByHogar(hogarId).map { list ->
            list.filter { it.tipo == TipoMiembro.PERSONA }
        }
    }
}

class GetPetsUseCase @Inject constructor(
    private val repository: FamilyRepository
) {
    operator fun invoke(hogarId: String): Flow<List<FamilyMember>> {
        return repository.getMembersByHogar(hogarId).map { list ->
            list.filter { it.tipo != TipoMiembro.PERSONA }
        }
    }
}

class AddMemberUseCase @Inject constructor(
    private val familyRepository: FamilyRepository,
    private val syncBirthdayEventUseCase: SyncBirthdayEventUseCase
) {
    suspend operator fun invoke(member: FamilyMember): Boolean {
        // Validación de unicidad de nombre en el hogar
        val existing = familyRepository.getMembersByHogar(member.hogarId).first()
        if (existing.any { it.nombre.equals(member.nombre, ignoreCase = true) && it.id != member.id }) {
            return false
        }
        
        familyRepository.upsertMember(member)
        if (member.fechaNacimiento != null) {
            syncBirthdayEventUseCase(member.id)
        }
        return true
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
