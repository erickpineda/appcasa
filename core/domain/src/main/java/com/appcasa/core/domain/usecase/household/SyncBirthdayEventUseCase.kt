package com.appcasa.core.domain.usecase.household

import com.appcasa.core.domain.model.Event
import com.appcasa.core.domain.model.TipoEvento
import com.appcasa.core.domain.repository.CalendarRepository
import com.appcasa.core.domain.repository.FamilyRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SyncBirthdayEventUseCase @Inject constructor(
    private val familyRepository: FamilyRepository,
    private val calendarRepository: CalendarRepository
) {
    suspend operator fun invoke(memberId: String) {
        val member = familyRepository.getMemberById(memberId) ?: return
        if (member.fechaNacimiento != null) {
            val existing = calendarRepository.getEventsByHogar(member.hogarId)
                .first().any { it.tipo == TipoEvento.CUMPLEANOS && it.titulo.contains(member.nombre) }
            
            if (!existing) {
                calendarRepository.upsertEvent(
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
}
