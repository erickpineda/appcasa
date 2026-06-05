package com.appcasa.features.tasks.domain.usecase

import com.appcasa.core.domain.model.*
import com.appcasa.core.domain.repository.TasksRepository
import com.appcasa.core.domain.scheduler.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetArchivedTasksUseCase @Inject constructor(
    private val repository: TasksRepository
) {
    operator fun invoke(hogarId: Long, page: Int): Flow<List<Task>> {
        return repository.getArchivedTasksPaged(hogarId, limit = page * 20, offset = 0)
    }
}

class GetTaskByIdUseCase @Inject constructor(
    private val repository: TasksRepository
) {
    operator fun invoke(taskId: Long): Flow<Task?> {
        return repository.getTaskById(taskId)
    }
}

class DeleteTaskUseCase @Inject constructor(
    private val repository: TasksRepository,
    private val reminderScheduler: ReminderScheduler
) {
    suspend operator fun invoke(task: Task) {
        repository.deleteTask(task)
        reminderScheduler.cancelReminder((task.id + 20000).toInt())
    }
}

class ArchiveTaskUseCase @Inject constructor(
    private val repository: TasksRepository,
    private val reminderScheduler: ReminderScheduler
) {
    suspend operator fun invoke(task: Task) {
        repository.insertTask(task.copy(archived = true))
        reminderScheduler.cancelReminder((task.id + 20000).toInt())
    }
}

class UnarchiveTaskUseCase @Inject constructor(
    private val repository: TasksRepository
) {
    suspend operator fun invoke(taskId: Long) {
        repository.unarchiveTarea(taskId)
    }
}

class ClearAllArchivedTasksUseCase @Inject constructor(
    private val repository: TasksRepository
) {
    suspend operator fun invoke(hogarId: Long) {
        repository.deleteAllArchivedTasks(hogarId)
    }
}

class GetSubTaskCountsUseCase @Inject constructor(
    private val repository: TasksRepository
) {
    operator fun invoke(hogarId: Long): Flow<Map<Long, Pair<Int, Int>>> {
        return repository.getAllCheckItemsCounts(hogarId)
    }
}

class AddTaskUseCase @Inject constructor(
    private val repository: TasksRepository,
    private val reminderScheduler: ReminderScheduler
) {
    suspend operator fun invoke(
        hogarId: Long,
        titulo: String,
        prioridad: Prioridad = Prioridad.MEDIA,
        asignadoId: Long? = null,
        esPersonal: Boolean = false,
        fotoUri: String? = null,
        fechaLimite: Long? = null,
        anticipacionMins: Int = 0,
        periodicidad: Periodicidad = Periodicidad.NINGUNA,
        tipoContenido: TipoContenidoTarea = TipoContenidoTarea.LISTA,
        createdById: Long? = null
    ) {
        val tareaId = repository.insertTask(
            Task(
                hogarId = hogarId,
                titulo = titulo,
                prioridad = prioridad,
                tipoContenido = tipoContenido,
                esPersonal = esPersonal,
                fotoUri = fotoUri,
                fechaLimite = fechaLimite,
                periodicidad = periodicidad,
                anticipacionMins = anticipacionMins,
                createdById = createdById
            )
        )
        
        if (asignadoId != null) {
            repository.insertAssignment(TaskAssignment(tareaId = tareaId, miembroId = asignadoId))
        }

        fechaLimite?.let { deadline ->
            val scheduledTime = deadline - (anticipacionMins * 60 * 1000)
            if (scheduledTime > System.currentTimeMillis()) {
                reminderScheduler.scheduleReminder(
                    id = (tareaId + 20000).toInt(),
                    title = "Tarea próxima: $titulo",
                    message = if (anticipacionMins > 0) "Aviso: En $anticipacionMins minutos vence tu tarea" else "Tienes una tarea que vence hoy",
                    timeInMillis = scheduledTime
                )
            }
        }
    }
}

