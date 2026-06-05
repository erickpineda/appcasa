package com.appcasa.core.domain.usecase

import com.appcasa.core.domain.model.*
import com.appcasa.core.domain.repository.*
import javax.inject.Inject

class GetMemberByIdUseCase @Inject constructor(
    private val repository: FamilyRepository
) {
    suspend operator fun invoke(id: Long): FamilyMember? {
        return repository.getMemberById(id)
    }
}

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

class SyncBirthdayEventUseCase @Inject constructor(
    private val familyRepository: FamilyRepository,
    private val calendarRepository: CalendarRepository
) {
    suspend operator fun invoke(memberId: Long) {
        val member = familyRepository.getMemberById(memberId)
        if (member?.fechaNacimiento != null) {
            calendarRepository.insertEvent(
                Event(
                    hogarId = member.hogarId,
                    titulo = "Cumpleaños: ${member.nombre} 🎂",
                    fecha = member.fechaNacimiento,
                    tipo = TipoEvento.CUMPLEANOS
                )
            )
        }
    }
}
