package com.appcasa.features.tasks.domain.usecase

import com.appcasa.core.domain.model.EstadoTarea
import com.appcasa.core.domain.model.Periodicidad
import com.appcasa.core.domain.model.Task
import com.appcasa.core.domain.repository.TasksRepository
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject

class SpawnNextTaskInstanceUseCase @Inject constructor(
    private val repository: TasksRepository
) {
    suspend operator fun invoke(task: Task) {
        if (task.periodicidad == Periodicidad.NINGUNA) return

        val nextDate = calculateNextDate(task.fechaLimite ?: System.currentTimeMillis(), task.periodicidad)
        
        val nextTaskId = repository.upsertTask(
            task.copy(
                id = "",
                estado = EstadoTarea.PENDIENTE,
                fechaLimite = nextDate,
                completadoEn = null,
                puntosOtorgados = false,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
        
        val subTasks = repository.getCheckItemsForTask(task.id).first()
        subTasks.forEach { sub ->
            repository.upsertCheckItem(
                sub.copy(
                    id = "",
                    tareaId = nextTaskId,
                    completado = false
                )
            )
        }
    }

    private fun calculateNextDate(currentDate: Long, periodicidad: Periodicidad): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = currentDate }
        when (periodicidad) {
            Periodicidad.DIARIA -> cal.add(Calendar.DAY_OF_YEAR, 1)
            Periodicidad.SEMANAL -> cal.add(Calendar.WEEK_OF_YEAR, 1)
            Periodicidad.QUINCENAL -> cal.add(Calendar.DAY_OF_YEAR, 15)
            Periodicidad.MENSUAL -> cal.add(Calendar.MONTH, 1)
            Periodicidad.TRIMESTRAL -> cal.add(Calendar.MONTH, 3)
            Periodicidad.ANUAL -> cal.add(Calendar.YEAR, 1)
            else -> {}
        }
        return cal.timeInMillis
    }
}