class UpdateTaskUseCase @Inject constructor(
    private val repository: TasksRepository,
    private val reminderScheduler: ReminderScheduler
) {
    suspend operator fun invoke(
        task: Task,
        nuevoTitulo: String? = null,
        nuevaDescripcion: String? = null,
        nuevaPrioridad: Prioridad? = null,
        nuevoEsPersonal: Boolean? = null,
        nuevaFotoUri: String? = null,
        nuevaFechaLimite: Long? = null,
        nuevaAnticipacionMins: Int? = null,
        nuevaPeriodicidad: Periodicidad? = null,
        nuevoTipoContenido: TipoContenidoTarea? = null
    ) {
        val updated = task.copy(
            titulo = nuevoTitulo ?: task.titulo,
            descripcion = nuevaDescripcion ?: task.descripcion,
            prioridad = nuevaPrioridad ?: task.prioridad,
            esPersonal = nuevoEsPersonal ?: task.esPersonal,
            fotoUri = nuevaFotoUri ?: task.fotoUri,
            fechaLimite = nuevaFechaLimite ?: task.fechaLimite,
            anticipacionMins = nuevaAnticipacionMins ?: task.anticipacionMins,
            periodicidad = nuevaPeriodicidad ?: task.periodicidad,
            tipoContenido = nuevoTipoContenido ?: task.tipoContenido,
            updatedAt = System.currentTimeMillis()
        )
        repository.insertTask(updated)

        updated.fechaLimite?.let { deadline ->
            val scheduledTime = deadline - (updated.anticipacionMins * 60 * 1000)
            if (scheduledTime > System.currentTimeMillis()) {
                reminderScheduler.scheduleReminder(
                    id = (updated.id + 20000).toInt(),
                    title = "Tarea próxima: ${updated.titulo}",
                    message = if (updated.anticipacionMins > 0) "Aviso: En ${updated.anticipacionMins} minutos vence tu tarea" else "Tienes una tarea que vence pronto",
                    timeInMillis = scheduledTime
                )
            }
        } ?: run {
            reminderScheduler.cancelReminder((updated.id + 20000).toInt())
        }
    }
}

class ArchiveOldTasksUseCase @Inject constructor(
    private val repository: TasksRepository
) {
    suspend operator fun invoke(hogarId: Long) {
        val threshold = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        repository.archiveOldCompletedTasks(hogarId, threshold)
    }
}

class GetTaskAssignmentsUseCase @Inject constructor(
    private val repository: TasksRepository
) {
    operator fun invoke(taskId: Long): Flow<List<TaskAssignment>> {
        return repository.getAssignmentsForTask(taskId)
    }
}

class GetTaskCheckItemsUseCase @Inject constructor(
    private val repository: TasksRepository
) {
    operator fun invoke(taskId: Long): Flow<List<TaskCheckItem>> {
        return repository.getCheckItemsForTask(taskId)
    }
}

class AddTaskCheckItemUseCase @Inject constructor(
    private val repository: TasksRepository
) {
    suspend operator fun invoke(taskId: Long, text: String, order: Int) {
        repository.insertCheckItem(TaskCheckItem(tareaId = taskId, texto = text, orden = order))
    }
}

class UpdateTaskCheckItemUseCase @Inject constructor(
    private val repository: TasksRepository
) {
    suspend operator fun invoke(item: TaskCheckItem) {
        repository.updateCheckItem(item)
    }
}

class DeleteTaskCheckItemUseCase @Inject constructor(
    private val repository: TasksRepository
) {
    suspend operator fun invoke(item: TaskCheckItem) {
        repository.deleteCheckItem(item)
    }
}

class BulkDeleteTaskCheckItemsUseCase @Inject constructor(
    private val repository: TasksRepository
) {
    suspend operator fun invoke(items: List<TaskCheckItem>) {
        items.forEach { repository.deleteCheckItem(it) }
    }
}

class BulkUpdateTaskCheckItemsUseCase @Inject constructor(
    private val repository: TasksRepository
) {
    suspend operator fun invoke(items: List<TaskCheckItem>, completed: Boolean) {
        items.forEach { repository.updateCheckItem(it.copy(completado = completed)) }
    }
}
