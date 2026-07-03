package com.appcasa.features.tasks.domain.usecase

import com.appcasa.core.domain.model.Prioridad
import com.appcasa.core.domain.model.Task
import com.appcasa.core.domain.model.TipoMiembro
import com.appcasa.core.domain.repository.FamilyRepository
import com.appcasa.core.domain.repository.TasksRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AwardTaskPointsUseCase @Inject constructor(
  private val tasksRepository: TasksRepository,
  private val familyRepository: FamilyRepository
) {
  suspend operator fun invoke(task: Task): Int {
    if (task.puntosOtorgados || task.esPersonal) return 0

    val points = when (task.prioridad) {
      Prioridad.ALTA -> 20
      Prioridad.BAJA -> 5
      else -> 10
    }

    val memberId = tasksRepository.getAssignmentsForTask(task.id).first().firstOrNull()?.miembroId
        
    val finalMemberId = memberId ?: findDefaultMember(task.hogarId)

    finalMemberId?.let { id ->
      val member = familyRepository.getMemberById(id)
      member?.let { m ->
        val newPoints = m.puntos + points
        val newLevel = (newPoints / 100) + 1
        familyRepository.updateMember(m.copy(
          puntos = newPoints,
          nivel = newLevel,
          updatedAt = System.currentTimeMillis()
        ))
      }
    }
        
    tasksRepository.upsertTask(task.copy(puntosOtorgados = true))
    return points
  }

  private suspend fun findDefaultMember(hogarId: String): String? {
    val members = familyRepository.getMembersByHogar(hogarId).first()
    return members.find { it.tipo == TipoMiembro.PERSONA }?.id
  }
}
