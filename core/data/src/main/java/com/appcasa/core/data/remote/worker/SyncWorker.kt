package com.appcasa.core.data.remote.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.appcasa.core.data.remote.FirestoreDataSource
import com.appcasa.core.domain.repository.FamilyRepository
import com.appcasa.core.domain.repository.FinanceRepository
import com.appcasa.core.domain.repository.TasksRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val tasksRepository: TasksRepository,
    private val financeRepository: FinanceRepository,
    private val familyRepository: FamilyRepository,
    private val firestoreDataSource: FirestoreDataSource
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val hogarId = inputData.getLong("hogarId", -1L)
        if (hogarId == -1L) return Result.failure()

        return try {
            // Sincronizar Tareas
            tasksRepository.getTasksByHogar(hogarId).first().forEach { 
                firestoreDataSource.syncTask(it) 
            }
            
            // Sincronizar Gastos
            financeRepository.getExpensesByHogar(hogarId).first().forEach {
                firestoreDataSource.syncExpense(it)
            }

            // Sincronizar Miembros
            familyRepository.getMembersByHogar(hogarId).first().forEach {
                firestoreDataSource.syncMember(it)
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
