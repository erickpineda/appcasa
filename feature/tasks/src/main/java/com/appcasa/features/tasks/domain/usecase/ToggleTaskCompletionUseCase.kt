package com.appcasa.features.tasks.domain.usecase

import com.appcasa.core.domain.model.EstadoTarea
import com.appcasa.core.domain.model.Periodicidad
import com.appcasa.core.domain.model.Task
import com.appcasa.core.domain.repository.TasksRepository
import com.appcasa.core.domain.scheduler.ReminderScheduler
import javax.inject.Inject

class ToggleTaskCompletionUseCase @Inject constructor(
    private val repository: TasksRepository,
    private val awardTaskPointsUseCase: AwardTaskPointsUseCase,
    private val spawnNextTaskInstanceUseCase: SpawnNextTaskInstanceUseCase,
    private val reminderScheduler: ReminderScheduler
) {
    suspend operator fun invoke(task: Task): Int {
        val nuevoEstado = if (task.estado == EstadoTarea.COMPLETADA) {
            EstadoTarea.PENDIENTE
        } else {
            EstadoTarea.COMPLETADA
        }
        
        val isMarkingAsCompleted = nuevoEstado == EstadoTarea.COMPLETADA
        
        val updatedTask = task.copy(
            estado = nuevoEstado,
            completadoEn = if (isMarkingAsCompleted) System.currentTimeMillis() else null,
            updatedAt = System.currentTimeMillis()
        )
        
        repository.upsertTask(updatedTask)
        
        var pointsGained = 0
        
        if (isMarkingAsCompleted) {
            reminderScheduler.cancelReminder((task.id + 20000).hashCode())
            pointsGained = awardTaskPointsUseCase(updatedTask)
            if (task.periodicidad != Periodicidad.NINGUNA) {
                spawnNextTaskInstanceUseCase(updatedTask)
            }
        }
        
        return pointsGained
    }
}
